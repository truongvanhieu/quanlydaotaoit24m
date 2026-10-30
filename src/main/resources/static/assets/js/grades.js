import { API_ENDPOINTS } from './config.js';

const classSelect = document.getElementById('courseClassSelect');
const gradeAlert = document.getElementById('gradeAlert');
const componentSection = document.getElementById('componentSection');
const componentForm = document.getElementById('componentForm');
const componentTableBody = document.getElementById('componentTableBody');
const gradeTableHead = document.getElementById('gradeTableHead');
const gradeTableBody = document.getElementById('gradeTableBody');
const weightSummary = document.getElementById('weightSummary');

let currentGradeBook = null;

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

    await loadCourseClasses();
});

async function loadCourseClasses() {
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
            .map((item) => `<option value="${escapeHtml(item.id)}">${escapeHtml(item.classCode)} — ${escapeHtml(item.courseCode || '')} ${escapeHtml(item.courseName || '')}</option>`)
            .join('');
        document.getElementById('toggleComponentForm').disabled = false;
    } catch (error) {
        classSelect.innerHTML = '<option value="">Không thể tải lớp học phần</option>';
        showAlert(error.message, 'danger');
    }
}

async function loadGradeBook() {
    const courseClassId = classSelect.value;
    currentGradeBook = null;
    resetComponentForm();
    if (!courseClassId) {
        componentTableBody.innerHTML = emptyRow(6, 'Chọn lớp học phần để xem thành phần điểm.');
        gradeTableHead.innerHTML = '<tr><th>Sinh viên</th><th>Chọn lớp học phần để xem điểm</th></tr>';
        gradeTableBody.innerHTML = emptyRow(2, 'Chọn lớp học phần để tải bảng điểm.');
        weightSummary.textContent = '';
        showAlert('Chọn lớp học phần để xem danh sách điểm.', 'info');
        return;
    }

    showAlert('Đang tải bảng điểm...', 'info');
    gradeTableBody.innerHTML = emptyRow(2, 'Đang tải dữ liệu...');
    try {
        currentGradeBook = await request(`${API_ENDPOINTS.GRADES}?courseClassId=${encodeURIComponent(courseClassId)}`);
        renderGradeBook(currentGradeBook);
        renderComponents(currentGradeBook.components);
        document.getElementById('toggleComponentForm').disabled = false;
        showAlert(currentGradeBook.statusMessage, currentGradeBook.gradebookReady ? 'success' : 'warning');
    } catch (error) {
        showAlert(error.message, 'danger');
        gradeTableBody.innerHTML = emptyRow(2, 'Không thể tải bảng điểm.');
    }
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
                <input class="form-control form-control-sm grade-input" type="number" min="${escapeHtml(min)}" max="${escapeHtml(max)}" step="0.01" value="${score}" aria-label="Điểm ${escapeHtml(component?.componentName || '')}" data-score-input="${escapeHtml(registrationId)}:${escapeHtml(grade.componentId)}" />
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
        const score = Number(input.value);
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
