import { API_ENDPOINTS } from './config.js';

// Lưu cache danh sách học phí sinh viên để phục vụ nút "Thu tiền"
let studentTuitionItems = [];

document.addEventListener('DOMContentLoaded', () => {
    initTuitionPage();
});

// =========================
// KHỞI TẠO TRANG
// =========================

function initTuitionPage() {
    const feeSearchForm = document.getElementById('feeSearchForm');
    const studentTuitionFilterForm = document.getElementById('studentTuitionFilterForm');
    const paymentFilterForm = document.getElementById('paymentFilterForm');

    const tuitionFeeForm = document.getElementById('tuitionFeeForm');
    const studentTuitionForm = document.getElementById('studentTuitionForm');
    const paymentForm = document.getElementById('paymentForm');

    const addTuitionFeeButton = document.getElementById('addTuitionFeeButton');
    const addStudentTuitionButton = document.getElementById('addStudentTuitionButton');
    const addPaymentButton = document.getElementById('addPaymentButton');

    // Tìm kiếm định mức học phí
    if (feeSearchForm) {
        feeSearchForm.addEventListener('submit', (event) => {
            event.preventDefault();

            loadTuitionFees(getValue('keyword'));
        });
    }

    if (tuitionFeeForm) {
        tuitionFeeForm.addEventListener('submit', saveTuitionFee);
    }

    if (addTuitionFeeButton) {
        addTuitionFeeButton.addEventListener('click', () => {
            openTuitionFeeModal(null);
        });
    }

    // Lọc học phí sinh viên
    if (studentTuitionFilterForm) {
        studentTuitionFilterForm.addEventListener('submit', (event) => {
            event.preventDefault();

            loadStudentTuitions();
        });
    }

    if (studentTuitionForm) {
        studentTuitionForm.addEventListener('submit', saveStudentTuition);
    }

    if (addStudentTuitionButton) {
        addStudentTuitionButton.addEventListener('click', () => {
            openStudentTuitionModal(null);
        });
    }

    // Lọc giao dịch thanh toán
    if (paymentFilterForm) {
        paymentFilterForm.addEventListener('submit', (event) => {
            event.preventDefault();

            loadPayments();
        });
    }

    if (paymentForm) {
        paymentForm.addEventListener('submit', savePayment);
    }

    if (addPaymentButton) {
        addPaymentButton.addEventListener('click', () => {
            openPaymentModal(null, null);
        });
    }

    // Nút xuất Excel (áp dụng cho mọi tab)
    document.querySelectorAll('[data-export]').forEach((button) => {
        button.addEventListener('click', () => {
            exportExcel(button.dataset.export, button);
        });
    });

    // Tab báo cáo công nợ: tải dữ liệu khi bấm "Tải lại"
    const reloadReportButton = document.getElementById('reloadReportButton');

    if (reloadReportButton) {
        reloadReportButton.addEventListener('click', () => {
            loadSemesterReports();
        });
    }

    // In biên lai
    const printReceiptButton = document.getElementById('printReceiptButton');

    if (printReceiptButton) {
        printReceiptButton.addEventListener('click', printReceipt);
    }

    // Tải danh mục cho các hộp chọn + cả 3 danh sách ngay khi mở trang
    loadLookupOptions();
    loadTuitionFees();
    loadStudentTuitions();
    loadPayments();
    loadSummary();
    loadSemesterReports();
}

// =========================
// DANH MỤC CHO HỘP CHỌN (DROPDOWN)
// =========================

/** Nạp danh mục sinh viên / học kỳ / chương trình / định mức / học phí sinh viên vào các hộp chọn. */
async function loadLookupOptions() {
    try {
        const [students, semesters, programs, tuitionFees, studentTuitions] = await Promise.all([
            fetchLookup(`${API_ENDPOINTS.TUITION_LOOKUPS}/students`),
            fetchLookup(`${API_ENDPOINTS.TUITION_LOOKUPS}/semesters`),
            fetchLookup(`${API_ENDPOINTS.TUITION_LOOKUPS}/training-programs`),
            fetchLookup(API_ENDPOINTS.TUITION_FEES),
            fetchLookup(API_ENDPOINTS.STUDENT_TUITIONS)
        ]);

        populateSelect('studentId', students, 'Tất cả sinh viên', studentLabel);
        populateSelect('stStudentId', students, '-- Chọn sinh viên --', studentLabel);

        populateSelect('semesterId', semesters, 'Tất cả học kỳ', semesterLabel);
        populateSelect('stSemesterId', semesters, '-- Chọn học kỳ --', semesterLabel);

        populateSelect('feeProgramId', programs, '-- Chọn chương trình đào tạo --', programLabel);
        populateSelect('stTuitionFeeId', tuitionFees, '-- Chọn định mức học phí --', tuitionFeeLabel);

        populateSelect('tuitionId', studentTuitions, 'Tất cả học phí sinh viên', studentTuitionLabel);
        populateSelect('payTuitionId', studentTuitions, '-- Chọn học phí sinh viên --', studentTuitionLabel);
    } catch (error) {
        console.error('Lỗi khi tải danh mục cho hộp chọn:', error);
    }
}

async function fetchLookup(url) {
    const response = await fetch(url);

    if (!response.ok) {
        const message = await readErrorMessage(response, `HTTP ${response.status}`);
        throw new Error(message);
    }

    const items = await response.json();

    return Array.isArray(items) ? items : [];
}

/** Đổ danh sách lựa chọn vào một thẻ <select>, giữ lại giá trị đang chọn nếu còn tồn tại. */
function populateSelect(id, options, placeholder, labelFn) {
    const select = document.getElementById(id);

    if (!select) {
        return;
    }

    const current = select.value;

    const optionHtml = options
        .map((option) => {
            const value = escapeHtml(option.id);

            return `<option value="${value}">${escapeHtml(labelFn(option))}</option>`;
        })
        .join('');

    select.innerHTML = `<option value="">${escapeHtml(placeholder)}</option>${optionHtml}`;

    if (current) {
        select.value = current;
    }
}

function studentLabel(option) {
    return option.code ? `${option.name} (${option.code})` : option.name;
}

function semesterLabel(option) {
    return option.name || option.code;
}

function programLabel(option) {
    return option.code ? `${option.name} (${option.code})` : option.name;
}

function tuitionFeeLabel(item) {
    const program = item.programName || 'Chưa rõ chương trình';

    return item.courseYear ? `${program} - ${item.courseYear}` : program;
}

function studentTuitionLabel(item) {
    const student = item.studentCode
        ? `${item.studentName} (${item.studentCode})`
        : item.studentName;

    return `${student} - ${item.semesterName}`;
}

// =========================
// ĐỊNH MỨC HỌC PHÍ
// =========================

async function loadTuitionFees(keyword = '') {
    const tableBody = document.getElementById('tuitionFeeTableBody');

    if (!tableBody) {
        return;
    }

    try {
        tableBody.innerHTML = renderLoadingRow(7);

        const trimmedKeyword = (keyword || '').trim();

        const url = trimmedKeyword
            ? `${API_ENDPOINTS.TUITION_FEES}?keyword=${encodeURIComponent(trimmedKeyword)}`
            : API_ENDPOINTS.TUITION_FEES;

        const response = await fetch(url);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const items = await response.json();

        renderTuitionFees(items);
    } catch (error) {
        console.error('Lỗi khi tải định mức học phí:', error);

        tableBody.innerHTML = renderErrorRow(
            7,
            error.message || 'Không thể tải định mức học phí'
        );
    }
}

function renderTuitionFees(items) {
    const tableBody = document.getElementById('tuitionFeeTableBody');

    if (!tableBody) {
        return;
    }

    if (!items || items.length === 0) {
        tableBody.innerHTML = renderEmptyRow(7);
        return;
    }

    tableBody.innerHTML = items
        .map((item) => {
            return `
                <tr>
                    <td>
                        ${escapeHtml(item.courseYear)}
                    </td>

                    <td>
                        ${renderNameCode(item.programName, item.programCode)}
                    </td>

                    <td>
                        ${formatCurrency(item.pricePerCredit)}
                    </td>

                    <td>
                        ${formatCurrency(item.baseTuition)}
                    </td>

                    <td>
                        ${escapeHtml(item.effectiveDate)}
                    </td>

                    <td>
                        ${renderActiveBadge(item.isActive)}
                    </td>

                    <td class="text-end">
                        <div class="table-actions d-flex justify-content-end gap-1">

                            <!-- Sửa -->
                            <button
                                type="button"
                                class="btn btn-warning btn-sm"
                                title="Sửa"
                                onclick="editTuitionFee('${escapeHtml(item.id)}')"
                            >
                                <i class="bi bi-pencil-square"></i>
                            </button>

                            <!-- Xóa -->
                            <button
                                type="button"
                                class="btn btn-danger btn-sm"
                                title="Xóa"
                                onclick="deleteTuitionFee('${escapeHtml(item.id)}')"
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

function openTuitionFeeModal(item) {
    const form = document.getElementById('tuitionFeeForm');

    if (!form) {
        return;
    }

    form.reset();

    const idField = document.getElementById('tuitionFeeId');
    const isActiveField = document.getElementById('feeIsActive');

    if (item && item.id) {
        // Chế độ sửa
        if (idField) {
            idField.value = item.id;
        }

        setValue('feeProgramId', item.programId);
        setValue('feeCourseYear', item.courseYear);
        setValue('feePricePerCredit', item.pricePerCredit);
        setValue('feeBaseTuition', item.baseTuition);
        setValue(
            'feeEffectiveDate',
            item.effectiveDate ? String(item.effectiveDate).slice(0, 10) : ''
        );

        if (isActiveField) {
            isActiveField.checked = item.isActive === true;
        }

        setModalMode('tuitionFee', true);
    } else {
        // Chế độ thêm mới
        if (idField) {
            idField.value = '';
        }

        if (isActiveField) {
            isActiveField.checked = true;
        }

        setModalMode('tuitionFee', false);
    }

    showModal('tuitionFeeModal');
}

async function saveTuitionFee(event) {
    event.preventDefault();

    const id = getValue('tuitionFeeId');

    const payload = {
        programId: getValue('feeProgramId'),
        courseYear: getValue('feeCourseYear') || null,
        pricePerCredit: toNumberOrNull(getValue('feePricePerCredit')),
        baseTuition: toNumberOrNull(getValue('feeBaseTuition')),
        effectiveDate: getValue('feeEffectiveDate') || null,
        isActive: getChecked('feeIsActive')
    };

    if (!payload.programId) {
        alert('Vui lòng chọn chương trình đào tạo');
        return;
    }

    if (payload.pricePerCredit === null) {
        alert('Vui lòng nhập đơn giá tín chỉ');
        return;
    }

    if (!payload.effectiveDate) {
        alert('Vui lòng chọn ngày áp dụng');
        return;
    }

    try {
        const isEdit = Boolean(id);

        const url = isEdit
            ? `${API_ENDPOINTS.TUITION_FEES}/${encodeURIComponent(id)}`
            : API_ENDPOINTS.TUITION_FEES;

        const response = await fetch(url, {
            method: isEdit ? 'PUT' : 'POST',

            headers: {
                'Content-Type': 'application/json'
            },

            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        closeModal('tuitionFeeModal');

        alert(
            isEdit
                ? 'Cập nhật định mức học phí thành công'
                : 'Thêm định mức học phí thành công'
        );

        await loadTuitionFees(getValue('keyword'));
    } catch (error) {
        console.error('Lỗi khi lưu định mức học phí:', error);

        alert(error.message || 'Lưu định mức học phí thất bại');
    }
}

window.editTuitionFee = async function (id) {
    try {
        const item = await fetchDetail(API_ENDPOINTS.TUITION_FEES, id);

        openTuitionFeeModal(item);
    } catch (error) {
        console.error('Lỗi khi lấy định mức học phí:', error);

        alert(error.message || 'Không thể lấy định mức học phí');
    }
};

window.deleteTuitionFee = async function (id) {
    const confirmed = window.confirm(
        'Bạn có chắc muốn xóa định mức học phí này?'
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `${API_ENDPOINTS.TUITION_FEES}/${encodeURIComponent(id)}`,
            {
                method: 'DELETE'
            }
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        alert('Xóa định mức học phí thành công');

        await loadTuitionFees(getValue('keyword'));
    } catch (error) {
        console.error('Lỗi khi xóa định mức học phí:', error);

        alert(error.message || 'Xóa định mức học phí thất bại');
    }
};

// =========================
// HỌC PHÍ SINH VIÊN
// =========================

async function loadStudentTuitions() {
    const tableBody = document.getElementById('studentTuitionTableBody');

    if (!tableBody) {
        return;
    }

    try {
        tableBody.innerHTML = renderLoadingRow(13);

        const params = new URLSearchParams();

        const studentId = getValue('studentId');
        const semesterId = getValue('semesterId');
        const status = getValue('studentTuitionStatus');

        if (studentId) {
            params.append('studentId', studentId);
        }

        if (semesterId) {
            params.append('semesterId', semesterId);
        }

        if (status) {
            params.append('status', status);
        }

        const query = params.toString();

        const url = query
            ? `${API_ENDPOINTS.STUDENT_TUITIONS}?${query}`
            : API_ENDPOINTS.STUDENT_TUITIONS;

        const response = await fetch(url);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const items = await response.json();

        studentTuitionItems = Array.isArray(items) ? items : [];

        renderStudentTuitions(studentTuitionItems);
    } catch (error) {
        console.error('Lỗi khi tải học phí sinh viên:', error);

        tableBody.innerHTML = renderErrorRow(
            13,
            error.message || 'Không thể tải học phí sinh viên'
        );
    }
}

function renderStudentTuitions(items) {
    const tableBody = document.getElementById('studentTuitionTableBody');

    if (!tableBody) {
        return;
    }

    if (!items || items.length === 0) {
        tableBody.innerHTML = renderEmptyRow(13);
        return;
    }

    tableBody.innerHTML = items
        .map((item) => {
            return `
                <tr>
                    <td>
                        ${renderNameCode(item.studentName, item.studentCode)}
                    </td>

                    <td>
                        ${escapeHtml(item.semesterName || '-')}
                    </td>

                    <td>
                        ${renderProgramCell(item.programName, item.courseYear)}
                    </td>

                    <td>
                        ${escapeHtml(item.totalCredits)}
                    </td>

                    <td>
                        ${formatCurrency(item.rawAmount)}
                        <div class="text-muted small">
                            ${renderRawAmountDetail(item)}
                        </div>
                    </td>

                    <td>
                        ${formatCurrency(item.scholarshipDeduction)}
                    </td>

                    <td>
                        ${formatCurrency(item.exemptionAmount)}
                    </td>

                    <td>
                        ${formatCurrency(item.netAmount)}
                    </td>

                    <td>
                        ${formatCurrency(item.paidAmount)}
                    </td>

                    <td>
                        ${formatCurrency(item.debtAmount)}
                    </td>

                    <td>
                        ${renderStudentStatusBadge(item.statusName || item.status)}
                    </td>

                    <td>
                        ${escapeHtml(item.deadline)}
                    </td>

                    <td class="text-end">
                        <div class="table-actions d-flex justify-content-end gap-1">

                            <!-- Thu tiền -->
                            <button
                                type="button"
                                class="btn btn-success btn-sm"
                                title="Thu tiền"
                                onclick="collectTuitionPayment('${escapeHtml(item.id)}')"
                            >
                                <i class="bi bi-cash-coin"></i>
                            </button>

                            <!-- Sửa -->
                            <button
                                type="button"
                                class="btn btn-warning btn-sm"
                                title="Sửa"
                                onclick="editStudentTuition('${escapeHtml(item.id)}')"
                            >
                                <i class="bi bi-pencil-square"></i>
                            </button>

                            <!-- Xóa -->
                            <button
                                type="button"
                                class="btn btn-danger btn-sm"
                                title="Xóa"
                                onclick="deleteStudentTuition('${escapeHtml(item.id)}')"
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

function openStudentTuitionModal(item) {
    const form = document.getElementById('studentTuitionForm');

    if (!form) {
        return;
    }

    form.reset();

    const idField = document.getElementById('studentTuitionId');

    if (item && item.id) {
        // Chế độ sửa
        if (idField) {
            idField.value = item.id;
        }

        setValue('stStudentId', item.studentId);
        setValue('stSemesterId', item.semesterId);
        setValue('stTuitionFeeId', item.tuitionFeeId);
        setValue('stTotalCredits', item.totalCredits);
        setValue('stScholarshipDeduction', item.scholarshipDeduction);
        setValue('stExemptionAmount', item.exemptionAmount);
        setValue(
            'stDeadline',
            item.deadline ? String(item.deadline).slice(0, 10) : ''
        );

        setModalMode('studentTuition', true);
    } else {
        // Chế độ thêm mới
        if (idField) {
            idField.value = '';
        }

        setModalMode('studentTuition', false);
    }

    showModal('studentTuitionModal');
}

async function saveStudentTuition(event) {
    event.preventDefault();

    const id = getValue('studentTuitionId');

    const payload = {
        studentId: getValue('stStudentId'),
        semesterId: getValue('stSemesterId'),
        tuitionFeeId: getValue('stTuitionFeeId'),
        totalCredits: toNumberOrNull(getValue('stTotalCredits')),
        scholarshipDeduction: toNumberOrNull(getValue('stScholarshipDeduction')),
        exemptionAmount: toNumberOrNull(getValue('stExemptionAmount')),
        deadline: getValue('stDeadline') || null
    };

    if (!payload.studentId) {
        alert('Vui lòng chọn sinh viên');
        return;
    }

    if (!payload.semesterId) {
        alert('Vui lòng chọn học kỳ');
        return;
    }

    if (!payload.tuitionFeeId) {
        alert('Vui lòng chọn định mức học phí');
        return;
    }

    if (payload.totalCredits === null || payload.totalCredits < 1) {
        alert('Vui lòng nhập số tín chỉ hợp lệ');
        return;
    }

    try {
        const isEdit = Boolean(id);

        const url = isEdit
            ? `${API_ENDPOINTS.STUDENT_TUITIONS}/${encodeURIComponent(id)}`
            : API_ENDPOINTS.STUDENT_TUITIONS;

        const response = await fetch(url, {
            method: isEdit ? 'PUT' : 'POST',

            headers: {
                'Content-Type': 'application/json'
            },

            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        closeModal('studentTuitionModal');

        alert(
            isEdit
                ? 'Cập nhật học phí sinh viên thành công'
                : 'Thêm học phí sinh viên thành công'
        );

        await loadStudentTuitions();
        await loadLookupOptions();
    } catch (error) {
        console.error('Lỗi khi lưu học phí sinh viên:', error);

        alert(error.message || 'Lưu học phí sinh viên thất bại');
    }
}

window.editStudentTuition = async function (id) {
    try {
        const item = await fetchDetail(API_ENDPOINTS.STUDENT_TUITIONS, id);

        openStudentTuitionModal(item);
    } catch (error) {
        console.error('Lỗi khi lấy học phí sinh viên:', error);

        alert(error.message || 'Không thể lấy học phí sinh viên');
    }
};

window.deleteStudentTuition = async function (id) {
    const confirmed = window.confirm(
        'Bạn có chắc muốn xóa học phí sinh viên này?'
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `${API_ENDPOINTS.STUDENT_TUITIONS}/${encodeURIComponent(id)}`,
            {
                method: 'DELETE'
            }
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        alert('Xóa học phí sinh viên thành công');

        await loadStudentTuitions();
        await loadLookupOptions();
    } catch (error) {
        console.error('Lỗi khi xóa học phí sinh viên:', error);

        alert(error.message || 'Xóa học phí sinh viên thất bại');
    }
};

// Mở modal thanh toán với học phí và số tiền còn nợ điền sẵn
window.collectTuitionPayment = function (id) {
    const item = studentTuitionItems.find(
        (entry) => String(entry.id) === String(id)
    );

    if (!item) {
        alert('Không tìm thấy học phí sinh viên');
        return;
    }

    const debt = Number(item.debtAmount || 0);

    openPaymentModal(null, {
        tuitionId: item.id,
        amountPaid: debt > 0 ? debt : ''
    });
};

// =========================
// GIAO DỊCH THANH TOÁN
// =========================

async function loadPayments() {
    const tableBody = document.getElementById('paymentTableBody');

    if (!tableBody) {
        return;
    }

    try {
        tableBody.innerHTML = renderLoadingRow(8);

        const params = new URLSearchParams();

        const tuitionId = getValue('tuitionId');
        const status = getValue('paymentStatus');
        const method = getValue('method');

        if (tuitionId) {
            params.append('tuitionId', tuitionId);
        }

        if (status) {
            params.append('status', status);
        }

        if (method) {
            params.append('method', method);
        }

        const query = params.toString();

        const url = query
            ? `${API_ENDPOINTS.PAYMENTS}?${query}`
            : API_ENDPOINTS.PAYMENTS;

        const response = await fetch(url);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const items = await response.json();

        renderPayments(items);
    } catch (error) {
        console.error('Lỗi khi tải giao dịch thanh toán:', error);

        tableBody.innerHTML = renderErrorRow(
            8,
            error.message || 'Không thể tải giao dịch thanh toán'
        );
    }
}

function renderPayments(items) {
    const tableBody = document.getElementById('paymentTableBody');

    if (!tableBody) {
        return;
    }

    if (!items || items.length === 0) {
        tableBody.innerHTML = renderEmptyRow(8);
        return;
    }

    tableBody.innerHTML = items
        .map((item) => {
            return `
                <tr>
                    <td>
                        ${formatDateTime(item.paymentDate)}
                    </td>

                    <td>
                        ${renderNameCode(item.studentName, item.studentCode)}
                        <div class="text-muted small">
                            ${escapeHtml(item.semesterName || '-')}
                        </div>
                    </td>

                    <td>
                        ${formatCurrency(item.amountPaid)}
                    </td>

                    <td>
                        ${escapeHtml(renderPaymentMethod(item.paymentMethod))}
                    </td>

                    <td>
                        ${renderPaymentStatusBadge(item.paymentStatus)}
                    </td>

                    <td>
                        ${escapeHtml(item.transactionRef)}
                    </td>

                    <td>
                        ${escapeHtml(item.notes)}
                    </td>

                    <td class="text-end">
                        <div class="table-actions d-flex justify-content-end gap-1">

                            <!-- In biên lai -->
                            <button
                                type="button"
                                class="btn btn-outline-primary btn-sm"
                                title="In biên lai"
                                onclick="openReceipt('${escapeHtml(item.id)}')"
                            >
                                <i class="bi bi-printer"></i>
                            </button>

                            <!-- Sửa -->
                            <button
                                type="button"
                                class="btn btn-warning btn-sm"
                                title="Sửa"
                                onclick="editPayment('${escapeHtml(item.id)}')"
                            >
                                <i class="bi bi-pencil-square"></i>
                            </button>

                            <!-- Hủy -->
                            <button
                                type="button"
                                class="btn btn-danger btn-sm"
                                title="Hủy"
                                onclick="cancelPayment('${escapeHtml(item.id)}')"
                            >
                                <i class="bi bi-x-circle"></i>
                            </button>

                        </div>
                    </td>
                </tr>
            `;
        })
        .join('');
}

function openPaymentModal(item, preset) {
    const form = document.getElementById('paymentForm');

    if (!form) {
        return;
    }

    form.reset();

    const idField = document.getElementById('paymentId');
    const isActiveField = document.getElementById('payIsActive');

    if (item && item.id) {
        // Chế độ sửa
        if (idField) {
            idField.value = item.id;
        }

        setValue('payTuitionId', item.tuitionId);
        setValue('payAmountPaid', item.amountPaid);
        setValue(
            'payPaymentDate',
            item.paymentDate ? String(item.paymentDate).slice(0, 16) : ''
        );
        setValue(
            'payPaymentMethod',
            item.paymentMethod === null || item.paymentMethod === undefined
                ? ''
                : item.paymentMethod
        );
        setValue('payPaymentStatus', item.paymentStatus || 'SUCCESS');
        setValue('payTransactionRef', item.transactionRef);
        setValue('payCashierId', item.cashierId);
        setValue('payNotes', item.notes);

        if (isActiveField) {
            isActiveField.checked = item.isActive === true;
        }

        setModalMode('payment', true);
    } else {
        // Chế độ thêm mới
        if (idField) {
            idField.value = '';
        }

        setValue('payPaymentStatus', 'SUCCESS');

        if (isActiveField) {
            isActiveField.checked = true;
        }

        if (preset) {
            setValue('payTuitionId', preset.tuitionId);
            setValue('payAmountPaid', preset.amountPaid);
        }

        setModalMode('payment', false);
    }

    showModal('paymentModal');
}

async function savePayment(event) {
    event.preventDefault();

    const id = getValue('paymentId');

    const payload = {
        tuitionId: getValue('payTuitionId'),
        amountPaid: toNumberOrNull(getValue('payAmountPaid')),
        paymentDate: getValue('payPaymentDate') || null,
        paymentMethod: toNumberOrNull(getValue('payPaymentMethod')),
        paymentStatus: getValue('payPaymentStatus') || 'SUCCESS',
        transactionRef: getValue('payTransactionRef') || null,
        cashierId: getValue('payCashierId') || null,
        notes: getValue('payNotes') || null,
        isActive: getChecked('payIsActive')
    };

    if (!payload.tuitionId) {
        alert('Vui lòng chọn học phí sinh viên');
        return;
    }

    if (payload.amountPaid === null || payload.amountPaid <= 0) {
        alert('Vui lòng nhập số tiền thanh toán hợp lệ');
        return;
    }

    try {
        const isEdit = Boolean(id);

        const url = isEdit
            ? `${API_ENDPOINTS.PAYMENTS}/${encodeURIComponent(id)}`
            : API_ENDPOINTS.PAYMENTS;

        const response = await fetch(url, {
            method: isEdit ? 'PUT' : 'POST',

            headers: {
                'Content-Type': 'application/json'
            },

            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        closeModal('paymentModal');

        alert(
            isEdit
                ? 'Cập nhật giao dịch thành công'
                : 'Ghi nhận thanh toán thành công'
        );

        // Số tiền đã nộp / còn nợ thay đổi nên tải lại cả 2 bảng
        await loadPayments();
        await loadStudentTuitions();
        await loadLookupOptions();
    } catch (error) {
        console.error('Lỗi khi lưu giao dịch thanh toán:', error);

        alert(error.message || 'Lưu giao dịch thanh toán thất bại');
    }
}

window.editPayment = async function (id) {
    try {
        const item = await fetchDetail(API_ENDPOINTS.PAYMENTS, id);

        openPaymentModal(item, null);
    } catch (error) {
        console.error('Lỗi khi lấy giao dịch thanh toán:', error);

        alert(error.message || 'Không thể lấy giao dịch thanh toán');
    }
};

window.cancelPayment = async function (id) {
    const confirmed = window.confirm(
        'Bạn có chắc muốn hủy giao dịch này?'
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `${API_ENDPOINTS.PAYMENTS}/${encodeURIComponent(id)}`,
            {
                method: 'DELETE'
            }
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        alert('Hủy giao dịch thanh toán thành công');

        // Tải lại cả bảng thanh toán và bảng học phí sinh viên
        await loadPayments();
        await loadStudentTuitions();
        await loadLookupOptions();
    } catch (error) {
        console.error('Lỗi khi hủy giao dịch thanh toán:', error);

        alert(error.message || 'Hủy giao dịch thanh toán thất bại');
    }
};

// =========================
// THỐNG KÊ TỔNG QUAN
// =========================

async function loadSummary() {
    const container = document.getElementById('summaryCards');

    if (!container) {
        return;
    }

    try {
        const response = await fetch(`${API_ENDPOINTS.TUITION_REPORTS}/summary`);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const summary = await response.json();

        renderSummary(summary);
    } catch (error) {
        console.error('Lỗi khi tải thống kê tổng quan:', error);

        container.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger mb-0">
                    ${escapeHtml(error.message || 'Không thể tải thống kê tổng quan')}
                </div>
            </div>
        `;
    }
}

function renderSummary(summary) {
    const container = document.getElementById('summaryCards');

    if (!container) {
        return;
    }

    const data = summary || {};

    const cards = [
        {
            label: 'Tổng bản ghi học phí',
            value: formatNumber(data.totalRecords),
            icon: 'bi-journal-text',
            className: 'text-bg-primary'
        },
        {
            label: 'Đã hoàn tất (PAID)',
            value: formatNumber(data.paidCount),
            icon: 'bi-check2-circle',
            className: 'text-bg-success'
        },
        {
            label: 'Sinh viên còn nợ',
            value: formatNumber(data.debtorCount),
            icon: 'bi-exclamation-triangle',
            className: 'text-bg-danger'
        },
        {
            label: 'Tổng phải thu',
            value: `${formatCurrency(data.totalNet)} đ`,
            icon: 'bi-cash-stack',
            className: 'text-bg-info'
        },
        {
            label: 'Đã thu',
            value: `${formatCurrency(data.totalPaid)} đ`,
            icon: 'bi-cash-coin',
            className: 'text-bg-success'
        },
        {
            label: 'Còn nợ',
            value: `${formatCurrency(data.totalDebt)} đ`,
            icon: 'bi-exclamation-octagon',
            className: 'text-bg-warning'
        }
    ];

    container.innerHTML = cards
        .map((card) => `
            <div class="col-sm-6 col-xl-4">
                <div class="card shadow-sm border-0 h-100">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="d-inline-flex align-items-center justify-content-center rounded-circle ${card.className}" style="width: 48px; height: 48px;">
                            <i class="bi ${card.icon} fs-5"></i>
                        </span>
                        <div>
                            <div class="text-muted small">
                                ${escapeHtml(card.label)}
                            </div>
                            <div class="fs-4 fw-semibold">
                                ${card.value}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `)
        .join('');

    // Tỷ lệ thu gộp vào thẻ "Đã thu" dưới dạng ghi chú nếu cần
    if (data.collectionRate !== null && data.collectionRate !== undefined) {
        const note = document.createElement('div');
        note.className = 'col-12';
        note.innerHTML = `
            <div class="text-muted small">
                Tỷ lệ thu học phí: <strong>${formatNumber(data.collectionRate)}%</strong>
            </div>
        `;
        container.appendChild(note);
    }
}

// =========================
// BÁO CÁO CÔNG NỢ THEO HỌC KỲ
// =========================

async function loadSemesterReports() {
    const tableBody = document.getElementById('semesterReportTableBody');

    if (!tableBody) {
        return;
    }

    try {
        tableBody.innerHTML = renderLoadingRow(7);

        const response = await fetch(`${API_ENDPOINTS.TUITION_REPORTS}/by-semester`);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const items = await response.json();

        renderSemesterReports(items);
    } catch (error) {
        console.error('Lỗi khi tải báo cáo công nợ:', error);

        tableBody.innerHTML = renderErrorRow(
            7,
            error.message || 'Không thể tải báo cáo công nợ'
        );
    }
}

function renderSemesterReports(items) {
    const tableBody = document.getElementById('semesterReportTableBody');

    if (!tableBody) {
        return;
    }

    if (!items || items.length === 0) {
        tableBody.innerHTML = renderEmptyRow(7);
        return;
    }

    tableBody.innerHTML = items
        .map((item) => {
            const rate = computeRate(item.totalPaid, item.totalNet);

            return `
                <tr>
                    <td>
                        ${escapeHtml(item.semesterName)}
                    </td>

                    <td class="text-end">
                        ${formatNumber(item.studentCount)}
                    </td>

                    <td class="text-end">
                        ${item.debtorCount > 0
                            ? `<span class="badge text-bg-danger">${formatNumber(item.debtorCount)}</span>`
                            : '<span class="badge text-bg-success">0</span>'}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalNet)}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalPaid)}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalDebt)}
                    </td>

                    <td class="text-end">
                        ${formatNumber(rate)}%
                    </td>
                </tr>
            `;
        })
        .join('');
}

function computeRate(paid, net) {
    const total = Number(net || 0);
    const collected = Number(paid || 0);

    if (total === 0) {
        return 0;
    }

    return Math.round((collected * 100 / total) * 10) / 10;
}

// =========================
// XUẤT EXCEL
// =========================

async function exportExcel(type, button) {
    if (!type) {
        return;
    }

    const originalHtml = button ? button.innerHTML : '';

    if (button) {
        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang xuất...';
    }

    try {
        const response = await fetch(
            `${API_ENDPOINTS.TUITION_REPORTS}/export?type=${encodeURIComponent(type)}`
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const blob = await response.blob();

        const objectUrl = URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = `${type}.xlsx`;

        document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(objectUrl);
    } catch (error) {
        console.error('Lỗi khi xuất Excel:', error);

        alert(error.message || 'Xuất Excel thất bại');
    } finally {
        if (button) {
            button.disabled = false;
            button.innerHTML = originalHtml;
        }
    }
}

// =========================
// BIÊN LAI THU TIỀN
// =========================

window.openReceipt = async function (paymentId) {
    const container = document.getElementById('receiptContent');

    if (!container) {
        return;
    }

    container.innerHTML = '<div class="text-muted">Đang tải biên lai...</div>';

    showModal('receiptModal');

    try {
        const response = await fetch(
            `${API_ENDPOINTS.TUITION_REPORTS}/receipt/${encodeURIComponent(paymentId)}`
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const receipt = await response.json();

        container.innerHTML = renderReceipt(receipt);
    } catch (error) {
        console.error('Lỗi khi tải biên lai:', error);

        container.innerHTML = `
            <div class="alert alert-danger mb-0">
                ${escapeHtml(error.message || 'Không thể tải biên lai')}
            </div>
        `;
    }
};

function renderReceipt(receipt) {
    const data = receipt || {};

    const paymentStatusClasses = {
        SUCCESS: 'badge text-bg-success',
        PENDING: 'badge text-bg-warning',
        FAILED: 'badge text-bg-danger',
        REFUNDED: 'badge text-bg-info',
        CANCELLED: 'badge text-bg-secondary'
    };

    const statusClass = paymentStatusClasses[data.paymentStatus] || 'badge text-bg-secondary';

    return `
        <div class="border rounded p-4">
            <!-- Tiêu đề -->
            <div class="text-center mb-4">
                <h4 class="mb-1">
                    TRƯỜNG / TRUNG TÂM ĐÀO TẠO
                </h4>
                <div class="fw-semibold">
                    BIÊN LAI THU HỌC PHÍ
                </div>
                <div class="text-muted small">
                    Mã giao dịch: ${renderUuid(data.paymentId)}
                </div>
            </div>

            <!-- Thông tin sinh viên -->
            <table class="table table-sm mb-3">
                <tbody>
                    <tr>
                        <th style="width: 220px;">
                            Mã sinh viên
                        </th>
                        <td>
                            ${escapeHtml(data.studentCode || '-')}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Họ và tên
                        </th>
                        <td>
                            ${escapeHtml(data.studentName || '-')}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Chương trình đào tạo
                        </th>
                        <td>
                            ${escapeHtml(data.programName || '-')}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Khóa học
                        </th>
                        <td>
                            ${escapeHtml(data.courseYear || '-')}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Học kỳ
                        </th>
                        <td>
                            ${escapeHtml(data.semesterName || '-')}
                        </td>
                    </tr>
                </tbody>
            </table>

            <!-- Nội dung thu -->
            <table class="table table-sm table-bordered mb-3">
                <tbody>
                    <tr>
                        <th style="width: 220px;">
                            Ngày thu
                        </th>
                        <td>
                            ${formatDateTime(data.paymentDate)}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Hình thức thanh toán
                        </th>
                        <td>
                            ${escapeHtml(data.paymentMethodName || '-')}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Mã tham chiếu
                        </th>
                        <td>
                            ${escapeHtml(data.transactionRef || '-')}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Trạng thái giao dịch
                        </th>
                        <td>
                            <span class="${statusClass}">
                                ${escapeHtml(data.paymentStatus || '-')}
                            </span>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Số tiền nộp
                        </th>
                        <td class="fw-semibold">
                            ${formatCurrency(data.amountPaid)} đ
                        </td>
                    </tr>
                </tbody>
            </table>

            <!-- Tình trạng học phí -->
            <table class="table table-sm mb-3">
                <tbody>
                    <tr>
                        <th style="width: 220px;">
                            Thực nộp (sau miễn giảm)
                        </th>
                        <td>
                            ${formatCurrency(data.netAmount)} đ
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Đã nộp (lũy kế)
                        </th>
                        <td>
                            ${formatCurrency(data.paidAmount)} đ
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Còn nợ
                        </th>
                        <td>
                            ${formatCurrency(data.debtAmount)} đ
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Trạng thái học phí
                        </th>
                        <td>
                            ${escapeHtml(data.tuitionStatusName || '-')}
                        </td>
                    </tr>
                </tbody>
            </table>

            <!-- Ghi chú -->
            <div class="mb-4">
                <div class="fw-semibold mb-1">
                    Ghi chú
                </div>
                <div class="text-muted">
                    ${escapeHtml(data.notes || '—')}
                </div>
            </div>

            <!-- Chữ ký -->
            <div class="row text-center mt-5">
                <div class="col-6">
                    <div class="fw-semibold">
                        Người nộp tiền
                    </div>
                    <div class="text-muted small fst-italic">
                        (Ký, ghi rõ họ tên)
                    </div>
                </div>
                <div class="col-6">
                    <div class="fw-semibold">
                        Người thu tiền
                    </div>
                    <div class="text-muted small fst-italic">
                        (Ký, ghi rõ họ tên)
                    </div>
                </div>
            </div>
        </div>
    `;
}

function printReceipt() {
    window.print();
}

// =========================
// TIỆN ÍCH DÙNG CHUNG
// =========================

async function fetchDetail(baseUrl, id) {
    const response = await fetch(
        `${baseUrl}/${encodeURIComponent(id)}`
    );

    if (!response.ok) {
        const message = await readErrorMessage(response, `HTTP ${response.status}`);
        throw new Error(message);
    }

    return response.json();
}

function getModalInstance(id) {
    const element = document.getElementById(id);

    if (!element || typeof bootstrap === 'undefined') {
        return null;
    }

    return bootstrap.Modal.getOrCreateInstance(element);
}

function showModal(id) {
    const modal = getModalInstance(id);

    if (modal) {
        modal.show();
    }
}

function closeModal(id) {
    const modal = getModalInstance(id);

    if (modal) {
        modal.hide();
    }
}

function setModalMode(prefix, isEdit) {
    const titles = {
        tuitionFee: 'tuitionFeeModalTitle',
        studentTuition: 'studentTuitionModalTitle',
        payment: 'paymentModalTitle'
    };

    const buttons = {
        tuitionFee: 'tuitionFeeSubmitButton',
        studentTuition: 'studentTuitionSubmitButton',
        payment: 'paymentSubmitButton'
    };

    const createTitles = {
        tuitionFee: 'Thêm định mức học phí',
        studentTuition: 'Thêm học phí sinh viên',
        payment: 'Ghi nhận thanh toán'
    };

    const editTitles = {
        tuitionFee: 'Sửa định mức học phí',
        studentTuition: 'Sửa học phí sinh viên',
        payment: 'Sửa giao dịch'
    };

    const titleElement = document.getElementById(titles[prefix]);

    if (titleElement) {
        titleElement.textContent = isEdit
            ? editTitles[prefix]
            : createTitles[prefix];
    }

    const buttonElement = document.getElementById(buttons[prefix]);

    if (buttonElement) {
        buttonElement.textContent = isEdit
            ? 'Lưu thay đổi'
            : 'Thêm mới';
    }
}

function getValue(id) {
    const element = document.getElementById(id);

    return element
        ? element.value.trim()
        : '';
}

function getChecked(id) {
    const element = document.getElementById(id);

    return element
        ? element.checked
        : false;
}

function setValue(id, value) {
    const element = document.getElementById(id);

    if (!element) {
        return;
    }

    element.value = value === null || value === undefined
        ? ''
        : value;
}

function toNumberOrNull(value) {
    if (value === null || value === undefined || String(value).trim() === '') {
        return null;
    }

    const number = Number(value);

    return Number.isNaN(number) ? null : number;
}

function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
}

function formatNumber(value) {
    return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }

    return escapeHtml(
        String(value).slice(0, 16).replace('T', ' ')
    );
}

/** Rút gọn mã UUID (dùng cho mã giao dịch trên biên lai). */
function renderUuid(value) {
    if (!value) {
        return '-';
    }

    const full = String(value);

    const short = full.length > 8
        ? `${full.slice(0, 8)}…`
        : full;

    return `<span title="${escapeHtml(full)}">${escapeHtml(short)}</span>`;
}

function renderNameCode(name, code) {
    const safeName = (name || '').trim();
    const safeCode = (code || '').trim();

    if (!safeName && !safeCode) {
        return '-';
    }

    return escapeHtml(
        safeName
            ? `${safeName}${safeCode ? ` (${safeCode})` : ''}`
            : safeCode
    );
}

function renderProgramCell(programName, courseYear) {
    const safeName = (programName || '').trim();
    const safeYear = (courseYear || '').trim();

    if (!safeName && !safeYear) {
        return '-';
    }

    return escapeHtml(
        safeYear
            ? `${safeName || 'Chưa rõ chương trình'} - ${safeYear}`
            : safeName
    );
}

/** Diễn giải tiền gốc: số tín chỉ × đơn giá (+ học phí cơ bản). */
function renderRawAmountDetail(item) {
    const credits = Number(item.totalCredits || 0);
    const price = Number(item.pricePerCredit || 0);
    const base = Number(item.baseTuition || 0);

    let detail = `${credits} TC × ${formatCurrency(price)}`;

    if (base > 0) {
        detail += ` + ${formatCurrency(base)}`;
    }

    return escapeHtml(detail);
}

function renderPaymentMethod(method) {
    if (Number(method) === 1) {
        return 'Chuyển khoản';
    }

    if (Number(method) === 2) {
        return 'Tiền mặt';
    }

    if (Number(method) === 3) {
        return 'Ví điện tử';
    }

    return '-';
}

function renderStudentStatusBadge(status) {
    if (!status) {
        return '-';
    }

    const classes = {
        PAID: 'text-bg-success',
        PARTIAL: 'text-bg-warning',
        DEBT: 'text-bg-secondary',
        OVERDUE: 'text-bg-danger'
    };

    const className = classes[status] || 'text-bg-secondary';

    return `<span class="badge ${className}">${escapeHtml(status)}</span>`;
}

function renderPaymentStatusBadge(status) {
    if (!status) {
        return '-';
    }

    const classes = {
        SUCCESS: 'text-bg-success',
        PENDING: 'text-bg-warning',
        FAILED: 'text-bg-danger',
        REFUNDED: 'text-bg-info',
        CANCELLED: 'text-bg-secondary'
    };

    const className = classes[status] || 'text-bg-secondary';

    return `<span class="badge ${className}">${escapeHtml(status)}</span>`;
}

function renderActiveBadge(isActive) {
    if (isActive === true) {
        return '<span class="badge text-bg-success">Đang hiệu lực</span>';
    }

    return '<span class="badge text-bg-secondary">Ngừng hiệu lực</span>';
}

function renderLoadingRow(colspan) {
    return `
        <tr>
            <td colspan="${colspan}" class="text-center text-muted py-4">
                Đang tải dữ liệu...
            </td>
        </tr>
    `;
}

function renderEmptyRow(colspan) {
    return `
        <tr>
            <td colspan="${colspan}" class="text-center text-muted py-4">
                Không có dữ liệu
            </td>
        </tr>
    `;
}

function renderErrorRow(colspan, message) {
    return `
        <tr>
            <td colspan="${colspan}" class="text-center text-danger py-4">
                ${escapeHtml(message)}
            </td>
        </tr>
    `;
}

async function readErrorMessage(response, fallback) {
    try {
        const data = await response.json();

        if (data && data.message) {
            return data.message;
        }
    } catch (error) {
        // bỏ qua, dùng thông báo mặc định
    }

    return fallback;
}

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
