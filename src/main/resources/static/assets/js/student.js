import { API_ENDPOINTS } from './config.js';
import { requireAuth } from './auth.js?v=1.0';

function isAdminUser() {
    return window.currentUser?.role === 'ADMIN';
}

document.addEventListener('DOMContentLoaded', async () => {
    const user = await requireAuth();
    if (!user) return;

    const params = new URLSearchParams(window.location.search);
    const page = params.get('page') || 'list';
    const mode = params.get('mode') || 'create';

    if (page === 'form' && mode !== 'view' && !isAdminUser()) {
        window.location.href = 'index.html?page=list';
        return;
    }

    const createBtn = document.getElementById('createStudentBtn');
    if (createBtn && !isAdminUser()) {
        createBtn.style.display = 'none';
    }

    const pageList = document.getElementById('page-list');
    const pageForm = document.getElementById('page-form');

    if (page === 'form') {
        if (pageList) pageList.style.display = 'none';
        if (pageForm) pageForm.style.display = '';
    } else {
        if (pageList) pageList.style.display = '';
        if (pageForm) pageForm.style.display = 'none';
    }

    initStudentPage();
});

function initStudentPage() {
    const studentTableBody = document.getElementById('studentTableBody');
    const searchForm = document.getElementById('searchForm');
    const studentForm = document.getElementById('studentForm');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    const keywordInput = document.getElementById('keyword');

    // =========================
    // TRANG DANH SÁCH
    // =========================
    if (studentTableBody) {
        loadStudents();
        loadNextCode();

        if (searchForm) {
            searchForm.addEventListener('submit', handleSearch);
        }

        if (keywordInput && clearSearchBtn) {
            keywordInput.addEventListener('input', () => {
                clearSearchBtn.style.display = keywordInput.value.trim() ? '' : 'none';
            });
            clearSearchBtn.addEventListener('click', () => {
                keywordInput.value = '';
                clearSearchBtn.style.display = 'none';
                loadStudents('');
            });
        }
    }

    // =========================
    // TRANG FORM THÊM / SỬA / XEM
    // =========================
    if (studentForm) {
        initStudentForm();
    }
}

// =========================
// AUTO GENERATE CODE
// =========================

async function loadNextCode() {
    try {
        const response = await fetch(`${API_ENDPOINTS.STUDENTS}/next-code`);
        if (!response.ok) return null;
        const data = await response.json();
        const nextCode = data.nextStudentCode || 'SV001';

        const statNextCode = document.getElementById('statNextCode');
        if (statNextCode) {
            statNextCode.textContent = nextCode;
        }

        const studentCodeInput = document.getElementById('studentCode');
        if (studentCodeInput && (!studentCodeInput.value || studentCodeInput.value === 'Đang tạo mã...')) {
            studentCodeInput.value = nextCode;
        }

        return nextCode;
    } catch (e) {
        console.warn('Không thể lấy mã sinh viên tiếp theo:', e);
        return null;
    }
}

// =========================
// LIST STUDENTS
// =========================

let allLoadedStudents = [];
let activeClassFilter = 'ALL';

async function loadStudents(keyword = '') {
    const studentTableBody = document.getElementById('studentTableBody');
    if (!studentTableBody) return;

    try {
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-muted py-5">
                    <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
                    Đang tải dữ liệu sinh viên...
                </td>
            </tr>
        `;

        const trimmedKeyword = keyword.trim();
        const url = trimmedKeyword
            ? `${API_ENDPOINTS.STUDENTS}?keyword=${encodeURIComponent(trimmedKeyword)}`
            : API_ENDPOINTS.STUDENTS;

        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        allLoadedStudents = await response.json();
        renderClassTabs(allLoadedStudents);
        applyClassFilterAndRender();
    } catch (error) {
        console.error('Lỗi khi tải danh sách sinh viên:', error);
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-danger py-5">
                    <i class="bi bi-exclamation-triangle fs-3 d-block mb-2"></i>
                    Không thể tải danh sách sinh viên. Vui lòng thử lại sau.
                </td>
            </tr>
        `;
    }
}

function renderClassTabs(students) {
    const tabsContainer = document.getElementById('classFilterTabs');
    if (!tabsContainer) return;

    const counts = new Map();
    ['IT24M', 'EE24M'].forEach((cls) => counts.set(cls, 0));
    (students || []).forEach((s) => {
        const cls = (s.className || '').trim() || 'Chưa xếp lớp';
        counts.set(cls, (counts.get(cls) || 0) + 1);
    });

    if (activeClassFilter !== 'ALL' && !counts.has(activeClassFilter)) {
        activeClassFilter = 'ALL';
    }

    const totalAll = students ? students.length : 0;
    const classNames = Array.from(counts.keys());

    tabsContainer.innerHTML = [
        `<button type="button" class="btn btn-sm ${activeClassFilter === 'ALL' ? 'btn-primary' : 'btn-outline-secondary'} fw-semibold" data-class-filter="ALL">
            Tất cả <span class="badge ${activeClassFilter === 'ALL' ? 'bg-white text-primary' : 'bg-secondary-subtle text-secondary'} ms-1">${totalAll}</span>
        </button>`,
        ...classNames.map((cls) => {
            const isActive = activeClassFilter === cls;
            return `<button type="button" class="btn btn-sm ${isActive ? 'btn-primary' : 'btn-outline-secondary'} fw-semibold" data-class-filter="${escapeHtml(cls)}">
                ${escapeHtml(cls)} <span class="badge ${isActive ? 'bg-white text-primary' : 'bg-secondary-subtle text-secondary'} ms-1">${counts.get(cls)}</span>
            </button>`;
        })
    ].join('');

    tabsContainer.querySelectorAll('button[data-class-filter]').forEach((btn) => {
        btn.addEventListener('click', () => {
            activeClassFilter = btn.dataset.classFilter || 'ALL';
            renderClassTabs(allLoadedStudents);
            applyClassFilterAndRender();
        });
    });

    const createBtn = document.getElementById('createStudentBtn');
    if (createBtn) {
        createBtn.href = activeClassFilter !== 'ALL' && activeClassFilter !== 'Chưa xếp lớp'
            ? `index.html?page=form&className=${encodeURIComponent(activeClassFilter)}`
            : 'index.html?page=form';
    }
}

function applyClassFilterAndRender() {
    const filtered = activeClassFilter === 'ALL'
        ? allLoadedStudents
        : allLoadedStudents.filter((s) => ((s.className || '').trim() || 'Chưa xếp lớp') === activeClassFilter);

    // Cập nhật thống kê tổng toàn trường
    const statTotalStudents = document.getElementById('statTotalStudents');
    const statTotalClasses = document.getElementById('statTotalClasses');
    if (statTotalStudents) statTotalStudents.textContent = allLoadedStudents ? allLoadedStudents.length : 0;
    if (statTotalClasses) {
        const uniqueClasses = new Set((allLoadedStudents || []).map((s) => (s.className || '').trim()).filter(Boolean));
        statTotalClasses.textContent = uniqueClasses.size || 0;
    }

    renderStudents(filtered);
}

// =========================
// RENDER STUDENTS
// =========================

function renderStudents(students) {
    const studentTableBody = document.getElementById('studentTableBody');
    if (!studentTableBody) return;

    // Cập nhật số lượng hiển thị trên bảng hiện tại
    const tableCountBadge = document.getElementById('tableCountBadge');
    const total = students ? students.length : 0;
    if (tableCountBadge) tableCountBadge.textContent = `${total} sinh viên`;

    if (!students || students.length === 0) {
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-muted py-5">
                    <i class="bi bi-inbox fs-2 d-block text-secondary mb-2"></i>
                    Chưa có dữ liệu sinh viên phù hợp.
                </td>
            </tr>
        `;
        return;
    }

    const avatarGradients = [
        'linear-gradient(135deg, #2563eb, #1d4ed8)',
        'linear-gradient(135deg, #0d9488, #0f766e)',
        'linear-gradient(135deg, #7c3aed, #6d28d9)',
        'linear-gradient(135deg, #db2777, #be185d)',
        'linear-gradient(135deg, #d97706, #b45309)',
        'linear-gradient(135deg, #059669, #047857)',
    ];

    studentTableBody.innerHTML = students
        .map((student, index) => {
            const initials = getInitials(student.fullName || student.studentCode || 'SV');
            const bgGradient = avatarGradients[index % avatarGradients.length];

            return `
                <tr>
                    <td class="text-center fw-bold text-secondary">
                        ${index + 1}
                    </td>

                    <td>
                        <span class="student-code-badge font-monospace fw-bold text-primary">
                            ${escapeHtml(student.studentCode)}
                        </span>
                    </td>

                    <td>
                        <div class="student-identity">
                            <div class="student-avatar" style="background: ${bgGradient}">
                                ${escapeHtml(initials)}
                            </div>
                            <div>
                                <div class="student-name fw-semibold">
                                    ${escapeHtml(student.fullName)}
                                </div>
                            </div>
                        </div>
                    </td>

                    <td>
                        <span class="badge bg-light text-dark border px-2 py-1">
                            <i class="bi bi-calendar-event text-primary me-1"></i>${escapeHtml(student.dateOfBirth || '---')}
                        </span>
                    </td>

                    <td>
                        <a href="mailto:${escapeHtml(student.email)}" class="text-decoration-none text-secondary">
                            <i class="bi bi-envelope text-muted me-1"></i>${escapeHtml(student.email || '---')}
                        </a>
                    </td>

                    <td>
                        <a href="tel:${escapeHtml(student.phone)}" class="text-decoration-none text-secondary">
                            <i class="bi bi-telephone text-muted me-1"></i>${escapeHtml(student.phone || '---')}
                        </a>
                    </td>

                    <td>
                        <span class="class-badge">
                            <i class="bi bi-mortarboard me-1"></i>${escapeHtml(student.className || 'Chưa xếp lớp')}
                        </span>
                    </td>

                    <td class="text-end">
                        <div class="table-actions">
                            <!-- Xem -->
                            <a
                                class="action-btn view"
                                href="index.html?page=form&id=${encodeURIComponent(student.id)}&mode=view"
                                title="Xem chi tiết"
                            >
                                <i class="bi bi-eye"></i>
                            </a>

                            ${isAdminUser() ? `
                            <!-- Sửa -->
                            <a
                                class="action-btn edit"
                                href="index.html?page=form&id=${encodeURIComponent(student.id)}&mode=edit"
                                title="Sửa thông tin"
                            >
                                <i class="bi bi-pencil-square"></i>
                            </a>

                            <!-- Xóa -->
                            <button
                                type="button"
                                class="action-btn delete"
                                title="Xóa sinh viên"
                                onclick="deleteStudent('${escapeHtml(student.id)}', '${escapeHtml(student.fullName)}')"
                            >
                                <i class="bi bi-trash"></i>
                            </button>
                            ` : ''}
                        </div>
                    </td>
                </tr>
            `;
        })
        .join('');
}

function getInitials(name) {
    if (!name) return 'SV';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    const first = parts[0][0];
    const last = parts[parts.length - 1][0];
    return (last + first).toUpperCase();
}

// =========================
// SEARCH
// =========================

function handleSearch(event) {
    event.preventDefault();
    const keywordInput = document.getElementById('keyword');
    const keyword = keywordInput ? keywordInput.value.trim() : '';
    loadStudents(keyword);
}

// =========================
// DELETE STUDENT
// =========================

window.deleteStudent = async function (id, name = 'này') {
    const confirmed = window.confirm(
        `Bạn có chắc chắn muốn xóa sinh viên "${name}" khỏi hệ thống không?\nThao tác này không thể hoàn tác!`
    );

    if (!confirmed) return;

    try {
        const response = await fetch(
            `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`,
            { method: 'DELETE' }
        );

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        showToast(`Đã xóa sinh viên "${name}" thành công!`, 'success');

        const keywordInput = document.getElementById('keyword');
        const keyword = keywordInput ? keywordInput.value.trim() : '';
        await loadStudents(keyword);
        await loadNextCode();
    } catch (error) {
        console.error('Lỗi khi xóa sinh viên:', error);
        showToast('Xóa sinh viên thất bại! Vui lòng thử lại.', 'danger');
    }
};

// =========================
// FORM
// =========================

function initStudentForm() {
    const studentForm = document.getElementById('studentForm');
    if (!studentForm) return;

    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');
    const mode = urlParams.get('mode') || 'create';

    setupFormMode(mode);

    const refreshBtn = document.getElementById('refreshCodeBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', async () => {
            const next = await loadNextCode();
            if (next) showToast(`Mã sinh viên tiếp theo: ${next}`, 'info');
        });
    }

    if (mode === 'create') {
        loadNextCode();
        const presetClass = urlParams.get('className');
        const classInput = document.getElementById('className');
        if (presetClass && classInput) {
            classInput.value = presetClass;
        }
    } else if ((mode === 'view' || mode === 'edit') && id) {
        loadStudentDetail(id, mode);
    }

    studentForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (mode === 'view') return;
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
    const formStatusBadge = document.getElementById('formStatusBadge');
    const codeAutoBadge = document.getElementById('codeAutoBadge');
    const refreshCodeBtn = document.getElementById('refreshCodeBtn');
    const studentCodeInput = document.getElementById('studentCode');

    const fields = [
        document.getElementById('fullName'),
        document.getElementById('dateOfBirth'),
        document.getElementById('email'),
        document.getElementById('phone'),
        document.getElementById('className')
    ];

    if (mode === 'create') {
        if (pageTitle) pageTitle.textContent = 'Thêm sinh viên | School Manager';
        if (pageHeading) pageHeading.textContent = 'Thêm sinh viên mới';
        if (breadcrumbTitle) breadcrumbTitle.textContent = 'Thêm mới';
        if (formTitle) formTitle.innerHTML = '<i class="bi bi-person-plus-fill me-2 text-primary"></i>Thêm sinh viên mới';
        if (formStatusBadge) {
            formStatusBadge.textContent = 'Tạo mới';
            formStatusBadge.className = 'badge bg-success-subtle text-success border border-success-subtle px-2 py-1';
        }
        if (codeAutoBadge) codeAutoBadge.style.display = '';
        if (refreshCodeBtn) refreshCodeBtn.style.display = '';
        if (studentCodeInput) studentCodeInput.readOnly = true;

        if (submitButton) {
            submitButton.style.display = '';
            submitButton.className = 'btn btn-primary px-4 fw-semibold shadow-sm';
            submitButton.innerHTML = '<i class="bi bi-plus-circle me-1"></i>Thêm sinh viên';
        }
        return;
    }

    if (mode === 'edit') {
        if (pageTitle) pageTitle.textContent = 'Sửa sinh viên | School Manager';
        if (pageHeading) pageHeading.textContent = 'Chỉnh sửa sinh viên';
        if (breadcrumbTitle) breadcrumbTitle.textContent = 'Chỉnh sửa';
        if (formTitle) formTitle.innerHTML = '<i class="bi bi-pencil-square me-2 text-warning"></i>Cập nhật thông tin sinh viên';
        if (formStatusBadge) {
            formStatusBadge.textContent = 'Chỉnh sửa';
            formStatusBadge.className = 'badge bg-warning-subtle text-warning border border-warning-subtle px-2 py-1';
        }
        if (codeAutoBadge) codeAutoBadge.style.display = 'none';
        if (refreshCodeBtn) refreshCodeBtn.style.display = 'none';
        if (studentCodeInput) studentCodeInput.readOnly = true;

        if (submitButton) {
            submitButton.style.display = '';
            submitButton.className = 'btn btn-warning px-4 fw-semibold shadow-sm text-dark';
            submitButton.innerHTML = '<i class="bi bi-check-circle me-1"></i>Lưu thay đổi';
        }
        return;
    }

    if (mode === 'view') {
        if (pageTitle) pageTitle.textContent = 'Chi tiết sinh viên | School Manager';
        if (pageHeading) pageHeading.textContent = 'Chi tiết sinh viên';
        if (breadcrumbTitle) breadcrumbTitle.textContent = 'Chi tiết';
        if (formTitle) formTitle.innerHTML = '<i class="bi bi-person-lines-fill me-2 text-info"></i>Hồ sơ sinh viên';
        if (formStatusBadge) {
            formStatusBadge.textContent = 'Xem chi tiết';
            formStatusBadge.className = 'badge bg-info-subtle text-info border border-info-subtle px-2 py-1';
        }
        if (codeAutoBadge) codeAutoBadge.style.display = 'none';
        if (refreshCodeBtn) refreshCodeBtn.style.display = 'none';

        if (submitButton) submitButton.style.display = 'none';

        if (studentCodeInput) studentCodeInput.readOnly = true;
        fields.forEach((field) => {
            if (field) field.readOnly = true;
        });
    }
}

// =========================
// GET STUDENT DETAIL
// =========================

async function loadStudentDetail(id, mode) {
    try {
        const response = await fetch(
            `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const student = await response.json();
        fillStudentForm(student);
    } catch (error) {
        console.error('Lỗi khi lấy thông tin sinh viên:', error);
        showToast('Không thể lấy thông tin sinh viên!', 'danger');
        setTimeout(() => {
            window.location.href = 'index.html?page=list';
        }, 1500);
    }
}

// =========================
// FILL FORM
// =========================

function fillStudentForm(student) {
    const studentCode = document.getElementById('studentCode');
    const fullName = document.getElementById('fullName');
    const dateOfBirth = document.getElementById('dateOfBirth');
    const email = document.getElementById('email');
    const phone = document.getElementById('phone');
    const className = document.getElementById('className');

    if (studentCode) studentCode.value = student.studentCode || '';
    if (fullName) fullName.value = student.fullName || '';
    if (dateOfBirth) dateOfBirth.value = student.dateOfBirth || '';
    if (email) email.value = student.email || '';
    if (phone) phone.value = student.phone || '';
    if (className) className.value = student.className || '';
}

// =========================
// SAVE STUDENT
// =========================

async function saveStudent(mode, id = null) {
    const submitButton = document.getElementById('submitButton');
    const originalBtnHtml = submitButton ? submitButton.innerHTML : '';

    try {
        const student = getStudentFormData();
        if (!validateStudent(student)) return;

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang lưu...';
        }

        let url = API_ENDPOINTS.STUDENTS;
        let method = 'POST';

        if (mode === 'edit' && id) {
            url = `${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(id)}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student)
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const savedStudent = await response.json();
        showToast(
            mode === 'edit'
                ? `Cập nhật sinh viên ${savedStudent.studentCode} thành công!`
                : `Thêm sinh viên ${savedStudent.studentCode} thành công!`,
            'success'
        );

        setTimeout(() => {
            window.location.href = 'index.html?page=list';
        }, 1200);
    } catch (error) {
        console.error('Lỗi khi lưu sinh viên:', error);
        showToast(
            mode === 'edit'
                ? 'Cập nhật sinh viên thất bại!'
                : 'Thêm sinh viên thất bại!',
            'danger'
        );
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.innerHTML = originalBtnHtml;
        }
    }
}

function getStudentFormData() {
    return {
        studentCode: getValue('studentCode'),
        fullName: getValue('fullName'),
        dateOfBirth: getValue('dateOfBirth'),
        email: getValue('email'),
        phone: getValue('phone'),
        className: getValue('className')
    };
}

function getValue(id) {
    const element = document.getElementById(id);
    return element ? element.value.trim() : '';
}

function validateStudent(student) {
    if (!student.fullName) {
        showToast('Vui lòng nhập họ và tên sinh viên', 'warning');
        document.getElementById('fullName')?.focus();
        return false;
    }

    if (!student.dateOfBirth) {
        showToast('Vui lòng nhập ngày sinh của sinh viên (dd/MM/yyyy)', 'warning');
        document.getElementById('dateOfBirth')?.focus();
        return false;
    }

    if (!student.email) {
        showToast('Vui lòng nhập email', 'warning');
        document.getElementById('email')?.focus();
        return false;
    }

    if (!student.phone) {
        showToast('Vui lòng nhập số điện thoại', 'warning');
        document.getElementById('phone')?.focus();
        return false;
    }

    if (!student.className) {
        showToast('Vui lòng nhập lớp sinh hoạt', 'warning');
        document.getElementById('className')?.focus();
        return false;
    }

    return true;
}

// =========================
// TOAST NOTIFICATION
// =========================

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer') || document.body;
    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;

    const icons = {
        success: 'bi-check-circle-fill text-success',
        danger: 'bi-exclamation-octagon-fill text-danger',
        warning: 'bi-exclamation-triangle-fill text-warning',
        info: 'bi-info-circle-fill text-info'
    };

    toast.innerHTML = `
        <i class="bi ${icons[type] || icons.info} fs-4"></i>
        <div class="flex-grow-1 fw-semibold" style="font-size: 0.9375rem;">
            ${escapeHtml(message)}
        </div>
        <button type="button" class="btn-close ms-2" aria-label="Close"></button>
    `;

    toast.querySelector('.btn-close').addEventListener('click', () => {
        toast.remove();
    });

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
