document.addEventListener("DOMContentLoaded", async () => {
	const setCount = async (url, targetId) => {
		const result = await adminApi.get(url);
		document.getElementById(targetId).textContent = result.totalElements.toLocaleString("vi-VN");
	};

	const statusBadge = document.getElementById("api-status-badge");
	const statusDetail = document.getElementById("api-status-detail");
	try {
		const health = await adminApi.get("/api/v1/health");
		statusBadge.className = "status-pill status-ok";
		statusBadge.textContent = "Đang hoạt động";
		statusDetail.textContent = `API kết nối thành công tới database ${health.database}.`;
	} catch (error) {
		statusBadge.className = "status-pill status-error";
		statusBadge.textContent = "Không kết nối";
		statusDetail.textContent = error.message;
	}

	const counts = [
		["/api/v1/majors?page=0&limit=1", "major-count"],
		["/api/v1/training-programs?page=0&limit=1", "program-count"],
		["/api/v1/courses?page=0&limit=1", "course-count"]
	];
	await Promise.all(counts.map(async ([url, id]) => {
		try {
			await setCount(url, id);
		} catch (error) {
			document.getElementById(id).textContent = "!";
			adminUI.notify(error.message, "danger");
		}
	}));
});
