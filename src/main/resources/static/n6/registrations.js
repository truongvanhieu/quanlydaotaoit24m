
'use strict';

// =====================================================
// N6 - DANG KY HOC PHAN
// FULL VERSION
// =====================================================


// =====================================================
// 1. API BACKEND
// =====================================================

const API = {

    students: '/api/registration-options/students',

    classes: '/api/registration-options/course-classes',

    periods: '/api/registration-periods',

    registrations: '/api/course-registrations',

    registrationDetails: '/api/course-registrations/details'

};


// =====================================================
// 2. DU LIEU
// =====================================================

let students = [];

let classes = [];

let availableClasses = [];

let periods = [];

let registrations = [];


// DOM HELPER
const $ = id => document.getElementById(id);


// =====================================================
// 3. GOI API CO THOI GIAN CHO
// =====================================================

async function apiRequest(url, options = {}, timeoutMs = 15000) {

    const controller = new AbortController();

    const timer = setTimeout(
        () => controller.abort(),
        timeoutMs
    );

    try {

        return await N6.request(url, {
            ...options,
            signal: controller.signal
        });

    } catch (error) {

        if (error.name === 'AbortError') {

            throw new Error(
                `API quá thời gian chờ ${timeoutMs / 1000} giây: ${url}`
            );

        }

        throw error;

    } finally {

        clearTimeout(timer);

    }

}


// =====================================================
// 4. KIEM TRA DU LIEU TRA VE
// =====================================================

function asArray(value, name) {

    if (!Array.isArray(value)) {

        throw new Error(
            `API ${name} không trả về danh sách JSON hợp lệ.`
        );

    }

    return value;

}


// =====================================================
// 5. HIEN THI THONG BAO
// =====================================================

function showMessage(message, type = 'info') {

    N6.showMessage(message, type);

}


// =====================================================
// 6. DO DU LIEU VAO SELECT
// =====================================================

function fillSelect(id, rows, placeholder, labelFn) {

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


    if (
        [...select.options].some(
            option => option.value === oldValue
        )
    ) {

        select.value = oldValue;

    }

}


// =====================================================
// 7. ESCAPE HTML
// =====================================================

function escapeHtml(value) {

    return N6.esc(value ?? '');

}


// =====================================================
// 8. TEN SINH VIEN
// =====================================================

function studentLabel(row) {

    const code =
        row.code ||
        row.studentCode ||
        '';

    const name =
        row.fullName ||
        row.name ||
        '';

    return `${code} — ${name}`.replace(/^ — /, '') || row.id;

}


// =====================================================
// 9. TEN LOP HOC PHAN
// =====================================================

function classLabel(row) {

    const code =
        row.code ||
        row.classCode ||
        row.id;

    const courseCode =
        row.courseCode || '';

    const courseName =
        row.courseName || '';

    const credits =
        row.credits ?? '?';

    return `${code} — ${courseCode} ${courseName} (${credits} tín chỉ)`;

}


// =====================================================
// 10. KIEM TRA TRANG THAI DOT DANG KY
// =====================================================

function getPeriodStatus(period) {

    const active =

        period.active === true ||

        period.active === 1 ||

        period.active === 'true';


    if (!active) {

        return 'Đã tắt';

    }


    const start = period.startTime
        ? new Date(period.startTime)
        : null;

    const end = period.endTime
        ? new Date(period.endTime)
        : null;


    const now = new Date();


    if (
        !start ||
        Number.isNaN(start.getTime()) ||
        !end ||
        Number.isNaN(end.getTime())
    ) {

        return 'Thiếu thời gian';

    }


    if (now < start) {

        return 'Chưa bắt đầu';

    }


    if (now > end) {

        return 'Đã hết hạn';

    }


    return 'Đang mở';

}


// =====================================================
// 11. HIEN THI DANH SACH DOT DANG KY
// =====================================================

function renderPeriods() {

    fillSelect(

        'registrationPeriodId',

        periods,

        periods.length
            ? 'Chọn đợt đăng ký'
            : 'Chưa có đợt đăng ký trong database',

        row => {

            return `${row.name || row.id} — ${getPeriodStatus(row)}`;

        }

    );


    fillSelect(

        'summaryPeriodId',

        periods,

        periods.length
            ? 'Chọn đợt đăng ký'
            : 'Chưa có đợt đăng ký',

        row => row.name || row.id

    );


    const hint = $('periodHint');

    if (!hint) return;


    if (periods.length === 0) {

        hint.textContent =
            'Database chưa có đợt đăng ký. Hãy tạo đợt tại trang Đợt đăng ký, sau đó bấm Tải lại đợt.';

    } else {

        const opened = periods.filter(

            row => getPeriodStatus(row) === 'Đang mở'

        ).length;


        hint.textContent =
            `Đã tải ${periods.length} đợt; ${opened} đợt đang mở.`;

    }

}


// =====================================================
// 12. TAI DANH SACH SINH VIEN
// =====================================================

async function loadStudents() {

    $('studentId').innerHTML =
        '<option value="">Đang tải danh sách sinh viên...</option>';


    $('summaryStudentId').innerHTML =
        '<option value="">Đang tải danh sách sinh viên...</option>';


    try {

        students = asArray(

            await apiRequest(API.students),

            'sinh viên'

        );


        fillSelect(

            'studentId',

            students,

            students.length
                ? 'Chọn sinh viên'
                : 'Không có sinh viên',

            studentLabel

        );


        fillSelect(

            'summaryStudentId',

            students,

            students.length
                ? 'Chọn sinh viên'
                : 'Không có sinh viên',

            studentLabel

        );


    } catch (error) {

        fillSelect(
            'studentId',
            [],
            'Không tải được sinh viên',
            studentLabel
        );

        fillSelect(
            'summaryStudentId',
            [],
            'Không tải được sinh viên',
            studentLabel
        );

        throw error;

    }

}


// =====================================================
// 13. TAI DANH SACH LOP HOC PHAN
// =====================================================

async function loadClasses(semesterId = null) {

    $('courseClassId').innerHTML =
        '<option value="">Đang tải lớp học phần...</option>';


    const url = semesterId

        ? `${API.classes}?semesterId=${encodeURIComponent(semesterId)}`

        : API.classes;


    try {

        const rows = asArray(

            await apiRequest(url),

            'lớp học phần'

        );


        if (semesterId) {

            availableClasses = rows;

        } else {

            classes = rows;

            availableClasses = rows;

        }


        fillSelect(

            'courseClassId',

            availableClasses,

            availableClasses.length
                ? 'Chọn lớp học phần'
                : 'Không có lớp học phần phù hợp',

            classLabel

        );


        return availableClasses;


    } catch (error) {

        fillSelect(

            'courseClassId',

            [],

            'Không tải được lớp học phần',

            classLabel

        );

        throw error;

    }

}


// =====================================================
// 14. TAI DANH SACH DOT DANG KY
// =====================================================

async function loadPeriods(showFeedback = false) {

    $('registrationPeriodId').innerHTML =
        '<option value="">Đang tải đợt đăng ký...</option>';


    $('periodHint').textContent =
        'Đang kết nối API đợt đăng ký...';


    try {

        periods = asArray(

            await apiRequest(API.periods),

            'đợt đăng ký'

        );


        renderPeriods();


        if (showFeedback) {

            showMessage(

                periods.length

                    ? `Đã tải ${periods.length} đợt đăng ký.`

                    : 'Database chưa có đợt đăng ký. Hãy tạo đợt mới.',

                periods.length ? 'success' : 'info'

            );

        }


        return periods;


    } catch (error) {

        periods = [];


        fillSelect(

            'registrationPeriodId',

            [],

            'Không tải được đợt đăng ký',

            row => row.name || row.id

        );


        fillSelect(

            'summaryPeriodId',

            [],

            'Không tải được đợt đăng ký',

            row => row.name || row.id

        );


        $('periodHint').textContent =
            'Lỗi tải API đợt đăng ký: ' + error.message;


        throw error;

    }

}


// =====================================================
// 15. TAI DANH SACH DANG KY
// =====================================================

async function loadRegistrations() {

    $('registrationTable').textContent =
        'Đang tải danh sách đăng ký...';


    try {

        registrations = asArray(

            await apiRequest(API.registrationDetails),

            'chi tiết đăng ký học phần'

        );


        renderRegistrations();


    } catch (error) {

        $('registrationTable').textContent =
            'Không tải được danh sách đăng ký: ' + error.message;


        throw error;

    }

}


// =====================================================
// 16. LOC LOP THEO HOC KY
// =====================================================

async function filterClassesForPeriod() {

    const periodId =
        $('registrationPeriodId').value;


    if (!periodId) {

        await loadClasses();

        return;

    }


    const period = periods.find(

        row => String(row.id) === String(periodId)

    );


    if (!period) {

        throw new Error(
            'Không tìm thấy đợt đăng ký đã chọn. Hãy tải lại danh sách.'
        );

    }


    if (!period.semesterId) {

        throw new Error(
            'Đợt đăng ký chưa có semesterId.'
        );

    }


    await loadClasses(period.semesterId);


    if (availableClasses.length === 0) {

        showMessage(

            'Không có lớp học phần thuộc học kỳ của đợt đăng ký này.',

            'info'

        );

    } else {

        N6.clearMessage();

    }

}


// =====================================================
// 17. HIEN THI DANH SACH DANG KY
// =====================================================

function renderRegistrations() {

    const container = $('registrationTable');


    if (!registrations.length) {

        container.innerHTML =
            '<p>Chưa có đăng ký học phần.</p>';

        return;

    }


    const studentMap = new Map(

        students.map(row => [

            String(row.id),

            studentLabel(row)

        ])

    );


    const classMap = new Map(

        classes.map(row => [

            String(row.id),

            classLabel(row)

        ])

    );


    const periodMap = new Map(

        periods.map(row => [

            String(row.id),

            row.name || row.id

        ])

    );


    container.innerHTML = `

        <table>

            <thead>

                <tr>

                    <th>Sinh viên (MSSV — Họ tên)</th>

                    <th>Lớp học phần (Mã lớp — Môn học, tín chỉ)</th>

                    <th>Đợt đăng ký</th>

                    <th>Loại</th>

                    <th>Trạng thái</th>

                    <th>Thời gian</th>

                    <th>Thanh toán</th>

                    <th>Thao tác</th>

                </tr>

            </thead>


            <tbody>

                ${registrations.map(row => `

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
                                (row.classCode
                                    ? `${row.classCode} — ${row.courseCode || ''} ${row.courseName || ''} (${row.credits ?? '?'} tín chỉ)`
                                    : classMap.get(String(row.courseClassId)))
                                || row.courseClassId
                            )}
                        </td>


                        <td>
                            ${escapeHtml(
                                row.periodName
                                || periodMap.get(String(row.registrationPeriodId))
                                || row.registrationPeriodId
                                || '—'
                            )}
                        </td>


                        <td>
                            ${escapeHtml(
                                N6.typeText(row.registrationType)
                            )}
                        </td>


                        <td>

                            <span class="badge ${
                                Number(row.status) === 2
                                    ? 'good'
                                    : Number(row.status) === 3
                                        ? 'off'
                                        : ''
                            }">

                                ${escapeHtml(
                                    N6.statusText(row.status)
                                )}

                            </span>

                        </td>


                        <td>
                            ${escapeHtml(
                                N6.dateTimeText(row.registeredAt)
                            )}
                        </td>


                        <td>
                            ${row.paid
                                ? 'Đã thanh toán'
                                : 'Chưa thanh toán'}
                        </td>


                        <td>

                            ${
                                Number(row.status) === 3

                                ? '—'

                                : `

                                    <button
                                        class="small danger"
                                        type="button"
                                        data-cancel="${escapeHtml(row.id)}">

                                        Hủy đăng ký

                                    </button>

                                `
                            }

                        </td>

                    </tr>

                `).join('')}

            </tbody>

        </table>

    `;

}


// =====================================================
// 18. LAY DU LIEU FORM
// =====================================================

function selectedRegistration() {

    const typeSelect = $('registrationType');

    if (!typeSelect) {

        throw new Error('Không tìm thấy ô chọn loại đăng ký.');

    }


    const selectedOption =
        typeSelect.options[typeSelect.selectedIndex];


    const registrationType =
        Number(selectedOption?.value);


    // LOG DE KIEM TRA GIA TRI THAT
    console.log('====================================');

    console.log('N6 - Loại đăng ký đang chọn:');

    console.log('Nhãn:', selectedOption?.textContent);

    console.log('Value:', selectedOption?.value);

    console.log('Number:', registrationType);

    console.log('====================================');


    if (![1, 2, 3].includes(registrationType)) {

        throw new Error(
            'Loại đăng ký không hợp lệ. Chỉ chấp nhận 1, 2 hoặc 3.'
        );

    }


    return {

        studentId: $('studentId').value,

        courseClassId: $('courseClassId').value,

        registrationPeriodId: $('registrationPeriodId').value,

        registrationType: registrationType

    };

}


// =====================================================
// 19. KIEM TRA DU LIEU NHAP
// =====================================================

function validateRegistration(data) {

    if (!data.studentId) {

        showMessage(
            'Vui lòng chọn sinh viên.',
            'error'
        );

        return false;

    }


    if (!data.registrationPeriodId) {

        showMessage(
            'Vui lòng chọn đợt đăng ký.',
            'error'
        );

        return false;

    }


    if (!data.courseClassId) {

        showMessage(
            'Vui lòng chọn lớp học phần.',
            'error'
        );

        return false;

    }


    if (![1, 2, 3].includes(data.registrationType)) {

        showMessage(
            'Loại đăng ký không hợp lệ.',
            'error'
        );

        return false;

    }


    return true;

}


// =====================================================
// 20. KIEM TRA DIEU KIEN DANG KY
// =====================================================

async function checkEligibility(showFeedback = true) {

    const data = selectedRegistration();


    if (!validateRegistration(data)) {

        return null;

    }


    // TAO QUERY TUONG MINH
    const query = new URLSearchParams();


    query.set('studentId', data.studentId);

    query.set('courseClassId', data.courseClassId);

    query.set('registrationPeriodId', data.registrationPeriodId);


    // DONG QUAN TRONG NHAT
    // Luon gui dung gia tri so 1, 2 hoac 3
    query.set(
        'registrationType',
        String(data.registrationType)
    );


    const requestUrl =
        `${API.registrations}/eligibility?${query.toString()}`;


    // KIEM TRA URL THUC TE GUI SANG BACKEND
    console.log('N6 - Eligibility request:');

    console.log(requestUrl);

    console.log(
        'N6 - registrationType:',
        data.registrationType
    );


    try {

        const result = await apiRequest(requestUrl);


        console.log('N6 - Eligibility response:', result);


        const eligible =
            result.eligible === true;


        const box = $('eligibilityResult');


        box.className =
            `notice show ${eligible ? 'success' : 'error'}`;


        box.textContent =

            `${eligible ? 'ĐỦ ĐIỀU KIỆN' : 'CHƯA ĐỦ ĐIỀU KIỆN'}: ${result.message || ''}`

            +

            (

                eligible

                    ? `\nTín chỉ môn: ${result.courseCredits ?? '—'}; tổng sau đăng ký: ${result.totalCreditsAfter ?? '—'}; giới hạn đợt: ${result.minCredits ?? '—'}–${result.maxCredits ?? '—'}.`

                    : ''

            );


        if (showFeedback) {

            showMessage(

                result.message || 'Đã kiểm tra điều kiện.',

                eligible ? 'success' : 'error'

            );

        }


        return result;


    } catch (error) {

        boxError(error);


        if (showFeedback) {

            showMessage(
                'Không kiểm tra được điều kiện: ' + error.message,
                'error'
            );

        }


        return null;

    }

}


function boxError(error) {

    $('eligibilityResult').className =
        'notice show error';


    $('eligibilityResult').textContent =
        'Không kiểm tra được điều kiện: ' + error.message;

}


// =====================================================
// 21. THUC HIEN DANG KY
// =====================================================

async function submitRegistration(event) {

    event.preventDefault();


    const data = selectedRegistration();


    if (!validateRegistration(data)) {

        return;

    }


    // KIEM TRA LAI DIEU KIEN
    const check = await checkEligibility(false);


    if (!check) {

        showMessage(
            'Không thể đăng ký vì API kiểm tra điều kiện chưa trả kết quả.',
            'error'
        );

        return;

    }


    if (check.eligible !== true) {

        showMessage(
            check.message || 'Sinh viên chưa đủ điều kiện đăng ký.',
            'error'
        );

        return;

    }


    // PAYLOAD GUI POST
    const body = {

        studentId: data.studentId,

        courseClassId: data.courseClassId,

        registrationPeriodId: data.registrationPeriodId,

        // Gui dung so, khong gui nhan hien thi
        registrationType: Number(data.registrationType),

        replacedGradeId:
            check.suggestedReplacedGradeId || null

    };


    console.log('N6 - POST registration payload:', body);


    try {

        await apiRequest(

            API.registrations,

            {

                method: 'POST',

                body: JSON.stringify(body)

            }

        );


        showMessage(
            'Đăng ký học phần thành công.',
            'success'
        );


        await loadRegistrations();


        if (

            $('summaryStudentId').value === data.studentId

            &&

            $('summaryPeriodId').value === data.registrationPeriodId

        ) {

            await loadSummary();

        }


    } catch (error) {

        showMessage(
            'Đăng ký học phần thất bại: ' + error.message,
            'error'
        );

    }

}


// =====================================================
// 22. TONG HOP TIN CHI
// =====================================================

async function loadSummary() {

    const studentId =
        $('summaryStudentId').value;


    const registrationPeriodId =
        $('summaryPeriodId').value;


    if (!studentId || !registrationPeriodId) {

        $('summaryResult').textContent =
            'Vui lòng chọn sinh viên và đợt đăng ký.';

        return;

    }


    try {

        const query = new URLSearchParams({

            studentId,

            registrationPeriodId

        });


        const summary = await apiRequest(

            `${API.registrations}/summary?${query}`

        );


        $('summaryResult').innerHTML = `

            <div class="stats" style="margin-top:12px">

                <div class="stat">

                    <span>Số môn đăng ký</span>

                    <strong>
                        ${escapeHtml(summary.registrationCount)}
                    </strong>

                </div>


                <div class="stat">

                    <span>Tổng tín chỉ</span>

                    <strong>
                        ${escapeHtml(summary.selectedCredits)}
                    </strong>

                </div>


                <div class="stat">

                    <span>Tín chỉ còn được đăng ký</span>

                    <strong>
                        ${escapeHtml(summary.creditsRemaining)}
                    </strong>

                </div>

            </div>


            <p>

                Giới hạn tín chỉ:

                ${escapeHtml(summary.minCredits)}–${escapeHtml(summary.maxCredits)}.

                Đạt tối thiểu:

                ${summary.minimumReached ? 'Có' : 'Chưa'}.

                Vượt tối đa:

                ${summary.maximumExceeded ? 'Có' : 'Không'}.

            </p>

        `;


    } catch (error) {

        $('summaryResult').textContent =
            'Không lấy được tổng hợp: ' + error.message;

    }

}


// =====================================================
// 23. HUY DANG KY
// =====================================================

async function cancelRegistration(id) {

    if (!confirm(
        'Bạn có chắc muốn hủy đăng ký học phần này?'
    )) {

        return;

    }


    try {

        await apiRequest(

            `${API.registrations}/${encodeURIComponent(id)}/cancel`,

            {

                method: 'PATCH'

            }

        );


        showMessage(
            'Đã hủy đăng ký học phần.',
            'success'
        );


        await loadRegistrations();


    } catch (error) {

        showMessage(
            'Không hủy được đăng ký: ' + error.message,
            'error'
        );

    }

}


// =====================================================
// 24. TAI LAI DOT DANG KY
// =====================================================

async function reloadPeriods() {

    try {

        await loadPeriods(true);


        if ($('registrationPeriodId').value) {

            await filterClassesForPeriod();

        } else {

            await loadClasses();

        }


    } catch (error) {

        showMessage(
            'Không tải được danh sách đợt đăng ký: ' + error.message,
            'error'
        );

    }

}


// =====================================================
// 25. SU KIEN CHON DOT DANG KY
// =====================================================

$('registrationPeriodId').addEventListener(
    'change',
    async () => {

        $('eligibilityResult').className = 'notice';

        $('eligibilityResult').textContent = '';


        try {

            await filterClassesForPeriod();

        } catch (error) {

            fillSelect(
                'courseClassId',
                [],
                'Không tải được lớp học phần',
                classLabel
            );


            showMessage(
                'Không tải được lớp theo học kỳ: ' + error.message,
                'error'
            );

        }

    }
);


// =====================================================
// 26. SU KIEN TAI LAI DOT
// =====================================================

$('reloadPeriodsButton').addEventListener(
    'click',
    reloadPeriods
);


// =====================================================
// 27. SU KIEN TAI LAI DANH SACH DANG KY
// =====================================================

$('reloadRegistrationsButton').addEventListener(
    'click',
    async () => {

        try {

            await loadRegistrations();

            showMessage(
                'Đã tải lại danh sách đăng ký.',
                'success'
            );

        } catch (error) {

            showMessage(
                'Không tải được danh sách đăng ký: ' + error.message,
                'error'
            );

        }

    }
);


// =====================================================
// 28. SU KIEN KIEM TRA DIEU KIEN
// =====================================================

$('checkEligibility').addEventListener(
    'click',
    () => checkEligibility(true)
);


// =====================================================
// 29. SU KIEN DANG KY
// =====================================================

$('registerForm').addEventListener(
    'submit',
    submitRegistration
);


// =====================================================
// 30. SU KIEN TONG HOP
// =====================================================

$('summaryForm').addEventListener(
    'submit',
    event => {

        event.preventDefault();

        loadSummary();

    }
);


// =====================================================
// 31. SU KIEN HUY DANG KY
// =====================================================

$('registrationTable').addEventListener(
    'click',
    event => {

        const button =
            event.target.closest('[data-cancel]');

        if (button) {

            cancelRegistration(button.dataset.cancel);

        }

    }
);


// =====================================================
// 32. KHOI DONG TRANG
// =====================================================

async function initializePage() {

    const tasks = [

        ['Sinh viên', loadStudents],

        ['Lớp học phần', loadClasses],

        ['Đợt đăng ký', () => loadPeriods(false)],

        ['Danh sách đăng ký', loadRegistrations]

    ];


    const results = await Promise.allSettled(

        tasks.map(([, task]) => task())

    );


    const errors = [];


    results.forEach((result, index) => {

        if (result.status === 'rejected') {

            errors.push(

                `${tasks[index][0]}: ${result.reason.message || result.reason}`

            );

        }

    });


    renderRegistrations();


    if (errors.length) {

        showMessage(

            'Một hoặc nhiều API chưa tải được:\n- '
            + errors.join('\n- '),

            'error'

        );

    } else if (periods.length === 0) {

        showMessage(

            'Database chưa có đợt đăng ký. Hãy tạo đợt tại trang Đợt đăng ký rồi bấm Tải lại đợt.',

            'info'

        );

    } else {

        const opened = periods.filter(

            period => getPeriodStatus(period) === 'Đang mở'

        ).length;


        showMessage(

            `Đã tải dữ liệu: ${periods.length} đợt đăng ký, ${opened} đợt đang mở.`,

            'success'

        );

    }

}


initializePage();
