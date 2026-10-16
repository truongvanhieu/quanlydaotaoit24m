
// =========================================
// QUẢN LÝ GIẢNG VIÊN - N3
// Spring Boot REST API + SQL Server
// =========================================

const API = "/api/n3/";

const $ = (id) => document.getElementById(id);

// Đọc tab từ đường dẫn
const params = new URLSearchParams(window.location.search);

const allowedTabs = [
    "lecturers",
    "departments",
    "positions"
];

let currentTab = allowedTabs.includes(params.get("tab"))
    ? params.get("tab")
    : "lecturers";

let editingId = null;
let currentMode = "add";

// Lưu dữ liệu từ API
const dataStore = {
    lecturers: [],
    departments: [],
    positions: []
};

// =========================================
// 1. CẤU HÌNH
// =========================================

const configs = {

    lecturers: {
        title: "Quản lý giảng viên",
        description: "Quản lý thông tin giảng viên và nhân sự",

        columns: [
            "lecturerCode",
            "fullName",
            "gender",
            "email",
            "phone",
            "departmentId",
            "positionId"
        ],

        fields: [
            {
                name: "lecturerCode",
                label: "Mã giảng viên",
                required: true
            },
            {
                name: "fullName",
                label: "Họ và tên",
                required: true
            },
            {
                name: "gender",
                label: "Giới tính",
                type: "gender"
            },
            {
                name: "dateOfBirth",
                label: "Ngày sinh",
                type: "date"
            },
            {
                name: "email",
                label: "Email",
                type: "email"
            },
            {
                name: "phone",
                label: "Số điện thoại"
            },
            {
                name: "departmentId",
                label: "Khoa / Viện",
                type: "department",
                required: true
            },
            {
                name: "positionId",
                label: "Chức danh",
                type: "position"
            },
            {
                name: "hireDate",
                label: "Ngày vào làm",
                type: "date"
            }
        ]
    },

    departments: {
        title: "Quản lý khoa / viện",
        description: "Quản lý các khoa và viện đào tạo",

        columns: [
            "code",
            "name",
            "description"
        ],

        fields: [
            {
                name: "code",
                label: "Mã khoa",
                required: true
            },
            {
                name: "name",
                label: "Tên khoa / viện",
                required: true
            },
            {
                name: "description",
                label: "Mô tả"
            },
            {
                name: "establishedYear",
                label: "Ngày thành lập",
                type: "date"
            }
        ]
    },

    positions: {
        title: "Quản lý chức danh",
        description: "Quản lý chức danh và chức vụ giảng viên",

        columns: [
            "code",
            "name",
            "level",
            "departmentId"
        ],

        fields: [
            {
                name: "code",
                label: "Mã chức danh",
                required: true
            },
            {
                name: "name",
                label: "Tên chức danh",
                required: true
            },
            {
                name: "description",
                label: "Mô tả"
            },
            {
                name: "level",
                label: "Cấp bậc"
            },
            {
                name: "departmentId",
                label: "Khoa / Viện",
                type: "department"
            }
        ]
    }
};

// =========================================
// 2. KHỞI TẠO MODAL
// =========================================

const modalElement = $("formModal");

const modal = new bootstrap.Modal(modalElement);

// =========================================
// 3. HÀM BẢO VỆ HTML
// =========================================

function escapeHtml(value) {

    return String(value ?? "").replace(
        /[&<>"']/g,
        (char) => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        }[char])
    );
}

// =========================================
// 4. GỌI API
// =========================================

async function request(path, options = {}) {

    const response = await fetch(API + path, {

        ...options,

        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }

    });

    if (!response.ok) {

        let message = "Lỗi HTTP " + response.status;

        try {

            const error = await response.json();

            message = error.detail ||
                      error.message ||
                      error.error ||
                      message;

        } catch (error) {
            console.error(error);
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();

    if (!text.trim()) {
        throw new Error("API trả về dữ liệu rỗng");
    }

    return JSON.parse(text);
}

// =========================================
// 5. TẢI DỮ LIỆU
// =========================================

async function loadAll() {

    $("pageMessage").textContent = "";

    try {

        const [
            lecturers,
            departments,
            positions
        ] = await Promise.all([

            request("lecturers"),
            request("departments"),
            request("positions")

        ]);

        dataStore.lecturers = lecturers;
        dataStore.departments = departments;
        dataStore.positions = positions;

        updateStatistics();

        renderTable();

    } catch (error) {

        console.error(error);

        $("pageMessage").textContent =
            "Không tải được dữ liệu: " + error.message;

        $("tableBody").innerHTML = `
            <tr>
                <td colspan="10"
                    class="text-danger text-center py-4">

                    Không thể tải dữ liệu từ API

                </td>
            </tr>
        `;
    }
}

// =========================================
// 6. THỐNG KÊ
// =========================================

function updateStatistics() {

    $("totalLecturers").textContent =
        dataStore.lecturers.length;

    $("totalDepartments").textContent =
        dataStore.departments.length;

    $("totalPositions").textContent =
        dataStore.positions.length;
}

// =========================================
// 7. HIỂN THỊ TÊN KHOA / CHỨC DANH
// =========================================

function displayValue(key, value) {

    if (
        value === null ||
        value === undefined ||
        value === ""
    ) {
        return "—";
    }

    if (key === "departmentId") {

        const department = dataStore.departments.find(
            item => item.id === value
        );

        return department
            ? department.name
            : String(value);
    }

    if (key === "positionId") {

        const position = dataStore.positions.find(
            item => item.id === value
        );

        return position
            ? position.name
            : String(value);
    }

    return String(value);
}

// =========================================
// 8. HIỂN THỊ BẢNG
// =========================================

function renderTable() {

    const config = configs[currentTab];

    // Tiêu đề trang
    document.title = config.title + " | AdminLTE 4";

    const heading = $("n3Heading") || $("pageTitle");

    if (heading) {
        heading.textContent = config.title;
    }

    $("pageDescription").textContent =
        config.description;

    $("tableTitle").textContent =
        config.title.replace("Quản lý", "Danh sách");

    const keyword = $("searchInput")
        .value
        .trim()
        .toLowerCase();

    // Tìm kiếm
    const records = dataStore[currentTab].filter(item => {

        const text = configs[currentTab].columns
            .map(key => displayValue(key, item[key]))
            .join(" ")
            .toLowerCase();

        return text.includes(keyword);
    });

    $("recordCount").textContent =
        records.length + " bản ghi";

    // Tạo header
    const headers = config.columns.map(column => {

        const field = config.fields.find(
            f => f.name === column
        );

        const label = field ? field.label : column;

        return `<th>${escapeHtml(label)}</th>`;

    }).join("");

    $("tableHead").innerHTML = `
        <tr>
            ${headers}
            <th>Hành động</th>
        </tr>
    `;

    // Bảng trống
    if (records.length === 0) {

        $("tableBody").innerHTML = `
            <tr>
                <td colspan="10" class="empty-message">
                    <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                    Không có dữ liệu phù hợp
                </td>
            </tr>
        `;

        return;
    }

    // Tạo danh sách
    $("tableBody").innerHTML = records.map(item => {

        const cells = config.columns.map(column => `
            <td>
                ${escapeHtml(displayValue(
                    column,
                    item[column]
                ))}
            </td>
        `).join("");

        return `
            <tr>

                ${cells}

                <td>

                    <div class="action-buttons">

                        <button
                            type="button"
                            class="btn btn-sm btn-outline-info"
                            data-action="view"
                            data-id="${item.id}"
                            title="Xem chi tiết">

                            <i class="bi bi-eye"></i>

                        </button>

                        <button
                            type="button"
                            class="btn btn-sm btn-outline-primary"
                            data-action="edit"
                            data-id="${item.id}"
                            title="Chỉnh sửa">

                            <i class="bi bi-pencil-square"></i>

                        </button>

                        <button
                            type="button"
                            class="btn btn-sm btn-outline-danger"
                            data-action="delete"
                            data-id="${item.id}"
                            title="Xóa">

                            <i class="bi bi-trash"></i>

                        </button>

                    </div>

                </td>

            </tr>
        `;

    }).join("");
}

// =========================================
// 9. TẠO DANH SÁCH SELECT
// =========================================

function selectOptions(type, selected, departmentId) {

    let items = type === "department"
        ? dataStore.departments
        : dataStore.positions;

    if (type === "position" && departmentId) {

        items = items.filter(item =>
            !item.departmentId ||
            item.departmentId === departmentId
        );
    }

    return `
        <option value="">-- Chọn --</option>
    ` + items.map(item => `
        <option
            value="${escapeHtml(item.id)}"
            ${item.id === selected ? "selected" : ""}>

            ${escapeHtml(item.name)}

        </option>
    `).join("");
}

// =========================================
// 10. TẠO INPUT CHO FORM
// =========================================

function createField(field, value, departmentId) {

    const name = field.name;

    const type = field.type || "text";

    const required = field.required
        ? "required"
        : "";

    let control = "";

    // Chọn khoa / chức danh
    if (
        type === "department" ||
        type === "position"
    ) {

        control = `
            <select class="form-select"
                    name="${name}"
                    ${required}>

                ${selectOptions(
                    type,
                    value,
                    departmentId
                )}

            </select>
        `;
    }

    // Giới tính
    else if (type === "gender") {

        control = `
            <select class="form-select" name="${name}">

                <option value="">-- Chọn --</option>

                ${["Nam", "Nữ", "Khác"].map(v => `
                    <option value="${v}"
                            ${v === value ? "selected" : ""}>
                        ${v}
                    </option>
                `).join("")}

            </select>
        `;
    }

    // Input thông thường
    else {

        control = `
            <input
                type="${type}"
                name="${name}"
                class="form-control"
                value="${escapeHtml(value ?? "")}"
                ${required}>
        `;
    }

    return `
        <div class="col-md-6">

            <label class="form-label">
                ${escapeHtml(field.label)}

                ${field.required
                    ? '<span class="text-danger">*</span>'
                    : ""}
            </label>

            ${control}

        </div>
    `;
}

// =========================================
// 11. MỞ FORM
// =========================================

function openForm(mode, item = null) {

    currentMode = mode;

    editingId = item ? item.id : null;

    const config = configs[currentTab];

    const actions = {
        add: "Thêm",
        edit: "Cập nhật",
        view: "Chi tiết"
    };

    $("modalTitle").textContent =
        actions[mode] + " - " + config.title;

    const departmentId = item?.departmentId || "";

    $("formFields").innerHTML = config.fields.map(field =>
        createField(
            field,
            item?.[field.name] ?? "",
            departmentId
        )
    ).join("");

    // Chế độ xem chi tiết
    const viewOnly = mode === "view";

    $("formFields")
        .querySelectorAll("input, select")
        .forEach(element => {

            element.disabled = viewOnly;

        });

    $("saveBtn").style.display =
        viewOnly ? "none" : "";

    // Thay khoa thì cập nhật chức danh
    const departmentSelect = $("dataForm").querySelector(
        '[name="departmentId"]'
    );

    const positionSelect = $("dataForm").querySelector(
        '[name="positionId"]'
    );

    if (departmentSelect && positionSelect) {

        departmentSelect.addEventListener(
            "change",
            () => {

                positionSelect.innerHTML = selectOptions(
                    "position",
                    "",
                    departmentSelect.value
                );
            }
        );
    }

    modal.show();
}

// =========================================
// 12. LƯU DỮ LIỆU
// =========================================

$("dataForm").addEventListener("submit", async event => {

    event.preventDefault();

    if (currentMode === "view") {
        return;
    }

    const formData = new FormData($("dataForm"));

    const payload = Object.fromEntries(
        formData.entries()
    );

    // Chuẩn hóa dữ liệu
    for (const key of Object.keys(payload)) {

        if (typeof payload[key] === "string") {
            payload[key] = payload[key].trim();
        }

        if (payload[key] === "") {
            payload[key] = null;
        }
    }

    const path = currentTab +
        (editingId ? "/" + editingId : "");

    const method = editingId
        ? "PUT"
        : "POST";

    $("saveBtn").disabled = true;

    try {

        await request(path, {
            method: method,
            body: JSON.stringify(payload)
        });

        modal.hide();

        await loadAll();

        alert("Lưu thông tin thành công!");

    } catch (error) {

        alert("Không thể lưu: " + error.message);

    } finally {

        $("saveBtn").disabled = false;
    }
});

// =========================================
// 13. XEM / SỬA / XÓA
// =========================================

$("tableBody").addEventListener("click", async event => {

    const button = event.target.closest(
        "button[data-action]"
    );

    if (!button) return;

    const action = button.dataset.action;

    const id = button.dataset.id;

    const item = dataStore[currentTab].find(
        x => x.id === id
    );

    if (!item) return;

    if (action === "view") {

        openForm("view", item);
    }

    if (action === "edit") {

        openForm("edit", item);
    }

    if (action === "delete") {

        const confirmed = confirm(
            "Bạn có chắc chắn muốn xóa bản ghi này?"
        );

        if (!confirmed) return;

        try {

            await request(currentTab + "/" + id, {
                method: "DELETE"
            });

            await loadAll();

            alert("Xóa thành công!");

        } catch (error) {

            alert("Không thể xóa: " + error.message);
        }
    }
});

// =========================================
// 14. TÌM KIẾM
// =========================================

$("searchInput").addEventListener(
    "input",
    renderTable
);

// =========================================
// 15. THÊM MỚI
// =========================================

$("addBtn").addEventListener("click", () => {

    openForm("add");

});

// =========================================
// 16. LÀM MỚI
// =========================================

$("refreshBtn").addEventListener(
    "click",
    loadAll
);

// =========================================
// 17. KHỞI ĐỘNG N3
// =========================================

loadAll();
