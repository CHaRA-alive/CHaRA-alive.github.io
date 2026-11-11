(function () {
  const SESSION_KEY = "qingqi_session";
  const ICONS = {
    dashboard: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
    bike: '<circle cx="5.5" cy="17" r="3.5"/><circle cx="18.5" cy="17" r="3.5"/><path d="M5.5 17 9 8h4l4 9H9l-1.5-4H14"/><path d="M12 8h3l2 4"/>',
    route: '<circle cx="6" cy="19" r="2"/><circle cx="18" cy="5" r="2"/><path d="M8 19h5a4 4 0 0 0 4-4V9a2 2 0 0 1 2-2h-1"/><path d="M16 5h-4a2 2 0 0 0-2 2v4"/>',
    wallet: '<path d="M4 6.5A2.5 2.5 0 0 1 6.5 4H19v16H6.5A2.5 2.5 0 0 1 4 17.5z"/><path d="M4 7h15"/><path d="M15 11h5v4h-5a2 2 0 1 1 0-4z"/>',
    wrench: '<path d="M14.5 6.5a4 4 0 0 0-5 5L4 17l3 3 5.5-5.5a4 4 0 0 0 5-5l-2.5 2.5-3-3z"/>',
    shield: '<path d="M12 3 4.5 6v5.5c0 4.5 3.1 7.8 7.5 9.5 4.4-1.7 7.5-5 7.5-9.5V6z"/><path d="m9 12 2 2 4-4"/>',
    users: '<path d="M16 20v-1.5a4.5 4.5 0 0 0-4.5-4.5h-3A4.5 4.5 0 0 0 4 18.5V20"/><circle cx="10" cy="7" r="3.5"/><path d="M17 11a3.5 3.5 0 0 0 0-7"/><path d="M20 20v-1.2a4 4 0 0 0-3-3.8"/>',
    sparkles: '<path d="m12 3 1.2 3.3L16.5 7.5l-3.3 1.2L12 12l-1.2-3.3-3.3-1.2 3.3-1.2z"/><path d="m18.5 13 .8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z"/><path d="m5.5 12 .8 2.2 2.2.8-2.2.8L5.5 18l-.8-2.2-2.2-.8 2.2-.8z"/>',
    layers: '<path d="m12 3 9 5-9 5-9-5z"/><path d="m3 12 9 5 9-5"/><path d="m3 16 9 5 9-5"/>',
    chart: '<path d="M4 20V4"/><path d="M4 20h16"/><path d="M7 16v-5"/><path d="M12 16V7"/><path d="M17 16v-8"/>',
    activity: '<path d="M3 12h4l2.2-6 4.1 12L16 12h5"/>',
    logout: '<path d="M10 4H5v16h5"/><path d="M14 8l4 4-4 4"/><path d="M18 12H9"/>',
    scan: '<path d="M8 4H5a1 1 0 0 0-1 1v3"/><path d="M16 4h3a1 1 0 0 1 1 1v3"/><path d="M20 16v3a1 1 0 0 1-1 1h-3"/><path d="M8 20H5a1 1 0 0 1-1-1v-3"/><path d="M7 12h10"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/>',
    pin: '<path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0z"/><circle cx="12" cy="10" r="2.5"/>',
    check: '<path d="m5 12 4 4L19 6"/>',
    alert: '<path d="M12 3 2.5 20h19z"/><path d="M12 9v5"/><path d="M12 17.2v.1"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/>',
    menu: '<path d="M4 7h16"/><path d="M4 12h16"/><path d="M4 17h16"/>',
    plus: '<path d="M12 5v14"/><path d="M5 12h14"/>',
    "arrow-right": '<path d="M5 12h14"/><path d="m14 7 5 5-5 5"/>',
    refresh: '<path d="M20 11a8 8 0 0 0-14.7-4L3 10"/><path d="M3 5v5h5"/><path d="M4 13a8 8 0 0 0 14.7 4L21 14"/><path d="M21 19v-5h-5"/>',
    lock: '<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>',
    phone: '<path d="M7 3h3l1.5 5-2 1.5a15 15 0 0 0 5 5l1.5-2 5 1.5v3a2 2 0 0 1-2 2C10.7 19 5 13.3 5 5a2 2 0 0 1 2-2z"/>',
    id: '<rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="8" cy="11" r="2"/><path d="M5.5 16c.6-1.6 4.4-1.6 5 0"/><path d="M14 9h4"/><path d="M14 13h4"/><path d="M14 16h3"/>',
    coins: '<ellipse cx="12" cy="6" rx="7" ry="3"/><path d="M5 6v5c0 1.7 3.1 3 7 3s7-1.3 7-3V6"/><path d="M5 11v5c0 1.7 3.1 3 7 3s7-1.3 7-3v-5"/>',
    x: '<path d="m6 6 12 12"/><path d="m18 6-12 12"/>',
    info: '<circle cx="12" cy="12" r="9"/><path d="M12 11v5"/><path d="M12 8v.1"/>',
    play: '<path d="m8 5 11 7-11 7z"/>',
    database: '<ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v6c0 1.7 3.6 3 8 3s8-1.3 8-3V5"/><path d="M4 11v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6"/>',
    bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    map: '<path d="m3 6 6-3 6 3 6-3v15l-6 3-6-3-6 3z"/><path d="M9 3v15"/><path d="M15 6v15"/>',
    list: '<path d="M8 6h12"/><path d="M8 12h12"/><path d="M8 18h12"/><circle cx="4" cy="6" r="1"/><circle cx="4" cy="12" r="1"/><circle cx="4" cy="18" r="1"/>',
    settings: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H2.8v-4H3a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-1.6v-.2h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1z"/>',
    calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4"/><path d="M8 3v4"/><path d="M3 10h18"/><path d="M8 14h.1"/><path d="M12 14h.1"/><path d="M16 14h.1"/>',
    home: '<path d="m3 11 9-8 9 8"/><path d="M5 10v10h14V10"/><path d="M9 20v-6h6v6"/>',
    chevron: '<path d="m9 18 6-6-6-6"/>',
    moon: '<path d="M20 15.2A8.5 8.5 0 0 1 8.8 4 8.5 8.5 0 1 0 20 15.2z"/>'
  };

  function icon(name, className) {
    const path = ICONS[name] || ICONS.info;
    return `<svg class="icon ${className || ""}" viewBox="0 0 24 24" aria-hidden="true">${path}</svg>`;
  }

  async function request(path, options) {
    const config = Object.assign({}, options || {});
    if (config.body && typeof config.body !== "string" && !(config.body instanceof FormData)) {
      config.headers = Object.assign({ "Content-Type": "application/json" }, config.headers || {});
      config.body = JSON.stringify(config.body);
    }
    const response = await fetch(path, config);
    const text = await response.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch (_) {
        data = text;
      }
    }
    if (!response.ok) {
      const message = data && typeof data === "object" ? data.message : null;
      throw new Error(message || `请求失败（HTTP ${response.status}）`);
    }
    return data;
  }

  function escapeHtml(value) {
    return String(value == null ? "" : value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function money(value) {
    const number = Number(value || 0);
    return `¥${number.toFixed(2)}`;
  }

  function fmtDateTime(value) {
    if (!value) return "--";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return new Intl.DateTimeFormat("zh-CN", {
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      hour12: false
    }).format(date);
  }

  function fmtDate(value) {
    if (!value) return "--";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return new Intl.DateTimeFormat("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit"
    }).format(date);
  }

  function statusPill(status) {
    const value = status || "未知";
    let cls = "pill-gray";
    if (["已支付", "可用", "正常", "已完成", "已处理", "已执行", "平稳"].includes(value)) cls = "pill-green";
    if (["使用中", "处理中", "调度中", "待执行", "中"].includes(value)) cls = "pill-blue";
    if (["未支付", "待处理", "未处理", "富余"].includes(value)) cls = "pill-amber";
    if (["故障", "损坏", "冻结", "标记", "高", "执行失败"].includes(value)) cls = "pill-red";
    return `<span class="pill ${cls}">${escapeHtml(value)}</span>`;
  }

  function toast(message, type) {
    let root = document.querySelector(".toast-root");
    if (!root) {
      root = document.createElement("div");
      root.className = "toast-root";
      document.body.appendChild(root);
    }
    const item = document.createElement("div");
    item.className = `toast ${type || "info"}`;
    item.innerHTML = `${icon(type === "success" ? "check" : type === "error" ? "alert" : "info", "icon-sm")}<span>${escapeHtml(message)}</span>`;
    root.appendChild(item);
    window.setTimeout(() => item.remove(), 3200);
  }

  function openModal(content, className) {
    closeModal();
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop";
    backdrop.innerHTML = `<div class="modal ${className || ""}">${content}</div>`;
    backdrop.addEventListener("click", (event) => {
      if (event.target === backdrop) closeModal();
    });
    document.body.appendChild(backdrop);
    document.body.style.overflow = "hidden";
    return backdrop;
  }

  function closeModal() {
    const root = document.querySelector(".modal-backdrop");
    if (root) root.remove();
    document.body.style.overflow = "";
  }

  function getSession() {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY) || "null");
    } catch (_) {
      return null;
    }
  }

  function setSession(session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  function clearSession() {
    localStorage.removeItem(SESSION_KEY);
  }

  function consoleUrl(role) {
    return `/console.html?role=${encodeURIComponent(role)}`;
  }

  function logout() {
    clearSession();
    window.location.href = "/login.html";
  }

  function roleLabel(role) {
    return {
      user: "校园用户",
      admin: "系统管理员",
      operator: "运维工程师",
      security: "安保人员"
    }[role] || "访客";
  }

  function roleIcon(role) {
    return {
      user: "user",
      admin: "dashboard",
      operator: "wrench",
      security: "shield"
    }[role] || "user";
  }

  function debounce(fn, wait) {
    let timer;
    return function (...args) {
      clearTimeout(timer);
      timer = setTimeout(() => fn.apply(this, args), wait || 200);
    };
  }

  window.BikeUI = {
    request,
    icon,
    escapeHtml,
    money,
    fmtDate,
    fmtDateTime,
    statusPill,
    toast,
    openModal,
    closeModal,
    getSession,
    setSession,
    clearSession,
    consoleUrl,
    logout,
    roleLabel,
    roleIcon,
    debounce
  };
})();
