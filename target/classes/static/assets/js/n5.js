import { API_ENDPOINTS } from './config.js';

const PAGE_SIZE = 8;
const state = {
    schoolYears: [], semesters: [], registrations: [], instructors: [],
    pages: { 'school-years': 1, semesters: 1, registrations: 1, instructors: 1 },
    filters: {}, modal: { type: null, id: null },
};

const configs = {
    'school-years': { key: 'schoolYears', endpoint: API_ENDPOINTS.SCHOOL_YEARS, body: 'schoolYearBody', pagination: 'schoolYearPagination', message: 'schoolYearMessage', title: 'năm học' },
    semesters: { key: 'semesters', endpoint: API_ENDPOINTS.SEMESTERS, body: 'semesterBody', pagination: 'semesterPagination', message: 'semesterMessage', title: 'học kỳ' },
    registrations: { key: 'registrations', endpoint: `${API_ENDPOINTS.STUDENT_COURSE_SECTIONS}`, body: 'registrationBody', pagination: 'registrationPagination', message: 'registrationMessage', title: 'đăng ký học phần' },
    instructors: { key: 'instructors', endpoint: API_ENDPOINTS.COURSE_CLASS_INSTRUCTORS, body: 'instructorBody', pagination: 'instructorPagination', message: 'instructorMessage', title: 'phân công giảng viên' },
};

const escapeHtml = (value) => String(value ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;');
const formatDate = (value) => value ? new Intl.DateTimeFormat('vi-VN').format(new Date(value)) : '—';
const dateInput = (value) => value ? String(value).slice(0, 10) : '';
const statusBadge = (active) => active ? '<span class="badge success">Đang hoạt động</span>' : '<span class="badge muted">Ngừng hoạt động</span>';
const statusText = (value) => String(value || '').toLowerCase();
const showMessage = (id, message) => { const el = document.getElementById(id); el.textContent = message; el.hidden = false; };
const clearMessage = (id) => { document.getElementById(id).hidden = true; };

async function request(url, options = {}) {
    const response = await fetch(url, { ...options, headers: { Accept: 'application/json', 'Content-Type': 'application/json', ...(options.headers || {}) } });
    if (!response.ok) {
        let detail = `HTTP ${response.status}`;
        try { const body = await response.json(); detail = body.message || body.error || detail; } catch { /* non-json response */ }
        throw new Error(detail);
    }
    return response.status === 204 ? null : response.json();
}

function updateSummary() {
    for (const [id, items] of [['schoolYear', state.schoolYears], ['semester', state.semesters], ['registration', state.registrations], ['instructor', state.instructors]]) {
        document.getElementById(`${id}Count`).textContent = items.length;
        document.getElementById(`active${id[0].toUpperCase()}${id.slice(1)}Count`).textContent = items.filter((item) => item.isActive === true).length;
    }
}

function actionButtons(type, item) {
    return `<div class="row-actions">
        <button class="icon-button edit-action" data-type="${type}" data-id="${item.id}" title="Sửa" aria-label="Sửa">✎</button>
        <button class="icon-button toggle-action" data-type="${type}" data-id="${item.id}" title="${item.isActive ? 'Tắt' : 'Bật'} trạng thái" aria-label="Đổi trạng thái">${item.isActive ? '◉' : '○'}</button>
        <button class="icon-button danger delete-action" data-type="${type}" data-id="${item.id}" title="Xóa" aria-label="Xóa">⌫</button>
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
    document.getElementById('schoolYearBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.code || '—')}</strong></td><td>${escapeHtml(item.name || item.schoolYearName || '—')}</td><td>${formatDate(item.startDate)}</td><td>${formatDate(item.endDate)}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('school-years', item)}</td></tr>`).join('') : '<tr><td colspan="6" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('school-years');
}

function renderSemesters() {
    const items = pageItems('semesters');
    document.getElementById('semesterBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.code || '—')}</strong></td><td>${escapeHtml(item.name || '—')}</td><td>${escapeHtml(item.schoolYearName || item.schoolYearId || '—')}</td><td>${formatDate(item.startDate)}</td><td>${formatDate(item.endDate)}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('semesters', item)}</td></tr>`).join('') : '<tr><td colspan="7" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('semesters');
}

function renderRegistrations() {
    const items = pageItems('registrations');
    document.getElementById('registrationBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.studentName || 'Chưa có tên')}</strong><small class="sub-cell">${escapeHtml(item.studentCode || item.studentId || '—')}</small></td><td><strong>${escapeHtml(item.courseSectionName || 'Chưa có tên lớp')}</strong><small class="sub-cell">${escapeHtml(item.courseSectionCode || item.courseSectionId || '—')}</small></td><td>${formatDate(item.registrationDate)}</td><td>${escapeHtml(item.registrationType || '—')}</td><td><span class="badge status status-${escapeHtml(statusText(item.status))}">${escapeHtml(item.status || '—')}</span></td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('registrations', item)}</td></tr>`).join('') : '<tr><td colspan="7" class="empty">Không có dữ liệu phù hợp.</td></tr>';
    renderPagination('registrations');
}

function renderInstructors() {
    const items = pageItems('instructors');
    document.getElementById('instructorBody').innerHTML = items.length ? items.map((item) => `<tr><td><strong>${escapeHtml(item.employeeName || 'Chưa có tên')}</strong><small class="sub-cell">${escapeHtml(item.employeeId || '—')}</small></td><td><strong>${escapeHtml(item.courseClassName || 'Chưa có tên lớp')}</strong><small class="sub-cell">${escapeHtml(item.courseClassId || '—')}</small></td><td><span class="badge role">${escapeHtml(item.role || 'Giảng viên chính')}</span></td><td>${formatDate(item.createdAt)}</td><td>${statusBadge(item.isActive)}</td><td>${actionButtons('instructors', item)}</td></tr>`).join('') : '<tr><td colspan="6" class="empty">Không có phân công phù hợp.</td></tr>';
    renderPagination('instructors');
}

const renderers = { 'school-years': renderSchoolYears, semesters: renderSemesters, registrations: renderRegistrations, instructors: renderInstructors };

async function load(type) {
    const config = configs[type];
    clearMessage(config.message);
    const params = new URLSearchParams(state.filters[type] || {});
    try {
        const url = type === 'registrations' ? `${config.endpoint}/details${params.toString() ? `?${params}` : ''}` : `${config.endpoint}${params.toString() ? `?${params}` : ''}`;
        state[config.key] = await request(url);
        state.pages[type] = 1;
        renderers[type]();
        updateSummary();
    } catch (error) {
        showMessage(config.message, `Không thể tải ${config.title}: ${error.message}.`);
    }
}

function collectFilters(type) {
    if (type === 'school-years') state.filters[type] = { keyword: document.getElementById('schoolYearKeyword').value.trim(), isActive: document.getElementById('schoolYearStatus').value };
    if (type === 'semesters') state.filters[type] = { keyword: document.getElementById('semesterKeyword').value.trim(), isActive: document.getElementById('semesterStatus').value };
    if (type === 'registrations') state.filters[type] = { status: document.getElementById('registrationStatus').value };
    if (type === 'instructors') state.filters[type] = { employeeName: document.getElementById('instructorEmployeeName').value.trim(), courseClassName: document.getElementById('instructorCourseClassName').value.trim(), role: document.getElementById('instructorRole').value.trim(), isActive: document.getElementById('instructorStatus').value };
    Object.keys(state.filters[type]).forEach((key) => { if (!state.filters[type][key]) delete state.filters[type][key]; });
}

function fieldsFor(type, item = {}) {
    if (type === 'school-years') return `<label>Mã năm học<input name="code" required maxlength="100" value="${escapeHtml(item.code)}"></label><label>Tên năm học<input name="name" required maxlength="255" value="${escapeHtml(item.name)}"></label><label>Ngày bắt đầu<input name="startDate" type="date" value="${dateInput(item.startDate)}"></label><label>Ngày kết thúc<input name="endDate" type="date" value="${dateInput(item.endDate)}"></label>`;
    if (type === 'semesters') return `<label>Mã học kỳ<input name="code" required maxlength="100" value="${escapeHtml(item.code)}"></label><label>Tên học kỳ<input name="name" required maxlength="255" value="${escapeHtml(item.name)}"></label><label>Mã năm học<input name="schoolYearId" value="${escapeHtml(item.schoolYearId)}" placeholder="UUID"></label><label>Tên năm học<input name="schoolYearName" maxlength="20" value="${escapeHtml(item.schoolYearName)}"></label><label>Ngày bắt đầu<input name="startDate" type="date" value="${dateInput(item.startDate)}"></label><label>Ngày kết thúc<input name="endDate" type="date" value="${dateInput(item.endDate)}"></label>`;
    if (type === 'registrations') return `<label>Mã sinh viên<input name="studentId" required value="${escapeHtml(item.studentId)}" placeholder="UUID"></label><label>Mã lớp học phần<input name="courseSectionId" required value="${escapeHtml(item.courseSectionId)}" placeholder="UUID"></label><label>Ngày đăng ký<input name="registrationDate" type="datetime-local" value="${item.registrationDate ? String(item.registrationDate).slice(0, 16) : ''}"></label><label>Loại đăng ký<input name="registrationType" value="${escapeHtml(item.registrationType)}"></label><label>Trạng thái<select name="status"><option value="REGISTERED" ${item.status === 'REGISTERED' ? 'selected' : ''}>REGISTERED</option><option value="STUDYING" ${item.status === 'STUDYING' ? 'selected' : ''}>STUDYING</option><option value="COMPLETED" ${item.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option><option value="CANCELLED" ${item.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option></select></label>`;
    return `<label>Tên giảng viên<input name="employeeName" required maxlength="255" value="${escapeHtml(item.employeeName)}" placeholder="Họ và tên giảng viên"></label><label>Tên lớp học phần<input name="courseClassName" required maxlength="255" value="${escapeHtml(item.courseClassName)}" placeholder="Tên lớp học phần"></label><label>Mã giảng viên<input name="employeeId" value="${escapeHtml(item.employeeId)}" placeholder="UUID (không bắt buộc)"></label><label>Mã lớp học phần<input name="courseClassId" value="${escapeHtml(item.courseClassId)}" placeholder="UUID (không bắt buộc)"></label><label>Vai trò<input name="role" maxlength="50" value="${escapeHtml(item.role || 'Giảng viên chính')}"></label>`;
}

function openModal(type, item = null) {
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
    ['schoolYearId', 'studentId', 'courseSectionId', 'employeeId'].forEach((key) => { if (payload[key] === '') delete payload[key]; });
    ['startDate', 'endDate', 'registrationDate'].forEach((key) => { if (payload[key] === '') delete payload[key]; });
    try {
        await request(id ? `${configs[type].endpoint}/${id}` : configs[type].endpoint, { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
        closeModal();
        await load(type);
    } catch (error) {
        const message = document.getElementById('modalMessage'); message.textContent = `Không thể lưu dữ liệu: ${error.message}`; message.hidden = false;
    }
}

async function deleteItem(type, id) {
    if (!window.confirm(`Bạn có chắc muốn xóa ${configs[type].title} này không?`)) return;
    try { await request(`${configs[type].endpoint}/${id}`, { method: 'DELETE' }); await load(type); } catch (error) { showMessage(configs[type].message, `Không thể xóa: ${error.message}.`); }
}

async function toggleItem(type, id) {
    const item = state[configs[type].key].find((record) => record.id === id);
    if (!item) return;
    try { await request(`${configs[type].endpoint}/${id}/status?isActive=${!item.isActive}`, { method: 'PATCH' }); await load(type); } catch (error) { showMessage(configs[type].message, `Không thể đổi trạng thái: ${error.message}.`); }
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
    document.getElementById('semesterSearch').addEventListener('click', () => { collectFilters('semesters'); load('semesters'); });
    document.getElementById('registrationSearch').addEventListener('click', () => { collectFilters('registrations'); load('registrations'); });
    document.getElementById('instructorSearch').addEventListener('click', () => { collectFilters('instructors'); load('instructors'); });
    document.addEventListener('click', (event) => {
        const button = event.target.closest('button');
        if (!button) return;
        if (button.classList.contains('edit-action')) openModal(button.dataset.type, state[configs[button.dataset.type].key].find((item) => item.id === button.dataset.id));
        if (button.classList.contains('delete-action')) deleteItem(button.dataset.type, button.dataset.id);
        if (button.classList.contains('toggle-action')) toggleItem(button.dataset.type, button.dataset.id);
        if (button.classList.contains('page-button') && !button.disabled) { state.pages[button.dataset.pageType] = Number(button.dataset.page); renderers[button.dataset.pageType](); }
    });
    Object.keys(configs).forEach((type) => load(type));
});
