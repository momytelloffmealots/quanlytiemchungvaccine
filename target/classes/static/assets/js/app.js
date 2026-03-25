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
  const auth = session?.authority || "";
  const doctorId = session?.doctorId;
  const cashierId = session?.cashierId;
  const inventoryManagerId = session?.inventoryManagerId;
  const administratorId = session?.administratorId;

  const items = [
    { id: "nav-vaccine", show: true },
    { id: "nav-forms", show: !!(doctorId || inventoryManagerId || administratorId) },
    { id: "nav-payment", show: !!(cashierId || administratorId) },
    { id: "nav-statistics", show: !!administratorId },
    { id: "nav-history", show: !!doctorId },
    { id: "nav-account", show: !!administratorId },
  ];

  // if you store authority strings differently, still keep a fallback
  const fallback = items.map((it) => it.show);
  if (fallback.every((x) => x === false) && auth) {
    // best effort: show all
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

