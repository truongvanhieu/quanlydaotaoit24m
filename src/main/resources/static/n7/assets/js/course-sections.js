const api = '/api';
const statusBox = document.getElementById('status');
const form = document.getElementById('sectionForm');
const rows = document.getElementById('sectionRows');
const rosterPanel = document.getElementById('rosterPanel');
const assignmentRows = document.getElementById('assignmentRows');
const classSearchRows = document.getElementById('classSearchRows');
const courseForm = document.getElementById('courseForm');
const courseRows = document.getElementById('courseRows');

function showStatus(message = '', error = false) {
    statusBox.textContent = message;
    statusBox.classList.toggle('error', error);
}
function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}
async function request(url, options = {}) {
    const response = await fetch(url, { headers: { 'Content-Type': 'application/json', Accept: 'application/json' }, ...options });
    if (!response.ok) {
        const body = await response.text();
        throw new Error(body || `Máy chủ phản hồi ${response.status}`);
    }
    return response.status === 204 ? null : response.json();
}
function value(id) { return document.getElementById(id).value.trim(); }
function dateValue(id) { return value(id) ? `${value(id)}:00` : null; }

async function loadCourses() {
    const courses = await request(`${api}/courses`);
    const select = document.getElementById('courseId');
    select.innerHTML = '<option value="">Chọn học phần</option>' + courses.map((c) =>
        `<option value="${escapeHtml(c.id)}">${escapeHtml(c.code)} — ${escapeHtml(c.name)}</option>`).join('');
    courseRows.innerHTML = courses.length ? courses.map((c) => `<tr><td>${escapeHtml(c.code)}</td><td>${escapeHtml(c.name)}</td><td>${escapeHtml(c.credits ?? '—')}</td><td>${c.active ? 'Đang hoạt động' : 'Ngừng'}</td><td><div class="action-row"><button class="btn btn-sm btn-outline-primary" data-edit-course="${escapeHtml(c.id)}">Sửa</button><button class="btn btn-sm btn-outline-danger" data-delete-course="${escapeHtml(c.id)}">Xóa</button></div></td></tr>`).join('')
        : '<tr><td colspan="5" class="text-center muted">Chưa có học phần.</td></tr>';
    courseRows.querySelectorAll('[data-edit-course]').forEach((button) => button.addEventListener('click', () => {
        const c = courses.find((item) => item.id === button.dataset.editCourse);
        if (!c) return;
        const fields = { courseRecordId: c.id, departmentId: c.departmentId, courseCode: c.code, courseName: c.name,
            courseNameEn: c.nameEn, credits: c.credits, courseType: c.courseType, theoryHours: c.theoryHours,
            practiceHours: c.practiceHours, selfStudyHours: c.selfStudyHours, internshipCredits: c.internshipCredits,
            courseDescription: c.description };
        Object.entries(fields).forEach(([id, v]) => { document.getElementById(id).value = v ?? ''; });
        document.getElementById('courseActive').checked = c.active !== false;
        courseForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }));
    courseRows.querySelectorAll('[data-delete-course]').forEach((button) => button.addEventListener('click', () => deleteCourse(button.dataset.deleteCourse)));
}

async function deleteCourse(id) {
    if (!window.confirm('Xóa học phần này? Học phần đang có lớp sẽ không thể xóa.')) return;
    try {
        await request(`${api}/courses/${encodeURIComponent(id)}`, { method: 'DELETE' });
        await loadCourses();
        showStatus('Đã xóa học phần.');
    } catch (error) {
        showStatus(`Không xóa được học phần: ${error.message}`, true);
    }
}

function numberValue(id) { return value(id) ? Number(value(id)) : null; }
courseForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = value('courseRecordId');
    const payload = { departmentId: value('departmentId') || null, code: value('courseCode'), name: value('courseName'),
        nameEn: value('courseNameEn') || null, credits: numberValue('credits'), courseType: value('courseType') || null,
        theoryHours: numberValue('theoryHours'), practiceHours: numberValue('practiceHours'),
        selfStudyHours: numberValue('selfStudyHours'), internshipCredits: numberValue('internshipCredits'),
        description: value('courseDescription') || null, active: document.getElementById('courseActive').checked };
    try {
        await request(id ? `${api}/courses/${encodeURIComponent(id)}` : `${api}/courses`,
            { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
        courseForm.reset(); document.getElementById('courseRecordId').value = ''; document.getElementById('courseActive').checked = true;
        await loadCourses(); showStatus(id ? 'Đã cập nhật học phần.' : 'Đã tạo học phần.');
    } catch (error) { showStatus(`Không lưu được học phần: ${error.message}`, true); }
});
document.getElementById('resetCourseForm').addEventListener('click', () => { courseForm.reset(); document.getElementById('courseRecordId').value = ''; document.getElementById('courseActive').checked = true; });

async function loadSections(url = `${api}/course-sections`) {
    try {
        const sections = await request(url);
        rows.innerHTML = sections.length ? sections.map((s) => `
            <tr><td>${escapeHtml(s.code)}</td><td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.courseCode)} — ${escapeHtml(s.courseName)}</td>
            <td>${escapeHtml(s.minStudents ?? '—')} / ${escapeHtml(s.maxStudents ?? '—')}</td>
            <td>${escapeHtml(s.status || (s.active ? 'Đang hoạt động' : 'Ngừng'))}</td>
            <td><div class="action-row"><button class="btn btn-sm btn-outline-primary" data-edit="${escapeHtml(s.id)}">Sửa</button>
            <button class="btn btn-sm btn-outline-secondary" data-roster="${escapeHtml(s.id)}">Sinh viên</button>
            <button class="btn btn-sm btn-outline-danger" data-delete="${escapeHtml(s.id)}">Xóa</button></div></td></tr>`).join('')
            : '<tr><td colspan="6" class="text-center muted">Không có lớp học phần phù hợp.</td></tr>';
        rows.querySelectorAll('[data-edit]').forEach((b) => b.addEventListener('click', () => editSection(sections.find((s) => s.id === b.dataset.edit))));
        rows.querySelectorAll('[data-roster]').forEach((b) => b.addEventListener('click', () => loadRoster(b.dataset.roster)));
        rows.querySelectorAll('[data-delete]').forEach((b) => b.addEventListener('click', () => deleteSection(b.dataset.delete)));
        showStatus();
    } catch (error) { showStatus(`Không tải được lớp học phần: ${error.message}`, true); }
}

function editSection(s) {
    if (!s) return;
    const map = { sectionId: s.id, code: s.code, name: s.name, courseId: s.courseId, semesterId: s.semesterId,
        roomId: s.roomId, buildingId: s.buildingId, maxStudents: s.maxStudents, minStudents: s.minStudents,
        classType: s.classType, sectionStatus: s.status, registrationStart: s.registrationStart,
        registrationEnd: s.registrationEnd, note: s.note, active: s.active !== false };
    Object.entries(map).forEach(([id, v]) => {
        const field = document.getElementById(id);
        if (field.type === 'checkbox') field.checked = Boolean(v);
        else if (field.type === 'datetime-local' && v) field.value = v.slice(0, 16);
        else field.value = v ?? '';
    });
    form.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function loadRoster(id) {
    try {
        const students = await request(`${api}/course-sections/${encodeURIComponent(id)}/students`);
        document.getElementById('rosterTitle').textContent = `(${id})`;
        document.getElementById('rosterRows').innerHTML = students.length ? students.map((s) =>
            `<tr><td>${escapeHtml(s.studentCode)}</td><td>${escapeHtml(s.fullName)}</td><td>${escapeHtml(s.email)}</td><td>${escapeHtml(s.status)}</td><td>${escapeHtml(s.registrationType)}</td></tr>`).join('')
            : '<tr><td colspan="5" class="text-center muted">Chưa có sinh viên trong lớp.</td></tr>';
        rosterPanel.hidden = false;
        rosterPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) { showStatus(`Không tải được danh sách sinh viên: ${error.message}`, true); }
}

async function deleteSection(id) {
    if (!window.confirm('Xóa lớp học phần này?')) return;
    try { await request(`${api}/course-sections/${encodeURIComponent(id)}`, { method: 'DELETE' }); await loadSections(); showStatus('Đã xóa lớp học phần.'); }
    catch (error) { showStatus(`Không xóa được lớp: ${error.message}`, true); }
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = value('sectionId');
    const payload = {
        code: value('code'), name: value('name'), courseId: value('courseId'),
        semesterId: value('semesterId') || null, roomId: value('roomId') || null, buildingId: value('buildingId') || null,
        maxStudents: value('maxStudents') ? Number(value('maxStudents')) : null,
        minStudents: value('minStudents') ? Number(value('minStudents')) : null,
        classType: value('classType') || null, status: value('sectionStatus') || null,
        registrationStart: dateValue('registrationStart'), registrationEnd: dateValue('registrationEnd'),
        note: value('note') || null, active: document.getElementById('active').checked
    };
    try {
        await request(id ? `${api}/course-sections/${encodeURIComponent(id)}` : `${api}/course-sections`,
            { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
        form.reset(); document.getElementById('sectionId').value = ''; document.getElementById('active').checked = true;
        await loadSections(); showStatus(id ? 'Đã cập nhật lớp học phần.' : 'Đã tạo lớp học phần.');
    } catch (error) { showStatus(`Không lưu được lớp học phần: ${error.message}`, true); }
});

document.getElementById('resetForm').addEventListener('click', () => { form.reset(); document.getElementById('sectionId').value = ''; document.getElementById('active').checked = true; });
document.getElementById('searchSections').addEventListener('click', () => {
    const studentId = value('studentSearchId');
    const query = value('sectionSearch');
    const path = studentId ? `${api}/course-sections/by-student/${encodeURIComponent(studentId)}?keyword=${encodeURIComponent(query)}`
        : `${api}/course-sections?keyword=${encodeURIComponent(query)}`;
    loadSections(path);
});
document.getElementById('allSections').addEventListener('click', () => { document.getElementById('studentSearchId').value = ''; document.getElementById('sectionSearch').value = ''; loadSections(); });
document.getElementById('searchSectionsByLecturer').addEventListener('click', () => {
    const lecturerId = value('sectionLecturerId');
    if (!lecturerId) { showStatus('Nhập UUID giảng viên để tìm lớp học phần.', true); return; }
    loadSections(`${api}/course-sections/by-lecturer/${encodeURIComponent(lecturerId)}?keyword=${encodeURIComponent(value('sectionSearch'))}`);
});

async function loadAssignments(lecturerId = null) {
    try {
        const params = new URLSearchParams();
        if (lecturerId) params.set('lecturerId', lecturerId);
        const assignments = await request(`${api}/lecturer-assignments?${params}`);
        assignmentRows.innerHTML = assignments.length ? assignments.map((a) => `
            <tr><td>${escapeHtml(a.courseClassCode)}<div class="small muted">${escapeHtml(a.courseClassId)}</div></td>
            <td>${escapeHtml(a.courseCode)} — ${escapeHtml(a.courseName)}</td><td>${escapeHtml(a.lecturerName)}<div class="small muted">${escapeHtml(a.lecturerCode)} · ${escapeHtml(a.lecturerId)}</div></td>
            <td>${escapeHtml(a.role)}</td><td><button class="btn btn-sm btn-outline-danger" data-unassign="${escapeHtml(a.id)}">Gỡ</button></td></tr>`).join('')
            : '<tr><td colspan="5" class="text-center muted">Chưa có phân công.</td></tr>';
        assignmentRows.querySelectorAll('[data-unassign]').forEach((b) => b.addEventListener('click', async () => {
            try { await request(`${api}/lecturer-assignments/${encodeURIComponent(b.dataset.unassign)}`, { method: 'DELETE' }); await loadAssignments(value('lecturerSearchId')); }
            catch (error) { showStatus(`Không gỡ được phân công: ${error.message}`, true); }
        }));
    } catch (error) { showStatus(`Không tải được phân công: ${error.message}`, true); }
}

document.getElementById('assignmentForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        await request(`${api}/lecturer-assignments`, { method: 'POST', body: JSON.stringify({
            courseClassId: value('courseClassId'), lecturerId: value('lecturerId'), role: value('lecturerRole') || null
        }) });
        await loadAssignments(value('lecturerId')); showStatus('Đã phân công giảng viên.');
    } catch (error) { showStatus(`Không tạo được phân công: ${error.message}`, true); }
});

document.getElementById('searchLecturerClasses').addEventListener('click', async () => {
    const lecturerId = value('lecturerSearchId');
    try {
        const classes = await request(`${api}/course-classes?lecturerId=${encodeURIComponent(lecturerId)}`);
        if (classes.length) {
            classSearchRows.innerHTML = classes.map((c) => `<tr><td><button class="btn btn-link p-0" data-pick-class="${escapeHtml(c.id)}">${escapeHtml(c.classCode)}</button><div class="small muted">${escapeHtml(c.id)}</div></td><td>${escapeHtml(c.courseCode)} — ${escapeHtml(c.courseName)}</td><td>${escapeHtml(c.lecturerName)}</td><td>${escapeHtml(c.status)}</td></tr>`).join('');
            classSearchRows.querySelectorAll('[data-pick-class]').forEach((b) => b.addEventListener('click', () => { document.getElementById('courseClassId').value = b.dataset.pickClass; document.getElementById('lecturerId').value = lecturerId; }));
        } else classSearchRows.innerHTML = '<tr><td colspan="4" class="text-center muted">Giảng viên chưa được gán lớp.</td></tr>';
        await loadAssignments(lecturerId);
    } catch (error) { showStatus(`Không tìm được lớp của giảng viên: ${error.message}`, true); }
});

document.getElementById('searchClasses').addEventListener('click', async () => {
    try {
        const classes = await request(`${api}/course-classes?keyword=${encodeURIComponent(value('classCodeSearch'))}`);
        classSearchRows.innerHTML = classes.length ? classes.map((c) => `<tr><td><button class="btn btn-link p-0" data-pick-class="${escapeHtml(c.id)}">${escapeHtml(c.classCode)}</button><div class="small muted">${escapeHtml(c.id)}</div></td><td>${escapeHtml(c.courseCode)} — ${escapeHtml(c.courseName)}</td><td>${escapeHtml(c.lecturerName)}</td><td>${escapeHtml(c.status)}</td></tr>`).join('')
            : '<tr><td colspan="4" class="text-center muted">Không tìm thấy lớp.</td></tr>';
        classSearchRows.querySelectorAll('[data-pick-class]').forEach((b) => b.addEventListener('click', () => { document.getElementById('courseClassId').value = b.dataset.pickClass; }));
    } catch (error) { showStatus(`Không tải được lớp học phần: ${error.message}`, true); }
});

loadCourses().catch((error) => showStatus(`Không tải được danh mục học phần: ${error.message}`, true));
loadSections();
loadAssignments();
