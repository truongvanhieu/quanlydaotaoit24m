(() => {
	function notify(message, type = "success") {
		const area = document.getElementById("toast-area");
		if (!area || !window.bootstrap) {
			window.alert(message);
			return;
		}

		const toast = document.createElement("div");
		toast.className = `toast align-items-center text-bg-${type} border-0`;
		toast.setAttribute("role", "status");
		toast.innerHTML = `
			<div class="d-flex">
				<div class="toast-body"></div>
				<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Đóng"></button>
			</div>`;
		toast.querySelector(".toast-body").textContent = message;
		area.appendChild(toast);
		const instance = new bootstrap.Toast(toast, { delay: 4500 });
		toast.addEventListener("hidden.bs.toast", () => toast.remove());
		instance.show();
	}

	async function request(url, options = {}) {
		const headers = new Headers(options.headers || {});
		if (options.body && !headers.has("Content-Type")) {
			headers.set("Content-Type", "application/json");
		}

		let response;
		try {
			response = await fetch(url, { ...options, headers });
		} catch {
			throw new Error("Không thể kết nối tới máy chủ. Hãy kiểm tra ứng dụng Spring Boot.");
		}

		if (response.status === 204) {
			return null;
		}

		const contentType = response.headers.get("content-type") || "";
		const payload = contentType.includes("application/json")
			? await response.json()
			: null;

		if (!response.ok) {
			throw new Error(payload?.message || `Yêu cầu thất bại (HTTP ${response.status}).`);
		}

		if (!contentType.includes("application/json")) {
			throw new Error("Máy chủ trả về nội dung không phải JSON.");
		}
		return payload;
	}

	window.adminApi = {
		get: url => request(url, { method: "GET" }),
		post: (url, body) => request(url, { method: "POST", body: JSON.stringify(body) }),
		put: (url, body) => request(url, { method: "PUT", body: JSON.stringify(body) }),
		delete: url => request(url, { method: "DELETE" })
	};

	window.adminUI = {
		notify,
		escape(value) {
			return String(value ?? "").replace(/[&<>"']/g, character => ({
				"&": "&amp;",
				"<": "&lt;",
				">": "&gt;",
				'"': "&quot;",
				"'": "&#39;"
			})[character]);
		},
		showDetails(title, value) {
			let modal = document.getElementById("details-modal");
			if (!modal) {
				modal = document.createElement("div");
				modal.id = "details-modal";
				modal.className = "modal fade";
				modal.tabIndex = -1;
				modal.innerHTML = `
					<div class="modal-dialog modal-lg modal-dialog-scrollable">
						<div class="modal-content">
							<div class="modal-header">
								<h2 class="modal-title fs-5"></h2>
								<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
							</div>
							<div class="modal-body"><pre class="details-json mb-0"></pre></div>
						</div>
					</div>`;
				document.body.appendChild(modal);
			}
			modal.querySelector(".modal-title").textContent = title;
			modal.querySelector(".details-json").textContent = JSON.stringify(value, null, 2);
			bootstrap.Modal.getOrCreateInstance(modal).show();
		}
	};
})();
