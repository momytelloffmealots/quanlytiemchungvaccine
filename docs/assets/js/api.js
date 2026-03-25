const API_BASE_URL = "http://localhost:8080";

function apiConnectionHint() {
  try {
    const host = typeof location !== "undefined" ? location.hostname : "";
    const onGhPages = host.endsWith("github.io");
    const localApi =
      API_BASE_URL.includes("localhost") || API_BASE_URL.includes("127.0.0.1");
    if (onGhPages && localApi) {
      return " Bạn đang xem site trên GitHub Pages (HTTPS); trình duyệt không cho gọi http://localhost:8080. Hãy đổi API_BASE_URL trong assets/js/api.js sang URL backend của bạn (HTTPS, công khai) hoặc chạy giao diện trên máy local cùng lúc với Spring Boot.";
    }
  } catch (_) {
    /* ignore */
  }
  return "";
}

async function apiRequest(path, { method = "GET", body } = {}) {
  const url = `${API_BASE_URL}${path}`;
  const headers = {};
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  let res;
  try {
    res = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    const base = (e && e.message) || "Không thể kết nối tới API.";
    throw new Error(base + apiConnectionHint());
  }

  // Try parse JSON error payloads from GlobalExceptionHandler
  let payload = null;
  try {
    payload = await res.json();
  } catch (_) {
    // ignore
  }

  if (!res.ok) {
    const msg = payload?.message || `Request failed: ${res.status}`;
    throw new Error(msg);
  }
  return payload;
}

function getSession() {
  const raw = localStorage.getItem("session");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function setSession(data) {
  localStorage.setItem("session", JSON.stringify(data));
}

function clearSession() {
  localStorage.removeItem("session");
}

