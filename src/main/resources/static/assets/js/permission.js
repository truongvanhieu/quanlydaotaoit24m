const API_URL = "";

const TOKEN_KEY = "n1.jwt";

let roles = [];
let permissions = [];
let assignedPermissions = [];
let selectedRole = null;

function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

async function apiRequest(url, options = {}) {
  const token = getToken();

  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(API_URL + url, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem("n1.userId");
    sessionStorage.removeItem("n1.username");
    window.location.href = "/auth.html";
    throw new Error("Phiên đăng nhập hết hạn");
  }

  if (response.status === 403) {
    const error = new Error(
      "Bạn không có quyền ROLE_MANAGE để truy cập trang này.",
    );
    error.status = response.status;
    throw error;
  }

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Có lỗi xảy ra khi gọi API");
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

function showError(message) {
  alert(message);
}

function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => {
    const entities = {
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;",
    };

    return entities[character];
  });
}

async function loadRoles() {
  roles = await apiRequest("/api/roles/manage");

  document.getElementById("totalRoles").textContent = roles.length;

  renderRoles();

  if (roles.length > 0) {
    await selectRole(roles[0]);
  } else {
    document.getElementById("roleList").textContent = "Không có vai trò nào.";
  }
}

function renderRoles() {
  const roleList = document.getElementById("roleList");

  roleList.innerHTML = roles
    .map(
      (role) => `
        <button
            class="role-item ${selectedRole?.id === role.id ? "active" : ""}"
            data-role-id="${escapeHTML(role.id)}"
        >
            <strong>${escapeHTML(role.name || role.code)}</strong>
            <small>${escapeHTML(role.code)}</small>
        </button>
    `,
    )
    .join("");

  roleList.querySelectorAll(".role-item").forEach((button) => {
    button.addEventListener("click", () => {
      const role = roles.find((item) => item.id === button.dataset.roleId);

      if (role) {
        selectRole(role);
      }
    });
  });
}

async function selectRole(role) {
  selectedRole = role;

  document.getElementById("selectedRole").textContent = role.name || role.code;

  renderRoles();

  await loadAssignedPermissions();
}

async function loadPermissions() {
  permissions = await apiRequest("/api/permissions/manage");

  document.getElementById("totalPermissions").textContent =
    permissions.length;

  renderPermissions();
}

document
  .getElementById("newPermissionForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const formData = new FormData(form);

    try {
      const permission = await apiRequest("/api/permissions/manage", {
        method: "POST",
        body: JSON.stringify(Object.fromEntries(formData.entries())),
      });

      permissions.push(permission);
      document.getElementById("totalPermissions").textContent =
        permissions.length;
      form.reset();
      renderPermissions();
      alert("Thêm quyền thành công.");
    } catch (error) {
      showError("Không thể thêm quyền: " + error.message);
    }
  });

async function loadAssignedPermissions() {
  if (!selectedRole) return;

  try {
    assignedPermissions = await apiRequest(
      `/api/roles/manage/${encodeURIComponent(selectedRole.code)}/permissions`,
    );

    document.getElementById("assignedPermissions").textContent =
      assignedPermissions.length;

    renderPermissions();
  } catch (error) {
    showError("Không thể tải quyền của vai trò: " + error.message);
  }
}

function renderPermissions() {
  const permissionList = document.getElementById("permissionList");
  const keyword = document
    .getElementById("searchPermission")
    .value.trim()
    .toLowerCase();

  if (!selectedRole) {
    permissionList.textContent = "Vui lòng chọn vai trò.";
    return;
  }

  const assignedIds = new Set(
    assignedPermissions.map((permission) => permission.id),
  );

  const filteredPermissions = permissions.filter((permission) => {
    const content = [
      permission.code,
      permission.name,
      permission.module,
      permission.description,
    ]
      .join(" ")
      .toLowerCase();

    return content.includes(keyword);
  });

  const assigned = filteredPermissions.filter((permission) =>
    assignedIds.has(permission.id),
  );

  const available = filteredPermissions.filter(
    (permission) => !assignedIds.has(permission.id),
  );

  let html = `
        <div class="permission-group">
            <h4>Quyền đã cấp (${assigned.length})</h4>
    `;

  if (assigned.length === 0) {
    html += `<p>Vai trò này chưa được cấp quyền nào.</p>`;
  } else {
    html += assigned
      .map(
        (permission) => `
            <div class="permission-item">
                <div>
                    <strong>${escapeHTML(permission.name)}</strong>
                    <small>${escapeHTML(permission.code)}</small>
                    <small>${escapeHTML(permission.module)}</small>
                    <p>${escapeHTML(permission.description)}</p>
                </div>

                <button
                    class="remove-button"
                    data-permission-id="${escapeHTML(permission.id)}"
                >
                    Gỡ quyền
                </button>
            </div>
        `,
      )
      .join("");
  }

  html += `
        </div>

        <div class="permission-group">
            <h4>Quyền chưa cấp (${available.length})</h4>
    `;

  if (available.length === 0) {
    html += `<p>Không còn quyền nào để gán.</p>`;
  } else {
    html += available
      .map(
        (permission) => `
            <div class="permission-item">
                <div>
                    <strong>${escapeHTML(permission.name)}</strong>
                    <small>${escapeHTML(permission.code)}</small>
                    <small>${escapeHTML(permission.module)}</small>
                    <p>${escapeHTML(permission.description)}</p>
                </div>

                <button
                    class="assign-button"
                    data-permission-id="${escapeHTML(permission.id)}"
                >
                    Gán quyền
                </button>
            </div>
        `,
      )
      .join("");
  }

  html += `</div>`;

  const scrollY = window.scrollY;
  if (permissionList.contains(document.activeElement)) {
    document.activeElement.blur();
  }

  permissionList.innerHTML = html;
  requestAnimationFrame(() => window.scrollTo(0, scrollY));

  permissionList.querySelectorAll(".remove-button").forEach((button) => {
    button.addEventListener("click", () => {
      removePermission(button.dataset.permissionId);
    });
  });

  permissionList.querySelectorAll(".assign-button").forEach((button) => {
    button.addEventListener("click", () => {
      assignPermission(button.dataset.permissionId);
    });
  });
}

async function assignPermission(permissionId) {
  if (!selectedRole) return;

  const confirmed = confirm("Bạn có chắc muốn gán quyền này?");

  if (!confirmed) return;

  try {
    await apiRequest(`/api/roles/${selectedRole.id}/permissions`, {
      method: "POST",
      body: JSON.stringify({
        permissionId: permissionId,
      }),
    });

    alert("Gán quyền thành công!");

    await loadAssignedPermissions();
  } catch (error) {
    showError("Không thể gán quyền: " + error.message);
  }
}

async function removePermission(permissionId) {
  if (!selectedRole) return;

  const confirmed = confirm("Bạn có chắc muốn gỡ quyền này?");

  if (!confirmed) return;

  try {
    await apiRequest(
      `/api/roles/${selectedRole.id}/permissions/${permissionId}`,
      {
        method: "DELETE",
      },
    );

    alert("Gỡ quyền thành công!");

    await loadAssignedPermissions();
  } catch (error) {
    showError("Không thể gỡ quyền: " + error.message);
  }
}

document
  .getElementById("searchPermission")
  .addEventListener("input", renderPermissions);

document.getElementById("logoutButton").addEventListener("click", () => {
  const confirmed = confirm("Bạn có chắc muốn đăng xuất?");

  if (!confirmed) return;

  sessionStorage.removeItem("n1.jwt");
  sessionStorage.removeItem("n1.userId");
  sessionStorage.removeItem("n1.username");

  window.location.href = "/auth.html";
});
async function init() {
  if (!getToken()) {
    alert("Vui lòng đăng nhập trước.");
    window.location.replace("/auth.html");
    return;
  }

  try {
    await loadRoles();
    await loadPermissions();
  } catch (error) {
    if (error.status === 403) {
      alert(error.message);
      window.location.replace("/index.html");
      return;
    }
    showError("Không thể tải dữ liệu phân quyền: " + error.message);
  }
}

init();
