const API_BASE_URL_OVERRIDE = "https://quanlytiemchungvaccine-production-f8af.up.railway.app";
//https://gigglier-colten-answerlessly.ngrok-free.dev
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

const API_TIMEOUT_MS = 15000;

async function apiRequest(path, { method = "GET", body } = {}) {
  // Sửa lỗi lặp đường dẫn: nếu path đã có sẵn API_BASE_URL thì không nối thêm nữa
  const finalPath = path.startsWith("http") ? path : `${API_BASE_URL}${path}`;
  
  const headers = {
    "Content-Type": "application/json",
    // QUAN TRỌNG: Header này giúp bỏ qua trang cảnh báo của ngrok
    "ngrok-skip-browser-warning": "true"
  };

  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), API_TIMEOUT_MS);

  try {
    const res = await fetch(finalPath, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: ctrl.signal,
    });

    let payload = null;
    try {
      payload = await res.json();
    } catch (_) {}

    if (!res.ok) {
      let msg = "";
      if (payload) {
        msg = payload.message || payload.error || "";
      }
      
      if (!msg) {
        try {
          const text = await res.text();
          if (text && text.length < 200) msg = text;
        } catch (_) {}
      }

      if (!msg) msg = `Lỗi hệ thống (${res.status})`;
      throw new Error(msg);
    }
    return payload;
  } finally {
    clearTimeout(timer);
  }
}

// Các hàm getSession, setSession, clearSession giữ nguyên của bạn...
function getSession() { const raw = localStorage.getItem("session"); if (!raw) return null; try { return JSON.parse(raw); } catch { return null; } }
function setSession(data) { localStorage.setItem("session", JSON.stringify(data)); }
function clearSession() { localStorage.removeItem("session"); }