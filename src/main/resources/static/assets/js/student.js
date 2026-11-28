import { API_ENDPOINTS } from './config.js';

<<<<<<< HEAD
const TOKEN_KEY = 'n1.jwt';
const USER_ID_KEY = 'n1.userId';
let studentPermissions = new Set();

function getAuthHeaders(json = false) {
    const headers = {};
    const token = sessionStorage.getItem(TOKEN_KEY);

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    if (json) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
}

async function loadStudentPermissions() {
    const userId = sessionStorage.getItem(USER_ID_KEY);
    const token = sessionStorage.getItem(TOKEN_KEY);

    studentPermissions = new Set();
    updateStudentSessionUi();

    if (!token || !userId) {
        return;
    }

    try {
        const response = await fetch(
            `http://localhost:8080/api/users/${encodeURIComponent(userId)}/permissions`,
            { headers: getAuthHeaders() }
        );

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const permissions = await response.json();
        studentPermissions = new Set(
            permissions
                .map((permission) => permission.code)
                .filter((code) => code && code.startsWith('STUDENT_'))
        );
    } catch (error) {
        console.error('Không thể tải quyền sinh viên:', error);
    }

    updateStudentSessionUi();
}

function requireStudentView() {
    const token = sessionStorage.getItem(TOKEN_KEY);
    const userId = sessionStorage.getItem(USER_ID_KEY);

    if (!token || !userId || !studentPermissions.has('STUDENT_VIEW')) {
        const pageList = document.getElementById('page-list');
        const pageForm = document.getElementById('page-form');
        if (pageList) {
            pageList.style.display = 'none';
        }
        if (pageForm) {
            pageForm.style.display = 'none';
        }

        window.location.replace('/auth.html?next=/index.html');
        return false;
    }

    return true;
}

function updateStudentSessionUi() {
    const token = sessionStorage.getItem(TOKEN_KEY);
    const username = sessionStorage.getItem('n1.username');
    const status = document.getElementById('studentSessionStatus');
    const loginButton = document.getElementById('studentLoginButton');
    const registerButton = document.getElementById('studentRegisterButton');
    const logoutButton = document.getElementById('studentLogoutButton');

    if (status) {
        status.textContent = token
            ? `Đang đăng nhập: ${username || 'tài khoản'}`
            : 'Chưa đăng nhập';
    }
    if (loginButton) {
        loginButton.hidden = Boolean(token);
    }
    if (registerButton) {
        registerButton.hidden = Boolean(token);
    }
    if (logoutButton) {
        logoutButton.hidden = !token;
    }

    const createButton = document.querySelector('a[href="index.html?page=form"]');
    if (createButton) {
        createButton.hidden = !studentPermissions.has('STUDENT_CREATE');
    }
}

=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
document.addEventListener('DOMContentLoaded', () => {

    const params = new URLSearchParams(window.location.search);

    const page = params.get('page') || 'list';

    const pageList = document.getElementById('page-list');
    const pageForm = document.getElementById('page-form');

    if (page === 'form') {

        if (pageList) {
            pageList.style.display = 'none';
        }

        if (pageForm) {
            pageForm.style.display = '';
        }

    } else {

        if (pageList) {
            pageList.style.display = '';
        }

        if (pageForm) {
            pageForm.style.display = 'none';
        }

    }

<<<<<<< HEAD
    initStudentPage(page);

});

function initStudentPage(page) {
=======
    initStudentPage();

});

function initStudentPage() {
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    const studentTableBody = document.getElementById('studentTableBody');
    const searchForm = document.getElementById('searchForm');
    const studentForm = document.getElementById('studentForm');

    // =========================
    // TRANG DANH SÁCH
    // =========================
    if (studentTableBody) {
<<<<<<< HEAD
        loadStudentPermissions().finally(() => {
            if (!requireStudentView()) {
                return;
            }
            if (searchForm) {
                searchForm.addEventListener('submit', handleSearch);
            }
            loadStudents();
        });
=======
        loadStudents();

        if (searchForm) {
            searchForm.addEventListener('submit', handleSearch);
        }
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    }

    // =========================
    // TRANG FORM THÊM / SỬA / XEM
    // =========================
<<<<<<< HEAD
    if (studentForm && page === 'form') {
=======
    if (studentForm) {
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
        initStudentForm();
    }
}

// =========================
// LIST STUDENTS
// =========================

async function loadStudents(keyword = '') {
    const studentTableBody = document.getElementById('studentTableBody');

    if (!studentTableBody) {
        return;
    }

    try {
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted py-4">
                    Đang tải dữ liệu...
                </td>
            </tr>
        `;

        const trimmedKeyword = keyword.trim();

        const url = trimmedKeyword
            ? `${API_ENDPOINTS.STUDENTS}?keyword=${encodeURIComponent(trimmedKeyword)}`
            : API_ENDPOINTS.STUDENTS;

<<<<<<< HEAD
        const response = await fetch(url, { headers: getAuthHeaders() });
=======
        const response = await fetch(url);
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const students = await response.json();

        renderStudents(students);
    } catch (error) {
        console.error('Lỗi khi tải danh sách sinh viên:', error);

        studentTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-danger py-4">
                    Không thể tải danh sách sinh viên
                </td>
            </tr>
        `;
    }
}

// =========================
// RENDER STUDENTS
// =========================

function renderStudents(students) {
    const studentTableBody = document.getElementById('studentTableBody');

    if (!studentTableBody) {
        return;
    }

    if (!students || students.length === 0) {
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted py-4">
                    Không có dữ liệu sinh viên
                </td>
            </tr>
        `;
        return;
    }

    studentTableBody.innerHTML = students
        .map((student) => {
            return `
                <tr>
                    <td>
                        ${escapeHtml(student.studentCode)}
                    </td>

                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <span>
                                ${escapeHtml(student.fullName)}
                            </span>
                        </div>
                    </td>

                    <td>
                        ${escapeHtml(student.email)}
                    </td>

                    <td>
                        ${escapeHtml(student.phone)}
                    </td>

                    <td>
                        ${escapeHtml(student.className)}
                    </td>

                    <td class="text-end">
                        <div class="table-actions d-flex justify-content-end gap-1">

                            <!-- Xem -->
<<<<<<< HEAD
                            ${studentPermissions.has('STUDENT_VIEW') ? `<a
=======
                            <a
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
                                class="btn btn-info btn-sm"
                                href="index.html?page=form&id=${encodeURIComponent(student.id)}&mode=view"
                                title="Xem"
                            >
                                <i class="bi bi-eye"></i>
<<<<<<< HEAD
                            </a>` : ''}

                            <!-- Sửa -->
                            ${studentPermissions.has('STUDENT_UPDATE') ? `<a
=======
                            </a>

                            <!-- Sửa -->
                            <a
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
                                class="btn btn-warning btn-sm"
                                href="index.html?page=form&id=${encodeURIComponent(student.id)}&mode=edit"
                                title="Sửa"
                            >
                                <i class="bi bi-pencil-square"></i>
<<<<<<< HEAD
                            </a>` : ''}

                            <!-- Xóa -->
                            ${studentPermissions.has('STUDENT_DELETE') ? `<button
=======
                            </a>

                            <!-- Xóa -->
                            <button
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
                                type="button"
                                class="btn btn-danger btn-sm"
                                title="Xóa"
                                onclick="deleteStudent('${escapeHtml(student.id)}')"
                            >
                                <i class="bi bi-trash"></i>
<<<<<<< HEAD
                            </button>` : ''}
=======
                            </button>
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac

                        </div>
                    </td>
                </tr>
            `;
        })
        .join('');
}

// =========================
// SEARCH
// =========================

function handleSearch(event) {
    event.preventDefault();
<<<<<<< HEAD
    if (!requireStudentView()) {
        return;
    }
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac

    const keywordInput = document.getElementById('keyword');

    const keyword = keywordInput
        ? keywordInput.value.trim()
        : '';

    loadStudents(keyword);
}

// =========================
// DELETE STUDENT
// =========================

window.deleteStudent = async function (id) {
    const confirmed = window.confirm(
        'Bạn có chắc muốn xóa sinh viên này?'
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`,
            {
<<<<<<< HEAD
                method: 'DELETE',
                headers: getAuthHeaders()
=======
                method: 'DELETE'
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
            }
        );

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        alert('Xóa sinh viên thành công');

        const keywordInput = document.getElementById('keyword');

        const keyword = keywordInput
            ? keywordInput.value.trim()
            : '';

        await loadStudents(keyword);
    } catch (error) {
        console.error('Lỗi khi xóa sinh viên:', error);

        alert('Xóa sinh viên thất bại');
    }
};

// =========================
// FORM
// =========================

function initStudentForm() {
    const studentForm = document.getElementById('studentForm');

    if (!studentForm) {
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);

    const id = urlParams.get('id');

    const mode = urlParams.get('mode') || 'create';

<<<<<<< HEAD
    loadStudentPermissions().then(() => {
        if (!requireStudentView()) {
            return;
        }
        if (mode === 'create' && !studentPermissions.has('STUDENT_CREATE')) {
            alert('Tài khoản của bạn không có quyền thêm sinh viên.');
            window.location.href = 'index.html?page=list';
            return;
        }
        if (mode === 'edit' && !studentPermissions.has('STUDENT_UPDATE')) {
            alert('Tài khoản của bạn không có quyền sửa sinh viên.');
            window.location.href = 'index.html?page=list';
            return;
        }
        if (mode === 'view' && !studentPermissions.has('STUDENT_VIEW')) {
            alert('Tài khoản của bạn không có quyền xem sinh viên.');
            window.location.href = 'index.html?page=list';
            return;
        }
        setupFormMode(mode);

        if ((mode === 'view' || mode === 'edit') && id) {
            loadStudentDetail(id, mode);
        }
    });
=======
    setupFormMode(mode);

    // Nếu là xem / sửa thì lấy dữ liệu từ API
    if ((mode === 'view' || mode === 'edit') && id) {
        loadStudentDetail(id, mode);
    }

>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    studentForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        if (mode === 'view') {
            return;
        }

        await saveStudent(mode, id);
    });
}

// =========================
// FORM MODE
// =========================

function setupFormMode(mode) {
    const pageTitle = document.getElementById('pageTitle');
    const pageHeading = document.getElementById('pageHeading');
    const breadcrumbTitle = document.getElementById('breadcrumbTitle');
    const formTitle = document.getElementById('formTitle');
    const submitButton = document.getElementById('submitButton');

    const fields = [
        document.getElementById('studentCode'),
        document.getElementById('fullName'),
        document.getElementById('email'),
        document.getElementById('phone'),
        document.getElementById('className')
    ];
<<<<<<< HEAD
    const usernameGroup = document.getElementById('studentUsernameGroup');
    const passwordGroup = document.getElementById('studentPasswordGroup');
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac

    // =========================
    // CREATE
    // =========================

    if (mode === 'create') {
        if (pageTitle) {
            pageTitle.textContent = 'Thêm sinh viên';
        }

        if (pageHeading) {
            pageHeading.textContent = 'Thêm sinh viên';
        }

        if (breadcrumbTitle) {
            breadcrumbTitle.textContent = 'Thêm';
        }

        if (formTitle) {
            formTitle.textContent = 'Thêm sinh viên mới';
        }

        if (submitButton) {
            submitButton.style.display = '';

            submitButton.innerHTML = `
                <i class="bi bi-plus-circle me-1"></i>
                Thêm sinh viên
            `;
        }

<<<<<<< HEAD
        if (usernameGroup) {
            usernameGroup.style.display = '';
        }
        if (passwordGroup) {
            passwordGroup.style.display = '';
        }

=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
        return;
    }

    // =========================
    // EDIT
    // =========================

    if (mode === 'edit') {
        if (pageTitle) {
            pageTitle.textContent = 'Sửa sinh viên';
        }

        if (pageHeading) {
            pageHeading.textContent = 'Sửa sinh viên';
        }

        if (breadcrumbTitle) {
            breadcrumbTitle.textContent = 'Sửa';
        }

        if (formTitle) {
            formTitle.textContent = 'Cập nhật sinh viên';
        }

        if (submitButton) {
            submitButton.style.display = '';

            submitButton.innerHTML = `
                <i class="bi bi-check-circle me-1"></i>
                Lưu thay đổi
            `;
        }

<<<<<<< HEAD
        if (usernameGroup) {
            usernameGroup.style.display = 'none';
        }
        if (passwordGroup) {
            passwordGroup.style.display = 'none';
        }

=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
        return;
    }

    // =========================
    // VIEW
    // =========================

    if (mode === 'view') {
        if (pageTitle) {
            pageTitle.textContent = 'Chi tiết sinh viên';
        }

        if (pageHeading) {
            pageHeading.textContent = 'Chi tiết sinh viên';
        }

        if (breadcrumbTitle) {
            breadcrumbTitle.textContent = 'Xem';
        }

        if (formTitle) {
            formTitle.textContent = 'Thông tin sinh viên';
        }

        if (submitButton) {
            submitButton.style.display = 'none';
        }

        fields.forEach((field) => {
            if (field) {
                field.readOnly = true;
            }
        });
<<<<<<< HEAD
        if (usernameGroup) {
            usernameGroup.style.display = 'none';
        }
        if (passwordGroup) {
            passwordGroup.style.display = 'none';
        }
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    }
}

// =========================
// GET STUDENT DETAIL
// =========================

async function loadStudentDetail(id, mode) {
    try {
        const response = await fetch(
<<<<<<< HEAD
            `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`,
            { headers: getAuthHeaders() }
=======
            `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
        );

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const student = await response.json();

        fillStudentForm(student);

        if (mode === 'view') {
            setFormReadOnly(true);
        }
    } catch (error) {
        console.error(
            'Lỗi khi lấy thông tin sinh viên:',
            error
        );

        alert('Không thể lấy thông tin sinh viên');

        window.location.href = 'index.html?page=list';
    }
}

// =========================
// FILL FORM
// =========================

function fillStudentForm(student) {
    const studentCode = document.getElementById('studentCode');
    const fullName = document.getElementById('fullName');
    const email = document.getElementById('email');
    const phone = document.getElementById('phone');
    const className = document.getElementById('className');

    if (studentCode) {
        studentCode.value = student.studentCode || '';
    }

    if (fullName) {
        fullName.value = student.fullName || '';
    }

    if (email) {
        email.value = student.email || '';
    }

    if (phone) {
        phone.value = student.phone || '';
    }

    if (className) {
        className.value = student.className || '';
    }
}

// =========================
// SAVE STUDENT
// =========================

async function saveStudent(mode, id = null) {
    try {
        const student = getStudentFormData();

        if (!validateStudent(student)) {
            return;
        }

        let url = API_ENDPOINTS.STUDENTS;
        let method = 'POST';

        // Sửa
        if (mode === 'edit' && id) {
            url = `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
<<<<<<< HEAD
            headers: getAuthHeaders(true),
=======

            headers: {
                'Content-Type': 'application/json'
            },

>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
            body: JSON.stringify(student)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const savedStudent = await response.json();

        console.log(
            'Sinh viên đã lưu:',
            savedStudent
        );

        alert(
            mode === 'edit'
                ? 'Cập nhật sinh viên thành công'
                : 'Thêm sinh viên thành công'
        );

        window.location.href = 'index.html?page=list';
    } catch (error) {
        console.error(
            'Lỗi khi lưu sinh viên:',
            error
        );

        alert(
            mode === 'edit'
                ? 'Cập nhật sinh viên thất bại'
                : 'Thêm sinh viên thất bại'
        );
    }
}

// =========================
// GET FORM DATA
// =========================

function getStudentFormData() {
    return {
        studentCode: getValue('studentCode'),
        fullName: getValue('fullName'),
        email: getValue('email'),
        phone: getValue('phone'),
<<<<<<< HEAD
        className: getValue('className'),
        username: getValue('studentUsername'),
        password: getValue('studentPassword')
=======
        className: getValue('className')
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    };
}

function getValue(id) {
    const element = document.getElementById(id);

    return element
        ? element.value.trim()
        : '';
}

// =========================
// VALIDATION
// =========================

function validateStudent(student) {
    if (!student.studentCode) {
        alert('Vui lòng nhập mã sinh viên');
        return false;
    }

    if (!student.fullName) {
        alert('Vui lòng nhập họ và tên');
        return false;
    }

    if (!student.email) {
        alert('Vui lòng nhập email');
        return false;
    }

    if (!student.phone) {
        alert('Vui lòng nhập số điện thoại');
        return false;
    }

    if (!student.className) {
        alert('Vui lòng nhập lớp');
        return false;
    }

<<<<<<< HEAD
    const username = document.getElementById('studentUsername');
    const password = document.getElementById('studentPassword');
    if (username && username.offsetParent !== null && !student.username) {
        alert('Vui lòng nhập tên tài khoản cho sinh viên');
        return false;
    }
    if (password && password.offsetParent !== null && student.password.length < 8) {
        alert('Mật khẩu tài khoản sinh viên phải có ít nhất 8 ký tự');
        return false;
    }

=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    return true;
}

// =========================
// READ ONLY
// =========================

function setFormReadOnly(readOnly) {
    const fields = [
        'studentCode',
        'fullName',
        'email',
        'phone',
        'className'
    ];

    fields.forEach((id) => {
        const element = document.getElementById(id);

        if (element) {
            element.readOnly = readOnly;
        }
    });
}

// =========================
// ESCAPE HTML
// =========================

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return '';
    }

    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
<<<<<<< HEAD
}

const studentLogoutButton = document.getElementById('studentLogoutButton');

if (studentLogoutButton) {
    studentLogoutButton.addEventListener('click', async () => {
        const token = sessionStorage.getItem(TOKEN_KEY);

        try {
            if (token) {
                const response = await fetch(
                    'http://localhost:8080/api/auth/logout',
                    {
                        method: 'POST',
                        headers: getAuthHeaders()
                    }
                );

                if (!response.ok) {
                    throw new Error(`Đăng xuất thất bại (HTTP ${response.status})`);
                }
            }
        } catch (error) {
            console.error(error);
            alert(error.message);
            return;
        }

        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(USER_ID_KEY);
        sessionStorage.removeItem('n1.username');
        studentPermissions = new Set();
        updateStudentSessionUi();
        window.location.href = '/index.html';
    });
}

updateStudentSessionUi();
=======
}
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
