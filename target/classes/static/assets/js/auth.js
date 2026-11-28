export async function requireAuth() {
    try {
        const res = await fetch('/api/auth/me');
        if (!res.ok) {
            window.location.href = '/login.html';
            return null;
        }
        const user = await res.json();
        window.currentUser = user;
        renderUserNavbar(user);
        return user;
    } catch (e) {
        window.location.href = '/login.html';
        return null;
    }
}

function renderUserNavbar(user) {
    const navList = document.querySelector('.app-header .navbar-nav.ms-auto');
    if (!navList || document.getElementById('authUserBadge')) return;

    const roleMeta = {
        ADMIN: { label: 'Admin', cls: 'bg-danger-subtle text-danger border-danger-subtle', icon: 'bi-shield-lock-fill' },
        LECTURER: { label: 'Giảng viên', cls: 'bg-primary-subtle text-primary border-primary-subtle', icon: 'bi-person-workspace' },
        STUDENT: { label: 'Sinh viên (Chỉ xem)', cls: 'bg-success-subtle text-success border-success-subtle', icon: 'bi-person-badge' },
    }[user.role] || { label: user.role, cls: 'bg-secondary-subtle text-secondary', icon: 'bi-person' };

    const userLi = document.createElement('li');
    userLi.id = 'authUserBadge';
    userLi.className = 'nav-item d-flex align-items-center gap-2 me-2';
    userLi.innerHTML = `
        <span class="badge ${roleMeta.cls} border px-3 py-2 rounded-pill">
            <i class="bi ${roleMeta.icon} me-1"></i>${escapeHtml(user.fullName)} · ${roleMeta.label}
        </span>
        <button type="button" id="logoutBtn" class="btn btn-outline-secondary btn-sm rounded-pill px-3" title="Đăng xuất">
            <i class="bi bi-box-arrow-right me-1"></i>Đăng xuất
        </button>
    `;
    navList.appendChild(userLi);

    document.getElementById('logoutBtn')?.addEventListener('click', async () => {
        try {
            await fetch('/api/auth/logout', { method: 'POST' });
        } finally {
            window.location.href = '/login.html';
        }
    });
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (ch) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    })[ch]);
}
