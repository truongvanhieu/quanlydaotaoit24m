const API_BASE = '/api';
const DAYS = ['Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy', 'Chủ nhật'];
const DEFAULT_PERIOD_COUNT = 12;
const today = startOfDay(new Date());
let focusedDate = new Date(today);
let currentView = 'week';
let schedules = [];
let requestId = 0;

const calendarView = document.getElementById('calendarView');
const statusElement = document.getElementById('calendarStatus');
const periodTitle = document.getElementById('periodTitle');
const miniTitle = document.getElementById('miniMonthTitle');
const miniDays = document.getElementById('miniDays');
const searchInput = document.getElementById('eventSearch');
const eventDialog = document.getElementById('eventDialog');

document.getElementById('todayButton').addEventListener('click', () => navigateTo(today));
document.getElementById('prevPeriod').addEventListener('click', () => shiftPeriod(-1));
document.getElementById('nextPeriod').addEventListener('click', () => shiftPeriod(1));
document.getElementById('miniPrev').addEventListener('click', () => shiftMiniMonth(-1));
document.getElementById('miniNext').addEventListener('click', () => shiftMiniMonth(1));
searchInput.addEventListener('input', renderCalendar);
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

function eventSearchText(schedule) {
    return [
        schedule.courseName, schedule.courseCode, schedule.courseSectionName,
        schedule.courseSectionCode, schedule.roomName, schedule.roomCode,
        schedule.buildingName
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
    return schedules.filter((schedule) => !query || eventSearchText(schedule).includes(query));
}

function setStatus(message = '', isError = false) {
    statusElement.textContent = message;
    statusElement.hidden = !message;
    statusElement.classList.toggle('error', isError);
}

async function loadSchedules() {
    const currentRequest = ++requestId;
    const dates = currentView === 'week'
        ? [mondayOf(focusedDate)]
        : Array.from({ length: 6 }, (_, index) => addDays(monthRangeStart(focusedDate), index * 7));
    setStatus('Đang tải lịch học…');
    calendarView.innerHTML = '';

    try {
        const weeks = await Promise.all(dates.map(async (date) => {
            const url = `${API_BASE}/schedules/weekly?date=${encodeURIComponent(toIsoDate(date))}`;
            const response = await fetch(url, { headers: { Accept: 'application/json' } });
            if (!response.ok) throw new Error(`Máy chủ phản hồi ${response.status}`);
            return response.json();
        }));
        if (currentRequest !== requestId) return;
        if (weeks.some((week) => !Array.isArray(week))) throw new Error('Dữ liệu lịch học trả về không đúng định dạng');
        schedules = [...new Map(weeks.flat().map((schedule) => [schedule.id, schedule])).values()];
        setStatus();
        renderCalendar();
    } catch (error) {
        if (currentRequest !== requestId) return;
        schedules = [];
        renderCalendar();
        setStatus(`Không thể tải lịch học. ${error.message}`, true);
    }
}

function updateTitle() {
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
    if (currentView === 'week') renderWeekView();
    else renderMonthView();
}

function renderWeekView() {
    const weekStart = mondayOf(focusedDate);
    const dates = Array.from({ length: 7 }, (_, index) => addDays(weekStart, index));
    const headers = dates.map((date, index) => `
        <div class="week-day-heading ${sameDate(date, today) ? 'today' : ''}">
            <span class="weekday">${DAYS[index]}</span>
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
        const dayNumber = columnIndex + 1;
        const dayEvents = events
            .filter((schedule) => Number(schedule.dayOfWeek) === dayNumber && occursOn(schedule, date))
            .filter((schedule) => Number(schedule.startPeriod) >= 1)
            .map((schedule) => renderWeekEvent(schedule, date));
        return `<div class="day-column ${sameDate(date, today) ? 'today' : ''}" data-column="${columnIndex + 1}" style="height:${periodCount * 66}px">${dayEvents.join('')}</div>`;
    }).join('');

    calendarView.innerHTML = `
        <div class="week-calendar">
            <div class="week-header"><div class="week-corner"></div>${headers}</div>
            <div class="week-grid"><div class="period-labels">${labels}</div>${columns}</div>
        </div>`;
    calendarView.querySelectorAll('[data-schedule-id]').forEach((button) => {
        button.addEventListener('click', () => showDetails(button.dataset.scheduleId, button.dataset.occurrence));
    });
}

function occursOn(schedule, date) {
    const start = parseDate(schedule.startDate);
    const end = parseDate(schedule.endDate);
    return Boolean(start && end && date >= start && date <= end);
}

function renderWeekEvent(schedule, date) {
    const start = Math.max(1, Number(schedule.startPeriod));
    const end = Math.max(start, Number(schedule.endPeriod || start));
    const top = (start - 1) * 66 + 3;
    const height = (end - start + 1) * 66 - 6;
    const time = `Tiết ${start}${end > start ? `–${end}` : ''}`;
    const location = [schedule.roomCode || schedule.roomName, schedule.buildingName].filter(Boolean).join(' · ');
    const occurrence = toIsoDate(date);
    return `
        <button type="button" class="day-event ${colorClass(schedule)}" data-schedule-id="${escapeHtml(schedule.id)}" data-occurrence="${occurrence}"
            style="top:${top}px;height:${height}px" title="${escapeHtml(eventName(schedule))}">
            <span class="event-name">${escapeHtml(eventName(schedule))}</span>
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
        const dayNumber = (date.getDay() + 6) % 7 + 1;
        const dayEvents = events
            .filter((schedule) => Number(schedule.dayOfWeek) === dayNumber && occursOn(schedule, date));
        const shown = dayEvents.slice(0, 3).map((schedule) => `
            <button type="button" class="month-event ${colorClass(schedule)}" data-schedule-id="${escapeHtml(schedule.id)}" data-occurrence="${toIsoDate(date)}">
                ${escapeHtml(eventName(schedule))}
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
    const schedule = schedules.find((item) => String(item.id) === String(id));
    if (!schedule) return;
    const occurrence = parseDate(occurrenceValue) || focusedDate;
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
    document.getElementById('dialogRoom').textContent = roomText;
    const status = schedule.scheduleStatus || schedule.status || '';
    document.getElementById('dialogStatusRow').hidden = !status;
    document.getElementById('dialogStatus').textContent = status;
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

loadSchedules();
