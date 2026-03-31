function requireLogin() {
  const session = getSession();
  if (!session?.success) {
    window.location.href = "login.html";
    return null;
  }
  return session;
}

function logout() {
  clearSession();
  window.location.href = "login.html";
}

function renderSidebar(session) {
  const authRaw = session?.authority ?? session?.role ?? session?.userRole ?? session?.AUTHORITY ?? "";
  const auth = String(authRaw).trim().toUpperCase();
  
  const doctorId = session?.doctorId;
  const cashierId = session?.cashierId;
  const inventoryManagerId = session?.inventoryManagerId;
  const administratorId = session?.administratorId;

  // Cấp toàn quyền cho Admin nếu có ID hoặc role là ADMIN/ADMINISTRATOR
  const isAdmin = auth.includes("ADMIN") || !!administratorId;

  const items = [
    { id: "nav-customers", show: !!(doctorId || isAdmin) },
    { id: "nav-vaccine", show: true },
    { id: "nav-forms", show: !!(doctorId || inventoryManagerId || isAdmin) },
    { id: "nav-payment", show: !!(cashierId || isAdmin) },
    { id: "nav-statistics", show: !!(doctorId || inventoryManagerId || cashierId || isAdmin) },
    { id: "nav-history", show: !!(doctorId || isAdmin) },
    { id: "nav-account", show: !!isAdmin },
  ];

  // Nếu có role khác lạ không khớp các ID trên, mặc định mở hết để tránh kẹt
  const fallback = items.filter(it => it.id !== "nav-vaccine").map((it) => it.show);
  if (fallback.every((x) => x === false) && auth) {
    items.forEach((it) => (it.show = true));
  }

  for (const it of items) {
    const el = document.getElementById(it.id);
    if (!el) continue;
    el.style.display = it.show ? "" : "none";
  }
}

function bindHeaderSession(session) {
  if (!session || !session.success) return;
  const roleRaw =
    session.authority ??
    session.role ??
    session.userRole ??
    session.AUTHORITY ??
    "";
  const role = roleRaw != null ? String(roleRaw).trim() : "";
  const nameRaw =
    session.username ??
    session.userName ??
    session.USERNAME ??
    "";
  let displayName = nameRaw != null ? String(nameRaw).trim() : "";
  if (!displayName && session.accountId != null) {
    displayName = String(session.accountId).trim();
  }

  const roleEl = document.getElementById("headerRole");
  const userLink = document.getElementById("headerUserLink");
  if (roleEl) {
    roleEl.textContent = role || "Chưa xác định";
    roleEl.title = "Vai trò: " + (role || "Chưa xác định");
  }
  if (userLink) {
    userLink.textContent = displayName || "Hồ sơ";
    userLink.href = "profile.html";
    userLink.title = displayName ? "Xem hồ sơ: " + displayName : "Hồ sơ cá nhân";
  }
  const sideBanner = document.getElementById("sidebarRoleBanner");
  if (sideBanner) {
    sideBanner.textContent = role ? "Vai trò: " + role : "Vai trò: Chưa xác định";
  }
}

function initPage() {
  const session = requireLogin();
  if (!session) return;
  bindHeaderSession(session);
  renderSidebar(session);
}

(function applySessionToChrome() {
  try {
    if (typeof getSession !== "function") return;
    const s = getSession();
    if (s && s.success) bindHeaderSession(s);
  } catch (_) {
    /* ignore */
  }
})();

