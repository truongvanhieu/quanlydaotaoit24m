import { API_ENDPOINTS, buildHeaders } from './config.js';

// Lưu cache danh sách học phí sinh viên để phục vụ nút "Thu tiền"
let studentTuitionItems = [];
let originalTuitionFeePrice = null;
let originalTuitionFeeBase = null;

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
    const syncTuitionButton = document.getElementById('syncTuitionButton');
    const addPaymentButton = document.getElementById('addPaymentButton');
    const summaryCards = document.getElementById('summaryCards');

    if (summaryCards) {
        summaryCards.addEventListener('click', (event) => {
            const card = event.target.closest('[data-summary-action]');
            if (card) {
                openSummaryDetail(card.dataset.summaryAction);
            }
        });

        summaryCards.addEventListener('keydown', (event) => {
            const card = event.target.closest('[data-summary-action]');
            if (card && (event.key === 'Enter' || event.key === ' ')) {
                event.preventDefault();
                openSummaryDetail(card.dataset.summaryAction);
            }
        });
    }

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

    if (syncTuitionButton) {
        syncTuitionButton.addEventListener('click', syncTuitionFees);
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

    // Tiền mặt chỉ được ghi nhận THÀNH CÔNG -> giới hạn lại hộp chọn trạng thái
    const payPaymentMethodField = document.getElementById('payPaymentMethod');

    if (payPaymentMethodField) {
        payPaymentMethodField.addEventListener('change', applyPaymentStatusRule);
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

    // Nút nhập Excel (dùng chung 1 input file ẩn)
    const importFileInput = document.getElementById('importFileInput');

    document.querySelectorAll('[data-import]').forEach((button) => {
        button.addEventListener('click', () => {
            if (!importFileInput) {
                return;
            }

            importFileInput.dataset.importType = button.dataset.import;
            importFileInput.value = '';
            importFileInput.click();
        });
    });

    if (importFileInput) {
        importFileInput.addEventListener('change', () => {
            const type = importFileInput.dataset.importType;

            if (importFileInput.files && importFileInput.files.length > 0) {
                importExcel(type, importFileInput.files[0]);
            }

            importFileInput.value = '';
        });
    }

    // Tab báo cáo công nợ: tải dữ liệu khi bấm "Tải lại"
    const reloadReportButton = document.getElementById('reloadReportButton');

    if (reloadReportButton) {
        reloadReportButton.addEventListener('click', () => {
            loadSemesterReports();
        });
    }

    // Bấm vào tên học kỳ trong báo cáo công nợ -> xổ danh sách sinh viên còn nợ
    const semesterReportBody = document.getElementById('semesterReportTableBody');

    if (semesterReportBody) {
        semesterReportBody.addEventListener('click', (event) => {
            const toggleButton = event.target.closest('[data-debtor-toggle]');

            if (toggleButton) {
                toggleSemesterDebtors(toggleButton);
            }
        });
    }

    // In biên lai
    const printReceiptButton = document.getElementById('printReceiptButton');

    if (printReceiptButton) {
        printReceiptButton.addEventListener('click', printReceipt);
    }

    // Tab công nợ & nhắc nợ
    const reloadDebtorsButton = document.getElementById('reloadDebtorsButton');
    const remindAllButton = document.getElementById('remindAllButton');
    const debtorKeyword = document.getElementById('debtorKeyword');

    if (reloadDebtorsButton) {
        reloadDebtorsButton.addEventListener('click', loadDebtors);
    }

    if (remindAllButton) {
        remindAllButton.addEventListener('click', remindAllOverdue);
    }

    if (debtorKeyword) {
        debtorKeyword.addEventListener('input', renderDebtors);
    }

    // Tải danh mục cho các hộp chọn + cả 3 danh sách ngay khi mở trang
    loadLookupOptions();
    loadTuitionFees();
    loadStudentTuitions();
    loadPayments();
    loadSummary();
    loadSemesterReports();

    // Chức năng mở rộng: tổng quan biểu đồ + công nợ
    loadDashboard();
    loadDebtors();
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
        originalTuitionFeePrice = Number(item.pricePerCredit);
        originalTuitionFeeBase = item.baseTuition === null || item.baseTuition === undefined
            ? null
            : Number(item.baseTuition);
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
        originalTuitionFeePrice = null;
        originalTuitionFeeBase = null;

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
        const normalizedBase = payload.baseTuition === null ? null : Number(payload.baseTuition);
        const amountChanged = isEdit
            && (originalTuitionFeePrice !== Number(payload.pricePerCredit)
                || originalTuitionFeeBase !== normalizedBase);

        const url = isEdit
            ? `${API_ENDPOINTS.TUITION_FEES}/${encodeURIComponent(id)}`
            : API_ENDPOINTS.TUITION_FEES;

        const response = await fetch(url, {
            method: isEdit ? 'PUT' : 'POST',

            headers: buildHeaders({
                'Content-Type': 'application/json'
            }),

            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        closeModal('tuitionFeeModal');

        alert(
            isEdit
                ? amountChanged
                    ? 'Cập nhật đơn giá và tự động đồng bộ học phí, công nợ thành công'
                    : 'Cập nhật định mức học phí thành công'
                : 'Thêm định mức học phí thành công'
        );

        await loadTuitionFees(getValue('keyword'));
        if (amountChanged) {
            await Promise.all([
                loadLookupOptions(),
                loadStudentTuitions(),
                loadPayments(),
                loadSummary(),
                loadSemesterReports()
            ]);
        }
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
                method: 'DELETE',
                headers: buildHeaders()
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
        tableBody.innerHTML = renderLoadingRow(14);

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
            14,
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
        tableBody.innerHTML = renderEmptyRow(14);
        return;
    }

    tableBody.innerHTML = items
        .map((item) => {
            return `
                <tr>
                    <td>
                        ${renderStudentLink(item)}
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
                        ${formatCurrency(item.overpaidAmount)}
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

            headers: buildHeaders({
                'Content-Type': 'application/json'
            }),

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
                method: 'DELETE',
                headers: buildHeaders()
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

async function syncTuitionFees() {
    const confirmed = window.confirm(
        'Bạn có chắc muốn đồng bộ lại học phí, công nợ và báo cáo toàn hệ thống? Các giao dịch thực thu sẽ được giữ nguyên; khoản thu vượt mức mới sẽ được ghi nhận riêng.'
    );

    if (!confirmed) {
        return;
    }

    const button = document.getElementById('syncTuitionButton');
    const originalHtml = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang đồng bộ...';

    try {
        const response = await fetch(`${API_ENDPOINTS.STUDENT_TUITIONS}/recalculate`, {
            method: 'POST',
            headers: buildHeaders()
        });

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        alert('Đồng bộ học phí hệ thống thành công');

        await Promise.all([
            loadLookupOptions(),
            loadStudentTuitions(),
            loadPayments(),
            loadSummary(),
            loadSemesterReports()
        ]);
    } catch (error) {
        console.error('Lỗi khi đồng bộ học phí:', error);
        alert(error.message || 'Đồng bộ học phí thất bại');
    } finally {
        button.disabled = false;
        button.innerHTML = originalHtml;
    }
}

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
        const transactionRef = getValue('transactionRef');

        if (tuitionId) {
            params.append('tuitionId', tuitionId);
        }

        if (status) {
            params.append('status', status);
        }

        if (method) {
            params.append('method', method);
        }

        if (transactionRef) {
            params.append('transactionRef', transactionRef);
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

                            ${renderPaymentStateButtons(item)}

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

/** Nút thao tác theo trạng thái: PENDING -> Duyệt/Từ chối, SUCCESS -> Hoàn tiền. */
function renderPaymentStateButtons(item) {
    const status = item.paymentStatus;
    const id = escapeHtml(item.id);

    if (status === 'PENDING') {
        return `
            <button
                type="button"
                class="btn btn-success btn-sm"
                title="Duyệt"
                onclick="approvePayment('${id}')"
            >
                <i class="bi bi-check-circle"></i>
            </button>

            <button
                type="button"
                class="btn btn-outline-danger btn-sm"
                title="Từ chối"
                onclick="rejectPayment('${id}')"
            >
                <i class="bi bi-slash-circle"></i>
            </button>
        `;
    }

    if (status === 'SUCCESS') {
        return `
            <button
                type="button"
                class="btn btn-info btn-sm"
                title="Hoàn tiền"
                onclick="refundPayment('${id}')"
            >
                <i class="bi bi-arrow-counterclockwise"></i>
            </button>
        `;
    }

    return '';
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

    applyPaymentStatusRule();
    showModal('paymentModal');
}

/**
 * Tiền mặt (2) được thu trực tiếp nên chỉ có THÀNH CÔNG.
 * PENDING / FAILED chỉ dành cho chuyển khoản (1) hoặc ví điện tử (3).
 */
function applyPaymentStatusRule() {
    const methodField = document.getElementById('payPaymentMethod');
    const statusField = document.getElementById('payPaymentStatus');

    if (!methodField || !statusField) {
        return;
    }

    const isCash = methodField.value === '2';

    // Tiền mặt chỉ thu trực tiếp nên trạng thái hợp lệ là SUCCESS;
    // REFUNDED vẫn được giữ để không vô hiệu hóa giao dịch đã hoàn tiền.
    Array.from(statusField.options).forEach((option) => {
        const allowed = !isCash
            || option.value === 'SUCCESS'
            || option.value === 'REFUNDED';
        option.disabled = !allowed;
        option.hidden = !allowed;
    });

    if (isCash && statusField.value !== 'REFUNDED') {
        statusField.value = 'SUCCESS';
    }
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

    // Tiền mặt thu trực tiếp nên chỉ ghi nhận THÀNH CÔNG (cho phép giữ REFUNDED khi đã hoàn tiền)
    if (payload.paymentMethod === 2
        && payload.paymentStatus !== 'SUCCESS'
        && payload.paymentStatus !== 'REFUNDED') {
        payload.paymentStatus = 'SUCCESS';
    }

    try {
        const isEdit = Boolean(id);

        const url = isEdit
            ? `${API_ENDPOINTS.PAYMENTS}/${encodeURIComponent(id)}`
            : API_ENDPOINTS.PAYMENTS;

        const response = await fetch(url, {
            method: isEdit ? 'PUT' : 'POST',

            headers: buildHeaders({
                'Content-Type': 'application/json'
            }),

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
        await loadSummary();
        await loadDashboard();
        await loadDebtors();
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
                method: 'DELETE',
                headers: buildHeaders()
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
        await loadSummary();
        await loadDashboard();
        await loadDebtors();
    } catch (error) {
        console.error('Lỗi khi hủy giao dịch thanh toán:', error);

        alert(error.message || 'Hủy giao dịch thanh toán thất bại');
    }
};

/** Gọi API đổi trạng thái giao dịch (approve/reject/refund) rồi tải lại dữ liệu. */
async function changePaymentState(id, action, successMessage) {
    try {
        const response = await fetch(
            `${API_ENDPOINTS.PAYMENTS}/${encodeURIComponent(id)}/${action}`,
            {
                method: 'POST',
                headers: buildHeaders()
            }
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        alert(successMessage);

        await loadPayments();
        await loadStudentTuitions();
        await loadLookupOptions();
        await loadSummary();
        await loadDashboard();
        await loadDebtors();
        if (document.getElementById('overpaidModal')?.classList.contains('show')) {
            await loadOverpaidDetails();
        }
    } catch (error) {
        console.error(`Lỗi khi ${action} giao dịch thanh toán:`, error);

        alert(error.message || 'Cập nhật trạng thái giao dịch thất bại');
    }
}

window.approvePayment = async function (id) {
    if (!window.confirm('Duyệt giao dịch này (PENDING → SUCCESS)?')) {
        return;
    }

    await changePaymentState(id, 'approve', 'Duyệt giao dịch thành công');
};

window.rejectPayment = async function (id) {
    if (!window.confirm('Từ chối giao dịch này (PENDING → FAILED)?')) {
        return;
    }

    await changePaymentState(id, 'reject', 'Từ chối giao dịch thành công');
};

window.refundPayment = async function (id) {
    if (!window.confirm('Hoàn tiền giao dịch này (SUCCESS → REFUNDED)?')) {
        return;
    }

    await changePaymentState(id, 'refund', 'Hoàn tiền giao dịch thành công');
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
            variant: 'primary',
            action: 'all-tuitions'
        },
        {
            label: 'Đã hoàn tất (PAID)',
            value: formatNumber(data.paidCount),
            icon: 'bi-check2-circle',
            variant: 'success',
            action: 'paid-tuitions'
        },
        {
            label: 'Sinh viên còn nợ',
            value: formatNumber(data.debtorCount),
            icon: 'bi-exclamation-triangle',
            variant: 'danger',
            action: 'debtors'
        },
        {
            label: 'Tổng phải thu',
            value: `${formatCurrency(data.totalNet)} đ`,
            icon: 'bi-cash-stack',
            variant: 'info',
            action: 'all-tuitions'
        },
        {
            label: 'Đã thu',
            value: `${formatCurrency(data.totalPaid)} đ`,
            icon: 'bi-cash-coin',
            variant: 'success',
            action: 'successful-payments'
        },
        {
            label: 'Khoản thu vượt',
            value: `${formatCurrency(data.totalOverpaid)} đ`,
            icon: 'bi-wallet2',
            variant: 'info',
            action: 'overpaid'
        },
        {
            label: 'Còn nợ',
            value: `${formatCurrency(data.totalDebt)} đ`,
            icon: 'bi-exclamation-octagon',
            variant: 'warning',
            action: 'debtors'
        }
    ];

    container.innerHTML = cards
        .map((card) => `
            <div class="col-sm-6 col-xl-4">
                <div
                    class="card stat-card stat-action-card stat-${card.variant}"
                    data-summary-action="${card.action}"
                    role="button"
                    tabindex="0"
                    aria-label="Xem chi tiết: ${escapeHtml(card.label)}"
                    title="Bấm để xem chi tiết"
                >
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="stat-icon">
                            <i class="bi ${card.icon}"></i>
                        </span>
                        <div class="flex-grow-1 min-w-0">
                            <div class="stat-label">
                                ${escapeHtml(card.label)}
                            </div>
                            <div class="stat-value text-truncate" title="${escapeHtml(card.value)}">
                                ${card.value}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `)
        .join('');

    // Tỷ lệ thu học phí: hiển thị dạng thanh tiến độ
    if (data.collectionRate !== null && data.collectionRate !== undefined) {
        const rate = Number(data.collectionRate) || 0;
        const percent = Math.min(100, Math.max(0, rate));

        const note = document.createElement('div');
        note.className = 'col-12';
        note.innerHTML = `
            <div class="card">
                <div class="card-body py-3">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="stat-label">
                            Tỷ lệ thu học phí
                        </span>
                        <span class="fw-bold">
                            ${formatNumber(rate)}%
                        </span>
                    </div>
                    <div class="progress" style="height: 0.5rem;" role="progressbar" aria-label="Tỷ lệ thu học phí" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100">
                        <div class="progress-bar bg-success" style="width: ${percent}%"></div>
                    </div>
                </div>
            </div>
        `;
        container.appendChild(note);
    }
}

async function openSummaryDetail(action) {
    if (action === 'overpaid') {
        showModal('overpaidModal');
        await loadOverpaidDetails();
        return;
    }

    if (action === 'debtors') {
        showTuitionTab('tab-debtors');
        await loadDebtors();
        return;
    }

    if (action === 'successful-payments') {
        setValue('tuitionId', '');
        setValue('paymentStatus', 'SUCCESS');
        setValue('method', '');
        setValue('transactionRef', '');
        showTuitionTab('tab-payments');
        await loadPayments();
        return;
    }

    setValue('studentId', '');
    setValue('semesterId', '');
    setValue('studentTuitionStatus', action === 'paid-tuitions' ? '1' : '');
    showTuitionTab('tab-student-tuitions');
    await loadStudentTuitions();
}

function showTuitionTab(tabId) {
    const tab = document.getElementById(tabId);
    if (tab && window.bootstrap?.Tab) {
        window.bootstrap.Tab.getOrCreateInstance(tab).show();
    }
}

async function loadOverpaidDetails() {
    const container = document.getElementById('overpaidContent');
    if (!container) {
        return;
    }

    container.innerHTML = '<div class="text-muted">Đang tải danh sách khoản thu vượt...</div>';

    try {
        const paymentParams = new URLSearchParams({ status: 'SUCCESS' });
        const [tuitionResponse, paymentResponse] = await Promise.all([
            fetch(API_ENDPOINTS.STUDENT_TUITIONS),
            fetch(`${API_ENDPOINTS.PAYMENTS}?${paymentParams.toString()}`)
        ]);

        if (!tuitionResponse.ok) {
            throw new Error(await readErrorMessage(tuitionResponse, `HTTP ${tuitionResponse.status}`));
        }
        if (!paymentResponse.ok) {
            throw new Error(await readErrorMessage(paymentResponse, `HTTP ${paymentResponse.status}`));
        }

        const [tuitions, payments] = await Promise.all([
            tuitionResponse.json(),
            paymentResponse.json()
        ]);
        const overpaidTuitions = (Array.isArray(tuitions) ? tuitions : [])
            .filter((item) => Number(item.overpaidAmount || 0) > 0);
        const successfulPayments = Array.isArray(payments) ? payments : [];

        if (overpaidTuitions.length === 0) {
            container.innerHTML = `
                <div class="alert alert-success mb-0">
                    Hiện không có khoản học phí nào nộp vượt.
                </div>
            `;
            return;
        }

        container.innerHTML = overpaidTuitions.map((tuition) => {
            const tuitionPayments = successfulPayments.filter(
                (payment) => String(payment.tuitionId) === String(tuition.id)
            );
            const paymentRows = tuitionPayments.length
                ? tuitionPayments.map((payment) => `
                    <div class="overpaid-payment-row d-flex flex-wrap align-items-center justify-content-between gap-2 py-2">
                        <div class="min-w-0">
                            <div class="fw-semibold">${formatCurrency(payment.amountPaid)} đ</div>
                            <div class="small text-muted">
                                ${formatDateTime(payment.paymentDate)}
                                · ${escapeHtml(renderPaymentMethod(payment.paymentMethod))}
                                · ${escapeHtml(payment.transactionRef || 'Không có mã tham chiếu')}
                            </div>
                        </div>
                        <div class="text-end">
                            <button
                                type="button"
                                class="btn btn-info btn-sm"
                                onclick="refundPayment('${escapeHtml(payment.id)}')"
                                title="Hoàn toàn bộ giao dịch này"
                            >
                                <i class="bi bi-arrow-counterclockwise me-1"></i>Hoàn tiền
                            </button>
                        </div>
                    </div>
                `).join('')
                : '<div class="small text-muted py-2">Không có giao dịch SUCCESS để hoàn.</div>';

            return `
                <section class="overpaid-item border rounded-3 p-3 mb-3">
                    <div class="d-flex flex-wrap justify-content-between gap-2 mb-2">
                        <div>
                            <div class="fw-semibold">${renderNameCode(tuition.studentName, tuition.studentCode)}</div>
                            <div class="small text-muted">
                                ${escapeHtml(tuition.semesterName || '-')} · ${escapeHtml(tuition.programName || '-')}
                            </div>
                        </div>
                        <div class="text-md-end">
                            <div class="small text-muted">Khoản nộp vượt</div>
                            <div class="fw-bold text-info">${formatCurrency(tuition.overpaidAmount)} đ</div>
                        </div>
                    </div>
                    <div class="small text-muted mb-2">
                        Phải thu ${formatCurrency(tuition.netAmount)} đ · Đã nộp ${formatCurrency(tuition.paidAmount)} đ
                    </div>
                    <div>${paymentRows}</div>
                </section>
            `;
        }).join('');
    } catch (error) {
        console.error('Lỗi khi tải danh sách khoản thu vượt:', error);
        container.innerHTML = `
            <div class="alert alert-danger mb-0">
                ${escapeHtml(error.message || 'Không thể tải danh sách khoản thu vượt')}
            </div>
        `;
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
        tableBody.innerHTML = renderLoadingRow(8);

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
            8,
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
        tableBody.innerHTML = renderEmptyRow(8);
        return;
    }

    tableBody.innerHTML = items
        .map((item) => {
            const rate = computeRate(
                Number(item.totalPaid || 0) - Number(item.totalOverpaid || 0),
                item.totalNet
            );

            const debtorCount = Number(item.debtorCount || 0);

            const semesterCell = debtorCount > 0
                ? `<button
                        type="button"
                        class="btn btn-link btn-sm p-0 text-decoration-none"
                        data-debtor-toggle="${escapeHtml(item.semesterId)}"
                        title="Xem danh sách sinh viên còn nợ"
                    >
                        <i class="bi bi-chevron-right me-1"></i>${escapeHtml(item.semesterName)}
                    </button>`
                : escapeHtml(item.semesterName);

            return `
                <tr>
                    <td>
                        ${semesterCell}
                    </td>

                    <td class="text-end">
                        ${formatNumber(item.studentCount)}
                    </td>

                    <td class="text-end">
                        ${debtorCount > 0
                            ? `<span class="badge text-bg-danger">${formatNumber(debtorCount)}</span>`
                            : '<span class="badge text-bg-success">0</span>'}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalNet)}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalPaid)}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalOverpaid)}
                    </td>

                    <td class="text-end">
                        ${formatCurrency(item.totalDebt)}
                    </td>

                    <td class="text-end">
                        ${formatNumber(rate)}%
                    </td>
                </tr>
                ${debtorCount > 0 ? renderDebtorDetailRow(item) : ''}
            `;
        })
        .join('');
}

/** Hàng chi tiết (ẩn) chứa danh sách sinh viên còn nợ của một học kỳ. */
function renderDebtorDetailRow(item) {
    return `
        <tr class="d-none" data-debtor-row="${escapeHtml(item.semesterId)}">
            <td colspan="8" class="bg-body-tertiary">
                <div class="text-muted small py-2">
                    Đang tải danh sách sinh viên còn nợ...
                </div>
            </td>
        </tr>
    `;
}

/** Bật/tắt hàng chi tiết; lần đầu mở sẽ tải danh sách sinh viên còn nợ. */
async function toggleSemesterDebtors(button) {
    const semesterId = button.dataset.debtorToggle;

    const detailRow = document.querySelector(
        `[data-debtor-row="${semesterId}"]`
    );

    if (!detailRow) {
        return;
    }

    const icon = button.querySelector('i');

    const isOpening = detailRow.classList.contains('d-none');

    if (!isOpening) {
        detailRow.classList.add('d-none');

        if (icon) {
            icon.classList.replace('bi-chevron-down', 'bi-chevron-right');
        }

        return;
    }

    detailRow.classList.remove('d-none');

    if (icon) {
        icon.classList.replace('bi-chevron-right', 'bi-chevron-down');
    }

    if (detailRow.dataset.loaded !== 'true') {
        await loadSemesterDebtors(semesterId, detailRow);
    }
}

/** Tải danh sách sinh viên còn nợ (DEBT/OVERDUE) của một học kỳ. */
async function loadSemesterDebtors(semesterId, detailRow) {
    const cell = detailRow.firstElementChild;

    if (!cell) {
        return;
    }

    try {
        cell.innerHTML = `
            <div class="text-muted small py-2">
                Đang tải danh sách sinh viên còn nợ...
            </div>
        `;

        const params = new URLSearchParams({ semesterId });

        const response = await fetch(
            `${API_ENDPOINTS.STUDENT_TUITIONS}?${params.toString()}`
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const items = await response.json();

        const debtors = (Array.isArray(items) ? items : []).filter((item) => {
            const status = Number(item.status);

            return status === 3 || status === 4;
        });

        detailRow.dataset.loaded = 'true';

        cell.innerHTML = renderSemesterDebtors(debtors);
    } catch (error) {
        console.error('Lỗi khi tải danh sách sinh viên còn nợ:', error);

        cell.innerHTML = `
            <div class="text-danger small py-2">
                ${escapeHtml(error.message || 'Không thể tải danh sách sinh viên còn nợ')}
            </div>
        `;
    }
}

/** Bảng con: danh sách sinh viên còn nợ của một học kỳ. */
function renderSemesterDebtors(debtors) {
    if (!debtors || debtors.length === 0) {
        return `
            <div class="text-muted small py-2">
                Học kỳ này không có sinh viên nào còn nợ.
            </div>
        `;
    }

    const totalDebt = debtors.reduce(
        (sum, item) => sum + Number(item.debtAmount || 0),
        0
    );

    const rows = debtors
        .map((item) => `
            <tr>
                <td>
                    ${renderNameCode(item.studentName, item.studentCode)}
                </td>

                <td>
                    ${renderProgramCell(item.programName, item.courseYear)}
                </td>

                <td class="text-end">
                    ${formatCurrency(item.netAmount)}
                </td>

                <td class="text-end">
                    ${formatCurrency(item.paidAmount)}
                </td>

                <td class="text-end text-danger fw-semibold">
                    ${formatCurrency(item.debtAmount)}
                </td>

                <td>
                    ${renderStudentStatusBadge(item.statusName || item.status)}
                </td>

                <td>
                    ${escapeHtml(item.deadline || '-')}
                </td>
            </tr>
        `)
        .join('');

    return `
        <div class="p-3">
            <div class="small fw-semibold mb-2">
                Danh sách sinh viên còn nợ (${formatNumber(debtors.length)})
                – Tổng còn nợ:
                <span class="text-danger">
                    ${formatCurrency(totalDebt)}
                </span>
            </div>

            <div class="table-responsive">
                <table class="table table-sm table-bordered align-middle bg-body mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>
                            Sinh viên
                        </th>
                        <th>
                            Chương trình
                        </th>
                        <th class="text-end">
                            Phải thu
                        </th>
                        <th class="text-end">
                            Đã thu
                        </th>
                        <th class="text-end">
                            Còn nợ
                        </th>
                        <th>
                            Trạng thái
                        </th>
                        <th>
                            Hạn nộp
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        </div>
    `;
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

const IMPORT_LABELS = {
    'tuition-fees': 'định mức học phí',
    'student-tuitions': 'học phí sinh viên',
    'payments': 'giao dịch thanh toán'
};

/** Nhập danh sách từ file Excel (cùng định dạng với file xuất). */
async function importExcel(type, file) {
    if (!type || !file) {
        return;
    }

    const label = IMPORT_LABELS[type] || type;

    try {
        const formData = new FormData();
        formData.append('type', type);
        formData.append('file', file);

        const response = await fetch(
            `${API_ENDPOINTS.TUITION_REPORTS}/import`,
            {
                method: 'POST',
                headers: buildHeaders(),
                body: formData
            }
        );

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const result = await response.json();

        alert(
            result && typeof result.imported === 'number'
                ? `Nhập ${label} thành công ${result.imported} dòng`
                : `Nhập ${label} thành công`
        );

        // Tải lại toàn bộ dữ liệu liên quan sau khi nhập
        await Promise.all([
            loadTuitionFees(getValue('keyword')),
            loadLookupOptions(),
            loadStudentTuitions(),
            loadPayments(),
            loadSummary(),
            loadSemesterReports()
        ]);
    } catch (error) {
        console.error('Lỗi khi nhập Excel:', error);

        alert(error.message || `Nhập ${label} thất bại`);
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

// =========================
// TỔNG QUAN (DASHBOARD)
// =========================

const CHART_COLORS = [
    '#0d6efd', '#198754', '#ffc107', '#dc3545', '#0dcaf0',
    '#6f42c1', '#fd7e14', '#20c997', '#d63384', '#6c757d'
];

const dashboardCharts = {};

/** Tải dữ liệu tổng quan và vẽ lại các biểu đồ. */
async function loadDashboard() {
    try {
        const response = await fetch(`${API_ENDPOINTS.TUITION_REPORTS}/dashboard`);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const data = await response.json();

        renderRevenueChart(data.monthlyRevenue || []);
        renderCollectionChart(data.summary || {});
        renderMethodChart(data.paymentMethods || []);
        renderDebtChart(data.debtBySemester || []);
    } catch (error) {
        console.error('Lỗi khi tải dữ liệu tổng quan:', error);
    }
}

function destroyChart(key) {
    if (dashboardCharts[key]) {
        dashboardCharts[key].destroy();
        dashboardCharts[key] = null;
    }
}

function renderRevenueChart(items) {
    const canvas = document.getElementById('revenueChart');

    if (!canvas || !window.Chart) {
        return;
    }

    destroyChart('revenue');

    dashboardCharts.revenue = new window.Chart(canvas, {
        type: 'bar',
        data: {
            labels: items.map((item) => item.label),
            datasets: [{
                label: 'Doanh thu',
                data: items.map((item) => Number(item.amount || 0)),
                backgroundColor: '#0d6efd',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (context) => `${formatCurrency(context.parsed.y)} đ`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: (value) => formatCurrency(value) }
                }
            }
        }
    });
}

function renderCollectionChart(summary) {
    const canvas = document.getElementById('collectionChart');

    if (!canvas || !window.Chart) {
        return;
    }

    destroyChart('collection');

    dashboardCharts.collection = new window.Chart(canvas, {
        type: 'doughnut',
        data: {
            labels: ['Đã thu', 'Còn nợ'],
            datasets: [{
                data: [Number(summary.totalPaid || 0), Number(summary.totalDebt || 0)],
                backgroundColor: ['#198754', '#dc3545']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: (context) => `${context.label}: ${formatCurrency(context.parsed)} đ`
                    }
                }
            }
        }
    });
}

function renderMethodChart(items) {
    const canvas = document.getElementById('methodChart');

    if (!canvas || !window.Chart) {
        return;
    }

    destroyChart('method');

    dashboardCharts.method = new window.Chart(canvas, {
        type: 'pie',
        data: {
            labels: items.map((item) => item.methodName),
            datasets: [{
                data: items.map((item) => Number(item.amount || 0)),
                backgroundColor: CHART_COLORS
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: (context) => `${context.label}: ${formatCurrency(context.parsed)} đ`
                    }
                }
            }
        }
    });
}

function renderDebtChart(items) {
    const canvas = document.getElementById('debtChart');

    if (!canvas || !window.Chart) {
        return;
    }

    destroyChart('debt');

    dashboardCharts.debt = new window.Chart(canvas, {
        type: 'bar',
        data: {
            labels: items.map((item) => item.semesterName),
            datasets: [{
                label: 'Còn nợ',
                data: items.map((item) => Number(item.totalDebt || 0)),
                backgroundColor: '#dc3545',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (context) => `${formatCurrency(context.parsed.y)} đ`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: (value) => formatCurrency(value) }
                }
            }
        }
    });
}

// =========================
// CÔNG NỢ & NHẮC NỢ
// =========================

let debtorItems = [];

/** Tải danh sách các khoản học phí còn nợ / quá hạn. */
async function loadDebtors() {
    const tableBody = document.getElementById('debtorTableBody');

    if (!tableBody) {
        return;
    }

    try {
        tableBody.innerHTML = renderLoadingRow(10);

        const response = await fetch(`${API_ENDPOINTS.TUITION_REPORTS}/debtors`);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        debtorItems = await response.json();

        renderDebtors();
    } catch (error) {
        console.error('Lỗi khi tải danh sách công nợ:', error);

        tableBody.innerHTML = renderErrorRow(10, error.message || 'Không thể tải danh sách công nợ');
    }
}

function renderDebtors() {
    const tableBody = document.getElementById('debtorTableBody');

    if (!tableBody) {
        return;
    }

    const keyword = getValue('debtorKeyword').toLowerCase();

    const items = debtorItems.filter((item) => {
        if (!keyword) {
            return true;
        }

        return (item.studentName || '').toLowerCase().includes(keyword)
            || (item.studentCode || '').toLowerCase().includes(keyword);
    });

    if (items.length === 0) {
        tableBody.innerHTML = renderEmptyRow(10);
        return;
    }

    tableBody.innerHTML = items.map((item) => `
        <tr>
            <td>
                ${renderStudentLink(item)}
            </td>
            <td>
                <div class="small">${escapeHtml(item.email || '-')}</div>
                <div class="small text-muted">${escapeHtml(item.phone || '-')}</div>
            </td>
            <td>
                ${escapeHtml(item.semesterName || '-')}
            </td>
            <td class="text-end">
                ${formatCurrency(item.netAmount)}
            </td>
            <td class="text-end">
                ${formatCurrency(item.paidAmount)}
            </td>
            <td class="text-end fw-semibold text-danger">
                ${formatCurrency(item.debtAmount)}
            </td>
            <td>
                ${escapeHtml(item.deadline || '-')}
            </td>
            <td class="text-end">
                ${renderOverdueBadge(item.daysOverdue)}
            </td>
            <td>
                ${renderStudentStatusBadge(item.statusName || item.status)}
            </td>
            <td class="text-end">
                <div class="table-actions d-flex justify-content-end gap-1">
                    <button
                        type="button"
                        class="btn btn-warning btn-sm"
                        title="Nhắc nợ"
                        onclick="remindDebtor('${escapeHtml(item.studentTuitionId)}')"
                    >
                        <i class="bi bi-envelope-paper"></i>
                    </button>
                    <button
                        type="button"
                        class="btn btn-outline-primary btn-sm"
                        title="Chi tiết sinh viên"
                        onclick="openStudentDetail('${escapeHtml(item.studentId)}')"
                    >
                        <i class="bi bi-person-lines-fill"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderOverdueBadge(days) {
    if (days === null || days === undefined) {
        return '<span class="text-muted">-</span>';
    }

    return `<span class="badge text-bg-danger">${formatNumber(days)} ngày</span>`;
}

/** Soạn email nhắc nợ cho một khoản công nợ. */
function remindDebtor(studentTuitionId) {
    const item = debtorItems.find((row) => String(row.studentTuitionId) === String(studentTuitionId));

    if (!item) {
        return;
    }

    if (!item.email) {
        alert(`Sinh viên ${item.studentName || ''} chưa có email liên hệ.`);
        return;
    }

    const subject = encodeURIComponent('[Nhắc nợ học phí] Thông báo thanh toán học phí');
    const body = encodeURIComponent(
        `Kính gửi ${item.studentName} (${item.studentCode || ''}),\n\n`
        + `Học kỳ ${item.semesterName || ''} hiện còn nợ ${formatCurrency(item.debtAmount)} đ `
        + `(hạn chót: ${item.deadline || 'chưa xác định'}).\n\n`
        + 'Vui lòng hoàn tất thanh toán học phí trong thời gian sớm nhất.\n\n'
        + 'Trân trọng.'
    );

    window.location.href = `mailto:${encodeURIComponent(item.email)}?subject=${subject}&body=${body}`;
}

/** Soạn email nhắc nợ cho tất cả các khoản đã quá hạn. */
function remindAllOverdue() {
    const overdue = debtorItems.filter((item) => item.daysOverdue !== null && item.daysOverdue !== undefined);

    if (overdue.length === 0) {
        alert('Không có khoản nào quá hạn để nhắc.');
        return;
    }

    const withEmail = overdue.filter((item) => item.email);

    if (withEmail.length === 0) {
        alert(`Có ${overdue.length} khoản quá hạn nhưng chưa có email liên hệ.`);
        return;
    }

    const subject = encodeURIComponent('[Nhắc nợ học phí] Thông báo thanh toán học phí');
    const lines = withEmail.map((item) =>
        `- ${item.studentName} (${item.studentCode || '-'}) - ${item.semesterName || '-'}: `
        + `còn nợ ${formatCurrency(item.debtAmount)} đ, hạn ${item.deadline || 'N/A'}`);

    const body = encodeURIComponent(
        'Kính gửi Quý sinh viên,\n\n'
        + 'Học phí của Quý sinh viên hiện còn nợ như sau:\n\n'
        + lines.join('\n')
        + '\n\nVui lòng hoàn tất thanh toán trong thời gian sớm nhất.\n\nTrân trọng.'
    );

    window.location.href = `mailto:?bcc=${withEmail.map((item) => item.email).join(',')}&subject=${subject}&body=${body}`;
}

// =========================
// CHI TIẾT SINH VIÊN
// =========================

/** Hiển thị liên kết tên sinh viên (bấm để xem chi tiết). */
function renderStudentLink(item) {
    const name = (item.studentName || '').trim();
    const code = (item.studentCode || '').trim();
    const label = name ? `${name}${code ? ` (${code})` : ''}` : (code || '-');

    if (!item.studentId) {
        return escapeHtml(label);
    }

    return `<a href="#" class="link-primary" onclick="openStudentDetail('${escapeHtml(item.studentId)}'); return false;">${escapeHtml(label)}</a>`;
}

/** Mở modal chi tiết học phí & thanh toán của một sinh viên. */
async function openStudentDetail(studentId) {
    if (!studentId) {
        return;
    }

    const content = document.getElementById('studentDetailContent');

    if (!content) {
        return;
    }

    content.innerHTML = '<div class="text-muted">Đang tải dữ liệu...</div>';

    showModal('studentDetailModal');

    try {
        const response = await fetch(`${API_ENDPOINTS.TUITION_REPORTS}/students/${encodeURIComponent(studentId)}`);

        if (!response.ok) {
            const message = await readErrorMessage(response, `HTTP ${response.status}`);
            throw new Error(message);
        }

        const detail = await response.json();

        renderStudentDetail(detail);
    } catch (error) {
        console.error('Lỗi khi tải chi tiết sinh viên:', error);

        content.innerHTML = `
            <div class="alert alert-danger mb-0">
                ${escapeHtml(error.message || 'Không thể tải chi tiết sinh viên')}
            </div>
        `;
    }
}

function renderStudentDetail(detail) {
    const content = document.getElementById('studentDetailContent');

    if (!content) {
        return;
    }

    const data = detail || {};
    const tuitions = data.tuitions || [];
    const payments = data.payments || [];

    const tuitionRows = tuitions.length === 0
        ? renderEmptyRow(7)
        : tuitions.map((item) => `
            <tr>
                <td>${escapeHtml(item.semesterName || '-')}</td>
                <td>${renderProgramCell(item.programName, item.courseYear)}</td>
                <td class="text-end">${formatCurrency(item.netAmount)}</td>
                <td class="text-end">${formatCurrency(item.paidAmount)}</td>
                <td class="text-end text-danger">${formatCurrency(item.debtAmount)}</td>
                <td>${renderStudentStatusBadge(item.statusName || item.status)}</td>
                <td>${escapeHtml(item.deadline || '-')}</td>
            </tr>
        `).join('');

    const paymentRows = payments.length === 0
        ? renderEmptyRow(5)
        : payments.map((item) => `
            <tr>
                <td>${formatDateTime(item.paymentDate)}</td>
                <td>${escapeHtml(item.semesterName || '-')}</td>
                <td class="text-end">${formatCurrency(item.amountPaid)}</td>
                <td>${escapeHtml(item.paymentMethodName || renderPaymentMethod(item.paymentMethod))}</td>
                <td>${renderPaymentStatusBadge(item.paymentStatus)}</td>
            </tr>
        `).join('');

    content.innerHTML = `
        <div class="d-flex flex-wrap justify-content-between align-items-start mb-3 gap-2">
            <div>
                <h4 class="mb-1">${escapeHtml(data.studentName || '-')}</h4>
                <div class="text-muted">
                    <i class="bi bi-person-badge me-1"></i>${escapeHtml(data.studentCode || '-')}
                    ${data.email ? `&nbsp;·&nbsp;<i class="bi bi-envelope me-1"></i>${escapeHtml(data.email)}` : ''}
                    ${data.phone ? `&nbsp;·&nbsp;<i class="bi bi-telephone me-1"></i>${escapeHtml(data.phone)}` : ''}
                </div>
            </div>
        </div>

        <div class="row g-2 mb-3">
            <div class="col-6 col-md-3">
                <div class="border rounded p-2 h-100">
                    <div class="text-muted small">Số khoản thu</div>
                    <div class="fw-semibold">${formatNumber(data.recordCount)}</div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="border rounded p-2 h-100">
                    <div class="text-muted small">Tổng phải thu</div>
                    <div class="fw-semibold">${formatCurrency(data.totalNet)} đ</div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="border rounded p-2 h-100">
                    <div class="text-muted small">Đã nộp</div>
                    <div class="fw-semibold text-success">${formatCurrency(data.totalPaid)} đ</div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="border rounded p-2 h-100">
                    <div class="text-muted small">Còn nợ</div>
                    <div class="fw-semibold text-danger">${formatCurrency(data.totalDebt)} đ</div>
                </div>
            </div>
        </div>

        <h6 class="mt-4">
            <i class="bi bi-journal-text me-1"></i>Học phí theo học kỳ
        </h6>
        <div class="table-responsive mb-4">
            <table class="table table-sm table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Học kỳ</th>
                    <th>Chương trình</th>
                    <th class="text-end">Phải thu</th>
                    <th class="text-end">Đã nộp</th>
                    <th class="text-end">Còn nợ</th>
                    <th>Trạng thái</th>
                    <th>Hạn chót</th>
                </tr>
                </thead>
                <tbody>${tuitionRows}</tbody>
            </table>
        </div>

        <h6 class="mt-4">
            <i class="bi bi-cash-coin me-1"></i>Lịch sử giao dịch
        </h6>
        <div class="table-responsive">
            <table class="table table-sm table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Ngày thu</th>
                    <th>Học kỳ</th>
                    <th class="text-end">Số tiền</th>
                    <th>Phương thức</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>${paymentRows}</tbody>
            </table>
        </div>
    `;
}

// Hàm gọi từ thuộc tính onclick trong HTML (module ES không tự phơi ra global)
window.remindDebtor = remindDebtor;
window.openStudentDetail = openStudentDetail;

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

function renderRawAmountDetail(item) {
    const credits = Number(item.totalCredits || 0);
    const price = Number(item.pricePerCredit || 0);
    const base = Number(item.baseTuition || 0);

    let detail = `${credits} TC × ${formatCurrency(price)}`;

    if (base > 0) {
        detail += ` + ${formatCurrency(base)} học phí cố định`;
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
