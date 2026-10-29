import { API_ENDPOINTS } from './config.js';

let studentClassesById = new Map();
let advisorClasses = [];
let studentPageInitialized = false;
let advisorPageInitialized = false;

document.addEventListener('DOMContentLoaded', () => {
    showPage(getRequestedPage());
});

function getRequestedPage() {
    const requestedPage = new URLSearchParams(window.location.search).get('page');
    return ['form', 'swagger', 'lecturers', 'classes', 'statuses', 'status-histories'].includes(requestedPage)
        ? requestedPage
        : 'list';
}

function showPage(page) {
    const managementPages = ['classes', 'statuses', 'status-histories'];
    const pageVisibility = {
        'page-list': page === 'list',
        'page-form': page === 'form',
        'page-lecturers': page === 'lecturers',
        'page-management': managementPages.includes(page),
        'page-swagger': page === 'swagger'
    };

    Object.entries(pageVisibility).forEach(([id, visible]) => {
        const element = document.getElementById(id);
        if (element) {
            element.style.display = visible ? '' : 'none';
        }
    });

    const navPageById = {
        studentNavLink: ['list', 'form'],
        instructorNavLink: ['lecturers'],
        classesNavLink: ['classes'],
        statusesNavLink: ['statuses'],
        statusHistoriesNavLink: ['status-histories'],
        swaggerNavLink: ['swagger']
    };
    Object.entries(navPageById).forEach(([id, pages]) => {
        document.getElementById(id)?.classList.toggle('active', pages.includes(page));
    });
    document.getElementById('swaggerNavLink')?.classList.toggle('active', page === 'swagger');

    if (page === 'lecturers' && !advisorPageInitialized) {
        advisorPageInitialized = true;
        initAdvisorPage();
    } else if (['list', 'form'].includes(page) && !studentPageInitialized) {
        studentPageInitialized = true;
        initStudentPage();
    }
}

function initStudentPage() {
    const studentTableBody = document.getElementById('studentTableBody');
    const searchForm = document.getElementById('searchForm');
    const studentForm = document.getElementById('studentForm');

    // =========================
    // TRANG DANH SÁCH
    // =========================
    const classLoad = loadStudentClasses()
        .catch((error) => console.error('Lỗi khi tải danh sách lớp:', error));

    if (studentTableBody) {
        classLoad.finally(() => loadStudents());
        if (searchForm) {
            searchForm.addEventListener('submit', handleSearch);
        }
    }

    // =========================
    // TRANG FORM THÊM / SỬA / XEM
    // =========================
    if (studentForm) {
        classLoad.finally(initStudentForm);
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

        const response = await fetch(url);

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
                        ${escapeHtml(student.code || student.studentCode || '-')}
                    </td>

                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <span>
                                ${escapeHtml(student.fullName || '-')}
                            </span>
                        </div>
                    </td>

                    <td>${escapeHtml(student.gender || '-')}</td>
                    <td>${escapeHtml(getStudentClassLabel(student.studentClassessId))}</td>
                    <td>${escapeHtml(student.status || '-')}</td>

                    <td class="text-end">
                        <div class="table-actions d-flex justify-content-end gap-1">

                            <!-- Xem -->
                            <a
                                class="btn btn-info btn-sm"
                                href="index.html?page=form&id=${encodeURIComponent(student.id)}&mode=view"
                                title="Xem"
                            >
                                <i class="bi bi-eye"></i>
                            </a>

                            <!-- Sửa -->
                            <a
                                class="btn btn-warning btn-sm"
                                href="index.html?page=form&id=${encodeURIComponent(student.id)}&mode=edit"
                                title="Sửa"
                            >
                                <i class="bi bi-pencil-square"></i>
                            </a>

                            <!-- Xóa -->
                            <button
                                type="button"
                                class="btn btn-danger btn-sm"
                                title="Xóa"
                                onclick="deleteStudent('${escapeHtml(student.id)}')"
                            >
                                <i class="bi bi-trash"></i>
                            </button>

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
                method: 'DELETE'
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

    setupFormMode(mode);

    // Nếu là xem / sửa thì lấy dữ liệu từ API
    if ((mode === 'view' || mode === 'edit') && id) {
        loadStudentDetail(id, mode);
    }

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
        ...getStudentFieldIds().map((id) => document.getElementById(id))
    ];

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
                field.disabled = true;
            }
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
    const classSelect = document.getElementById('studentClassessId');
    if (student.studentClassessId && classSelect && !studentClassesById.has(student.studentClassessId)) {
        const orphanedClass = document.createElement('option');
        orphanedClass.value = student.studentClassessId;
        orphanedClass.textContent = `ID lớp không có trong danh sách: ${student.studentClassessId}`;
        classSelect.append(orphanedClass);
    }

    const values = {
        studentCode: student.code || student.studentCode,
        fullName: student.fullName,
        dateOfBirth: student.dateOfBirth,
        gender: student.gender,
        personalIdentificationNumber: student.personalIdentificationNumber,
        dateOfIssue: student.dateOfIssue,
        cardPlace: student.cardPlace,
        address: student.address,
        currentAddress: student.currentAddress,
        academicYearYear: student.academicYearYear,
        departmentId: student.departmentId,
        majorId: student.majorId,
        trainingProgramId: student.trainingProgramId,
        status: student.status,
        studentClassessId: student.studentClassessId,
        admissionYear: formatDateTimeLocal(student.admissionYear),
        userId: student.userId
    };

    Object.entries(values).forEach(([id, value]) => {
        const field = document.getElementById(id);
        if (field) {
            field.value = value || '';
        }
    });

    const isActive = document.getElementById('isActive');
    if (isActive) {
        isActive.checked = student.isActive !== false;
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

            headers: {
                'Content-Type': 'application/json'
            },

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
        code: getValue('studentCode'),
        fullName: getValue('fullName'),
        dateOfBirth: getValue('dateOfBirth') || null,
        gender: getValue('gender') || null,
        personalIdentificationNumber: getValue('personalIdentificationNumber') || null,
        dateOfIssue: getValue('dateOfIssue') || null,
        cardPlace: getValue('cardPlace') || null,
        address: getValue('address') || null,
        currentAddress: getValue('currentAddress') || null,
        academicYearYear: getValue('academicYearYear') || null,
        departmentId: getValue('departmentId') || null,
        majorId: getValue('majorId') || null,
        trainingProgramId: getValue('trainingProgramId') || null,
        status: getValue('status') || null,
        studentClassessId: getValue('studentClassessId') || null,
        admissionYear: getValue('admissionYear') ? `${getValue('admissionYear')}:00` : null,
        userId: getValue('userId') || null,
        isActive: document.getElementById('isActive')?.checked ?? true
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
    if (!student.code) {
        alert('Vui lòng nhập mã sinh viên');
        return false;
    }

    if (!student.fullName) {
        alert('Vui lòng nhập họ và tên');
        return false;
    }

    if (student.studentClassessId && !isUuid(student.studentClassessId)) {
        alert('ID lớp không hợp lệ');
        return false;
    }

    for (const id of ['academicYearYear', 'departmentId', 'majorId', 'trainingProgramId', 'userId']) {
        const value = student[id];
        if (value && !isUuid(value)) {
            alert(`Trường ${id} phải là UUID hợp lệ`);
            return false;
        }
    }
    return true;
}

// =========================
// READ ONLY
// =========================

function setFormReadOnly(readOnly) {
    getStudentFieldIds().forEach((id) => {
        const element = document.getElementById(id);

        if (element) {
            element.disabled = readOnly;
        }
    });
}

function getStudentFieldIds() {
    return [
        'studentCode',
        'fullName',
        'dateOfBirth',
        'gender',
        'personalIdentificationNumber',
        'dateOfIssue',
        'cardPlace',
        'address',
        'currentAddress',
        'studentClassessId',
        'admissionYear',
        'academicYearYear',
        'departmentId',
        'majorId',
        'trainingProgramId',
        'userId',
        'status',
        'isActive'
    ];
}

async function loadStudentClasses() {
    const response = await fetch(API_ENDPOINTS.STUDENT_CLASSES);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    const classes = await response.json();
    studentClassesById = new Map(classes.map((studentClass) => [studentClass.id, studentClass]));
    setClassOptions(document.getElementById('studentClassessId'), classes);
}

function setClassOptions(select, classes) {
    if (!select) {
        return;
    }

    const selectedValue = select.value;
    select.innerHTML = '<option value="">-- Chọn lớp --</option>' + classes
        .map((studentClass) => {
            const label = [studentClass.code, studentClass.name].filter(Boolean).join(' - ') || studentClass.id;
            return `<option value="${escapeHtml(studentClass.id)}">${escapeHtml(label)}</option>`;
        })
        .join('');
    select.value = selectedValue;
}

function getStudentClassLabel(classId) {
    if (!classId) {
        return '-';
    }

    const studentClass = studentClassesById.get(classId);
    return studentClass
        ? [studentClass.code, studentClass.name].filter(Boolean).join(' - ') || classId
        : classId;
}

function formatDate(value) {
    return value ? String(value).slice(0, 10) : '-';
}

function formatDateTimeLocal(value) {
    return value ? String(value).slice(0, 16) : '';
}

function isUuid(value) {
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
}

function initAdvisorPage() {
    const form = document.getElementById('advisorForm');
    const addButton = document.getElementById('addAdvisorButton');
    const cancelButton = document.getElementById('cancelAdvisorButton');
    const tableBody = document.getElementById('advisorTableBody');

    if (addButton) {
        addButton.addEventListener('click', () => showAdvisorForm());
    }
    if (cancelButton) {
        cancelButton.addEventListener('click', hideAdvisorForm);
    }
    if (form) {
        form.addEventListener('submit', saveAdvisorAssignment);
    }
    if (tableBody) {
        tableBody.addEventListener('click', handleAdvisorTableAction);
    }

    loadAdvisorClasses()
        .then(loadAdvisorAssignments)
        .catch((error) => {
            console.error('Lỗi khi tải dữ liệu phân công giảng viên:', error);
            if (tableBody) {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Không thể tải dữ liệu phân công giảng viên. Vui lòng kiểm tra API.</td></tr>';
            }
        });
}

async function loadAdvisorClasses() {
    const response = await fetch(API_ENDPOINTS.STUDENT_CLASSES);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    advisorClasses = await response.json();
    setClassOptions(document.getElementById('advisorClassId'), advisorClasses);
}

async function loadAdvisorAssignments() {
    const response = await fetch(API_ENDPOINTS.ADVISOR_CLASSE_SECTIONS);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    renderAdvisorAssignments(await response.json());
}

function renderAdvisorAssignments(assignments) {
    const tableBody = document.getElementById('advisorTableBody');
    if (!tableBody) {
        return;
    }

    if (!assignments.length) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Chưa có phân công giảng viên</td></tr>';
        return;
    }

    tableBody.innerHTML = assignments.map((assignment) => {
        const studentClass = advisorClasses.find((item) => item.id === assignment.studentClasseId);
        const classLabel = studentClass
            ? [studentClass.code, studentClass.name].filter(Boolean).join(' - ')
            : (assignment.studentClasseId || '-');
        const active = assignment.isActive !== false;
        return `
            <tr>
                <td class="text-break">${escapeHtml(assignment.employeeId || '-')}</td>
                <td>${escapeHtml(classLabel)}</td>
                <td>${escapeHtml(formatDate(assignment.startDate))}</td>
                <td>${escapeHtml(formatDate(assignment.endDate))}</td>
                <td><span class="badge ${active ? 'text-bg-success' : 'text-bg-secondary'}">${active ? 'Đang hoạt động' : 'Đã ngừng'}</span></td>
                <td class="text-end">
                    <div class="table-actions">
                        <button type="button" class="btn btn-warning btn-sm" data-action="edit" data-id="${escapeHtml(assignment.id)}" title="Sửa"><i class="bi bi-pencil-square"></i></button>
                        <button type="button" class="btn btn-danger btn-sm" data-action="delete" data-id="${escapeHtml(assignment.id)}" title="Ngừng phân công"><i class="bi bi-trash"></i></button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    window.advisorAssignments = assignments;
}

function handleAdvisorTableAction(event) {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }

    const assignments = window.advisorAssignments || [];
    const assignment = assignments.find((item) => item.id === button.dataset.id);
    if (!assignment) {
        return;
    }

    if (button.dataset.action === 'edit') {
        showAdvisorForm(assignment);
    } else if (button.dataset.action === 'delete') {
        deleteAdvisorAssignment(assignment.id);
    }
}

function showAdvisorForm(assignment = null) {
    const form = document.getElementById('advisorForm');
    const card = document.getElementById('advisorFormCard');
    if (!form || !card) {
        return;
    }

    form.reset();
    document.getElementById('advisorAssignmentId').value = assignment?.id || '';
    document.getElementById('employeeId').value = assignment?.employeeId || '';
    const classSelect = document.getElementById('advisorClassId');
    if (assignment?.studentClasseId && !advisorClasses.some((item) => item.id === assignment.studentClasseId)) {
        const orphanedClass = document.createElement('option');
        orphanedClass.value = assignment.studentClasseId;
        orphanedClass.textContent = `ID lớp không có trong danh sách: ${assignment.studentClasseId}`;
        classSelect.append(orphanedClass);
    }
    document.getElementById('advisorClassId').value = assignment?.studentClasseId || '';
    document.getElementById('advisorStartDate').value = formatDateInput(assignment?.startDate);
    document.getElementById('advisorEndDate').value = formatDateInput(assignment?.endDate);
    document.getElementById('advisorIsActive').checked = assignment?.isActive !== false;
    document.getElementById('advisorFormTitle').textContent = assignment
        ? 'Cập nhật phân công giảng viên'
        : 'Thêm phân công giảng viên';
    card.style.display = '';
    card.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function formatDateInput(value) {
    return value ? String(value).slice(0, 10) : '';
}

function hideAdvisorForm() {
    const card = document.getElementById('advisorFormCard');
    const form = document.getElementById('advisorForm');
    if (card) {
        card.style.display = 'none';
    }
    if (form) {
        form.reset();
    }
}

async function saveAdvisorAssignment(event) {
    event.preventDefault();

    const id = getValue('advisorAssignmentId');
    const employeeId = getValue('employeeId');
    const studentClasseId = getValue('advisorClassId');
    const startDate = getValue('advisorStartDate');
    const endDate = getValue('advisorEndDate');

    if (!isUuid(employeeId)) {
        alert('Vui lòng nhập ID nhân viên/giảng viên đúng định dạng UUID');
        return;
    }
    if (startDate && endDate && startDate > endDate) {
        alert('Ngày kết thúc phải bằng hoặc sau ngày bắt đầu');
        return;
    }

    const assignment = {
        employeeId,
        studentClasseId,
        startDate: startDate ? `${startDate}T00:00:00` : null,
        endDate: endDate ? `${endDate}T00:00:00` : null,
        isActive: document.getElementById('advisorIsActive').checked
    };

    try {
        const response = await fetch(
            id ? `${API_ENDPOINTS.ADVISOR_CLASSE_SECTIONS}/${encodeURIComponent(id)}` : API_ENDPOINTS.ADVISOR_CLASSE_SECTIONS,
            {
                method: id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(assignment)
            }
        );
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        hideAdvisorForm();
        await loadAdvisorAssignments();
        alert(id ? 'Cập nhật phân công thành công' : 'Thêm phân công thành công');
    } catch (error) {
        console.error('Lỗi khi lưu phân công giảng viên:', error);
        alert('Không thể lưu phân công. Vui lòng kiểm tra lại dữ liệu và API.');
    }
}

async function deleteAdvisorAssignment(id) {
    if (!window.confirm('Bạn có chắc muốn ngừng phân công giảng viên này?')) {
        return;
    }

    try {
        const response = await fetch(
            `${API_ENDPOINTS.ADVISOR_CLASSE_SECTIONS}/${encodeURIComponent(id)}`,
            { method: 'DELETE' }
        );
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        await loadAdvisorAssignments();
        alert('Đã ngừng phân công giảng viên');
    } catch (error) {
        console.error('Lỗi khi ngừng phân công giảng viên:', error);
        alert('Không thể ngừng phân công giảng viên');
    }
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
}