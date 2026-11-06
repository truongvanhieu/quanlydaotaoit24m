import { API_ENDPOINTS } from './config.js';

const classSelect = document.getElementById('courseClassSelect');
const gradeAlert = document.getElementById('gradeAlert');
const componentSection = document.getElementById('componentSection');
const componentForm = document.getElementById('componentForm');
const componentTableBody = document.getElementById('componentTableBody');
const gradeTableHead = document.getElementById('gradeTableHead');
const gradeTableBody = document.getElementById('gradeTableBody');
const weightSummary = document.getElementById('weightSummary');
const addStudentBtn = document.getElementById('addStudentToClassBtn');
const selectStudent = document.getElementById('selectStudent');
const confirmAddStudentBtn = document.getElementById('confirmAddStudentBtn');
const modalAlert = document.getElementById('modalAlert');

const openCreateClassModalBtn = document.getElementById('openCreateClassModalBtn');
const confirmCreateClassBtn = document.getElementById('confirmCreateClassBtn');
const createClassModalAlert = document.getElementById('createClassModalAlert');

let currentGradeBook = null;
let addStudentModalInstance = null;
let createClassModalInstance = null;

document.addEventListener('DOMContentLoaded', async () => {
    classSelect.addEventListener('change', loadGradeBook);
    document.getElementById('toggleComponentForm').addEventListener('click', () => {
        componentSection.hidden = !componentSection.hidden;
        if (!componentSection.hidden) {
            componentForm.hidden = false;
        }
    });
    document.getElementById('cancelComponentEdit').addEventListener('click', resetComponentForm);
    componentForm.addEventListener('submit', saveComponent);
    componentTableBody.addEventListener('click', handleComponentAction);
    gradeTableBody.addEventListener('click', handleGradeAction);
    document.getElementById('exportCsvBtn')?.addEventListener('click', exportCsv);
    document.getElementById('exportWordBtn')?.addEventListener('click', exportWord);
    document.getElementById('exportPdfBtn')?.addEventListener('click', exportPdf);

    // Bắt sự kiện kiểm tra điểm số thời gian thực (báo lỗi liền khi gõ chữ)
    gradeTableBody.addEventListener('input', handleGradeInputRealtime);
    gradeTableBody.addEventListener('change', handleGradeInputRealtime);

    if (addStudentBtn) addStudentBtn.addEventListener('click', openAddStudentModal);
    if (confirmAddStudentBtn) confirmAddStudentBtn.addEventListener('click', handleConfirmAddStudent);
    if (openCreateClassModalBtn) openCreateClassModalBtn.addEventListener('click', openCreateClassModal);
    if (confirmCreateClassBtn) confirmCreateClassBtn.addEventListener('click', handleConfirmCreateClass);

    // Global modal close handlers
    document.addEventListener('click', (e) => {
        const dismissBtn = e.target.closest('[data-bs-dismiss="modal"]');
        if (dismissBtn) {
            const modal = dismissBtn.closest('.modal');
            if (modal) closeModal(modal);
        }
    });

    await loadCourseClasses();
});

function openModal(modalId) {
    const modalEl = typeof modalId === 'string' ? document.getElementById(modalId) : modalId;
    if (!modalEl) return;

    if (window.bootstrap && window.bootstrap.Modal) {
        try {
            const instance = bootstrap.Modal.getOrCreateInstance(modalEl);
            instance.show();
            return;
        } catch (e) {
            console.warn('Bootstrap modal error, using fallback:', e);
        }
    }

    modalEl.style.display = 'block';
    setTimeout(() => {
        modalEl.classList.add('show');
    }, 10);
    document.body.classList.add('modal-open');

    if (!document.querySelector('.modal-backdrop')) {
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop fade show';
        document.body.appendChild(backdrop);
    }
}

function closeModal(modalId) {
    const modalEl = typeof modalId === 'string' ? document.getElementById(modalId) : modalId;
    if (!modalEl) return;

    if (window.bootstrap && window.bootstrap.Modal) {
        try {
            const instance = bootstrap.Modal.getInstance(modalEl);
            if (instance) {
                instance.hide();
                return;
            }
        } catch (e) {
            console.warn('Bootstrap modal hide error, using fallback:', e);
        }
    }

    modalEl.classList.remove('show');
    setTimeout(() => {
        modalEl.style.display = 'none';
        document.body.classList.remove('modal-open');
        document.querySelectorAll('.modal-backdrop').forEach((el) => el.remove());
    }, 150);
}

async function loadCourseClasses(selectedId = null) {
    classSelect.innerHTML = '<option value="">Đang tải lớp học phần...</option>';
    try {
        const classes = await request(API_ENDPOINTS.GRADES + '/course-classes');
        if (classes.length === 0) {
            classSelect.innerHTML = '<option value="">Chưa có lớp học phần</option>';
            document.getElementById('toggleComponentForm').disabled = true;
            showAlert('Chưa có lớp học phần hoạt động để nhập điểm.', 'warning');
            return;
        }

        classSelect.innerHTML = '<option value="">-- Chọn lớp học phần --</option>' + classes
            .map((item) => `<option value="${escapeHtml(item.id)}">${escapeHtml(item.classCode)} — ${escapeHtml(item.courseName || '')}</option>`)
            .join('');
        document.getElementById('toggleComponentForm').disabled = false;

        if (selectedId) {
            classSelect.value = selectedId;
            await loadGradeBook();
        }
    } catch (error) {
        classSelect.innerHTML = '<option value="">Không thể tải lớp học phần</option>';
        showAlert(error.message, 'danger');
    }
}

function updateExportButtons(enabled) {
    ['exportCsvBtn', 'exportWordBtn', 'exportPdfBtn'].forEach((id) => {
        const btn = document.getElementById(id);
        if (btn) btn.disabled = !enabled;
    });
}

async function loadGradeBook() {
    const courseClassId = classSelect.value;
    currentGradeBook = null;
    resetComponentForm();
    const statsRow = document.getElementById('statsRow');

    if (!courseClassId) {
        componentTableBody.innerHTML = emptyRow(6, 'Chọn lớp học phần để xem thành phần điểm.');
        gradeTableHead.innerHTML = '<tr><th>Sinh viên</th><th>Chọn lớp học phần để xem điểm</th></tr>';
        gradeTableBody.innerHTML = emptyRow(2, 'Chọn lớp học phần để tải bảng điểm.');
        weightSummary.textContent = '';
        if (statsRow) statsRow.hidden = true;
        updateExportButtons(false);
        if (addStudentBtn) addStudentBtn.disabled = true;
        showAlert('Chọn lớp học phần để xem danh sách điểm.', 'info');
        return;
    }

    showAlert('Đang tải bảng điểm...', 'info');
    gradeTableBody.innerHTML = emptyRow(2, 'Đang tải dữ liệu...');
    try {
        currentGradeBook = await request(`${API_ENDPOINTS.GRADES}?courseClassId=${encodeURIComponent(courseClassId)}`);
        renderGradeBook(currentGradeBook);
        renderComponents(currentGradeBook.components);
        renderStats(currentGradeBook);
        document.getElementById('toggleComponentForm').disabled = false;
        if (addStudentBtn) addStudentBtn.disabled = false;
        updateExportButtons(Boolean(currentGradeBook.students && currentGradeBook.students.length));
        showAlert(currentGradeBook.statusMessage, currentGradeBook.gradebookReady ? 'success' : 'warning');
    } catch (error) {
        showAlert(error.message, 'danger');
        gradeTableBody.innerHTML = emptyRow(2, 'Không thể tải bảng điểm.');
        if (statsRow) statsRow.hidden = true;
        updateExportButtons(false);
        if (addStudentBtn) addStudentBtn.disabled = true;
    }
}

async function openAddStudentModal() {
    const courseClassId = classSelect.value;
    if (!courseClassId) return;

    if (modalAlert) modalAlert.classList.add('d-none');
    if (selectStudent) selectStudent.innerHTML = '<option value="">Đang tải danh sách...</option>';
    if (confirmAddStudentBtn) confirmAddStudentBtn.disabled = true;

    openModal('addStudentModal');

    try {
        const students = await request(`${API_ENDPOINTS.GRADES}/available-students?courseClassId=${encodeURIComponent(courseClassId)}`);
        if (!students || !students.length) {
            selectStudent.innerHTML = '<option value="">Tất cả sinh viên trong trường đã có trong lớp này</option>';
            confirmAddStudentBtn.disabled = true;
            return;
        }

        selectStudent.innerHTML = '<option value="">-- Chọn sinh viên để thêm vào lớp --</option>' + students
            .map((s) => `<option value="${escapeHtml(s.id)}">${escapeHtml(s.studentCode)} - ${escapeHtml(s.fullName)} (${escapeHtml(s.className || 'Chưa phân lớp')})</option>`)
            .join('');
        confirmAddStudentBtn.disabled = false;
    } catch (error) {
        if (modalAlert) {
            modalAlert.textContent = error.message;
            modalAlert.classList.remove('d-none');
        }
        if (selectStudent) selectStudent.innerHTML = '<option value="">Lỗi khi tải danh sách</option>';
    }
}

async function handleConfirmAddStudent() {
    const courseClassId = classSelect.value;
    const studentId = selectStudent.value;
    if (!studentId) {
        if (modalAlert) {
            modalAlert.textContent = 'Vui lòng chọn một sinh viên!';
            modalAlert.classList.remove('d-none');
        }
        return;
    }

    try {
        confirmAddStudentBtn.disabled = true;
        await request(`${API_ENDPOINTS.GRADES}/registrations`, {
            method: 'POST',
            body: JSON.stringify({ courseClassId, studentId }),
        });
        closeModal('addStudentModal');
        showAlert('Đã thêm sinh viên vào lớp học phần thành công!', 'success');
        await loadGradeBook();
    } catch (error) {
        if (modalAlert) {
            modalAlert.textContent = error.message;
            modalAlert.classList.remove('d-none');
        }
        confirmAddStudentBtn.disabled = false;
    }
}

function openCreateClassModal() {
    if (createClassModalAlert) createClassModalAlert.classList.add('d-none');
    const form = document.getElementById('createClassForm');
    if (form) form.reset();
    const check = document.getElementById('initDefaultComponentsCheck');
    if (check) check.checked = true;

    openModal('createClassModal');
}

async function handleConfirmCreateClass() {
    const code = document.getElementById('newClassCode')?.value.trim();
    const name = document.getElementById('newClassName')?.value.trim();
    const initDefault = document.getElementById('initDefaultComponentsCheck')?.checked ?? true;

    if (!code || !name) {
        if (createClassModalAlert) {
            createClassModalAlert.textContent = 'Vui lòng điền đầy đủ mã và tên lớp học phần!';
            createClassModalAlert.classList.remove('d-none');
        }
        return;
    }

    try {
        if (confirmCreateClassBtn) confirmCreateClassBtn.disabled = true;
        const newClass = await request(`${API_ENDPOINTS.GRADES}/course-classes`, {
            method: 'POST',
            body: JSON.stringify({
                code: code,
                name: name,
                initDefaultComponents: initDefault,
            }),
        });

        closeModal('createClassModal');
        showAlert(`Đã tạo thành công lớp học phần "${newClass.classCode}"!`, 'success');

        await loadCourseClasses(newClass.id);
    } catch (error) {
        if (createClassModalAlert) {
            createClassModalAlert.textContent = error.message;
            createClassModalAlert.classList.remove('d-none');
        }
    } finally {
        if (confirmCreateClassBtn) confirmCreateClassBtn.disabled = false;
    }
}

function renderStats(gradeBook) {
    const statsRow = document.getElementById('statsRow');
    if (!statsRow) return;
    const students = gradeBook.students || [];
    if (!students.length) {
        statsRow.hidden = true;
        return;
    }
    statsRow.hidden = false;
    const total = students.length;
    const completed = students.filter((s) => s.totalScore != null);
    const passed = completed.filter((s) => s.result === 'Đạt');

    document.getElementById('statTotalStudents').textContent = total;
    document.getElementById('statCompletedStudents').textContent = `${completed.length}/${total}`;

    if (completed.length > 0) {
        const sumScore = completed.reduce((acc, s) => acc + Number(s.totalScore), 0);
        const avg = (sumScore / completed.length).toFixed(2);
        const passRate = Math.round((passed.length / completed.length) * 100);
        document.getElementById('statAvgScore').textContent = avg;
        document.getElementById('statPassRate').textContent = `${passRate}% (${passed.length} SV)`;
    } else {
        document.getElementById('statAvgScore').textContent = '—';
        document.getElementById('statPassRate').textContent = '—';
    }
}

function exportCsv() {
    if (!currentGradeBook || !currentGradeBook.students.length) return;
    const components = currentGradeBook.components;
    const headers = ['Mã sinh viên', 'Họ và tên', ...components.map((c) => `"${c.componentName} (${c.weightPercentage}%)"`), 'Điểm tổng kết', 'Xếp loại', 'Kết quả'];
    const rows = currentGradeBook.students.map((s) => {
        const scores = components.map((c) => {
            const g = s.grades.find((item) => item.componentId === c.id);
            return g && g.score != null ? g.score : '';
        });
        return [
            `"${s.studentCode || ''}"`,
            `"${s.studentName || ''}"`,
            ...scores,
            s.totalScore != null ? s.totalScore : '',
            `"${s.letterGrade || ''}"`,
            `"${s.result || ''}"`,
        ].join(',');
    });
    const csvContent = '\uFEFF' + [headers.join(','), ...rows].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    const selectedOption = classSelect.options[classSelect.selectedIndex];
    const className = selectedOption ? selectedOption.text.split('—')[0].trim().replace(/\s+/g, '_') : 'BangDiem';
    a.href = url;
    a.download = `BangDiem_${className}.csv`;
    a.click();
    URL.revokeObjectURL(url);
}

function exportWord() {
    if (!currentGradeBook || !currentGradeBook.students.length) return;
    const selectedOption = classSelect.options[classSelect.selectedIndex];
    const className = selectedOption ? selectedOption.text : 'Bảng điểm';
    const components = currentGradeBook.components;

    const tableHeaderHtml = `
        <tr style="background-color: #f2f2f2;">
            <th style="border: 1px solid #000; padding: 6px; text-align: center; width: 40px;">STT</th>
            <th style="border: 1px solid #000; padding: 6px; text-align: center; width: 100px;">Mã SV</th>
            <th style="border: 1px solid #000; padding: 6px; text-align: left; width: 180px;">Họ và tên</th>
            ${components.map((c) => `<th style="border: 1px solid #000; padding: 6px; text-align: center;">${escapeHtml(c.componentName)}<br><small>(${formatNumber(c.weightPercentage)}%)</small></th>`).join('')}
            <th style="border: 1px solid #000; padding: 6px; text-align: center; width: 70px;">Tổng kết</th>
            <th style="border: 1px solid #000; padding: 6px; text-align: center; width: 60px;">Xếp loại</th>
            <th style="border: 1px solid #000; padding: 6px; text-align: center; width: 80px;">Kết quả</th>
        </tr>
    `;

    const tableRowsHtml = currentGradeBook.students.map((s, idx) => {
        const scoresHtml = components.map((c) => {
            const g = s.grades.find((item) => item.componentId === c.id);
            return `<td style="border: 1px solid #000; padding: 6px; text-align: center;">${g && g.score != null ? formatNumber(g.score) : '—'}</td>`;
        }).join('');
        const resText = s.result === 'PASS' || s.result === 'Đạt' ? 'Đạt' : (s.result ? 'Không đạt' : '—');
        return `
            <tr>
                <td style="border: 1px solid #000; padding: 6px; text-align: center;">${idx + 1}</td>
                <td style="border: 1px solid #000; padding: 6px; text-align: center;">${escapeHtml(s.studentCode || '')}</td>
                <td style="border: 1px solid #000; padding: 6px; text-align: left;">${escapeHtml(s.studentName || '')}</td>
                ${scoresHtml}
                <td style="border: 1px solid #000; padding: 6px; text-align: center; font-weight: bold;">${s.totalScore != null ? formatNumber(s.totalScore) : '—'}</td>
                <td style="border: 1px solid #000; padding: 6px; text-align: center;">${escapeHtml(s.letterGrade || '—')}</td>
                <td style="border: 1px solid #000; padding: 6px; text-align: center;">${resText}</td>
            </tr>
        `;
    }).join('');

    const today = new Date();
    const dateStr = `Đà Nẵng, ngày ${today.getDate()} tháng ${today.getMonth() + 1} năm ${today.getFullYear()}`;

    const wordHtml = `
        <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
        <head>
            <meta charset='utf-8'>
            <title>Bảng điểm - ${escapeHtml(className)}</title>
            <!--[if gte mso 9]>
            <xml>
                <w:WordDocument>
                    <w:View>Print</w:View>
                    <w:Zoom>100</w:Zoom>
                    <w:DoNotOptimizeForBrowser/>
                </w:WordDocument>
            </xml>
            <![endif]-->
            <style>
                @page {
                    size: A4 landscape;
                    margin: 1.5cm 1.5cm 1.5cm 1.5cm;
                    mso-page-orientation: landscape;
                }
                body {
                    font-family: 'Times New Roman', Times, serif;
                    font-size: 12pt;
                    line-height: 1.3;
                    color: #000;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                }
            </style>
        </head>
        <body>
            <table style="width: 100%; margin-bottom: 20px;">
                <tr>
                    <td style="text-align: left; vertical-align: top; font-size: 11pt;">
                        <strong>BỘ GIÁO DỤC VÀ ĐÀO TẠO</strong><br>
                        <strong>TRƯỜNG ĐẠI HỌC ĐÔNG Á</strong>
                    </td>
                    <td style="text-align: center; vertical-align: top; font-size: 11pt;">
                        <strong>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</strong><br>
                        <strong>Độc lập - Tự do - Hạnh phúc</strong><br>
                        -------------------
                    </td>
                </tr>
            </table>

            <div style="text-align: center; margin-bottom: 15px;">
                <h2 style="font-size: 16pt; margin: 0; text-transform: uppercase;">BẢNG TỔNG HỢP ĐIỂM HỌC PHẦN</h2>
                <p style="font-size: 12pt; margin: 5px 0 0 0; font-style: italic;">Lớp học phần: ${escapeHtml(className)}</p>
            </div>

            <table style="width: 100%; border-collapse: collapse; margin-top: 10px;">
                <thead>${tableHeaderHtml}</thead>
                <tbody>${tableRowsHtml}</tbody>
            </table>

            <p style="margin-top: 15px; font-style: italic; font-size: 11pt;">
                * Tổng số sinh viên: <strong>${currentGradeBook.students.length}</strong> | 
                Đã có điểm tổng kết: <strong>${currentGradeBook.summary?.gradedCount ?? 0}/${currentGradeBook.students.length}</strong> | 
                Điểm trung bình: <strong>${formatNumber(currentGradeBook.summary?.averageScore)}</strong> | 
                Tỷ lệ đạt: <strong>${formatNumber(currentGradeBook.summary?.passRate)}%</strong>
            </p>

            <table style="width: 100%; margin-top: 30px;">
                <tr>
                    <td style="width: 50%;"></td>
                    <td style="width: 50%; text-align: center; font-size: 12pt;">
                        <em>${dateStr}</em><br>
                        <strong>GIẢNG VIÊN CHẤM ĐIỂM</strong><br>
                        <br><br><br><br>
                        <em>(Ký và ghi rõ họ tên)</em>
                    </td>
                </tr>
            </table>
        </body>
        </html>
    `;

    const blob = new Blob(['\uFEFF' + wordHtml], { type: 'application/msword;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    const safeName = selectedOption ? selectedOption.text.split('—')[0].trim().replace(/\s+/g, '_') : 'BangDiem';
    a.href = url;
    a.download = `BangDiem_${safeName}.doc`;
    a.click();
    URL.revokeObjectURL(url);
}

function exportPdf() {
    if (!currentGradeBook || !currentGradeBook.students.length) return;
    const selectedOption = classSelect.options[classSelect.selectedIndex];
    const className = selectedOption ? selectedOption.text : 'Bảng điểm';
    const components = currentGradeBook.components;

    const tableHeaderHtml = `
        <tr>
            <th style="width: 40px; text-align: center;">STT</th>
            <th style="width: 100px; text-align: center;">Mã sinh viên</th>
            <th style="text-align: left;">Họ và tên</th>
            ${components.map((c) => `<th style="text-align: center;">${escapeHtml(c.componentName)}<br><small>(${formatNumber(c.weightPercentage)}%)</small></th>`).join('')}
            <th style="width: 80px; text-align: center;">Tổng kết</th>
            <th style="width: 70px; text-align: center;">Xếp loại</th>
            <th style="width: 90px; text-align: center;">Kết quả</th>
        </tr>
    `;

    const tableRowsHtml = currentGradeBook.students.map((s, idx) => {
        const scoresHtml = components.map((c) => {
            const g = s.grades.find((item) => item.componentId === c.id);
            return `<td style="text-align: center;">${g && g.score != null ? formatNumber(g.score) : '—'}</td>`;
        }).join('');
        const resText = s.result === 'PASS' || s.result === 'Đạt' ? 'Đạt' : (s.result ? 'Không đạt' : '—');
        const resColor = resText === 'Đạt' ? '#166534' : (resText === 'Không đạt' ? '#dc2626' : '#6b7280');
        return `
            <tr>
                <td style="text-align: center;">${idx + 1}</td>
                <td style="text-align: center; font-weight: 500;">${escapeHtml(s.studentCode || '')}</td>
                <td style="text-align: left;">${escapeHtml(s.studentName || '')}</td>
                ${scoresHtml}
                <td style="text-align: center; font-weight: 700;">${s.totalScore != null ? formatNumber(s.totalScore) : '—'}</td>
                <td style="text-align: center;">${escapeHtml(s.letterGrade || '—')}</td>
                <td style="text-align: center; font-weight: 600; color: ${resColor};">${resText}</td>
            </tr>
        `;
    }).join('');

    const printWindow = window.open('', '_blank');
    if (!printWindow) {
        alert('Trình duyệt đã chặn cửa sổ bật lên (popup). Vui lòng cho phép popup để in bảng điểm!');
        return;
    }

    const today = new Date();
    const dateStr = `Đà Nẵng, ngày ${today.getDate()} tháng ${today.getMonth() + 1} năm ${today.getFullYear()}`;

    printWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Bảng điểm - ${escapeHtml(className)}</title>
            <style>
                @page { size: A4 landscape; margin: 12mm 15mm; }
                body {
                    font-family: 'Times New Roman', serif;
                    font-size: 13pt;
                    color: #111;
                    margin: 0;
                    padding: 10px;
                }
                .header-table { width: 100%; margin-bottom: 20px; border-collapse: collapse; }
                .header-table td { border: none; padding: 2px; }
                .title { text-align: center; font-size: 18pt; font-weight: bold; margin-top: 10px; margin-bottom: 4px; text-transform: uppercase; }
                .subtitle { text-align: center; font-size: 13pt; margin-bottom: 20px; font-weight: 500; }
                table.data-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 10px;
                }
                table.data-table th, table.data-table td {
                    border: 1px solid #333;
                    padding: 7px 8px;
                    font-size: 11pt;
                }
                table.data-table th {
                    background-color: #f1f5f9;
                    font-weight: bold;
                }
                .stats-box {
                    margin-top: 15px;
                    font-size: 11pt;
                    font-style: italic;
                }
                .footer-table {
                    width: 100%;
                    margin-top: 30px;
                    border-collapse: collapse;
                }
                .footer-table td {
                    border: none;
                    text-align: center;
                    vertical-align: top;
                    font-size: 12pt;
                }
                @media print {
                    .no-print { display: none; }
                    body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                }
            </style>
        </head>
        <body>
            <div class="no-print" style="margin-bottom: 15px; background: #e0f2fe; padding: 12px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                <span>💡 <strong>Hướng dẫn:</strong> Hộp thoại in sẽ tự động hiện. Bạn chọn <em>"Save as PDF" (Lưu dưới dạng PDF)</em> tại mục Destination / Máy in.</span>
                <button onclick="window.print()" style="padding: 8px 16px; font-size: 13pt; background: #0284c7; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold;">In / Lưu PDF ngay</button>
            </div>

            <table class="header-table">
                <tr>
                    <td style="text-align: left; width: 50%;">
                        <strong>BỘ GIÁO DỤC VÀ ĐÀO TẠO</strong><br>
                        <strong>TRƯỜNG ĐẠI HỌC ĐÔNG Á</strong>
                    </td>
                    <td style="text-align: center; width: 50%;">
                        <strong>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</strong><br>
                        <strong>Độc lập - Tự do - Hạnh phúc</strong>
                    </td>
                </tr>
            </table>

            <div class="title">BẢNG TỔNG HỢP ĐIỂM HỌC PHẦN</div>
            <div class="subtitle">Lớp học phần: ${escapeHtml(className)}</div>

            <table class="data-table">
                <thead>${tableHeaderHtml}</thead>
                <tbody>${tableRowsHtml}</tbody>
            </table>

            <div class="stats-box">
                * Tổng số sinh viên: <strong>${currentGradeBook.students.length}</strong> | 
                Đã có điểm tổng kết: <strong>${currentGradeBook.summary?.gradedCount ?? 0}/${currentGradeBook.students.length}</strong> | 
                Điểm trung bình: <strong>${formatNumber(currentGradeBook.summary?.averageScore)}</strong> | 
                Tỷ lệ đạt: <strong>${formatNumber(currentGradeBook.summary?.passRate)}%</strong>
            </div>

            <table class="footer-table">
                <tr>
                    <td style="width: 50%;"></td>
                    <td style="width: 50%;">
                        <em>${dateStr}</em><br>
                        <strong>GIẢNG VIÊN CHẤM ĐIỂM</strong><br>
                        <br><br><br><br>
                        <em>(Ký và ghi rõ họ tên)</em>
                    </td>
                </tr>
            </table>
        </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => {
        printWindow.print();
    }, 500);
}

function renderComponents(components) {
    if (!components.length) {
        componentTableBody.innerHTML = emptyRow(6, 'Chưa có thành phần điểm. Hãy thêm thành phần để bắt đầu.');
        return;
    }
    componentTableBody.innerHTML = components.map((component) => `
        <tr>
            <td>${escapeHtml(component.componentCode)}</td>
            <td>${escapeHtml(component.componentName)}</td>
            <td>${formatNumber(component.weightPercentage)}%</td>
            <td>${formatNumber(component.minScore ?? 0)}–${formatNumber(component.maxScore ?? 10)}</td>
            <td>${component.required ? 'Có' : 'Không'}</td>
            <td class="text-end">
                <div class="component-actions">
                    <button class="btn btn-outline-primary btn-sm" type="button" data-action="edit-component" data-id="${escapeHtml(component.id)}" title="Sửa">
                        <i class="bi bi-pencil-square"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm" type="button" data-action="delete-component" data-id="${escapeHtml(component.id)}" title="Xóa">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderGradeBook(gradeBook) {
    const components = gradeBook.components;
    weightSummary.textContent = `Tổng trọng số: ${formatNumber(gradeBook.configuredWeightTotal)}%`;
    gradeTableHead.innerHTML = `
        <tr>
            <th>Mã sinh viên</th>
            <th>Họ và tên</th>
            ${components.map((component) => `<th>${escapeHtml(component.componentName)}<small class="d-block text-muted">${formatNumber(component.weightPercentage)}%</small></th>`).join('')}
            <th>Điểm tổng kết</th>
            <th>Xếp loại</th>
            <th>Kết quả</th>
        </tr>
    `;

    if (!gradeBook.students.length) {
        gradeTableBody.innerHTML = emptyRow(components.length + 5, 'Chưa có sinh viên đăng ký lớp học phần này.');
        return;
    }

    gradeTableBody.innerHTML = gradeBook.students.map((student) => `
        <tr>
            <td>${escapeHtml(student.studentCode || '')}</td>
            <td>${escapeHtml(student.studentName || '')}</td>
            ${student.grades.map((grade) => renderGradeCell(student.registrationId, grade, components.find((item) => item.id === grade.componentId))).join('')}
            <td class="fw-semibold">${student.totalScore == null ? '<span class="text-muted">Chưa đủ điểm</span>' : formatNumber(student.totalScore)}</td>
            <td>${student.letterGrade == null ? '—' : `<span class="badge text-bg-primary">${escapeHtml(student.letterGrade)}</span><small class="d-block text-muted">GPA ${formatNumber(student.gpaValue)}</small>`}</td>
            <td>${student.result == null ? '—' : `<span class="badge ${student.result === 'Đạt' ? 'text-bg-success' : 'text-bg-danger'}">${escapeHtml(student.result)}</span>`}</td>
        </tr>
    `).join('');
}

function renderGradeCell(registrationId, grade, component) {
    const score = grade.score == null ? '' : escapeHtml(grade.score);
    const min = component?.minScore ?? 0;
    const max = component?.maxScore ?? 10;
    return `
        <td>
            <div class="grade-cell">
                <input class="form-control form-control-sm grade-input"
                       type="text"
                       inputmode="decimal"
                       placeholder="${escapeHtml(min)}-${escapeHtml(max)}"
                       data-min="${escapeHtml(min)}"
                       data-max="${escapeHtml(max)}"
                       value="${score}"
                       aria-label="Điểm ${escapeHtml(component?.componentName || '')}"
                       data-score-input="${escapeHtml(registrationId)}:${escapeHtml(grade.componentId)}" />
                <button class="btn btn-outline-primary btn-sm" type="button" title="Lưu điểm" aria-label="Lưu điểm" data-action="save-grade" data-registration="${escapeHtml(registrationId)}" data-component="${escapeHtml(grade.componentId)}" data-entry="${escapeHtml(grade.id || '')}">
                    <i class="bi bi-check-lg"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm" type="button" title="Xóa điểm" aria-label="Xóa điểm" data-action="delete-grade" data-entry="${escapeHtml(grade.id || '')}" ${grade.id ? '' : 'disabled'}>
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </td>
    `;
}

async function saveComponent(event) {
    event.preventDefault();
    const id = document.getElementById('componentId').value;
    const payload = {
        courseClassId: classSelect.value,
        componentCode: document.getElementById('componentCode').value.trim(),
        componentName: document.getElementById('componentName').value.trim(),
        weightPercentage: Number(document.getElementById('componentWeight').value),
        minScore: Number(document.getElementById('componentMin').value || 0),
        maxScore: Number(document.getElementById('componentMax').value || 10),
        required: document.getElementById('componentRequired').checked,
        inputOrder: document.getElementById('componentOrder').value
            ? Number(document.getElementById('componentOrder').value)
            : null,
    };

    try {
        await request(id ? `${API_ENDPOINTS.GRADES}/components/${encodeURIComponent(id)}` : `${API_ENDPOINTS.GRADES}/components`, {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(payload),
        });
        resetComponentForm();
        await loadGradeBook();
    } catch (error) {
        showAlert(error.message, 'danger');
    }
}

async function handleComponentAction(event) {
    const button = event.target.closest('button[data-action]');
    if (!button || !currentGradeBook) {
        return;
    }
    const component = currentGradeBook.components.find((item) => item.id === button.dataset.id);
    if (!component) {
        return;
    }
    if (button.dataset.action === 'edit-component') {
        document.getElementById('componentId').value = component.id;
        document.getElementById('componentCode').value = component.componentCode;
        document.getElementById('componentName').value = component.componentName;
        document.getElementById('componentWeight').value = component.weightPercentage;
        document.getElementById('componentMin').value = component.minScore ?? 0;
        document.getElementById('componentMax').value = component.maxScore ?? 10;
        document.getElementById('componentRequired').checked = component.required;
        document.getElementById('componentOrder').value = component.inputOrder ?? '';
        componentSection.hidden = false;
        componentForm.hidden = false;
        document.getElementById('saveComponent').textContent = 'Lưu thay đổi';
        document.getElementById('componentCode').focus();
    } else if (button.dataset.action === 'delete-component'
        && window.confirm(`Xóa thành phần điểm "${component.componentName}"?`)) {
        try {
            await request(`${API_ENDPOINTS.GRADES}/components/${encodeURIComponent(component.id)}`, { method: 'DELETE' });
            await loadGradeBook();
        } catch (error) {
            showAlert(error.message, 'danger');
        }
    }
}

function handleGradeInputRealtime(event) {
    const input = event.target;
    if (!input || !input.classList.contains('grade-input')) return;
    const cell = input.closest('td');
    const saveBtn = cell?.querySelector('[data-action="save-grade"]');
    const rawVal = input.value.trim();
    const min = Number(input.dataset.min ?? 0);
    const max = Number(input.dataset.max ?? 10);

    input.classList.remove('is-invalid');
    if (saveBtn) {
        saveBtn.disabled = false;
        saveBtn.classList.remove('opacity-50');
    }

    if (rawVal === '') {
        return;
    }

    const normalizedVal = rawVal.replace(',', '.');
    const isValidNum = /^[0-9]+(\.[0-9]+)?$/.test(normalizedVal);
    const num = Number(normalizedVal);

    if (!isValidNum || isNaN(num)) {
        input.classList.add('is-invalid');
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.classList.add('opacity-50');
        }
        showAlert(`⚠️ Ký tự '${escapeHtml(rawVal)}' không hợp lệ! Vui lòng chỉ nhập số từ ${min} đến ${max} (không nhập chữ cái).`, 'danger');
        return;
    }

    if (num < min || num > max) {
        input.classList.add('is-invalid');
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.classList.add('opacity-50');
        }
        showAlert(`⚠️ Điểm số (${num}) không được vượt quá thang điểm ${min} - ${max}!`, 'warning');
        return;
    }

    if (rawVal.includes(',')) {
        input.value = normalizedVal;
    }
}

async function handleGradeAction(event) {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }

    if (button.dataset.action === 'save-grade') {
        const input = gradeTableBody.querySelector(`[data-score-input="${CSS.escape(button.dataset.registration)}:${CSS.escape(button.dataset.component)}"]`);
        if (!input || input.value.trim() === '') {
            showAlert('Nhập điểm từ 0 đến 10 trước khi lưu.', 'warning');
            return;
        }
        const rawVal = input.value.trim().replace(',', '.');
        const min = Number(input.dataset.min ?? 0);
        const max = Number(input.dataset.max ?? 10);
        const isValidNum = /^[0-9]+(\.[0-9]+)?$/.test(rawVal);
        const score = Number(rawVal);
        if (!isValidNum || isNaN(score) || score < min || score > max) {
            input.classList.add('is-invalid');
            showAlert(`⚠️ Điểm '${escapeHtml(input.value.trim())}' không hợp lệ! Vui lòng nhập số từ ${min} đến ${max}.`, 'danger');
            return;
        }
        const payload = {
            score,
            retake: false,
            note: null,
        };
        const id = button.dataset.entry;
        if (!id) {
            payload.registrationId = button.dataset.registration;
            payload.componentId = button.dataset.component;
        }
        try {
            await request(id
                ? `${API_ENDPOINTS.GRADES}/entries/${encodeURIComponent(id)}`
                : `${API_ENDPOINTS.GRADES}/entries`, {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(payload),
            });
            await loadGradeBook();
        } catch (error) {
            showAlert(error.message, 'danger');
        }
    } else if (button.dataset.action === 'delete-grade' && button.dataset.entry
        && window.confirm('Xóa điểm thành phần này?')) {
        try {
            await request(`${API_ENDPOINTS.GRADES}/entries/${encodeURIComponent(button.dataset.entry)}`, { method: 'DELETE' });
            await loadGradeBook();
        } catch (error) {
            showAlert(error.message, 'danger');
        }
    }
}

function resetComponentForm() {
    componentForm.reset();
    document.getElementById('componentId').value = '';
    document.getElementById('componentMin').value = '0';
    document.getElementById('componentMax').value = '10';
    document.getElementById('componentRequired').checked = true;
    document.getElementById('saveComponent').textContent = 'Lưu thành phần';
    componentForm.hidden = true;
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...options.headers,
        },
    });
    if (!response.ok) {
        let message = `Yêu cầu thất bại (HTTP ${response.status})`;
        try {
            const body = await response.json();
            message = body.detail || body.message || message;
        } catch (error) {
            console.error('Không thể đọc nội dung lỗi từ máy chủ:', error);
        }
        throw new Error(message);
    }
    const body = await response.text();
    return body ? JSON.parse(body) : null;
}

function showAlert(message, type) {
    gradeAlert.className = `alert alert-${type}`;
    gradeAlert.textContent = message;
}

function emptyRow(columns, message) {
    return `<tr><td colspan="${columns}" class="text-center text-muted py-4">${escapeHtml(message)}</td></tr>`;
}

function formatNumber(value) {
    return value == null ? '—' : new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 }).format(value);
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (character) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    })[character]);
}
