const API_BASE = '/api';
const pageParams = new URLSearchParams(window.location.search);
const audienceParam = pageParams.get('audience');
const personIdParam = pageParams.get('personId') || '';
const DAYS = ['Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy', 'Chủ nhật'];
const DEFAULT_PERIOD_COUNT = 12;
const today = startOfDay(new Date());
let focusedDate = new Date(today);
let currentView = 'week';
let schedules = [];
let scheduleExceptions = [];
let requestId = 0;
let listPage = 0;
let totalListPages = 0;
let editorSections = [];
let editorRooms = [];
let lecturerOptions = [];
let shiftOptions = [];
let selectedOccurrenceDate = null;
let selectedException = null;

const calendarView = document.getElementById('calendarView');
const statusElement = document.getElementById('calendarStatus');
const periodTitle = document.getElementById('periodTitle');
const miniTitle = document.getElementById('miniMonthTitle');
const miniDays = document.getElementById('miniDays');
const searchInput = document.getElementById('eventSearch');
const eventDialog = document.getElementById('eventDialog');
const occurrenceDialog = document.getElementById('occurrenceDialog');
const personalCalendarForm = document.getElementById('personalCalendarForm');
const audienceSelect = document.getElementById('calendarAudience');
const personIdInput = document.getElementById('calendarPersonId');
const addScheduleButton = document.getElementById('addScheduleButton');
const scheduleActions = document.getElementById('scheduleActions');
const scheduleEditorDialog = document.getElementById('scheduleEditorDialog');
const scheduleEditorForm = document.getElementById('scheduleEditorForm');
const scheduleEditorError = document.getElementById('scheduleEditorError');
let editingSchedule = null;
let selectedSchedule = null;
let editorOptionsLoaded = false;

if (audienceParam === 'student' || audienceParam === 'lecturer') audienceSelect.value = audienceParam;
personIdInput.value = personIdParam;
personalCalendarForm.addEventListener('submit', (event) => {
    event.preventDefault();
    if (!personIdInput.value.trim()) return;
    const params = new URLSearchParams({ audience: audienceSelect.value, personId: personIdInput.value.trim() });
    window.location.search = params.toString();
});

const canManageSchedules = true;
addScheduleButton.hidden = false;
addScheduleButton.addEventListener('click', () => openScheduleEditor());
document.getElementById('editScheduleButton').addEventListener('click', () => {
    if (selectedSchedule) openScheduleEditor(selectedSchedule);
});
document.getElementById('deleteScheduleButton').addEventListener('click', deleteSelectedSchedule);
document.getElementById('manageOccurrenceButton').addEventListener('click', openOccurrenceEditor);
document.getElementById('closeScheduleEditor').addEventListener('click', () => scheduleEditorDialog.close());
document.getElementById('cancelScheduleEditor').addEventListener('click', () => scheduleEditorDialog.close());
scheduleEditorForm.addEventListener('submit', saveSchedule);
document.getElementById('closeOccurrenceEditor').addEventListener('click', () => occurrenceDialog.close());
document.getElementById('cancelOccurrenceEditor').addEventListener('click', () => occurrenceDialog.close());
document.getElementById('occurrenceForm').addEventListener('submit', saveOccurrenceException);
document.getElementById('occurrenceType').addEventListener('change', updateOccurrenceFields);
document.getElementById('exceptionOccurrenceDate').addEventListener('change', loadExceptionForDate);
document.getElementById('replacementRoom').addEventListener('change', updateOccurrenceCapacityNotice);
document.getElementById('deleteOccurrenceException').addEventListener('click', deleteOccurrenceException);
document.getElementById('editorCourseSection').addEventListener('change', updateRoomOptions);
document.getElementById('editorRoom').addEventListener('change', updateCapacityNotice);
document.getElementById('editorShift').addEventListener('change', applyShiftPeriods);
document.getElementById('printScheduleButton').addEventListener('click', () => window.print());
document.getElementById('exportScheduleButton').addEventListener('click', exportSchedules);

document.getElementById('todayButton').addEventListener('click', () => navigateTo(today));
document.getElementById('prevPeriod').addEventListener('click', () => shiftPeriod(-1));
document.getElementById('nextPeriod').addEventListener('click', () => shiftPeriod(1));
document.getElementById('miniPrev').addEventListener('click', () => shiftMiniMonth(-1));
document.getElementById('miniNext').addEventListener('click', () => shiftMiniMonth(1));
let searchTimer = null;
searchInput.addEventListener('input', () => {
    if (currentView === 'list') {
        window.clearTimeout(searchTimer);
        searchTimer = window.setTimeout(() => { listPage = 0; loadSchedules(); }, 250);
    } else {
        renderCalendar();
    }
});
['filterSemester', 'filterYear', 'filterDay', 'filterRoom', 'filterSection'].forEach((id) => {
    document.getElementById(id).addEventListener('change', () => {
        listPage = 0;
        loadSchedules();
    });
});
document.querySelectorAll('[data-view]').forEach((button) => {
    button.addEventListener('click', () => {
        currentView = button.dataset.view;
        document.querySelectorAll('[data-view]').forEach((viewButton) => {
            viewButton.classList.toggle('active', viewButton === button);
        });
        loadSchedules();
    });
});

function startOfDay(date) {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

function mondayOf(date) {
    const day = (date.getDay() + 6) % 7;
    return addDays(startOfDay(date), -day);
}

function toIsoDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function parseDate(value) {
    return value ? new Date(`${String(value).slice(0, 10)}T00:00:00`) : null;
}

function sameDate(first, second) {
    return first.getFullYear() === second.getFullYear()
        && first.getMonth() === second.getMonth()
        && first.getDate() === second.getDate();
}

function formatMonthYear(date) {
    return new Intl.DateTimeFormat('vi-VN', { month: 'long', year: 'numeric' }).format(date);
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (character) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[character]);
}

function eventName(schedule) {
    return schedule.courseName || schedule.courseSectionName || schedule.courseCode || 'Buổi học';
}

function modeLabel(schedule) {
    const mode = String(schedule.mode || '').trim();
    if (!mode) return '';
    const normalized = mode.toLocaleLowerCase('vi');
    if (normalized.includes('online') || normalized.includes('trực tuyến')) return 'Online';
    if (normalized.includes('trực tiếp') || normalized.includes('offline')) return 'Trực tiếp';
    return mode;
}

function eventSearchText(schedule) {
    return [
        schedule.courseName, schedule.courseCode, schedule.courseSectionName,
        schedule.courseSectionCode, schedule.roomName, schedule.roomCode,
        schedule.buildingName, schedule.lecturerName
    ].join(' ').toLocaleLowerCase('vi');
}

function colorClass(schedule) {
    const key = schedule.courseCode || schedule.courseSectionCode || schedule.courseSectionId || '';
    let hash = 0;
    for (const character of key) hash = (hash * 31 + character.charCodeAt(0)) | 0;
    return `color-${Math.abs(hash) % 5}`;
}

function visibleEvents() {
    const query = searchInput.value.trim().toLocaleLowerCase('vi');
    const semesterId = document.getElementById('filterSemester').value;
    const year = Number(document.getElementById('filterYear').value) || null;
    const day = Number(document.getElementById('filterDay').value) || null;
    const roomId = document.getElementById('filterRoom').value;
    const sectionId = document.getElementById('filterSection').value;
    return schedules.filter((schedule) => {
        const starts = parseDate(schedule.startDate);
        const ends = parseDate(schedule.endDate);
        return (!query || eventSearchText(schedule).includes(query))
            && (!semesterId || schedule.semesterId === semesterId)
            && (!year || (starts && ends && starts.getFullYear() <= year && ends.getFullYear() >= year))
            && (!day || Number(schedule.dayOfWeek) === day)
            && (!roomId || schedule.roomId === roomId)
            && (!sectionId || schedule.courseSectionId === sectionId);
    });
}

function setStatus(message = '', isError = false) {
    statusElement.textContent = message;
    statusElement.hidden = !message;
    statusElement.classList.toggle('error', isError);
}

async function loadFilterOptions() {
    const endpoints = [
        ['/semesters', 'semester'],
        ['/rooms?active=true&size=200', 'room'],
        ['/course-sections?active=true', 'section'],
        ['/schedules/options/shifts', 'shift'],
        ['/lecturer-options', 'lecturer']
    ];
    const responses = await Promise.all(endpoints.map(async ([url, key]) => {
        const response = await fetch(`${API_BASE}${url}`, { headers: { Accept: 'application/json' } });
        if (!response.ok) throw new Error(`Không tải được dữ liệu ${key} (HTTP ${response.status}).`);
        return [key, await response.json()];
    }));
    const values = Object.fromEntries(responses);
    const rooms = Array.isArray(values.room) ? values.room : values.room.content || [];
    editorSections = values.section;
    editorRooms = rooms;
    lecturerOptions = values.lecturer;
    shiftOptions = values.shift;

    document.getElementById('filterSemester').insertAdjacentHTML('beforeend', values.semester.map((semester) =>
        `<option value="${escapeHtml(semester.id)}">${escapeHtml([semester.name, semester.schoolYearName].filter(Boolean).join(' · ') || semester.id.slice(0, 8))}</option>`
    ).join(''));
    document.getElementById('filterRoom').insertAdjacentHTML('beforeend', rooms.map((room) =>
        `<option value="${escapeHtml(room.id)}">${escapeHtml([room.roomCode, room.roomName].filter(Boolean).join(' · '))}</option>`
    ).join(''));
    document.getElementById('filterSection').insertAdjacentHTML('beforeend', editorSections.map((section) =>
        `<option value="${escapeHtml(section.id)}">${escapeHtml([section.code, section.name].filter(Boolean).join(' · '))}</option>`
    ).join(''));
}

function filterQueryParams() {
    const params = new URLSearchParams();
    const values = {
        keyword: searchInput.value.trim(),
        semesterId: document.getElementById('filterSemester').value,
        year: document.getElementById('filterYear').value,
        dayOfWeek: document.getElementById('filterDay').value,
        roomId: document.getElementById('filterRoom').value,
        courseSectionId: document.getElementById('filterSection').value
    };
    Object.entries(values).forEach(([key, value]) => {
        if (value) params.set(key, value);
    });
    return params;
}

async function loadSchedules() {
    const currentRequest = ++requestId;
    if (currentView === 'list') {
        await loadScheduleList(currentRequest);
        return;
    }
    const dates = currentView === 'week'
        ? [mondayOf(focusedDate)]
        : currentView === 'day'
            ? [focusedDate]
            : Array.from({ length: 6 }, (_, index) => addDays(monthRangeStart(focusedDate), index * 7));
    setStatus('Đang tải lịch học…');
    calendarView.innerHTML = '';

    try {
        const weeks = await Promise.all(dates.map(async (date) => {
            const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(personIdParam);
            const schedulePath = personIdParam && (audienceParam === 'student' || audienceParam === 'lecturer')
                ? `/schedules/${audienceParam}/${isUuid ? '' : 'by-code/'}${encodeURIComponent(personIdParam)}/weekly`
                : '/schedules/weekly';
            const url = `${API_BASE}${schedulePath}?date=${encodeURIComponent(toIsoDate(date))}`;
            const response = await fetch(url, { headers: { Accept: 'application/json' } });
            if (!response.ok) {
                if (response.status === 404 && audienceParam === 'student') {
                    throw new Error(`Mã ${personIdParam} chưa có hồ sơ học vụ hoặc lịch đăng ký. Hãy chọn sinh viên có lớp học phần.`);
                }
                throw new Error(`Máy chủ phản hồi ${response.status}`);
            }
            return response.json();
        }));
        if (currentRequest !== requestId) return;
        if (weeks.some((week) => !Array.isArray(week))) throw new Error('Dữ liệu lịch học trả về không đúng định dạng');
        schedules = [...new Map(weeks.flat().map((schedule) => [schedule.id, schedule])).values()];
        const rangeStart = currentView === 'month' ? dates[0] : dates[0];
        const rangeEnd = currentView === 'month' ? addDays(dates[dates.length - 1], 6)
            : currentView === 'day' ? focusedDate : addDays(dates[0], 6);
        let exceptionError = '';
        try {
            const exceptionsResponse = await fetch(
                `${API_BASE}/schedule-exceptions?fromDate=${toIsoDate(rangeStart)}&toDate=${toIsoDate(rangeEnd)}`,
                { headers: { Accept: 'application/json' } }
            );
            if (!exceptionsResponse.ok) throw new Error(`HTTP ${exceptionsResponse.status}`);
            scheduleExceptions = await exceptionsResponse.json();
            for (const exception of scheduleExceptions) {
                if (!schedules.some((schedule) => schedule.id === exception.scheduleId)) {
                    schedules.push(exception.schedule);
                }
            }
        } catch (error) {
            scheduleExceptions = [];
            exceptionError = `Không tải được ngoại lệ lịch (${error.message}). Nếu chưa có bảng, chạy database/migrations/20261127_create_schedule_exceptions.sql.`;
        }
        if (currentRequest !== requestId) return;
        setStatus(exceptionError, Boolean(exceptionError));
        renderCalendar();
    } catch (error) {
        if (currentRequest !== requestId) return;
        schedules = [];
        renderCalendar();
        setStatus(`Không thể tải lịch học. ${error.message}`, true);
    }
}

async function loadScheduleList(currentRequest) {
    setStatus('Đang tải danh sách lịch học…');
    calendarView.innerHTML = '';
    const params = filterQueryParams();
    params.set('page', String(listPage));
    params.set('size', '20');
    try {
        const response = await fetch(`${API_BASE}/schedules?${params}`, { headers: { Accept: 'application/json' } });
        if (!response.ok) throw new Error(`Máy chủ phản hồi ${response.status}`);
        const result = await response.json();
        if (currentRequest !== requestId) return;
        schedules = result.content || [];
        totalListPages = result.totalPages || 0;
        setStatus(`${result.totalElements || 0} lịch học`);
        renderScheduleList(result);
    } catch (error) {
        if (currentRequest !== requestId) return;
        schedules = [];
        calendarView.innerHTML = '<div class="empty-state">Không tải được danh sách lịch học.</div>';
        setStatus(error.message, true);
    }
}

function renderScheduleList(result) {
    if (!result.content?.length) {
        calendarView.innerHTML = '<div class="empty-state">Không có lịch học phù hợp.</div>';
        return;
    }
    const rows = result.content.map((schedule) => {
        const detailDate = firstOccurrence(schedule.startDate.slice(0, 10), Number(schedule.dayOfWeek));
        return `
        <tr>
            <td>${escapeHtml(schedule.courseSectionCode || '')}</td>
            <td>${escapeHtml(schedule.courseName || schedule.courseSectionName || '')}</td>
            <td>${escapeHtml(schedule.lecturerName || 'Chưa phân công')}</td>
            <td>${escapeHtml([schedule.roomCode, schedule.roomName].filter(Boolean).join(' · '))}</td>
            <td>${escapeHtml(DAYS[Number(schedule.dayOfWeek) - 1] || '')}</td>
            <td>${escapeHtml(shiftOptions.find((shift) => shift.id === schedule.shiftId)?.name || '')}</td>
            <td>${escapeHtml(`${schedule.startPeriod || ''}–${schedule.endPeriod || ''}`)}</td>
            <td>${escapeHtml(schedule.startDate?.slice(0, 10) || '')} – ${escapeHtml(schedule.endDate?.slice(0, 10) || '')}</td>
            <td>${escapeHtml(schedule.scheduleStatus || schedule.status || '')}</td>
            <td><button type="button" class="today-button" data-schedule-id="${escapeHtml(schedule.id)}" data-occurrence="${toIsoDate(detailDate)}">Chi tiết</button></td>
        </tr>`;
    }).join('');
    calendarView.innerHTML = `
        <div class="schedule-list-wrap">
            <table class="schedule-list-table">
                <thead><tr><th>Mã lớp</th><th>Môn / lớp học phần</th><th>Giảng viên</th><th>Phòng</th><th>Thứ</th><th>Ca</th><th>Tiết</th><th>Khoảng ngày</th><th>Trạng thái</th><th></th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
            <div class="list-pagination">
                <button type="button" class="today-button" id="listPrevious" ${result.first ? 'disabled' : ''}>Trước</button>
                <span>Trang ${Number(result.number || 0) + 1} / ${Math.max(1, Number(result.totalPages || 0))}</span>
                <button type="button" class="today-button" id="listNext" ${result.last ? 'disabled' : ''}>Sau</button>
            </div>
        </div>`;
    document.getElementById('listPrevious').addEventListener('click', () => {
        if (listPage > 0) { listPage--; loadSchedules(); }
    });
    document.getElementById('listNext').addEventListener('click', () => {
        if (listPage + 1 < totalListPages) { listPage++; loadSchedules(); }
    });
    calendarView.querySelectorAll('[data-schedule-id]').forEach((button) => {
        button.addEventListener('click', () => showDetails(button.dataset.scheduleId, button.dataset.occurrence));
    });
}

async function loadEditorOptions() {
    if (editorOptionsLoaded) return;
    const code = encodeURIComponent(personIdParam);
    const sectionPath = audienceParam === 'student'
        ? `/schedules/student/by-code/${code}/course-sections`
        : audienceParam === 'lecturer'
            ? `/schedules/lecturer/by-code/${code}/course-sections`
            : '/course-sections?active=true';
    const [sectionResponse, roomResponse] = await Promise.all([
        fetch(`${API_BASE}${sectionPath}`, { headers: { Accept: 'application/json' } }),
        fetch(`${API_BASE}/rooms?active=true&size=200`, { headers: { Accept: 'application/json' } })
    ]);
    if (!sectionResponse.ok) {
        if (audienceParam === 'student' && sectionResponse.status === 404) {
            throw new Error(`Mã ${personIdParam} chưa có hồ sơ học vụ. Hãy chọn sinh viên có lớp học phần.`);
        }
        if (audienceParam === 'lecturer' && sectionResponse.status === 404) {
            throw new Error(`Không tìm thấy hồ sơ giảng viên ${personIdParam}.`);
        }
        throw new Error(`Không tải được danh sách lớp học phần (HTTP ${sectionResponse.status}).`);
    }
    if (!roomResponse.ok) throw new Error(`Không tải được danh sách phòng học (HTTP ${roomResponse.status}).`);
    const sections = await sectionResponse.json();
    const roomPayload = await roomResponse.json();
    const rooms = Array.isArray(roomPayload) ? roomPayload : roomPayload.content || [];
    editorSections = sections;
    editorRooms = rooms;
    document.getElementById('editorCourseSection').innerHTML = sections.map((section) =>
        `<option value="${escapeHtml(section.id)}">${escapeHtml([section.code, section.name, section.courseName].filter(Boolean).join(' · '))}</option>`
    ).join('');
    document.getElementById('editorRoom').innerHTML = rooms.map((room) =>
        `<option value="${escapeHtml(room.id)}">${escapeHtml([room.roomCode, room.roomName].filter(Boolean).join(' · '))}</option>`
    ).join('');
    document.getElementById('editorLecturer').innerHTML = '<option value="">Chưa phân công</option>'
        + lecturerOptions.map((lecturer) =>
            `<option value="${escapeHtml(lecturer.employeeId)}">${escapeHtml([lecturer.code, lecturer.fullName].filter(Boolean).join(' · '))}</option>`
        ).join('');
    document.getElementById('editorShift').innerHTML = '<option value="">Chưa chọn ca</option>'
        + shiftOptions.map((shift) =>
            `<option value="${escapeHtml(shift.id)}">${escapeHtml(`${shift.name} (tiết ${shift.startPeriod}–${shift.endPeriod})`)}</option>`
        ).join('');
    document.getElementById('replacementRoom').innerHTML = rooms.map((room) =>
        `<option value="${escapeHtml(room.id)}">${escapeHtml(`${[room.roomCode, room.roomName].filter(Boolean).join(' · ')} · ${room.capacity ?? 'chưa rõ'} chỗ`)}</option>`
    ).join('');
    document.getElementById('substituteLecturer').innerHTML = lecturerOptions.map((lecturer) =>
        `<option value="${escapeHtml(lecturer.employeeId)}">${escapeHtml([lecturer.code, lecturer.fullName].filter(Boolean).join(' · '))}</option>`
    ).join('');
    if (!sections.length) {
        throw new Error(audienceParam === 'student'
            ? `Sinh viên ${personIdParam} chưa có lớp học phần đã đăng ký.`
            : 'Không tìm thấy lớp học phần phù hợp với đối tượng này.');
    }
    if (!rooms.length) throw new Error('Không tìm thấy phòng học đang hoạt động.');
    editorOptionsLoaded = true;
}

async function openScheduleEditor(schedule = null) {
    if (!canManageSchedules) return;
    try {
        scheduleEditorError.hidden = true;
        await loadEditorOptions();
        editingSchedule = schedule;
        document.getElementById('scheduleEditorTitle').textContent = schedule ? 'Sửa lịch học' : 'Thêm lịch học';
        document.getElementById('saveScheduleButton').textContent = schedule ? 'Lưu thay đổi' : 'Thêm lịch';
        document.getElementById('editorCourseSection').value = schedule?.courseSectionId || '';
        document.getElementById('editorRoom').value = schedule?.roomId || '';
        document.getElementById('editorLecturer').value = schedule?.employeeId || '';
        document.getElementById('editorShift').value = schedule?.shiftId || '';
        // Set day checkboxes
        const dayCheckboxes = document.querySelectorAll('#editorDays input[name="editorDay"]');
        const defaultDay = String(schedule?.dayOfWeek || ((focusedDate.getDay() + 6) % 7 + 1));
        dayCheckboxes.forEach((cb) => {
            cb.checked = schedule ? cb.value === String(schedule.dayOfWeek) : cb.value === defaultDay;
        });
        document.getElementById('editorStartPeriod').value = schedule?.startPeriod || 1;
        document.getElementById('editorEndPeriod').value = schedule?.endPeriod || 3;
        const monday = mondayOf(focusedDate);
        const defaultStart = toIsoDate(monday);
        const defaultEnd = toIsoDate(addDays(monday, 90));
        document.getElementById('editorStartDate').value = schedule?.startDate?.slice(0, 10) || defaultStart;
        document.getElementById('editorEndDate').value = schedule?.endDate?.slice(0, 10) || defaultEnd;
        document.getElementById('editorMode').value = schedule?.mode || 'Trực tiếp';
        document.getElementById('editorNote').value = schedule?.note || '';
        document.getElementById('roomAlternatives').hidden = true;
        await updateRoomOptions();
        if (!scheduleEditorDialog.open) scheduleEditorDialog.showModal();
    } catch (error) {
        scheduleEditorError.textContent = error.message;
        scheduleEditorError.hidden = false;
        if (!scheduleEditorDialog.open) scheduleEditorDialog.showModal();
    }
}

function applyShiftPeriods() {
    const shift = shiftOptions.find((item) => item.id === document.getElementById('editorShift').value);
    if (!shift) return;
    document.getElementById('editorStartPeriod').value = shift.startPeriod;
    document.getElementById('editorEndPeriod').value = shift.endPeriod;
}

async function updateCapacityNotice() {
    const notice = document.getElementById('editorCapacityNotice');
    const sectionId = document.getElementById('editorCourseSection').value;
    const roomId = document.getElementById('editorRoom').value;
    if (!sectionId) {
        notice.hidden = true;
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/course-sections/${encodeURIComponent(sectionId)}/enrollment-count`);
        if (!response.ok) throw new Error(`Không lấy được sĩ số lớp (HTTP ${response.status}).`);
        const count = await response.json();
        const room = editorRooms.find((item) => item.id === roomId);
        notice.textContent = room && room.capacity != null && count > room.capacity
            ? `Cảnh báo: lớp có ${count} sinh viên, vượt sức chứa ${room.roomCode} (${room.capacity}). Khi lưu sẽ cần xác nhận.`
            : `Sĩ số hiện tại: ${count} sinh viên.`;
        notice.hidden = false;
    } catch (error) {
        notice.textContent = error.message;
        notice.hidden = false;
    }
}

async function updateRoomOptions() {
    const sectionId = document.getElementById('editorCourseSection').value;
    const roomSelect = document.getElementById('editorRoom');
    const selectedRoomId = roomSelect.value;
    if (!sectionId) return updateCapacityNotice();
    try {
        const response = await fetch(`${API_BASE}/course-sections/${encodeURIComponent(sectionId)}/enrollment-count`);
        if (!response.ok) throw new Error(`Không xác định được sĩ số lớp (HTTP ${response.status}).`);
        const count = Number(await response.json());
        const sufficient = editorRooms.filter((room) => room.capacity == null || room.capacity >= count);
        const small = editorRooms.filter((room) => room.capacity != null && room.capacity < count);
        const option = (room) => `<option value="${escapeHtml(room.id)}">${escapeHtml(
            `${[room.roomCode, room.roomName].filter(Boolean).join(' · ')} · ${room.capacity ?? 'chưa rõ'} chỗ`
        )}</option>`;
        roomSelect.innerHTML = `${sufficient.length
            ? `<optgroup label="Đủ sức chứa">${sufficient.map(option).join('')}</optgroup>` : ''}
            ${small.length ? `<optgroup label="Thiếu sức chứa — cần xác nhận">${small.map(option).join('')}</optgroup>` : ''}`;
        if (editorRooms.some((room) => room.id === selectedRoomId)) roomSelect.value = selectedRoomId;
        await updateCapacityNotice();
    } catch (error) {
        const notice = document.getElementById('editorCapacityNotice');
        notice.textContent = error.message;
        notice.hidden = false;
    }
}

async function saveSchedule(event) {
    event.preventDefault();
    scheduleEditorError.hidden = true;
    const startPeriod = Number(document.getElementById('editorStartPeriod').value);
    const endPeriod = Number(document.getElementById('editorEndPeriod').value);
    if (endPeriod < startPeriod) {
        scheduleEditorError.textContent = 'Tiết kết thúc phải lớn hơn hoặc bằng tiết bắt đầu.';
        scheduleEditorError.hidden = false;
        return;
    }
    const startDate = document.getElementById('editorStartDate').value;
    const endDate = document.getElementById('editorEndDate').value;
    const startTime = editingSchedule?.startDate?.slice(11, 19) || '00:00:00';
    const endTime = editingSchedule?.endDate?.slice(11, 19) || '00:00:00';
    const selectedDays = [...document.querySelectorAll('#editorDays input[name="editorDay"]:checked')].map((cb) => Number(cb.value));
    if (selectedDays.length === 0) {
        scheduleEditorError.textContent = 'Vui lòng chọn ít nhất một thứ trong tuần.';
        scheduleEditorError.hidden = false;
        return;
    }
    const payload = {
        courseSectionId: document.getElementById('editorCourseSection').value,
        roomId: document.getElementById('editorRoom').value,
        employeeId: document.getElementById('editorLecturer').value || null,
        shiftId: document.getElementById('editorShift').value || null,
        daysOfWeek: selectedDays,
        dayOfWeek: selectedDays[0],
        startPeriod,
        endPeriod,
        numberOfPeriods: endPeriod - startPeriod + 1,
        startDate: `${startDate}T${startTime}`,
        endDate: `${endDate}T${endTime}`,
        mode: document.getElementById('editorMode').value.trim() || null,
        note: document.getElementById('editorNote').value.trim() || null,
        active: true
    };
    const personPath = personIdParam && ['student', 'lecturer'].includes(audienceParam)
        ? `/schedules/${audienceParam}/by-code/${encodeURIComponent(personIdParam)}`
        : '/schedules';
    const url = editingSchedule
        ? `${API_BASE}${personPath}/${encodeURIComponent(editingSchedule.id)}`
        : `${API_BASE}${personPath}`;
    try {
        const room = editorRooms.find((item) => item.id === payload.roomId);
        if (room && room.capacity != null) {
            const countResponse = await fetch(`${API_BASE}/course-sections/${encodeURIComponent(payload.courseSectionId)}/enrollment-count`);
            if (!countResponse.ok) throw new Error(`Không xác định được sĩ số lớp (HTTP ${countResponse.status}).`);
            const count = await countResponse.json();
            if (count > room.capacity && !window.confirm(
                `Lớp có ${count} sinh viên nhưng phòng ${room.roomCode} chỉ chứa ${room.capacity}. Bạn vẫn muốn lưu?`
            )) return;
        }
        const response = await fetch(url, {
            method: editingSchedule ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error(await responseMessage(response));
        scheduleEditorDialog.close();
        document.getElementById('eventDialog').close();
        await loadSchedules();
    } catch (error) {
        scheduleEditorError.textContent = error.message;
        scheduleEditorError.hidden = false;
        const conflictMessage = error.message.toLocaleLowerCase('vi');
        if (conflictMessage.includes('trùng') && conflictMessage.includes('phòng')) {
            try {
                await showRoomAlternatives(payload);
            } catch (alternativeError) {
                scheduleEditorError.textContent += ` ${alternativeError.message}`;
            }
        }
    }
}

async function showRoomAlternatives(payload) {
    const container = document.getElementById('roomAlternatives');
    const params = new URLSearchParams({
        startDate: payload.startDate.slice(0, 10),
        endDate: payload.endDate.slice(0, 10),
        dayOfWeek: String(payload.daysOfWeek?.[0] || payload.dayOfWeek),
        startPeriod: String(payload.startPeriod),
        endPeriod: String(payload.endPeriod)
    });
    const countResponse = await fetch(`${API_BASE}/course-sections/${encodeURIComponent(payload.courseSectionId)}/enrollment-count`);
    if (!countResponse.ok) throw new Error(`Không xác định được sĩ số lớp (HTTP ${countResponse.status}).`);
    const capacity = Number(await countResponse.json());
    if (capacity > 0) params.set('minCapacity', String(capacity));
    const response = await fetch(`${API_BASE}/rooms/available/recurring?${params}`);
    if (!response.ok) throw new Error(`Không tải được phòng thay thế (HTTP ${response.status}).`);
    const rooms = await response.json();
    container.hidden = false;
    container.innerHTML = rooms.length
        ? `<strong>Phòng đang trống:</strong> ${rooms.map((room) =>
            `<button type="button" class="room-alternative" data-room-id="${escapeHtml(room.id)}">${escapeHtml(room.roomCode)}</button>`
        ).join(' ')}`
        : 'Không tìm thấy phòng trống phù hợp.';
    container.querySelectorAll('[data-room-id]').forEach((button) => {
        button.addEventListener('click', () => {
            document.getElementById('editorRoom').value = button.dataset.roomId;
            updateCapacityNotice();
        });
    });
}

function firstOccurrence(startValue, dayOfWeek) {
    let date = parseDate(startValue);
    while ((date.getDay() || 7) !== dayOfWeek) date = addDays(date, 1);
    return date;
}

async function deleteSelectedSchedule() {
    if (!selectedSchedule || !canManageSchedules) return;
    if (!window.confirm('Xóa lịch này? Lịch gắn với lớp học phần nên thao tác có thể ảnh hưởng các sinh viên hoặc giảng viên khác cùng lớp.')) return;
    const personPath = personIdParam && ['student', 'lecturer'].includes(audienceParam)
        ? `/schedules/${audienceParam}/by-code/${encodeURIComponent(personIdParam)}`
        : '/schedules';
    const url = `${API_BASE}${personPath}/${encodeURIComponent(selectedSchedule.id)}`;
    try {
        const response = await fetch(url, { method: 'DELETE' });
        if (!response.ok) throw new Error(await responseMessage(response));
        document.getElementById('eventDialog').close();
        selectedSchedule = null;
        await loadSchedules();
    } catch (error) {
        setStatus(`Không xóa được lịch. ${error.message}`, true);
    }
}

function updateOccurrenceFields() {
    const type = document.getElementById('occurrenceType').value;
    document.getElementById('makeupFields').hidden = type !== 'MAKEUP';
    document.getElementById('substituteFields').hidden = type !== 'SUBSTITUTE';
    updateOccurrenceCapacityNotice();
}

async function openOccurrenceEditor() {
    if (!selectedSchedule || !canManageSchedules || !selectedOccurrenceDate) return;
    document.getElementById('exceptionOccurrenceDate').value =
        selectedException?.occurrenceDate || selectedOccurrenceDate;
    await loadExceptionForDate();
    const exception = selectedException;
    const originalDate = exception?.occurrenceDate || document.getElementById('exceptionOccurrenceDate').value;
    document.getElementById('occurrenceSummary').textContent =
        `${eventName(selectedSchedule)} · buổi gốc ${originalDate}`;
    document.getElementById('occurrenceType').value = exception?.exceptionType || 'CANCELLED';
    document.getElementById('replacementDate').value = exception?.replacementDate || '';
    document.getElementById('replacementRoom').value = exception?.roomId || selectedSchedule.roomId || '';
    document.getElementById('substituteLecturer').value = exception?.employeeId || '';
    document.getElementById('occurrenceNote').value = exception?.note || '';
    document.getElementById('occurrenceError').hidden = true;
    document.getElementById('deleteOccurrenceException').hidden = !exception;
    updateOccurrenceFields();
    await updateOccurrenceCapacityNotice();
    occurrenceDialog.showModal();
}

async function loadExceptionForDate() {
    if (!selectedSchedule) return;
    const date = document.getElementById('exceptionOccurrenceDate').value;
    if (!date) return;
    try {
        const response = await fetch(`${API_BASE}/schedule-exceptions?fromDate=${date}&toDate=${date}`);
        if (!response.ok) throw new Error(`Không tải được ngoại lệ (HTTP ${response.status}).`);
        const exceptions = await response.json();
        selectedException = exceptions.find((item) =>
            item.scheduleId === selectedSchedule.id && item.occurrenceDate === date) || null;
        document.getElementById('deleteOccurrenceException').hidden = !selectedException;
        if (selectedException) {
            document.getElementById('occurrenceType').value = selectedException.exceptionType;
            document.getElementById('replacementDate').value = selectedException.replacementDate || '';
            document.getElementById('replacementRoom').value = selectedException.roomId || selectedSchedule.roomId || '';
            document.getElementById('substituteLecturer').value = selectedException.employeeId || '';
            document.getElementById('occurrenceNote').value = selectedException.note || '';
            updateOccurrenceFields();
        } else {
            document.getElementById('occurrenceType').value = 'CANCELLED';
            document.getElementById('replacementDate').value = '';
            document.getElementById('replacementRoom').value = selectedSchedule.roomId || '';
            document.getElementById('substituteLecturer').value = '';
            document.getElementById('occurrenceNote').value = '';
            updateOccurrenceFields();
        }
        document.getElementById('occurrenceSummary').textContent = `${eventName(selectedSchedule)} · buổi gốc ${date}`;
    } catch (error) {
        const errorElement = document.getElementById('occurrenceError');
        errorElement.textContent = error.message;
        errorElement.hidden = false;
    }
}

async function updateOccurrenceCapacityNotice() {
    const notice = document.getElementById('occurrenceCapacityNotice');
    if (document.getElementById('occurrenceType').value !== 'MAKEUP' || !selectedSchedule) {
        notice.hidden = true;
        return;
    }
    try {
        const countResponse = await fetch(
            `${API_BASE}/course-sections/${encodeURIComponent(selectedSchedule.courseSectionId)}/enrollment-count`);
        if (!countResponse.ok) throw new Error(`Không xác định được sĩ số lớp (HTTP ${countResponse.status}).`);
        const count = Number(await countResponse.json());
        const room = editorRooms.find((item) => item.id === document.getElementById('replacementRoom').value);
        notice.textContent = room?.capacity != null && count > room.capacity
            ? `Cảnh báo: ${count} sinh viên vượt sức chứa phòng (${room.capacity}). Khi lưu sẽ cần xác nhận.`
            : `Sĩ số hiện tại: ${count} sinh viên.`;
        notice.hidden = false;
    } catch (error) {
        notice.textContent = error.message;
        notice.hidden = false;
    }
}

async function saveOccurrenceException(event) {
    event.preventDefault();
    const errorElement = document.getElementById('occurrenceError');
    errorElement.hidden = true;
    const type = document.getElementById('occurrenceType').value;
    const payload = {
        occurrenceDate: document.getElementById('exceptionOccurrenceDate').value,
        exceptionType: type,
        replacementDate: type === 'MAKEUP' ? document.getElementById('replacementDate').value || null : null,
        roomId: type === 'MAKEUP' ? document.getElementById('replacementRoom').value || null : null,
        employeeId: type === 'SUBSTITUTE' ? document.getElementById('substituteLecturer').value || null : null,
        note: document.getElementById('occurrenceNote').value.trim() || null
    };
    try {
        if (type === 'MAKEUP') {
            const countResponse = await fetch(`${API_BASE}/course-sections/${encodeURIComponent(selectedSchedule.courseSectionId)}/enrollment-count`);
            if (!countResponse.ok) throw new Error(`Không xác định được sĩ số lớp (HTTP ${countResponse.status}).`);
            const count = Number(await countResponse.json());
            const room = editorRooms.find((item) => item.id === payload.roomId);
            if (room?.capacity != null && count > room.capacity && !window.confirm(
                `Lớp có ${count} sinh viên nhưng phòng ${room.roomCode} chỉ chứa ${room.capacity}. Bạn vẫn muốn lưu?`
            )) return;
        }
        const response = await fetch(`${API_BASE}/schedules/${encodeURIComponent(selectedSchedule.id)}/exceptions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error(await responseMessage(response));
        occurrenceDialog.close();
        eventDialog.close();
        await loadSchedules();
    } catch (error) {
        errorElement.textContent = error.message;
        errorElement.hidden = false;
    }
}

async function deleteOccurrenceException() {
    if (!selectedException) return;
    try {
        const response = await fetch(`${API_BASE}/schedule-exceptions/${encodeURIComponent(selectedException.id)}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error(await responseMessage(response));
        occurrenceDialog.close();
        eventDialog.close();
        await loadSchedules();
    } catch (error) {
        const errorElement = document.getElementById('occurrenceError');
        errorElement.textContent = error.message;
        errorElement.hidden = false;
    }
}

async function responseMessage(response) {
    const body = await response.text();
    const conflictMessage = 'Lịch bị trùng phòng, ngày hoặc tiết với lịch khác. Chưa có thay đổi nào được lưu.';
    if (!body) return response.status === 409 ? conflictMessage : `Máy chủ phản hồi ${response.status}`;
    try {
        const parsed = JSON.parse(body);
        const message = parsed.detail || parsed.message;
        if (response.status === 409 && (!message || message === 'Conflict')) return conflictMessage;
        return message || parsed.error || body;
    } catch {
        return response.status === 409 && body === 'Conflict' ? conflictMessage : body;
    }
}

function updateTitle() {
    if (currentView === 'list') {
        periodTitle.textContent = 'Danh sách lịch học';
        return;
    }
    if (currentView === 'day') {
        periodTitle.textContent = new Intl.DateTimeFormat('vi-VN', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
        }).format(focusedDate);
        return;
    }
    if (currentView === 'week') {
        const start = mondayOf(focusedDate);
        const end = addDays(start, 6);
        if (start.getMonth() === end.getMonth()) {
            periodTitle.textContent = `${start.getDate()} – ${end.getDate()} ${formatMonthYear(end)}`;
        } else {
            periodTitle.textContent = `${start.getDate()} ${new Intl.DateTimeFormat('vi-VN', { month: 'short' }).format(start)} – ${end.getDate()} ${formatMonthYear(end)}`;
        }
    } else {
        periodTitle.textContent = formatMonthYear(focusedDate);
    }
}

function renderMiniCalendar() {
    miniTitle.textContent = formatMonthYear(focusedDate);
    const firstDay = new Date(focusedDate.getFullYear(), focusedDate.getMonth(), 1);
    const firstCell = mondayOf(firstDay);
    miniDays.innerHTML = Array.from({ length: 42 }, (_, index) => {
        const date = addDays(firstCell, index);
        const classes = [
            'mini-day',
            date.getMonth() !== focusedDate.getMonth() ? 'outside' : '',
            sameDate(date, today) ? 'today' : '',
            sameDate(date, focusedDate) ? 'selected' : ''
        ].filter(Boolean).join(' ');
        return `<button type="button" class="${classes}" data-date="${toIsoDate(date)}" aria-label="${escapeHtml(date.toLocaleDateString('vi-VN'))}">${date.getDate()}</button>`;
    }).join('');
    miniDays.querySelectorAll('[data-date]').forEach((button) => {
        button.addEventListener('click', () => navigateTo(parseDate(button.dataset.date)));
    });
}

function renderCalendar() {
    updateTitle();
    renderMiniCalendar();
    if (currentView === 'week' || currentView === 'day') renderWeekView();
    else if (currentView === 'month') renderMonthView();
    else renderScheduleList({ content: schedules, number: listPage, totalPages: totalListPages });
}

function renderWeekView() {
    const weekStart = mondayOf(focusedDate);
    const dates = currentView === 'day'
        ? [focusedDate]
        : Array.from({ length: 7 }, (_, index) => addDays(weekStart, index));
    const headers = dates.map((date) => `
        <div class="week-day-heading ${sameDate(date, today) ? 'today' : ''}">
            <span class="weekday">${DAYS[(date.getDay() + 6) % 7]}</span>
            <span class="day-number">${date.getDate()}</span>
        </div>
    `).join('');
    const events = visibleEvents();
    const periodCount = Math.max(
        DEFAULT_PERIOD_COUNT,
        ...events.map((schedule) => Number(schedule.endPeriod || schedule.startPeriod || 0))
    );
    const labels = Array.from({ length: periodCount }, (_, index) =>
        `<div class="period-label">Tiết ${index + 1}</div>`).join('');
    const columns = dates.map((date, columnIndex) => {
        const dayEvents = occurrenceEvents(date, events)
            .filter((schedule) => Number(schedule.startPeriod) >= 1);
        return `<div class="day-column ${sameDate(date, today) ? 'today' : ''}" data-column="${columnIndex + 1}" style="height:${periodCount * 66}px">${renderOverlappingEvents(dayEvents, date)}</div>`;
    }).join('');

    calendarView.innerHTML = `
        <div class="week-calendar ${currentView === 'day' ? 'day-calendar' : ''}">
            <div class="week-header"><div class="week-corner"></div>${headers}</div>
            <div class="week-grid"><div class="period-labels">${labels}</div>${columns}</div>
        </div>`;
    calendarView.querySelectorAll('[data-schedule-id]').forEach((button) => {
        button.addEventListener('click', () => showDetails(button.dataset.scheduleId, button.dataset.occurrence));
    });
}

function renderOverlappingEvents(events, date) {
    const ordered = [...events].sort((first, second) =>
        Number(first.startPeriod) - Number(second.startPeriod)
        || Number(first.endPeriod || first.startPeriod) - Number(second.endPeriod || second.startPeriod));
    const rendered = [];
    let cluster = [];
    let clusterEnd = 0;

    const flushCluster = () => {
        const laneEnds = [];
        const lanes = cluster.map((schedule) => {
            const start = Math.max(1, Number(schedule.startPeriod));
            const end = Math.max(start, Number(schedule.endPeriod || start));
            let lane = laneEnds.findIndex((laneEnd) => laneEnd < start);
            if (lane === -1) lane = laneEnds.length;
            laneEnds[lane] = end;
            return { schedule, lane };
        });
        const laneCount = laneEnds.length;
        lanes.forEach(({ schedule, lane }) => {
            rendered.push(renderWeekEvent(schedule, date, lane, laneCount));
        });
        cluster = [];
        clusterEnd = 0;
    };

    ordered.forEach((schedule) => {
        const start = Math.max(1, Number(schedule.startPeriod));
        const end = Math.max(start, Number(schedule.endPeriod || start));
        if (cluster.length && start > clusterEnd) flushCluster();
        cluster.push(schedule);
        clusterEnd = Math.max(clusterEnd, end);
    });
    if (cluster.length) flushCluster();
    return rendered.join('');
}

function occursOn(schedule, date) {
    const start = parseDate(schedule.startDate);
    const end = parseDate(schedule.endDate);
    return Boolean(start && end && date >= start && date <= end);
}

function occurrenceEvents(date, candidates = visibleEvents()) {
    const dateValue = toIsoDate(date);
    const dayNumber = (date.getDay() + 6) % 7 + 1;
    const events = [];
    candidates.filter((schedule) => Number(schedule.dayOfWeek) === dayNumber && occursOn(schedule, date))
        .forEach((schedule) => {
            const exception = scheduleExceptions.find((item) =>
                item.scheduleId === schedule.id && item.occurrenceDate === dateValue);
            if (exception?.exceptionType === 'CANCELLED' || exception?.exceptionType === 'MAKEUP') return;
            if (exception?.exceptionType === 'SUBSTITUTE') {
                const lecturer = lecturerOptions.find((item) => item.employeeId === exception.employeeId);
                events.push({
                    ...schedule,
                    employeeId: exception.employeeId,
                    lecturerName: lecturer?.fullName || schedule.lecturerName,
                    exceptionType: exception.exceptionType
                });
            } else {
                events.push(schedule);
            }
        });
    scheduleExceptions.filter((item) =>
        item.exceptionType === 'MAKEUP' && item.replacementDate === dateValue
            && candidates.some((schedule) => schedule.id === item.scheduleId))
        .forEach((exception) => {
            const schedule = exception.schedule;
            const lecturer = lecturerOptions.find((item) => item.employeeId === exception.employeeId);
            const room = editorRooms.find((item) => item.id === exception.roomId);
            events.push({
                ...schedule,
                dayOfWeek: dayNumber,
                startDate: `${dateValue}T00:00:00`,
                endDate: `${dateValue}T23:59:59`,
                roomId: exception.roomId || schedule.roomId,
                roomCode: room?.roomCode || schedule.roomCode,
                roomName: room?.roomName || schedule.roomName,
                startPeriod: exception.startPeriod || schedule.startPeriod,
                endPeriod: exception.endPeriod || schedule.endPeriod,
                employeeId: exception.employeeId || schedule.employeeId,
                lecturerName: lecturer?.fullName || schedule.lecturerName,
                exceptionType: exception.exceptionType,
                originalOccurrenceDate: exception.occurrenceDate
            });
        });
    return events;
}

function renderWeekEvent(schedule, date, lane, laneCount) {
    const start = Math.max(1, Number(schedule.startPeriod));
    const end = Math.max(start, Number(schedule.endPeriod || start));
    const top = (start - 1) * 66 + 3;
    const height = (end - start + 1) * 66 - 6;
    const laneWidth = 100 / laneCount;
    const laneInset = laneWidth > 6 ? 3 : 0;
    const compactLane = laneWidth > 6 ? '' : ' compact-lane';
    const time = `Tiết ${start}${end > start ? `–${end}` : ''}`;
    const location = [schedule.roomCode || schedule.roomName, schedule.buildingName].filter(Boolean).join(' · ');
    const mode = modeLabel(schedule);
    const occurrence = toIsoDate(date);
    return `
        <button type="button" class="day-event${compactLane} ${colorClass(schedule)}" data-schedule-id="${escapeHtml(schedule.id)}" data-occurrence="${occurrence}"
            style="top:${top}px;height:${height}px;left:calc(${laneWidth * lane}% + ${laneInset}px);width:calc(${laneWidth}% - ${laneInset * 2}px)" title="${escapeHtml(eventName(schedule))}">
            <span class="event-name">${escapeHtml(eventName(schedule))}</span>
            ${mode ? `<span class="event-mode">${escapeHtml(mode)}</span>` : ''}
            ${schedule.lecturerName ? `<span class="event-meta event-lecturer">Giảng viên: ${escapeHtml(schedule.lecturerName)}</span>` : ''}
            <span class="event-meta">${time}${location ? ` · ${escapeHtml(location)}` : ''}</span>
        </button>`;
}

function monthRangeStart(date) {
    return mondayOf(new Date(date.getFullYear(), date.getMonth(), 1));
}

function renderMonthView() {
    const firstCell = monthRangeStart(focusedDate);
    const dates = Array.from({ length: 42 }, (_, index) => addDays(firstCell, index));
    const events = visibleEvents();
    const head = DAYS.map((day) => `<div>${day}</div>`).join('');
    const cells = dates.map((date) => {
        const dayEvents = occurrenceEvents(date, events);
        const shown = dayEvents.slice(0, 3).map((schedule) => `
            <button type="button" class="month-event ${colorClass(schedule)}" data-schedule-id="${escapeHtml(schedule.id)}" data-occurrence="${toIsoDate(date)}"
                title="${escapeHtml([eventName(schedule), schedule.lecturerName ? `Giảng viên: ${schedule.lecturerName}` : ''].filter(Boolean).join(' · '))}">
                <span class="month-event-name">${escapeHtml(eventName(schedule))}</span>
                ${modeLabel(schedule) ? `<span class="month-mode">${escapeHtml(modeLabel(schedule))}</span>` : ''}
            </button>`).join('');
        const more = dayEvents.length > 3 ? `<div class="month-more">+${dayEvents.length - 3} lịch khác</div>` : '';
        return `<div class="month-cell ${date.getMonth() === focusedDate.getMonth() ? '' : 'outside'}">
            <span class="month-date ${sameDate(date, today) ? 'today' : ''}">${date.getDate()}</span>${shown}${more}
        </div>`;
    }).join('');
    calendarView.innerHTML = `<div class="month-calendar"><div class="month-head">${head}</div><div class="month-grid">${cells}</div></div>`;
    calendarView.querySelectorAll('[data-schedule-id]').forEach((button) => {
        button.addEventListener('click', () => showDetails(button.dataset.scheduleId, button.dataset.occurrence));
    });
}

function shiftPeriod(direction) {
    if (currentView === 'week') focusedDate = addDays(focusedDate, direction * 7);
    else if (currentView === 'day') focusedDate = addDays(focusedDate, direction);
    else focusedDate = new Date(focusedDate.getFullYear(), focusedDate.getMonth() + direction, 1);
    loadSchedules();
}

function shiftMiniMonth(direction) {
    focusedDate = new Date(focusedDate.getFullYear(), focusedDate.getMonth() + direction, Math.min(focusedDate.getDate(), 28));
    loadSchedules();
}

function navigateTo(date) {
    focusedDate = startOfDay(date);
    loadSchedules();
}

function showDetails(id, occurrenceValue) {
    const occurrence = parseDate(occurrenceValue) || focusedDate;
    const schedule = occurrenceEvents(occurrence).find((item) => String(item.id) === String(id));
    if (!schedule) return;
    selectedSchedule = schedules.find((item) => String(item.id) === String(id)) || schedule;
    selectedOccurrenceDate = toIsoDate(occurrence);
    selectedException = scheduleExceptions.find((item) =>
        item.scheduleId === schedule.id
        && (item.occurrenceDate === selectedOccurrenceDate || item.replacementDate === selectedOccurrenceDate)) || null;
    scheduleActions.hidden = !canManageSchedules;
    const dateText = new Intl.DateTimeFormat('vi-VN', {
        weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    }).format(occurrence);
    const start = Number(schedule.startPeriod);
    const end = Number(schedule.endPeriod || start);
    const periodText = `Tiết ${start}${end > start ? `–${end}` : ''}`;
    const roomText = [schedule.roomCode, schedule.roomName, schedule.buildingName].filter(Boolean).join(' · ') || 'Chưa có thông tin phòng';
    document.getElementById('dialogCourse').textContent = eventName(schedule);
    document.getElementById('dialogSection').textContent = [schedule.courseCode, schedule.courseSectionName, schedule.courseSectionCode].filter(Boolean).join(' · ');
    document.getElementById('dialogDate').textContent = dateText;
    document.getElementById('dialogPeriod').textContent = periodText;
    const lecturerRow = document.getElementById('dialogLecturerRow');
    lecturerRow.hidden = !schedule.lecturerName;
    document.getElementById('dialogLecturer').textContent = schedule.lecturerName || '';
    const mode = modeLabel(schedule);
    document.getElementById('dialogModeRow').hidden = !mode;
    document.getElementById('dialogMode').textContent = mode;
    document.getElementById('dialogRoom').textContent = roomText;
    const status = schedule.scheduleStatus || schedule.status || '';
    const displayStatus = schedule.exceptionType
        ? ({ CANCELLED: 'Nghỉ học', MAKEUP: 'Học bù', SUBSTITUTE: 'Giảng viên dạy thay' }[schedule.exceptionType])
        : status;
    document.getElementById('dialogStatusRow').hidden = !displayStatus;
    document.getElementById('dialogStatus').textContent = displayStatus;
    const description = document.getElementById('dialogDescription');
    description.textContent = schedule.description || schedule.note || '';
    description.hidden = !description.textContent;
    document.querySelector('.dialog-color-bar').style.backgroundColor = colorHex(schedule);
    if (typeof eventDialog.showModal === 'function') eventDialog.showModal();
}

function colorHex(schedule) {
    const colors = ['#039be5', '#188038', '#d56a00', '#9334e6', '#d93025'];
    const match = colorClass(schedule).match(/\d/);
    return colors[Number(match?.[0] || 0)];
}

async function exportSchedules() {
    const params = filterQueryParams();
    try {
        const response = await fetch(`${API_BASE}/schedules/export?${params}`);
        if (!response.ok) throw new Error(await responseMessage(response));
        const blob = await response.blob();
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'schedules.xlsx';
        link.click();
        URL.revokeObjectURL(link.href);
    } catch (error) {
        setStatus(`Không xuất được Excel. ${error.message}`, true);
    }
}

loadFilterOptions().then(loadSchedules).catch((error) => setStatus(error.message, true));
