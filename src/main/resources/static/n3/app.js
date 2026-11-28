
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
        "employee-positions",
        "lecturer-positions-history"
    ];

    const allTabs = [...types, "history"];

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
                "Phân công và lịch sử vị trí công tác của nhân viên",

            columns: [
                "employeeId",
                "positionId",
                "startDate",
                "endDate",
                "termStatus",
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
        },

        // LICH SU CHUC VU GIANG VIEN
        "lecturer-positions-history": {
            title: "Lịch sử chức vụ giảng viên",

            description:
                "Quá trình đảm nhiệm chức danh của giảng viên (lecturer_positions_history)",

            columns: [
                "lecturerId",
                "positionId",
                "startDate",
                "endDate",
                "termStatus",
                "isActive"
            ],

            fields: [
                F("lecturerId", "Giảng viên", "lecturer", {
                    required: true
                }),

                F("positionId", "Chức danh", "position", {
                    required: true
                }),

                F("startDate", "Ngày bắt đầu", "date", {
                    required: true
                }),

                F("endDate", "Ngày kết thúc", "date"),

                F("isActive", "Trạng thái", "status")
            ]
        }
    };

    // ===================================
    // 2. BIEN TRANG THAI
    // ===================================

    const params = new URLSearchParams(location.search);

    let tab = allTabs.includes(params.get("tab"))
        ? params.get("tab")
        : "lecturers";

    const data = Object.fromEntries(
        types.map(kind => [kind, []])
    );
    data.users = [];
    data.auditLogs = [];
    data.deleted = Object.fromEntries(
        types.map(kind => [kind, []])
    );

    const errors = {};

    let editId = null;
    let mode = "add";
    let formTab = tab === "history" ? "employee-positions" : tab;
    let busy = false;
    let selectedEmployeeId = "";
    let employeeHistoryRows = null;
    let selectedLecturerId = "";
    let lecturerHistoryRows = null;
    let historySubTab = "audit";
    let historyModalContext = null;

    const modal = (window.bootstrap && $("formModal"))
        ? bootstrap.Modal.getOrCreateInstance($("formModal"))
        : null;
    const historyModal = (window.bootstrap && $("historyModal"))
        ? bootstrap.Modal.getOrCreateInstance($("historyModal"))
        : null;

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

        const activeList = data[kind] || [];
        const deletedList = (data.deleted && data.deleted[kind]) || [];

        let row = activeList.find(
            item => String(item.id) === String(id)
        );
        let fromDeleted = false;

        if (!row) {
            row = deletedList.find(
                item => String(item.id) === String(id)
            );
            fromDeleted = Boolean(row);
        }

        if (!row) {
            return String(id);
        }

        let labelText = "";
        if (kind === "employees") {
            labelText = (`${row.code || ""} - ${row.fullName || ""}`).trim();
        } else if (kind === "lecturers") {
            labelText = (`${row.lecturerCode || ""} - ${row.fullName || ""}`).trim();
        } else if (kind === "users") {
            labelText = (`${row.username || ""}${row.fullName ? " - " + row.fullName : ""}`).trim();
        } else {
            labelText = row.name || row.code || String(id);
        }

        return fromDeleted ? `${labelText} (đã xóa)` : (labelText || String(id));
    }

    function termBadgeHtml(row) {
        if (row.deletedAt) {
            return '<span class="badge-term deleted">Đã xóa mềm</span>';
        }
        if (!row.endDate) {
            return '<span class="badge-term current">Đang đương nhiệm</span>';
        }
        return '<span class="badge-term ended">Đã kết thúc</span>';
    }

    function display(key, value, row = null, contextTab = tab) {

        if (key === "termStatus" && row) {
            if (row.deletedAt) return "Đã xóa mềm";
            return row.endDate ? "Đã kết thúc" : "Đang đương nhiệm";
        }

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

        if (key === "lecturerId") {
            return related("lecturers", value);
        }

        if (key === "userId") {
            return related("users", value);
        }

        if (
            key === "positionId" &&
            contextTab !== "employees"
        ) {
            return related("positions", value);
        }

        if (
            /Date$|^establishedYear$|^deletedAt$|^createdAt$|^updatedAt$/.test(key) &&
            typeof value === "string"
        ) {
            return value
                .replace("T", " ")
                .slice(0, 19);
        }

        return String(value).trim() || "—";
    }

    function label(key, kind = tab) {
        if (key === "termStatus") return "Tình trạng nhiệm kỳ";
        if (key === "deletedAt") return "Thời điểm xóa mềm";
        return C[kind]?.fields.find(
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
            "employee-positions": "totalEmployeePositions",
            "lecturer-positions-history": "totalLecturerHistory"
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

        const isHistoryTab = tab === "history";
        if ($("standardCard")) $("standardCard").style.display = isHistoryTab ? "none" : "";
        if ($("historyCard")) $("historyCard").style.display = isHistoryTab ? "" : "none";

        if (isHistoryTab) {
            document.title = "Lịch sử & Nhật ký | IT24M";
            text("pageTitle", "Lịch sử Thêm / Sửa / Xóa & Công tác");
            text("n3Heading", "Lịch sử Thêm / Sửa / Xóa & Công tác");
            text("pageDescription", "Nhật ký thao tác cập nhật dữ liệu, lịch sử vị trí công tác và dữ liệu đã xóa mềm của Nhóm N3");
            renderHistoryCenter();
            return;
        }

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

        let sourceRows = data[tab];
        if (
            tab === "employee-positions" &&
            selectedEmployeeId &&
            Array.isArray(employeeHistoryRows)
        ) {
            sourceRows = employeeHistoryRows;
        } else if (
            tab === "lecturer-positions-history" &&
            selectedLecturerId &&
            Array.isArray(lecturerHistoryRows)
        ) {
            sourceRows = lecturerHistoryRows;
        }

        const searchKeys = Array.from(
            new Set([
                ...cfg.columns,
                ...cfg.fields.map(field => field.name)
            ])
        );

        const rows = sourceRows.filter(row => {

            if (!query) return true;

            return searchKeys.some(key =>
                display(key, row[key], row, tab)
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
                    <th>${escapeHTML(label(key, tab))}</th>
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
                        ${key === "termStatus"
                            ? termBadgeHtml(row)
                            : escapeHTML(display(key, row[key], row, tab))}
                    </td>
                `).join("")}

                <td class="text-end text-nowrap">

                    ${(tab === "employees" || tab === "lecturers") ? `
                        <button type="button"
                                class="btn btn-sm btn-outline-secondary"
                                data-action="history"
                                data-id="${escapeHTML(row.id)}"
                                title="${tab === "employees" ? "Xem lịch sử công tác" : "Xem lịch sử chức vụ"}">
                            <i class="bi bi-clock-history"></i>
                        </button>
                    ` : ""}

                    ${((tab === "employee-positions" || tab === "lecturer-positions-history") && !row.endDate) ? `
                        <button type="button"
                                class="btn btn-sm btn-outline-success"
                                data-action="end-term"
                                data-id="${escapeHTML(row.id)}"
                                title="Kết thúc nhiệm kỳ">
                            <i class="bi bi-calendar-check"></i>
                        </button>
                    ` : ""}

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
                            title="Xóa mềm">

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
            employee: "employees",
            lecturer: "lecturers"
        };

        const kind = map[field.type];
        let items = data[kind] || [];

        // Loc chuc danh theo khoa khi them/sua giang vien
        if (
            formTab === "lecturers" &&
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
                : kind === "lecturers"
                    ? `${row.lecturerCode || ""} - ${row.fullName || ""}`
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
            "lecturer",
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

    function openForm(nextMode, row = null, targetTab = tab) {

        mode = nextMode;
        formTab = C[targetTab] ? targetTab : "employee-positions";
        editId = row?.id || null;

        const action =
            nextMode === "add"
                ? "Thêm"
                : nextMode === "edit"
                    ? "Sửa"
                    : "Xem";

        text(
            "modalTitle",
            `${action} - ${C[formTab].title}`
        );

        $("formFields").innerHTML =
            C[formTab].fields.map(field =>
                control(
                    field,
                    row?.[field.name],
                    nextMode === "view",
                    row
                )
            ).join("");

        // Dong bo loc chuc danh theo khoa tren form giang vien
        if (formTab === "lecturers" && nextMode !== "view") {
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

        for (const field of C[formTab].fields) {

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
            (formTab === "employee-positions" || formTab === "lecturer-positions-history") &&
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
    // 12. TAI DU LIEU & LICH SU
    // ===================================

    function historyNotice(message = "", success = false) {
        const box = $("historyMessage");
        if (!box) return;
        box.className = success ? "mt-2 text-success" : "mt-2 text-danger";
        box.textContent = message;
    }

    function syncEmployeeFilter() {
        const filter = $("employeeFilter");
        const timelineBtn = $("openTimelineBtn");
        if (!filter) return;

        if (tab === "employee-positions") {
            filter.style.display = "";
            const options = (data.employees || []).map(emp => `
                <option value="${escapeHTML(emp.id)}"
                    ${String(emp.id) === String(selectedEmployeeId) ? "selected" : ""}>
                    ${escapeHTML((`${emp.code || ""} - ${emp.fullName || ""}`).trim())}
                </option>
            `).join("");
            filter.innerHTML =
                '<option value="">-- Lọc lịch sử theo nhân viên (Tất cả) --</option>' +
                options;
            if (timelineBtn) {
                timelineBtn.style.display = selectedEmployeeId ? "" : "none";
            }
            return;
        }

        if (tab === "lecturer-positions-history") {
            filter.style.display = "";
            const options = (data.lecturers || []).map(lec => `
                <option value="${escapeHTML(lec.id)}"
                    ${String(lec.id) === String(selectedLecturerId) ? "selected" : ""}>
                    ${escapeHTML((`${lec.lecturerCode || ""} - ${lec.fullName || ""}`).trim())}
                </option>
            `).join("");
            filter.innerHTML =
                '<option value="">-- Lọc lịch sử theo giảng viên (Tất cả) --</option>' +
                options;
            if (timelineBtn) {
                timelineBtn.style.display = selectedLecturerId ? "" : "none";
            }
            return;
        }

        filter.style.display = "none";
        if (timelineBtn) {
            timelineBtn.style.display = "none";
        }
    }

    async function loadPersonFilter(personId) {
        if (tab === "employee-positions") {
            selectedEmployeeId = personId || "";
            if (!selectedEmployeeId) {
                employeeHistoryRows = null;
                syncEmployeeFilter();
                render();
                notice("");
                return;
            }
            try {
                const rows = await api("employee-positions/employee/" + selectedEmployeeId);
                employeeHistoryRows = Array.isArray(rows) ? rows : [];
                syncEmployeeFilter();
                render();
                notice("");
            } catch (error) {
                employeeHistoryRows = [];
                syncEmployeeFilter();
                render();
                notice(error.message);
            }
            return;
        }

        if (tab === "lecturer-positions-history") {
            selectedLecturerId = personId || "";
            if (!selectedLecturerId) {
                lecturerHistoryRows = null;
                syncEmployeeFilter();
                render();
                notice("");
                return;
            }
            try {
                const rows = await api("lecturer-positions-history/lecturer/" + selectedLecturerId);
                lecturerHistoryRows = Array.isArray(rows) ? rows : [];
                syncEmployeeFilter();
                render();
                notice("");
            } catch (error) {
                lecturerHistoryRows = [];
                syncEmployeeFilter();
                render();
                notice(error.message);
            }
        }
    }

    function renderSummaryPills(rows) {
        const total = rows.length;
        const current = rows.filter(r => !r.deletedAt && !r.endDate).length;
        const ended = rows.filter(r => !r.deletedAt && r.endDate).length;
        const deleted = rows.filter(r => r.deletedAt).length;

        return `
            <div class="summary-pill">Tổng lượt: <strong>${total}</strong></div>
            <div class="summary-pill text-success">Đang đương nhiệm: <strong>${current}</strong></div>
            <div class="summary-pill text-secondary">Đã kết thúc nhiệm kỳ: <strong>${ended}</strong></div>
            ${deleted > 0 ? `<div class="summary-pill text-danger">Đã xóa mềm (lưu lịch sử): <strong>${deleted}</strong></div>` : ""}
        `;
    }

    function renderTimelineItems(rows, targetKind, showPerson = false) {
        if (!rows || rows.length === 0) {
            return `<div class="text-center text-muted py-4 border rounded-3 bg-light">Chưa có bản ghi lịch sử công tác phù hợp.</div>`;
        }

        return `
            <div class="timeline-list">
                ${rows.map(row => {
                    const cls = row.deletedAt ? "deleted" : (row.endDate ? "ended" : "");
                    const personTitle = targetKind === "employee-positions"
                        ? related("employees", row.employeeId)
                        : related("lecturers", row.lecturerId);
                    const posTitle = related("positions", row.positionId);
                    const startText = display("startDate", row.startDate);
                    const endText = row.endDate ? display("endDate", row.endDate) : "Hiện tại";

                    return `
                        <div class="timeline-item ${cls}">
                            <div class="timeline-header">
                                <div>
                                    <strong>${escapeHTML(posTitle)}</strong>
                                    ${showPerson ? `<span class="text-muted ms-2">(${escapeHTML(personTitle)})</span>` : ""}
                                </div>
                                <div class="d-flex align-items-center gap-2">
                                    ${termBadgeHtml(row)}
                                    ${(!row.deletedAt && !row.endDate) ? `
                                        <button type="button"
                                                class="btn btn-sm btn-outline-success py-0 px-2"
                                                data-hist-action="end-term"
                                                data-kind="${escapeHTML(targetKind)}"
                                                data-id="${escapeHTML(row.id)}"
                                                title="Kết thúc nhiệm kỳ">
                                            <i class="bi bi-calendar-check me-1"></i>Kết thúc
                                        </button>
                                    ` : ""}
                                    ${!row.deletedAt ? `
                                        <button type="button"
                                                class="btn btn-sm btn-outline-primary py-0 px-2"
                                                data-hist-action="edit"
                                                data-kind="${escapeHTML(targetKind)}"
                                                data-id="${escapeHTML(row.id)}"
                                                title="Sửa">
                                            <i class="bi bi-pencil-square"></i>
                                        </button>
                                    ` : `
                                        <button type="button"
                                                class="btn btn-sm btn-outline-success py-0 px-2"
                                                data-hist-action="restore"
                                                data-kind="${escapeHTML(targetKind)}"
                                                data-id="${escapeHTML(row.id)}"
                                                title="Khôi phục">
                                            <i class="bi bi-arrow-counterclockwise me-1"></i>Khôi phục
                                        </button>
                                    `}
                                </div>
                            </div>
                            <div class="timeline-meta">
                                <i class="bi bi-calendar-range me-1"></i>
                                <span>${escapeHTML(startText)}</span>
                                <span class="mx-1">→</span>
                                <strong>${escapeHTML(endText)}</strong>
                                ${row.deletedAt ? `<span class="text-danger ms-3"><i class="bi bi-trash me-1"></i>Xóa mềm lúc: ${escapeHTML(display("deletedAt", row.deletedAt))}</span>` : ""}
                            </div>
                            ${row.description ? `<div class="small mt-2"><strong>Mô tả:</strong> ${escapeHTML(row.description)}</div>` : ""}
                            ${row.note ? `<div class="small text-muted mt-1"><strong>Ghi chú:</strong> ${escapeHTML(row.note)}</div>` : ""}
                        </div>
                    `;
                }).join("")}
            </div>
        `;
    }

    async function openHistoryModal(personKind, personId) {
        if (!historyModal) return;

        const isEmployee = personKind === "employees";
        const targetKind = isEmployee ? "employee-positions" : "lecturer-positions-history";
        const endpoint = isEmployee
            ? `employee-positions/employee/${personId}/history?includeDeleted=true`
            : `lecturer-positions-history/lecturer/${personId}?includeDeleted=true`;

        const personLabel = related(personKind, personId);
        historyModalContext = { personKind, personId, targetKind };

        text(
            "historyModalTitle",
            isEmployee ? `Lịch sử công tác: ${personLabel}` : `Lịch sử chức vụ: ${personLabel}`
        );
        text(
            "historyModalSubtitle",
            isEmployee
                ? "Toàn bộ các vị trí đang đương nhiệm, đã kết thúc nhiệm kỳ và đã xóa mềm (bảng employee_positions)"
                : "Toàn bộ quá trình đảm nhiệm chức danh (bảng lecturer_positions_history)"
        );

        const isActivePerson = (data[personKind] || []).some(p => String(p.id) === String(personId));
        if ($("historyModalAddBtn")) {
            $("historyModalAddBtn").style.display = isActivePerson ? "" : "none";
            $("historyModalAddBtn").innerHTML = isEmployee
                ? '<i class="bi bi-plus-lg"></i> Phân công thêm vị trí'
                : '<i class="bi bi-plus-lg"></i> Thêm lịch sử chức vụ';
        }

        $("historyModalSummary").innerHTML = "";
        $("historyModalBody").innerHTML = '<div class="text-center text-muted py-4">Đang tải lịch sử...</div>';
        historyModal.show();

        try {
            const rows = await api(endpoint);
            const list = Array.isArray(rows) ? rows : [];
            $("historyModalSummary").innerHTML = renderSummaryPills(list);
            $("historyModalBody").innerHTML = renderTimelineItems(list, targetKind, false);
        } catch (error) {
            $("historyModalBody").innerHTML = `<div class="alert alert-danger mb-0">${escapeHTML(error.message)}</div>`;
        }
    }

    function syncHistoryEmployeeSelect() {
        const select = $("historyEmployeeSelect");
        if (!select) return;
        const currentVal = select.value;

        const activeOpts = (data.employees || []).map(emp => `
            <option value="${escapeHTML(emp.id)}">
                ${escapeHTML((`${emp.code || ""} - ${emp.fullName || ""}`).trim())}
            </option>
        `).join("");

        const deletedOpts = (data.deleted.employees || []).map(emp => `
            <option value="${escapeHTML(emp.id)}">
                ${escapeHTML((`${emp.code || ""} - ${emp.fullName || ""}`).trim())} [Đã xóa mềm]
            </option>
        `).join("");

        select.innerHTML =
            '<option value="">-- Tất cả nhân viên (xem toàn bộ lịch sử công tác) --</option>' +
            (activeOpts ? `<optgroup label="Nhân viên đang hoạt động">${activeOpts}</optgroup>` : "") +
            (deletedOpts ? `<optgroup label="Nhân viên đã xóa mềm (còn lưu lịch sử)">${deletedOpts}</optgroup>` : "");

        if (currentVal && [...(data.employees || []), ...(data.deleted.employees || [])].some(e => String(e.id) === String(currentVal))) {
            select.value = currentVal;
        }
    }

    async function renderWorkHistoryPanel() {
        syncHistoryEmployeeSelect();
        const empId = $("historyEmployeeSelect")?.value || "";
        const termFilter = $("historyTermFilter")?.value || "all";
        const includeDeleted = Boolean($("historyIncludeDeleted")?.checked);

        let rows = [];
        try {
            if (empId) {
                const res = await api(`employee-positions/employee/${empId}/history?includeDeleted=${includeDeleted}`);
                rows = Array.isArray(res) ? res : [];
            } else {
                rows = includeDeleted
                    ? [...(data["employee-positions"] || []), ...(data.deleted["employee-positions"] || [])]
                    : [...(data["employee-positions"] || [])];
                rows.sort((a, b) => String(b.startDate || "").localeCompare(String(a.startDate || "")));
            }

            if (termFilter === "current") {
                rows = rows.filter(r => !r.endDate);
            } else if (termFilter === "ended") {
                rows = rows.filter(r => Boolean(r.endDate));
            }

            $("historyWorkSummary").innerHTML = renderSummaryPills(rows);
            $("historyWorkTimeline").innerHTML = renderTimelineItems(rows, "employee-positions", !empId);
            historyNotice("");
        } catch (error) {
            $("historyWorkSummary").innerHTML = "";
            $("historyWorkTimeline").innerHTML = "";
            historyNotice(error.message);
        }
    }

    function renderDeletedHistoryPanel() {
        const entityKind = $("deletedEntitySelect")?.value || "employees";
        const cfg = C[entityKind];
        const rows = data.deleted[entityKind] || [];

        const cols = [
            ...cfg.columns.filter(k => k !== "termStatus" && k !== "isActive").slice(0, 4),
            "deletedAt",
            "isActive"
        ];

        text("deletedCount", `${rows.length} bản ghi đã xóa mềm`);

        $("deletedTableHead").innerHTML = `
            <tr>
                ${cols.map(key => `<th>${escapeHTML(label(key, entityKind))}</th>`).join("")}
                <th class="text-end">Thao tác</th>
            </tr>
        `;

        if (rows.length === 0) {
            $("deletedTableBody").innerHTML = `
                <tr>
                    <td colspan="${cols.length + 1}" class="text-center text-muted py-4">
                        Chưa có bản ghi nào bị xóa mềm ở phân hệ này.
                    </td>
                </tr>
            `;
            return;
        }

        $("deletedTableBody").innerHTML = rows.map(row => `
            <tr>
                ${cols.map(key => `
                    <td>${escapeHTML(display(key, row[key], row, entityKind))}</td>
                `).join("")}
                <td class="text-end text-nowrap">
                    ${(entityKind === "employees" || entityKind === "lecturers") ? `
                        <button type="button"
                                class="btn btn-sm btn-outline-secondary"
                                data-del-action="history"
                                data-kind="${escapeHTML(entityKind)}"
                                data-id="${escapeHTML(row.id)}"
                                title="Xem lịch sử công tác vẫn được lưu giữ">
                            <i class="bi bi-clock-history"></i>
                        </button>
                    ` : ""}
                    <button type="button"
                            class="btn btn-sm btn-outline-info"
                            data-del-action="view"
                            data-kind="${escapeHTML(entityKind)}"
                            data-id="${escapeHTML(row.id)}"
                            title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button type="button"
                            class="btn btn-sm btn-outline-success"
                            data-del-action="restore"
                            data-kind="${escapeHTML(entityKind)}"
                            data-id="${escapeHTML(row.id)}"
                            title="Khôi phục bản ghi">
                        <i class="bi bi-arrow-counterclockwise me-1"></i>Khôi phục
                    </button>
                </td>
            </tr>
        `).join("");
    }

    function auditActionBadge(action) {
        const map = {
            THEM: ["create", "Thêm mới"],
            CAP_NHAT: ["update", "Cập nhật / Sửa"],
            XOA_MEM: ["delete", "Xóa mềm"],
            KHOI_PHUC: ["restore", "Khôi phục"],
            KET_THUC_NHIEM_KY: ["end", "Kết thúc nhiệm kỳ"]
        };
        const [cls, labelText] = map[action] || ["update", action || "Khác"];
        return `<span class="badge-action ${cls}">${escapeHTML(labelText)}</span>`;
    }

    function auditTableLabel(tableName) {
        const map = {
            departments: "Khoa / viện",
            positions: "Chức danh",
            lecturers: "Giảng viên",
            employees: "Nhân viên",
            employee_positions: "Vị trí công tác",
            lecturer_positions_history: "Lịch sử chức vụ GV"
        };
        return map[tableName] || tableName || "—";
    }

    function renderAuditHistoryPanel() {
        const query = ($("auditSearchInput")?.value || "").trim().toLocaleLowerCase("vi");
        const actFilter = $("auditActionFilter")?.value || "";
        const tblFilter = $("auditTableFilter")?.value || "";

        const rows = (data.auditLogs || []).filter(log => {
            if (actFilter && log.action !== actFilter) return false;
            if (tblFilter && log.tableName !== tblFilter) return false;
            if (!query) return true;

            const haystack = [
                log.description,
                log.action,
                log.tableName,
                auditTableLabel(log.tableName),
                log.recordId,
                log.createdAt
            ].join(" ").toLocaleLowerCase("vi");
            return haystack.includes(query);
        });

        text("auditCount", `${rows.length} / ${(data.auditLogs || []).length} nhật ký`);

        if (rows.length === 0) {
            $("auditTableBody").innerHTML = `
                <tr>
                    <td colspan="4" class="text-center text-muted py-4">
                        Chưa có nhật ký thao tác nào (hãy thử Thêm, Sửa, Xóa mềm hoặc Khôi phục một bản ghi ở các tab Nhóm N3).
                    </td>
                </tr>
            `;
            return;
        }

        $("auditTableBody").innerHTML = rows.map(log => `
            <tr>
                <td class="text-nowrap">${escapeHTML(display("createdAt", log.createdAt))}</td>
                <td>${auditActionBadge(log.action)}</td>
                <td class="text-nowrap">${escapeHTML(auditTableLabel(log.tableName))}</td>
                <td>
                    <div>${escapeHTML(log.description || "—")}</div>
                    ${log.recordId ? `<small class="text-muted">ID: ${escapeHTML(log.recordId)}</small>` : ""}
                </td>
            </tr>
        `).join("");
    }

    function renderHistoryCenter() {
        const isAudit = historySubTab === "audit";
        const isWork = historySubTab === "work";
        const isDeleted = historySubTab === "deleted";

        if ($("historyAuditPanel")) $("historyAuditPanel").style.display = isAudit ? "" : "none";
        if ($("historyWorkPanel")) $("historyWorkPanel").style.display = isWork ? "" : "none";
        if ($("historyDeletedPanel")) $("historyDeletedPanel").style.display = isDeleted ? "" : "none";

        if ($("histTabAudit")) {
            $("histTabAudit").className = isAudit ? "btn btn-primary btn-sm" : "btn btn-outline-primary btn-sm";
        }
        if ($("histTabWork")) {
            $("histTabWork").className = isWork ? "btn btn-primary btn-sm" : "btn btn-outline-primary btn-sm";
        }
        if ($("histTabDeleted")) {
            $("histTabDeleted").className = isDeleted ? "btn btn-primary btn-sm" : "btn btn-outline-primary btn-sm";
        }

        if (isAudit) {
            renderAuditHistoryPanel();
        } else if (isWork) {
            renderWorkHistoryPanel();
        } else {
            renderDeletedHistoryPanel();
        }
    }

    async function handleEndTerm(targetKind, id) {
        if (!confirm("Xác nhận kết thúc nhiệm kỳ của vị trí công tác này (ghi nhận ngày kết thúc hôm nay và giữ lại lịch sử)?")) {
            return;
        }
        try {
            await api(`${targetKind}/${id}/end`, "PUT", {});
            await loadAll();
            if (historyModalContext && $("historyModal")?.classList.contains("show")) {
                await openHistoryModal(historyModalContext.personKind, historyModalContext.personId);
            }
            notice("Đã kết thúc nhiệm kỳ và lưu lịch sử thành công", true);
            historyNotice("Đã kết thúc nhiệm kỳ và lưu lịch sử thành công", true);
        } catch (error) {
            notice(error.message);
            historyNotice(error.message);
            alert("Không thể kết thúc nhiệm kỳ: " + error.message);
        }
    }

    async function handleRestore(targetKind, id) {
        if (!confirm("Bạn có muốn khôi phục bản ghi đã xóa mềm này trở lại hoạt động?")) {
            return;
        }
        try {
            await api(`${targetKind}/${id}/restore`, "PUT");
            await loadAll();
            if (historyModalContext && $("historyModal")?.classList.contains("show")) {
                await openHistoryModal(historyModalContext.personKind, historyModalContext.personId);
            }
            notice("Đã khôi phục bản ghi thành công", true);
            historyNotice("Đã khôi phục bản ghi thành công", true);
        } catch (error) {
            notice(error.message);
            historyNotice(error.message);
            alert("Không thể khôi phục: " + error.message);
        }
    }

    async function loadAll() {

        // Mot API loi khong lam cac API khac mat du lieu
        const [results, deletedResults, usersResult, auditResult] = await Promise.all([
            Promise.allSettled(types.map(kind => api(kind))),
            Promise.allSettled(types.map(kind => api(kind + "/deleted"))),
            api("users").catch(() => []),
            api("audit-logs").catch(() => [])
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

        deletedResults.forEach((result, index) => {
            const kind = types[index];
            data.deleted[kind] =
                result.status === "fulfilled" && Array.isArray(result.value)
                    ? result.value
                    : [];
        });

        data.users = Array.isArray(usersResult) ? usersResult : [];
        data.auditLogs = Array.isArray(auditResult) ? auditResult : [];

        if (selectedEmployeeId) {
            try {
                const rows = await api("employee-positions/employee/" + selectedEmployeeId);
                employeeHistoryRows = Array.isArray(rows) ? rows : [];
            } catch {
                selectedEmployeeId = "";
                employeeHistoryRows = null;
            }
        }

        if (selectedLecturerId) {
            try {
                const rows = await api("lecturer-positions-history/lecturer/" + selectedLecturerId);
                lecturerHistoryRows = Array.isArray(rows) ? rows : [];
            } catch {
                selectedLecturerId = "";
                lecturerHistoryRows = null;
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

        if (!allTabs.includes(next)) {
            return;
        }

        tab = next;

        if ($("searchInput")) {
            $("searchInput").value = "";
        }
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
        () => openForm("add", null, tab)
    );

    // ===================================
    // 15. LAM MOI
    // ===================================

    $("refreshBtn").addEventListener(
        "click",
        loadAll
    );

    // ===================================
    // 16. TIM KIEM & LOC LICH SU
    // ===================================

    $("searchInput").addEventListener(
        "input",
        render
    );

    if ($("employeeFilter")) {
        $("employeeFilter").addEventListener(
            "change",
            event => loadPersonFilter(event.target.value)
        );
    }

    if ($("openTimelineBtn")) {
        $("openTimelineBtn").addEventListener("click", () => {
            if (tab === "employee-positions" && selectedEmployeeId) {
                openHistoryModal("employees", selectedEmployeeId);
            } else if (tab === "lecturer-positions-history" && selectedLecturerId) {
                openHistoryModal("lecturers", selectedLecturerId);
            }
        });
    }

    if ($("histTabAudit")) {
        $("histTabAudit").addEventListener("click", () => {
            historySubTab = "audit";
            renderHistoryCenter();
        });
    }

    if ($("auditSearchInput")) {
        $("auditSearchInput").addEventListener("input", renderAuditHistoryPanel);
    }

    if ($("auditActionFilter")) {
        $("auditActionFilter").addEventListener("change", renderAuditHistoryPanel);
    }

    if ($("auditTableFilter")) {
        $("auditTableFilter").addEventListener("change", renderAuditHistoryPanel);
    }

    if ($("auditRefreshBtn")) {
        $("auditRefreshBtn").addEventListener("click", loadAll);
    }

    if ($("histTabWork")) {
        $("histTabWork").addEventListener("click", () => {
            historySubTab = "work";
            renderHistoryCenter();
        });
    }

    if ($("histTabDeleted")) {
        $("histTabDeleted").addEventListener("click", () => {
            historySubTab = "deleted";
            renderHistoryCenter();
        });
    }

    if ($("historyEmployeeSelect")) {
        $("historyEmployeeSelect").addEventListener("change", renderWorkHistoryPanel);
    }

    if ($("historyTermFilter")) {
        $("historyTermFilter").addEventListener("change", renderWorkHistoryPanel);
    }

    if ($("historyIncludeDeleted")) {
        $("historyIncludeDeleted").addEventListener("change", renderWorkHistoryPanel);
    }

    if ($("historyAssignBtn")) {
        $("historyAssignBtn").addEventListener("click", () => {
            const empId = $("historyEmployeeSelect")?.value || "";
            const isActiveEmp = empId && (data.employees || []).some(e => String(e.id) === String(empId));
            openForm("add", isActiveEmp ? { employeeId: empId } : null, "employee-positions");
        });
    }

    if ($("deletedEntitySelect")) {
        $("deletedEntitySelect").addEventListener("change", renderDeletedHistoryPanel);
    }

    if ($("deletedRefreshBtn")) {
        $("deletedRefreshBtn").addEventListener("click", loadAll);
    }

    if ($("historyModalAddBtn")) {
        $("historyModalAddBtn").addEventListener("click", () => {
            if (!historyModalContext) return;
            const { personKind, personId, targetKind } = historyModalContext;
            historyModal.hide();
            const preset = personKind === "employees"
                ? { employeeId: personId }
                : { lecturerId: personId };
            openForm("add", preset, targetKind);
        });
    }

    // Xu ly click trong Timeline (ca tren tab Lich su va trong Modal)
    async function handleTimelineClick(event) {
        const btn = event.target.closest("[data-hist-action]");
        if (!btn) return;
        const action = btn.dataset.histAction;
        const kind = btn.dataset.kind;
        const id = btn.dataset.id;

        if (action === "end-term") {
            await handleEndTerm(kind, id);
            return;
        }
        if (action === "restore") {
            await handleRestore(kind, id);
            return;
        }
        if (action === "edit") {
            const row = (data[kind] || []).find(item => String(item.id) === String(id));
            if (row) {
                if ($("historyModal")?.classList.contains("show")) {
                    historyModal.hide();
                }
                openForm("edit", row, kind);
            }
        }
    }

    if ($("historyWorkTimeline")) {
        $("historyWorkTimeline").addEventListener("click", handleTimelineClick);
    }
    if ($("historyModalBody")) {
        $("historyModalBody").addEventListener("click", handleTimelineClick);
    }

    // Xu ly click trong bang Du lieu da xoa mem
    if ($("deletedTableBody")) {
        $("deletedTableBody").addEventListener("click", async event => {
            const btn = event.target.closest("[data-del-action]");
            if (!btn) return;
            const action = btn.dataset.delAction;
            const kind = btn.dataset.kind;
            const id = btn.dataset.id;

            if (action === "restore") {
                await handleRestore(kind, id);
                return;
            }
            if (action === "history") {
                await openHistoryModal(kind, id);
                return;
            }
            if (action === "view") {
                const row = (data.deleted[kind] || []).find(item => String(item.id) === String(id));
                if (row) {
                    openForm("view", row, kind);
                }
            }
        });
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

                const path = formTab + (
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
                historyNotice(
                    "Đã lưu thành công",
                    true
                );

            } catch (error) {

                notice(error.message);
                historyNotice(error.message);

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
    // 18. XEM / SUA / XOA / LICH SU / KET THUC
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

            const row = (data[tab] || []).find(
                item =>
                    String(item.id) === button.dataset.id
            );

            if (!row) {
                return;
            }

            const action = button.dataset.action;

            if (action === "history") {
                await openHistoryModal(tab, row.id);
                return;
            }

            if (action === "end-term") {
                await handleEndTerm(tab, row.id);
                return;
            }

            if (
                action === "view" ||
                action === "edit"
            ) {

                openForm(action, row, tab);
                return;
            }

            if (action === "delete") {

                const name = tab === "employee-positions"
                    ? `${related("employees", row.employeeId)} - ${related("positions", row.positionId)}`
                    : tab === "lecturer-positions-history"
                        ? `${related("lecturers", row.lecturerId)} - ${related("positions", row.positionId)}`
                        : (
                            row.code ||
                            row.lecturerCode ||
                            row.fullName ||
                            row.name ||
                            row.id
                        );

                if (!confirm(
                    `Bạn có chắc muốn xóa mềm bản ghi ${name}?`
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
                        "Đã xóa mềm thành công (bản ghi vẫn được lưu trong mục Lịch sử & Xóa mềm)",
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

    const sidebarNav = document.querySelector(".sidebar-nav");
    if (sidebarNav) {
        sidebarNav.addEventListener("click", event => {
            const item = event.target.closest("[data-tab]");
            if (!item) return;
            changeTab(item.dataset.tab, true);
        });
    }

    // ===================================
    // 20. KHOI DONG
    // ===================================

    changeTab(tab);
    loadAll();

})();
