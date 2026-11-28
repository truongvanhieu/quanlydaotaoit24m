================================================================================
HỆ THỐNG QUẢN LÝ ĐÀO TẠO ĐẠI HỌC - IT24M (PHÂN HỆ NHÓM N3)
QUẢN LÝ GIẢNG VIÊN, NHÂN SỰ, KHOA/VIỆN, CHỨC VỤ & VỊ TRÍ CÔNG TÁC
================================================================================

1. THÔNG TIN DỰ ÁN & LINK GITHUB
--------------------------------------------------------------------------------
- Tên dự án        : Hệ thống Quản lý Đào tạo Đại học (quanlydaotaoit24m)
- Phân hệ phụ trách: Nhóm N3 - Quản lý Giảng viên & Nhân sự
- Link GitHub      : https://github.com/truongvanhieu/quanlydaotaoit24m.git
- Nhánh phát triển : nhom-3
- Công nghệ sử dụng:
  + Backend  : Java 21, Spring Boot 4.1.1, Spring Web, Spring Data JPA
  + API Docs : Springdoc OpenAPI / Swagger UI 2.8.13
  + Frontend : HTML5, CSS3, Bootstrap 5.3, Bootstrap Icons, Vanilla JavaScript
  + Database : Microsoft SQL Server (CSDL: it24m)

2. YÊU CẦU MÔI TRƯỜNG CÀI ĐẶT
--------------------------------------------------------------------------------
- JDK 21 trở lên (kiểm tra bằng lệnh: java -version)
- Microsoft SQL Server (2019 / 2022 / Express) + SSMS (SQL Server Management Studio)
- Git (để clone mã nguồn)
- Maven (không bắt buộc cài sẵn vì dự án đã kèm Maven Wrapper: mvnw.cmd / mvnw)
- IDE khuyên dùng: IntelliJ IDEA hoặc Visual Studio Code (Extension Pack for Java)

3. CẤU HÌNH & TÀI KHOẢN SQL SERVER
--------------------------------------------------------------------------------
Thông tin kết nối cấu hình trong file: src/main/resources/application.properties

- Host / Port : localhost:1433
- Database    : it24m
- Username    : sa
- Password    : 123123
- JDBC URL    : jdbc:sqlserver://localhost:1433;databaseName=it24m;encrypt=true;trustServerCertificate=true
- File script : database/it24mN3.sql (hoặc database/it24m.sql)

* Lưu ý thiết lập SQL Server trước khi chạy:
  1) Bật chế độ xác thực "SQL Server and Windows Authentication mode" (Mixed Mode)
     và kích hoạt tài khoản đăng nhập "sa" với mật khẩu "123123" (hoặc đổi lại
     username/password tương ứng trong file src/main/resources/application.properties).
  2) Mở SQL Server Configuration Manager -> SQL Server Network Configuration ->
     Protocols for SQLEXPRESS/MSSQLSERVER -> Bật TCP/IP -> Cấu hình TCP Port = 1433
     -> Khởi động lại dịch vụ SQL Server.
  3) Mở SSMS, tạo mới database tên "it24m", sau đó mở và chạy file:
     database/it24mN3.sql để khởi tạo đầy đủ các bảng và dữ liệu mẫu.

4. HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN
--------------------------------------------------------------------------------
Bước 1: Clone mã nguồn từ GitHub và chuyển sang nhánh nhom-3
    git clone https://github.com/truongvanhieu/quanlydaotaoit24m.git
    cd quanlydaotaoit24m
    git checkout nhom-3

Bước 2: Tạo CSDL và import dữ liệu
    - Tạo database [it24m] trong SQL Server.
    - Chạy script database/it24m.sql.
    - Kiểm tra lại tài khoản SQL Server trong src/main/resources/application.properties.

Bước 3: Chạy ứng dụng Spring Boot
  Cách 1 - Chạy bằng dòng lệnh trên Windows (PowerShell / CMD):
    .\mvnw.cmd spring-boot:run

  Cách 2 - Chạy trên macOS / Linux:
    ./mvnw spring-boot:run

  Cách 3 - Chạy trực tiếp trong IDE (VS Code / IntelliJ IDEA):
    Mở file src/main/java/com/example/quanlydaotao/QuanlydaotaoApplication.java
    và nhấn Run.

Bước 4: Truy cập ứng dụng sau khi server khởi động thành công (cổng 8080)
    - Giao diện quản trị Nhóm N3 : http://localhost:8080/n3/index.html
    - Trang tài liệu Swagger UI  : http://localhost:8080/swagger-ui.html
    - OpenAPI JSON Nhóm N3       : http://localhost:8080/v3/api-docs/n3

5. DANH SÁCH CHỨC NĂNG PHÂN HỆ NHÓM N3
--------------------------------------------------------------------------------
5.1. Quản lý Khoa / Viện / Phòng ban (Bảng: departments)
    - API: GET / POST / PUT / DELETE /api/n3/departments
    - Hiển thị danh sách đang hoạt động, xem chi tiết (chỉ đọc), thêm mới,
      cập nhật (tự động cập nhật updated_at), quản lý trạng thái hiệu lực
      (is_active: Hoạt động / Tạm khóa), tìm kiếm theo mã/tên, xóa mềm
      (deleted_at, is_active = 0), chống trùng mã và thống kê tổng số đơn vị.

5.2. Quản lý Chức vụ / Chức danh (Bảng: positions)
    - API: GET / POST / PUT / DELETE /api/n3/positions
    - Hiển thị danh sách kèm tên Khoa/Viện trực thuộc, thêm mới, thiết lập
      cấp bậc (level - cho phép NULL), gán Khoa/Viện quản lý (department_id),
      cập nhật, xem chi tiết, tìm kiếm theo mã/tên/cấp bậc và xóa mềm.

5.3. Quản lý Giảng viên - Nhân viên (Bảng: employees, lecturers, users)
    - API Nhân viên  : GET / POST / PUT / DELETE /api/n3/employees
                       GET /api/n3/employees/department/{departmentId}
    - API Giảng viên : GET / POST / PUT / DELETE /api/n3/lecturers
    - API Tài khoản  : GET /api/n3/users (hỗ trợ liên kết tài khoản user_id)
    - Quản lý đầy đủ hồ sơ nhân sự: mã NV, họ tên, thông tin cá nhân & liên hệ,
      Khoa/Viện, chức vụ chính, liên kết tài khoản (user_id), ngày tuyển dụng
      (hire_date), loại hợp đồng, hệ số lương (0 - 99.99), học vị, học hàm,
      chuyên môn; tìm kiếm nhanh theo mã, họ tên, email, SĐT, chuyên môn;
      kiểm tra trùng mã (bỏ qua bản ghi đã xóa mềm) và xóa mềm lưu lịch sử.
    * Lưu ý CSDL: Thời gian bắt đầu/kết thúc vị trí công tác (start_date,
      end_date) được quản lý chi tiết tại bảng employee_positions (mục 5.4).

5.4. Phân công & Quản lý Vị trí công tác (Bảng: employee_positions)
    - API: GET / POST / PUT / DELETE /api/n3/employee-positions
           GET /api/n3/employee-positions/employee/{employeeId}
           GET /api/n3/employee-positions/employee/{employeeId}/history
           PUT /api/n3/employee-positions/{id}/end (Kết thúc nhiệm kỳ)
    - Hiển thị danh sách phân công (tự động map tên nhân viên & tên chức vụ),
      chọn nhân viên từ bảng employees, chọn chức vụ từ bảng positions, thiết lập
      vị trí đang đương nhiệm (end_date = NULL), phân công kiêm nhiệm nhiều vị
      trí cho cùng 1 nhân viên, cập nhật ngày kết thúc nhiệm kỳ (validate
      end_date >= start_date), lọc/xem lịch sử công tác theo nhân viên dạng
      Timeline và xóa mềm.

5.5. Lịch sử hoạt động, Lịch sử chức vụ GV & Dữ liệu đã xóa mềm
    - Nhật ký Thêm/Sửa/Xóa/Cập nhật (Bảng: logs):
      + API: GET /api/n3/audit-logs (lọc ?tableName=, ?action=, ?keyword=)
             GET /api/n3/audit-logs/record/{recordId}
      + Tự động ghi nhận mọi thao tác Thêm, Cập nhật (cũ -> mới), Xóa mềm,
        Khôi phục và Kết thúc nhiệm kỳ trên toàn bộ 6 phân hệ N3.
    - Lịch sử chức vụ Giảng viên (Bảng: lecturer_positions_history):
      + API: GET / POST / PUT / DELETE /api/n3/lecturer-positions-history
             GET /api/n3/lecturer-positions-history/lecturer/{lecturerId}
             PUT /api/n3/lecturer-positions-history/{id}/end
    - Dữ liệu đã xóa mềm & Khôi phục (cho cả 6 phân hệ N3):
      + API: GET /api/n3/{resource}/deleted và PUT /api/n3/{resource}/{id}/restore

6. CẤU TRÚC MÃ NGUỒN PHẦN NHÓM N3
--------------------------------------------------------------------------------
src/main/java/com/example/quanlydaotao/n3/
  ├── config/
  │   ├── SwaggerConfig.java                      (Cấu hình Swagger nhóm API N3)
  │   └── N3ExceptionHandler.java                 (Chuẩn hóa JSON thông báo lỗi N3)
  ├── controller/
  │   ├── DepartmentController.java               (/api/n3/departments)
  │   ├── PositionController.java                 (/api/n3/positions)
  │   ├── LecturerController.java                 (/api/n3/lecturers)
  │   ├── EmployeeController.java                 (/api/n3/employees)
  │   ├── EmployeePositionController.java         (/api/n3/employee-positions)
  │   ├── LecturerPositionHistoryController.java  (/api/n3/lecturer-positions-history)
  │   ├── N3AuditLogController.java               (/api/n3/audit-logs)
  │   └── UserLookupController.java               (/api/n3/users)
  ├── entity/
  │   ├── Department.java, Position.java, Lecturer.java, Employee.java,
  │   ├── EmployeePosition.java, LecturerPositionHistory.java,
  │   └── N3User.java, N3AuditLog.java
  ├── repository/
  │   └── Các JPA Repository tương ứng cho 8 entity trên
  └── service/
      └── Xử lý nghiệp vụ, kiểm tra dữ liệu, xóa mềm và ghi nhật ký N3

src/main/resources/static/n3/
  ├── index.html                          (Giao diện quản trị Nhóm N3)
  ├── app.js                              (Xử lý REST API, bảng, modal, lọc)
  └── style.css                           (Giao diện CSS)

7. XỬ LÝ SỰ CỐ THƯỜNG GẶP
--------------------------------------------------------------------------------
- Lỗi "Port 8080 was already in use":
  Tắt tiến trình đang chiếm cổng 8080 hoặc đổi server.port trong application.properties.
- Lỗi "The TCP/IP connection to the host localhost, port 1433 has failed":
  Kiểm tra SQL Server Service đang chạy và đã bật TCP/IP port 1433.
- Lỗi "Login failed for user 'sa'":
  Kiểm tra lại mật khẩu tài khoản sa trong SQL Server và application.properties.
================================================================================
