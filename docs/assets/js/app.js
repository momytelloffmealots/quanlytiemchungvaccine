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

function initPage() {
  const session = requireLogin();
  if (!session) return;
  renderSidebar(session);
}

