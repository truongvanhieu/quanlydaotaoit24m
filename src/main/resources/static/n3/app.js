
/*
 * N3 - Quan ly giang vien va nhan su
 * Spring Boot + SQL Server it24m
 *
 * Dung cho:
 * /n3/index.html
 * /index.html?module=n3&tab=...
 */

(() => {
    "use strict";

    const BASE = "/api/n3/";
    const $ = id => document.getElementById(id);

    const types = [
        "lecturers",
        "departments",
        "positions",
        "employees",
        "employee-positions"
    ];

    const F = (
        name,
        label,
        type = "text",
        options = {}
    ) => ({
        name,
        label,
        type,
        ...options
    });

    // ===================================
    // 1. CAU HINH CAC CHUC NANG
    // ===================================

    const C = {

        // QUAN LY GIANG VIEN
        lecturers: {
            title: "Quản lý giảng viên",
            description: "Quản lý hồ sơ giảng viên",

            columns: [
                "lecturerCode",
                "fullName",
                "gender",
                "email",
                "phone",
                "departmentId",
                "positionId",
                "isActive"
            ],

            fields: [
                F("lecturerCode", "Mã giảng viên", "text", {
                    required: true,
                    maxlength: 20
                }),

                F("fullName", "Họ tên", "text", {
                    required: true,
                    maxlength: 100
                }),

                F("gender", "Giới tính", "gender"),

                F("dateOfBirth", "Ngày sinh", "date"),

                F("email", "Email", "email", {
                    maxlength: 100
                }),

                F("phone", "Số điện thoại", "tel", {
                    maxlength: 15
                }),

                F("departmentId", "Khoa / viện", "department", {
                    required: true
                }),

                F("positionId", "Chức danh", "position"),

                F("hireDate", "Ngày vào làm", "date"),

                F("isActive", "Trạng thái", "status")
            ]
        },

        // QUAN LY KHOA / VIEN
        departments: {
            title: "Quản lý khoa / viện",
            description: "Danh mục khoa, viện, phòng ban",

            columns: [
                "code",
                "name",
                "description",
                "establishedYear",
                "isActive"
            ],

            fields: [
                F("code", "Mã khoa", "text", {
                    required: true,
                    maxlength: 100
                }),

                F("name", "Tên khoa / viện", "text", {
                    required: true,
                    maxlength: 255
                }),

                F("description", "Mô tả", "textarea", {
                    maxlength: 255
                }),

                F("establishedYear", "Ngày thành lập", "date"),

                F("isActive", "Trạng thái", "status")
            ]
        },

        // QUAN LY CHUC DANH
        positions: {
            title: "Quản lý chức danh",
            description: "Danh mục chức vụ và vị trí",

            columns: [
                "code",
                "name",
                "level",
                "departmentId",
                "isActive"
            ],

            fields: [
                F("code", "Mã chức danh", "text", {
                    required: true,
                    maxlength: 100
                }),

                F("name", "Tên chức danh", "text", {
                    required: true,
                    maxlength: 255
                }),

                F("description", "Mô tả", "textarea", {
                    maxlength: 255
                }),

                F("level", "Cấp bậc", "text", {
                    maxlength: 100
                }),

                F("departmentId", "Khoa / viện", "department"),

                F("isActive", "Trạng thái", "status")
            ]
        },

        // QUAN LY NHAN VIEN
        employees: {
            title: "Quản lý nhân viên",

            description:
                "Giảng viên, giáo vụ, nhân viên và thông tin công tác",

            columns: [
                "code",
                "fullName",
                "gender",
                "email",
                "departmentId",
                "positionId",
                "academicDegree",
                "isActive"
            ],

            fields: [
                F("code", "Mã nhân viên", "text", {
                    required: true,
                    maxlength: 100
                }),

                F("fullName", "Họ tên", "text", {
                    required: true,
                    maxlength: 255
                }),

                F("dateOfBirth", "Ngày sinh", "date"),

                F("gender", "Giới tính", "gender"),

                F("email", "Email", "email", {
                    maxlength: 100
                }),

                F("phone", "Số điện thoại", "tel", {
                    maxlength: 50
                }),

                F("address", "Địa chỉ", "text", {
                    maxlength: 255,
                    wide: true
                }),

                F("departmentId", "Khoa / viện", "department"),

                // SQL employees.position_id = NVARCHAR(100)
                // Cho phep chon ma chuc vu tu bang positions hoac nhap ma (vi du: GV)
                F("positionId", "Chức vụ chính (mã vị trí)", "positionCode", {
                    maxlength: 100
                }),

                F("userId", "Tài khoản liên kết (user_id)", "user"),

                F("hireDate", "Ngày tuyển dụng", "datetime-local"),

                F("contractType", "Loại hợp đồng", "text", {
                    maxlength: 255
                }),

                F("salaryCoefficient", "Hệ số lương", "number", {
                    min: 0,
                    max: 99.99,
                    step: 0.01
                }),

                F("academicDegree", "Học vị", "text", {
                    maxlength: 50
                }),

                F("academicTitle", "Học hàm", "text", {
                    maxlength: 50
                }),

                F("specialization", "Chuyên môn", "text", {
                    maxlength: 255,
                    wide: true
                }),

                F("isActive", "Trạng thái", "status")
            ]
        },

        // LICH SU VI TRI CONG TAC
        "employee-positions": {
            title: "Vị trí công tác",

            description:
                "Phân công và lịch sử vị trí của nhân viên",

            columns: [
                "employeeId",
                "positionId",
                "startDate",
                "endDate",
                "note",
                "isActive"
            ],

            fields: [
                F("employeeId", "Nhân viên", "employee", {
                    required: true
                }),

                F("positionId", "Chức vụ", "position", {
                    required: true
                }),

                F("startDate", "Ngày bắt đầu", "datetime-local", {
                    required: true
                }),

                F("endDate", "Ngày kết thúc", "datetime-local"),

                F("description", "Mô tả", "textarea", {
                    maxlength: 255,
                    wide: true
                }),

                F("note", "Ghi chú", "textarea", {
                    maxlength: 255,
                    wide: true
                }),

                F("isActive", "Trạng thái", "status")
            ]
        }
    };

    // ===================================
    // 2. BIEN TRANG THAI
    // ===================================

    const params = new URLSearchParams(location.search);

    let tab = types.includes(params.get("tab"))
        ? params.get("tab")
        : "lecturers";

    const data = Object.fromEntries(
        types.map(kind => [kind, []])
    );
    data.users = [];

    const errors = {};

    let editId = null;
    let mode = "add";
    let busy = false;
    let selectedEmployeeId = "";
    let employeeHistoryRows = null;

    const modal = bootstrap.Modal.getOrCreateInstance(
        $("formModal")
    );

    // ===================================
    // 3. HAM HO TRO
    // ===================================

    const escapeHTML = value => String(
        value ?? ""
    ).replace(
        /[&<>"']/g,
        character => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        }[character])
    );

    const text = (id, value) => {
        const element = $(id);

        if (element) {
            element.textContent = value;
        }
    };

    function notice(message = "", success = false) {
        const box = $("pageMessage");

        if (!box) return;

        box.className = success
            ? "mt-2 text-success"
            : "mt-2 text-danger";

        box.textContent = message;
    }

    // ===================================
    // 4. GOI REST API
    // ===================================

    async function api(path, method = "GET", body) {

        const response = await fetch(BASE + path, {
            method,

            headers: {
                Accept: "application/json",

                ...(body === undefined
                    ? {}
                    : {
                        "Content-Type": "application/json"
                    })
            },

            ...(body === undefined
                ? {}
                : {
                    body: JSON.stringify(body)
                })
        });

        if (!response.ok) {
            let error = `HTTP ${response.status}`;

            try {
                const detail = await response.json();

                error += ": " + (
                    detail.detail ||
                    detail.message ||
                    detail.error ||
                    detail.title ||
                    "Lỗi API"
                );

            } catch {
                // API co the tra ve loi khong phai JSON
            }

            throw new Error(error);
        }

        if (response.status === 204) {
            return null;
        }

        const value = await response.text();

        return value ? JSON.parse(value) : null;
    }

    // ===================================
    // 5. HIEN THI DU LIEU LIEN QUAN
    // ===================================

    function related(kind, id) {

        const row = data[kind].find(
            item => String(item.id) === String(id)
        );

        if (!row) {
            return String(id);
        }

        if (kind === "employees") {
            return (
                `${row.code || ""} - ${row.fullName || ""}`
            ).trim();
        }

        if (kind === "users") {
            return (
                `${row.username || ""}${row.fullName ? " - " + row.fullName : ""}`
            ).trim() || String(id);
        }

        return row.name || row.code || id;
    }

    function display(key, value) {

        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return "—";
        }

        if (key === "isActive") {
            return value ? "Hoạt động" : "Tạm khóa";
        }

        if (key === "departmentId") {
            return related("departments", value);
        }

        if (key === "employeeId") {
            return related("employees", value);
        }

        if (key === "userId") {
            return related("users", value);
        }

        if (
            key === "positionId" &&
            tab !== "employees"
        ) {
            return related("positions", value);
        }

        if (
            /Date$|^establishedYear$/.test(key) &&
            typeof value === "string"
        ) {
            return value
                .replace("T", " ")
                .slice(0, 19);
        }

        return String(value).trim() || "—";
    }

    function label(key) {
        return C[tab].fields.find(
            field => field.name === key
        )?.label || key;
    }

    // ===================================
    // 6. THONG KE
    // ===================================

    function counters() {

        const ids = {
            lecturers: "totalLecturers",
            departments: "totalDepartments",
            positions: "totalPositions",
            employees: "totalEmployees",
            "employee-positions": "totalEmployeePositions"
        };

        for (const kind of types) {
            text(
                ids[kind],
                errors[kind]
                    ? "—"
                    : String(data[kind].length)
            );
        }
    }

    // ===================================
    // 7. HIEN THI BANG
    // ===================================

    function render() {

        const cfg = C[tab];

        const query = (
            $("searchInput")?.value || ""
        )
            .trim()
            .toLocaleLowerCase("vi");

        document.title = cfg.title + " | IT24M";

        text("pageTitle", cfg.title);
        text("n3Heading", cfg.title);
        text("pageDescription", cfg.description);

        text(
            "tableTitle",
            cfg.title.replace("Quản lý", "Danh sách")
        );

        const sourceRows = (
            tab === "employee-positions" &&
            selectedEmployeeId &&
            Array.isArray(employeeHistoryRows)
        )
            ? employeeHistoryRows
            : data[tab];

        const searchKeys = Array.from(
            new Set([
                ...cfg.columns,
                ...cfg.fields.map(field => field.name)
            ])
        );

        const rows = sourceRows.filter(row => {

            if (!query) return true;

            return searchKeys.some(key =>
                display(key, row[key])
                    .toLocaleLowerCase("vi")
                    .includes(query)
            );
        });

        text(
            "recordCount",
            `${rows.length} / ${sourceRows.length} bản ghi`
        );

        // Header
        $("tableHead").innerHTML = `
            <tr>
                ${cfg.columns.map(key => `
                    <th>${escapeHTML(label(key))}</th>
                `).join("")}

                <th class="text-end">Thao tác</th>
            </tr>
        `;

        // Bang trong
        if (rows.length === 0) {

            const message = errors[tab]
                ? "Không tải được dữ liệu"
                : query
                    ? "Không có kết quả phù hợp"
                    : "Chưa có bản ghi";

            $("tableBody").innerHTML = `
                <tr>
                    <td colspan="${cfg.columns.length + 1}"
                        class="text-center text-muted py-4">
                        ${message}
                    </td>
                </tr>
            `;

            return;
        }

        // Noi dung bang
        $("tableBody").innerHTML = rows.map(row => `
            <tr>

                ${cfg.columns.map(key => `
                    <td>
                        ${escapeHTML(display(key, row[key]))}
                    </td>
                `).join("")}

                <td class="text-end text-nowrap">

                    <button type="button"
                            class="btn btn-sm btn-outline-info"
                            data-action="view"
                            data-id="${escapeHTML(row.id)}"
                            title="Xem">

                        <i class="bi bi-eye"></i>
                    </button>

                    <button type="button"
                            class="btn btn-sm btn-outline-primary"
                            data-action="edit"
                            data-id="${escapeHTML(row.id)}"
                            title="Sửa">

                        <i class="bi bi-pencil-square"></i>
                    </button>

                    <button type="button"
                            class="btn btn-sm btn-outline-danger"
                            data-action="delete"
                            data-id="${escapeHTML(row.id)}"
                            title="Xóa">

                        <i class="bi bi-trash"></i>
                    </button>

                </td>
            </tr>
        `).join("");
    }

    // ===================================
    // 8. DU LIEU CHO SELECT
    // ===================================

    function optionList(field, selectedDeptId = "") {

        if (field.type === "status") {
            return [
                ["true", "Hoạt động"],
                ["false", "Tạm khóa"]
            ];
        }

        if (field.type === "gender") {
            return [
                ["Nam", "Nam"],
                ["Nữ", "Nữ"],
                ["Khác", "Khác"]
            ];
        }

        if (field.type === "user") {
            return (data.users || []).map(row => [
                row.id,
                `${row.username || ""}${row.fullName ? " - " + row.fullName : ""}`.trim()
            ]);
        }

        const map = {
            department: "departments",
            position: "positions",
            employee: "employees"
        };

        const kind = map[field.type];
        let items = data[kind] || [];

        // Loc chuc danh theo khoa khi them/sua giang vien
        if (
            tab === "lecturers" &&
            field.type === "position" &&
            selectedDeptId
        ) {
            items = items.filter(row =>
                !row.departmentId ||
                String(row.departmentId) === String(selectedDeptId)
            );
        }

        return items.map(row => {

            const name = kind === "employees"
                ? `${row.code || ""} - ${row.fullName || ""}`
                : (
                    (row.code ? row.code + " - " : "") +
                    (row.name || row.id)
                );

            return [row.id, name];
        });
    }

    function renderSelectOptions(field, value, selectedDeptId = "") {
        const options = optionList(field, selectedDeptId);
        return `
            ${field.type === "status"
                ? ""
                : '<option value="">-- Chọn --</option>'}

            ${options.map(([v, name]) => `
                <option value="${escapeHTML(v)}"
                    ${String(v) === value ? "selected" : ""}>
                    ${escapeHTML(name)}
                </option>
            `).join("")}

            ${
                value &&
                !options.some(
                    ([v]) => String(v) === value
                )
                    ? `
                        <option selected
                                value="${escapeHTML(value)}">
                            ${escapeHTML(value)} (đã lưu)
                        </option>
                    `
                    : ""
            }
        `;
    }

    // ===================================
    // 9. TAO FORM DONG
    // ===================================

    function control(field, raw, readonly, row = null) {

        const value = raw == null
            ? (
                field.type === "status"
                    ? "true"
                    : ""
            )
            : String(raw).trim();

        const required =
            field.required && !readonly
                ? "required"
                : "";

        const disabled = readonly ? "disabled" : "";

        const max = field.maxlength
            ? `maxlength="${field.maxlength}"`
            : "";

        let html = "";

        // Select
        if ([
            "status",
            "gender",
            "department",
            "position",
            "employee",
            "user"
        ].includes(field.type)) {

            const selectedDeptId = row?.departmentId
                ? String(row.departmentId)
                : "";

            html = `
                <select name="${field.name}"
                        class="form-select"
                        ${required}
                        ${disabled}>
                    ${renderSelectOptions(field, value, selectedDeptId)}
                </select>
            `;

        } else if (field.type === "positionCode") {

            const positionOptions = (data.positions || []).map(p => {
                const code = (p.code || "").trim();
                const name = (p.name || "").trim();
                return `<option value="${escapeHTML(code)}">${escapeHTML(code)}${name ? " - " + escapeHTML(name) : ""}</option>`;
            }).join("");

            html = `
                <input name="${field.name}"
                       type="text"
                       list="positionCodeList"
                       value="${escapeHTML(value)}"
                       placeholder="Chọn hoặc nhập mã chức vụ (VD: GV, TBM...)"
                       class="form-control"
                       ${required}
                       ${disabled}
                       ${max}>
                <datalist id="positionCodeList">${positionOptions}</datalist>
            `;

        } else if (field.type === "textarea") {

            html = `
                <textarea name="${field.name}"
                          rows="2"
                          class="form-control"
                          ${max}
                          ${required}
                          ${disabled}>${escapeHTML(value)}</textarea>
            `;

        } else {

            const inputValue =
                field.type === "datetime-local"
                    ? value.slice(0, 16)
                    : value;

            const limits = field.type === "number"
                ? `
                    min="${field.min ?? 0}"
                    max="${field.max ?? 99.99}"
                    step="${field.step ?? 0.01}"
                `
                : "";

            html = `
                <input name="${field.name}"
                       type="${field.type}"
                       value="${escapeHTML(inputValue)}"
                       class="form-control"
                       ${required}
                       ${disabled}
                       ${max}
                       ${limits}>
            `;
        }

        return `
            <div class="${field.wide ? "col-12" : "col-md-6"}">

                <label class="form-label">
                    ${escapeHTML(field.label)}

                    ${field.required
                        ? '<span class="text-danger">*</span>'
                        : ""}
                </label>

                ${html}
            </div>
        `;
    }

    // ===================================
    // 10. MO MODAL
    // ===================================

    function openForm(nextMode, row = null) {

        mode = nextMode;
        editId = row?.id || null;

        const action =
            nextMode === "add"
                ? "Thêm"
                : nextMode === "edit"
                    ? "Sửa"
                    : "Xem";

        text(
            "modalTitle",
            `${action} - ${C[tab].title}`
        );

        $("formFields").innerHTML =
            C[tab].fields.map(field =>
                control(
                    field,
                    row?.[field.name],
                    nextMode === "view",
                    row
                )
            ).join("");

        // Dong bo loc chuc danh theo khoa tren form giang vien
        if (tab === "lecturers" && nextMode !== "view") {
            const deptSelect = $("formFields").querySelector('select[name="departmentId"]');
            const posSelect = $("formFields").querySelector('select[name="positionId"]');
            const posField = C.lecturers.fields.find(f => f.name === "positionId");

            if (deptSelect && posSelect && posField) {
                deptSelect.addEventListener("change", () => {
                    const deptId = deptSelect.value;
                    const currentPos = posSelect.value;
                    const validIds = optionList(posField, deptId).map(([v]) => String(v));
                    const nextPos = validIds.includes(currentPos) ? currentPos : "";
                    posSelect.innerHTML = renderSelectOptions(posField, nextPos, deptId);
                });
            }
        }

        $("saveBtn").hidden =
            nextMode === "view";

        modal.show();
    }

    // ===================================
    // 11. LAY DU LIEU FORM
    // ===================================

    function payloadFromForm() {

        const fd = new FormData($("dataForm"));

        const payload = {};

        for (const field of C[tab].fields) {

            let value = String(
                fd.get(field.name) ?? ""
            ).trim();

            if (!value) {
                payload[field.name] = null;
                continue;
            }

            if (field.type === "status") {

                value = value === "true";

            } else if (field.type === "number") {

                value = Number(value);

                if (!Number.isFinite(value)) {
                    throw new Error(
                        `${field.label} phải là số`
                    );
                }

            } else if (
                field.type === "datetime-local" &&
                value.length === 16
            ) {

                value += ":00";
            }

            payload[field.name] = value;
        }

        // Kiem tra lich su cong tac
        if (
            tab === "employee-positions" &&
            payload.endDate &&
            payload.startDate &&
            payload.endDate < payload.startDate
        ) {
            throw new Error(
                "Ngày kết thúc phải sau ngày bắt đầu"
            );
        }

        return payload;
    }

    // ===================================
    // 12. TAI DU LIEU
    // ===================================

    function syncEmployeeFilter() {
        const filter = $("employeeFilter");
        if (!filter) return;

        filter.style.display =
            tab === "employee-positions" ? "" : "none";

        const options = (data.employees || []).map(emp => `
            <option value="${escapeHTML(emp.id)}"
                ${String(emp.id) === String(selectedEmployeeId) ? "selected" : ""}>
                ${escapeHTML((`${emp.code || ""} - ${emp.fullName || ""}`).trim())}
            </option>
        `).join("");

        filter.innerHTML =
            '<option value="">-- Lọc lịch sử theo nhân viên (Tất cả) --</option>' +
            options;
    }

    async function loadEmployeeHistory(empId) {
        selectedEmployeeId = empId || "";

        if (!selectedEmployeeId) {
            employeeHistoryRows = null;
            render();
            notice("");
            return;
        }

        try {
            const rows = await api("employee-positions/employee/" + selectedEmployeeId);
            employeeHistoryRows = Array.isArray(rows) ? rows : [];
            render();
            notice("");
        } catch (error) {
            employeeHistoryRows = [];
            render();
            notice(error.message);
        }
    }

    async function loadAll() {

        // Mot API loi khong lam cac API khac mat du lieu
        const [results, usersResult] = await Promise.all([
            Promise.allSettled(types.map(kind => api(kind))),
            api("users").catch(() => [])
        ]);

        results.forEach((result, index) => {

            const kind = types[index];

            if (
                result.status === "fulfilled" &&
                Array.isArray(result.value)
            ) {

                data[kind] = result.value;
                delete errors[kind];

            } else {

                data[kind] = [];

                errors[kind] =
                    result.status === "rejected"
                        ? result.reason.message
                        : "API không trả về danh sách JSON";
            }
        });

        data.users = Array.isArray(usersResult) ? usersResult : [];

        if (selectedEmployeeId) {
            try {
                const rows = await api("employee-positions/employee/" + selectedEmployeeId);
                employeeHistoryRows = Array.isArray(rows) ? rows : [];
            } catch {
                selectedEmployeeId = "";
                employeeHistoryRows = null;
            }
        }

        syncEmployeeFilter();
        counters();
        render();

        notice(
            errors[tab]
                ? `${tab}: ${errors[tab]}`
                : ""
        );
    }

    // ===================================
    // 13. CHUYEN CHUC NANG
    // ===================================

    function changeTab(
        next,
        updateUrl = false
    ) {

        if (!types.includes(next)) {
            return;
        }

        tab = next;

        $("searchInput").value = "";
        syncEmployeeFilter();

        // Menu N3 rieng
        document.querySelectorAll(
            ".sidebar-nav [data-tab]"
        ).forEach(element => {

            element.classList.toggle(
                "active",
                element.dataset.tab === tab
            );
        });

        // Menu AdminLTE chung
        document.querySelectorAll(
            "[id^='menu-']"
        ).forEach(element => {

            if (element.id !== "menu-students") {

                element.classList.toggle(
                    "active",
                    element.id === "menu-" + tab
                );
            }
        });

        if (updateUrl) {

            const url = new URL(location.href);

            url.searchParams.set("tab", tab);

            history.replaceState(
                {},
                "",
                url
            );
        }

        render();

        notice(
            errors[tab]
                ? `${tab}: ${errors[tab]}`
                : ""
        );
    }

    // ===================================
    // 14. THEM MOI
    // ===================================

    $("addBtn").addEventListener(
        "click",
        () => openForm("add")
    );

    // ===================================
    // 15. LAM MOI
    // ===================================

    $("refreshBtn").addEventListener(
        "click",
        loadAll
    );

    // ===================================
    // 16. TIM KIEM
    // ===================================

    $("searchInput").addEventListener(
        "input",
        render
    );

    if ($("employeeFilter")) {
        $("employeeFilter").addEventListener(
            "change",
            event => loadEmployeeHistory(event.target.value)
        );
    }

    // ===================================
    // 17. LUU DU LIEU
    // ===================================

    $("dataForm").addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            if (mode === "view" || busy) {
                return;
            }

            let payload;

            try {
                payload = payloadFromForm();

            } catch (error) {
                alert(error.message);
                return;
            }

            busy = true;

            $("saveBtn").disabled = true;

            try {

                const path = tab + (
                    editId
                        ? "/" + editId
                        : ""
                );

                const method = editId
                    ? "PUT"
                    : "POST";

                await api(
                    path,
                    method,
                    payload
                );

                modal.hide();

                await loadAll();

                notice(
                    "Đã lưu thành công",
                    true
                );

            } catch (error) {

                notice(error.message);

                alert(
                    "Không thể lưu: " + error.message
                );

            } finally {

                busy = false;

                $("saveBtn").disabled = false;
            }
        }
    );

    // ===================================
    // 18. XEM / SUA / XOA
    // ===================================

    $("tableBody").addEventListener(
        "click",
        async event => {

            const button = event.target.closest(
                "[data-action]"
            );

            if (!button) {
                return;
            }

            const row = data[tab].find(
                item =>
                    String(item.id) === button.dataset.id
            );

            if (!row) {
                return;
            }

            const action = button.dataset.action;

            if (
                action === "view" ||
                action === "edit"
            ) {

                openForm(action, row);
                return;
            }

            if (action === "delete") {

                const name = tab === "employee-positions"
                    ? `${related("employees", row.employeeId)} - ${related("positions", row.positionId)}`
                    : (
                        row.code ||
                        row.lecturerCode ||
                        row.fullName ||
                        row.name ||
                        row.id
                    );

                if (!confirm(
                    `Bạn có chắc muốn xóa bản ghi ${name}?`
                )) {
                    return;
                }

                try {

                    await api(
                        tab + "/" + row.id,
                        "DELETE"
                    );

                    await loadAll();

                    notice(
                        "Đã xóa thành công",
                        true
                    );

                } catch (error) {

                    notice(error.message);

                    alert(
                        "Không thể xóa: " + error.message
                    );
                }
            }
        }
    );

    // ===================================
    // 19. MENU N3 RIENG
    // ===================================

    document.querySelectorAll(
        ".sidebar-nav [data-tab]"
    ).forEach(element => {

        element.addEventListener(
            "click",
            () => changeTab(
                element.dataset.tab,
                true
            )
        );
    });

    // ===================================
    // 20. KHOI DONG
    // ===================================

    changeTab(tab);
    loadAll();

})();
