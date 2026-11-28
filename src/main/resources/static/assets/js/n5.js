import { API_ENDPOINTS } from './config.js';

const PAGE_SIZE = 8;
const state = {
    schoolYears: [], semesters: [], courseSections: [], registrations: [], instructors: [],
    sectionStudents: [],
    pages: { 'school-years': 1, semesters: 1, 'course-sections': 1, registrations: 1, instructors: 1 },
    filters: {}, modal: { type: null, id: null },
};

const configs = {
    'school-years': { key: 'schoolYears', endpoint: API_ENDPOINTS.SCHOOL_YEARS, body: 'schoolYearBody', pagination: 'schoolYearPagination', message: 'schoolYearMessage', title: 'năm học' },
    semesters: { key: 'semesters', endpoint: API_ENDPOINTS.SEMESTERS, body: 'semesterBody', pagination: 'semesterPagination', message: 'semesterMessage', title: 'học kỳ' },
    'course-sections': { key: 'courseSections', endpoint: API_ENDPOINTS.COURSE_SECTIONS, body: 'courseSectionBody', pagination: 'courseSectionPagination', message: 'courseSectionMessage', title: 'lớp học phần' },
    registrations: { key: 'registrations', endpoint: `${API_ENDPOINTS.STUDENT_COURSE_SECTIONS}`, body: 'registrationBody', pagination: 'registrationPagination', message: 'registrationMessage', title: 'đăng ký học phần' },
    instructors: { key: 'instructors', endpoint: API_ENDPOINTS.COURSE_CLASS_INSTRUCTORS, body: 'instructorBody', pagination: 'instructorPagination', message: 'instructorMessage', title: 'phân công giảng viên' },
};

const escapeHtml = (value) => String(value ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;');
const formatDate = (value) => value ? new Intl.DateTimeFormat('vi-VN').format(new Date(value)) : '—';
const dateInput = (value) => value ? String(value).slice(0, 10) : '';
const formatDateTime = (value) => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)) : '—';
const dateTimeInput = (value) => {
    if (!value) return '';
    const normalized = String(value).trim().replace(' ', 'T');
    if (/^\d{4}-\d{2}-\d{2}$/.test(normalized)) return `${normalized}T00:00`;
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(normalized)) return normalized;
    return normalized.slice(0, 16);
};
const courseSectionClassTypes = [
    { value: 'theory', label: 'Lý thuyết' },
    { value: 'practice', label: 'Thực hành' },
    { value: 'theory-practice', label: 'Lý thuyết + thực hành' },
];
const courseSectionStatuses = [
    { value: 'planned', label: 'Chưa mở' },
    { value: 'open', label: 'Đang mở' },
    { value: 'studying', label: 'Đang học' },
    { value: 'completed', label: 'Đã hoàn thành' },
    { value: 'closed', label: 'Đã đóng' },
    { value: 'cancelled', label: 'Đã hủy' },
];
const instructorRoles = [
    { value: 'Giảng viên chính', label: 'Giảng viên chính' },
    { value: 'Giảng viên phụ', label: 'Giảng viên phụ' },
    { value: 'Trợ giảng', label: 'Trợ giảng' },
];
const courseSectionOptionLabel = (options, value) =>
    options.find((option) => option.value === value)?.label || value || '—';
const activeOptions = (item) => `<label>Hoạt động<select name="isActive"><option value="true" ${item.isActive !== false ? 'selected' : ''}>Có</option><option value="false" ${item.isActive === false ? 'selected' : ''}>Không</option></select></label>`;
const schoolYearStart = (schoolYear) => {
    for (const value of [schoolYear.code, schoolYear.name, schoolYear.schoolYearName]) {
        const match = String(value || '').match(/(?:19|20)\d{2}/);
        if (match) return Number(match[0]);
    }
    const dateYear = String(schoolYear.startDate || '').match(/^\d{4}/);
    return dateYear ? Number(dateYear[0]) : Number.MAX_SAFE_INTEGER;
};
const sortSchoolYears = (schoolYears) => [...schoolYears].sort((a, b) =>
    schoolYearStart(a) - schoolYearStart(b)
    || String(a.code || '').localeCompare(String(b.code || ''), 'vi', { numeric: true, sensitivity: 'base' })
    || String(a.name || '').localeCompare(String(b.name || ''), 'vi', { numeric: true, sensitivity: 'base' }));
const statusBadge = (active) => active ? '<span class="badge success">Đang hoạt động</span>' : '<span class="badge muted">Ngừng hoạt động</span>';
const semesterLabel = (id) => {
    const semester = state.semesters.find((item) => String(item.id) === String(id));
    return semester ? `${semester.code} · ${semester.name}` : 'Chưa gán học kỳ';
};
const schoolYearLabel = (id, fallback) => {
    const schoolYear = state.schoolYears.find((item) => String(item.id) === String(id));
    return schoolYear ? `${schoolYear.code} · ${schoolYear.name}` : (fallback || 'Chưa gán năm học');
};
const showMessage = (id, message) => { const el = document.getElementById(id); el.textContent = message; el.hidden = false; };
const clearMessage = (id) => { document.getElementById(id).hidden = true; };

async function request(url, options = {}) {
    const response = await fetch(url, { ...options, headers: { Accept: 'application/json', 'Content-Type': 'application/json', ...(options.headers || {}) } });
    if (!response.ok) {
        let detail = `HTTP ${response.status}`;
        try { const body = await response.json(); detail = body.message || body.error || detail; } catch { /* non-json response */ }
        throw new Error(detail);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text.trim() ? JSON.parse(text) : null;
}

function updateSummary() {
    for (const [id, items] of [['schoolYear', state.schoolYears], ['semester', state.semesters], ['courseSection', state.courseSections], ['registration', state.registrations], ['instructor', state.instructors]]) {
        document.getElementById(`${id}Count`).textContent = items.length;
        const total = items.filter((item) => item.isActive === true).length;
        document.getElementById(`active${id[0].toUpperCase()}${id.slice(1)}Count`).textContent = total;
    }
}

function actionButtons(type, item) {
    const statusButton = type === 'school-years'
        ? `<button class="icon-button status-toggle ${item.isActive ? 'active' : 'inactive'}" data-type="${type}" data-id="${item.id}" data-next-active="${!item.isActive}" title="${item.isActive ? 'Ngừng hoạt động' : 'Kích hoạt'}" aria-label="${item.isActive ? 'Ngừng hoạt động' : 'Kích hoạt'}">${item.isActive ? '●' : '○'}</button>`
        : '';
    const deleteTitle = 'Xóa vĩnh viễn';
    return `<div class="row-actions">
        <button class="icon-button edit-action" data-type="${type}" data-id="${item.id}" title="Chỉnh sửa" aria-label="Chỉnh sửa">✎</button>
        ${statusButton}
        <button class="icon-button danger delete-action" data-type="${type}" data-id="${item.id}" title="${deleteTitle}" aria-label="${deleteTitle}">⌫</button>
    </div>`;
}

function pageItems(type) {
    const config = configs[type];
    const items = state[config.key];
    const start = (state.pages[type] - 1) * PAGE_SIZE;
    return items.slice(start, start + PAGE_SIZE);
}

function renderPagination(type) {
    const config = configs[type];
    const total = Math.ceil(state[config.key].length / PAGE_SIZE);
    const container = document.getElementById(config.pagination);
    if (total <= 1) { container.innerHTML = ''; return; }
    const current = state.pages[type];
    let html = `<button class="page-button" data-page-type="${type}" data-page="${current - 1}" ${current === 1 ? 'disabled' : ''}>‹</button>`;
    for (let page = 1; page <= total; page += 1) html += `<button class="page-button ${page === current ? 'active' : ''}" data-page-type="${type}" data-page="${page}">${page}</button>`;
    html += `<button class="page-button" data-page-type="${type}" data-page="${current + 1}" ${current === total ? 'disabled' : ''}>›</button>`;
    container.innerHTML = html;
}

function renderSchoolYears() {
    const items = pageItems('school-years');
    document.getElementById('schoolYearBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.code)}</strong></td><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.description || '—')}</td><td>${escapeHtml(item.note || '—')}</td><td>${formatDate(item.startDate)}</td><td>${formatDate(item.endDate)}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('school-years', item)}</td></tr>`).join('') : '<tr><td colspan="8" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('school-years');
}

function renderSemesters() {
    const items = pageItems('semesters');
    document.getElementById('semesterBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.code)}</strong></td><td>${escapeHtml(item.name)}</td><td><span class="sub-cell">${escapeHtml(schoolYearLabel(item.schoolYearId, item.schoolYearName))}</span></td><td>${formatDate(item.startDate)}</td><td>${formatDate(item.endDate)}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('semesters', item)}</td></tr>`).join('') : '<tr><td colspan="7" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('semesters');
}

function renderCourseSections() {
    const items = pageItems('course-sections');
    document.getElementById('courseSectionBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.code)}</strong></td><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.courseId || '—')}</td><td>${escapeHtml(semesterLabel(item.semesterId))}</td><td>${escapeHtml(item.roomId || '—')}</td><td>${escapeHtml(item.buildingId || '—')}</td><td>${escapeHtml(item.minStudents ?? '—')} / ${escapeHtml(item.maxStudents ?? '—')}</td><td>${escapeHtml(courseSectionOptionLabel(courseSectionClassTypes, item.classType))}</td><td>${escapeHtml(courseSectionOptionLabel(courseSectionStatuses, item.status))}</td><td>${formatDateTime(item.registrationStart)}</td><td>${formatDateTime(item.registrationEnd)}</td><td>${escapeHtml(item.note || '—')}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('course-sections', item)}</td></tr>`).join('') : '<tr><td colspan="14" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('course-sections');
}

function renderRegistrations() {
    const items = pageItems('registrations');
    document.getElementById('registrationBody').innerHTML = items.length ? items.map((item) => {
        const sectionLabel = item.courseSectionCode && item.courseSectionName
            ? `[${item.courseSectionCode}] - ${item.courseSectionName}`
            : item.courseSectionName || 'Không tìm thấy lớp học phần';
        return `<tr><td><strong>${escapeHtml(item.studentId)}</strong></td><td><strong>${escapeHtml(sectionLabel)}</strong></td><td>${formatDateTime(item.registrationDate)}</td><td>${escapeHtml(item.registrationType || '—')}</td><td>${escapeHtml(item.status || '—')}</td><td>${escapeHtml(item.attemptNumber || '—')}</td><td>${escapeHtml(item.note || '—')}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('registrations', item)}</td></tr>`;
    }).join('') : '<tr><td colspan="9" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('registrations');
}

function renderInstructors() {
    const items = pageItems('instructors');
    document.getElementById('instructorBody').innerHTML = items.length ? items.map((item) => {
        return `<tr><td><strong>${escapeHtml(item.instructorCode || item.employeeId)}</strong></td><td>${escapeHtml(item.instructorName || 'Chưa cập nhật')}</td><td>${escapeHtml(item.courseSectionCode || '—')}</td><td>${escapeHtml(item.courseSectionName || item.courseClassId)}</td><td><span class="badge role">${escapeHtml(item.role || '—')}</span></td><td>${escapeHtml(item.teachingHours ?? '—')}</td><td>${formatDateTime(item.createdAt)}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('instructors', item)}</td></tr>`;
    }).join('') : '<tr><td colspan="9" class="empty">Không có phân công phù hợp.</td></tr>';
    renderPagination('instructors');
}

const renderers = { 'school-years': renderSchoolYears, semesters: renderSemesters, 'course-sections': renderCourseSections, registrations: renderRegistrations, instructors: renderInstructors };

async function load(type) {
    const config = configs[type];
    clearMessage(config.message);
    const params = new URLSearchParams(state.filters[type] || {});
    try {
        const url = type === 'registrations' ? `${config.endpoint}/details${params.toString() ? `?${params}` : ''}` : `${config.endpoint}${params.toString() ? `?${params}` : ''}`;
        const result = await request(url);
        state[config.key] = type === 'school-years' ? sortSchoolYears(result) : result;
        state.pages[type] = 1;
        renderers[type]();
        updateSummary();
    } catch (error) {
        showMessage(config.message, `Không thể tải ${config.title}: ${error.message}.`);
    }
}

async function loadStudentsByCourseSection(courseSectionId) {
    const body = document.getElementById('sectionStudentBody');
    const count = document.getElementById('sectionStudentCount');
    clearMessage('studentLookupMessage');
    state.sectionStudents = [];
    if (!courseSectionId) {
        count.textContent = '—';
        body.innerHTML = '<tr><td colspan="5" class="empty">Vui lòng chọn lớp học phần để xem danh sách sinh viên.</td></tr>';
        return;
    }

    body.innerHTML = '<tr><td colspan="5" class="empty">Đang tải danh sách sinh viên...</td></tr>';
    count.textContent = '…';
    try {
        const result = await request(`${API_ENDPOINTS.STUDENT_COURSE_SECTIONS}/by-course-section/${encodeURIComponent(courseSectionId)}`);
        state.sectionStudents = result.students || [];
        count.textContent = result.totalStudents ?? state.sectionStudents.length;
        body.innerHTML = state.sectionStudents.length
            ? state.sectionStudents.map((student, index) => `<tr><td>${index + 1}</td><td><strong>${escapeHtml(student.studentId)}</strong></td><td>${formatDateTime(student.registrationDate)}</td><td>${escapeHtml(student.status || '—')}</td><td>${escapeHtml(student.attemptNumber || '—')}</td></tr>`).join('')
            : '<tr><td colspan="5" class="empty">Lớp học phần này chưa có sinh viên đăng ký.</td></tr>';
    } catch (error) {
        count.textContent = '—';
        body.innerHTML = '<tr><td colspan="5" class="empty">Không thể tải danh sách sinh viên.</td></tr>';
        showMessage('studentLookupMessage', `Không thể tải danh sách sinh viên: ${error.message}.`);
    }
}

function collectFilters(type) {
    if (type === 'school-years') state.filters[type] = {
        keyword: document.getElementById('schoolYearKeyword').value.trim(),
        isActive: document.getElementById('schoolYearStatus').value,
    };
    if (type === 'semesters') state.filters[type] = { keyword: document.getElementById('semesterKeyword').value.trim() };
    if (type === 'course-sections') state.filters[type] = { keyword: document.getElementById('courseSectionKeyword').value.trim(), semesterId: document.getElementById('courseSectionSemester').value };
    if (type === 'instructors') state.filters[type] = {
        instructorCode: document.getElementById('instructorCode').value.trim(),
        instructorName: document.getElementById('instructorName').value.trim(),
        courseClassName: document.getElementById('instructorCourseClassName').value.trim(),
        role: document.getElementById('instructorRole').value.trim(),
    };
    Object.keys(state.filters[type]).forEach((key) => { if (!state.filters[type][key]) delete state.filters[type][key]; });
}

function fieldsFor(type, item = {}) {
    if (type === 'school-years') return `<label>Mã năm học<input name="code" required maxlength="100" value="${escapeHtml(item.code)}"></label><label>Tên năm học<input name="name" required maxlength="255" value="${escapeHtml(item.name)}"></label><label>Mô tả<input name="description" maxlength="255" value="${escapeHtml(item.description)}"></label><label>Ghi chú<input name="note" maxlength="255" value="${escapeHtml(item.note)}"></label><label>Ngày bắt đầu<input name="startDate" type="date" value="${dateInput(item.startDate)}"></label><label>Ngày kết thúc<input name="endDate" type="date" value="${dateInput(item.endDate)}"></label>${activeOptions(item)}`;
    if (type === 'semesters') return `<label>Mã học kỳ<input name="code" required maxlength="100" value="${escapeHtml(item.code)}"></label><label>Tên học kỳ<input name="name" required maxlength="255" value="${escapeHtml(item.name)}"></label><label>Năm học<select name="schoolYearId" required><option value="">Chọn năm học</option>${state.schoolYears.map((year) => `<option value="${year.id}" ${String(year.id) === String(item.schoolYearId) ? 'selected' : ''}>${escapeHtml(year.code)} - ${escapeHtml(year.name)}</option>`).join('')}</select></label><label>Ngày bắt đầu<input name="startDate" type="date" value="${dateInput(item.startDate)}"></label><label>Ngày kết thúc<input name="endDate" type="date" value="${dateInput(item.endDate)}"></label>${activeOptions(item)}`;
    if (type === 'course-sections') {
        const selectedClassType = item.classType || '';
        const classTypeOptions = [...courseSectionClassTypes];
        if (selectedClassType && !classTypeOptions.some((option) => option.value === selectedClassType)) {
            classTypeOptions.push({ value: selectedClassType, label: selectedClassType });
        }
        const selectedStatus = item.status || 'open';
        const statusOptions = [...courseSectionStatuses];
        if (!statusOptions.some((option) => option.value === selectedStatus)) {
            statusOptions.push({ value: selectedStatus, label: selectedStatus });
        }
        return `<label>Mã lớp học phần<input name="code" required maxlength="100" value="${escapeHtml(item.code)}" placeholder="Ví dụ: IT001"></label>
            <label>Tên lớp học phần<input name="name" required maxlength="255" value="${escapeHtml(item.name)}" placeholder="Ví dụ: Lập trình Java căn bản"></label>
            <label>Học kỳ<select name="semesterId"><option value="">Chọn học kỳ</option>${state.semesters.map((semester) => `<option value="${semester.id}" ${String(semester.id) === String(item.semesterId) ? 'selected' : ''}>${escapeHtml(semester.code)} - ${escapeHtml(semester.name)}</option>`).join('')}</select></label>
            <label>Sĩ số tối thiểu<input name="minStudents" type="number" min="0" value="${escapeHtml(item.minStudents)}"></label>
            <label>Sĩ số tối đa<input name="maxStudents" type="number" min="0" value="${escapeHtml(item.maxStudents)}"></label>
            <label>Loại lớp<select name="classType"><option value="">Chọn loại lớp</option>${classTypeOptions.map((option) => `<option value="${escapeHtml(option.value)}" ${option.value === selectedClassType ? 'selected' : ''}>${escapeHtml(option.label)}</option>`).join('')}</select></label>
            <label>Trạng thái<select name="status">${statusOptions.map((option) => `<option value="${escapeHtml(option.value)}" ${option.value === selectedStatus ? 'selected' : ''}>${escapeHtml(option.label)}</option>`).join('')}</select></label>
            <label>Bắt đầu đăng ký<input name="registrationStart" type="datetime-local" value="${dateTimeInput(item.registrationStart)}"></label>
            <label>Kết thúc đăng ký<input name="registrationEnd" type="datetime-local" value="${dateTimeInput(item.registrationEnd)}"></label>
            <label>Ghi chú<input name="note" maxlength="255" value="${escapeHtml(item.note)}"></label>
            <input type="hidden" name="courseId" value="${escapeHtml(item.courseId)}">
            <input type="hidden" name="roomId" value="${escapeHtml(item.roomId)}">
            <input type="hidden" name="buildingId" value="${escapeHtml(item.buildingId)}">
            <input type="hidden" name="isActive" value="${item.isActive === false ? 'false' : 'true'}">`;
    }
    if (type === 'registrations') {
        const sectionOptions = state.courseSections.map((section) => `<option value="${section.id}" ${String(section.id) === String(item.courseSectionId) ? 'selected' : ''}>${escapeHtml(section.code)} - ${escapeHtml(section.name)}</option>`).join('');
        return `<label>UUID sinh viên<input name="studentId" type="text" required value="${escapeHtml(item.studentId)}" placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"></label><label>Lớp học phần<select name="courseSectionId" required><option value="">Chọn lớp học phần</option>${sectionOptions}</select></label><label>Ngày đăng ký<input name="registrationDate" type="datetime-local" value="${dateTimeInput(item.registrationDate)}"></label><label>Hình thức đăng ký<input name="registrationType" maxlength="100" value="${escapeHtml(item.registrationType)}"></label><label>Trạng thái<input name="status" maxlength="100" value="${escapeHtml(item.status)}"></label><label>Lần học<input name="attemptNumber" maxlength="100" value="${escapeHtml(item.attemptNumber)}"></label><label>Ghi chú<input name="note" maxlength="255" value="${escapeHtml(item.note)}"></label>${activeOptions(item)}`;
    }
    const sectionOptions = state.courseSections.map((section) => `<option value="${section.id}" ${String(section.id) === String(item.courseClassId) ? 'selected' : ''}>[${escapeHtml(section.code)}] - ${escapeHtml(section.name)}</option>`).join('');
    const selectedRole = item.role || instructorRoles[0].value;
    const roles = [...instructorRoles];
    if (!roles.some((role) => role.value === selectedRole)) {
        roles.push({ value: selectedRole, label: selectedRole });
    }
    return `<label>Tên giảng viên<input name="instructorName" required maxlength="255" value="${escapeHtml(item.instructorName)}" placeholder="Họ và tên giảng viên"></label>
        <label>Lớp học phần<select name="courseClassId" required><option value="">Chọn lớp học phần</option>${sectionOptions}</select></label>
        <label>Mã giảng viên<input name="instructorCode" required maxlength="50" value="${escapeHtml(item.instructorCode || item.employeeId)}" placeholder="Ví dụ: ECO-005"></label>
        <label>Vai trò<select name="role" required>${roles.map((role) => `<option value="${escapeHtml(role.value)}" ${role.value === selectedRole ? 'selected' : ''}>${escapeHtml(role.label)}</option>`).join('')}</select></label>
        <label>Số tiết<input name="teachingHours" type="number" min="0" value="${escapeHtml(item.teachingHours)}"></label>
        <input type="hidden" name="isActive" value="${item.isActive === false ? 'false' : 'true'}">`;
}

async function openModal(type, item = null) {
    if (type === 'semesters' || type === 'registrations' || type === 'course-sections' || type === 'instructors') {
        try {
            const [schoolYears, semesters, courseSections] = await Promise.all([
                request(API_ENDPOINTS.SCHOOL_YEARS),
                request(API_ENDPOINTS.SEMESTERS),
                request(API_ENDPOINTS.COURSE_SECTIONS),
            ]);
            state.schoolYears = sortSchoolYears(schoolYears);
            state.semesters = semesters;
            state.courseSections = courseSections;
        } catch (error) {
            showMessage(configs[type].message, `Không thể làm mới danh sách năm học, học kỳ và lớp học phần: ${error.message}.`);
            return;
        }
    }
    state.modal = { type, id: item?.id || null };
    document.getElementById('modalTitle').textContent = `${item ? 'Sửa' : 'Thêm'} ${configs[type].title}`;
    document.getElementById('modalFields').innerHTML = fieldsFor(type, item || {});
    document.getElementById('modalMessage').hidden = true;
    document.getElementById('recordModal').hidden = false;
}

function closeModal() { document.getElementById('recordModal').hidden = true; }

async function saveModal(event) {
    event.preventDefault();
    const { type, id } = state.modal;
    const formData = new FormData(event.currentTarget);
    const payload = Object.fromEntries(formData.entries());
    ['schoolYearId', 'studentId', 'courseSectionId', 'courseClassId', 'courseId', 'roomId', 'buildingId',
        'maxStudents', 'minStudents']
        .forEach((key) => { if (payload[key] === '') delete payload[key]; });
    if (payload.teachingHours === '') {
        payload.teachingHours = null;
    } else if (payload.teachingHours !== undefined) {
        payload.teachingHours = Number(payload.teachingHours);
    }
    ['startDate', 'endDate', 'registrationDate', 'registrationStart', 'registrationEnd']
        .forEach((key) => { if (payload[key] === '') delete payload[key]; });
    if (payload.isActive !== undefined) payload.isActive = payload.isActive === 'true';
    if (payload.startDate && payload.endDate && payload.startDate > payload.endDate) {
        document.getElementById('modalMessage').textContent = 'Ngày bắt đầu không được sau ngày kết thúc.';
        document.getElementById('modalMessage').hidden = false;
        return;
    }
    if (payload.registrationStart && payload.registrationEnd
            && payload.registrationStart > payload.registrationEnd) {
        document.getElementById('modalMessage').textContent = 'Thời gian mở đăng ký không được sau thời gian đóng đăng ký.';
        document.getElementById('modalMessage').hidden = false;
        return;
    }
    try {
        await request(id ? `${configs[type].endpoint}/${id}` : configs[type].endpoint, { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
        closeModal();
        await load(type);
    } catch (error) {
        const message = document.getElementById('modalMessage'); message.textContent = `Không thể lưu dữ liệu: ${error.message}`; message.hidden = false;
    }
}

async function deleteItem(type, id) {
    const confirmation = `Bạn có chắc muốn xóa vĩnh viễn ${configs[type].title} này không?`;
    if (!window.confirm(confirmation)) return;
    try { await request(`${configs[type].endpoint}/${id}`, { method: 'DELETE' }); await load(type); } catch (error) { showMessage(configs[type].message, `Không thể xóa: ${error.message}.`); }
}

async function updateSchoolYearStatus(id, nextActive) {
    const action = nextActive ? 'kích hoạt' : 'ngừng hoạt động';
    if (!window.confirm(`Bạn có chắc muốn ${action} năm học này không?`)) return;
    try {
        await request(`${configs['school-years'].endpoint}/${id}/status?isActive=${nextActive}`, { method: 'PATCH' });
        await load('school-years');
    } catch (error) {
        showMessage(configs['school-years'].message, `Không thể cập nhật trạng thái: ${error.message}.`);
    }
}

function setupTabs() {
    document.querySelectorAll('.tab-button').forEach((button) => button.addEventListener('click', () => {
        document.querySelectorAll('.tab-button').forEach((item) => item.classList.remove('active'));
        document.querySelectorAll('.data-panel').forEach((panel) => { panel.hidden = true; });
        button.classList.add('active'); document.getElementById(`tab-${button.dataset.tab}`).hidden = false;
    }));
}

document.addEventListener('DOMContentLoaded', () => {
    setupTabs();
    document.querySelectorAll('.add-button').forEach((button) => button.addEventListener('click', () => openModal(button.dataset.create)));
    document.querySelectorAll('[data-close-modal]').forEach((button) => button.addEventListener('click', closeModal));
    document.getElementById('recordModal').addEventListener('click', (event) => { if (event.target.id === 'recordModal') closeModal(); });
    document.getElementById('recordForm').addEventListener('submit', saveModal);
    document.getElementById('schoolYearSearch').addEventListener('click', () => { collectFilters('school-years'); load('school-years'); });
    document.getElementById('schoolYearKeyword').addEventListener('keydown', (event) => {
        if (event.key === 'Enter') { collectFilters('school-years'); load('school-years'); }
    });
    document.getElementById('semesterSearch').addEventListener('click', () => { collectFilters('semesters'); load('semesters'); });
    document.getElementById('courseSectionSearch').addEventListener('click', () => { collectFilters('course-sections'); load('course-sections'); });
    document.getElementById('studentLookupCourseSection').addEventListener('change', (event) => loadStudentsByCourseSection(event.target.value));
    document.getElementById('instructorSearch').addEventListener('click', () => { collectFilters('instructors'); load('instructors'); });
    document.addEventListener('click', (event) => {
        const button = event.target.closest('button');
        if (!button) return;
        if (button.classList.contains('edit-action')) openModal(button.dataset.type, state[configs[button.dataset.type].key].find((item) => String(item.id) === button.dataset.id));
        if (button.classList.contains('delete-action')) deleteItem(button.dataset.type, button.dataset.id);
        if (button.classList.contains('status-toggle')) updateSchoolYearStatus(button.dataset.id, button.dataset.nextActive === 'true');
        if (button.classList.contains('page-button') && !button.disabled) { state.pages[button.dataset.pageType] = Number(button.dataset.page); renderers[button.dataset.pageType](); }
    });
    Promise.all([
        request(API_ENDPOINTS.SCHOOL_YEARS),
        request(API_ENDPOINTS.SEMESTERS),
        request(API_ENDPOINTS.COURSE_SECTIONS),
    ]).then(([schoolYears, semesters, courseSections]) => {
        state.schoolYears = sortSchoolYears(schoolYears);
        state.semesters = semesters;
        state.courseSections = courseSections;
        document.getElementById('courseSectionSemester').innerHTML = `<option value="">Tất cả học kỳ</option>${semesters.map((semester) => `<option value="${semester.id}">${escapeHtml(semester.code)} - ${escapeHtml(semester.name)}</option>`).join('')}`;
        document.getElementById('studentLookupCourseSection').innerHTML = `<option value="">Chọn lớp học phần</option>${courseSections.map((section) => `<option value="${section.id}">[${escapeHtml(section.code)}] - ${escapeHtml(section.name)}</option>`).join('')}`;
        Object.keys(configs).forEach((type) => load(type));
    }).catch((error) => {
        showMessage('courseSectionMessage', `Không thể tải dữ liệu lựa chọn: ${error.message}.`);
        Object.keys(configs).forEach((type) => load(type));
    });
});
