# Auto-detect a *running* local SQL Server TCP port which contains exam_management.
# Windows PowerShell 5.1 ships with System.Data.SqlClient, so no new dependencies.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$jar = Join-Path $PSScriptRoot 'target\exam-management-backend-1.0.0.jar'
if (-not (Test-Path -LiteralPath $jar)) {
    Write-Host '[ERROR] Khong tim thay file backend JAR:' -ForegroundColor Red
    Write-Host $jar
    exit 1
}

$dbName = 'exam_management'
$dbUser = if ($env:EXAM_DB_USER) { $env:EXAM_DB_USER } else { 'sa' }
$dbPasswords = if ($null -ne $env:EXAM_DB_PASSWORD -and $env:EXAM_DB_PASSWORD -ne '') {
    @($env:EXAM_DB_PASSWORD)
} else {
    @('123456', '123123', '12345678', 'sa', 'admin', 'Admin@123', '')
}

# Explicit configuration always takes precedence (remote SQL Server, custom ports, etc.)
if (-not $env:EXAM_DB_JDBC_URL) {
    # Each candidate has an SQL Client TCP target and a JDBC endpoint.
    $candidates = New-Object System.Collections.ArrayList
    $added = @{}
    function Add-Candidate([string] $dataSource, [string] $jdbcEndpoint) {
        if (-not $script:added.ContainsKey($dataSource)) {
            $script:added[$dataSource] = $true
            [void] $script:candidates.Add([pscustomobject]@{ DataSource = $dataSource; JdbcEndpoint = $jdbcEndpoint })
        }
    }

    # If specified, try the requested SQL Server TCP port first.
    if ($env:EXAM_DB_PORT -and $env:EXAM_DB_PORT -match '^\d{1,5}$') {
        $p = [int] $env:EXAM_DB_PORT
        if ($p -ge 1 -and $p -le 65535) {
            Add-Candidate "tcp:127.0.0.1,$p" "127.0.0.1:$p"
        }
    }

    # Find which ports belong to the *running SQL Server instances* rather
    # than assuming the old 52446 dynamic port still exists after reboot.
    try {
        $services = @(Get-CimInstance Win32_Service -ErrorAction Stop | Where-Object {
            $_.State -eq 'Running' -and ($_.Name -eq 'MSSQLSERVER' -or $_.Name -like 'MSSQL$*')
        })
        $pids = @($services | ForEach-Object { [int] $_.ProcessId })
        $listeners = @(Get-NetTCPConnection -State Listen -ErrorAction Stop |
            Where-Object { $pids -contains [int] $_.OwningProcess } |
            Sort-Object -Property LocalPort -Unique)
        foreach ($listener in $listeners) {
            $p = [int] $listener.LocalPort
            if ($p -ge 1 -and $p -le 65535) {
                Add-Candidate "tcp:127.0.0.1,$p" "127.0.0.1:$p"
            }
        }
    }
    catch {
        Write-Host '[SQL] Khong doc duoc cong tu Windows; se thu cac cong/instance pho bien.' -ForegroundColor Yellow
    }

    # Fallback: typical default SQL Server instance and legacy project port.
    Add-Candidate 'tcp:127.0.0.1,1433' '127.0.0.1:1433'
    Add-Candidate 'tcp:127.0.0.1,52446' '127.0.0.1:52446'
    # SQL Browser can resolve named SQLEXPRESS even when it uses a dynamic port.
    Add-Candidate 'tcp:localhost\SQLEXPRESS' 'localhost\SQLEXPRESS'

    $selected = $null
    $selectedPassword = $null
    $failures = New-Object System.Collections.ArrayList
    Write-Host '[SQL] Dang tim SQL Server chua database exam_management...' -ForegroundColor Cyan
    foreach ($candidate in $candidates) {
        foreach ($pw in $dbPasswords) {
            $connection = $null
            try {
                $builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
                $builder['Data Source'] = $candidate.DataSource
                $builder['Initial Catalog'] = $dbName
                $builder['User ID'] = $dbUser
                $builder['Password'] = $pw
                $builder['Integrated Security'] = $false
                $builder['Connect Timeout'] = 2
                $builder['Encrypt'] = $false
                $builder['TrustServerCertificate'] = $true
                $connection = New-Object System.Data.SqlClient.SqlConnection($builder.ConnectionString)
                $connection.Open()
                $command = $connection.CreateCommand()
                $command.CommandText = 'SELECT DB_NAME()'
                $actualDatabase = [string] $command.ExecuteScalar()
                if ($actualDatabase -eq $dbName) {
                    $selected = $candidate
                    $selectedPassword = $pw
                    break
                }
            }
            catch {
                $message = $_.Exception.Message -replace '[\r\n]+', ' '
                [void] $failures.Add("$($candidate.DataSource) : $message")
                if ($message -notmatch 'Login failed') {
                    break
                }
            }
            finally {
                if ($null -ne $connection) { $connection.Dispose() }
            }
        }
        if ($null -ne $selected) { break }
    }

    if ($null -eq $selected) {
        Write-Host ''
        Write-Host '[ERROR] Khong the ket noi SQL Server voi tai khoan sa hoac khong tim thay database exam_management.' -ForegroundColor Red
        Write-Host 'Hay kiem tra:' -ForegroundColor Yellow
        Write-Host '  1. SSMS: dang nhap dung instance va kiem tra database exam_management.'
        Write-Host '  2. SQL Server Configuration Manager: bat TCP/IP va restart dung SQL Server service.'
        Write-Host '  3. SQL Server Authentication (Mixed Mode) phai bat va tai khoan sa phai enabled.'
        Write-Host '  4. Tai khoan va mat khau local phai dung; neu da doi, su dung EXAM_DB_USER / EXAM_DB_PASSWORD.'
        if ($failures.Count -gt 0) {
            Write-Host 'Chi tiet (khong chua mat khau):' -ForegroundColor Yellow
            $failures | Select-Object -First 8 | ForEach-Object { Write-Host "  $_" }
        }
        Write-Host 'Khong tu dong chay file .sql vi script goc CO LENH DROP TABLE.' -ForegroundColor Yellow
        exit 1
    }

    $env:EXAM_DB_JDBC_URL = "jdbc:sqlserver://$($selected.JdbcEndpoint);databaseName=$dbName;encrypt=false;trustServerCertificate=true"
    $env:EXAM_DB_PASSWORD = $selectedPassword
    Write-Host "[SQL] Da xac nhan ket noi: $($selected.DataSource) / database: $dbName" -ForegroundColor Green
}
else {
    Write-Host '[SQL] Dang dung EXAM_DB_JDBC_URL do ban cau hinh.' -ForegroundColor Cyan
}

# Driver gets user/password from application.properties or environment overrides.
$localJava = Join-Path $PSScriptRoot 'jre\bin\java.exe'
$javaExe = if (Test-Path -LiteralPath $localJava) { $localJava } else { 'java' }
Write-Host "[BACKEND] Khoi dong Spring Boot tai http://localhost:5000 (Java: $javaExe) ..." -ForegroundColor Cyan
& $javaExe -jar $jar
exit $LASTEXITCODE
