import { API_ENDPOINTS } from './config.js';

const LOOKUPS = {
    students: {
        endpoint: API_ENDPOINTS.STUDENTS,
        label: (item) => [item.code, item.fullName].filter(Boolean).join(' - ') || item.id
    },
    classes: {
        endpoint: API_ENDPOINTS.STUDENT_CLASSES,
        label: (item) => [item.code, item.name].filter(Boolean).join(' - ') || item.id
    },
    statuses: {
        endpoint: API_ENDPOINTS.STUDENT_STATUSES,
        label: (item) => [item.code, item.name].filter(Boolean).join(' - ') || item.id
    }
};

const PAGES = {
    classes: {
        title: 'Lớp hành chính',
        description: 'Quản lý danh sách lớp hành chính trong student_classes.',
        endpoint: API_ENDPOINTS.STUDENT_CLASSES,
        lookups: [],
        classList: true,
        columns: [
            { key: 'code', label: 'Mã lớp' },
            { key: 'name', label: 'Tên lớp' },
            { key: 'academicYearId', label: 'ID niên khóa' },
            { key: 'departmentId', label: 'ID khoa' },
            { key: 'isActive', label: 'Hoạt động', type: 'boolean' }
        ],
        fields: [
            { key: 'code', label: 'Mã lớp', maxLength: 100 },
            { key: 'name', label: 'Tên lớp', maxLength: 255 },
            { key: 'academicYearId', label: 'ID niên khóa (UUID)', type: 'uuid' },
            { key: 'departmentId', label: 'ID khoa (UUID)', type: 'uuid' },
            { key: 'majorId', label: 'ID ngành (UUID)', type: 'uuid' },
            { key: 'trainingProgramId', label: 'ID chương trình đào tạo (UUID)', type: 'uuid' },
            { key: 'advisorId', label: 'ID cố vấn (UUID)', type: 'uuid' },
            { key: 'isActive', label: 'Đang hoạt động', type: 'boolean', defaultValue: true }
        ]
    },
    statuses: {
        title: 'Trạng thái sinh viên',
        description: 'Quản lý danh mục trạng thái trong student_status.',
        endpoint: API_ENDPOINTS.STUDENT_STATUSES,
        lookups: ['students'],
        columns: [
            { key: 'code', label: 'Mã trạng thái' },
            { key: 'name', label: 'Tên trạng thái' },
            { key: 'studentId', label: 'ID sinh viên', lookup: 'students' },
            { key: 'description', label: 'Mô tả' },
            { key: 'isActive', label: 'Hoạt động', type: 'boolean' }
        ],
        fields: [
            { key: 'studentId', label: 'Sinh viên (để trống nếu là danh mục dùng chung)', type: 'lookup', lookup: 'students' },
            { key: 'code', label: 'Mã trạng thái', maxLength: 100 },
            { key: 'name', label: 'Tên trạng thái', maxLength: 100 },
            { key: 'description', label: 'Mô tả', type: 'textarea', maxLength: 255 },
            { key: 'isActive', label: 'Đang hoạt động', type: 'boolean', defaultValue: true }
        ]
    },
    'status-histories': {
        title: 'Lịch sử trạng thái',
        description: 'Ghi nhận và quản lý các lần thay đổi trạng thái trong student_status_histories.',
        endpoint: API_ENDPOINTS.STUDENT_STATUS_HISTORIES,
        lookups: ['students', 'statuses'],
        columns: [
            { key: 'studentId', label: 'Sinh viên', lookup: 'students' },
            { key: 'statusId', label: 'Trạng thái', lookup: 'statuses' },
            { key: 'startDate', label: 'Ngày bắt đầu', type: 'date' },
            { key: 'endDate', label: 'Ngày kết thúc', type: 'date' },
            { key: 'decisionNumber', label: 'Số quyết định' },
            { key: 'reason', label: 'Lý do' },
            { key: 'isActive', label: 'Hoạt động', type: 'boolean' }
        ],
        fields: [
            { key: 'studentId', label: 'Sinh viên', type: 'lookup', lookup: 'students' },
            { key: 'statusId', label: 'Trạng thái', type: 'lookup', lookup: 'statuses' },
            { key: 'startDate', label: 'Ngày bắt đầu', type: 'date' },
            { key: 'endDate', label: 'Ngày kết thúc', type: 'date' },
            { key: 'reason', label: 'Lý do', maxLength: 255 },
            { key: 'decisionNumber', label: 'Số quyết định', maxLength: 255 },
            { key: 'decisionDate', label: 'Ngày quyết định', type: 'date' },
            { key: 'note', label: 'Ghi chú', type: 'textarea', maxLength: 255 },
            { key: 'isActive', label: 'Đang hoạt động', type: 'boolean', defaultValue: true }
        ]
    }
};

document.addEventListener('DOMContentLoaded', () => {
    const pageKey = new URLSearchParams(window.location.search).get('page');
    const config = PAGES[pageKey];
    if (!config) {
        return;
    }

    initializeManagementPage(config);
});

async function initializeManagementPage(config) {
    const form = document.getElementById('managementForm');
    const addButton = document.getElementById('addManagementRecord');
    const cancelButton = document.getElementById('cancelManagementForm');
    const tableBody = document.getElementById('managementTableBody');
    const searchInput = document.getElementById('managementSearch');
    const backToClasses = document.getElementById('backToClasses');
    const selectedClassId = config.classList
        ? new URLSearchParams(window.location.search).get('classId')
        : null;
    let classStudentRows = null;

    document.getElementById('managementHeading').textContent = config.title;
    document.getElementById('managementDescription').textContent = config.description;
    document.getElementById('managementBreadcrumb').textContent = config.title;
    document.getElementById('managementTableTitle').textContent = `Danh sách ${config.title.toLowerCase()}`;
    document.getElementById('managementFormTitle').textContent = `Thêm ${config.title.toLowerCase()}`;
    document.title = `${config.title} | AdminLTE 4`;
    renderTableHeader(config);
    renderFormFields(config);

    let records = [];
    const lookupData = {};

    try {
        const [recordResponse, ...lookupResponses] = await Promise.all([
            fetch(config.endpoint),
            ...config.lookups.map((lookupKey) => fetch(LOOKUPS[lookupKey].endpoint))
        ]);
        if (!recordResponse.ok) {
            throw new Error(`Tải danh sách thất bại: HTTP ${recordResponse.status}`);
        }
        records = await recordResponse.json();

        window.managementRecords = records;
        for (let index = 0; index < config.lookups.length; index += 1) {
            const response = lookupResponses[index];
            if (!response.ok) {
                throw new Error(`Tải dữ liệu tham chiếu thất bại: HTTP ${response.status}`);
            }
            lookupData[config.lookups[index]] = await response.json();
        }
        populateReferenceFields(config, lookupData);
        renderRecords(config, records, lookupData);
    } catch (error) {
        console.error(`Lỗi khi mở trang ${config.title}:`, error);
        tableBody.innerHTML = `<tr><td colspan="${config.columns.length + 1}" class="text-center text-danger py-4">${escapeHtml(error.message || 'Không thể tải dữ liệu')}</td></tr>`;
        return;
    }

    addButton.addEventListener('click', () => openRecordForm(config, lookupData));
    cancelButton.addEventListener('click', closeRecordForm);
    form.addEventListener('submit', (event) => saveRecord(event, config, lookupData));
    tableBody.addEventListener('click', (event) => handleTableAction(event, config, lookupData));
    searchInput.addEventListener('input', () => {
        if (classStudentRows) {
            renderClassStudents(classStudentRows, searchInput.value);
        } else {
            renderRecords(config, records, lookupData, searchInput.value);
        }
    });
    backToClasses.addEventListener('click', (event) => {
        if (config.classList && selectedClassId) {
            event.preventDefault();
            window.location.href = '/index.html?page=classes';
        }
    });

    if (selectedClassId && config.classList) {
        const selectedClass = records.find((item) => item.id === selectedClassId);
        if (!selectedClass) {
            showClassDetail(selectedClassId, null);
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Không tìm thấy lớp hành chính.</td></tr>';
        } else {
            showClassDetail(selectedClassId, selectedClass);
            try {
                classStudentRows = await loadClassStudents(selectedClassId);
                renderClassStudents(classStudentRows);
            } catch (error) {
                console.error('Lỗi khi tải danh sách sinh viên của lớp:', error);
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Không thể tải danh sách sinh viên trong lớp.</td></tr>';
            }
        }
    }

    tableBody.addEventListener('management:refresh', async () => {
        try {
            const response = await fetch(config.endpoint);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            records = await response.json();
            window.managementRecords = records;
            renderRecords(config, records, lookupData, searchInput.value);
        } catch (error) {
            console.error(`Lỗi khi tải lại ${config.title}:`, error);
            window.alert('Thao tác đã thực hiện nhưng không thể tải lại danh sách. Vui lòng làm mới trang.');
        }
    });
}

function renderTableHeader(config) {
    const head = document.getElementById('managementTableHead');
    head.innerHTML = `<tr>${config.columns.map((column) => `<th>${escapeHtml(column.label)}</th>`).join('')}<th class="text-end">Hành động</th></tr>`;
}

function renderFormFields(config) {
    const container = document.getElementById('managementFields');
    container.innerHTML = config.fields.map((field) => {
        const id = `record-${field.key}`;
        if (field.type === 'boolean') {
            return `<div class="col-md-6"><div class="form-check mt-4"><input class="form-check-input" id="${id}" name="${field.key}" type="checkbox" ${field.defaultValue ? 'checked' : ''}><label class="form-check-label" for="${id}">${escapeHtml(field.label)}</label></div></div>`;
        }

        const common = `id="${id}" name="${field.key}" class="form-control" ${field.maxLength ? `maxlength="${field.maxLength}"` : ''} ${field.placeholder ? `placeholder="${escapeHtml(field.placeholder)}"` : ''}`;
        if (field.type === 'lookup') {
            return `<div class="col-md-6"><label class="form-label" for="${id}">${escapeHtml(field.label)}</label><select ${common}><option value="">-- Chọn --</option></select></div>`;
        }
        if (field.type === 'textarea') {
            return `<div class="col-md-12"><label class="form-label" for="${id}">${escapeHtml(field.label)}</label><textarea ${common} rows="2"></textarea></div>`;
        }
        const inputType = field.type === 'date' || field.type === 'datetime' ? field.type === 'date' ? 'date' : 'datetime-local' : 'text';
        return `<div class="col-md-6"><label class="form-label" for="${id}">${escapeHtml(field.label)}</label><input ${common} type="${inputType}"></div>`;
    }).join('');
}

function populateReferenceFields(config, lookupData) {
    config.fields.filter((field) => field.type === 'lookup').forEach((field) => {
        const select = document.getElementById(`record-${field.key}`);
        const lookup = LOOKUPS[field.lookup];
        select.innerHTML = '<option value="">-- Chọn --</option>' + (lookupData[field.lookup] || [])
            .map((item) => `<option value="${escapeHtml(item.id)}">${escapeHtml(lookup.label(item))}</option>`)
            .join('');
    });
}

function renderRecords(config, records, lookupData, keyword = '') {
    const tableBody = document.getElementById('managementTableBody');
    const filteredRecords = records.filter((record) => {
        const normalized = keyword.trim().toLocaleLowerCase();
        if (!normalized) {
            return true;
        }
        const values = config.columns.map((column) => displayValue(record, column, lookupData));
        return values.some((value) => String(value).toLocaleLowerCase().includes(normalized));
    });

    if (!filteredRecords.length) {
        tableBody.innerHTML = `<tr><td colspan="${config.columns.length + 1}" class="text-center text-muted py-4">${keyword ? 'Không tìm thấy dữ liệu phù hợp' : 'Chưa có dữ liệu'}</td></tr>`;
        return;
    }

    tableBody.innerHTML = filteredRecords.map((record) => `
        <tr>
            ${config.columns.map((column) => `<td>${config.classList && column.key === 'name'
                ? `<a href="/index.html?page=classes&classId=${encodeURIComponent(record.id)}" class="fw-semibold">${escapeHtml(displayValue(record, column, lookupData))}</a>`
                : escapeHtml(displayValue(record, column, lookupData))}</td>`).join('')}
            <td class="text-end text-nowrap">
                <div class="table-actions">
                    <button type="button" class="btn btn-warning btn-sm" data-action="edit" data-id="${escapeHtml(record.id)}" title="Sửa"><i class="bi bi-pencil-square"></i></button>
                    <button type="button" class="btn btn-danger btn-sm" data-action="delete" data-id="${escapeHtml(record.id)}" title="Xóa mềm"><i class="bi bi-trash"></i></button>
                </div>
            </td>
        </tr>
    `).join('');
}

function showClassDetail(classId, studentClass) {
    const title = studentClass
        ? [studentClass.code, studentClass.name].filter(Boolean).join(' - ')
        : 'Chi tiết lớp';
    document.getElementById('managementHeading').textContent = title;
    document.getElementById('managementDescription').textContent = 'Danh sách sinh viên đang học trong lớp hành chính này.';
    document.getElementById('managementBreadcrumb').textContent = title;
    document.getElementById('managementTableTitle').textContent = `Sinh viên đang học${studentClass?.name ? ` - ${studentClass.name}` : ''}`;
    document.title = `${title} | AdminLTE 4`;
    document.getElementById('managementFormCard').style.display = 'none';
    document.getElementById('addManagementRecord').style.display = 'none';
    document.getElementById('backToClasses').style.display = '';
    document.getElementById('managementSearch').placeholder = 'Tìm theo mã sinh viên / họ tên...';
    document.getElementById('managementSearch').setAttribute('aria-label', 'Tìm theo mã sinh viên hoặc họ tên');
    document.getElementById('managementTableHead').innerHTML = `
        <tr>
            <th>Mã sinh viên</th>
            <th>Họ tên</th>
            <th>Giới tính</th>
            <th>Trạng thái</th>
            <th>Ngày bắt đầu</th>
            <th>Ngày kết thúc</th>
        </tr>`;
    document.getElementById('managementTableBody').dataset.classId = classId;
}

async function loadClassStudents(classId) {
    const [sectionResponse, studentsResponse] = await Promise.all([
        fetch(`${API_ENDPOINTS.STUDENT_CLASSE_SECTIONS}/class/${encodeURIComponent(classId)}`),
        fetch(API_ENDPOINTS.STUDENTS)
    ]);
    if (!sectionResponse.ok || !studentsResponse.ok) {
        throw new Error(`HTTP ${!sectionResponse.ok ? sectionResponse.status : studentsResponse.status}`);
    }

    const [sections, students] = await Promise.all([sectionResponse.json(), studentsResponse.json()]);
    const studentsById = new Map(students.map((student) => [student.id, student]));
    const activeSections = sections.filter((section) => (
        section.isActive !== false
        && !section.deletedAt
        && String(section.status || '').toLocaleLowerCase() === 'studying'
    ));

    return activeSections.map((section) => {
        const student = studentsById.get(section.studentId);
        return {
            code: student?.code || student?.studentCode || '-',
            fullName: student?.fullName || (section.studentId ? `Không tìm thấy sinh viên (${section.studentId})` : '-'),
            gender: student?.gender || '-',
            status: section.status || '-',
            startDate: section.startDate || '-',
            endDate: section.endDate || '-'
        };
    });
}

function renderClassStudents(students, keyword = '') {
    const tableBody = document.getElementById('managementTableBody');
    const normalizedKeyword = keyword.trim().toLocaleLowerCase();
    const filteredStudents = students.filter((student) => (
        !normalizedKeyword
        || [student.code, student.fullName, student.gender].some((value) => String(value).toLocaleLowerCase().includes(normalizedKeyword))
    ));

    if (!filteredStudents.length) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Lớp này chưa có sinh viên đang học.</td></tr>';
        return;
    }
    tableBody.innerHTML = filteredStudents.map((student) => `
        <tr>
            <td>${escapeHtml(student.code)}</td>
            <td>${escapeHtml(student.fullName)}</td>
            <td>${escapeHtml(student.gender)}</td>
            <td>${escapeHtml(student.status)}</td>
            <td>${escapeHtml(String(student.startDate).slice(0, 10))}</td>
            <td>${escapeHtml(student.endDate === '-' ? '-' : String(student.endDate).slice(0, 10))}</td>
        </tr>
    `).join('');
}

function displayValue(record, column, lookupData) {
    const value = record[column.key];
    if (value === null || value === undefined || value === '') {
        return '-';
    }
    if (column.type === 'boolean') {
        return value ? 'Có' : 'Không';
    }
    if (column.type === 'date') {
        return String(value).slice(0, 10);
    }
    if (column.lookup) {
        const item = (lookupData[column.lookup] || []).find((entry) => entry.id === value);
        return item ? LOOKUPS[column.lookup].label(item) : value;
    }
    return value;
}

function openRecordForm(config, lookupData, record = null) {
    const form = document.getElementById('managementForm');
    form.reset();
    form.dataset.recordId = record?.id || '';
    document.getElementById('managementFormTitle').textContent = record
        ? `Sửa ${config.title.toLowerCase()}`
        : `Thêm ${config.title.toLowerCase()}`;

    config.fields.forEach((field) => {
        const input = document.getElementById(`record-${field.key}`);
        if (!input) {
            return;
        }
        if (field.type === 'boolean') {
            input.checked = record ? record[field.key] !== false : Boolean(field.defaultValue);
        } else {
            const value = record?.[field.key];
            input.value = field.type === 'datetime' && value
                ? String(value).slice(0, 16)
                : field.type === 'date' && value
                    ? String(value).slice(0, 10)
                    : value ?? '';
            if (field.type === 'lookup' && value && !Array.from(input.options).some((option) => option.value === value)) {
                const missingOption = document.createElement('option');
                missingOption.value = value;
                missingOption.textContent = `Không tìm thấy: ${value}`;
                input.append(missingOption);
                input.value = value;
            }
        }
    });

    const card = document.getElementById('managementFormCard');
    card.style.display = '';
    card.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function closeRecordForm() {
    const form = document.getElementById('managementForm');
    form.reset();
    form.dataset.recordId = '';
    document.getElementById('managementFormCard').style.display = 'none';
}

function collectFormData(config) {
    const body = {};
    for (const field of config.fields) {
        const input = document.getElementById(`record-${field.key}`);
        if (field.type === 'boolean') {
            body[field.key] = input.checked;
            continue;
        }
        const value = input.value.trim();
        if (field.type === 'datetime') {
            body[field.key] = value ? `${value}:00` : null;
        } else {
            body[field.key] = value || null;
        }
    }
    return body;
}

async function saveRecord(event, config, lookupData) {
    event.preventDefault();
    const form = event.currentTarget;
    const recordId = form.dataset.recordId;
    const body = collectFormData(config);

    try {
        const response = await fetch(
            recordId ? `${config.endpoint}/${encodeURIComponent(recordId)}` : config.endpoint,
            {
                method: recordId ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            }
        );
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        closeRecordForm();
        document.getElementById('managementTableBody').dispatchEvent(new Event('management:refresh'));
        window.alert(recordId ? 'Cập nhật thành công' : 'Thêm mới thành công');
    } catch (error) {
        console.error(`Lỗi khi lưu ${config.title}:`, error);
        window.alert('Không thể lưu dữ liệu. Vui lòng kiểm tra thông tin và thử lại.');
    }
}

function handleTableAction(event, config, lookupData) {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }
    const record = window.managementRecords?.find((item) => item.id === button.dataset.id);
    if (button.dataset.action === 'edit' && record) {
        openRecordForm(config, lookupData, record);
    } else if (button.dataset.action === 'delete') {
        deleteRecord(button.dataset.id, config);
    }
}

async function deleteRecord(id, config) {
    if (!window.confirm(`Bạn có chắc muốn xóa ${config.title.toLowerCase()} này?`)) {
        return;
    }
    try {
        const response = await fetch(`${config.endpoint}/${encodeURIComponent(id)}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const tableBody = document.getElementById('managementTableBody');
        tableBody.dispatchEvent(new Event('management:refresh'));
    } catch (error) {
        console.error(`Lỗi khi xóa ${config.title}:`, error);
        window.alert('Không thể xóa dữ liệu. Vui lòng thử lại.');
    }
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
