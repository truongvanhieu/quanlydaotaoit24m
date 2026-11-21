'use strict';

// =====================================================
// 1. API BACKEND
// =====================================================
const API = {
    students: '/api/registration-options/students',
    classes: '/api/registration-options/course-classes',
    classSummary: '/api/registration-options/course-classes/summary',
    periods: '/api/registration-periods',
    registrations: '/api/course-registrations',
    registrationDetails: '/api/course-registrations/details/search',
    studentHistory: '/api/course-registrations/student-history'
};

const CLASS_PREVIEW_LIMIT = 6;
const REG_PREVIEW_LIMIT = 8;

// =====================================================
// 2. DU LIEU & TRANG THAI HIEN THI
// =====================================================
let students = [];
let classes = [];
let availableClasses = [];
let classSummaryRows = [];
let periods = [];
let registrations = [];

let showAllClasses = false;
let showAllRegistrations = false;
let currentViewingClassId = null;
let currentViewingClassCode = '';

const $ = id => document.getElementById(id);

// =====================================================
// 3. HELPER CHUNG
// =====================================================
async function apiRequest(url, options = {}, timeoutMs = 15000) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
        return await N6.request(url, { ...options, signal: controller.signal });
    } catch (error) {
        if (error.name === 'AbortError') {
            throw new Error(`API quá thời gian chờ ${timeoutMs / 1000} giây: ${url}`);
        }
        throw error;
    } finally {
        clearTimeout(timer);
    }
}

function asArray(value, name) {
    if (!Array.isArray(value)) {
        throw new Error(`API ${name} không trả về danh sách JSON hợp lệ.`);
    }
    return value;
}

function showMessage(message, type = 'info') {
    N6.showMessage(message, type);
}

function escapeHtml(value) {
    return N6.esc(value ?? '');
}

function fillSelect(id, rows, placeholder, labelFn, preserveOld = true) {
    const select = $(id);
    if (!select) return;

    const oldValue = select.value;
    select.replaceChildren();

    const first = document.createElement('option');
    first.value = '';
    first.textContent = placeholder;
    select.appendChild(first);

    rows.forEach(row => {
        const option = document.createElement('option');
        option.value = row.id;
        option.textContent = labelFn(row);
        select.appendChild(option);
    });

    if (preserveOld && [...select.options].some(option => option.value === oldValue)) {
        select.value = oldValue;
    }
}

function studentLabel(row) {
    const code = row.code || row.studentCode || '';
    const name = row.fullName || row.name || '';
    return `${code} — ${name}`.replace(/^ — /, '') || row.id;
}

function classLabel(row) {
    const code = row.code || row.classCode || row.id;
    const courseCode = row.courseCode || '';
    const courseName = row.courseName || '';
    const credits = row.credits ?? '?';
    return `${code} — ${courseCode} ${courseName} (${credits} tín chỉ)`;
}

function getPeriodStatus(period) {
    const active = period.active === true || period.active === 1 || period.active === 'true';
    if (!active) return 'Đã tắt';

    const start = period.startTime ? new Date(period.startTime) : null;
    const end = period.endTime ? new Date(period.endTime) : null;
    const now = new Date();

    if (!start || Number.isNaN(start.getTime()) || !end || Number.isNaN(end.getTime())) {
        return 'Thiếu thời gian';
    }
    if (now < start) return 'Chưa bắt đầu';
    if (now >= end) return 'Đã hết hạn';
    return 'Đang mở';
}

// =====================================================
// 4. LOC NHANH SINH VIEN & LOP + TOM TAT TIN CHI TU DONG
// =====================================================
function renderStudentSelect(filterText = '') {
    const kw = filterText.trim().toLowerCase();
    const filtered = !kw
        ? students
        : students.filter(s => studentLabel(s).toLowerCase().includes(kw));

    fillSelect(
        'studentId',
        filtered,
        filtered.length ? `Chọn sinh viên (${filtered.length})` : 'Không khớp sinh viên nào',
        studentLabel
    );

    if (kw && filtered.length === 1) {
        $('studentId').value = filtered[0].id;
        refreshQuickSummary();
        suggestRegistrationType();
    }
}

function renderClassSelect(filterText = '') {
    const kw = filterText.trim().toLowerCase();
    const filtered = !kw
        ? availableClasses
        : availableClasses.filter(c => classLabel(c).toLowerCase().includes(kw));

    fillSelect(
        'courseClassId',
        filtered,
        filtered.length ? `Chọn lớp học phần (${filtered.length})` : 'Không có lớp học phần phù hợp',
        classLabel
    );

    if (kw && filtered.length === 1) {
        $('courseClassId').value = filtered[0].id;
        suggestRegistrationType();
    }
}

async function refreshQuickSummary() {
    const bar = $('quickSummaryBar');
    const studentId = $('studentId')?.value;
    const registrationPeriodId = $('registrationPeriodId')?.value;
    if (!bar) return;

    if (!studentId || !registrationPeriodId) {
        bar.style.display = 'none';
        return;
    }

    try {
        const query = new URLSearchParams({ studentId, registrationPeriodId });
        const summary = await apiRequest(`${API.registrations}/summary?${query}`);
        bar.style.display = 'flex';
        bar.innerHTML = `
            <span><i class="bi bi-person-check"></i> Đã đăng ký: <strong>${escapeHtml(summary.registrationCount)} môn</strong></span>
            <span>Tín chỉ đã chọn: <strong>${escapeHtml(summary.selectedCredits)} / ${escapeHtml(summary.maxCredits)} TC</strong> (tối thiểu ${escapeHtml(summary.minCredits)} TC)</span>
            <span>Còn lại: <strong>${escapeHtml(summary.creditsRemaining)} TC</strong></span>
            <span class="badge ${summary.minimumReached ? 'good' : ''}">
                ${summary.minimumReached ? 'Đạt tối thiểu' : 'Chưa đạt tối thiểu'}
            </span>
        `;
    } catch (_) {
        bar.style.display = 'none';
    }
}

// =====================================================
// 5. TU GOI Y LOAI DANG KY (PHAN BIET CUNG KY / KY TRUOC)
// =====================================================
async function suggestRegistrationType() {
    const studentId = $('studentId')?.value;
    const classId = $('courseClassId')?.value;
    const typeSelect = $('registrationType');
    const hintEl = $('registerTypeHint');

    if (!hintEl || !typeSelect) return;
    if (!studentId || !classId) {
        hintEl.textContent = 'Hệ thống tự gợi ý Học lại nếu sinh viên đã học môn này ở kỳ trước.';
        return;
    }

    const cls = availableClasses.find(c => String(c.id) === String(classId))
        || classes.find(c => String(c.id) === String(classId));
    if (!cls || !cls.courseId) return;

    try {
        const history = asArray(
            await apiRequest(`${API.studentHistory}?studentId=${encodeURIComponent(studentId)}`),
            'lịch sử môn học'
        );

        const sameSemester = history.find(
            h => String(h.courseId) === String(cls.courseId)
                && cls.semesterId
                && String(h.semesterId) === String(cls.semesterId)
        );
        if (sameSemester) {
            hintEl.textContent = `Cảnh báo: Sinh viên đã đăng ký môn ${sameSemester.courseCode || ''} trong học kỳ này rồi (lớp ${sameSemester.classCode || ''}).`;
            return;
        }

        const prevSemester = history.find(
            h => String(h.courseId) === String(cls.courseId)
                && (!cls.semesterId || String(h.semesterId) !== String(cls.semesterId))
        );
        if (prevSemester) {
            if (Number(typeSelect.value) === 1) {
                typeSelect.value = '2';
            }
            hintEl.textContent = `Gợi ý: Sinh viên đã học môn ${prevSemester.courseCode || ''} ở ${prevSemester.semesterName || 'kỳ trước'} → đã chọn 2 - Học lại (có thể đổi sang 3 - Cải thiện).`;
        } else {
            typeSelect.value = '1';
            hintEl.textContent = 'Gợi ý: Sinh viên chưa có lịch sử môn này → chọn 1 - Học mới.';
        }
    } catch (_) {
        hintEl.textContent = 'Hệ thống tự gợi ý Học lại nếu sinh viên đã học môn này ở kỳ trước.';
    }
}

// =====================================================
// 6. TAI DU LIEU CO BAN
// =====================================================
function renderPeriods() {
    fillSelect(
        'registrationPeriodId',
        periods,
        periods.length ? 'Chọn đợt đăng ký' : 'Chưa có đợt đăng ký trong database',
        row => `${row.name || row.id} — ${getPeriodStatus(row)}`
    );

    fillSelect('regFilterPeriod', periods, 'Tất cả đợt đăng ký', row => row.name || row.id);

    if (!$('registrationPeriodId').value) {
        const openedPeriod = periods.find(row => getPeriodStatus(row) === 'Đang mở');
        if (openedPeriod) {
            $('registrationPeriodId').value = openedPeriod.id;
        }
    }

    const hint = $('periodHint');
    if (!hint) return;
    if (periods.length === 0) {
        hint.textContent = 'Database chưa có đợt đăng ký. Hãy tạo đợt tại trang Đợt đăng ký.';
    } else {
        const opened = periods.filter(row => getPeriodStatus(row) === 'Đang mở').length;
        hint.textContent = `Đã tải ${periods.length} đợt; ${opened} đợt đang mở.`;
    }
}

async function loadStudents() {
    students = asArray(await apiRequest(API.students), 'sinh viên');
    renderStudentSelect($('studentQuickSearch')?.value || '');
}

async function loadClasses(semesterId = null) {
    const url = semesterId
        ? `${API.classes}?semesterId=${encodeURIComponent(semesterId)}`
        : API.classes;

    const rows = asArray(await apiRequest(url), 'lớp học phần');
    if (semesterId) {
        availableClasses = rows;
    } else {
        classes = rows;
        availableClasses = rows;
    }
    renderClassSelect($('classQuickSearch')?.value || '');
    return availableClasses;
}

async function loadPeriods(showFeedback = false) {
    periods = asArray(await apiRequest(API.periods), 'đợt đăng ký');
    renderPeriods();
    if (showFeedback) {
        showMessage(
            periods.length ? `Đã tải ${periods.length} đợt đăng ký.` : 'Chưa có đợt đăng ký nào.',
            periods.length ? 'success' : 'info'
        );
    }
    return periods;
}

async function filterClassesForPeriod() {
    const periodId = $('registrationPeriodId').value;
    if (!periodId) {
        await loadClasses();
        return;
    }
    const period = periods.find(row => String(row.id) === String(periodId));
    if (!period || !period.semesterId) {
        await loadClasses();
        return;
    }
    await loadClasses(period.semesterId);
}

// =====================================================
// 7. DANH SACH LOP HOC PHAN (CO THU GON / MO RONG) & POPUP DSSV
// =====================================================
async function loadClassSummary() {
    const keyword = ($('classListKeyword')?.value || '').trim();
    const onlyCurrent = Boolean($('classListOnlyCurrentPeriod')?.checked);
    const params = new URLSearchParams();

    if (onlyCurrent) {
        const periodId = $('registrationPeriodId')?.value;
        const period = periods.find(p => String(p.id) === String(periodId));
        if (period?.semesterId) params.set('semesterId', period.semesterId);
    }
    if (keyword) params.set('keyword', keyword);

    const url = params.toString() ? `${API.classSummary}?${params}` : API.classSummary;
    classSummaryRows = asArray(await apiRequest(url), 'danh sách lớp học phần');
    renderClassSummaryTable();
}

function renderClassSummaryTable() {
    const container = $('classSummaryTable');
    const footer = $('classTableFooter');
    if (!container) return;

    const onlyHasStudents = Boolean($('classListOnlyHasStudents')?.checked);
    const filtered = onlyHasStudents
        ? classSummaryRows.filter(cls => Number(cls.registeredCount ?? 0) > 0)
        : classSummaryRows;

    if (!filtered.length) {
        container.innerHTML = '<p class="muted">Không tìm thấy lớp học phần nào phù hợp.</p>';
        if (footer) footer.style.display = 'none';
        return;
    }

    const visibleRows = showAllClasses ? filtered : filtered.slice(0, CLASS_PREVIEW_LIMIT);

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Mã lớp</th>
                    <th>Môn học</th>
                    <th>Học kỳ</th>
                    <th>Tín chỉ</th>
                    <th>Sĩ số</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                ${visibleRows.map(cls => {
                    const count = Number(cls.registeredCount ?? 0);
                    const max = cls.maxStudents ? Number(cls.maxStudents) : null;
                    const isFull = max !== null && max > 0 && count >= max;
                    return `
                    <tr>
                        <td><strong>${escapeHtml(cls.code)}</strong></td>
                        <td><strong>${escapeHtml(cls.courseCode || '')}</strong> — ${escapeHtml(cls.courseName || '')}</td>
                        <td>${escapeHtml(cls.semesterName || cls.semesterCode || '—')}</td>
                        <td>${escapeHtml(cls.credits ?? '—')}</td>
                        <td>
                            <span class="badge ${isFull ? 'off' : 'good'}">
                                ${escapeHtml(count)}${max ? ` / ${escapeHtml(max)}` : ''}
                            </span>
                        </td>
                        <td>
                            <div class="actions" style="margin-top:0">
                                <button type="button" class="small secondary"
                                        data-view-students="${escapeHtml(cls.id)}"
                                        data-class-code="${escapeHtml(cls.code)}">
                                    <i class="bi bi-people"></i> Xem DSSV (${escapeHtml(count)})
                                </button>
                                <button type="button" class="small"
                                        data-pick-class="${escapeHtml(cls.id)}"
                                        data-pick-semester="${escapeHtml(cls.semesterId || '')}">
                                    <i class="bi bi-plus-circle"></i> Chọn ĐK
                                </button>
                            </div>
                        </td>
                    </tr>`;
                }).join('')}
            </tbody>
        </table>
    `;

    if (footer) {
        if (filtered.length > CLASS_PREVIEW_LIMIT) {
            footer.style.display = 'flex';
            footer.innerHTML = `
                <span class="muted">Đang hiển thị <strong>${visibleRows.length}</strong> / <strong>${filtered.length}</strong> lớp học phần.</span>
                <button type="button" id="toggleClassLimitBtn" class="secondary small">
                    <i class="bi ${showAllClasses ? 'bi-chevron-up' : 'bi-chevron-down'}"></i>
                    ${showAllClasses ? `Thu gọn còn ${CLASS_PREVIEW_LIMIT} lớp` : `Xem tất cả (${filtered.length} lớp)`}
                </button>
            `;
            $('toggleClassLimitBtn').addEventListener('click', () => {
                showAllClasses = !showAllClasses;
                renderClassSummaryTable();
            });
        } else {
            footer.style.display = 'flex';
            footer.innerHTML = `<span class="muted">Tổng cộng <strong>${filtered.length}</strong> lớp học phần.</span>`;
        }
    }
}

async function openStudentsOfClass(classId, classCode, keyword = '') {
    currentViewingClassId = classId;
    currentViewingClassCode = classCode;

    const modal = $('classStudentsModal');
    const title = $('classStudentDetailTitle');
    const body = $('classStudentDetailBody');
    if (!modal || !body) return;

    modal.classList.add('open');
    if (title) title.textContent = `Danh sách sinh viên lớp ${classCode || ''}`;
    body.innerHTML = '<p class="muted">Đang tải danh sách sinh viên...</p>';

    try {
        const params = new URLSearchParams();
        if (keyword.trim()) params.set('keyword', keyword.trim());
        const url = `/api/registration-options/course-classes/${encodeURIComponent(classId)}/students${params.toString() ? `?${params}` : ''}`;
        const list = asArray(await apiRequest(url), 'danh sách sinh viên của lớp');

        if (!list.length) {
            body.innerHTML = '<p class="muted">Lớp chưa có sinh viên đăng ký hoặc không khớp từ khóa tìm kiếm.</p>';
            return;
        }

        body.innerHTML = `
            <p class="muted" style="margin-top:0">Tìm thấy <strong>${list.length}</strong> sinh viên trong lớp.</p>
            <table>
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>MSSV</th>
                        <th>Họ và tên</th>
                        <th>Lớp sinh hoạt</th>
                        <th>Loại ĐK</th>
                        <th>Trạng thái</th>
                        <th>Thanh toán</th>
                        <th>Thời gian ĐK</th>
                    </tr>
                </thead>
                <tbody>
                    ${list.map((sv, idx) => `
                        <tr>
                            <td>${idx + 1}</td>
                            <td><strong>${escapeHtml(sv.studentCode || '—')}</strong></td>
                            <td>${escapeHtml(sv.studentName || '—')}</td>
                            <td>${escapeHtml(sv.studentClassCode || sv.studentClassName || '—')}</td>
                            <td>${escapeHtml(sv.registrationTypeName || N6.typeText(sv.registrationType))}</td>
                            <td><span class="badge good">${escapeHtml(sv.statusName || N6.statusText(sv.status))}</span></td>
                            <td>${sv.paid ? 'Đã thanh toán' : 'Chưa thanh toán'}</td>
                            <td>${escapeHtml(N6.dateTimeText(sv.registeredAt))}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        body.innerHTML = `<p class="muted">Không tải được danh sách sinh viên: ${escapeHtml(error.message)}</p>`;
    }
}

function closeStudentsModal() {
    $('classStudentsModal')?.classList.remove('open');
    currentViewingClassId = null;
}

// =====================================================
// 8. DANH SACH DANG KY HOC PHAN (CO THU GON / MO RONG)
// =====================================================
async function loadRegistrations() {
    const keyword = ($('regFilterKeyword')?.value || '').trim();
    const periodId = $('regFilterPeriod')?.value || '';
    const status = $('regFilterStatus')?.value || '';

    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (periodId) params.set('registrationPeriodId', periodId);
    if (status !== '') params.set('status', status);

    const url = params.toString()
        ? `${API.registrationDetails}?${params}`
        : API.registrationDetails;

    registrations = asArray(await apiRequest(url), 'chi tiết đăng ký học phần');
    renderRegistrations();
}

function renderRegistrations() {
    const container = $('registrationTable');
    const footer = $('regTableFooter');
    if (!registrations.length) {
        container.innerHTML = '<p class="muted">Không có đăng ký học phần nào phù hợp.</p>';
        if (footer) footer.style.display = 'none';
        return;
    }

    const studentMap = new Map(students.map(row => [String(row.id), studentLabel(row)]));
    const classMap = new Map(classes.map(row => [String(row.id), classLabel(row)]));
    const periodMap = new Map(periods.map(row => [String(row.id), row.name || row.id]));
    const visibleRows = showAllRegistrations ? registrations : registrations.slice(0, REG_PREVIEW_LIMIT);

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Sinh viên</th>
                    <th>Lớp học phần &amp; Môn học</th>
                    <th>Đợt đăng ký</th>
                    <th>Loại</th>
                    <th>Trạng thái</th>
                    <th>Thời gian</th>
                    <th>Thanh toán</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                ${visibleRows.map(row => {
                    const regId = row.id || row.registrationId;
                    const clsCode = row.classCode || row.courseClassCode;
                    const perName = row.periodName || row.registrationPeriodName;
                    return `
                    <tr>
                        <td>
                            ${escapeHtml(
                                (row.studentName
                                    ? `${row.studentCode || ''} — ${row.studentName}`.replace(/^ — /, '')
                                    : studentMap.get(String(row.studentId)))
                                || row.studentId
                            )}
                        </td>
                        <td>
                            ${escapeHtml(
                                (clsCode
                                    ? `${clsCode} — ${row.courseCode || ''} ${row.courseName || ''} (${row.credits ?? '?'} tín chỉ)`
                                    : classMap.get(String(row.courseClassId)))
                                || row.courseClassId
                            )}
                        </td>
                        <td>${escapeHtml(perName || periodMap.get(String(row.registrationPeriodId)) || '—')}</td>
                        <td>${escapeHtml(row.registrationTypeName || N6.typeText(row.registrationType))}</td>
                        <td>
                            <span class="badge ${Number(row.status) === 2 ? 'good' : Number(row.status) === 3 ? 'off' : ''}">
                                ${escapeHtml(row.statusName || N6.statusText(row.status))}
                            </span>
                        </td>
                        <td>${escapeHtml(N6.dateTimeText(row.registeredAt))}</td>
                        <td>${row.paid ? 'Đã thanh toán' : 'Chưa thanh toán'}</td>
                        <td>
                            ${Number(row.status) === 3 ? '—' : `
                                <button class="small danger" type="button" data-cancel="${escapeHtml(regId)}">
                                    Hủy đăng ký
                                </button>
                            `}
                        </td>
                    </tr>`;
                }).join('')}
            </tbody>
        </table>
    `;

    if (footer) {
        if (registrations.length > REG_PREVIEW_LIMIT) {
            footer.style.display = 'flex';
            footer.innerHTML = `
                <span class="muted">Đang hiển thị <strong>${visibleRows.length}</strong> / <strong>${registrations.length}</strong> lượt đăng ký.</span>
                <button type="button" id="toggleRegLimitBtn" class="secondary small">
                    <i class="bi ${showAllRegistrations ? 'bi-chevron-up' : 'bi-chevron-down'}"></i>
                    ${showAllRegistrations ? `Thu gọn còn ${REG_PREVIEW_LIMIT} dòng` : `Xem tất cả (${registrations.length} dòng)`}
                </button>
            `;
            $('toggleRegLimitBtn').addEventListener('click', () => {
                showAllRegistrations = !showAllRegistrations;
                renderRegistrations();
            });
        } else {
            footer.style.display = 'flex';
            footer.innerHTML = `<span class="muted">Tổng cộng <strong>${registrations.length}</strong> lượt đăng ký.</span>`;
        }
    }
}

// =====================================================
// 9. KIEM TRA DIEU KIEN, DANG KY, HUY
// =====================================================
function selectedRegistration() {
    const registrationType = Number($('registrationType')?.value);
    if (![1, 2, 3].includes(registrationType)) {
        throw new Error('Loại đăng ký không hợp lệ. Chỉ chấp nhận 1, 2 hoặc 3.');
    }
    return {
        studentId: $('studentId').value,
        courseClassId: $('courseClassId').value,
        registrationPeriodId: $('registrationPeriodId').value,
        registrationType
    };
}

function validateRegistration(data) {
    if (!data.studentId) {
        showMessage('Vui lòng chọn sinh viên.', 'error');
        return false;
    }
    if (!data.registrationPeriodId) {
        showMessage('Vui lòng chọn đợt đăng ký.', 'error');
        return false;
    }
    if (!data.courseClassId) {
        showMessage('Vui lòng chọn lớp học phần.', 'error');
        return false;
    }
    return true;
}

async function checkEligibility(showFeedback = true) {
    const data = selectedRegistration();
    if (!validateRegistration(data)) return null;

    const query = new URLSearchParams({
        studentId: data.studentId,
        courseClassId: data.courseClassId,
        registrationPeriodId: data.registrationPeriodId,
        registrationType: String(data.registrationType)
    });

    try {
        const result = await apiRequest(`${API.registrations}/eligibility?${query}`);
        const eligible = result.eligible === true;
        const box = $('eligibilityResult');

        box.className = `notice show ${eligible ? 'success' : 'error'}`;
        box.textContent = `${eligible ? 'ĐỦ ĐIỀU KIỆN' : 'CHƯA ĐỦ ĐIỀU KIỆN'}: ${result.message || ''}`
            + (eligible
                ? `\nTín chỉ môn: ${result.courseCredits ?? '—'}; tổng sau đăng ký: ${result.totalCreditsAfter ?? '—'}; giới hạn đợt: ${result.minCredits ?? '—'}–${result.maxCredits ?? '—'}.`
                : '');

        if (showFeedback) {
            showMessage(result.message || 'Đã kiểm tra điều kiện.', eligible ? 'success' : 'error');
        }
        return result;
    } catch (error) {
        $('eligibilityResult').className = 'notice show error';
        $('eligibilityResult').textContent = 'Không kiểm tra được điều kiện: ' + error.message;
        if (showFeedback) showMessage('Không kiểm tra được điều kiện: ' + error.message, 'error');
        return null;
    }
}

async function submitRegistration(event) {
    event.preventDefault();
    const data = selectedRegistration();
    if (!validateRegistration(data)) return;

    const check = await checkEligibility(false);
    if (!check || check.eligible !== true) {
        showMessage(check?.message || 'Sinh viên chưa đủ điều kiện đăng ký.', 'error');
        return;
    }

    const body = {
        studentId: data.studentId,
        courseClassId: data.courseClassId,
        registrationPeriodId: data.registrationPeriodId,
        registrationType: Number(data.registrationType),
        replacedGradeId: check.suggestedReplacedGradeId || null
    };

    try {
        await apiRequest(API.registrations, {
            method: 'POST',
            body: JSON.stringify(body)
        });
        showMessage('Đăng ký học phần thành công.', 'success');

        await Promise.all([loadRegistrations(), loadClassSummary(), refreshQuickSummary()]);
        await suggestRegistrationType();
    } catch (error) {
        showMessage('Đăng ký học phần thất bại: ' + error.message, 'error');
    }
}

async function cancelRegistration(id) {
    if (!confirm('Bạn có chắc muốn hủy đăng ký học phần này?')) return;
    try {
        await apiRequest(`${API.registrations}/${encodeURIComponent(id)}/cancel`, { method: 'PATCH' });
        showMessage('Đã hủy đăng ký học phần.', 'success');
        await Promise.all([loadRegistrations(), loadClassSummary(), refreshQuickSummary()]);
        await suggestRegistrationType();
    } catch (error) {
        showMessage('Không hủy được đăng ký: ' + error.message, 'error');
    }
}

// =====================================================
// 10. GAN SU KIEN (BAO GOM THU GON MUC & MODAL)
// =====================================================
function setupCollapseButton(cardId, btnId) {
    const card = $(cardId);
    const btn = $(btnId);
    if (!card || !btn) return;
    btn.addEventListener('click', () => {
        const collapsed = card.classList.toggle('card-collapsed');
        btn.innerHTML = collapsed
            ? '<i class="bi bi-chevron-down"></i> Mở rộng mục'
            : '<i class="bi bi-chevron-up"></i> Thu gọn mục';
    });
}

setupCollapseButton('classSectionCard', 'toggleClassSectionBtn');
setupCollapseButton('regSectionCard', 'toggleRegSectionBtn');

$('studentQuickSearch').addEventListener('input', e => renderStudentSelect(e.target.value));
$('classQuickSearch').addEventListener('input', e => renderClassSelect(e.target.value));

$('studentId').addEventListener('change', () => {
    refreshQuickSummary();
    suggestRegistrationType();
});

$('courseClassId').addEventListener('change', () => {
    suggestRegistrationType();
});

$('registrationPeriodId').addEventListener('change', async () => {
    $('eligibilityResult').className = 'notice';
    $('eligibilityResult').textContent = '';
    try {
        await filterClassesForPeriod();
        await refreshQuickSummary();
        if ($('classListOnlyCurrentPeriod').checked) {
            await loadClassSummary();
        }
    } catch (error) {
        showMessage('Không tải được lớp theo học kỳ: ' + error.message, 'error');
    }
});

$('reloadPeriodsButton').addEventListener('click', async () => {
    try {
        await loadPeriods(true);
        await filterClassesForPeriod();
        await refreshQuickSummary();
    } catch (error) {
        showMessage('Không tải được danh sách đợt đăng ký: ' + error.message, 'error');
    }
});

$('checkEligibility').addEventListener('click', () => checkEligibility(true));
$('registerForm').addEventListener('submit', submitRegistration);

// Bang lop hoc phan & DSSV
$('searchClassListButton').addEventListener('click', () => {
    showAllClasses = false;
    loadClassSummary();
});
$('classListOnlyCurrentPeriod').addEventListener('change', () => {
    showAllClasses = false;
    loadClassSummary();
});
$('classListOnlyHasStudents').addEventListener('change', () => {
    showAllClasses = false;
    renderClassSummaryTable();
});
$('classListKeyword').addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        e.preventDefault();
        showAllClasses = false;
        loadClassSummary();
    }
});
$('resetClassListButton').addEventListener('click', () => {
    $('classListKeyword').value = '';
    $('classListOnlyCurrentPeriod').checked = false;
    $('classListOnlyHasStudents').checked = false;
    showAllClasses = false;
    loadClassSummary();
});

$('classSummaryTable').addEventListener('click', async event => {
    const viewBtn = event.target.closest('[data-view-students]');
    const pickBtn = event.target.closest('[data-pick-class]');

    if (viewBtn) {
        $('studentInClassKeyword').value = '';
        await openStudentsOfClass(viewBtn.dataset.viewStudents, viewBtn.dataset.classCode, '');
    }

    if (pickBtn) {
        const classId = pickBtn.dataset.pickClass;
        const semesterId = pickBtn.dataset.pickSemester;

        const currentPeriod = periods.find(p => String(p.id) === String($('registrationPeriodId').value));
        if (semesterId && (!currentPeriod || String(currentPeriod.semesterId) !== String(semesterId))) {
            const matchedPeriod = periods.find(
                p => String(p.semesterId) === String(semesterId) && getPeriodStatus(p) === 'Đang mở'
            ) || periods.find(p => String(p.semesterId) === String(semesterId));
            if (matchedPeriod) {
                $('registrationPeriodId').value = matchedPeriod.id;
                await filterClassesForPeriod();
            }
        }

        $('classQuickSearch').value = '';
        renderClassSelect('');
        $('courseClassId').value = classId;
        await refreshQuickSummary();
        await suggestRegistrationType();
        window.scrollTo({ top: 0, behavior: 'smooth' });
        showMessage('Đã đưa lớp học phần lên biểu mẫu đăng ký.', 'info');
    }
});

// Modal DSSV
$('closeClassDetailButton').addEventListener('click', closeStudentsModal);
$('classStudentsModal').addEventListener('click', e => {
    if (e.target === $('classStudentsModal')) closeStudentsModal();
});
document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && $('classStudentsModal').classList.contains('open')) {
        closeStudentsModal();
    }
});
$('searchStudentInClassButton').addEventListener('click', () => {
    if (currentViewingClassId) {
        openStudentsOfClass(currentViewingClassId, currentViewingClassCode, $('studentInClassKeyword').value);
    }
});
$('studentInClassKeyword').addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        e.preventDefault();
        $('searchStudentInClassButton').click();
    }
});

// Tim kiem & huy dang ky
$('searchRegistrationsButton').addEventListener('click', () => {
    showAllRegistrations = false;
    loadRegistrations();
});
$('regFilterKeyword').addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        e.preventDefault();
        showAllRegistrations = false;
        loadRegistrations();
    }
});
$('resetRegFilterButton').addEventListener('click', () => {
    $('regFilterKeyword').value = '';
    $('regFilterPeriod').value = '';
    $('regFilterStatus').value = '';
    showAllRegistrations = false;
    loadRegistrations();
});

$('registrationTable').addEventListener('click', event => {
    const button = event.target.closest('[data-cancel]');
    if (button) cancelRegistration(button.dataset.cancel);
});

// =====================================================
// 11. KHOI DONG TRANG
// =====================================================
async function initializePage() {
    try {
        await Promise.all([loadStudents(), loadClasses(), loadPeriods(false)]);
        if ($('registrationPeriodId').value) {
            await filterClassesForPeriod();
        }
        await Promise.all([loadClassSummary(), loadRegistrations()]);

        const opened = periods.filter(p => getPeriodStatus(p) === 'Đang mở').length;
        showMessage(
            periods.length
                ? `Đã tải dữ liệu: ${periods.length} đợt đăng ký (${opened} đợt đang mở), ${classSummaryRows.length} lớp học phần.`
                : 'Database chưa có đợt đăng ký. Hãy tạo đợt tại trang Đợt đăng ký.',
            periods.length ? 'success' : 'info'
        );
    } catch (error) {
        showMessage('Lỗi khi tải dữ liệu trang đăng ký:\n' + error.message, 'error');
    }
}

initializePage();