(function () {
  const UI = window.BikeUI;

  document.querySelectorAll("[data-icon]").forEach((node) => {
    node.innerHTML = UI.icon(node.getAttribute("data-icon"));
  });
  document.getElementById("footer-year").textContent = new Date().getFullYear();

  function setText(id, value) {
    const node = document.getElementById(id);
    if (node) node.textContent = value == null ? "--" : value;
  }

  async function loadLiveData() {
    try {
      const [overview, bikes] = await Promise.all([
        UI.request("/api/dashboard/overview"),
        UI.request("/api/bikes")
      ]);
      const kpis = overview.kpis || {};
      setText("hero-available", kpis.availableBikes);
      setText("hero-inuse", kpis.inUseBikes);
      setText("hero-orders", kpis.todayOrders);
      document.getElementById("system-status").textContent = "校园车队在线";

      const countArea = (area) => bikes.filter((bike) =>
        bike.bikeStatus === "可用" && (bike.area || "").includes(area)
      ).length;
      setText("route-teaching", `${countArea("教学")} 辆可借`);
      setText("route-library", `${countArea("图书")} 辆可借`);
      setText("route-sports", `${countArea("体育")} 辆可借`);
    } catch (error) {
      document.getElementById("system-status").textContent = "数据暂不可用";
      const dot = document.querySelector(".wd-live i");
      if (dot) dot.style.background = "var(--wd-red)";
    }
  }

  const session = UI.getSession();
  const loginLinks = document.querySelectorAll('a[href="/login.html"]');
  if (session) {
    loginLinks.forEach((link) => {
      link.href = UI.consoleUrl(session.role);
      if (link.classList.contains("wd-login-btn")) {
        link.innerHTML = `继续骑行 ${UI.icon("arrow-right")}`;
      }
    });
  }

  loadLiveData();
})();
