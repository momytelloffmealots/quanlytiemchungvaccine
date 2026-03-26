/**
 * Chạy "như web thật" cùng Spring Boot: để null — trình duyệt gọi /api... trên cùng host (sau mvn spring-boot:run mở http://localhost:8080/login.html).
 * GitHub Pages + API riêng: đặt URL đầy đủ HTTPS, ví dụ "https://ten-service.railway.app"
 * Giao diện mở bằng Live Server (cổng 5500) mà API ở 8080: đặt "http://localhost:8080"
 */
const API_BASE_URL_OVERRIDE ="https://gigglier-colten-answerlessly.ngrok-free.dev/api/auth";

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
      return " Bạn đang xem site trên GitHub Pages (HTTPS); trình duyệt không cho gọi http://localhost:8080. Hãy đổi API_BASE_URL trong assets/js/api.js sang URL backend của bạn (HTTPS, công khai) hoặc chạy giao diện trên máy local cùng lúc với Spring Boot.";
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

