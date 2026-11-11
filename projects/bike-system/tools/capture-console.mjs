import { spawn } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";

const role = process.argv[2] || "admin";
const view = process.argv[3] || "overview";
const output = process.argv[4] || path.resolve(`artifacts/${role}-${view}.png`);
const width = Number(process.argv[5] || 1440);
const height = Number(process.argv[6] || 1000);
const chromePath = "C:\\Users\\caijiayi\\AppData\\Local\\ms-playwright\\chromium-1234\\chrome-win64\\chrome.exe";
const port = 9300 + Math.floor(Math.random() * 300);
const profileDir = fs.mkdtempSync(path.join(os.tmpdir(), "qingqi-chrome-"));

const accounts = {
  user: ["/api/auth/user/login", { userId: "3124000001", phone: "13800138001" }],
  admin: ["/api/auth/admin/login", { adminId: "A000000001", phone: "13900139001" }],
  operator: ["/api/auth/admin/login", { adminId: "O000000001", phone: "13900139002" }],
  security: ["/api/auth/security/login", { securityId: "S000000001", phone: "13900139004" }]
};

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function buildSession() {
  const [endpoint, credentials] = accounts[role];
  const query = new URLSearchParams(credentials);
  const response = await fetch(`http://localhost:8080${endpoint}?${query}`, { method: "POST" });
  if (!response.ok) throw new Error(`login failed: ${response.status}`);
  const data = await response.json();
  if (role === "user") {
    return {
      type: "user",
      role: "user",
      id: data.user.userId,
      name: data.user.userName,
      user: data.user
    };
  }
  if (role === "security") {
    return {
      type: "security",
      role: "security",
      id: data.securityPerson.securityId,
      name: data.securityPerson.secName,
      person: data.securityPerson
    };
  }
  return {
    type: "admin",
    role: data.role,
    id: data.admin.adminId,
    name: data.displayName,
    admin: data.admin
  };
}

async function waitForDebugger() {
  for (let attempt = 0; attempt < 50; attempt++) {
    try {
      const response = await fetch(`http://127.0.0.1:${port}/json/version`);
      if (response.ok) return;
    } catch {
      // Chrome is still starting.
    }
    await sleep(150);
  }
  throw new Error("Chrome DevTools endpoint did not start");
}

async function connect(url) {
  const socket = new WebSocket(url);
  await new Promise((resolve, reject) => {
    socket.addEventListener("open", resolve, { once: true });
    socket.addEventListener("error", reject, { once: true });
  });
  let id = 0;
  const pending = new Map();
  const errors = [];
  socket.addEventListener("message", (event) => {
    const message = JSON.parse(event.data);
    if (message.id && pending.has(message.id)) {
      const { resolve, reject } = pending.get(message.id);
      pending.delete(message.id);
      if (message.error) reject(new Error(message.error.message));
      else resolve(message.result);
    }
    if (message.method === "Runtime.exceptionThrown") {
      errors.push(message.params.exceptionDetails?.text || "runtime exception");
    }
    if (message.method === "Runtime.consoleAPICalled" && message.params.type === "error") {
      errors.push(message.params.args.map((arg) => arg.value || arg.description || "").join(" "));
    }
  });
  function send(method, params) {
    return new Promise((resolve, reject) => {
      const messageId = ++id;
      pending.set(messageId, { resolve, reject });
      socket.send(JSON.stringify({ id: messageId, method, params: params || {} }));
    });
  }
  return { socket, send, errors };
}

async function main() {
  const session = role === "guest" ? null : await buildSession();
  const chrome = spawn(chromePath, [
    "--headless=new",
    "--disable-gpu",
    "--hide-scrollbars",
    "--no-first-run",
    "--no-default-browser-check",
    `--remote-debugging-port=${port}`,
    `--user-data-dir=${profileDir}`,
    `--window-size=${width},${height}`,
    "about:blank"
  ], { stdio: "ignore", windowsHide: true });

  try {
    await waitForDebugger();
    const targetResponse = await fetch(`http://127.0.0.1:${port}/json/new?http://localhost:8080/login.html`, {
      method: "PUT"
    });
    const target = await targetResponse.json();
    const client = await connect(target.webSocketDebuggerUrl);
    const { send, errors } = client;
    await send("Page.enable");
    await send("Runtime.enable");
    await send("Emulation.setDeviceMetricsOverride", {
      width,
      height,
      deviceScaleFactor: 1,
      mobile: false
    });
    await sleep(800);
    if (session) {
      await send("Runtime.evaluate", {
        expression: `localStorage.setItem("qingqi_session", ${JSON.stringify(JSON.stringify(session))})`
      });
    }
    await send("Page.navigate", {
      url: session
        ? `http://localhost:8080/console.html?role=${encodeURIComponent(session.role)}&view=${encodeURIComponent(view)}`
        : "http://localhost:8080/"
    });
    await sleep(2200);
    const title = await send("Runtime.evaluate", {
      expression: "document.title",
      returnByValue: true
    });
    const headerDebug = await send("Runtime.evaluate", {
      expression: `(() => {
        const header = document.querySelector(".wd-console-header");
        const headerNode = header || document.querySelector(".wd-header-right");
        if (!headerNode) return null;
        const rect = headerNode.getBoundingClientRect();
        const style = getComputedStyle(headerNode);
        const login = document.querySelector(".wd-login-btn");
        const loginRect = login?.getBoundingClientRect();
        const actions = document.querySelector(".wd-console-actions");
        const actionsRect = actions?.getBoundingClientRect();
        const logout = document.querySelector(".console-logout");
        const logoutRect = logout?.getBoundingClientRect();
        return {
          text: headerNode.innerText,
          rect: { x: rect.x, y: rect.y, width: rect.width, height: rect.height },
          display: style.display,
          visibility: style.visibility,
          opacity: style.opacity,
          login: loginRect ? { x: loginRect.x, y: loginRect.y, width: loginRect.width, height: loginRect.height } : null,
          actions: actionsRect ? { x: actionsRect.x, y: actionsRect.y, width: actionsRect.width, height: actionsRect.height } : null,
          logout: logoutRect ? { x: logoutRect.x, y: logoutRect.y, width: logoutRect.width, height: logoutRect.height } : null
        };
      })()`,
      returnByValue: true
    });
    const screenshot = await send("Page.captureScreenshot", {
      format: "png",
      fromSurface: true,
      captureBeyondViewport: false
    });
    fs.mkdirSync(path.dirname(output), { recursive: true });
    fs.writeFileSync(output, Buffer.from(screenshot.data, "base64"));
    console.log(JSON.stringify({
      output,
      title: title.result?.value,
      header: headerDebug.result?.value,
      errors
    }, null, 2));
    client.socket.close();
  } finally {
    chrome.kill();
    await sleep(200);
    try {
      fs.rmSync(profileDir, { recursive: true, force: true });
    } catch {
      // Profile cleanup is best-effort.
    }
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
