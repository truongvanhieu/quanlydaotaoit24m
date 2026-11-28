const TOKEN_KEY = "n1.jwt";
const USER_ID_KEY = "n1.userId";

const notice = document.getElementById("authNotice");

function showNotice(message, type = "success") {
  notice.textContent = message;
  notice.className = `notice ${type}`;
  notice.hidden = false;
}

function showError(error) {
  showNotice(error instanceof Error ? error.message : String(error), "error");
}

function formValues(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function optionalValue(value) {
  return value.trim() || null;
}

async function request(path, options = {}) {
  const headers = new Headers({
    Accept: "application/json, text/plain",
    ...Object.fromEntries(new Headers(options.headers || {}).entries()),
  });

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`/api${path}`, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

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

    throw new Error(message);
  }

  return result;
}

function showSignedInState(username, message) {
  const session = document.getElementById("authSession");
  document.getElementById("authSessionText").textContent =
    message || `Đang đăng nhập: ${username}`;
  session.hidden = false;
}

document
  .getElementById("loginForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    try {
      const result = await request("/auth/login", {
        method: "POST",
        body: formValues(form),
      });
      sessionStorage.setItem(TOKEN_KEY, result.token);
      sessionStorage.setItem(USER_ID_KEY, result.userId);
      sessionStorage.setItem("n1.username", result.username);

      let message = `Đăng nhập thành công: ${result.username}`;
      try {
        const permissions = await request(
          `/users/${encodeURIComponent(result.userId)}/permissions`,
          {
            headers: { Authorization: `Bearer ${result.token}` },
          },
        );
        if (
          permissions.some((permission) => permission.code === "ROLE_MANAGE")
        ) {
          message += " · Có quyền quản lý N1";
        } else {
          message += " · Tài khoản không có quyền ROLE_MANAGE";
        }
      } catch (error) {
        message += ` · Không thể kiểm tra quyền: ${error.message}`;
      }

      form.reset();
      showSignedInState(result.username, message);
      showNotice("Đăng nhập thành công. Bạn có thể tiếp tục sang trang N1.");
      const nextUrl = new URLSearchParams(window.location.search).get("next");
      if (nextUrl && nextUrl.startsWith("/")) {
        window.setTimeout(() => window.location.assign(nextUrl), 300);
      }
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("registerForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    try {
      const values = formValues(form);
      const result = await request("/auth/register", {
        method: "POST",
        body: {
          ...values,
          email: optionalValue(values.email),
          phone: optionalValue(values.phone),
        },
      });
      sessionStorage.setItem(USER_ID_KEY, result.id);
      form.reset();
      showNotice(`Đăng ký thành công. ID người dùng của bạn: ${result.id}`);
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("forgotForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      const result = await request("/auth/forgot-password", {
        method: "POST",
        body: formValues(event.currentTarget),
      });
      const tokenResult = document.getElementById("resetTokenResult");
      if (result.resetToken) {
        document.querySelector("#resetForm [name=token]").value =
          result.resetToken;
        tokenResult.textContent = `Mã khôi phục có hiệu lực trong 15 phút: ${result.resetToken}`;
        tokenResult.hidden = false;
      } else {
        tokenResult.hidden = true;
      }
      showNotice(result.message || "Đã nhận yêu cầu khôi phục mật khẩu.");
    } catch (error) {
      showError(error);
    }
  });

document
  .getElementById("resetForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    try {
      const result = await request("/auth/reset-password", {
        method: "POST",
        body: formValues(form),
      });
      form.reset();
      document.getElementById("resetTokenResult").hidden = true;
      showNotice(result.message || "Đặt lại mật khẩu thành công.");
    } catch (error) {
      showError(error);
    }
  });

const existingUsername = sessionStorage.getItem("n1.username");
if (sessionStorage.getItem(TOKEN_KEY) && existingUsername) {
  showSignedInState(existingUsername);
}

if (window.location.hash === "#register") {
  document.getElementById("register")?.scrollIntoView({ block: "start" });
}
