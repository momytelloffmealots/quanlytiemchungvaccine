/**
 * Chạy "như web thật" cùng Spring Boot: để null — trình duyệt gọi /api... trên cùng host (sau mvn spring-boot:run mở http://localhost:8080/login.html).
 * GitHub Pages + API riêng: đặt URL đầy đủ HTTPS, ví dụ "https://ten-service.railway.app"
 * Giao diện mở bằng Live Server (cổng 5500) mà API ở 8080: đặt "http://localhost:8080"
 */
/** Chỉ domain (KHÔNG thêm /api/...). VD bạn gửi .../api/auth → đặt hết phần ...ngrok-free.dev */
const API_BASE_URL_OVERRIDE = "https://gigglier-colten-answerlessly.ngrok-free.dev";

const API_BASE_URL = (() => {
  if (API_BASE_URL_OVERRIDE != null && String(API_BASE_URL_OVERRIDE).trim() !== "") {
    return String(API_BASE_URL_OVERRIDE).replace(/\/$/, "");
  }
  try {
    const host = typeof location !== "undefined" ? location.hostname : "";
    if (host.endsWith("github.io")) {
      return "http://localhost:8080";
    }
    return "";
  } catch {
    return "";
  }
})();

function apiConnectionHint() {
  try {
    const host = typeof location !== "undefined" ? location.hostname : "";
    const onGhPages = host.endsWith("github.io");
    const localApi =
      API_BASE_URL.includes("localhost") || API_BASE_URL.includes("127.0.0.1");
    if (onGhPages && localApi) {
      return " Bạn đang xem site trên GitHub Pages (HTTPS); trình duyệt không cho gọi http://localhost:8080. Trong assets/js/api.js hãy đặt API_BASE_URL_OVERRIDE = URL HTTPS (vd ngrok), rồi commit + push để Pages cập nhật.";
    }
    if (onGhPages && !localApi) {
      return " Kiểm tra: (1) Spring Boot đang chạy, (2) ngrok/tunnel còn mở và URL trong api.js khớp, (3) đã push file mới lên GitHub — thử Ctrl+F5. Lỗi mạng/CORS cũng gây Failed to fetch.";
    }
  } catch (_) {
    /* ignore */
  }
  return "";
}

/**
 * Site đang chạy trên github.io (HTTPS) nhưng API vẫn là localhost → trình duyệt không gọi được.
 * Trả về true thì form đăng nhập dùng session demo và vào dashboard ngay (chỉ để xem giao diện).
 */
function isGithubPagesStaticDemo() {
  try {
    const host = typeof location !== "undefined" ? location.hostname : "";
    if (!host.endsWith("github.io")) return false;
    const localApi =
      API_BASE_URL.includes("localhost") || API_BASE_URL.includes("127.0.0.1");
    return localApi;
  } catch (_) {
    return false;
  }
}

/** Session giống LoginResponse khi đăng nhập quản trị (full menu). */
function buildDemoSession(username) {
  const u = (username || "").trim() || "demo";
  return {
    success: true,
    message: "Demo UI (GitHub Pages — không gọi được API localhost)",
    accountId: "DEMO",
    username: u,
    authority: "ADMINISTRATOR",
    email: "",
    doctorId: "BS-DEMO",
    cashierId: "TN-DEMO",
    inventoryManagerId: "QL-DEMO",
    administratorId: "QT-DEMO",
    demo: true,
  };
}

const API_TIMEOUT_MS = 15000;

async function apiRequest(path, { method = "GET", body } = {}) {
  const url = `${API_BASE_URL}${path}`;
  const headers = {};
  if (API_BASE_URL.includes("ngrok")) {
    headers["ngrok-skip-browser-warning"] = "true";
  }
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), API_TIMEOUT_MS);

  try {
    let res;
    try {
      res = await fetch(url, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
        signal: ctrl.signal,
      });
    } catch (e) {
      if (e && e.name === "AbortError") {
        throw new Error(
          `Hết thời gian chờ API (${API_TIMEOUT_MS / 1000}s). Server không phản hồi hoặc bị chặn. Kiểm tra Spring Boot đã bật chưa, và trên github.io không dùng được localhost.` +
            apiConnectionHint()
        );
      }
      const base = (e && e.message) || "Không thể kết nối tới API.";
      throw new Error(base + apiConnectionHint());
    }

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
  } finally {
    clearTimeout(timer);
  }
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

