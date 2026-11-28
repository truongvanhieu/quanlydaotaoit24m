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

    const transferBtn = document.getElementById('transferClassBtn');
    if (transferBtn) {
        transferBtn.style.display = isAdminUser() ? '' : 'none';
    }
    document.querySelectorAll('.admin-only-col').forEach((el) => {
        el.style.display = isAdminUser() ? '' : 'none';
    });

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

        initTransferClassFeature();
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
let currentlyRenderedStudents = [];
let activeClassFilter = 'ALL';
const selectedStudentIds = new Set();

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

    currentlyRenderedStudents = students || [];
    const colSpan = isAdminUser() ? 9 : 8;

    // Cập nhật số lượng hiển thị trên bảng hiện tại
    const tableCountBadge = document.getElementById('tableCountBadge');
    const total = students ? students.length : 0;
    if (tableCountBadge) tableCountBadge.textContent = `${total} sinh viên`;

    if (!students || students.length === 0) {
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="${colSpan}" class="text-center text-muted py-5">
                    <i class="bi bi-inbox fs-2 d-block text-secondary mb-2"></i>
                    Chưa có dữ liệu sinh viên phù hợp.
                </td>
            </tr>
        `;
        updateSelectionUi();
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
            const isChecked = selectedStudentIds.has(String(student.id));

            return `
                <tr>
                    ${isAdminUser() ? `
                    <td class="text-center admin-only-col">
                        <input
                            type="checkbox"
                            class="form-check-input student-row-checkbox"
                            data-student-id="${escapeHtml(student.id)}"
                            ${isChecked ? 'checked' : ''}
                            aria-label="Chọn sinh viên ${escapeHtml(student.fullName)}"
                        />
                    </td>
                    ` : ''}
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
                            <!-- Chuyển lớp nhanh -->
                            <button
                                type="button"
                                class="action-btn text-primary"
                                title="Chuyển lớp sinh hoạt"
                                onclick="openTransferClassModalForStudent('${escapeHtml(student.id)}')"
                            >
                                <i class="bi bi-arrow-left-right"></i>
                            </button>

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

    studentTableBody.querySelectorAll('.student-row-checkbox').forEach((cb) => {
        cb.addEventListener('change', () => {
            const id = cb.dataset.studentId;
            if (!id) return;
            if (cb.checked) {
                selectedStudentIds.add(id);
            } else {
                selectedStudentIds.delete(id);
            }
            updateSelectionUi();
        });
    });

    updateSelectionUi();
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

// =========================
// CHUYỂN LỚP SINH HOẠT
// =========================

let pendingTransferIds = [];
let transferModalInstance = null;

function initTransferClassFeature() {
    const selectAllCb = document.getElementById('selectAllStudentsCheckbox');
    const transferBtn = document.getElementById('transferClassBtn');
    const transferForm = document.getElementById('transferClassForm');

    if (selectAllCb) {
        selectAllCb.addEventListener('change', () => {
            currentlyRenderedStudents.forEach((s) => {
                const id = String(s.id);
                if (selectAllCb.checked) {
                    selectedStudentIds.add(id);
                } else {
                    selectedStudentIds.delete(id);
                }
            });
            document.querySelectorAll('.student-row-checkbox').forEach((cb) => {
                cb.checked = selectAllCb.checked;
            });
            updateSelectionUi();
        });
    }

    if (transferBtn) {
        transferBtn.addEventListener('click', () => {
            if (!isAdminUser()) return;
            const ids = Array.from(selectedStudentIds);
            if (ids.length === 0) {
                showToast('Hãy tick chọn ít nhất 1 sinh viên trong bảng (hoặc bấm nút mũi tên ở dòng sinh viên) để chuyển lớp.', 'warning');
                return;
            }
            openTransferClassModal(ids);
        });
    }

    if (transferForm) {
        transferForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const targetInput = document.getElementById('targetClassNameInput');
            const confirmBtn = document.getElementById('confirmTransferClassBtn');
            const targetClassName = targetInput ? targetInput.value.trim() : '';

            if (!targetClassName) {
                showToast('Vui lòng nhập hoặc chọn lớp sinh hoạt đích.', 'warning');
                return;
            }
            if (!pendingTransferIds.length) {
                showToast('Không có sinh viên nào được chọn.', 'warning');
                return;
            }

            if (confirmBtn) {
                confirmBtn.disabled = true;
                confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang chuyển...';
            }

            try {
                const resp = await fetch(`${API_ENDPOINTS.STUDENTS}/transfer-class`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        studentIds: pendingTransferIds,
                        targetClassName
                    })
                });
                const data = await resp.json().catch(() => ({}));
                if (!resp.ok) {
                    throw new Error(data.message || `HTTP ${resp.status}`);
                }

                selectedStudentIds.clear();
                closeTransferClassModal();
                showToast(data.message || `Đã chuyển lớp sang ${targetClassName} thành công!`, 'success');
                const keywordInput = document.getElementById('keyword');
                await loadStudents(keywordInput ? keywordInput.value.trim() : '');
            } catch (err) {
                console.error('Lỗi chuyển lớp:', err);
                showToast(err.message || 'Không thể chuyển lớp sinh viên.', 'danger');
            } finally {
                if (confirmBtn) {
                    confirmBtn.disabled = false;
                    confirmBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Xác nhận chuyển lớp';
                }
            }
        });
    }
}

function updateSelectionUi() {
    const transferBtn = document.getElementById('transferClassBtn');
    const selectAllCb = document.getElementById('selectAllStudentsCheckbox');

    const count = selectedStudentIds.size;
    if (transferBtn) {
        transferBtn.innerHTML = count > 0
            ? `<i class="bi bi-arrow-left-right me-1"></i>Chuyển lớp (${count})`
            : `<i class="bi bi-arrow-left-right me-1"></i>Chuyển lớp`;
        transferBtn.classList.toggle('btn-primary', count > 0);
        transferBtn.classList.toggle('btn-outline-primary', count === 0);
    }

    if (selectAllCb) {
        if (!currentlyRenderedStudents.length) {
            selectAllCb.checked = false;
            selectAllCb.indeterminate = false;
        } else {
            const visibleSelected = currentlyRenderedStudents.filter((s) => selectedStudentIds.has(String(s.id))).length;
            selectAllCb.checked = visibleSelected === currentlyRenderedStudents.length;
            selectAllCb.indeterminate = visibleSelected > 0 && visibleSelected < currentlyRenderedStudents.length;
        }
    }
}

function openTransferClassModal(studentIds) {
    pendingTransferIds = studentIds.slice();
    const summaryEl = document.getElementById('transferStudentSummary');
    const targetInput = document.getElementById('targetClassNameInput');
    const datalist = document.getElementById('transferClassOptions');
    const pillsContainer = document.getElementById('transferQuickClassPills');
    const modalEl = document.getElementById('transferClassModal');

    const selectedStudents = allLoadedStudents.filter((s) => pendingTransferIds.includes(String(s.id)));
    if (summaryEl) {
        const preview = selectedStudents.slice(0, 3).map((s) => `${s.fullName} (${s.studentCode})`).join(', ');
        const extra = selectedStudents.length > 3 ? ` và ${selectedStudents.length - 3} sinh viên khác` : '';
        summaryEl.innerHTML = `Đã chọn <strong>${selectedStudents.length}</strong> sinh viên: ${escapeHtml(preview)}${escapeHtml(extra)}`;
    }

    const existingClasses = Array.from(new Set(
        ['IT24M', 'EE24M', 'IT24A', ...allLoadedStudents.map((s) => (s.className || '').trim()).filter(Boolean)]
    ));

    if (datalist) {
        datalist.innerHTML = existingClasses.map((cls) => `<option value="${escapeHtml(cls)}"></option>`).join('');
    }
    if (pillsContainer) {
        pillsContainer.innerHTML = existingClasses.map((cls) =>
            `<button type="button" class="btn btn-sm btn-outline-secondary quick-class-pill" data-cls="${escapeHtml(cls)}">${escapeHtml(cls)}</button>`
        ).join('');
        pillsContainer.querySelectorAll('.quick-class-pill').forEach((btn) => {
            btn.addEventListener('click', () => {
                if (targetInput) targetInput.value = btn.dataset.cls || '';
            });
        });
    }
    if (targetInput) {
        targetInput.value = '';
    }

    if (modalEl) {
        if (window.bootstrap && window.bootstrap.Modal) {
            transferModalInstance = window.bootstrap.Modal.getOrCreateInstance(modalEl);
            transferModalInstance.show();
        } else {
            modalEl.style.display = 'block';
            modalEl.classList.add('show');
            modalEl.removeAttribute('aria-hidden');
        }
    }
}

function closeTransferClassModal() {
    const modalEl = document.getElementById('transferClassModal');
    if (transferModalInstance) {
        transferModalInstance.hide();
    } else if (modalEl) {
        modalEl.style.display = 'none';
        modalEl.classList.remove('show');
        modalEl.setAttribute('aria-hidden', 'true');
    }
}

window.openTransferClassModalForStudent = function (studentId) {
    if (!isAdminUser()) return;
    openTransferClassModal([String(studentId)]);
};
