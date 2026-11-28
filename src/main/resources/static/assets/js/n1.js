const API_BASE = "/api";
const TOKEN_KEY = "n1.jwt";
const USER_ID_KEY = "n1.userId";

const notice = document.getElementById("notice");
const rolesBody = document.getElementById("rolesBody");
const rolePermissionsBody = document.getElementById("rolePermissionsBody");
const permissionsBody = document.getElementById("permissionsBody");
const modulesBody = document.getElementById("modulesBody");
const usersBody = document.getElementById("usersBody");
const userPermissionsBody = document.getElementById("userPermissionsBody");
const userPermissionsByIdBody = document.getElementById("userPermissionsByIdBody");
const userRolesBody = document.getElementById("userRolesBody");
const roleCodeSelect = document.getElementById("roleCodeSelect");
const assignRoleSelect = document.getElementById("assignRoleSelect");
const userLookupSelect = document.getElementById("userLookupSelect");
const rolePermissionPagination = document.getElementById(
  "rolePermissionPagination",
);
const permissionsCatalogPagination = document.getElementById(
  "permissionsCatalogPagination",
);
const userPermissionPagination = document.getElementById(
  "userPermissionPagination",
);
const usersPagination = document.getElementById("usersPagination");
const PERMISSIONS_PER_PAGE = 10;
const USERS_PER_PAGE = 10;

let roles = [];
let managedRoles = [];
let permissionsCatalog = [];
let moduleCatalog = [];
let assignedPermissions = [];
let users = [];
let usersPage = 1;
let usersLoaded = false;
let lookupUsers = [];
let selectedLookupUser = null;
let selectedRoleId = "";
let selectedUser = null;
let selectedUserPermissions = [];
let rolePermissionDraftIds = new Set();
let userPermissionDraftIds = new Set();
let rolePermissionPage = 1;
let permissionsCatalogPage = 1;
let userPermissionPage = 1;
let canManageRoles = false;
let roleManagementApiAvailable = false;
let currentUserId = sessionStorage.getItem(USER_ID_KEY) || "";
let lastNoticeTarget = null;

document.addEventListener(
  "click",
  (event) => {
    lastNoticeTarget = event.target.closest("button, input[type='submit']");
  },
  true,
);
document.addEventListener(
  "submit",
  (event) => {
    lastNoticeTarget = event.target;
  },
  true,
);
document.addEventListener(
  "change",
  (event) => {
    lastNoticeTarget = event.target;
  },
  true,
);

function showNotice(message, type = "success") {
  notice.textContent = message;
  notice.className = `notice ${type}`;
  notice.hidden = false;

  const dialog = lastNoticeTarget?.closest("dialog");
  const card = lastNoticeTarget?.closest(".card");
  let anchor = lastNoticeTarget?.matches("form")
    ? lastNoticeTarget
    : lastNoticeTarget?.closest(
        "form, .matrix-toolbar, .form-actions, .card-heading",
      );

  if (dialog || !anchor?.isConnected) {
    const section = dialog
      ? document.getElementById("catalog")
      : lastNoticeTarget?.closest("section");
    const targetCard = card || section?.querySelector(".card");
    anchor = targetCard?.querySelector(".card-heading") || targetCard;
  }

  if (anchor?.isConnected) {
    anchor.insertAdjacentElement("afterend", notice);
  }
}

function showError(error) {
  showNotice(error instanceof Error ? error.message : String(error), "error");
}

function escapeHtml(value) {
  return String(value ?? "").replace(
    /[&<>"']/g,
    (character) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      })[character],
  );
}

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set("Accept", "application/json, text/plain");
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  const token = sessionStorage.getItem(TOKEN_KEY);
  if (token && options.auth !== false) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });
  if (response.status === 204) {
    if (!response.ok) {
      throw new Error(`Yêu cầu thất bại (${response.status})`);
    }
    return null;
  }

  const contentType = response.headers.get("content-type") || "";
  const result = contentType.includes("application/json")
    ? await response.json()
    : await response.text();
  if (!response.ok) {
    const message =
      typeof result === "string"
        ? result
        : result.message ||
          result.error ||
          `Yêu cầu thất bại (${response.status})`;
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  return result;
}

function updateRoleManagementUi() {
  document
    .querySelectorAll("[data-requires-role-manage]")
    .forEach((element) => {
      element.hidden = !canManageRoles;
    });
  document
    .querySelectorAll("[data-requires-role-api]")
    .forEach((element) => {
      element.hidden = !canManageRoles || !roleManagementApiAvailable;
    });
}

function updateSession() {
  const username = sessionStorage.getItem("n1.username");
  const hasToken = Boolean(sessionStorage.getItem(TOKEN_KEY));
  document.getElementById("sessionStatus").textContent = hasToken
    ? `Đang đăng nhập: ${username || "tài khoản N1"}`
    : "Chưa đăng nhập";
  document.getElementById("logoutButton").disabled = !hasToken;
  updateRoleManagementUi();
}

async function refreshRoleManagementAccess() {
  canManageRoles = false;
  roleManagementApiAvailable = false;
  updateRoleManagementUi();
  if (!sessionStorage.getItem(TOKEN_KEY) || !currentUserId) {
    return;
  }

  try {
    const permissions = await request(
      `/users/${encodeURIComponent(currentUserId)}/permissions`,
    );
    canManageRoles = permissions.some(
      (permission) => permission.code === "ROLE_MANAGE",
    );
  } catch (error) {
    showError(error);
  }
  updateRoleManagementUi();
}

function selectedRole() {
  return roles.find((role) => role.id === selectedRoleId) || null;
}

function moduleDisplayName(moduleName) {
  const module = moduleCatalog.find((item) => item.code === moduleName);
  if (module) {
    return module.name;
  }

  const labels = {
    ACADEMIC: "Đào tạo",
    ATTENDANCE: "Điểm danh",
    CLASSES: "Lớp hành chính",
    COURSES: "Học phần",
    CURRICULUM: "Chương trình đào tạo",
    EXAMS: "Lịch thi",
    FACILITIES: "Cơ sở vật chất",
    GRADES: "Điểm số",
    REGISTRATION: "Đăng ký học phần",
    REPORTS: "Báo cáo",
    SCHEDULE: "Thời khóa biểu",
    STUDENTS: "Sinh viên",
    TRANSCRIPTS: "Bảng điểm",
    TUITION: "Học phí",
    USERS: "Người dùng",
    SETTINGS: "Cài đặt",
  };
  return labels[moduleName] || moduleName || "Khác";
}

async function loadModules() {
  try {
    moduleCatalog = await request("/permission-modules");
    renderModulesCatalog();
    updatePermissionModuleOptions();
  } catch (error) {
    modulesBody.innerHTML =
      '<tr><td colspan="4" class="empty">Không thể tải danh mục phân hệ.</td></tr>';
    throw error;
  }
}

function renderModulesCatalog() {
  if (!moduleCatalog.length) {
    modulesBody.innerHTML =
      '<tr><td colspan="4" class="empty">Chưa có phân hệ nào. Hãy thêm phân hệ mới.</td></tr>';
    return;
  }

  modulesBody.innerHTML = moduleCatalog
    .map(
      (module) => `
        <tr>
          <td><strong>${escapeHtml(module.name)}</strong></td>
          <td><code>${escapeHtml(module.code)}</code></td>
          <td>${escapeHtml(module.description || "—")}</td>
          <td>
            <div class="module-actions">
              <button class="button secondary edit-module" type="button" data-module-id="${escapeHtml(module.id)}">Sửa</button>
              <button class="button deactivate-permission deactivate-module" type="button" data-module-id="${escapeHtml(module.id)}">Ẩn</button>
            </div>
          </td>
        </tr>`,
    )
    .join("");
}

function updatePermissionModuleOptions() {
  const moduleSelect = document.getElementById("permissionModuleInput");
  const selectedCode = moduleSelect.value;
  moduleSelect.innerHTML = `
    <option value="">Chọn phân hệ</option>
    ${moduleCatalog
      .map(
        (module) =>
          `<option value="${escapeHtml(module.code)}">${escapeHtml(module.name)} (${escapeHtml(module.code)})</option>`,
      )
      .join("")}`;
  if (moduleCatalog.some((module) => module.code === selectedCode)) {
    moduleSelect.value = selectedCode;
  }
  updateModuleCodePreview();
}

function renderListPagination(
  container,
  page,
  total,
  onPageChange,
  itemLabel = "quyền",
  pageSize = PERMISSIONS_PER_PAGE,
) {
  const pageCount = Math.max(1, Math.ceil(total / pageSize));
  const currentPage = Math.min(page, pageCount);

  if (total <= pageSize) {
    container.innerHTML = "";
    return currentPage;
  }

  container.innerHTML = `
    <button class="button secondary list-page-button" type="button" data-page="${currentPage - 1}" ${currentPage === 1 ? "disabled" : ""}>Trước</button>
    <span>Trang ${currentPage} / ${pageCount} · ${total} ${itemLabel}</span>
    <button class="button secondary list-page-button" type="button" data-page="${currentPage + 1}" ${currentPage === pageCount ? "disabled" : ""}>Sau</button>`;

  container.querySelectorAll(".list-page-button").forEach((button) => {
    button.addEventListener("click", () => {
      const scrollY = window.scrollY;
      onPageChange(Number(button.dataset.page));
      requestAnimationFrame(() => window.scrollTo(0, scrollY));
    });
  });

  return currentPage;
}

function groupPermissionsByModule(permissionList) {
  const sortedPermissions = [...permissionList].sort((left, right) => {
    const moduleNameComparison = moduleDisplayName(left.module).localeCompare(
      moduleDisplayName(right.module),
      "vi",
    );
    if (moduleNameComparison !== 0) {
      return moduleNameComparison;
    }

    const moduleCodeComparison = (left.module || "").localeCompare(
      right.module || "",
    );
    return moduleCodeComparison || (left.name || left.code).localeCompare(
      right.name || right.code,
      "vi",
    );
  });
  const groups = new Map();

  sortedPermissions.forEach((permission) => {
    const moduleCode = permission.module || "";
    if (!groups.has(moduleCode)) {
      groups.set(moduleCode, {
        code: moduleCode,
        name: moduleDisplayName(moduleCode),
        permissions: [],
      });
    }
    groups.get(moduleCode).permissions.push(permission);
  });

  return [...groups.values()];
}

function paginatePermissionGroups(groups, pageSize = PERMISSIONS_PER_PAGE) {
  const pages = [];
  let page = [];
  let pageItemCount = 0;

  groups.forEach((group) => {
    const groupSize = group.permissions.length;
    if (page.length && pageItemCount + groupSize > pageSize) {
      pages.push(page);
      page = [];
      pageItemCount = 0;
    }
    page.push(group);
    pageItemCount += groupSize;
  });

  if (page.length) {
    pages.push(page);
  }
  return pages;
}

function renderGroupedPagination(
  container,
  page,
  pages,
  total,
  onPageChange,
  itemLabel = "quyền",
) {
  const pageCount = Math.max(1, pages.length);
  const currentPage = Math.min(page, pageCount);

  if (pageCount <= 1) {
    container.innerHTML = "";
    return currentPage;
  }

  container.innerHTML = `
    <button class="button secondary list-page-button" type="button" data-page="${currentPage - 1}" ${currentPage === 1 ? "disabled" : ""}>Trước</button>
    <span>Trang ${currentPage} / ${pageCount} · ${total} ${itemLabel}</span>
    <button class="button secondary list-page-button" type="button" data-page="${currentPage + 1}" ${currentPage === pageCount ? "disabled" : ""}>Sau</button>`;

  container.querySelectorAll(".list-page-button").forEach((button) => {
    button.addEventListener("click", () => {
      const scrollY = window.scrollY;
      onPageChange(Number(button.dataset.page));
      requestAnimationFrame(() => window.scrollTo(0, scrollY));
    });
  });

  return currentPage;
}

function filterPermissions(keyword) {
  const query = keyword.trim().toLocaleLowerCase();
  return permissionsCatalog.filter((permission) =>
    [
      permission.name,
      permission.code,
      moduleDisplayName(permission.module),
      permission.module,
      permission.description,
    ]
      .join(" ")
      .toLocaleLowerCase()
      .includes(query),
  );
}

function renderRoles() {
  const keyword = document
    .getElementById("roleSearchInput")
    .value.trim()
    .toLowerCase();
  const status = document.getElementById("roleStatusFilter").value;
  const displayedRoles = (canManageRoles ? managedRoles : roles).filter(
    (role) => {
      const matchesSearch = `${role.name} ${role.code}`
        .toLowerCase()
        .includes(keyword);
      const isActive = role.isActive !== false;
      return (
        matchesSearch &&
        (status === "all" ||
          (status === "active" && isActive) ||
          (status === "inactive" && !isActive))
      );
    },
  );

  if (!displayedRoles.length) {
    rolesBody.innerHTML =
      '<tr><td colspan="6" class="empty">Không tìm thấy vai trò phù hợp</td></tr>';
    return;
  }
  rolesBody.innerHTML = displayedRoles
    .map(
      (role, index) => `
        <tr>
          <td>${index + 1}</td>
          <td><strong>${escapeHtml(role.name)}</strong>${role.isSystem ? '<small class="muted role-system-label"> · Hệ thống</small>' : ""}</td>
          <td><code>${escapeHtml(role.code)}</code></td>
          <td>${escapeHtml(role.description || "—")}</td>
          <td><span class="status-badge ${role.isActive === false ? "inactive" : "active"}">${role.isActive === false ? "Ngừng hoạt động" : "Hoạt động"}</span></td>
          <td>
            <div class="row-actions">
              <button class="button secondary edit-role" type="button" data-role-id="${escapeHtml(role.id)}" ${canManageRoles && roleManagementApiAvailable ? "" : "hidden"} aria-label="Sửa vai trò">✎</button>
              <button class="button delete-role" type="button" data-role-id="${escapeHtml(role.id)}" ${canManageRoles && roleManagementApiAvailable && !role.isSystem ? "" : "hidden"} aria-label="Xóa vai trò">▣</button>
            </div>
          </td>
        </tr>`,
    )
    .join("");
}

function renderRoleSelect() {
  const previousId = selectedRoleId;
  roleCodeSelect.innerHTML = `<option value="">Chọn vai trò</option>${roles
    .map(
      (role) =>
        `<option value="${escapeHtml(role.id)}">${escapeHtml(role.name)} (${escapeHtml(role.code)})</option>`,
    )
    .join("")}`;
  if (roles.some((role) => role.id === previousId)) {
    roleCodeSelect.value = previousId;
  } else {
    selectedRoleId = "";
  }
}

async function loadRoles() {
  if (canManageRoles) {
    try {
      managedRoles = await request("/roles/manage");
      roleManagementApiAvailable = true;
      roles = managedRoles.filter((role) => role.isActive !== false);
    } catch (error) {
      if (error.status !== 404) {
        throw error;
      }
      roleManagementApiAvailable = false;
      roles = await request("/roles", { auth: false });
      managedRoles = roles;
      showNotice(
        "Đã tải vai trò. Backend hiện tại chưa có API quản lý vai trò; hãy khởi động lại ứng dụng để bật thêm/sửa/xóa vai trò và tìm kiếm người dùng.",
        "error",
      );
    }
  } else {
    roleManagementApiAvailable = false;
    roles = await request("/roles", { auth: false });
    managedRoles = roles;
  }
  updateRoleManagementUi();
  assignRoleSelect.innerHTML = `<option value="">Chọn vai trò</option>${roles
    .map(
      (role) =>
        `<option value="${escapeHtml(role.id)}">${escapeHtml(role.name)} (${escapeHtml(role.code)})</option>`,
    )
    .join("")}`;
  renderRoleSelect();
  renderRoles();
  if (selectedRoleId) {
    await loadRolePermissions();
  } else {
    rolePermissionDraftIds = new Set();
    rolePermissionPage = 1;
    rolePermissionsBody.innerHTML =
      '<tr><td colspan="4" class="empty">Chọn một vai trò để xem quyền</td></tr>';
    rolePermissionPagination.innerHTML = "";
  }
  renderUsers();
  renderUserPermissionChoices();
}

async function loadPermissions() {
  permissionsCatalog = await request("/permissions", { auth: false });
  renderPermissionsCatalog();
  if (selectedRoleId) {
    await loadRolePermissions();
  }
}

function renderPermissionsCatalog() {
  const filteredPermissions = filterPermissions(
    document.getElementById("permissionsCatalogSearch").value,
  );
  if (!filteredPermissions.length) {
    permissionsBody.innerHTML =
      `<tr><td colspan="6" class="empty">${permissionsCatalog.length ? "Không tìm thấy quyền phù hợp" : "Không có quyền đang hoạt động"}</td></tr>`;
    permissionsCatalogPagination.innerHTML = "";
    return;
  }
  const groupedPermissions = groupPermissionsByModule(filteredPermissions);
  const pages = paginatePermissionGroups(groupedPermissions);
  permissionsCatalogPage = renderGroupedPagination(
    permissionsCatalogPagination,
    permissionsCatalogPage,
    pages,
    filteredPermissions.length,
    (page) => {
      permissionsCatalogPage = page;
      renderPermissionsCatalog();
    },
    "quyền",
  );
  const pagePermissions = pages[permissionsCatalogPage - 1].flatMap(
    (group) => group.permissions,
  );
  permissionsBody.innerHTML = pagePermissions
    .map(
      (permission) => `
        <tr>
          <td>${escapeHtml(permission.name)}</td>
          <td><code>${escapeHtml(permission.code)}</code></td>
          <td>${escapeHtml(moduleDisplayName(permission.module))}</td>
          <td><code>${escapeHtml(permission.module)}</code></td>
          <td>${escapeHtml(permission.description || "—")}</td>
          <td>
            ${canManageRoles && permission.code === "ROLE_MANAGE"
              ? '<span class="protected-permission">Quyền hệ thống</span>'
              : canManageRoles
                ? `<button class="button deactivate-permission" type="button" data-permission-id="${escapeHtml(permission.id)}">Ngừng</button>`
                : ""}
          </td>
        </tr>`,
    )
    .join("");
}

function renderPermissionMatrix() {
  if (!selectedRoleId) {
    rolePermissionsBody.innerHTML =
      '<tr><td colspan="4" class="empty">Chọn một vai trò để xem quyền</td></tr>';
    rolePermissionPagination.innerHTML = "";
    document.getElementById("permissionCount").textContent =
      "Chọn vai trò để xem quyền";
    return;
  }

  const filteredPermissions = filterPermissions(
    document.getElementById("rolePermissionSearch").value,
  );
  const groupedPermissions = groupPermissionsByModule(filteredPermissions);
  const pages = paginatePermissionGroups(groupedPermissions);
  rolePermissionPage = renderGroupedPagination(
    rolePermissionPagination,
    rolePermissionPage,
    pages,
    filteredPermissions.length,
    (page) => {
      rolePermissionPage = page;
      renderPermissionMatrix();
    },
    "quyền",
  );
  const pageGroups = pages[rolePermissionPage - 1];

  const rows = [];
  pageGroups.forEach((group) => {
    group.permissions.forEach((permission, index) => {
      rows.push(`
        <tr>
          ${index === 0 ? `<td class="permission-module" rowspan="${group.permissions.length}">${escapeHtml(group.name)}</td>` : ""}
          <td class="permission-name">${escapeHtml(permission.name || permission.code)}</td>
          <td><code>${escapeHtml(permission.code)}</code></td>
          <td class="permission-toggle-cell"><input class="permission-toggle" type="checkbox" data-permission-id="${escapeHtml(permission.id)}" aria-label="Cấp quyền ${escapeHtml(permission.name || permission.code)}" ${rolePermissionDraftIds.has(permission.id) ? "checked" : ""} ${canManageRoles ? "" : "disabled"}></td>
        </tr>`);
    });
  });

  rolePermissionsBody.innerHTML = rows.length
    ? rows.join("")
    : `<tr><td colspan="4" class="empty">${permissionsCatalog.length ? "Không tìm thấy quyền phù hợp" : "Danh mục chưa có quyền nào"}</td></tr>`;
  document.getElementById("permissionCount").textContent =
    `Đã cấp ${rolePermissionDraftIds.size}/${permissionsCatalog.length} quyền · Hiển thị ${filteredPermissions.length} kết quả`;
}

async function loadRolePermissions() {
  const role = selectedRole();
  if (!role) {
    selectedRoleId = "";
    rolePermissionDraftIds = new Set();
    renderPermissionMatrix();
    return;
  }
  assignedPermissions = await request(
    `/roles/${encodeURIComponent(role.code)}/permissions`,
    { auth: false },
  );
  rolePermissionDraftIds = new Set(
    assignedPermissions.map((permission) => permission.id),
  );
  renderPermissionMatrix();
}

async function saveRolePermissions() {
  const role = selectedRole();
  if (!role) {
    throw new Error("Vui lòng chọn vai trò cần phân quyền.");
  }

  const desiredIds = rolePermissionDraftIds;
  const currentIds = new Set(
    assignedPermissions.map((permission) => permission.id),
  );
  const requests = [];
  desiredIds.forEach((permissionId) => {
    if (!currentIds.has(permissionId)) {
      requests.push(
        request(`/roles/${encodeURIComponent(role.id)}/permissions`, {
          method: "POST",
          body: { permissionId },
        }),
      );
    }
  });
  currentIds.forEach((permissionId) => {
    if (!desiredIds.has(permissionId)) {
      requests.push(
        request(
          `/roles/${encodeURIComponent(role.id)}/permissions/${encodeURIComponent(permissionId)}`,
          { method: "DELETE" },
        ),
      );
    }
  });
  await Promise.all(requests);
  await loadRolePermissions();
}

function openRoleDialog(role = null) {
  const form = document.getElementById("roleForm");
  form.reset();
  document.getElementById("roleIdInput").value = role?.id || "";
  document.getElementById("roleCodeInput").value = role?.code || "";
  document.getElementById("roleCodeInput").disabled =
    Boolean(role?.isSystem);
  document.getElementById("roleNameInput").value = role?.name || "";
  document.getElementById("roleDescriptionInput").value =
    role?.description || "";
  document.getElementById("roleActiveInput").checked =
    role ? role.isActive !== false : true;
  document.getElementById("roleDialogTitle").textContent = role
    ? "Cập nhật vai trò"
    : "Thêm vai trò";
  document.getElementById("roleDialog").showModal();
}

function openPermissionDialog() {
  document.getElementById("permissionForm").reset();
  document.getElementById("permissionFormError").hidden = true;
  updatePermissionModuleOptions();
  document.getElementById("permissionDialog").showModal();
  document.getElementById("permissionCodeInput").focus();
}

function updateModuleCodePreview() {
  document.getElementById("moduleCodePreview").textContent =
    document.getElementById("permissionModuleInput").value ||
    "Chưa chọn phân hệ";
}

function openModuleDialog(module = null) {
  const form = document.getElementById("moduleForm");
  form.reset();
  document.getElementById("moduleIdInput").value = module?.id || "";
  const codeInput = document.getElementById("moduleCodeInput");
  codeInput.value = module?.code || "";
  codeInput.disabled = Boolean(module);
  document.getElementById("moduleNameInput").value = module?.name || "";
  document.getElementById("moduleDescriptionInput").value =
    module?.description || "";
  document.getElementById("moduleFormError").hidden = true;
  document.getElementById("moduleDialogTitle").textContent = module
    ? "Sửa phân hệ"
    : "Thêm phân hệ";
  document.getElementById("moduleDialog").showModal();
}

function renderUsers() {
  if (!users.length) {
    usersBody.innerHTML =
      `<tr><td colspan="5" class="empty">${usersLoaded ? "Không tìm thấy người dùng phù hợp" : "Đang tải danh sách người dùng..."}</td></tr>`;
    usersPagination.innerHTML = "";
    return;
  }
  usersPage = renderListPagination(
    usersPagination,
    usersPage,
    users.length,
    (page) => {
      usersPage = page;
      renderUsers();
    },
    "người dùng",
    USERS_PER_PAGE,
  );
  const start = (usersPage - 1) * USERS_PER_PAGE;
  usersBody.innerHTML = users
    .slice(start, start + USERS_PER_PAGE)
    .map(
      (user, index) => `
        <tr>
          <td>${start + index + 1}</td>
          <td><strong>${escapeHtml(user.fullName)}</strong></td>
          <td>${escapeHtml(user.email || "—")}</td>
          <td><div class="role-badges">${user.roles.length ? user.roles.map((role) => `<span class="role-badge">${escapeHtml(role.name)}</span>`).join("") : "—"}</div></td>
          <td><button class="button select-user" type="button" data-user-id="${escapeHtml(user.id)}">Gán quyền</button></td>
        </tr>`,
    )
    .join("");
}

function renderUserPermissionChoices() {
  const container = document.getElementById("userPermissionChoices");
  if (!selectedUser) {
    container.innerHTML = '<p class="muted">Chọn một người dùng trong danh sách.</p>';
    userPermissionPagination.innerHTML = "";
    return;
  }
  const filteredPermissions = filterPermissions(
    document.getElementById("userPermissionSearch").value,
  );
  const groupedPermissions = groupPermissionsByModule(filteredPermissions);
  const pages = paginatePermissionGroups(groupedPermissions);
  userPermissionPage = renderGroupedPagination(
    userPermissionPagination,
    userPermissionPage,
    pages,
    filteredPermissions.length,
    (page) => {
      userPermissionPage = page;
      renderUserPermissionChoices();
    },
    "quyền",
  );
  const pageGroups = pages[userPermissionPage - 1];
  container.innerHTML = pageGroups.length
    ? pageGroups
        .map(
          (group) => `
            <section class="permission-choice-group">
              <h4>${escapeHtml(group.name)}</h4>
              ${group.permissions
                .map(
                  (permission) => `
                    <label class="role-choice">
                      <input type="checkbox" class="user-permission-toggle" value="${escapeHtml(permission.id)}" ${userPermissionDraftIds.has(permission.id) ? "checked" : ""}>
                      <span><strong>${escapeHtml(permission.name)}</strong><small>${escapeHtml(moduleDisplayName(permission.module))} · ${escapeHtml(permission.code)}</small></span>
                    </label>`,
                )
                .join("")}
            </section>`,
        )
        .join("")
    : `<p class="muted">${permissionsCatalog.length ? "Không tìm thấy quyền phù hợp." : "Không có quyền đang hoạt động."}</p>`;
}

async function selectUser(user) {
  selectedUser = user;
  userPermissionPage = 1;
  document.getElementById("userDetail").hidden = !canManageRoles;
  document.getElementById("selectedUserName").textContent =
    `${user.fullName}${user.email ? ` · ${user.email}` : ""} · ID ${user.id}`;
  document.getElementById("userPermissionsBody").innerHTML =
    '<tr><td colspan="3" class="empty">Chọn “Xem quyền hiệu lực” để tải</td></tr>';
  selectedUserPermissions = await request(
    `/users/${encodeURIComponent(user.id)}/direct-permissions`,
  );
  userPermissionDraftIds = new Set(
    selectedUserPermissions.map((permission) => permission.id),
  );
  renderUserPermissionChoices();
  document.getElementById("userDetail").scrollIntoView({
    behavior: "smooth",
    block: "start",
  });
}

async function searchUsers(keyword) {
  const params = new URLSearchParams({ keyword });
  users = await request(`/n1/users?${params.toString()}`);
  usersLoaded = true;
  usersPage = 1;
  renderUsers();
  selectedUser = null;
  selectedUserPermissions = [];
  userPermissionDraftIds = new Set();
  userPermissionPage = 1;
  renderUserPermissionChoices();
  document.getElementById("userDetail").hidden = true;
  if (!users.length) {
    usersBody.innerHTML =
      '<tr><td colspan="5" class="empty">Không tìm thấy người dùng phù hợp</td></tr>';
  }
}

function selectedLookupUserId() {
  if (!selectedLookupUser) {
    throw new Error("Vui lòng tìm và chọn người dùng trước.");
  }
  return encodeURIComponent(selectedLookupUser.id);
}

async function searchUsersForRoleLookup(keyword) {
  const params = new URLSearchParams({ keyword });
  lookupUsers = await request(`/n1/users?${params.toString()}`);
  selectedLookupUser = null;
  userLookupSelect.innerHTML = `
    <option value="">Chọn người dùng trong kết quả (${lookupUsers.length})</option>
    ${lookupUsers
      .map(
        (user) =>
          `<option value="${escapeHtml(user.id)}">${escapeHtml(user.fullName)}${user.email ? ` · ${escapeHtml(user.email)}` : ""}${user.roles.length ? ` · ${escapeHtml(user.roles.map((role) => role.name).join(", "))}` : ""}</option>`,
      )
      .join("")}`;
  document.getElementById("userLookupSelectLabel").hidden =
    lookupUsers.length === 0;
  userRolesBody.innerHTML = lookupUsers.length
    ? '<tr><td colspan="3" class="empty">Chọn người dùng để xem vai trò</td></tr>'
    : '<tr><td colspan="3" class="empty">Không tìm thấy người dùng phù hợp</td></tr>';
  userPermissionsByIdBody.innerHTML =
    '<tr><td colspan="4" class="empty">Chọn người dùng để xem quyền hiệu lực</td></tr>';
  return lookupUsers;
}

async function loadUserRolesById() {
  const userId = selectedLookupUserId();
  const userRoles = await request(`/users/${userId}/roles`);
  userRolesBody.innerHTML = userRoles.length
    ? userRoles
        .map(
          (role) => `
            <tr>
              <td>${escapeHtml(role.name)}</td>
              <td><code>${escapeHtml(role.code)}</code></td>
              <td><button class="button secondary remove-user-role" type="button" data-role-id="${escapeHtml(role.id)}">Thu hồi</button></td>
            </tr>`,
        )
        .join("")
    : '<tr><td colspan="3" class="empty">Người dùng chưa có vai trò</td></tr>';
}

async function loadUserPermissionsById() {
  const userId = selectedLookupUserId();
  const permissions = await request(`/users/${userId}/permissions`);
  userPermissionsByIdBody.innerHTML = permissions.length
    ? permissions
        .map(
          (permission) => `
            <tr>
              <td>${escapeHtml(permission.name)}</td>
              <td><code>${escapeHtml(permission.code)}</code></td>
              <td>${escapeHtml(moduleDisplayName(permission.module))}</td>
              <td>${escapeHtml(permission.description || "—")}</td>
            </tr>`,
        )
        .join("")
    : '<tr><td colspan="4" class="empty">Người dùng chưa có quyền hiệu lực</td></tr>';
}

document
  .getElementById("createUserForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form).entries());
    try {
      await request("/n1/users", {
        method: "POST",
        body: {
          username: values.username,
          passwordHash: values.password,
          fullName: values.fullName,
          email: values.email.trim() || null,
          phone: values.phone.trim() || null,
        },
      });
      window.location.assign("/index.html?page=list");
    } catch (error) {
      showError(error);
    }
  });

document.getElementById("logoutButton").addEventListener("click", async () => {
  try {
    if (sessionStorage.getItem(TOKEN_KEY)) {
      await request("/auth/logout", { method: "POST" });
    }
  } catch (error) {
    showError(error);
    return;
  }
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_ID_KEY);
  sessionStorage.removeItem("n1.username");
  currentUserId = "";
  canManageRoles = false;
  roleManagementApiAvailable = false;
  managedRoles = roles;
  updateSession();
  renderRoles();
  showNotice("Đăng xuất thành công.");
});

document
  .getElementById("loadRolesButton")
  .addEventListener("click", async () => {
    try {
      await loadRoles();
      showNotice("Đã tải danh sách vai trò.");
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("loadPermissionsButton")
  .addEventListener("click", async () => {
    try {
      await loadPermissions();
      showNotice("Đã tải danh mục quyền.");
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("loadModulesButton")
  .addEventListener("click", async () => {
    try {
      await loadModules();
      showNotice("Đã tải danh mục phân hệ.");
    } catch (error) {
      showError(error);
    }
  });

modulesBody.addEventListener("click", async (event) => {
  const editButton = event.target.closest(".edit-module");
  if (editButton) {
    const module = moduleCatalog.find(
      (item) => item.id === editButton.dataset.moduleId,
    );
    if (module) {
      openModuleDialog(module);
    }
    return;
  }

  const deactivateButton = event.target.closest(".deactivate-module");
  if (!deactivateButton) {
    return;
  }

  const module = moduleCatalog.find(
    (item) => item.id === deactivateButton.dataset.moduleId,
  );
  if (!module) {
    return;
  }

  if (
    !window.confirm(
      `Ẩn phân hệ "${module.name}" (${module.code})?\n\nChỉ có thể ẩn khi phân hệ không còn quyền đang hoạt động.`,
    )
  ) {
    return;
  }

  try {
    await request(
      `/permission-modules/manage/${encodeURIComponent(module.id)}`,
      { method: "DELETE" },
    );
    await loadModules();
    showNotice(`Đã ẩn phân hệ ${module.code}.`);
  } catch (error) {
    showError(error);
  }
});

permissionsBody.addEventListener("click", async (event) => {
  const button = event.target.closest(".deactivate-permission");
  if (!button) {
    return;
  }

  const permission = permissionsCatalog.find(
    (item) => item.id === button.dataset.permissionId,
  );
  if (!permission) {
    return;
  }

  try {
    const usage = await request(
      `/permissions/manage/${encodeURIComponent(permission.id)}/usage`,
    );
    const assignmentSummary =
      `Hiện được gán cho ${usage.roleAssignments} vai trò và ${usage.userAssignments} người dùng.`;
    const confirmed = window.confirm(
      `Ngừng kích hoạt quyền "${permission.name}" (${permission.code})?\n\n${assignmentSummary}\nQuyền sẽ mất hiệu lực và bị ẩn khỏi danh mục.`,
    );
    if (!confirmed) {
      return;
    }

    await request(`/permissions/manage/${encodeURIComponent(permission.id)}`, {
      method: "DELETE",
    });
    await loadPermissions();
    showNotice(`Đã ngừng kích hoạt quyền ${permission.code}.`);
  } catch (error) {
    showError(error);
  }
});

document.getElementById("roleSearchInput").addEventListener("input", renderRoles);
document
  .getElementById("roleStatusFilter")
  .addEventListener("change", renderRoles);
document.getElementById("addRoleButton").addEventListener("click", () => {
  openRoleDialog();
});
document
  .getElementById("addPermissionButton")
  .addEventListener("click", openPermissionDialog);
document
  .getElementById("addModuleButton")
  .addEventListener("click", () => openModuleDialog());

document
  .getElementById("closePermissionDialog")
  .addEventListener("click", () => {
    document.getElementById("permissionDialog").close();
  });
document
  .getElementById("cancelPermissionDialog")
  .addEventListener("click", () => {
    document.getElementById("permissionDialog").close();
  });
document
  .getElementById("closeModuleDialog")
  .addEventListener("click", () => {
    document.getElementById("moduleDialog").close();
  });
document
  .getElementById("cancelModuleDialog")
  .addEventListener("click", () => {
    document.getElementById("moduleDialog").close();
  });

document
  .getElementById("moduleForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const submitButton = form.querySelector('button[type="submit"]');
    const errorMessage = document.getElementById("moduleFormError");
    const moduleId = document.getElementById("moduleIdInput").value;
    const body = {
      code: document.getElementById("moduleCodeInput").value,
      name: document.getElementById("moduleNameInput").value,
      description: document.getElementById("moduleDescriptionInput").value,
    };

    errorMessage.hidden = true;
    submitButton.disabled = true;
    try {
      await request(
        moduleId
          ? `/permission-modules/manage/${encodeURIComponent(moduleId)}`
          : "/permission-modules/manage",
        {
          method: moduleId ? "PUT" : "POST",
          body,
        },
      );
      document.getElementById("moduleDialog").close();
      await loadModules();
      renderPermissionsCatalog();
      renderPermissionMatrix();
      renderUserPermissionChoices();
      showNotice(moduleId ? "Đã cập nhật phân hệ." : "Đã thêm phân hệ.");
    } catch (error) {
      errorMessage.textContent = error.message;
      errorMessage.hidden = false;
    } finally {
      submitButton.disabled = false;
    }
  });

document
  .getElementById("permissionForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const submitButton = form.querySelector('button[type="submit"]');
    const errorMessage = document.getElementById("permissionFormError");
    const formData = new FormData(form);

    errorMessage.hidden = true;
    submitButton.disabled = true;
    try {
      await request("/permissions/manage", {
        method: "POST",
        body: Object.fromEntries(formData.entries()),
      });
      document.getElementById("permissionDialog").close();
      form.reset();
      permissionsCatalogPage = 1;
      await loadPermissions();
      showNotice("Đã thêm chức năng vào danh mục quyền.");
    } catch (error) {
      errorMessage.textContent = error.message;
      errorMessage.hidden = false;
    } finally {
      submitButton.disabled = false;
    }
  });

rolesBody.addEventListener("click", async (event) => {
  const editButton = event.target.closest(".edit-role");
  const deleteButton = event.target.closest(".delete-role");
  if (editButton) {
    const role = managedRoles.find(
      (item) => item.id === editButton.dataset.roleId,
    );
    if (role) {
      openRoleDialog(role);
    }
  }
  if (deleteButton) {
    const role = managedRoles.find(
      (item) => item.id === deleteButton.dataset.roleId,
    );
    if (!role || !window.confirm(`Xóa vai trò "${role.name}"?`)) {
      return;
    }
    try {
      await request(`/roles/${encodeURIComponent(role.id)}`, {
        method: "DELETE",
      });
      if (selectedRoleId === role.id) {
        selectedRoleId = "";
      }
      await loadRoles();
      showNotice("Đã xóa vai trò.");
    } catch (error) {
      showError(error);
    }
  }
});

document.getElementById("roleForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const roleId = document.getElementById("roleIdInput").value;
  const codeInput = document.getElementById("roleCodeInput");
  const body = {
    code: codeInput.value.trim(),
    name: document.getElementById("roleNameInput").value.trim(),
    description:
      document.getElementById("roleDescriptionInput").value.trim() || null,
    isActive: document.getElementById("roleActiveInput").checked,
  };
  try {
    await request(
      roleId ? `/roles/${encodeURIComponent(roleId)}` : "/roles",
      { method: roleId ? "PUT" : "POST", body },
    );
    document.getElementById("roleDialog").close();
    selectedRoleId = "";
    await loadRoles();
    showNotice(roleId ? "Đã cập nhật vai trò." : "Đã tạo vai trò.");
  } catch (error) {
    showError(error);
  }
});

document.getElementById("closeRoleDialog").addEventListener("click", () => {
  document.getElementById("roleDialog").close();
});
document.getElementById("cancelRoleDialog").addEventListener("click", () => {
  document.getElementById("roleDialog").close();
});

roleCodeSelect.addEventListener("change", async () => {
  selectedRoleId = roleCodeSelect.value;
  rolePermissionPage = 1;
  try {
    await loadRolePermissions();
  } catch (error) {
    showError(error);
  }
});

document
  .getElementById("rolePermissionSearch")
  .addEventListener("input", () => {
    rolePermissionPage = 1;
    renderPermissionMatrix();
  });

document
  .getElementById("permissionsCatalogSearch")
  .addEventListener("input", () => {
    permissionsCatalogPage = 1;
    renderPermissionsCatalog();
  });

document
  .getElementById("permissionModuleInput")
  .addEventListener("change", updateModuleCodePreview);

document
  .getElementById("userPermissionSearch")
  .addEventListener("input", () => {
    userPermissionPage = 1;
    renderUserPermissionChoices();
  });

rolePermissionsBody.addEventListener("change", (event) => {
  const checkbox = event.target.closest(".permission-toggle");
  if (!checkbox) {
    return;
  }
  if (checkbox.checked) {
    rolePermissionDraftIds.add(checkbox.dataset.permissionId);
  } else {
    rolePermissionDraftIds.delete(checkbox.dataset.permissionId);
  }
  document.getElementById("permissionCount").textContent =
    `Đã cấp ${rolePermissionDraftIds.size}/${permissionsCatalog.length} quyền · Hiển thị ${filterPermissions(document.getElementById("rolePermissionSearch").value).length} kết quả`;
});

document
  .getElementById("saveRolePermissionsButton")
  .addEventListener("click", async () => {
    try {
      await saveRolePermissions();
      showNotice("Đã lưu quyền cho vai trò.");
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("userSearchForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const keyword = document.getElementById("userSearchInput").value.trim();
    try {
      await searchUsers(keyword);
      showNotice(
        keyword
          ? `Tìm thấy ${users.length} người dùng phù hợp.`
          : `Đang hiển thị ${users.length} người dùng.`,
      );
    } catch (error) {
      showError(
        error.status === 404 || error.status === 405
          ? new Error(
              "Backend hiện tại chưa hỗ trợ API tìm kiếm người dùng. Hãy khởi động lại ứng dụng để nạp phiên bản mới.",
            )
          : error,
      );
    }
  });

usersBody.addEventListener("click", async (event) => {
  const button = event.target.closest(".select-user");
  if (!button) {
    return;
  }
  const user = users.find((item) => item.id === button.dataset.userId);
  if (!user) {
    return;
  }
  try {
    await selectUser(user);
  } catch (error) {
    showError(error);
  }
});

document
  .getElementById("saveUserPermissionsButton")
  .addEventListener("click", async () => {
    if (!selectedUser) {
      showError(new Error("Vui lòng chọn người dùng."));
      return;
    }
    const desiredIds = userPermissionDraftIds;
    const currentIds = new Set(
      selectedUserPermissions.map((permission) => permission.id),
    );
    const userId = encodeURIComponent(selectedUser.id);
    const operations = [];
    desiredIds.forEach((permissionId) => {
      if (!currentIds.has(permissionId)) {
        operations.push(
          request(`/users/${userId}/permissions`, {
            method: "POST",
            body: { permissionId },
          }),
        );
      }
    });
    currentIds.forEach((permissionId) => {
      if (!desiredIds.has(permissionId)) {
        operations.push(
          request(
            `/users/${userId}/permissions/${encodeURIComponent(permissionId)}`,
            { method: "DELETE" },
          ),
        );
      }
    });
    try {
      await Promise.all(operations);
      const currentUser = selectedUser;
      await searchUsers(document.getElementById("userSearchInput").value.trim());
      const refreshedUser = users.find((user) => user.id === currentUser.id);
      if (refreshedUser) {
        await selectUser(refreshedUser);
      }
      showNotice("Đã cập nhật quyền trực tiếp của người dùng.");
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("userPermissionChoices")
  .addEventListener("change", (event) => {
    const checkbox = event.target.closest(".user-permission-toggle");
    if (!checkbox) {
      return;
    }
    if (checkbox.checked) {
      userPermissionDraftIds.add(checkbox.value);
    } else {
      userPermissionDraftIds.delete(checkbox.value);
    }
  });

document
  .getElementById("loadUserPermissionsButton")
  .addEventListener("click", async () => {
    if (!selectedUser) {
      showError(new Error("Vui lòng chọn người dùng trước."));
      return;
    }
    try {
      const permissions = await request(
        `/users/${encodeURIComponent(selectedUser.id)}/permissions`,
      );
      userPermissionsBody.innerHTML = permissions.length
        ? permissions
            .map(
              (permission) => `
                <tr>
                  <td>${escapeHtml(permission.name)}</td>
                  <td><code>${escapeHtml(permission.code)}</code></td>
                  <td>${escapeHtml(moduleDisplayName(permission.module))}</td>
                </tr>`,
            )
            .join("")
        : '<tr><td colspan="3" class="empty">Người dùng chưa có quyền hiệu lực</td></tr>';
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("userLookupForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      const keyword = document.getElementById("userLookupSearch").value.trim();
      const matches = await searchUsersForRoleLookup(keyword);
      showNotice(
        matches.length
          ? `Tìm thấy ${matches.length} người dùng. Hãy chọn đúng tài khoản trong danh sách.`
          : "Không tìm thấy người dùng phù hợp.",
        matches.length ? "success" : "error",
      );
    } catch (error) {
      showError(error);
    }
  });

userLookupSelect.addEventListener("change", async () => {
  selectedLookupUser =
    lookupUsers.find((user) => user.id === userLookupSelect.value) || null;
  if (!selectedLookupUser) {
    return;
  }
  try {
    await Promise.all([loadUserRolesById(), loadUserPermissionsById()]);
    showNotice(
      `Đã tải vai trò và quyền hiệu lực của ${selectedLookupUser.fullName}.`,
    );
  } catch (error) {
    showError(error);
  }
});

document
  .getElementById("assignRoleForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      const userId = selectedLookupUserId();
      const roleId = assignRoleSelect.value;
      if (!roleId) {
        throw new Error("Vui lòng chọn vai trò cần cấp.");
      }
      await request(`/users/${userId}/roles`, {
        method: "POST",
        body: { roleId },
      });
      await Promise.all([loadUserRolesById(), loadUserPermissionsById()]);
      showNotice("Đã cấp vai trò cho người dùng.");
    } catch (error) {
      showError(error);
    }
  });

userRolesBody.addEventListener("click", async (event) => {
  const button = event.target.closest(".remove-user-role");
  if (!button) {
    return;
  }
  try {
    const userId = selectedLookupUserId();
    await request(
      `/users/${userId}/roles/${encodeURIComponent(button.dataset.roleId)}`,
      { method: "DELETE" },
    );
    await Promise.all([loadUserRolesById(), loadUserPermissionsById()]);
    showNotice("Đã thu hồi vai trò khỏi người dùng.");
  } catch (error) {
    showError(error);
  }
});

document
  .getElementById("checkGradeButton")
  .addEventListener("click", async () => {
    const output = document.getElementById("gradeResult");
    try {
      output.textContent = await request("/rbac-test/grade");
    } catch (error) {
      output.textContent = error.message;
    }
  });

const sideNavigation = document.getElementById("sideNavigation");
const sidebarToggle = document.getElementById("sidebarToggle");
const sidebarOverlay = document.getElementById("sidebarOverlay");

function setSidebarOpen(isOpen) {
  sideNavigation.classList.toggle("is-open", isOpen);
  sidebarOverlay.classList.toggle("is-visible", isOpen);
  sidebarToggle.setAttribute("aria-expanded", String(isOpen));
  sidebarToggle.setAttribute(
    "aria-label",
    isOpen ? "Đóng điều hướng" : "Mở điều hướng",
  );
  document.body.classList.toggle("sidebar-open", isOpen);
}

sidebarToggle.addEventListener("click", () => {
  setSidebarOpen(!sideNavigation.classList.contains("is-open"));
});
document
  .getElementById("sidebarClose")
  .addEventListener("click", () => setSidebarOpen(false));
sidebarOverlay.addEventListener("click", () => setSidebarOpen(false));
sideNavigation.querySelectorAll('a[href^="#"]').forEach((link) => {
  link.addEventListener("click", () => {
    sideNavigation.querySelectorAll(".sidebar-link").forEach((item) => {
      item.classList.toggle("active", item === link);
    });
    setSidebarOpen(false);
  });
});
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    setSidebarOpen(false);
  }
});

async function initialize() {
  updateSession();
  try {
    await refreshRoleManagementAccess();
    try {
      await loadModules();
    } catch (error) {
      showError(error);
    }
    await Promise.all([loadRoles(), loadPermissions()]);
    if (canManageRoles) {
      await searchUsers("");
    }
  } catch (error) {
    showError(error);
  }
}

initialize();
