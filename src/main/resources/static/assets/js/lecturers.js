import { API_ENDPOINTS } from './config.js?v=2';

const LECTURERS_API = API_ENDPOINTS?.LECTURERS || '/api/lecturers';

const escapeHtml = (v) =>
    String(v ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');

async function request(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
    });
    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const err = await res.json();
            msg = err.message || err.error || msg;
        } catch {
            // ignore
        }
        throw new Error(msg);
    }
    return res.status === 204 ? null : res.json();
}

async function loadLecturers(keyword = '', isActive = '') {
    const tbody = document.getElementById('lecturerTableBody');
    const params = new URLSearchParams();
    if (keyword.trim()) params.set('keyword', keyword.trim());
    if (isActive !== '') params.set('isActive', isActive);

    try {
        const list = await request(`${LECTURERS_API}${params.toString() ? `?${params}` : ''}`);
        if (!list.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Không tìm thấy giảng viên nào.</td></tr>';
            return;
        }

        tbody.innerHTML = list
            .map((gv) => {
                const active = gv.isActive !== false;
                const badge = active
                    ? '<span class="badge text-bg-success">Đang hoạt động</span>'
                    : '<span class="badge text-bg-secondary">Ngừng hoạt động</span>';
                const degreeSpec = [gv.academicDegree, gv.specialization].filter(Boolean).join(' — ') || '—';
                return `<tr>
                    <td><strong>${escapeHtml(gv.code || '—')}</strong></td>
                    <td>${escapeHtml(gv.fullName || '—')}</td>
                    <td>${escapeHtml(gv.email || '—')}</td>
                    <td>${escapeHtml(gv.phone || '—')}</td>
                    <td>${escapeHtml(degreeSpec)}</td>
                    <td>${badge}</td>
                    <td class="text-end">
                        <div class="d-inline-flex gap-1">
                            <a class="btn btn-outline-secondary btn-sm" href="lecturers.html?page=form&id=${gv.id}&mode=view" title="Xem chi tiết">
                                <i class="bi bi-eye"></i>
                            </a>
                            <a class="btn btn-outline-secondary btn-sm" href="lecturers.html?page=form&id=${gv.id}" title="Chỉnh sửa">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <button type="button" class="btn btn-outline-secondary btn-sm delete-lecturer-btn" data-id="${gv.id}" title="Xóa mềm">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>`;
            })
            .join('');
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">Lỗi tải dữ liệu: ${escapeHtml(error.message)}</td></tr>`;
    }
}

async function initForm(id, mode) {
    document.getElementById('page-list').style.display = 'none';
    document.getElementById('page-form').style.display = 'block';

    const isView = mode === 'view';
    const isEdit = Boolean(id) && !isView;

    if (isView) {
        document.getElementById('pageTitle').textContent = 'Chi tiết giảng viên | AdminLTE 4';
        document.getElementById('pageHeading').textContent = 'Chi tiết giảng viên';
        document.getElementById('breadcrumbTitle').textContent = 'Chi tiết';
        document.getElementById('formTitle').textContent = 'Thông tin giảng viên';
        document.getElementById('submitButton').style.display = 'none';
    } else if (isEdit) {
        document.getElementById('pageTitle').textContent = 'Cập nhật giảng viên | AdminLTE 4';
        document.getElementById('pageHeading').textContent = 'Cập nhật giảng viên';
        document.getElementById('breadcrumbTitle').textContent = 'Cập nhật';
        document.getElementById('formTitle').textContent = 'Chỉnh sửa thông tin giảng viên';
        document.getElementById('submitButton').innerHTML = '<i class="bi bi-check-circle me-1"></i>Cập nhật giảng viên';
    }

    if (id) {
        try {
            const gv = await request(`${LECTURERS_API}/${id}`);
            document.getElementById('code').value = gv.code || '';
            document.getElementById('fullName').value = gv.fullName || '';
            document.getElementById('email').value = gv.email || '';
            document.getElementById('phone').value = gv.phone || '';
            document.getElementById('academicDegree').value = gv.academicDegree || '';
            document.getElementById('specialization').value = gv.specialization || '';
            document.getElementById('isActive').value = gv.isActive !== false ? 'true' : 'false';
        } catch (error) {
            const alertBox = document.getElementById('formAlert');
            alertBox.textContent = `Không thể tải thông tin giảng viên: ${error.message}`;
            alertBox.classList.remove('d-none');
        }
    }

    if (isView) {
        document.querySelectorAll('#lecturerForm input, #lecturerForm select').forEach((el) => {
            el.disabled = true;
        });
    }

    document.getElementById('lecturerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        if (isView) return;
        const alertBox = document.getElementById('formAlert');
        alertBox.classList.add('d-none');

        const payload = {
            code: document.getElementById('code').value.trim(),
            fullName: document.getElementById('fullName').value.trim(),
            email: document.getElementById('email').value.trim(),
            phone: document.getElementById('phone').value.trim(),
            academicDegree: document.getElementById('academicDegree').value,
            specialization: document.getElementById('specialization').value.trim(),
            isActive: document.getElementById('isActive').value === 'true',
        };

        try {
            await request(id ? `${LECTURERS_API}/${id}` : LECTURERS_API, {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(payload),
            });
            window.location.href = 'lecturers.html';
        } catch (error) {
            alertBox.textContent = error.message;
            alertBox.classList.remove('d-none');
        }
    });
}

document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('page') === 'form') {
        await initForm(params.get('id'), params.get('mode'));
        return;
    }

    const searchForm = document.getElementById('searchForm');
    const keywordEl = document.getElementById('keyword');
    const statusEl = document.getElementById('statusFilter');

    searchForm?.addEventListener('submit', (e) => {
        e.preventDefault();
        loadLecturers(keywordEl.value, statusEl.value);
    });
    statusEl?.addEventListener('change', () => loadLecturers(keywordEl.value, statusEl.value));

    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('.delete-lecturer-btn');
        if (!btn) return;
        if (!window.confirm('Bạn có chắc muốn xóa mềm giảng viên này không?')) return;
        try {
            await request(`${LECTURERS_API}/${btn.dataset.id}`, { method: 'DELETE' });
            await loadLecturers(keywordEl.value, statusEl.value);
        } catch (error) {
            window.alert(`Không thể xóa giảng viên: ${error.message}`);
        }
    });

    await loadLecturers();
});
