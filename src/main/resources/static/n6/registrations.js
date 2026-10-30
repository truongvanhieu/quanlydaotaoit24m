
let students = [];
let classes = [];
let availableClasses = [];
let periods = [];
let registrations = [];

let lastEligibility = null;

function selectedRegistration() {
    return {
        studentId: document.getElementById('studentId').value,

        courseClassId: document.getElementById('courseClassId').value,

        registrationPeriodId:
            document.getElementById('registrationPeriodId').value,

        registrationType:
            Number(document.getElementById('registrationType').value)
    };
}

function registrationQuery(data) {
    const query = new URLSearchParams({
        studentId: data.studentId,
        courseClassId: data.courseClassId,
        registrationPeriodId: data.registrationPeriodId,
        registrationType: String(data.registrationType)
    });

    return query.toString();
}

async function checkEligibility(show = true) {
    const data = selectedRegistration();

    if (
        !data.studentId ||
        !data.courseClassId ||
        !data.registrationPeriodId
    ) {
        lastEligibility = null;

        if (show) {
            N6.showMessage(
                'Vui lòng chọn sinh viên, lớp học phần và đợt đăng ký.',
                'error'
            );
        }

        return null;
    }

    try {
        const result = await N6.request(
            `${N6.API.registrations}/eligibility?${registrationQuery(data)}`
        );

        lastEligibility = result;

        const box = document.getElementById('eligibilityResult');

        box.className =
            `notice show ${result.eligible ? 'success' : 'error'}`;

        box.textContent =
            `${result.eligible ? 'ĐỦ ĐIỀU KIỆN' : 'CHƯA ĐỦ ĐIỀU KIỆN'}: `
            + `${result.message || ''}`
            + (
                result.eligible
                    ? `\nTín chỉ môn: ${result.courseCredits ?? '—'}; `
                    + `tổng sau đăng ký: ${result.totalCreditsAfter ?? '—'}; `
                    + `giới hạn đợt: ${result.minCredits ?? '—'}–${result.maxCredits ?? '—'}.`
                    : ''
            );

        if (show) {
            N6.showMessage(
                result.message || 'Đã kiểm tra điều kiện.',
                result.eligible ? 'success' : 'error'
            );
        }

        return result;

    } catch (error) {

        lastEligibility = null;

        if (show) {
            N6.showMessage(
                'Không kiểm tra được điều kiện.\n' + error.message,
                'error'
            );
        }

        return null;
    }
}

function renderRegistrations() {
    const container =
        document.getElementById('registrationTable');

    if (!registrations.length) {
        container.innerHTML =
            '<p>Chưa có đăng ký học phần.</p>';
        return;
    }

    const studentMap = new Map(
        students.map(row => [
            row.id,
            `${row.code || ''} ${row.fullName || ''}`.trim()
        ])
    );

    const classMap = new Map(
        classes.map(row => [
            row.id,
            `${row.code || row.id} — ${row.courseCode || ''} ${row.courseName || ''}`.trim()
        ])
    );

    const periodMap = new Map(
        periods.map(row => [
            row.id,
            row.name || row.id
        ])
    );

    container.innerHTML = `
        <table>

            <thead>
                <tr>
                    <th>Sinh viên</th>
                    <th>Lớp học phần</th>
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
                            ${N6.esc(
                                studentMap.get(row.studentId)
                                || row.studentId
                            )}
                        </td>

                        <td>
                            ${N6.esc(
                                classMap.get(row.courseClassId)
                                || row.courseClassId
                            )}
                        </td>

                        <td>
                            ${N6.esc(
                                periodMap.get(row.registrationPeriodId)
                                || row.registrationPeriodId
                                || '—'
                            )}
                        </td>

                        <td>
                            ${N6.esc(
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
                                ${N6.esc(N6.statusText(row.status))}
                            </span>
                        </td>

                        <td>
                            ${N6.esc(
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
                                            data-cancel="${N6.esc(row.id)}">

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

async function loadRegistrations() {
    registrations = await N6.request(
        N6.API.registrations
    );

    renderRegistrations();
}

async function loadPage() {
    try {

        const results = await Promise.all([

            N6.request(
                N6.API.options + '/students'
            ),

            N6.request(
                N6.API.options + '/course-classes'
            ),

            N6.request(
                N6.API.periods
            )

        ]);

        students = results[0];
        classes = results[1];
        periods = results[2];

        N6.fillSelect(
            'studentId',
            students,
            'Chọn sinh viên',
            row => `${row.code || ''} — ${row.fullName || row.id}`
        );

        N6.fillSelect(
            'summaryStudentId',
            students,
            'Chọn sinh viên',
            row => `${row.code || ''} — ${row.fullName || row.id}`
        );

        availableClasses = classes;

        N6.fillSelect(
            'courseClassId',
            availableClasses,
            'Chọn lớp học phần',
            row =>
                `${row.code || row.id} — ${row.courseCode || ''} ${row.courseName || ''} (${row.credits ?? '?'} tín chỉ)`
        );

        N6.fillSelect(
            'registrationPeriodId',
            periods.filter(row => row.active),
            'Chọn đợt đang mở',
            row => row.name
        );

        N6.fillSelect(
            'summaryPeriodId',
            periods,
            'Chọn đợt đăng ký',
            row => row.name
        );

        await loadRegistrations();

        N6.clearMessage();

    } catch (error) {

        N6.showMessage(
            'Không tải được dữ liệu sinh viên/lớp học phần/đợt đăng ký.\n'
            + error.message,
            'error'
        );

        document.getElementById('registrationTable').textContent =
            'Không thể tải dữ liệu.';
    }
}

async function filterClassesForPeriod() {
    const periodId =
        document.getElementById('registrationPeriodId').value;

    const period = periods.find(
        row => row.id === periodId
    );

    const query = period
        ? `?semesterId=${encodeURIComponent(period.semesterId)}`
        : '';

    try {

        availableClasses = await N6.request(
            `${N6.API.options}/course-classes${query}`
        );

        N6.fillSelect(
            'courseClassId',
            availableClasses,
            'Chọn lớp học phần',
            row =>
                `${row.code || row.id} — ${row.courseCode || ''} ${row.courseName || ''} (${row.credits ?? '?'} tín chỉ)`
        );

    } catch (error) {

        N6.showMessage(
            'Không tải được lớp học phần của học kỳ đã chọn.\n'
            + error.message,
            'error'
        );
    }
}

document.getElementById('registrationPeriodId')
    .addEventListener('change', filterClassesForPeriod);

document.getElementById('checkEligibility')
    .addEventListener('click', () => checkEligibility(true));

[
    'studentId',
    'courseClassId',
    'registrationPeriodId',
    'registrationType'
].forEach(id => {

    document.getElementById(id)
        .addEventListener('change', () => {

            lastEligibility = null;

            const box =
                document.getElementById('eligibilityResult');

            box.className = 'notice';
            box.textContent = '';
        });
});

document.getElementById('registerForm')
    .addEventListener('submit', async event => {

        event.preventDefault();

        const data = selectedRegistration();

        const result = await checkEligibility(false);

        if (!result) {
            N6.showMessage(
                'Không kiểm tra được điều kiện. Vui lòng thử lại.',
                'error'
            );
            return;
        }

        if (!result.eligible) {
            N6.showMessage(
                result.message || 'Sinh viên chưa đủ điều kiện đăng ký.',
                'error'
            );
            return;
        }

        try {

            await N6.request(
                N6.API.registrations,
                {
                    method: 'POST',

                    body: JSON.stringify({
                        ...data,
                        replacedGradeId:
                            result.suggestedReplacedGradeId || null
                    })
                }
            );

            N6.showMessage(
                'Đăng ký học phần thành công.',
                'success'
            );

            await loadRegistrations();

            if (
                document.getElementById('summaryStudentId').value
                    === data.studentId
                &&
                document.getElementById('summaryPeriodId').value
                    === data.registrationPeriodId
            ) {
                await loadSummary();
            }

        } catch (error) {

            N6.showMessage(
                'Đăng ký học phần thất bại.\n' + error.message,
                'error'
            );
        }
    });

async function loadSummary() {
    const studentId =
        document.getElementById('summaryStudentId').value;

    const periodId =
        document.getElementById('summaryPeriodId').value;

    if (!studentId || !periodId) {

        document.getElementById('summaryResult').textContent =
            'Vui lòng chọn sinh viên và đợt đăng ký.';

        return;
    }

    try {

        const query = new URLSearchParams({
            studentId,
            registrationPeriodId: periodId
        });

        const summary = await N6.request(
            `${N6.API.registrations}/summary?${query}`
        );

        document.getElementById('summaryResult').innerHTML = `
            <div class="stats" style="margin-top:12px">

                <div class="stat">
                    <span>Số môn đăng ký</span>
                    <strong>${N6.esc(summary.registrationCount)}</strong>
                </div>

                <div class="stat">
                    <span>Tổng tín chỉ</span>
                    <strong>${N6.esc(summary.selectedCredits)}</strong>
                </div>

                <div class="stat">
                    <span>Tín chỉ còn được đăng ký</span>
                    <strong>${N6.esc(summary.creditsRemaining)}</strong>
                </div>

            </div>

            <p>
                Giới hạn tín chỉ:
                ${N6.esc(summary.minCredits)}–${N6.esc(summary.maxCredits)}.
                Đạt tối thiểu:
                ${summary.minimumReached ? 'Có' : 'Chưa'}.
                Vượt tối đa:
                ${summary.maximumExceeded ? 'Có' : 'Không'}.
            </p>
        `;

    } catch (error) {

        document.getElementById('summaryResult').textContent =
            'Không lấy được tổng hợp: ' + error.message;
    }
}

document.getElementById('summaryForm')
    .addEventListener('submit', event => {
        event.preventDefault();
        loadSummary();
    });

document.getElementById('registrationTable')
    .addEventListener('click', async event => {

        const button = event.target.closest('[data-cancel]');

        if (!button) return;

        if (!confirm('Bạn có chắc muốn hủy đăng ký học phần này?')) {
            return;
        }

        try {

            await N6.request(
                `${N6.API.registrations}/${encodeURIComponent(button.dataset.cancel)}/cancel`,
                { method: 'PATCH' }
            );

            N6.showMessage(
                'Đã hủy đăng ký học phần.',
                'success'
            );

            await loadRegistrations();

        } catch (error) {

            N6.showMessage(
                'Không hủy được đăng ký.\n' + error.message,
                'error'
            );
        }
    });

loadPage();
