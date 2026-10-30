
let periodRows = [];
let semesterRows = [];

const periodForm = document.getElementById('periodForm');

function resetPeriodForm() {
    periodForm.reset();

    document.getElementById('periodId').value = '';

    document.getElementById('minCredits').value = 12;
    document.getElementById('maxCredits').value = 25;

    document.getElementById('allowRetake').checked = false;
    document.getElementById('periodActive').checked = true;

    document.getElementById('formHeading').textContent =
        'Tạo đợt đăng ký';

    document.getElementById('savePeriodButton').textContent =
        'Lưu đợt đăng ký';
}

function renderPeriods() {
    const container = document.getElementById('periodTable');

    if (!periodRows.length) {
        container.innerHTML = '<p>Chưa có đợt đăng ký nào.</p>';
        return;
    }

    const semesterMap = new Map(
        semesterRows.map(row => [
            row.id,
            `${row.code || ''} ${row.name || ''}`.trim()
        ])
    );

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Tên đợt</th>
                    <th>Học kỳ</th>
                    <th>Thời gian</th>
                    <th>Tín chỉ</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>
                ${periodRows.map(row => `
                    <tr>

                        <td>
                            <strong>${N6.esc(row.name)}</strong>
                            <div class="muted">
                                ID: ${N6.esc(row.id)}
                            </div>
                        </td>

                        <td>
                            ${N6.esc(
                                semesterMap.get(row.semesterId)
                                || row.semesterId
                            )}
                        </td>

                        <td>
                            ${N6.esc(N6.dateTimeText(row.startTime))}
                            <br>
                            đến
                            <br>
                            ${N6.esc(N6.dateTimeText(row.endTime))}
                        </td>

                        <td>
                            ${N6.esc(row.minCredits ?? 12)}
                            –
                            ${N6.esc(row.maxCredits ?? 25)}
                        </td>

                        <td>
                            <span class="badge ${row.active ? 'good' : 'off'}">
                                ${row.active ? 'Đang mở' : 'Đã đóng'}
                            </span>

                            <div class="muted">
                                ${row.allowRetake
                                    ? 'Cho phép học lại/cải thiện'
                                    : 'Không cho học lại/cải thiện'}
                            </div>
                        </td>

                        <td>
                            <div class="actions">

                                <button
                                    class="small secondary"
                                    type="button"
                                    data-edit="${N6.esc(row.id)}">

                                    Sửa

                                </button>

                                <button
                                    class="small danger"
                                    type="button"
                                    data-delete="${N6.esc(row.id)}">

                                    Xóa

                                </button>

                            </div>
                        </td>

                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function loadPeriods() {
    periodRows = await N6.request(N6.API.periods);
    renderPeriods();
}

async function loadPage() {
    try {
        semesterRows = await N6.request(
            N6.API.options + '/semesters'
        );

        N6.fillSelect(
            'semesterId',
            semesterRows,
            'Chọn học kỳ',
            row => `${row.code || ''} — ${row.name || ''}`
                .replace(/^ — /, '')
        );

        await loadPeriods();

        N6.clearMessage();

    } catch (error) {

        N6.showMessage(
            'Không tải được dữ liệu đợt đăng ký/học kỳ.\n'
            + error.message,
            'error'
        );

        document.getElementById('periodTable').textContent =
            'Không thể tải dữ liệu.';
    }
}

periodForm.addEventListener('submit', async event => {
    event.preventDefault();

    const id = document.getElementById('periodId').value;

    const body = {
        name: document.getElementById('periodName').value.trim(),

        semesterId: document.getElementById('semesterId').value,

        startTime: document.getElementById('startTime').value,

        endTime: document.getElementById('endTime').value,

        minCredits: Number(
            document.getElementById('minCredits').value
        ),

        maxCredits: Number(
            document.getElementById('maxCredits').value
        ),

        targetConfig:
            document.getElementById('targetConfig').value.trim()
            || null,

        allowRetake:
            document.getElementById('allowRetake').checked,

        active:
            document.getElementById('periodActive').checked
    };

    if (body.maxCredits < body.minCredits) {
        N6.showMessage(
            'Tín chỉ tối đa phải lớn hơn hoặc bằng tín chỉ tối thiểu.',
            'error'
        );
        return;
    }

    if (new Date(body.endTime) <= new Date(body.startTime)) {
        N6.showMessage(
            'Thời gian kết thúc phải sau thời gian bắt đầu.',
            'error'
        );
        return;
    }

    try {

        await N6.request(
            id
                ? `${N6.API.periods}/${encodeURIComponent(id)}`
                : N6.API.periods,
            {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(body)
            }
        );

        N6.showMessage(
            id
                ? 'Cập nhật đợt đăng ký thành công.'
                : 'Tạo đợt đăng ký thành công.',
            'success'
        );

        resetPeriodForm();

        await loadPeriods();

    } catch (error) {

        N6.showMessage(
            'Không lưu được đợt đăng ký.\n' + error.message,
            'error'
        );
    }
});

document.getElementById('resetPeriodButton')
    .addEventListener('click', resetPeriodForm);

document.getElementById('periodTable')
    .addEventListener('click', async event => {

        const editButton = event.target.closest('[data-edit]');
        const deleteButton = event.target.closest('[data-delete]');

        if (editButton) {

            const row = periodRows.find(
                item => item.id === editButton.dataset.edit
            );

            if (!row) return;

            document.getElementById('periodId').value = row.id;

            document.getElementById('periodName').value =
                row.name || '';

            document.getElementById('semesterId').value =
                row.semesterId || '';

            document.getElementById('startTime').value =
                N6.dateTimeInput(row.startTime);

            document.getElementById('endTime').value =
                N6.dateTimeInput(row.endTime);

            document.getElementById('minCredits').value =
                row.minCredits ?? 12;

            document.getElementById('maxCredits').value =
                row.maxCredits ?? 25;

            document.getElementById('targetConfig').value =
                row.targetConfig || '';

            document.getElementById('allowRetake').checked =
                Boolean(row.allowRetake);

            document.getElementById('periodActive').checked =
                Boolean(row.active);

            document.getElementById('formHeading').textContent =
                'Cập nhật đợt đăng ký';

            document.getElementById('savePeriodButton').textContent =
                'Cập nhật đợt đăng ký';

            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        }

        if (deleteButton) {

            const id = deleteButton.dataset.delete;

            if (!confirm('Bạn có chắc muốn xóa mềm đợt đăng ký này?')) {
                return;
            }

            try {

                await N6.request(
                    `${N6.API.periods}/${encodeURIComponent(id)}`,
                    { method: 'DELETE' }
                );

                N6.showMessage(
                    'Đã xóa mềm đợt đăng ký.',
                    'success'
                );

                await loadPeriods();

            } catch (error) {

                N6.showMessage(
                    'Không xóa được đợt đăng ký.\n' + error.message,
                    'error'
                );
            }
        }
    });

resetPeriodForm();
loadPage();
