
(() => {
    const API = {
        periods: '/api/registration-periods',
        registrations: '/api/course-registrations',
        equivalences: '/api/course-equivalences',
        options: '/api/registration-options'
    };

    async function request(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...(options.body
                    ? { 'Content-Type': 'application/json' }
                    : {}),
                ...options.headers
            }
        });

        const raw = response.status === 204
            ? ''
            : await response.text();

        let data = null;

        if (raw) {
            try {
                data = JSON.parse(raw);
            } catch {
                data = raw;
            }
        }

        if (!response.ok) {
            const detail = data && typeof data === 'object'
                ? (
                    data.detail ||
                    data.message ||
                    data.error ||
                    data.title
                )
                : data;

            throw new Error(detail || `HTTP ${response.status}`);
        }

        return data;
    }

    function esc(value) {
        return String(value ?? '').replace(/[&<>"']/g, ch => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[ch]));
    }

    function showMessage(text, type = 'info') {
        const el = document.getElementById('message');

        if (!el) return;

        el.textContent = text;
        el.className = `notice show ${type}`;
    }

    function clearMessage() {
        const el = document.getElementById('message');

        if (!el) return;

        el.textContent = '';
        el.className = 'notice';
    }

    function fillSelect(id, rows, placeholder, labelFn) {
        const select = document.getElementById(id);

        if (!select) return;

        const previous = select.value;

        select.replaceChildren();

        const first = document.createElement('option');
        first.value = '';
        first.textContent = placeholder;

        select.append(first);

        for (const row of rows || []) {
            const option = document.createElement('option');

            option.value = row.id;
            option.textContent = labelFn(row);

            select.append(option);
        }

        if ([...select.options].some(option => option.value === previous)) {
            select.value = previous;
        }
    }

    function dateTimeInput(value) {
        return value ? String(value).slice(0, 16) : '';
    }

    function dateTimeText(value) {
        return value
            ? String(value).replace('T', ' ').slice(0, 16)
            : '—';
    }

    function dateText(value) {
        return value ? String(value).slice(0, 10) : '—';
    }

    function typeText(value) {
        return ({
            1: 'Học mới',
            2: 'Học lại',
            3: 'Cải thiện'
        })[Number(value)] || 'Không xác định';
    }

    function statusText(value) {
        return ({
            1: 'Chờ xử lý',
            2: 'Đang đăng ký',
            3: 'Đã hủy'
        })[Number(value)] || 'Không xác định';
    }

    function navActive() {
        const path = window.location.pathname;

        document.querySelectorAll('[data-nav]').forEach(link => {
            if (link.getAttribute('href') === path) {
                link.classList.add('active');
            }
        });
    }

    window.N6 = {
        API,
        request,
        esc,
        showMessage,
        clearMessage,
        fillSelect,
        dateTimeInput,
        dateTimeText,
        dateText,
        typeText,
        statusText,
        navActive
    };

    document.addEventListener('DOMContentLoaded', navActive);
})();
