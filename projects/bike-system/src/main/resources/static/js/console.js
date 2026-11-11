(function () {
  const UI = window.BikeUI;
  const session = UI.getSession();
  const query = new URLSearchParams(window.location.search);

  if (!session || !session.role) {
    window.location.replace(`/login.html?role=${encodeURIComponent(query.get("role") || "user")}`);
    return;
  }

  const role = session.role;
  const roleNames = {
    user: "校园用户",
    admin: "运营管理员",
    operator: "运维工程师",
    security: "安保人员"
  };
  const NAV = {
    user: [
      {
        label: "个人空间",
        items: [
          { view: "overview", label: "骑行概览", icon: "dashboard" },
          { view: "ride", label: "扫码用车", icon: "scan" },
          { view: "orders", label: "我的订单", icon: "route" },
          { view: "wallet", label: "余额与账单", icon: "wallet" },
          { view: "repair", label: "车辆报修", icon: "wrench" },
          { view: "credit", label: "信用记录", icon: "shield" }
        ]
      }
    ],
    admin: [
      {
        label: "运营线路",
        items: [
          { view: "overview", label: "运营总览", sub: "实时脉搏", icon: "dashboard" },
          { view: "dispatch", label: "智能调度", sub: "供需调度", icon: "sparkles" },
          { view: "orders", label: "骑行订单", sub: "订单轨迹", icon: "route" },
          { view: "fleet", label: "车辆资产", sub: "车队编组", icon: "bike" }
        ]
      },
      {
        label: "运营网络",
        items: [
          { view: "users", label: "用户管理", sub: "骑行者档案", icon: "users" },
          { view: "maintenance", label: "运维工单", sub: "现场接力", icon: "wrench" },
          { view: "security", label: "安全记录", sub: "校园站点", icon: "shield" }
        ]
      }
    ],
    operator: [
      {
        label: "今日线路",
        items: [
          { view: "overview", label: "今日任务", sub: "任务集合", icon: "dashboard" },
          { view: "works", label: "工单中心", sub: "现场接力", icon: "list" },
          { view: "fleet", label: "车辆状态", sub: "车队巡检", icon: "bike" }
        ]
      }
    ],
    security: [
      {
        label: "安全线路",
        items: [
          { view: "overview", label: "安全概览", sub: "校园巡逻", icon: "dashboard" },
          { view: "records", label: "违规记录", sub: "事件处理", icon: "shield" },
          { view: "zones", label: "围栏区域", sub: "站点边界", icon: "map" }
        ]
      }
    ]
  };

  const navItems = NAV[role] || NAV.user;
  const allowedViews = new Set(navItems.flatMap((group) => group.items.map((item) => item.view)));
  let currentView = query.get("view");
  if (!allowedViews.has(currentView)) currentView = "overview";

  const state = {
    userHome: null,
    dashboard: null,
    dispatch: null,
    plans: [],
    bikes: [],
    orders: [],
    users: [],
    works: [],
    records: [],
    fences: [],
    fleetFilter: "全部",
    workFilter: "全部",
    orderFilter: "全部"
  };

  const shell = document.getElementById("console-shell");
  const sidebar = document.getElementById("console-sidebar");
  const viewRoot = document.getElementById("console-view");
  const breadcrumb = document.getElementById("breadcrumb-current");

  function applyIcons(root) {
    (root || document).querySelectorAll("[data-icon]").forEach((node) => {
      node.innerHTML = UI.icon(node.getAttribute("data-icon"));
    });
  }

  function currentLabel() {
    for (const group of navItems) {
      const item = group.items.find((entry) => entry.view === currentView);
      if (item) return item.label;
    }
    return "工作台";
  }

  function renderShell() {
    const initials = (session.name || roleNames[role] || "青").slice(0, 1);
    if (role === "user") {
      shell.classList.add("user-theme");
      sidebar.innerHTML = `
        <div class="wd-console-header">
          <div class="wd-console-header-inner">
            <a class="wd-brand" href="/">
              <span class="wd-seal">青骑</span>
              <span class="wd-brand-copy">
                <b>青骑智行</b>
                <span>QINGQI · CAMPUS RIDE</span>
              </span>
            </a>
            <nav class="wd-console-nav">
              <button class="${currentView === "overview" ? "active" : ""}" data-nav="overview" type="button">
                <i>骑</i><span>骑行概览</span>
              </button>
              <button class="${currentView === "ride" ? "active" : ""}" data-nav="ride" type="button">
                <i>借</i><span>扫码用车</span>
              </button>
              <button class="${currentView === "orders" ? "active" : ""}" data-nav="orders" type="button">
                <i>程</i><span>我的订单</span>
              </button>
              <button class="${currentView === "wallet" ? "active" : ""}" data-nav="wallet" type="button">
                <i>账</i><span>余额账单</span>
              </button>
              <button class="${currentView === "repair" ? "active" : ""}" data-nav="repair" type="button">
                <i>修</i><span>车辆报修</span>
              </button>
              <button class="${currentView === "credit" ? "active" : ""}" data-nav="credit" type="button">
                <i>信</i><span>信用记录</span>
              </button>
            </nav>
            <div class="wd-console-actions">
              <span class="wd-header-balance">余额 <b id="header-balance">--</b></span>
              <span class="wd-header-avatar" id="header-avatar">${UI.escapeHtml(initials)}</span>
              <button class="console-logout" type="button" data-action="logout" title="退出登录">
                ${UI.icon("logout", "icon-sm")}
              </button>
            </div>
          </div>
        </div>
      `;
      breadcrumb.textContent = currentLabel();
      applyIcons(sidebar);
      return;
    }
    sidebar.innerHTML = `
      <a class="console-brand" href="/">
        <span class="brand-seal">青骑</span>
        <span>
          <strong>青骑智行</strong>
          <span>BIKE OPERATIONS SYSTEM</span>
        </span>
      </a>
      <div class="console-role">
        <span>CURRENT ROLE</span>
        <b>${UI.escapeHtml(roleNames[role] || "用户")}</b>
      </div>
      <nav class="console-nav">
        ${navItems.map((group) => `
          <div class="nav-label">${UI.escapeHtml(group.label)}</div>
          ${group.items.map((item) => `
            <button class="console-nav-item ${item.view === currentView ? "active" : ""}" data-nav="${item.view}" type="button">
              <span class="route-station"></span>
              <span class="route-icon">${UI.icon(item.icon)}</span>
              <span class="route-copy">
                <b>${UI.escapeHtml(item.label)}</b>
                <small>${UI.escapeHtml(item.sub || "")}</small>
              </span>
            </button>
          `).join("")}
        `).join("")}
      </nav>
      <div class="console-sidebar-foot">
        <div class="sidebar-profile">
          <div class="profile-avatar">${UI.escapeHtml(initials)}</div>
          <div class="profile-copy">
            <b>${UI.escapeHtml(session.name || roleNames[role])}</b>
            <span>${UI.escapeHtml(session.id || "--")}</span>
          </div>
          <button class="console-logout" type="button" data-action="logout" title="退出登录">
            ${UI.icon("logout", "icon-sm")}
          </button>
        </div>
      </div>
    `;
    breadcrumb.textContent = currentLabel();
    applyIcons(sidebar);
  }

  function updateClock() {
    const now = new Date();
    const formatter = new Intl.DateTimeFormat("zh-CN", {
      month: "2-digit",
      day: "2-digit",
      weekday: "short",
      hour: "2-digit",
      minute: "2-digit",
      hour12: false
    });
    const element = document.getElementById("live-clock");
    if (element) element.textContent = formatter.format(now);
  }

  function pageHead(title, description, actions) {
    return `
      <div class="page-head">
        <div>
          <h1>${UI.escapeHtml(title)}</h1>
          <p>${UI.escapeHtml(description || "")}</p>
        </div>
        <div class="page-actions">${actions || ""}</div>
      </div>
    `;
  }

  function updateUserHeader(user) {
    if (!user) return;
    const balance = document.getElementById("header-balance");
    const avatar = document.getElementById("header-avatar");
    if (balance) balance.textContent = UI.money(user.balance);
    if (avatar) avatar.textContent = (user.userName || session.name || "青").slice(0, 1);
  }

  function riderBanner(eyebrow, title, description, actions, side) {
    return `
      <section class="wd-user-banner">
        <div class="wd-user-banner-inner wd-container">
          <div>
            <em class="wd-eyebrow">${UI.escapeHtml(eyebrow)}</em>
            <h1>${title}</h1>
            <p>${description}</p>
            ${actions ? `<div class="wd-user-banner-actions">${actions}</div>` : ""}
          </div>
          ${side || ""}
        </div>
      </section>
    `;
  }

  function riderStatus(order) {
    if (!order.endTime) return `<span class="wd-rider-status active">骑行中</span>`;
    if (order.payStatus === "未支付") return `<span class="wd-rider-status unpaid">待支付</span>`;
    return `<span class="wd-rider-status paid">已完成</span>`;
  }

  function riderOrderRows(orders, actions) {
    if (!orders || !orders.length) return `<div class="wd-empty-rider">还没有骑行记录，去校园里兜一圈吧。</div>`;
    return `
      <div class="wd-ride-list">
        ${orders.map((order) => `
          <div class="wd-ride-row">
            <div class="wd-ride-time">
              <b>${UI.fmtDateTime(order.startTime).slice(0, 5)}</b>
              ${UI.fmtDateTime(order.startTime).slice(6)}
            </div>
            <div class="wd-ride-route">
              <b>${UI.escapeHtml(order.startPosition || "校园出发")} <span style="color:var(--wd-ink-faint);font-weight:400">→</span> ${UI.escapeHtml(order.endPosition || "骑行中")}</b>
              <p class="mono">${UI.escapeHtml(order.orderId)} · 车辆 ${UI.escapeHtml(order.bikeId || "--")}</p>
            </div>
            <div class="wd-ride-fee">
              <b>${order.fee == null ? "计费中" : UI.money(order.fee)}</b>
              ${riderStatus(order)}
              ${actions ? `
                <div style="display:flex;gap:6px;justify-content:flex-end;margin-top:7px">
                  ${!order.endTime ? `<button class="wd-mini-btn" type="button" data-action="end-order" data-id="${UI.escapeHtml(order.orderId)}">结束</button>` : ""}
                  ${order.endTime && order.payStatus === "未支付" ? `<button class="wd-mini-btn primary" type="button" data-action="pay-order" data-id="${UI.escapeHtml(order.orderId)}">支付</button>` : ""}
                </div>
              ` : ""}
            </div>
          </div>
        `).join("")}
      </div>
    `;
  }

  function riderBikeCards(bikes, compact) {
    if (!bikes || !bikes.length) return `<div class="wd-empty-rider">附近暂时没有可用车辆。</div>`;
    return `
      <div class="wd-bike-grid-rider">
        ${bikes.map((bike) => `
          <article class="wd-bike-rider">
            <div class="wd-bike-rider-art">
              ${UI.icon("bike")}
            </div>
            <h3>${UI.escapeHtml(bike.bikeId)}</h3>
            <p>${UI.escapeHtml(bike.area || "校园")} · ${UI.escapeHtml(bike.healthStatus || "正常")}</p>
            ${compact ? "" : `<button class="wd-btn wd-btn-indigo" type="button" data-action="use-bike" data-id="${UI.escapeHtml(bike.bikeId)}">选择这辆车</button>`}
          </article>
        `).join("")}
      </div>
    `;
  }

  function kpi(label, value, unit, icon, hint, tone) {
    return `
      <article class="kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">${UI.escapeHtml(label)}</span>
          <span class="kpi-icon" style="${tone || ""}">${UI.icon(icon)}</span>
        </div>
        <div class="kpi-value">${UI.escapeHtml(value)}${unit ? `<small>${UI.escapeHtml(unit)}</small>` : ""}</div>
        <div class="kpi-hint">${UI.escapeHtml(hint || "")}</div>
      </article>
    `;
  }

  function loadingView() {
    viewRoot.innerHTML = `<div class="loading-panel"><div><span class="spinner"></span><span>正在读取业务数据...</span></div></div>`;
  }

  function errorView(error) {
    viewRoot.innerHTML = `
      ${pageHead("数据读取失败", "后端服务或数据库暂时不可用")}
      <div class="panel">
        <div class="empty-state">
          <div>
            <div class="empty-icon">${UI.icon("alert")}</div>
            <b>${UI.escapeHtml(error.message || "未知错误")}</b>
            <p class="muted" style="margin-top:6px">请确认 MySQL 与 Spring Boot 服务已启动，然后刷新当前页面。</p>
            <button class="btn btn-primary" style="margin-top:18px" data-action="refresh-view">重新加载</button>
          </div>
        </div>
      </div>
    `;
  }

  function emptyView(message, iconName) {
    return `
      <div class="empty-state">
        <div>
          <div class="empty-icon">${UI.icon(iconName || "info")}</div>
          <b>${UI.escapeHtml(message)}</b>
        </div>
      </div>
    `;
  }

  function navigate(view, push) {
    if (!allowedViews.has(view)) return;
    currentView = view;
    shell.classList.remove("menu-open");
    if (push !== false) {
      const next = new URL(window.location.href);
      next.searchParams.set("role", role);
      next.searchParams.set("view", view);
      window.history.replaceState({}, "", next);
    }
    sidebar.querySelectorAll("[data-nav]").forEach((item) => {
      item.classList.toggle("active", item.dataset.nav === view);
    });
    breadcrumb.textContent = currentLabel();
    loadView();
  }

  async function loadView() {
    loadingView();
    try {
      if (role === "user") {
        if (currentView === "overview") await loadUserOverview();
        if (currentView === "ride") await loadUserRide();
        if (currentView === "orders") await loadUserOrders();
        if (currentView === "wallet") await loadUserWallet();
        if (currentView === "repair") await loadUserRepair();
        if (currentView === "credit") await loadUserCredit();
      } else if (role === "admin") {
        if (currentView === "overview") await loadAdminOverview();
        if (currentView === "dispatch") await loadDispatch();
        if (currentView === "orders") await loadAdminOrders();
        if (currentView === "fleet") await loadFleet("admin");
        if (currentView === "users") await loadAdminUsers();
        if (currentView === "maintenance") await loadMaintenance("admin");
        if (currentView === "security") await loadSecurityRecords("admin");
      } else if (role === "operator") {
        if (currentView === "overview") await loadOperatorOverview();
        if (currentView === "works") await loadMaintenance("operator");
        if (currentView === "fleet") await loadFleet("operator");
      } else if (role === "security") {
        if (currentView === "overview") await loadSecurityOverview();
        if (currentView === "records") await loadSecurityRecords("security");
        if (currentView === "zones") await loadSecurityZones();
      }
      viewRoot.classList.remove("view-enter");
      void viewRoot.offsetWidth;
      viewRoot.classList.add("view-enter");
      applyIcons(viewRoot);
    } catch (error) {
      errorView(error);
      applyIcons(viewRoot);
    }
  }

  function lineChart(data, key) {
    const values = data.map((item) => Number(item[key] || 0));
    const labels = data.map((item) => item.label || "");
    const width = 720;
    const height = 230;
    const pad = { left: 34, right: 18, top: 22, bottom: 31 };
    const max = Math.max(1, ...values);
    const chartWidth = width - pad.left - pad.right;
    const chartHeight = height - pad.top - pad.bottom;
    const points = values.map((value, index) => {
      const x = pad.left + (values.length <= 1 ? chartWidth / 2 : (chartWidth * index) / (values.length - 1));
      const y = pad.top + chartHeight - (value / max) * chartHeight;
      return { x, y, value };
    });
    const polyline = points.map((point) => `${point.x},${point.y}`).join(" ");
    const areaPoints = points.map((point) => `L ${point.x} ${point.y}`).join(" ");
    const area = points.length
      ? `M ${points[0].x} ${pad.top + chartHeight} ${areaPoints} L ${points[points.length - 1].x} ${pad.top + chartHeight} Z`
      : "";
    const grid = [0, 1, 2, 3].map((index) => {
      const y = pad.top + (chartHeight * index) / 3;
      return `<line class="chart-grid-line" x1="${pad.left}" y1="${y}" x2="${width - pad.right}" y2="${y}"/>`;
    }).join("");
    const dots = points.map((point) => `
      <circle class="chart-point" cx="${point.x}" cy="${point.y}" r="3.5"/>
      <text class="chart-value" x="${point.x}" y="${point.y - 9}" text-anchor="middle">${point.value}</text>
    `).join("");
    const labelsHtml = labels.map((label, index) => {
      const x = pad.left + (labels.length <= 1 ? chartWidth / 2 : (chartWidth * index) / (labels.length - 1));
      return `<text class="chart-label" x="${x}" y="${height - 8}" text-anchor="middle">${UI.escapeHtml(label)}</text>`;
    }).join("");
    return `
      <div class="chart">
        <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" role="img" aria-label="近七日趋势">
          <defs>
            <linearGradient id="chart-area-gradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0" stop-color="#1faf72" stop-opacity="0.19"/>
              <stop offset="1" stop-color="#1faf72" stop-opacity="0"/>
            </linearGradient>
          </defs>
          ${grid}
          <path class="chart-area" d="${area}"/>
          <polyline class="chart-line" points="${polyline}"/>
          ${dots}
          ${labelsHtml}
        </svg>
      </div>
    `;
  }

  function donutChart(items) {
    const colors = {
      "可用": "#1faf72",
      "使用中": "#35c886",
      "故障": "#c46d52",
      "调度中": "#6f8f7f"
    };
    const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0);
    let cursor = 0;
    const segments = items.map((item) => {
      const start = cursor;
      cursor += total ? (Number(item.value || 0) / total) * 100 : 0;
      return `${colors[item.name] || "#8b97a8"} ${start}% ${cursor}%`;
    }).join(", ");
    return `
      <div class="donut-wrap">
        <div class="donut" style="background:conic-gradient(${segments || "#e9e7e1 0 100%"})">
          <div class="donut-center"><b>${total}</b><span>车辆总数</span></div>
        </div>
        <div class="legend">
          ${items.map((item) => `
            <div class="legend-item">
              <span class="legend-dot" style="background:${colors[item.name] || "#8b97a8"}"></span>
              <span>${UI.escapeHtml(item.name)}</span>
              <b>${UI.escapeHtml(item.value)}</b>
            </div>
          `).join("")}
        </div>
      </div>
    `;
  }

  function ordersTable(orders, options) {
    const opts = options || {};
    if (!orders || !orders.length) return emptyView("暂无骑行订单", "route");
    return `
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>订单编号</th>
              ${opts.showUser ? "<th>用户</th>" : ""}
              <th>车辆</th>
              <th>路线</th>
              <th>开始时间</th>
              <th>费用</th>
              <th>状态</th>
              ${opts.actions ? "<th>操作</th>" : ""}
            </tr>
          </thead>
          <tbody>
            ${orders.map((order) => `
              <tr>
                <td><strong class="mono">${UI.escapeHtml(order.orderId)}</strong></td>
                ${opts.showUser ? `<td>${UI.escapeHtml(order.userId || "--")}</td>` : ""}
                <td class="mono">${UI.escapeHtml(order.bikeId || "--")}</td>
                <td>${UI.escapeHtml(order.startPosition || "--")} <span class="muted">→</span> ${UI.escapeHtml(order.endPosition || "骑行中")}</td>
                <td>${UI.fmtDateTime(order.startTime)}</td>
                <td>${order.fee == null ? "--" : UI.money(order.fee)}</td>
                <td>${UI.statusPill(order.payStatus)} ${UI.statusPill(order.parkStatus)}</td>
                ${opts.actions ? `
                  <td>
                    <div class="table-actions">
                      ${!order.endTime ? `<button class="btn btn-sm btn-outline" type="button" data-action="end-order" data-id="${UI.escapeHtml(order.orderId)}">结束骑行</button>` : ""}
                      ${order.endTime && order.payStatus === "未支付" ? `<button class="btn btn-sm btn-primary" type="button" data-action="pay-order" data-id="${UI.escapeHtml(order.orderId)}">余额支付</button>` : ""}
                    </div>
                  </td>
                ` : ""}
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  }

  function bikesTable(bikes, options) {
    const opts = options || {};
    if (!bikes || !bikes.length) return emptyView("暂无车辆数据", "bike");
    return `
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>车辆编号</th>
              <th>所属区域</th>
              <th>运营状态</th>
              <th>健康状态</th>
              <th>二维码</th>
              <th>位置坐标</th>
              ${opts.actions ? "<th>操作</th>" : ""}
            </tr>
          </thead>
          <tbody>
            ${bikes.map((bike) => `
              <tr>
                <td><strong class="mono">${UI.escapeHtml(bike.bikeId)}</strong></td>
                <td>${UI.escapeHtml(bike.area || "未分区")}</td>
                <td>${UI.statusPill(bike.bikeStatus)}</td>
                <td>${UI.statusPill(bike.healthStatus)}</td>
                <td class="mono">${UI.escapeHtml(bike.qrCode || "--")}</td>
                <td class="mono">${bike.location == null ? "--" : UI.escapeHtml(bike.location)}</td>
                ${opts.actions ? `
                  <td>
                    ${bike.bikeStatus === "故障" && bike.healthStatus !== "损坏" ? `<button class="btn btn-sm btn-outline" type="button" data-action="repair-bike" data-id="${UI.escapeHtml(bike.bikeId)}">创建维修单</button>` : `<span class="muted">--</span>`}
                  </td>
                ` : ""}
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  }

  function workTable(works, options) {
    const opts = options || {};
    if (!works || !works.length) return emptyView("暂无运维工单", "wrench");
    return `
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>工单编号</th>
              <th>车辆</th>
              <th>类型</th>
              <th>问题描述</th>
              <th>上报时间</th>
              <th>处理人</th>
              <th>状态</th>
              ${opts.actions ? "<th>操作</th>" : ""}
            </tr>
          </thead>
          <tbody>
            ${works.map((work) => `
              <tr>
                <td><strong class="mono">${UI.escapeHtml(work.workId)}</strong></td>
                <td class="mono">${UI.escapeHtml(work.bikeId || "--")}</td>
                <td>${UI.escapeHtml(work.workType || "--")}</td>
                <td style="max-width:260px">${UI.escapeHtml(work.desc || work.result || "--")}</td>
                <td>${UI.fmtDateTime(work.reportTime)}</td>
                <td>${UI.escapeHtml(work.handlerId || "未指派")}</td>
                <td>${UI.statusPill(work.handleStatus)}</td>
                ${opts.actions ? `
                  <td>
                    <div class="table-actions">
                      ${work.handleStatus === "待处理" ? `<button class="btn btn-sm btn-primary" type="button" data-action="accept-work" data-id="${UI.escapeHtml(work.workId)}">接受任务</button>` : ""}
                      ${work.handleStatus === "处理中" ? `<button class="btn btn-sm btn-outline" type="button" data-action="complete-work" data-id="${UI.escapeHtml(work.workId)}">完成处理</button>` : ""}
                      ${work.handleStatus === "已完成" ? `<span class="muted">已归档</span>` : ""}
                    </div>
                  </td>
                ` : ""}
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  }

  function recordsTable(records, options) {
    const opts = options || {};
    if (!records || !records.length) return emptyView("暂无违规记录", "shield");
    return `
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>记录编号</th>
              <th>车辆</th>
              <th>违规类型</th>
              <th>违规位置</th>
              <th>上报人</th>
              <th>上报时间</th>
              <th>状态</th>
              ${opts.actions ? "<th>操作</th>" : ""}
            </tr>
          </thead>
          <tbody>
            ${records.map((record) => `
              <tr>
                <td><strong class="mono">${UI.escapeHtml(record.reportId)}</strong></td>
                <td class="mono">${UI.escapeHtml(record.violationBikeId || "--")}</td>
                <td>${UI.escapeHtml(record.violationType || "其他违规")}</td>
                <td>${UI.escapeHtml(record.violationPosition || "--")}</td>
                <td>${UI.escapeHtml(record.reporterId || "--")}</td>
                <td>${UI.fmtDateTime(record.reportTime)}</td>
                <td>${UI.statusPill(record.handleStatus)}</td>
                ${opts.actions ? `
                  <td>
                    ${record.handleStatus === "未处理" ? `<button class="btn btn-sm btn-primary" type="button" data-action="handle-record" data-id="${UI.escapeHtml(record.reportId)}">标记已处理</button>` : `<span class="muted">已归档</span>`}
                  </td>
                ` : ""}
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  }

  function statusBars(items) {
    const maxDemand = Math.max(1, ...items.map((item) => Number(item.demandScore || 0)));
    const maxSupply = Math.max(1, ...items.map((item) => Number(item.availableBikes || 0)));
    return `
      <div class="zone-list">
        ${items.map((item) => `
          <div class="zone-row">
            <div class="zone-name">
              ${UI.icon("pin", "icon-sm")}
              <span>${UI.escapeHtml(item.area)}</span>
            </div>
            <div class="zone-bars">
              <div class="zone-bar demand"><i style="width:${Math.max(2, (Number(item.demandScore || 0) / maxDemand) * 100)}%"></i></div>
              <div class="zone-bar supply"><i style="width:${Math.max(2, (Number(item.availableBikes || 0) / maxSupply) * 100)}%"></i></div>
            </div>
            <div class="zone-values">
              <span>需求 ${item.demandScore}</span>
              <span>供给 ${item.availableBikes}</span>
              ${UI.statusPill(item.pressure)}
            </div>
          </div>
        `).join("")}
      </div>
      <div style="display:flex;gap:16px;margin-top:16px;color:var(--muted);font-size:9px">
        <span><i style="display:inline-block;width:14px;height:4px;border-radius:9px;background:#1faf72;margin-right:5px"></i>预测需求</span>
        <span><i style="display:inline-block;width:14px;height:4px;border-radius:9px;background:#9ac7b1;margin-right:5px"></i>可用车辆</span>
      </div>
    `;
  }

  function opsMap(zones) {
    const positions = {
      "教学区": { left: 27, top: 42 },
      "宿舍区": { left: 16, top: 72 },
      "图书馆": { left: 52, top: 24 },
      "食堂区": { left: 70, top: 59 },
      "体育馆": { left: 84, top: 30 }
    };
    return `
      <div class="ops-map">
        <div class="ops-map-head">
          <span>运营站点 · 供需实时</span>
          <b>${zones.length} STATIONS</b>
        </div>
        ${(zones || []).map((zone) => {
          const pos = positions[zone.area] || { left: 48, top: 48 };
          const gap = Number(zone.gap || 0);
          const tone = gap > 1.2 ? "demand" : (Number(zone.availableBikes || 0) - Number(zone.demandScore || 0) > 1 ? "surplus" : "steady");
          return `
            <div class="zone-node ${tone}" style="left:${pos.left}%;top:${pos.top}%">
              <i></i>
              <div>
                <b>${UI.escapeHtml(zone.area)}</b>
                <span>可借 ${zone.availableBikes} · 需求 ${zone.demandScore}</span>
              </div>
            </div>
          `;
        }).join("")}
        <span class="ops-map-rider">${UI.icon("bike")}</span>
        <div class="ops-map-legend">
          <span><i class="steady"></i>供需平稳</span>
          <span><i class="demand"></i>需求偏高</span>
          <span><i class="surplus"></i>车辆富余</span>
        </div>
      </div>
    `;
  }

  function fleetLanes(items) {
    const tones = {
      "可用": "#1faf72",
      "使用中": "#35c886",
      "故障": "#c46d52",
      "调度中": "#789487"
    };
    const total = Math.max(1, (items || []).reduce((sum, item) => sum + Number(item.value || 0), 0));
    return `
      <div class="fleet-lanes">
        ${(items || []).map((item) => `
          <div class="fleet-lane">
            <span class="fleet-lane-icon">${UI.icon("bike", "icon-sm")}</span>
            <span class="fleet-lane-name">${UI.escapeHtml(item.name)}</span>
            <span class="fleet-lane-track">
              <i style="width:${Math.max(4, Number(item.value || 0) / total * 100)}%;background:${tones[item.name] || "#9aa9a1"}"></i>
            </span>
            <b>${item.value}</b>
          </div>
        `).join("")}
        <div class="fleet-lanes-foot">
          <span>车队编组完成度</span>
          <b>${total} 辆在册</b>
        </div>
      </div>
    `;
  }

  function opsRelay(kpis, dispatch) {
    const suggestions = dispatch && dispatch.items ? dispatch.items.length : 0;
    const steps = [
      { no: "01", title: "需求识别", value: `${dispatch ? dispatch.sampleOrders : 0} 条订单样本`, icon: "activity" },
      { no: "02", title: "车辆补给", value: suggestions ? `${suggestions} 条调配建议` : "当前无缺口", icon: "sparkles" },
      { no: "03", title: "现场接力", value: `${kpis.pendingWorks || 0} 个运维工单`, icon: "wrench" },
      { no: "04", title: "安全收口", value: `${kpis.unhandledViolations || 0} 条待处理记录`, icon: "shield" }
    ];
    return `
      <div class="ops-relay">
        ${steps.map((step, index) => `
          <div class="ops-relay-step">
            <span class="ops-relay-icon">${UI.icon(step.icon)}</span>
            <div><em>${step.no}</em><b>${UI.escapeHtml(step.title)}</b><small>${UI.escapeHtml(step.value)}</small></div>
          </div>
          ${index < steps.length - 1 ? '<span class="ops-relay-link">→</span>' : ""}
        `).join("")}
      </div>
    `;
  }

  async function loadUserOverview() {
    const [home, bikes] = await Promise.all([
      UI.request(`/api/dashboard/user/${encodeURIComponent(session.id)}/home`),
      UI.request("/api/bikes/available")
    ]);
    state.userHome = home;
    state.bikes = bikes;
    updateUserHeader(home.user);
    const stats = home.stats || {};
    const active = home.activeOrder;
    viewRoot.innerHTML = `
      ${riderBanner(
        "MY RIDE · 我的骑行",
        `${UI.escapeHtml(home.user.userName || session.name)}，今天骑哪一程？`,
        active
          ? `订单 ${UI.escapeHtml(active.orderId)} 正在进行，车辆 ${UI.escapeHtml(active.bikeId || "--")}。记得把车停进推荐区域。`
          : `校园里还有 ${stats.availableBikeCount || 0} 辆车可以借。去教学楼、图书馆，或者只是绕着操场吹吹风。`,
        active
          ? `<button class="wd-btn wd-btn-primary" data-action="end-order" data-id="${UI.escapeHtml(active.orderId)}" type="button">结束当前骑行</button>
             <button class="wd-btn wd-btn-outline-light" data-nav="orders" type="button">查看订单</button>`
          : `<button class="wd-btn wd-btn-primary" data-nav="ride" type="button">扫码借车 ${UI.icon("arrow-right")}</button>
             <button class="wd-btn wd-btn-outline-light" data-nav="orders" type="button">我的骑行</button>`,
        `<div class="wd-user-banner-side">
          <span class="wd-avatar-large">${UI.escapeHtml((home.user.userName || session.name || "青").slice(0, 1))}</span>
          <div class="wd-banner-user">
            <b>${UI.escapeHtml(home.user.userName || session.name)}</b>
            <span>${UI.escapeHtml(home.user.userId)} · 信用${UI.escapeHtml(home.user.credit || "良好")}</span>
          </div>
        </div>`
      )}
      <div class="wd-user-body">
        <div class="wd-rider-stats">
          <div class="wd-stat"><span class="wd-stat-icon">${UI.icon("wallet")}</span><div><b>${UI.money(home.user.balance)}</b><span>账户余额</span></div></div>
          <div class="wd-stat"><span class="wd-stat-icon">${UI.icon("route")}</span><div><b>${stats.totalRides || 0}<small style="font-size:11px;color:var(--wd-ink-faint)"> 次</small></b><span>累计骑行</span></div></div>
          <div class="wd-stat"><span class="wd-stat-icon">${UI.icon("alert")}</span><div><b>${stats.unpaidCount || 0}<small style="font-size:11px;color:var(--wd-ink-faint)"> 笔</small></b><span>待支付订单</span></div></div>
          <div class="wd-stat"><span class="wd-stat-icon">${UI.icon("bike")}</span><div><b>${stats.availableBikeCount || 0}<small style="font-size:11px;color:var(--wd-ink-faint)"> 辆</small></b><span>此刻可借</span></div></div>
        </div>

        <div class="wd-quick-strip">
          <button class="wd-quick-card" data-nav="ride" type="button">${UI.icon("scan")}<div><b>扫码借车</b><span>从附近车辆开始</span></div></button>
          <button class="wd-quick-card" data-nav="orders" type="button">${UI.icon("route")}<div><b>我的骑行</b><span>看每一段路</span></div></button>
          <button class="wd-quick-card" data-nav="wallet" type="button">${UI.icon("coins")}<div><b>余额账单</b><span>充值与消费记录</span></div></button>
          <button class="wd-quick-card" data-nav="repair" type="button">${UI.icon("wrench")}<div><b>车辆报修</b><span>让下一程更顺</span></div></button>
        </div>

        <div class="wd-user-layout">
          <main class="wd-user-main">
            ${active ? `
              <div class="wd-active-ride">
                <em>RIDING NOW · 正在骑行</em>
                <h3>${UI.escapeHtml(active.startPosition || "校园出发")} → ${UI.escapeHtml(active.endPosition || "还在路上")}</h3>
                <p>${UI.fmtDateTime(active.startTime)} 出发 · 车辆 ${UI.escapeHtml(active.bikeId || "--")} · 请规范停放</p>
                <button class="wd-btn wd-btn-primary" data-action="end-order" data-id="${UI.escapeHtml(active.orderId)}" type="button">结束这一程</button>
              </div>
            ` : `
              <div class="wd-active-ride" style="background:radial-gradient(ellipse at 88% 18%,rgba(255,255,255,.16),transparent 38%),linear-gradient(120deg,#0f5e45,#1f9b68)">
                <em>READY TO RIDE · 随时出发</em>
                <h3>去校园里，吹一会儿风。</h3>
                <p>从宿舍楼下借一辆车，经过林荫道、食堂和图书馆，把今天走成一条只属于你的路线。</p>
                <button class="wd-btn wd-btn-primary" data-nav="ride" type="button">找一辆车</button>
              </div>
            `}

            <section style="margin-top:30px">
              <div class="wd-section-title-row">
                <h2>最近骑行 <small>RECENT RIDES</small></h2>
                <button class="wd-text-link" data-nav="orders" type="button">全部记录 →</button>
              </div>
              ${riderOrderRows((home.recentOrders || []).slice(0, 5), true)}
            </section>

            <section style="margin-top:31px">
              <div class="wd-section-title-row">
                <h2>附近可借 <small>NEARBY BIKES</small></h2>
                <button class="wd-text-link" data-nav="ride" type="button">查看更多 →</button>
              </div>
              ${riderBikeCards(bikes.slice(0, 3), false)}
            </section>
          </main>

          <aside class="wd-user-aside">
            <section class="wd-aside-card dark">
              <h3>我的信用</h3>
              <p>规范停车，让每一辆车都能被下一位同学顺利找到。</p>
              <div class="wd-credit-block" style="border-color:rgba(255,255,255,.26);background:rgba(255,255,255,.1)">
                <b style="color:#ffffff">当前状态 <span>${UI.escapeHtml(home.user.credit || "未标记")}</span></b>
              </div>
              <div class="wd-side-list">
                <div class="wd-side-row"><i></i>账户余额 ${UI.money(home.user.balance)}</div>
                <div class="wd-side-row"><i></i>${stats.unpaidCount || 0} 笔订单待支付</div>
                <div class="wd-side-row"><i></i>${home.creditEvents ? home.creditEvents.length : 0} 条信用事件</div>
              </div>
            </section>
            <section class="wd-aside-card">
              <h3>骑行小贴士</h3>
              <p>前 15 分钟 1.50 元，之后每 15 分钟加收 0.50 元。</p>
              <div class="wd-side-list">
                <div class="wd-side-row"><i></i>停入推荐区域，车头朝向一致</div>
                <div class="wd-side-row"><i></i>不占消防通道和宿舍出入口</div>
                <div class="wd-side-row"><i></i>发现故障，及时提交报修</div>
              </div>
            </section>
          </aside>
        </div>
      </div>
    `;
  }

  async function loadUserRide() {
    const [home, bikes] = await Promise.all([
      UI.request(`/api/dashboard/user/${encodeURIComponent(session.id)}/home`),
      UI.request("/api/bikes/available")
    ]);
    state.userHome = home;
    state.bikes = bikes;
    updateUserHeader(home.user);
    const active = home.activeOrder;
    viewRoot.innerHTML = `
      ${riderBanner(
        "BORROW · 扫码借车",
        active ? "这一程还没结束" : "风从宿舍楼下起，车在等你。",
        active
          ? `当前订单 ${UI.escapeHtml(active.orderId)} 已经开始。结束这一程之后，才能借下一辆车。`
          : "车身二维码下方有 8 位车辆编号。也可以直接从附近可用车辆中选择一辆。",
        `<button class="wd-btn wd-btn-outline-light" data-nav="overview" type="button">回到概览</button>`,
        ""
      )}
      <div class="wd-user-body">
        ${active ? `
          <div class="wd-active-ride" style="margin-bottom:24px">
            <em>RIDING NOW · 正在骑行</em>
            <h3>${UI.escapeHtml(active.startPosition || "校园出发")} → ${UI.escapeHtml(active.endPosition || "还在路上")}</h3>
            <p>${UI.fmtDateTime(active.startTime)} 出发 · 车辆 ${UI.escapeHtml(active.bikeId || "--")} · 订单 ${UI.escapeHtml(active.orderId)}</p>
            <button class="wd-btn wd-btn-primary" data-action="end-order" data-id="${UI.escapeHtml(active.orderId)}" type="button">结束当前骑行</button>
          </div>
        ` : ""}
        <div class="wd-scan-layout">
          <section class="wd-paper-panel">
            <h2>${active ? "先结束，再出发" : "识别车辆，开始骑行"}</h2>
            <p>${active ? "订单结束后会按骑行时间计费，并生成可支付的电子账单。" : "输入车辆编号和起始位置。开锁后车辆会立即锁定，避免被重复租用。"}</p>
            ${active ? `
              <div style="margin-top:22px;padding:18px;border:1px dashed var(--wd-line-strong);border-radius:5px;background:var(--wd-paper)">
                <div style="color:var(--wd-ink-faint);font-size:10px;letter-spacing:.08em">CURRENT ORDER</div>
                <div style="margin-top:6px;font-family:var(--wd-display);font-size:19px;color:var(--wd-ink)">${UI.escapeHtml(active.orderId)}</div>
                <div style="margin-top:5px;color:var(--wd-ink-soft);font-size:11px">结束位置可在弹窗中选择，违规停车会加收费用并标记信用。</div>
              </div>
            ` : `
              <form id="ride-form" class="wd-rider-form">
                <div class="wd-rider-field">
                  <label for="ride-bike-id">车辆编号</label>
                  <input id="ride-bike-id" name="bikeId" maxlength="8" placeholder="例如：00000004" required>
                </div>
                <div class="wd-rider-field">
                  <label for="ride-position">从哪里出发</label>
                  <select id="ride-position" name="startPosition" required>
                    <option value="宿舍区6号楼">宿舍区6号楼</option>
                    <option value="教学区A区">教学区A区</option>
                    <option value="图书馆北门">图书馆北门</option>
                    <option value="食堂区">食堂区</option>
                    <option value="体育馆">体育馆</option>
                  </select>
                </div>
                <div class="wd-rider-field span-2">
                  <button class="wd-rider-submit" id="ride-submit" type="submit">确认开锁 · 开始骑行</button>
                </div>
              </form>
            `}
          </section>
          <div class="wd-scan-visual">
            ${UI.icon("scan")}
          </div>
        </div>

        <section style="margin-top:34px">
          <div class="wd-section-title-row">
            <h2>此刻可借 <small>${bikes.length} BIKES</small></h2>
            <span class="wd-text-link">车辆状态实时同步</span>
          </div>
          ${riderBikeCards(bikes.slice(0, 9), Boolean(active))}
        </section>
      </div>
    `;
  }

  async function loadUserOrders() {
    const [home, rawOrders] = await Promise.all([
      UI.request(`/api/dashboard/user/${encodeURIComponent(session.id)}/home`),
      UI.request(`/api/orders/user/${encodeURIComponent(session.id)}`)
    ]);
    const orders = rawOrders.slice().sort((a, b) =>
      new Date(b.startTime || 0).getTime() - new Date(a.startTime || 0).getTime()
    );
    state.userHome = home;
    state.orders = orders;
    updateUserHeader(home.user);
    const filtered = state.orderFilter === "全部"
      ? orders
      : orders.filter((order) => state.orderFilter === "待支付"
        ? order.payStatus === "未支付"
        : order.endTime == null);
    viewRoot.innerHTML = `
      ${riderBanner(
        "MY ORDERS · 我的骑行",
        "每一段路，都留了痕迹。",
        `共 ${orders.length} 次骑行记录。未支付的订单可以在余额充足时直接结算。`,
        `<button class="wd-btn wd-btn-outline-light" data-nav="ride" type="button">再借一辆车</button>`,
        ""
      )}
      <div class="wd-user-body">
        <div class="wd-user-layout">
          <main class="wd-user-main">
            <div class="wd-rider-toolbar">
              ${["全部", "待支付", "进行中"].map((filter) => `
                <button class="wd-rider-tab ${state.orderFilter === filter ? "on" : ""}" type="button" data-action="filter-orders" data-filter="${filter}">${filter}</button>
              `).join("")}
              <span style="margin-left:auto;color:var(--wd-ink-faint);font-size:9.5px">按时间倒序 · ${filtered.length} 条</span>
            </div>
            <section class="wd-paper-panel">
              ${riderOrderRows(filtered, true)}
            </section>
          </main>
          <aside class="wd-user-aside">
            <section class="wd-aside-card dark">
              <h3>骑行小结</h3>
              <p>把散落在校园里的每一程，重新收进这一页。</p>
              <div class="wd-side-list">
                <div class="wd-side-row"><i></i>累计骑行 ${home.stats.totalRides || 0} 次</div>
                <div class="wd-side-row"><i></i>累计消费 ${UI.money(home.stats.totalSpent || 0)}</div>
                <div class="wd-side-row"><i></i>待支付 ${home.stats.unpaidCount || 0} 笔</div>
              </div>
            </section>
            <section class="wd-aside-card">
              <h3>支付说明</h3>
              <p>结束骑行后按分钟计费，支付成功后自动生成账单。余额不足时，可以先到“余额账单”充值。</p>
              <button class="wd-btn wd-btn-primary" style="width:100%;min-height:36px;margin-top:14px" data-nav="wallet" type="button">去充值</button>
            </section>
          </aside>
        </div>
      </div>
    `;
  }

  async function loadUserWallet() {
    const home = await UI.request(`/api/dashboard/user/${encodeURIComponent(session.id)}/home`);
    state.userHome = home;
    updateUserHeader(home.user);
    viewRoot.innerHTML = `
      ${riderBanner(
        "WALLET · 余额与账单",
        "把每一程，都记得清清楚楚。",
        "余额用于骑行结束后自动结算。每一次支付都会生成一份独立账单，可在这里逐笔回看。",
        "",
        ""
      )}
      <div class="wd-user-body">
        <div class="wd-user-layout">
          <main class="wd-user-main">
            <section class="wd-paper-panel">
              <div class="wd-section-title-row" style="margin-bottom:15px">
                <h2>充值 <small>TOP UP</small></h2>
                <span style="color:var(--wd-ink-faint);font-size:9.5px">即时到账</span>
              </div>
              <form id="recharge-form" class="wd-rider-form">
                <div class="wd-rider-field span-2">
                  <label for="recharge-amount">充值金额</label>
                  <input id="recharge-amount" name="amount" type="number" min="0.01" max="1000" step="0.01" placeholder="请输入充值金额" required>
                </div>
                <div class="wd-rider-field span-2" style="flex-direction:row;flex-wrap:wrap;align-items:center">
                  <button class="wd-rider-submit" style="padding:0 24px" type="submit">确认充值</button>
                  ${[20, 50, 100].map((amount) => `<button class="wd-rider-tab" type="button" data-action="quick-recharge" data-amount="${amount}">¥${amount}</button>`).join("")}
                </div>
              </form>
            </section>

            <section class="wd-paper-panel" style="margin-top:22px">
              <div class="wd-section-title-row">
                <h2>消费账单 <small>RECEIPTS</small></h2>
                <span style="color:var(--wd-ink-faint);font-size:9.5px">${home.bills ? home.bills.length : 0} 笔</span>
              </div>
              ${home.bills && home.bills.length ? `
                <div class="wd-receipt-list">
                  ${home.bills.map((bill) => `
                    <div class="wd-receipt-row">
                      <div>
                        <b>${UI.escapeHtml(bill.reason || "骑行费用")}</b>
                        <p>${UI.escapeHtml(bill.orderId || "--")} · ${UI.escapeHtml(bill.payMethod || "余额")} · ${UI.fmtDateTime(bill.payTime)}</p>
                      </div>
                      <div class="wd-receipt-amount">${UI.money(bill.amount)}<span>${UI.escapeHtml(bill.billId)}</span></div>
                    </div>
                  `).join("")}
                </div>
              ` : `<div class="wd-empty-rider">还没有消费账单。</div>`}
            </section>
          </main>
          <aside class="wd-user-aside">
            <section class="wd-active-ride" style="padding:25px">
              <em>AVAILABLE BALANCE</em>
              <h3 style="font-size:35px;margin-top:13px">${UI.money(home.user.balance)}</h3>
              <p>账户 ${UI.escapeHtml(home.user.userId)} · 信用 ${UI.escapeHtml(home.user.credit || "未标记")}</p>
            </section>
            <section class="wd-aside-card">
              <h3>计费方式</h3>
              <div class="wd-side-list">
                <div class="wd-side-row"><i></i>前 15 分钟 1.50 元</div>
                <div class="wd-side-row"><i></i>之后每 15 分钟加收 0.50 元</div>
                <div class="wd-side-row"><i></i>违规停车加收 1.50 元</div>
              </div>
            </section>
          </aside>
        </div>
      </div>
    `;
  }

  async function loadUserRepair() {
    const home = await UI.request(`/api/dashboard/user/${encodeURIComponent(session.id)}/home`);
    state.userHome = home;
    updateUserHeader(home.user);
    viewRoot.innerHTML = `
      ${riderBanner(
        "SERVICE · 车辆报修",
        "让下一程，更顺一点。",
        "发现车锁、刹车、车筐或清洁问题，可以在这里告诉运维人员。报修后，故障车辆会暂停被其他同学借走。",
        "",
        ""
      )}
      <div class="wd-user-body">
        <div class="wd-scan-layout">
          <section class="wd-paper-panel">
            <h2>告诉我们，哪里不舒服</h2>
            <p>车辆编号可以在车身二维码下方找到。描述得越具体，现场处理越快。</p>
            <form id="repair-form" class="wd-rider-form">
              <div class="wd-rider-field">
                <label for="repair-bike">车辆编号</label>
                <input id="repair-bike" name="bikeId" maxlength="8" placeholder="例如：00000001" required>
              </div>
              <div class="wd-rider-field">
                <label for="repair-type">问题类型</label>
                <select id="repair-type" name="workType">
                  <option value="维修">车辆故障</option>
                  <option value="清洁">车辆脏污</option>
                  <option value="调度">停放与调度</option>
                </select>
              </div>
              <div class="wd-rider-field span-2">
                <label for="repair-desc">问题描述</label>
                <textarea id="repair-desc" name="desc" maxlength="200" placeholder="例如：后刹车有些松，骑行时会响。"></textarea>
              </div>
              <div class="wd-rider-field span-2">
                <button class="wd-rider-submit" type="submit">提交报修 · 通知运维</button>
              </div>
            </form>
          </section>
          <aside class="wd-user-aside" style="position:static">
            <section class="wd-aside-card dark">
              <h3>报修之后</h3>
              <p>你在校园里多留意的一眼，会让下一程更安心。</p>
              <div class="wd-side-list">
                <div class="wd-side-row"><i></i>维修车辆会立即暂停租用</div>
                <div class="wd-side-row"><i></i>运维接单后开始现场处理</div>
                <div class="wd-side-row"><i></i>工单完成后车辆恢复可用</div>
              </div>
            </section>
            <section class="wd-aside-card">
              <h3>也可以这样做</h3>
              <p>如果车辆只是轻微脏污，可以选择“清洁”；如果影响骑行安全，请选择“车辆故障”。</p>
            </section>
          </aside>
        </div>
      </div>
    `;
  }

  async function loadUserCredit() {
    const home = await UI.request(`/api/dashboard/user/${encodeURIComponent(session.id)}/home`);
    state.userHome = home;
    updateUserHeader(home.user);
    const events = home.creditEvents || [];
    viewRoot.innerHTML = `
      ${riderBanner(
        "CREDIT · 信用记录",
        "把车停好，是下一次相遇的开始。",
        "规范停车不仅关系到这次骑行的费用，也会同步记录到账户信用。愿每一次还车，都为下一位同学留出方便。",
        "",
        ""
      )}
      <div class="wd-user-body">
        <div class="wd-user-layout">
          <main class="wd-user-main">
            <section class="wd-paper-panel">
              <div class="wd-section-title-row">
                <h2>信用事件 <small>CREDIT EVENTS</small></h2>
                <span class="wd-text-link">${events.length} 条记录</span>
              </div>
              ${events.length ? `
                <div>
                  ${events.map((event) => `
                    <div class="wd-credit-event">
                      <span class="wd-credit-event-icon">${UI.icon("alert")}</span>
                      <div>
                        <b>${UI.escapeHtml(event.type || "违规停车")} · ${UI.escapeHtml(event.orderId)}</b>
                        <span>${UI.escapeHtml(event.location || "未知位置")} · ${UI.escapeHtml(event.impact || "信用标记")}</span>
                      </div>
                      <time>${UI.fmtDateTime(event.time)}</time>
                    </div>
                  `).join("")}
                </div>
              ` : `
                <div class="wd-empty-rider">
                  <div style="color:var(--wd-indigo);margin-bottom:10px">${UI.icon("check", "icon-lg")}</div>
                  目前没有信用违规记录。继续保持，把车停进推荐区域。
                </div>
              `}
            </section>
          </main>
          <aside class="wd-user-aside">
            <section class="wd-active-ride" style="padding:25px">
              <em>CREDIT STATUS</em>
              <h3>${UI.escapeHtml(home.user.credit || "未标记")}</h3>
              <p>账户 ${UI.escapeHtml(home.user.userId)} · 状态 ${UI.escapeHtml(home.user.status || "正常")}</p>
            </section>
            <section class="wd-aside-card">
              <h3>信用小规则</h3>
              <div class="wd-side-list">
                <div class="wd-side-row"><i></i>推荐区停车，保持正常信用</div>
                <div class="wd-side-row"><i></i>违规停车额外收取 1.50 元</div>
                <div class="wd-side-row"><i></i>连续规范骑行后可由管理员复核</div>
              </div>
            </section>
          </aside>
        </div>
      </div>
    `;
  }

  async function loadAdminOverview() {
    const [data, dispatch] = await Promise.all([
      UI.request("/api/dashboard/overview"),
      UI.request("/api/dispatch/overview")
    ]);
    state.dashboard = data;
    state.dispatch = dispatch;
    const kpis = data.kpis || {};
    viewRoot.innerHTML = `
      ${pageHead("运营总览", "把订单、车辆、工单与安全记录，落到一张会流动的校园车队线路图上。", `
        <button class="btn btn-outline" data-action="refresh-view" type="button">${UI.icon("refresh")} 刷新车队</button>
        <button class="btn btn-primary" data-nav="dispatch" type="button">${UI.icon("sparkles")} 调整线路</button>
      `)}
      <section class="ops-cockpit">
        <div class="ops-cockpit-copy">
          <em>LIVE FLEET · 校园车队正在流动</em>
          <h2>今天有 ${kpis.todayOrders || 0} 段校园路，<br>正被 ${kpis.availableBikes || 0} 辆车接住。</h2>
          <p>从宿舍区早高峰，到图书馆闭馆后的晚归。运营不是看静态数字，而是让车辆在正确的时间出现在正确的位置。</p>
          <div class="ops-numbers">
            <div><span>可借车辆</span><b>${kpis.availableBikes || 0}</b><small>辆</small></div>
            <div><span>正在骑行</span><b>${kpis.inUseBikes || 0}</b><small>辆</small></div>
            <div><span>今日收入</span><b>${Number(kpis.todayRevenue || 0).toFixed(0)}</b><small>元</small></div>
            <div><span>待办任务</span><b>${(kpis.pendingWorks || 0) + (kpis.unhandledViolations || 0)}</b><small>项</small></div>
          </div>
          <div class="welcome-actions">
            <button class="btn btn-primary" data-nav="dispatch" type="button">${UI.icon("sparkles")} 查看调度线路</button>
            <button class="btn btn-outline" data-nav="maintenance" type="button">${UI.icon("wrench")} 进入现场接力</button>
          </div>
        </div>
        ${opsMap(dispatch.zones || [])}
      </section>

      ${opsRelay(kpis, dispatch)}

      <div class="dashboard-grid">
        <section class="panel">
          <div class="panel-head"><div><h3>骑行潮汐</h3><p>近七日订单节奏</p></div><span class="chip chip-blue">7 DAYS</span></div>
          ${lineChart(data.orderTrend || [], "orders")}
        </section>
        <section class="panel">
          <div class="panel-head"><div><h3>车队编组</h3><p>可用、骑行、故障与调度中的车辆分布</p></div><span class="chip">${kpis.totalBikes || 0} 辆</span></div>
          <div class="panel-body">${fleetLanes(data.bikeStatus || [])}</div>
        </section>
      </div>
      <div class="dashboard-grid" style="margin-top:15px">
        <section class="panel">
          <div class="panel-head">
            <div><h3>运营接力清单</h3><p>从需求识别到现场闭环</p></div>
            <button class="btn btn-sm btn-ghost" data-nav="maintenance" type="button">进入工单 ${UI.icon("arrow-right", "icon-sm")}</button>
          </div>
          <div class="panel-body" style="padding-top:8px">
            ${(dispatch.items || []).map((item) => `
              <div class="ops-action-row">
                <span class="ops-action-icon">${UI.icon("sparkles")}</span>
                <div><b>${UI.escapeHtml(item.fromArea)} → ${UI.escapeHtml(item.toArea)}</b><small>${UI.escapeHtml(item.reason)}</small></div>
                <button class="btn btn-sm btn-primary" data-nav="dispatch" type="button">调配 ${item.bikeCount} 辆</button>
              </div>
            `).join("") || `<div class="notice" style="margin:0">${UI.icon("check")}<div>当前区域供需平稳，没有待执行的跨区调配。</div></div>`}
            ${(data.recentWorks || []).filter((work) => work.handleStatus !== "已完成").slice(0, 3).map((work) => `
              <div class="ops-action-row">
                <span class="ops-action-icon amber">${UI.icon("wrench")}</span>
                <div><b>${UI.escapeHtml(work.workType)} · 车辆 ${UI.escapeHtml(work.bikeId || "--")}</b><small>${UI.escapeHtml(work.desc || "--")}</small></div>
                <button class="btn btn-sm btn-outline" data-nav="maintenance" type="button">处理</button>
              </div>
            `).join("")}
            ${(data.recentViolations || []).filter((record) => record.handleStatus === "未处理").slice(0, 2).map((record) => `
              <div class="ops-action-row">
                <span class="ops-action-icon red">${UI.icon("shield")}</span>
                <div><b>安全记录 · ${UI.escapeHtml(record.violationBikeId)}</b><small>${UI.escapeHtml(record.violationPosition)}</small></div>
                <button class="btn btn-sm btn-outline" data-nav="security" type="button">核实</button>
              </div>
            `).join("")}
          </div>
        </section>
        <section class="panel">
          <div class="panel-head">
            <div><h3>最新骑行轨迹</h3><p>刚刚发生的校园移动</p></div>
            <button class="btn btn-sm btn-ghost" data-nav="orders" type="button">全部订单 ${UI.icon("arrow-right", "icon-sm")}</button>
          </div>
          ${ordersTable(data.recentOrders || [], { showUser: true })}
        </section>
      </div>
    `;
  }

  async function loadDispatch() {
    const [dispatch, plans] = await Promise.all([
      UI.request("/api/dispatch/overview"),
      UI.request("/api/dispatch/plans")
    ]);
    state.dispatch = dispatch;
    state.plans = plans;
    const items = dispatch.items || [];
    viewRoot.innerHTML = `
      ${pageHead("智能调度", "从真实骑行订单中识别区域需求，再结合可用车辆生成可执行方案。", `
        <button class="btn btn-outline" data-action="refresh-view" type="button">${UI.icon("refresh")} 刷新模型</button>
        <button class="btn btn-primary" data-action="generate-plan" type="button">${UI.icon("sparkles")} 生成调度方案</button>
      `)}
      <section class="dispatch-hero">
        <div class="dispatch-hero-content">
          <div>
            <div class="eyebrow">QINGQI-DISPATCH-1.0</div>
            <h1>让车辆提前去往需求发生的地方</h1>
            <p>模型汇总近 ${dispatch.windowDays} 天 ${dispatch.sampleOrders} 笔真实订单，按起点区域与时间衰减权重计算需求，再与当前可用车辆做供需差。样本覆盖 ${dispatch.observedDays} 个自然日，建议只作为运营决策辅助。</p>
            <div class="welcome-actions">
              <button class="btn btn-primary" data-action="generate-plan" type="button">${UI.icon("sparkles")} 生成本次方案</button>
              <button class="btn btn-outline" data-nav="fleet" type="button">${UI.icon("bike")} 查看车辆库存</button>
            </div>
          </div>
          <div class="dispatch-score">
            <div class="score-ring"><b>${Number(dispatch.confidence || 0).toFixed(0)}</b></div>
            <div class="score-copy">
              <span>MODEL CONFIDENCE</span>
              <b>置信度 ${Number(dispatch.confidence || 0).toFixed(1)}%</b>
              <span style="margin-top:5px">${dispatch.staleFallback ? "使用最近可用历史样本" : "使用近期订单样本"}</span>
            </div>
          </div>
        </div>
      </section>
      <div class="dashboard-grid">
        <section class="panel">
          <div class="panel-head"><div><h3>区域供需画像</h3><p>蓝色为加权日均需求，绿色为可用车辆</p></div><span class="chip chip-blue">${dispatch.sampleOrders} 条样本</span></div>
          <div class="panel-body">${statusBars(dispatch.zones || [])}</div>
        </section>
        <section class="panel">
          <div class="panel-head"><div><h3>模型方法</h3><p>算法可解释、结果可复现</p></div></div>
          <div class="panel-body">
            <div class="method-grid" style="grid-template-columns:1fr 1fr">
              ${(dispatch.methodology || []).map((text, index) => `
                <div class="method-step"><span>STEP ${String(index + 1).padStart(2, "0")}</span><p>${UI.escapeHtml(text)}</p></div>
              `).join("")}
            </div>
          </div>
        </section>
      </div>
      <div class="dashboard-grid" style="margin-top:15px">
        <section class="panel">
          <div class="panel-head"><div><h3>本次建议</h3><p>按缺口从高到低匹配车辆</p></div><span class="chip ${items.length ? "chip-amber" : "chip-green"}">${items.length ? `${items.length} 条建议` : "供需平衡"}</span></div>
          <div class="panel-body">
            ${items.length ? items.map((item) => `
              <div class="dispatch-item" style="margin-bottom:10px">
                <div>
                  <div class="dispatch-route">${UI.escapeHtml(item.fromArea)} <i></i> ${UI.escapeHtml(item.toArea)}</div>
                  <p>${UI.escapeHtml(item.reason)}</p>
                  <div class="plan-meta"><span>优先级 ${UI.statusPill(item.priority)}</span><span>目标缺口 ${item.demandGap}</span></div>
                </div>
                <div class="dispatch-item-count"><b>${item.bikeCount}</b><span>辆建议调配</span></div>
              </div>
            `).join("") : `
              <div class="notice">${UI.icon("check")}<div>当前各区域供需相对平衡，没有需要立即执行的跨区调配建议。可以等待新的骑行数据进入后再次评估。</div></div>
            `}
          </div>
        </section>
        <section class="panel">
          <div class="panel-head"><div><h3>历史方案</h3><p>方案落库后可执行并生成调度工单</p></div><span class="chip">${plans.length} 个</span></div>
          <div class="panel-body">
            ${plans.length ? plans.slice(0, 6).map((plan) => `
              <div class="plan-card">
                <div class="plan-head">
                  <b>${UI.escapeHtml(plan.planId)}</b>
                  ${UI.statusPill(plan.status)}
                </div>
                <p>${UI.escapeHtml(plan.summary)}</p>
                <div class="plan-meta">
                  <span>${UI.fmtDateTime(plan.createdAt)}</span>
                  <span>置信度 ${Number(plan.confidence || 0).toFixed(1)}%</span>
                  <span>${(plan.items || []).length} 条调配</span>
                </div>
                ${plan.status === "待执行" ? `<button class="btn btn-sm btn-primary" style="margin-top:11px" data-action="apply-plan" data-id="${UI.escapeHtml(plan.planId)}" type="button">执行方案</button>` : ""}
              </div>
            `).join("") : emptyView("还没有历史调度方案", "sparkles")}
          </div>
        </section>
      </div>
    `;
  }

  async function loadFleet(kind) {
    const bikes = await UI.request("/api/bikes");
    state.bikes = bikes;
    const statuses = ["全部", "可用", "使用中", "故障", "调度中"];
    const filtered = state.fleetFilter === "全部" ? bikes : bikes.filter((bike) => bike.bikeStatus === state.fleetFilter);
    viewRoot.innerHTML = `
      ${pageHead(kind === "operator" ? "车辆状态" : "车辆资产", "查看车辆运营状态、健康状态与区域归属。", `
        <button class="btn btn-outline" data-action="refresh-view" type="button">${UI.icon("refresh")} 刷新库存</button>
      `)}
      <div class="kpi-grid">
        ${kpi("在册车辆", bikes.length, "辆", "bike", "车辆资产总数", "")}
        ${kpi("可用车辆", bikes.filter((bike) => bike.bikeStatus === "可用").length, "辆", "check", "可以立即扫码", "background:#e3f3ea;color:#0f8151")}
        ${kpi("使用中", bikes.filter((bike) => bike.bikeStatus === "使用中").length, "辆", "route", "当前骑行占用", "background:#dff2e7;color:#1f9b68")}
        ${kpi("故障车辆", bikes.filter((bike) => bike.bikeStatus === "故障" || bike.healthStatus === "损坏").length, "辆", "alert", "需要运维介入", "background:#f8e9e4;color:#b65a45")}
      </div>
      <section class="panel">
        <div class="panel-head">
          <div><h3>车辆清单</h3><p>${kind === "operator" ? "现场运维视角" : "运营资产视角"}</p></div>
          <div class="filter-row" style="margin:0">
            ${statuses.map((status) => `<button class="chip ${state.fleetFilter === status ? "chip-blue" : ""}" type="button" data-action="filter-fleet" data-filter="${status}">${status}</button>`).join("")}
          </div>
        </div>
        ${bikesTable(filtered, { actions: true })}
      </section>
    `;
  }

  async function loadAdminOrders() {
    const orders = await UI.request("/api/orders");
    state.orders = orders;
    viewRoot.innerHTML = `
      ${pageHead("骑行订单", "全局订单中心，用于核对用户、车辆、路线与支付状态。", `
        <button class="btn btn-outline" data-action="refresh-view" type="button">${UI.icon("refresh")} 刷新</button>
      `)}
      <div class="kpi-grid">
        ${kpi("订单总量", orders.length, "笔", "route", "全部历史订单", "")}
        ${kpi("待支付", orders.filter((order) => order.payStatus === "未支付").length, "笔", "alert", "需要用户结算", "background:#f8ece7;color:#ad604c")}
        ${kpi("骑行中", orders.filter((order) => !order.endTime).length, "笔", "bike", "实时进行中", "background:#dff2e7;color:#1f9b68")}
        ${kpi("已支付", orders.filter((order) => order.payStatus === "已支付").length, "笔", "check", "已产生账单", "background:#e3f3ea;color:#0f8151")}
      </div>
      <section class="panel">
        <div class="panel-head"><div><h3>订单明细</h3><p>按开始时间倒序</p></div><span class="chip">${orders.length} 条</span></div>
        ${ordersTable(orders.slice().sort((a, b) => new Date(b.startTime || 0) - new Date(a.startTime || 0)), { showUser: true })}
      </section>
    `;
  }

  async function loadAdminUsers() {
    const users = await UI.request("/api/users");
    state.users = users;
    viewRoot.innerHTML = `
      ${pageHead("用户管理", "查看账户余额、信用状态与使用资格。", "")}
      <div class="kpi-grid">
        ${kpi("注册用户", users.length, "人", "users", "平台用户总量", "")}
        ${kpi("正常账户", users.filter((user) => user.status === "正常").length, "人", "check", "可以正常用车", "background:#e3f3ea;color:#0f8151")}
        ${kpi("信用标记", users.filter((user) => user.credit === "标记").length, "人", "alert", "存在违规行为", "background:#f8e9e4;color:#b65a45")}
        ${kpi("冻结账户", users.filter((user) => user.status === "冻结").length, "人", "lock", "暂停用车权限", "background:#f8ece7;color:#ad604c")}
      </div>
      <section class="panel">
        <div class="panel-head"><div><h3>用户主档</h3><p>手机号与账户状态来自真实表</p></div></div>
        <div class="table-wrap">
          <table class="data-table">
            <thead><tr><th>用户编号</th><th>姓名</th><th>手机号</th><th>余额</th><th>信用</th><th>账户状态</th><th>年龄</th></tr></thead>
            <tbody>
              ${users.map((user) => `
                <tr>
                  <td><strong class="mono">${UI.escapeHtml(user.userId)}</strong></td>
                  <td>${UI.escapeHtml(user.userName || "--")}</td>
                  <td class="mono">${UI.escapeHtml(user.phone || "--")}</td>
                  <td>${UI.money(user.balance)}</td>
                  <td>${UI.statusPill(user.credit)}</td>
                  <td>${UI.statusPill(user.status)}</td>
                  <td>${UI.escapeHtml(user.age || "--")}</td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        </div>
      </section>
    `;
  }

  async function loadMaintenance(kind) {
    const works = await UI.request("/api/maintenance");
    state.works = works;
    const filters = ["全部", "待处理", "处理中", "已完成"];
    const filtered = state.workFilter === "全部" ? works : works.filter((work) => work.handleStatus === state.workFilter);
    const pending = works.filter((work) => work.handleStatus !== "已完成").length;
    viewRoot.innerHTML = `
      ${pageHead(kind === "operator" ? "工单中心" : "运维工单", "按状态接收、处理并完成车辆维护任务。完成维修会自动恢复车辆状态。", `
        <button class="btn btn-outline" data-action="refresh-view" type="button">${UI.icon("refresh")} 刷新</button>
      `)}
      <div class="kpi-grid">
        ${kpi("待办工单", pending, "个", "wrench", "含待处理与处理中", "background:#f8ece7;color:#ad604c")}
        ${kpi("待处理", works.filter((work) => work.handleStatus === "待处理").length, "个", "clock", "等待运维接单", "")}
        ${kpi("处理中", works.filter((work) => work.handleStatus === "处理中").length, "个", "play", "已由运维接单", "background:#dff2e7;color:#1f9b68")}
        ${kpi("已完成", works.filter((work) => work.handleStatus === "已完成").length, "个", "check", "车辆状态已回写", "background:#e3f3ea;color:#0f8151")}
      </div>
      <section class="panel">
        <div class="panel-head">
          <div><h3>工单列表</h3><p>按上报时间倒序</p></div>
          <div class="filter-row" style="margin:0">
            ${filters.map((filter) => `<button class="chip ${state.workFilter === filter ? "chip-blue" : ""}" type="button" data-action="filter-works" data-filter="${filter}">${filter}</button>`).join("")}
          </div>
        </div>
        ${workTable(filtered.slice().sort((a, b) => new Date(b.reportTime || 0) - new Date(a.reportTime || 0)), { actions: true })}
      </section>
    `;
  }

  async function loadSecurityRecords(kind) {
    const [records, fences] = await Promise.all([
      UI.request("/api/security-records"),
      UI.request("/api/fence-rules")
    ]);
    state.records = records;
    state.fences = fences;
    const unhandled = records.filter((record) => record.handleStatus === "未处理").length;
    viewRoot.innerHTML = `
      ${pageHead(kind === "admin" ? "安全记录" : "违规记录", "记录违规位置、车辆与处理状态，并维护校园安全治理闭环。", `
        <button class="btn btn-outline" data-action="refresh-view" type="button">${UI.icon("refresh")} 刷新</button>
        <button class="btn btn-primary" data-action="open-report-record" type="button">${UI.icon("plus")} 上报违规</button>
      `)}
      <div class="kpi-grid">
        ${kpi("违规总数", records.length, "条", "shield", "全部历史记录", "")}
        ${kpi("未处理", unhandled, "条", "alert", "需要现场复核", "background:#f8e9e4;color:#b65a45")}
        ${kpi("已处理", records.length - unhandled, "条", "check", "已完成闭环", "background:#e3f3ea;color:#0f8151")}
        ${kpi("围栏区域", fences.length, "个", "map", "禁停与推荐区", "background:#dff2e7;color:#1f9b68")}
      </div>
      <section class="panel">
        <div class="panel-head"><div><h3>违规记录</h3><p>按上报时间倒序</p></div><span class="chip">${records.length} 条</span></div>
        ${recordsTable(records.slice().sort((a, b) => new Date(b.reportTime || 0) - new Date(a.reportTime || 0)), { actions: true })}
      </section>
    `;
  }

  async function loadOperatorOverview() {
    const [dashboard, works, bikes] = await Promise.all([
      UI.request("/api/dashboard/overview"),
      UI.request("/api/maintenance"),
      UI.request("/api/bikes")
    ]);
    state.dashboard = dashboard;
    state.works = works;
    state.bikes = bikes;
    const pending = works.filter((work) => work.handleStatus === "待处理");
    viewRoot.innerHTML = `
      ${pageHead("今日任务", `你好，${session.name}。今天优先处理待接单工单，并关注故障车辆。`, `
        <button class="btn btn-primary" data-nav="works" type="button">${UI.icon("list")} 进入工单中心</button>
      `)}
      <section class="welcome-band">
        <div>
          <div class="eyebrow">FIELD OPERATIONS</div>
          <h2>${pending.length ? `${pending.length} 个工单等待接单` : "当前工单已处理完毕"}</h2>
          <p>系统会把维修、清洁和调度任务分配到统一的工单队列。完成处理后，车辆状态会自动恢复并重新进入可租用库存。</p>
          <div class="welcome-actions">
            <button class="btn btn-primary" data-nav="works" type="button">查看待办</button>
            <button class="btn btn-outline" data-nav="fleet" type="button">${UI.icon("bike")} 车辆状态</button>
          </div>
        </div>
        <div class="welcome-visual"><div class="signal-orb"><span class="orb-dot one"></span><span class="orb-dot two"></span>${UI.icon("wrench")}</div></div>
      </section>
      <div class="kpi-grid">
        ${kpi("待接单", pending.length, "个", "clock", "尚未指派处理人", "background:#f8ece7;color:#ad604c")}
        ${kpi("处理中", works.filter((work) => work.handleStatus === "处理中").length, "个", "play", "正在进行现场处理", "background:#dff2e7;color:#1f9b68")}
        ${kpi("今日完成", works.filter((work) => work.handleStatus === "已完成").length, "个", "check", "已完成并回写车辆", "background:#e3f3ea;color:#0f8151")}
        ${kpi("可用车辆", bikes.filter((bike) => bike.bikeStatus === "可用").length, "辆", "bike", "当前可租库存", "")}
      </div>
      <div class="dashboard-grid">
        <section class="panel">
          <div class="panel-head"><div><h3>待接单工单</h3><p>按上报时间排序</p></div><button class="btn btn-sm btn-ghost" data-nav="works" type="button">全部工单</button></div>
          ${workTable(pending, { actions: true })}
        </section>
        <section class="panel">
          <div class="panel-head"><div><h3>车辆健康</h3><p>故障与脏污需要关注</p></div></div>
          <div class="panel-body" style="padding-top:7px">
            ${bikes.filter((bike) => bike.bikeStatus === "故障" || bike.healthStatus !== "正常").slice(0, 6).map((bike) => `
              <div class="list-row">
                <div class="list-icon ${bike.healthStatus === "损坏" ? "red" : "amber"}">${UI.icon("bike")}</div>
                <div class="list-copy"><b class="mono">${UI.escapeHtml(bike.bikeId)}</b><span>${UI.escapeHtml(bike.area || "未分区")} · ${UI.escapeHtml(bike.healthStatus)}</span></div>
                <div class="list-meta">${UI.statusPill(bike.bikeStatus)}</div>
              </div>
            `).join("") || emptyView("车辆健康状态正常", "check")}
          </div>
        </section>
      </div>
    `;
  }

  async function loadSecurityOverview() {
    const [dashboard, records, fences] = await Promise.all([
      UI.request("/api/dashboard/overview"),
      UI.request("/api/security-records"),
      UI.request("/api/fence-rules")
    ]);
    state.dashboard = dashboard;
    state.records = records;
    state.fences = fences;
    const unhandled = records.filter((record) => record.handleStatus === "未处理");
    viewRoot.innerHTML = `
      ${pageHead("安全概览", `你好，${session.name}。这里是校园车辆停放与安全事件的实时动态。`, `
        <button class="btn btn-primary" data-action="open-report-record" type="button">${UI.icon("plus")} 上报违规</button>
      `)}
      <section class="welcome-band">
        <div>
          <div class="eyebrow">CAMPUS SAFETY</div>
          <h2>${unhandled.length ? `还有 ${unhandled.length} 条违规记录待处理` : "当前安全记录已全部处理"}</h2>
          <p>违规上报会记录车辆、位置与时间。处理完成后保留完整历史，便于后续复盘和责任追踪。</p>
          <div class="welcome-actions">
            <button class="btn btn-primary" data-nav="records" type="button">处理违规记录</button>
            <button class="btn btn-outline" data-nav="zones" type="button">${UI.icon("map")} 查看围栏区域</button>
          </div>
        </div>
        <div class="welcome-visual"><div class="signal-orb"><span class="orb-dot one"></span><span class="orb-dot two"></span>${UI.icon("shield")}</div></div>
      </section>
      <div class="kpi-grid">
        ${kpi("待处理违规", unhandled.length, "条", "alert", "需要现场核实", "background:#f8e9e4;color:#b65a45")}
        ${kpi("今日记录", records.filter((record) => record.reportTime && new Date(record.reportTime).toDateString() === new Date().toDateString()).length, "条", "calendar", "今日新增记录", "")}
        ${kpi("处理完成率", records.length ? `${Math.round(((records.length - unhandled.length) / records.length) * 100)}%` : "100%", "", "check", "安全事件闭环比例", "background:#e3f3ea;color:#0f8151")}
        ${kpi("围栏区域", fences.length, "个", "map", "禁停与推荐区域", "background:#dff2e7;color:#1f9b68")}
      </div>
      <section class="panel">
        <div class="panel-head"><div><h3>最新违规</h3><p>优先处理未闭环记录</p></div><button class="btn btn-sm btn-ghost" data-nav="records" type="button">全部记录</button></div>
        ${recordsTable(records.slice().sort((a, b) => new Date(b.reportTime || 0) - new Date(a.reportTime || 0)).slice(0, 8), { actions: true })}
      </section>
    `;
  }

  async function loadSecurityZones() {
    const [fences, bikes] = await Promise.all([
      UI.request("/api/fence-rules"),
      UI.request("/api/bikes")
    ]);
    state.fences = fences;
    state.bikes = bikes;
    viewRoot.innerHTML = `
      ${pageHead("围栏区域", "查看禁停区与推荐区规则，并结合车辆区域分布开展巡检。", "")}
      <div class="dashboard-grid">
        <section class="panel">
          <div class="panel-head"><div><h3>区域规则</h3><p>GeoRange 来自 FenceRule 表</p></div><span class="chip">${fences.length} 个</span></div>
          <div class="panel-body">
            ${fences.length ? fences.map((fence) => `
              <div class="bike-card" style="margin-bottom:11px">
                <div class="bike-card-top">
                  <div class="bike-symbol">${UI.icon("map")}</div>
                  ${UI.statusPill(fence.fenceType)}
                </div>
                <h4>${UI.escapeHtml(fence.fenceId)}</h4>
                <p class="code-block" style="margin-top:10px;max-height:84px;overflow:auto">${UI.escapeHtml(fence.geoRange || "--")}</p>
              </div>
            `).join("") : emptyView("暂无围栏规则", "map")}
          </div>
        </section>
        <section class="panel">
          <div class="panel-head"><div><h3>车辆区域分布</h3><p>用于判断巡检重点</p></div></div>
          <div class="panel-body" style="padding-top:7px">
            ${Object.entries(bikes.reduce((acc, bike) => {
              const area = bike.area || "未分区";
              acc[area] = (acc[area] || 0) + 1;
              return acc;
            }, {})).map(([area, count]) => `
              <div class="list-row">
                <div class="list-icon">${UI.icon("pin")}</div>
                <div class="list-copy"><b>${UI.escapeHtml(area)}</b><span>车辆区域归属</span></div>
                <div class="list-meta">${count} 辆</div>
              </div>
            `).join("")}
          </div>
        </section>
      </div>
    `;
  }

  function showEndRideModal(orderId) {
    UI.openModal(`
      <div class="modal-head">
        <h3>结束当前骑行</h3>
        <button class="icon-btn" type="button" data-action="close-modal">${UI.icon("x")}</button>
      </div>
      <div class="modal-body">
        <form id="end-ride-form" data-order-id="${UI.escapeHtml(orderId)}">
          <div class="form-grid">
            <div class="field span-2">
              <label for="end-position">结束位置</label>
              <select class="select" id="end-position" name="endPosition" required>
                <option value="宿舍区6号楼">宿舍区6号楼</option>
                <option value="教学区A区">教学区A区</option>
                <option value="图书馆北门">图书馆北门</option>
                <option value="食堂区">食堂区</option>
                <option value="体育馆">体育馆</option>
              </select>
            </div>
            <div class="field span-2">
              <label for="park-status">停车合规性</label>
              <select class="select" id="park-status" name="parkStatus">
                <option value="合规">合规停车</option>
                <option value="违规">违规停车（加收 1.50 元并标记信用）</option>
              </select>
            </div>
          </div>
          <div class="notice" style="margin-top:16px">${UI.icon("info")}<div>前 15 分钟 1.50 元，之后每 15 分钟加收 0.50 元；实际费用由后端根据订单时间计算。</div></div>
          <div class="form-actions">
            <button class="btn btn-primary" type="submit">确认结束并计费</button>
            <button class="btn btn-outline" type="button" data-action="close-modal">取消</button>
          </div>
        </form>
      </div>
    `);
  }

  function showWorkCompleteModal(workId) {
    UI.openModal(`
      <div class="modal-head">
        <h3>完成运维工单</h3>
        <button class="icon-btn" type="button" data-action="close-modal">${UI.icon("x")}</button>
      </div>
      <div class="modal-body">
        <form id="complete-work-form" data-work-id="${UI.escapeHtml(workId)}">
          <div class="field">
            <label for="work-result">处理结果</label>
            <textarea class="textarea" id="work-result" name="result" maxlength="200" required placeholder="例如：已完成刹车调试，车辆恢复正常。"></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" type="submit">${UI.icon("check")} 确认完成</button>
            <button class="btn btn-outline" type="button" data-action="close-modal">取消</button>
          </div>
        </form>
      </div>
    `);
  }

  function showReportRecordModal() {
    UI.openModal(`
      <div class="modal-head">
        <h3>上报违规记录</h3>
        <button class="icon-btn" type="button" data-action="close-modal">${UI.icon("x")}</button>
      </div>
      <div class="modal-body">
        <form id="report-record-form" class="form-grid">
          <div class="field">
            <label for="record-bike">车辆编号</label>
            <input class="input mono" id="record-bike" name="bikeId" maxlength="8" required placeholder="例如：00000005">
          </div>
          <div class="field">
            <label for="record-type">违规类型</label>
            <select class="select" id="record-type" name="violationType">
              <option value="违规停车">违规停车</option>
              <option value="车辆损坏">车辆损坏</option>
              <option value="疑似盗窃">疑似盗窃</option>
              <option value="其他违规">其他违规</option>
            </select>
          </div>
          <div class="field span-2">
            <label for="record-position">违规位置</label>
            <input class="input" id="record-position" name="violationPosition" required placeholder="例如：教学区A区消防通道">
          </div>
          <div class="form-actions span-2">
            <button class="btn btn-primary" type="submit">提交安全记录</button>
            <button class="btn btn-outline" type="button" data-action="close-modal">取消</button>
          </div>
        </form>
      </div>
    `);
  }

  async function startRide(form) {
    const bikeId = form.bikeId.value.trim();
    const startPosition = form.startPosition.value;
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
      await UI.request("/api/ride/start", {
        method: "POST",
        body: { userId: session.id, bikeId, startPosition }
      });
      UI.toast("骑行已开始，车辆状态已锁定", "success");
      navigate("orders", false);
    } catch (error) {
      UI.toast(error.message, "error");
    } finally {
      button.disabled = false;
    }
  }

  async function endRide(form) {
    const orderId = form.dataset.orderId;
    const endPosition = form.endPosition.value;
    const parkStatus = form.parkStatus.value;
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
      const data = await UI.request(
        `/api/orders/${encodeURIComponent(orderId)}/end?endPosition=${encodeURIComponent(endPosition)}&parkStatus=${encodeURIComponent(parkStatus)}`,
        { method: "PUT" }
      );
      UI.closeModal();
      UI.toast(`骑行已结束，本次费用 ${UI.money(data.order.fee)}`, "success");
      await loadView();
    } catch (error) {
      UI.toast(error.message, "error");
    } finally {
      button.disabled = false;
    }
  }

  async function payOrder(orderId) {
    try {
      const data = await UI.request(`/api/orders/${encodeURIComponent(orderId)}/pay`, {
        method: "POST",
        body: { payMethod: "余额" }
      });
      UI.toast(`支付成功，账单 ${data.billId} 已生成`, "success");
      await loadView();
    } catch (error) {
      UI.toast(error.message, "error");
    }
  }

  async function acceptWork(workId) {
    try {
      await UI.request(
        `/api/maintenance/${encodeURIComponent(workId)}/status?status=${encodeURIComponent("处理中")}&result=${encodeURIComponent("已接受任务")}&handlerId=${encodeURIComponent(session.id)}`,
        { method: "PUT" }
      );
      UI.toast("工单已接受", "success");
      await loadView();
    } catch (error) {
      UI.toast(error.message, "error");
    }
  }

  async function completeWork(form) {
    const workId = form.dataset.workId;
    const result = form.result.value.trim();
    try {
      await UI.request(
        `/api/maintenance/${encodeURIComponent(workId)}/status?status=${encodeURIComponent("已完成")}&result=${encodeURIComponent(result)}&handlerId=${encodeURIComponent(session.id)}`,
        { method: "PUT" }
      );
      UI.closeModal();
      UI.toast("工单已完成，车辆状态已回写", "success");
      await loadView();
    } catch (error) {
      UI.toast(error.message, "error");
    }
  }

  async function generatePlan() {
    try {
      await UI.request("/api/dispatch/plans", { method: "POST" });
      UI.toast("调度方案已生成并写入数据库", "success");
      navigate("dispatch");
    } catch (error) {
      UI.toast(error.message, "error");
    }
  }

  async function applyPlan(planId) {
    if (!window.confirm("执行方案会生成调度工单并改变车辆所属区域，确认继续吗？")) return;
    try {
      const data = await UI.request(`/api/dispatch/plans/${encodeURIComponent(planId)}/apply`, {
        method: "POST"
      });
      UI.toast(`方案已执行，共调配 ${data.movedBikes || 0} 辆车`, "success");
      await loadView();
    } catch (error) {
      UI.toast(error.message, "error");
    }
  }

  document.addEventListener("click", async (event) => {
    const nav = event.target.closest("[data-nav]");
    if (nav) {
      navigate(nav.dataset.nav);
      return;
    }
    const actionNode = event.target.closest("[data-action]");
    if (!actionNode) return;
    const action = actionNode.dataset.action;
    const id = actionNode.dataset.id;

    if (action === "logout") {
      UI.logout();
    } else if (action === "close-modal") {
      UI.closeModal();
    } else if (action === "refresh-view") {
      await loadView();
    } else if (action === "use-bike") {
      const input = document.getElementById("ride-bike-id");
      if (input) {
        input.value = id;
        input.focus();
        UI.toast(`已选择车辆 ${id}`, "info");
      }
    } else if (action === "end-order") {
      showEndRideModal(id);
    } else if (action === "pay-order") {
      await payOrder(id);
    } else if (action === "accept-work") {
      await acceptWork(id);
    } else if (action === "complete-work") {
      showWorkCompleteModal(id);
    } else if (action === "filter-works") {
      state.workFilter = actionNode.dataset.filter;
      await loadView();
    } else if (action === "filter-fleet") {
      state.fleetFilter = actionNode.dataset.filter;
      await loadView();
    } else if (action === "filter-orders") {
      state.orderFilter = actionNode.dataset.filter;
      await loadView();
    } else if (action === "quick-recharge") {
      const input = document.getElementById("recharge-amount");
      if (input) input.value = actionNode.dataset.amount;
    } else if (action === "generate-plan") {
      await generatePlan();
    } else if (action === "apply-plan") {
      await applyPlan(id);
    } else if (action === "open-report-record") {
      showReportRecordModal();
    } else if (action === "handle-record") {
      try {
        await UI.request(`/api/security-records/${encodeURIComponent(id)}/handle`, { method: "PUT" });
        UI.toast("违规记录已标记为已处理", "success");
        await loadView();
      } catch (error) {
        UI.toast(error.message, "error");
      }
    } else if (action === "repair-bike") {
      const bike = state.bikes.find((item) => item.bikeId === id);
      if (bike && bike.bikeStatus !== "故障") {
        UI.toast("该车辆当前无需创建维修单", "info");
        return;
      }
      UI.toast("请在工单中心创建维修工单", "info");
      navigate(role === "operator" ? "works" : "maintenance");
    }
  });

  document.addEventListener("submit", async (event) => {
    const form = event.target;
    if (!["ride-form", "end-ride-form", "recharge-form", "repair-form", "complete-work-form", "report-record-form"].includes(form.id)) {
      return;
    }
    event.preventDefault();
    if (form.id === "ride-form") {
      await startRide(form);
    } else if (form.id === "end-ride-form") {
      await endRide(form);
    } else if (form.id === "recharge-form") {
      const amount = Number(form.amount.value);
      const button = form.querySelector('button[type="submit"]');
      button.disabled = true;
      try {
        await UI.request(`/api/users/${encodeURIComponent(session.id)}/recharge`, {
          method: "POST",
          body: { amount }
        });
        UI.toast("充值成功，余额已更新", "success");
        await loadView();
      } catch (error) {
        UI.toast(error.message, "error");
      } finally {
        button.disabled = false;
      }
    } else if (form.id === "repair-form") {
      const button = form.querySelector('button[type="submit"]');
      button.disabled = true;
      try {
        await UI.request("/api/maintenance", {
          method: "POST",
          body: {
            bikeId: form.bikeId.value.trim(),
            reporterId: session.id,
            desc: form.desc.value.trim(),
            workType: form.workType.value
          }
        });
        UI.toast("报修工单已提交，车辆状态已同步", "success");
        form.reset();
      } catch (error) {
        UI.toast(error.message, "error");
      } finally {
        button.disabled = false;
      }
    } else if (form.id === "complete-work-form") {
      await completeWork(form);
    } else if (form.id === "report-record-form") {
      const button = form.querySelector('button[type="submit"]');
      button.disabled = true;
      try {
        await UI.request("/api/security-records", {
          method: "POST",
          body: {
            bikeId: form.bikeId.value.trim(),
            reporterId: session.id,
            violationPosition: form.violationPosition.value.trim(),
            violationType: form.violationType.value
          }
        });
        UI.closeModal();
        UI.toast("违规记录已上报", "success");
        await loadView();
      } catch (error) {
        UI.toast(error.message, "error");
      } finally {
        button.disabled = false;
      }
    }
  });

  document.getElementById("mobile-menu").addEventListener("click", () => {
    shell.classList.toggle("menu-open");
  });
  document.getElementById("refresh-view").addEventListener("click", loadView);
  document.getElementById("top-logout").addEventListener("click", UI.logout);

  renderShell();
  applyIcons(document);
  updateClock();
  window.setInterval(updateClock, 30000);
  loadView();
})();
