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
            { key: 'academicYearId', label: 'ID niên khóa (tự động sinh)', type: 'uuid' },
            { key: 'departmentId', label: 'ID khoa (tự động sinh)', type: 'uuid' },
            { key: 'majorId', label: 'ID ngành (tự động sinh)', type: 'uuid' },
            { key: 'trainingProgramId', label: 'ID chương trình đào tạo (tự động sinh)', type: 'uuid' },
            { key: 'advisorId', label: 'ID cố vấn (tự động sinh)', type: 'uuid' },
            { key: 'isActive', label: 'Đang hoạt động', type: 'boolean', defaultValue: true }
        ]
    },
    statuses: {
        title: 'Trạng thái sinh viên',
        description: 'Quản lý danh mục trạng thái dùng chung trong student_status.',
        endpoint: API_ENDPOINTS.STUDENT_STATUSES,
        lookups: [],
        statusList: true,
        columns: [
            { key: 'code', label: 'Mã trạng thái' },
            { key: 'name', label: 'Tên trạng thái' },
            { key: 'description', label: 'Mô tả' },
            { key: 'isActive', label: 'Hoạt động', type: 'boolean' }
        ],
        fields: [
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
    const selectedStatusId = config.statusList
        ? new URLSearchParams(window.location.search).get('statusId')
        : null;
    let detailStudentRows = null;
    let sortField = 'startDate';
    let sortDirection = 'desc';

    if (config.endpoint === API_ENDPOINTS.STUDENT_STATUS_HISTORIES) {
        const sortGroup = document.createElement('div');
        sortGroup.className = 'd-flex align-items-center gap-2 flex-shrink-0';
        sortGroup.innerHTML = `
            <select id="historySortField" class="form-select form-select-sm" style="width: 170px;" aria-label="Sắp xếp theo">
                <option value="studentId">Tên sinh viên</option>
                <option value="statusId">Tên trạng thái</option>
                <option value="startDate" selected>Ngày bắt đầu</option>
                <option value="endDate">Ngày kết thúc</option>
            </select>
            <select id="historySortDirection" class="form-select form-select-sm" style="width: 125px;" aria-label="Thứ tự sắp xếp">
                <option value="asc">Tăng dần</option>
                <option value="desc" selected>Giảm dần</option>
            </select>`;
        searchInput.parentElement.insertBefore(sortGroup, searchInput);
    }

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
        renderRecords(config, records, lookupData, '', sortField, sortDirection);
    } catch (error) {
        console.error(`Lỗi khi mở trang ${config.title}:`, error);
        tableBody.innerHTML = `<tr><td colspan="${config.columns.length + 1}" class="text-center text-danger py-4">${escapeHtml(error.message || 'Không thể tải dữ liệu')}</td></tr>`;
        return;
    }

    addButton.addEventListener('click', () => openRecordForm(config, lookupData));
    cancelButton.addEventListener('click', closeRecordForm);
    form.addEventListener('submit', (event) => saveRecord(event, config, lookupData));
    tableBody.addEventListener('click', (event) => handleTableAction(event, config, lookupData));
    const historySortField = document.getElementById('historySortField');
    const historySortDirection = document.getElementById('historySortDirection');
    historySortField?.addEventListener('change', () => {
        sortField = historySortField.value;
        renderRecords(config, records, lookupData, searchInput.value, sortField, sortDirection);
    });
    historySortDirection?.addEventListener('change', () => {
        sortDirection = historySortDirection.value;
        renderRecords(config, records, lookupData, searchInput.value, sortField, sortDirection);
    });

    searchInput.addEventListener('input', () => {
        if (detailStudentRows) {
            if (config.classList) {
                renderClassStudents(detailStudentRows, searchInput.value);
            } else if (config.statusList) {
                renderStatusStudents(detailStudentRows, searchInput.value);
            }
        } else {
            renderRecords(config, records, lookupData, searchInput.value, sortField, sortDirection);
        }
    });
    backToClasses.addEventListener('click', (event) => {
        if (config.classList && selectedClassId) {
            event.preventDefault();
            window.location.href = '/index.html?page=classes';
        } else if (config.statusList && selectedStatusId) {
            event.preventDefault();
            window.location.href = '/index.html?page=statuses';
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
                detailStudentRows = await loadClassStudents(selectedClassId);
                renderClassStudents(detailStudentRows);
            } catch (error) {
                console.error('Lỗi khi tải danh sách sinh viên của lớp:', error);
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Không thể tải danh sách sinh viên trong lớp.</td></tr>';
            }
        }
    }

    if (selectedStatusId && config.statusList) {
        const selectedStatus = records.find((item) => item.id === selectedStatusId);
        if (!selectedStatus) {
            showStatusDetail(selectedStatusId, null);
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-4">Không tìm thấy trạng thái sinh viên.</td></tr>';
        } else {
            showStatusDetail(selectedStatusId, selectedStatus);
            try {
                detailStudentRows = await loadStatusStudents(selectedStatus);
                renderStatusStudents(detailStudentRows);
            } catch (error) {
                console.error('Lỗi khi tải danh sách sinh viên theo trạng thái:', error);
                tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-4">Không thể tải danh sách sinh viên theo trạng thái.</td></tr>';
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
            renderRecords(config, records, lookupData, searchInput.value, sortField, sortDirection);
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
        if (field.type === 'uuid') {
            return `<div class="col-md-6"><label class="form-label" for="${id}">${escapeHtml(field.label)}</label><input id="${id}" name="${field.key}" class="form-control bg-light" type="text" readonly></div>`;
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

function sortHistoryRecords(records, lookupData, sortField, sortDirection) {
    const direction = sortDirection === 'asc' ? 1 : -1;
    return [...records].sort((left, right) => {
        const leftValue = sortField === 'studentId' || sortField === 'statusId'
            ? displayValue(left, { key: sortField, lookup: sortField === 'studentId' ? 'students' : 'statuses' }, lookupData)
            : left[sortField];
        const rightValue = sortField === 'studentId' || sortField === 'statusId'
            ? displayValue(right, { key: sortField, lookup: sortField === 'studentId' ? 'students' : 'statuses' }, lookupData)
            : right[sortField];

        if (sortField === 'startDate' || sortField === 'endDate') {
            const leftTime = leftValue ? new Date(leftValue).getTime() : 0;
            const rightTime = rightValue ? new Date(rightValue).getTime() : 0;
            return (leftTime - rightTime) * direction;
        }

        return String(leftValue || '').localeCompare(String(rightValue || ''), 'vi', { sensitivity: 'base' }) * direction;
    });
}

function renderRecords(config, records, lookupData, keyword = '', sortField = '', sortDirection = 'asc') {
    const tableBody = document.getElementById('managementTableBody');
    const filteredRecords = (config.endpoint === API_ENDPOINTS.STUDENT_STATUS_HISTORIES && sortField
        ? sortHistoryRecords(records, lookupData, sortField, sortDirection)
        : records).filter((record) => {
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
                : config.statusList && column.key === 'name'
                    ? `<a href="/index.html?page=statuses&statusId=${encodeURIComponent(record.id)}" class="fw-semibold">${escapeHtml(displayValue(record, column, lookupData))}</a>`
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
    document.getElementById('managementTableHead').innerHTML = `
        <tr>
            <th>Mã sinh viên</th>
            <th>Họ tên</th>
            <th>Giới tính</th>
            <th>Trạng thái</th>
            <th>Ngày bắt đầu</th>
            <th>Ngày kết thúc</th>
            <th class="text-end">Hành động</th>
        </tr>`;
    document.getElementById('managementTableBody').dataset.classId = classId;
    setupDetailStudentPicker('class', classId);
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

    const classStudentsById = new Map();
    activeSections.forEach((section) => {
        const student = studentsById.get(section.studentId);
        classStudentsById.set(section.studentId, {
            code: student?.code || student?.studentCode || '-',
            fullName: student?.fullName || (section.studentId ? `Không tìm thấy sinh viên (${section.studentId})` : '-'),
            gender: student?.gender || '-',
            status: section.status || '-',
            startDate: section.startDate || '-',
            endDate: section.endDate || '-',
            sectionId: section.id
        });
    });

    students
        .filter((student) => (
            student.studentClassessId === classId
            && student.isActive !== false
            && !student.deletedAt
        ))
        .forEach((student) => {
            if (!classStudentsById.has(student.id)) {
                classStudentsById.set(student.id, {
                    code: student.code || student.studentCode || '-',
                    fullName: student.fullName || '-',
                    gender: student.gender || '-',
                    status: student.status || 'studying',
                    startDate: '-',
                    endDate: '-',
                    sectionId: null
                });
            }
        });

    return Array.from(classStudentsById.values());
}

function showStatusDetail(statusId, status) {
    const title = status
        ? [status.code, status.name].filter(Boolean).join(' - ')
        : 'Chi tiết trạng thái';
    document.getElementById('managementHeading').textContent = title;
    document.getElementById('managementDescription').textContent = 'Danh sách sinh viên đang có trạng thái này.';
    document.getElementById('managementBreadcrumb').textContent = title;
    document.getElementById('managementTableTitle').textContent = `Danh sách sinh viên${status?.name ? ` - ${status.name}` : ''}`;
    document.title = `${title} | AdminLTE 4`;
    document.getElementById('managementFormCard').style.display = 'none';
    document.getElementById('addManagementRecord').style.display = 'none';
    document.getElementById('backToClasses').style.display = '';
    document.getElementById('backToClasses').innerHTML = '<i class="bi bi-arrow-left me-1"></i>Quay lại trạng thái';
    document.getElementById('backToClasses').href = '/index.html?page=statuses';
    document.getElementById('managementSearch').placeholder = 'Tìm theo mã sinh viên / họ tên...';
    document.getElementById('managementSearch').setAttribute('aria-label', 'Tìm theo mã sinh viên hoặc họ tên');
    document.getElementById('managementTableHead').innerHTML = `
        <tr>
            <th>Mã sinh viên</th>
            <th>Họ tên</th>
            <th>Giới tính</th>
            <th>Lớp hành chính</th>
            <th>Trạng thái</th>
        </tr>`;
    document.getElementById('managementTableHead').innerHTML = `
        <tr>
            <th>Mã sinh viên</th>
            <th>Họ tên</th>
            <th>Giới tính</th>
            <th>Lớp hành chính</th>
            <th>Trạng thái</th>
            <th class="text-end">Hành động</th>
        </tr>`;
    document.getElementById('managementTableBody').dataset.statusId = statusId;
    setupDetailStudentPicker('status', statusId, status);
}

function setupDetailStudentPicker(mode, detailId, status = null) {
    const addButton = document.getElementById('addManagementRecord');
    if (!addButton) {
        return;
    }

    const oldSelect = document.getElementById('detailStudentSelect');
    oldSelect?.remove();

    const select = document.createElement('select');
    select.id = 'detailStudentSelect';
    select.className = 'form-select form-select-sm';
    select.style.width = '260px';
    select.innerHTML = '<option value="">-- Chọn sinh viên --</option>';
    addButton.parentElement.insertBefore(select, addButton);

    const replacement = addButton.cloneNode(true);
    addButton.replaceWith(replacement);
    replacement.style.display = '';
    replacement.innerHTML = mode === 'class'
        ? '<i class="bi bi-plus-circle me-1"></i>Thêm vào lớp'
        : '<i class="bi bi-plus-circle me-1"></i>Thêm sinh viên';

    replacement.addEventListener('click', async () => {
        const studentId = select.value;
        if (!studentId) {
            window.alert('Vui lòng chọn sinh viên');
            return;
        }

        try {
            if (mode === 'class') {
                const response = await fetch(API_ENDPOINTS.STUDENT_CLASSE_SECTIONS, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        studentId,
                        studentClasseId: detailId,
                        status: 'studying',
                        startDate: new Date().toISOString().slice(0, 19),
                        isActive: true
                    })
                });
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
            } else {
                const detailResponse = await fetch(`${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(studentId)}`);
                if (!detailResponse.ok) throw new Error(`HTTP ${detailResponse.status}`);
                const student = await detailResponse.json();
                student.status = status.code;
                const response = await fetch(`${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(studentId)}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(student)
                });
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
            }
            window.location.reload();
        } catch (error) {
            console.error('Lỗi khi thêm sinh viên:', error);
            window.alert('Không thể thêm sinh viên. Vui lòng thử lại.');
        }
    });

    Promise.all([
        fetch(API_ENDPOINTS.STUDENTS),
        mode === 'class' ? fetch(`${API_ENDPOINTS.STUDENT_CLASSE_SECTIONS}/class/${encodeURIComponent(detailId)}`) : Promise.resolve(null)
    ]).then(async ([studentsResponse, sectionsResponse]) => {
        if (!studentsResponse.ok) throw new Error(`HTTP ${studentsResponse.status}`);
        const students = await studentsResponse.json();
        let assignedIds = new Set();

        if (mode === 'class' && sectionsResponse) {
            if (!sectionsResponse.ok) throw new Error(`HTTP ${sectionsResponse.status}`);
            const sections = await sectionsResponse.json();
            assignedIds = new Set(sections.filter((section) => section.isActive !== false).map((section) => section.studentId));
        } else if (mode === 'status') {
            assignedIds = new Set(students.filter((student) => (
                student.isActive !== false && String(student.status || '').trim() === status.code
            )).map((student) => student.id));
        }

        select.innerHTML = '<option value="">-- Chọn sinh viên --</option>' + students
            .filter((student) => {
                if (student.isActive === false) return false;
                if (mode === 'class') {
                    return (student.studentClassessId == null || String(student.studentClassessId).trim() === '')
                        && !assignedIds.has(student.id);
                }
                return !assignedIds.has(student.id);
            })
            .map((student) => {
                const label = [student.code || student.studentCode, student.fullName].filter(Boolean).join(' - ') || student.id;
                return `<option value="${escapeHtml(student.id)}">${escapeHtml(label)}</option>`;
            })
            .join('');
    }).catch((error) => {
        console.error('Lỗi khi tải danh sách sinh viên:', error);
    });
}

async function loadStatusStudents(status) {
    if (!status.code) {
        return [];
    }

    const [studentsResponse, classesResponse] = await Promise.all([
        fetch(API_ENDPOINTS.STUDENTS),
        fetch(API_ENDPOINTS.STUDENT_CLASSES)
    ]);
    if (!studentsResponse.ok || !classesResponse.ok) {
        throw new Error(`HTTP ${!studentsResponse.ok ? studentsResponse.status : classesResponse.status}`);
    }

    const [students, classes] = await Promise.all([
        studentsResponse.json(),
        classesResponse.json()
    ]);
    const classesById = new Map(classes.map((studentClass) => [studentClass.id, studentClass]));

    return students
        .filter((student) => (
            student.isActive !== false
            && !student.deletedAt
            && String(student.status || '').trim() === status.code
        ))
        .map((student) => {
            const studentClass = classesById.get(student.studentClassessId);
            return {
                code: student.code || student.studentCode || '-',
                fullName: student.fullName || '-',
                gender: student.gender || '-',
                className: studentClass
                    ? [studentClass.code, studentClass.name].filter(Boolean).join(' - ') || student.studentClassessId
                    : student.studentClassessId || '-',
                status: status.name || status.code,
                studentId: student.id
            };
        });
}

function renderStatusStudents(students, keyword = '') {
    const tableBody = document.getElementById('managementTableBody');
    const normalizedKeyword = keyword.trim().toLocaleLowerCase();
    const filteredStudents = students.filter((student) => (
        !normalizedKeyword
        || [student.code, student.fullName, student.gender, student.className]
            .some((value) => String(value).toLocaleLowerCase().includes(normalizedKeyword))
    ));

    if (!filteredStudents.length) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Chưa có sinh viên thuộc trạng thái này.</td></tr>';
        return;
    }

    tableBody.innerHTML = filteredStudents.map((student) => `
        <tr>
            <td>${escapeHtml(student.code)}</td>
            <td>${escapeHtml(student.fullName)}</td>
            <td>${escapeHtml(student.gender)}</td>
            <td>${escapeHtml(student.className)}</td>
            <td>${escapeHtml(student.status)}</td>
            <td class="text-end text-nowrap">
                <button type="button" class="btn btn-danger btn-sm" data-action="remove-from-status" data-student-id="${escapeHtml(student.studentId)}" title="Xóa khỏi trạng thái"><i class="bi bi-trash"></i></button>
            </td>
        </tr>
    `).join('');
}

function renderClassStudents(students, keyword = '') {
    const tableBody = document.getElementById('managementTableBody');
    const normalizedKeyword = keyword.trim().toLocaleLowerCase();
    const filteredStudents = students.filter((student) => (
        !normalizedKeyword
        || [student.code, student.fullName, student.gender].some((value) => String(value).toLocaleLowerCase().includes(normalizedKeyword))
    ));

    if (!filteredStudents.length) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Lớp này chưa có sinh viên đang học.</td></tr>';
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
            <td class="text-end text-nowrap">
                ${student.sectionId ? `<button type="button" class="btn btn-danger btn-sm" data-action="remove-from-class" data-section-id="${escapeHtml(student.sectionId)}" title="Xóa khỏi lớp"><i class="bi bi-trash"></i></button>` : ''}
            </td>
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

    const usedUuids = new Set();
    (window.managementRecords || []).forEach((item) => {
        config.fields.filter((f) => f.type === 'uuid').forEach((f) => {
            if (item[f.key]) usedUuids.add(item[f.key]);
        });
    });

    config.fields.forEach((field) => {
        const input = document.getElementById(`record-${field.key}`);
        if (!input) {
            return;
        }
        if (field.type === 'boolean') {
            input.checked = record ? record[field.key] !== false : Boolean(field.defaultValue);
        } else if (field.type === 'uuid') {
            const value = record?.[field.key];
            input.value = value || generateUniqueUuid(usedUuids);
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

async function handleTableAction(event, config, lookupData) {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }
    if (button.dataset.action === 'remove-from-class') {
        if (!window.confirm('Bạn có chắc muốn xóa sinh viên khỏi lớp hành chính này?')) return;
        try {
            const response = await fetch(`${API_ENDPOINTS.STUDENT_CLASSE_SECTIONS}/${encodeURIComponent(button.dataset.sectionId)}`, { method: 'DELETE' });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            window.location.reload();
        } catch (error) {
            console.error('Lỗi khi xóa sinh viên khỏi lớp:', error);
            window.alert('Không thể xóa sinh viên khỏi lớp.');
        }
        return;
    }

    if (button.dataset.action === 'remove-from-status') {
        if (!window.confirm('Bạn có chắc muốn xóa sinh viên khỏi trạng thái này?')) return;
        try {
            const studentResponse = await fetch(`${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(button.dataset.studentId)}`);
            if (!studentResponse.ok) throw new Error(`HTTP ${studentResponse.status}`);
            const student = await studentResponse.json();
            student.status = null;
            const response = await fetch(`${API_ENDPOINTS.STUDENTS}/${encodeURIComponent(button.dataset.studentId)}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(student)
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            window.location.reload();
        } catch (error) {
            console.error('Lỗi khi xóa sinh viên khỏi trạng thái:', error);
            window.alert('Không thể cập nhật trạng thái sinh viên.');
        }
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

function generateUniqueUuid(usedSet = new Set()) {
    let uuid = '';
    do {
        if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
            uuid = crypto.randomUUID();
        } else {
            uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
                const r = (Math.random() * 16) | 0;
                const v = c === 'x' ? r : (r & 0x3) | 0x8;
                return v.toString(16);
            });
        }
    } while (usedSet.has(uuid));
    usedSet.add(uuid);
    return uuid;
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
