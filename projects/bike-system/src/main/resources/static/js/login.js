(function () {
  const UI = window.BikeUI;
  const params = new URLSearchParams(window.location.search);
  let role = ["user", "admin", "security"].includes(params.get("role"))
    ? params.get("role")
    : "user";

  const labels = {
    user: {
      account: "用户编号 / 学号",
      placeholder: "例如：3124000001",
      demo: [
        { id: "3124000001", phone: "13800138001", name: "张三" },
        { id: "3124000002", phone: "13800138002", name: "李四" }
      ]
    },
    admin: {
      account: "管理员 / 运维人员编号",
      placeholder: "例如：A000000001",
      demo: [
        { id: "A000000001", phone: "13900139001", name: "系统管理员" },
        { id: "O000000001", phone: "13900139002", name: "王师傅" }
      ]
    },
    security: {
      account: "安保人员编号",
      placeholder: "例如：S000000001",
      demo: [
        { id: "S000000001", phone: "13900139004", name: "张保安" },
        { id: "S000000002", phone: "13900139005", name: "李保安" }
      ]
    }
  };

  const form = document.getElementById("login-form");
  const account = document.getElementById("account");
  const phone = document.getElementById("phone");
  const errorBox = document.getElementById("login-error");
  const submit = document.getElementById("login-submit");
  const accountLabel = document.getElementById("account-label");
  const demoRoot = document.getElementById("demo-accounts");

  function setRole(nextRole) {
    role = nextRole;
    const config = labels[role];
    document.querySelectorAll(".wd-role-tab").forEach((tab) => {
      tab.classList.toggle("active", tab.dataset.role === role);
    });
    accountLabel.textContent = config.account;
    account.placeholder = config.placeholder;
    account.value = config.demo[0].id;
    phone.value = config.demo[0].phone;
    errorBox.classList.remove("show");
    demoRoot.innerHTML = config.demo.map((item) =>
      `<button class="wd-demo-chip" type="button" data-id="${UI.escapeHtml(item.id)}" data-phone="${UI.escapeHtml(item.phone)}">${UI.escapeHtml(item.name)} · ${UI.escapeHtml(item.id)}</button>`
    ).join("");
  }

  document.querySelector(".wd-role-tabs").addEventListener("click", (event) => {
    const tab = event.target.closest(".wd-role-tab");
    if (tab) setRole(tab.dataset.role);
  });

  demoRoot.addEventListener("click", (event) => {
    const chip = event.target.closest(".wd-demo-chip");
    if (!chip) return;
    account.value = chip.dataset.id;
    phone.value = chip.dataset.phone;
    errorBox.classList.remove("show");
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");
    submit.disabled = true;
    submit.textContent = "正在验证身份...";

    try {
      const accountValue = account.value.trim();
      const phoneValue = phone.value.trim();
      if (!accountValue || !phoneValue) {
        throw new Error("请输入完整编号和手机号");
      }

      let path;
      if (role === "user") {
        path = `/api/auth/user/login?userId=${encodeURIComponent(accountValue)}&phone=${encodeURIComponent(phoneValue)}`;
      } else if (role === "admin") {
        path = `/api/auth/admin/login?adminId=${encodeURIComponent(accountValue)}&phone=${encodeURIComponent(phoneValue)}`;
      } else {
        path = `/api/auth/security/login?securityId=${encodeURIComponent(accountValue)}&phone=${encodeURIComponent(phoneValue)}`;
      }

      const data = await UI.request(path, { method: "POST" });
      let session;
      if (role === "user") {
        session = {
          type: "user",
          role: "user",
          id: data.user.userId,
          name: data.user.userName || "校园用户",
          user: data.user,
          loginAt: new Date().toISOString()
        };
      } else if (role === "admin") {
        session = {
          type: "admin",
          role: data.role || "admin",
          id: data.admin.adminId,
          name: data.displayName || data.admin.adminName || "管理员",
          admin: data.admin,
          loginAt: new Date().toISOString()
        };
      } else {
        session = {
          type: "security",
          role: "security",
          id: data.securityPerson.securityId,
          name: data.securityPerson.secName || "安保人员",
          person: data.securityPerson,
          loginAt: new Date().toISOString()
        };
      }
      UI.setSession(session);
      window.location.href = UI.consoleUrl(session.role);
    } catch (error) {
      errorBox.textContent = error.message;
      errorBox.classList.add("show");
      submit.disabled = false;
      submit.textContent = "进入青骑";
    }
  });

  setRole(role);
})();
