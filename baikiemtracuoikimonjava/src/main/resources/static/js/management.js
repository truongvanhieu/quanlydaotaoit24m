(() => {
	const root = document.getElementById("management-root");
	const page = document.body.dataset.page;
	const api = window.adminApi;
	const ui = window.adminUI;
	const escape = ui.escape;
	const pageState = { page: 0, limit: 10, query: "" };

	function showError(error) {
		ui.notify(error.message || "Đã xảy ra lỗi.", "danger");
	}

	function setPager(result) {
		document.getElementById("page-info").textContent =
			`Trang ${result.totalPages === 0 ? 0 : result.page + 1}/${result.totalPages} · ${result.totalElements} bản ghi`;
		document.getElementById("previous-page").disabled = result.first;
		document.getElementById("next-page").disabled = result.last;
	}

	function addPagerHandlers(load) {
		document.getElementById("previous-page").addEventListener("click", async () => {
			if (pageState.page > 0) {
				pageState.page -= 1;
				await load();
			}
		});
		document.getElementById("next-page").addEventListener("click", async () => {
			pageState.page += 1;
			await load();
		});
		document.getElementById("page-limit").addEventListener("change", async event => {
			pageState.limit = Number(event.target.value);
			pageState.page = 0;
			await load();
		});
	}

	function modal(id) {
		return bootstrap.Modal.getOrCreateInstance(document.getElementById(id));
	}

	function statusBadge(active) {
		return active
			? '<span class="state-badge state-active">Đang sử dụng</span>'
			: '<span class="state-badge state-inactive">Ngừng sử dụng</span>';
	}

	function actions(id) {
		return `<div class="row-actions">
			<button class="btn btn-sm btn-light" data-action="detail" data-id="${id}">Chi tiết</button>
			<button class="btn btn-sm btn-outline-primary" data-action="edit" data-id="${id}">Sửa</button>
			<button class="btn btn-sm btn-outline-danger" data-action="deactivate" data-id="${id}">Ngừng</button>
		</div>`;
	}

	function baseTable({ subtitle, addLabel, filters, columns, formFields }) {
		root.innerHTML = `
			<div class="data-card">
				<div class="content-heading">
					<div><p class="eyebrow mb-1">DANH SÁCH DỮ LIỆU</p><h2>${subtitle}</h2></div>
					<button class="btn btn-primary" id="add-record" type="button">+ ${addLabel}</button>
				</div>
				<form class="toolbar" id="search-form">
					<div class="filter-form">${filters}</div>
					<button class="btn btn-outline-secondary" type="submit">Tìm kiếm / lọc</button>
				</form>
				<div class="table-responsive">
					<table class="table table-hover">
						<thead><tr>${columns.map(column => `<th>${column}</th>`).join("")}</tr></thead>
						<tbody id="records"><tr><td colspan="${columns.length}" class="empty-state">Đang tải dữ liệu...</td></tr></tbody>
					</table>
				</div>
				<div class="pagination-bar">
					<span id="page-info">Đang tải...</span>
					<div class="d-flex align-items-center gap-2">
						<select class="form-select form-select-sm" id="page-limit" aria-label="Số bản ghi mỗi trang">
							<option value="10">10 / trang</option><option value="20">20 / trang</option><option value="50">50 / trang</option>
						</select>
						<button class="btn btn-sm btn-light" id="previous-page" type="button">Trước</button>
						<button class="btn btn-sm btn-light" id="next-page" type="button">Sau</button>
					</div>
				</div>
			</div>
			<div class="modal fade" id="editor-modal" tabindex="-1" aria-hidden="true">
				<div class="modal-dialog modal-lg modal-dialog-scrollable">
					<form class="modal-content" id="editor-form">
						<div class="modal-header">
							<h2 class="modal-title fs-5" id="editor-title">${addLabel}</h2>
							<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
						</div>
						<div class="modal-body">
							<input type="hidden" name="id">
							${formFields}
						</div>
						<div class="modal-footer">
							<button class="btn btn-light" type="button" data-bs-dismiss="modal">Hủy</button>
							<button class="btn btn-primary" type="submit">Lưu dữ liệu</button>
						</div>
					</form>
				</div>
			</div>`;
	}

	function renderMajorPage() {
		baseTable({
			subtitle: "Danh mục ngành đào tạo",
			addLabel: "Thêm ngành",
			filters: '<input class="form-control" id="search-query" placeholder="Tìm mã hoặc tên ngành">',
			columns: ["Mã ngành", "Tên ngành", "Mô tả", "Trạng thái", "Thao tác"],
			formFields: `
				<div class="row g-3">
					<div class="col-md-4"><label class="form-label">Mã ngành</label><input class="form-control" name="majorCode" maxlength="20" required></div>
					<div class="col-md-8"><label class="form-label">Tên ngành</label><input class="form-control" name="majorName" maxlength="150" required></div>
					<div class="col-12"><label class="form-label">Mô tả</label><textarea class="form-control" name="description" rows="3"></textarea></div>
					<div class="col-md-4"><label class="form-label">Trạng thái</label><select class="form-select" name="isActive"><option value="true">Đang sử dụng</option><option value="false">Ngừng sử dụng</option></select></div>
				</div>`
		});

		const form = document.getElementById("editor-form");
		const editor = modal("editor-modal");

		async function load() {
			const query = new URLSearchParams({
				page: pageState.page,
				limit: pageState.limit
			});
			if (pageState.query) query.set("q", pageState.query);
			const result = await api.get(`/api/v1/majors?${query}`);
			document.getElementById("records").innerHTML = result.content.length
				? result.content.map(item => `<tr>
					<td class="table-code">${escape(item.majorCode)}</td>
					<td>${escape(item.majorName)}</td>
					<td>${escape(item.description || "—")}</td>
					<td>${statusBadge(item.isActive)}</td>
					<td>${actions(item.id)}</td>
				</tr>`).join("")
				: '<tr><td colspan="5" class="empty-state">Không tìm thấy ngành đào tạo.</td></tr>';
			setPager(result);
		}

		document.getElementById("add-record").addEventListener("click", () => {
			form.reset();
			form.elements.id.value = "";
			form.elements.isActive.value = "true";
			document.getElementById("editor-title").textContent = "Thêm ngành đào tạo";
			editor.show();
		});
		document.getElementById("search-form").addEventListener("submit", async event => {
			event.preventDefault();
			pageState.query = document.getElementById("search-query").value.trim();
			pageState.page = 0;
			try { await load(); } catch (error) { showError(error); }
		});
		addPagerHandlers(() => load().catch(showError));
		document.getElementById("records").addEventListener("click", async event => {
			const button = event.target.closest("button[data-action]");
			if (!button) return;
			const id = button.dataset.id;
			try {
				if (button.dataset.action === "detail") {
					ui.showDetails("Chi tiết ngành đào tạo", await api.get(`/api/v1/majors/${id}`));
				} else if (button.dataset.action === "edit") {
					const item = await api.get(`/api/v1/majors/${id}`);
					for (const field of ["majorCode", "majorName", "description"]) form.elements[field].value = item[field] || "";
					form.elements.isActive.value = String(item.isActive);
					form.elements.id.value = item.id;
					document.getElementById("editor-title").textContent = "Cập nhật ngành đào tạo";
					editor.show();
				} else if (button.dataset.action === "deactivate"
						&& window.confirm("Ngừng sử dụng ngành này? Dữ liệu không bị xóa khỏi database.")) {
					await api.delete(`/api/v1/majors/${id}`);
					ui.notify("Đã ngừng sử dụng ngành đào tạo.");
					await load();
				}
			} catch (error) { showError(error); }
		});
		form.addEventListener("submit", async event => {
			event.preventDefault();
			const id = form.elements.id.value;
			const body = {
				majorCode: form.elements.majorCode.value,
				majorName: form.elements.majorName.value,
				description: form.elements.description.value,
				isActive: form.elements.isActive.value === "true"
			};
			try {
				if (id) await api.put(`/api/v1/majors/${id}`, body);
				else await api.post("/api/v1/majors", body);
				editor.hide();
				ui.notify(id ? "Đã cập nhật ngành đào tạo." : "Đã thêm ngành đào tạo.");
				await load();
			} catch (error) { showError(error); }
		});
		load().catch(showError);
	}

	function renderCoursePage() {
		baseTable({
			subtitle: "Danh mục môn học và học phần",
			addLabel: "Thêm môn học",
			filters: `
				<input class="form-control" id="search-query" placeholder="Tìm mã hoặc tên môn">
				<input class="form-control" id="credits-filter" type="number" min="0" placeholder="Số tín chỉ">`,
			columns: ["Mã môn", "Tên môn", "Tín chỉ (LT/TH)", "Trạng thái", "Thao tác"],
			formFields: `
				<div class="row g-3">
					<div class="col-md-4"><label class="form-label">Mã môn</label><input class="form-control" name="courseCode" maxlength="20" required></div>
					<div class="col-md-8"><label class="form-label">Tên môn</label><input class="form-control" name="courseName" maxlength="200" required></div>
					<div class="col-md-4"><label class="form-label">Tổng số tín chỉ</label><input class="form-control" name="credits" type="number" min="0" required></div>
					<div class="col-md-4"><label class="form-label">Tín chỉ lý thuyết</label><input class="form-control" name="theoryCredits" type="number" min="0" required></div>
					<div class="col-md-4"><label class="form-label">Tín chỉ thực hành</label><input class="form-control" name="practiceCredits" type="number" min="0" required></div>
					<div class="col-12"><label class="form-label">Mô tả</label><textarea class="form-control" name="description" rows="3"></textarea></div>
					<div class="col-md-4"><label class="form-label">Trạng thái</label><select class="form-select" name="isActive"><option value="true">Đang sử dụng</option><option value="false">Ngừng sử dụng</option></select></div>
				</div>`
		});

		const form = document.getElementById("editor-form");
		const editor = modal("editor-modal");

		async function load() {
			const query = new URLSearchParams({ page: pageState.page, limit: pageState.limit });
			if (pageState.query) query.set("q", pageState.query);
			const credits = document.getElementById("credits-filter").value;
			if (credits !== "") query.set("credits", credits);
			const result = await api.get(`/api/v1/courses?${query}`);
			document.getElementById("records").innerHTML = result.content.length
				? result.content.map(item => `<tr>
					<td class="table-code">${escape(item.courseCode)}</td>
					<td>${escape(item.courseName)}</td>
					<td>${item.credits} <span class="text-secondary">(${item.theoryCredits}/${item.practiceCredits})</span></td>
					<td>${statusBadge(item.isActive)}</td>
					<td>${actions(item.id)}</td>
				</tr>`).join("")
				: '<tr><td colspan="5" class="empty-state">Không tìm thấy môn học.</td></tr>';
			setPager(result);
		}

		document.getElementById("add-record").addEventListener("click", () => {
			form.reset();
			form.elements.id.value = "";
			form.elements.isActive.value = "true";
			document.getElementById("editor-title").textContent = "Thêm môn học";
			editor.show();
		});
		document.getElementById("search-form").addEventListener("submit", async event => {
			event.preventDefault();
			pageState.query = document.getElementById("search-query").value.trim();
			pageState.page = 0;
			try { await load(); } catch (error) { showError(error); }
		});
		addPagerHandlers(() => load().catch(showError));
		document.getElementById("records").addEventListener("click", async event => {
			const button = event.target.closest("button[data-action]");
			if (!button) return;
			const id = button.dataset.id;
			try {
				if (button.dataset.action === "detail") {
					ui.showDetails("Chi tiết môn học", await api.get(`/api/v1/courses/${id}`));
				} else if (button.dataset.action === "edit") {
					const item = await api.get(`/api/v1/courses/${id}`);
					for (const field of ["courseCode", "courseName", "credits", "theoryCredits", "practiceCredits", "description"]) {
						form.elements[field].value = item[field] ?? "";
					}
					form.elements.isActive.value = String(item.isActive);
					form.elements.id.value = item.id;
					document.getElementById("editor-title").textContent = "Cập nhật môn học";
					editor.show();
				} else if (button.dataset.action === "deactivate"
						&& window.confirm("Ngừng sử dụng môn này? Dữ liệu không bị xóa khỏi database.")) {
					await api.delete(`/api/v1/courses/${id}`);
					ui.notify("Đã ngừng sử dụng môn học.");
					await load();
				}
			} catch (error) { showError(error); }
		});
		form.addEventListener("submit", async event => {
			event.preventDefault();
			const id = form.elements.id.value;
			const body = {
				courseCode: form.elements.courseCode.value,
				courseName: form.elements.courseName.value,
				credits: Number(form.elements.credits.value),
				theoryCredits: Number(form.elements.theoryCredits.value),
				practiceCredits: Number(form.elements.practiceCredits.value),
				description: form.elements.description.value,
				isActive: form.elements.isActive.value === "true"
			};
			try {
				if (id) await api.put(`/api/v1/courses/${id}`, body);
				else await api.post("/api/v1/courses", body);
				editor.hide();
				ui.notify(id ? "Đã cập nhật môn học." : "Đã thêm môn học.");
				await load();
			} catch (error) { showError(error); }
		});
		load().catch(showError);
	}

	async function loadMajors(select, selectedId = "") {
		const result = await api.get("/api/v1/majors?page=0&limit=100");
		select.innerHTML = `<option value="">Tất cả ngành</option>${result.content.map(major =>
			`<option value="${major.id}" ${String(major.id) === String(selectedId) ? "selected" : ""}>${escape(major.majorCode)} — ${escape(major.majorName)}</option>`
		).join("")}`;
		return result.content;
	}

	function renderProgramPage() {
		baseTable({
			subtitle: "Danh mục chương trình đào tạo",
			addLabel: "Thêm chương trình",
			filters: `
				<input class="form-control" id="search-query" placeholder="Tìm mã hoặc tên chương trình">
				<select class="form-select" id="major-filter"><option value="">Tất cả ngành</option></select>
				<input class="form-control" id="cohort-filter" placeholder="Khóa">
				<input class="form-control" id="academic-year-filter" placeholder="Năm học">`,
			columns: ["Mã chương trình", "Tên chương trình", "Ngành", "Khóa / Năm học", "Tín chỉ", "Trạng thái", "Thao tác"],
			formFields: `
				<div class="row g-3">
					<div class="col-md-4"><label class="form-label">Mã chương trình</label><input class="form-control" name="programCode" maxlength="30" required></div>
					<div class="col-md-8"><label class="form-label">Tên chương trình</label><input class="form-control" name="programName" maxlength="200" required></div>
					<div class="col-md-12"><label class="form-label">Ngành đào tạo</label><select class="form-select" name="majorId" required></select></div>
					<div class="col-md-6"><label class="form-label">Khóa</label><input class="form-control" name="cohort" maxlength="20"></div>
					<div class="col-md-6"><label class="form-label">Năm học</label><input class="form-control" name="academicYear" maxlength="20"></div>
					<div class="col-12"><label class="form-label">Mô tả</label><textarea class="form-control" name="description" rows="3"></textarea></div>
					<div class="col-md-4"><label class="form-label">Trạng thái</label><select class="form-select" name="isActive"><option value="true">Đang áp dụng</option><option value="false">Ngừng áp dụng</option></select></div>
				</div>`
		});

		const form = document.getElementById("editor-form");
		const editor = modal("editor-modal");
		loadMajors(document.getElementById("major-filter")).catch(showError);
		loadMajors(form.elements.majorId, "").then(majors => {
			if (majors.length === 0) {
				form.elements.majorId.disabled = true;
				document.getElementById("add-record").disabled = true;
				ui.notify("Cần có ít nhất một ngành đào tạo trước khi tạo chương trình.", "warning");
			}
		}).catch(showError);

		async function load() {
			const query = new URLSearchParams({ page: pageState.page, limit: pageState.limit });
			if (pageState.query) query.set("q", pageState.query);
			const majorId = document.getElementById("major-filter").value;
			const cohort = document.getElementById("cohort-filter").value.trim();
			const academicYear = document.getElementById("academic-year-filter").value.trim();
			if (majorId) query.set("majorId", majorId);
			if (cohort) query.set("cohort", cohort);
			if (academicYear) query.set("academicYear", academicYear);
			const result = await api.get(`/api/v1/training-programs?${query}`);
			document.getElementById("records").innerHTML = result.content.length
				? result.content.map(item => `<tr>
					<td class="table-code">${escape(item.programCode)}</td>
					<td>${escape(item.programName)}</td>
					<td>${escape(item.majorCode)} — ${escape(item.majorName)}</td>
					<td>${escape(item.cohort || "—")} / ${escape(item.academicYear || "—")}</td>
					<td>${item.totalCredits}</td>
					<td>${statusBadge(item.isActive)}</td>
					<td>${actions(item.id)}</td>
				</tr>`).join("")
				: '<tr><td colspan="7" class="empty-state">Không tìm thấy chương trình đào tạo.</td></tr>';
			setPager(result);
		}

		document.getElementById("add-record").addEventListener("click", () => {
			form.reset();
			form.elements.id.value = "";
			form.elements.isActive.value = "true";
			document.getElementById("editor-title").textContent = "Thêm chương trình đào tạo";
			editor.show();
		});
		document.getElementById("search-form").addEventListener("submit", async event => {
			event.preventDefault();
			pageState.query = document.getElementById("search-query").value.trim();
			pageState.page = 0;
			try { await load(); } catch (error) { showError(error); }
		});
		for (const id of ["major-filter", "cohort-filter", "academic-year-filter"]) {
			const input = document.getElementById(id);
			input.addEventListener(id === "major-filter" ? "change" : "input", () => {
				pageState.page = 0;
				load().catch(showError);
			});
		}
		addPagerHandlers(() => load().catch(showError));
		document.getElementById("records").addEventListener("click", async event => {
			const button = event.target.closest("button[data-action]");
			if (!button) return;
			const id = button.dataset.id;
			try {
				if (button.dataset.action === "detail") {
					ui.showDetails("Chi tiết chương trình đào tạo", await api.get(`/api/v1/training-programs/${id}`));
				} else if (button.dataset.action === "edit") {
					const item = await api.get(`/api/v1/training-programs/${id}`);
					for (const field of ["programCode", "programName", "cohort", "academicYear", "description"]) {
						form.elements[field].value = item[field] || "";
					}
					form.elements.majorId.value = item.majorId;
					form.elements.isActive.value = String(item.isActive);
					form.elements.id.value = item.id;
					document.getElementById("editor-title").textContent = "Cập nhật chương trình đào tạo";
					editor.show();
				} else if (button.dataset.action === "deactivate"
						&& window.confirm("Ngừng áp dụng chương trình này? Dữ liệu không bị xóa khỏi database.")) {
					await api.delete(`/api/v1/training-programs/${id}`);
					ui.notify("Đã ngừng áp dụng chương trình.");
					await load();
				}
			} catch (error) { showError(error); }
		});
		form.addEventListener("submit", async event => {
			event.preventDefault();
			const id = form.elements.id.value;
			const body = {
				programCode: form.elements.programCode.value,
				programName: form.elements.programName.value,
				majorId: Number(form.elements.majorId.value),
				cohort: form.elements.cohort.value,
				academicYear: form.elements.academicYear.value,
				description: form.elements.description.value,
				isActive: form.elements.isActive.value === "true"
			};
			try {
				if (id) await api.put(`/api/v1/training-programs/${id}`, body);
				else await api.post("/api/v1/training-programs", body);
				editor.hide();
				ui.notify(id ? "Đã cập nhật chương trình đào tạo." : "Đã tạo chương trình đào tạo.");
				await load();
			} catch (error) { showError(error); }
		});
		load().catch(showError);
	}

	async function loadProgramOptions(select) {
		const result = await api.get("/api/v1/training-programs?page=0&limit=100");
		select.innerHTML = result.content.map(program =>
			`<option value="${program.id}">${escape(program.programCode)} — ${escape(program.programName)}</option>`
		).join("");
		return result.content;
	}

	async function renderProgramCoursesPage() {
		root.innerHTML = `
			<div class="surface-card mb-3">
				<div class="row g-3 align-items-end">
					<div class="col-md-8"><label class="form-label" for="program-select">Chương trình đào tạo</label><select class="form-select" id="program-select"></select></div>
					<div class="col-md-4"><div class="stat-card py-2"><div class="stat-icon icon-blue">∑</div><div><span>Tổng số tín chỉ</span><strong id="program-credit-total">—</strong></div></div></div>
				</div>
			</div>
			<div class="data-card mb-3">
				<div class="content-heading"><div><p class="eyebrow mb-1">KẾ HOẠCH HỌC TẬP</p><h2>Môn học theo học kỳ</h2></div></div>
				<div class="p-3" id="semester-list"><div class="empty-state">Chọn chương trình để xem các môn học.</div></div>
			</div>
			<div class="surface-card">
				<div class="section-heading mb-3"><div><p class="eyebrow mb-1">CẬP NHẬT CHƯƠNG TRÌNH</p><h2>Gán môn học vào chương trình</h2></div></div>
				<form class="row g-3 align-items-end" id="assign-form">
					<div class="col-lg-5"><label class="form-label" for="assign-course">Môn học</label><select class="form-select" id="assign-course" required></select></div>
					<div class="col-lg-2"><label class="form-label" for="assign-semester">Học kỳ dự kiến</label><input class="form-control" id="assign-semester" type="number" min="1" value="1" required></div>
					<div class="col-lg-3"><label class="form-label" for="assign-compulsory">Phân loại</label><select class="form-select" id="assign-compulsory"><option value="true">Bắt buộc</option><option value="false">Tự chọn</option></select></div>
					<div class="col-lg-2"><button class="btn btn-primary w-100" type="submit">Gán môn học</button></div>
				</form>
			</div>`;

		const programSelect = document.getElementById("program-select");
		const courseSelect = document.getElementById("assign-course");
		let currentProgram = null;
		let availableCourses = [];

		async function loadDetail() {
			if (!programSelect.value) {
				document.getElementById("semester-list").innerHTML = '<div class="empty-state">Chưa có chương trình đào tạo.</div>';
				document.getElementById("program-credit-total").textContent = "0";
				return;
			}
			currentProgram = await api.get(`/api/v1/training-programs/${programSelect.value}`);
			document.getElementById("program-credit-total").textContent =
				currentProgram.totalCredits.toLocaleString("vi-VN");
			const groups = new Map();
			for (const course of currentProgram.courses) {
				if (!groups.has(course.semesterIndex)) groups.set(course.semesterIndex, []);
				groups.get(course.semesterIndex).push(course);
			}
			const semesters = [...groups.keys()].sort((first, second) => first - second);
			document.getElementById("semester-list").innerHTML = semesters.length
				? semesters.map(semester => `<section class="semester-card">
					<div class="semester-heading"><h3>Học kỳ ${semester}</h3><span class="text-secondary small">${groups.get(semester).reduce((sum, course) => sum + course.credits, 0)} tín chỉ</span></div>
					<div class="table-responsive"><table class="table"><thead><tr><th>Mã môn</th><th>Tên môn</th><th>Tín chỉ</th><th>Phân loại</th><th></th></tr></thead><tbody>
						${groups.get(semester).map(course => `<tr>
							<td class="table-code">${escape(course.courseCode)}</td>
							<td>${escape(course.courseName)}</td>
							<td>${course.credits}</td>
							<td>${course.isCompulsory ? "Bắt buộc" : "Tự chọn"}</td>
							<td><button class="btn btn-sm btn-outline-danger" data-remove-course="${course.courseId}">Gỡ</button></td>
						</tr>`).join("")}
					</tbody></table></div>
				</section>`).join("")
				: '<div class="empty-state">Chương trình chưa có môn học nào.</div>';
			const assigned = new Set(currentProgram.courses.map(course => String(course.courseId)));
			courseSelect.innerHTML = availableCourses
				.filter(course => course.isActive && !assigned.has(String(course.id)))
				.map(course => `<option value="${course.id}">${escape(course.courseCode)} — ${escape(course.courseName)} (${course.credits} tín chỉ)</option>`)
				.join("");
			document.querySelector("#assign-form button[type=submit]").disabled = courseSelect.options.length === 0;
		}

		try {
			const programs = await loadProgramOptions(programSelect);
			const courses = await api.get("/api/v1/courses?page=0&limit=100");
			availableCourses = courses.content;
			if (programs.length === 0) {
				programSelect.innerHTML = "";
				document.getElementById("assign-form").hidden = true;
				await loadDetail();
				return;
			}
			await loadDetail();
		} catch (error) { showError(error); }

		programSelect.addEventListener("change", () => loadDetail().catch(showError));
		document.getElementById("semester-list").addEventListener("click", async event => {
			const button = event.target.closest("button[data-remove-course]");
			if (!button || !currentProgram) return;
			if (!window.confirm("Gỡ môn học này khỏi chương trình?")) return;
			try {
				await api.delete(`/api/v1/training-programs/${currentProgram.id}/courses/${button.dataset.removeCourse}`);
				ui.notify("Đã gỡ môn học khỏi chương trình.");
				await loadDetail();
			} catch (error) { showError(error); }
		});
		document.getElementById("assign-form").addEventListener("submit", async event => {
			event.preventDefault();
			if (!currentProgram || !courseSelect.value) return;
			try {
				await api.post(`/api/v1/training-programs/${currentProgram.id}/courses`, {
					courseId: Number(courseSelect.value),
					semesterIndex: Number(document.getElementById("assign-semester").value),
					isCompulsory: document.getElementById("assign-compulsory").value === "true"
				});
				ui.notify("Đã gán môn học vào chương trình.");
				await loadDetail();
			} catch (error) { showError(error); }
		});
	}

	async function renderPrerequisitesPage() {
		root.innerHTML = `
			<div class="surface-card mb-3">
				<div class="row g-3 align-items-end">
					<div class="col-md-8"><label class="form-label" for="course-select">Môn học cần khai báo tiên quyết</label><select class="form-select" id="course-select"></select></div>
					<div class="col-md-4"><span class="text-secondary small">Hệ thống sẽ chặn tự tham chiếu, quan hệ trùng và chu trình.</span></div>
				</div>
			</div>
			<div class="data-card mb-3">
				<div class="content-heading"><div><p class="eyebrow mb-1">MÔN HỌC TIÊN QUYẾT</p><h2>Danh sách quan hệ</h2></div></div>
				<div class="table-responsive"><table class="table"><thead><tr><th>Mã môn tiên quyết</th><th>Tên môn</th><th>Loại quan hệ</th><th></th></tr></thead><tbody id="prerequisite-rows"><tr><td colspan="4" class="empty-state">Chọn môn học để xem dữ liệu.</td></tr></tbody></table></div>
			</div>
			<div class="surface-card">
				<div class="section-heading mb-3"><div><p class="eyebrow mb-1">CẬP NHẬT QUAN HỆ</p><h2>Thêm môn tiên quyết</h2></div></div>
				<form class="row g-3 align-items-end" id="prerequisite-form">
					<div class="col-lg-6"><label class="form-label" for="prerequisite-course">Môn tiên quyết</label><select class="form-select" id="prerequisite-course" required></select></div>
					<div class="col-lg-4"><label class="form-label" for="prerequisite-type">Loại quan hệ</label><select class="form-select" id="prerequisite-type" required></select></div>
					<div class="col-lg-2"><button class="btn btn-primary w-100" type="submit">Gán quan hệ</button></div>
				</form>
			</div>`;

		const courseSelect = document.getElementById("course-select");
		const prerequisiteSelect = document.getElementById("prerequisite-course");
		let courseOptions = [];
		let currentPrerequisites = [];

		async function loadPrerequisites() {
			if (!courseSelect.value) {
				document.getElementById("prerequisite-rows").innerHTML = '<tr><td colspan="4" class="empty-state">Chưa có môn học.</td></tr>';
				return;
			}
			currentPrerequisites = await api.get(`/api/v1/courses/${courseSelect.value}/prerequisites`);
			document.getElementById("prerequisite-rows").innerHTML = currentPrerequisites.length
				? currentPrerequisites.map(item => `<tr>
					<td class="table-code">${escape(item.courseCode)}</td>
					<td>${escape(item.courseName)}</td>
					<td>${escape(item.type)}</td>
					<td><button class="btn btn-sm btn-outline-danger" data-remove-prerequisite="${item.prerequisiteCourseId}">Gỡ</button></td>
				</tr>`).join("")
				: '<tr><td colspan="4" class="empty-state">Môn học này chưa có môn tiên quyết.</td></tr>';
			const existingIds = new Set(currentPrerequisites.map(item => String(item.prerequisiteCourseId)));
			const courseId = courseSelect.value;
			prerequisiteSelect.innerHTML = courseOptions
				.filter(course => String(course.id) !== courseId && !existingIds.has(String(course.id)))
				.map(course => `<option value="${course.id}">${escape(course.courseCode)} — ${escape(course.courseName)}</option>`)
				.join("");
			document.querySelector("#prerequisite-form button[type=submit]").disabled = prerequisiteSelect.options.length === 0;
		}

		try {
			const [courses, metadata] = await Promise.all([
				api.get("/api/v1/courses?page=0&limit=100"),
				api.get("/api/v1/metadata")
			]);
			courseOptions = courses.content.filter(course => course.isActive);
			courseSelect.innerHTML = courseOptions.map(course =>
				`<option value="${course.id}">${escape(course.courseCode)} — ${escape(course.courseName)}</option>`
			).join("");
			document.getElementById("prerequisite-type").innerHTML = metadata.prerequisiteTypes
				.map(type => `<option value="${escape(type)}">${escape(type)}</option>`)
				.join("");
			await loadPrerequisites();
		} catch (error) { showError(error); }

		courseSelect.addEventListener("change", () => loadPrerequisites().catch(showError));
		document.getElementById("prerequisite-rows").addEventListener("click", async event => {
			const button = event.target.closest("button[data-remove-prerequisite]");
			if (!button || !courseSelect.value) return;
			if (!window.confirm("Gỡ quan hệ tiên quyết này?")) return;
			try {
				await api.delete(`/api/v1/courses/${courseSelect.value}/prerequisites/${button.dataset.removePrerequisite}`);
				ui.notify("Đã gỡ quan hệ tiên quyết.");
				await loadPrerequisites();
			} catch (error) { showError(error); }
		});
		document.getElementById("prerequisite-form").addEventListener("submit", async event => {
			event.preventDefault();
			if (!courseSelect.value || !prerequisiteSelect.value) return;
			try {
				await api.post(`/api/v1/courses/${courseSelect.value}/prerequisites`, {
					prerequisiteCourseId: Number(prerequisiteSelect.value),
					type: document.getElementById("prerequisite-type").value
				});
				ui.notify("Đã thêm môn tiên quyết.");
				await loadPrerequisites();
			} catch (error) { showError(error); }
		});
	}

	document.addEventListener("DOMContentLoaded", () => {
		try {
			if (page === "majors") renderMajorPage();
			else if (page === "courses") renderCoursePage();
			else if (page === "training-programs") renderProgramPage();
			else if (page === "program-courses") renderProgramCoursesPage().catch(showError);
			else if (page === "prerequisites") renderPrerequisitesPage().catch(showError);
		} catch (error) {
			showError(error);
		}
	});
})();
