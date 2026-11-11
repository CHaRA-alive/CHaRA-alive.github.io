/* =========================================================
   全部动效都是手写的，没有引用任何外部库
   画布上依次画：星星 → 云 → 花瓣 → 微光
   ========================================================= */
(function () {
  "use strict";

  var root = document.documentElement;
  var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ==================== 日间 / 夜空 ==================== */
  var THEME_KEY = "theme";
  var themeBtn = document.getElementById("theme-toggle");
  var themeHooks = [];
  var saved = null;

  try {
    saved = localStorage.getItem(THEME_KEY);
  } catch (e) {
    saved = null;
  }

  function setTheme(theme) {
    root.setAttribute("data-theme", theme);
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch (e) {
      /* 隐私模式下 localStorage 不可用，忽略 */
    }
    themeHooks.forEach(function (fn) {
      fn();
    });
  }

  function isDark() {
    return root.getAttribute("data-theme") === "dark";
  }

  setTheme(saved === "dark" ? "dark" : "light");

  if (themeBtn) {
    themeBtn.addEventListener("click", function () {
      setTheme(isDark() ? "light" : "dark");
    });
  }

  /* ==================== 打字机 ==================== */
  var typedEl = document.getElementById("typed");
  var LINES = [
    "广东工业大学 · 在读学生",
    "在学 Python 与自然语言处理",
    "慢慢来，比较快"
  ];

  if (typedEl) {
    if (reduceMotion) {
      typedEl.textContent = LINES[0];
    } else {
      var lineIndex = 0;
      var charIndex = 0;
      var deleting = false;

      (function step() {
        var line = LINES[lineIndex];
        typedEl.textContent = line.slice(0, charIndex);

        if (!deleting) {
          charIndex++;
          if (charIndex > line.length) {
            deleting = true;
            setTimeout(step, 2000);
            return;
          }
        } else {
          charIndex--;
          if (charIndex === 0) {
            deleting = false;
            lineIndex = (lineIndex + 1) % LINES.length;
          }
        }
        setTimeout(step, deleting ? 55 : 125);
      })();
    }
  }

  /* ==================== 天空场景 ==================== */
  var canvas = document.getElementById("scene");
  var ctx = canvas && canvas.getContext ? canvas.getContext("2d") : null;
  var W = 0;
  var H = 0;
  var time = 0;
  var running = false;
  var clouds = [];
  var stars = [];
  var petals = [];
  var sparks = [];

  // 花瓣配色 [红, 绿, 蓝]
  var PETAL_LIGHT = [
    [244, 168, 192],
    [199, 180, 234],
    [166, 200, 242],
    [167, 221, 208]
  ];
  var PETAL_DARK = [
    [240, 179, 200],
    [206, 189, 238],
    [176, 205, 245],
    [176, 227, 215]
  ];

  function petalPalette() {
    return isDark() ? PETAL_DARK : PETAL_LIGHT;
  }

  // 把一朵云画到离屏 canvas 上（只画一次，之后每帧贴图，省性能）
  function buildCloud(scale) {
    var w = Math.round(330 * scale);
    var h = Math.round(150 * scale);
    var dpr = Math.min(window.devicePixelRatio || 1, 2);
    var off = document.createElement("canvas");
    off.width = Math.round(w * dpr);
    off.height = Math.round(h * dpr);
    var g = off.getContext("2d");
    g.scale(dpr, dpr);

    var rgb = isDark() ? "190,200,240" : "255,255,255";
    var puffs = 13 + Math.floor(Math.random() * 6);

    for (var i = 0; i < puffs; i++) {
      var px = 60 + Math.random() * (w - 120);
      var py = h * 0.6 + (Math.random() - 0.5) * h * 0.2;
      var pr = (26 + Math.random() * 30) * scale;
      var grad = g.createRadialGradient(px, py, 0, px, py, pr);
      var top = isDark() ? 0.3 : 1;
      grad.addColorStop(0, "rgba(" + rgb + "," + top + ")");
      grad.addColorStop(0.5, "rgba(" + rgb + "," + top * 0.45 + ")");
      grad.addColorStop(1, "rgba(" + rgb + ",0)");
      g.fillStyle = grad;
      g.beginPath();
      g.arc(px, py, pr, 0, Math.PI * 2);
      g.fill();
    }

    return { canvas: off, w: w, h: h };
  }

  function seed() {
    // 云
    clouds = [];
    var cloudCount = Math.max(3, Math.min(Math.round(W / 340), 6));
    for (var i = 0; i < cloudCount; i++) {
      var scale = 0.6 + Math.random() * 0.9;
      var cloud = buildCloud(scale);
      clouds.push({
        img: cloud.canvas,
        w: cloud.w,
        h: cloud.h,
        x: Math.random() * (W + 300) - 150,
        y: H * (0.02 + Math.random() * 0.3),
        speed: 0.07 + Math.random() * 0.14,
        alpha: (isDark() ? 0.34 : 0.68) + Math.random() * 0.28
      });
    }

    // 星星（只在夜空显示）
    stars = [];
    for (var s = 0; s < 70; s++) {
      stars.push({
        x: Math.random() * W,
        y: Math.random() * H * 0.72,
        r: Math.random() * 1.1 + 0.35,
        phase: Math.random() * Math.PI * 2,
        speed: 0.5 + Math.random() * 1.4
      });
    }

    // 花瓣
    petals = [];
    var petalCount = Math.max(8, Math.min(Math.round(W / 78), 18));
    for (var p = 0; p < petalCount; p++) {
      petals.push({
        x: Math.random() * W,
        y: Math.random() * H,
        size: 6 + Math.random() * 8,
        speed: 0.16 + Math.random() * 0.42,
        sway: 0.5 + Math.random() * 1.5,
        phase: Math.random() * Math.PI * 2,
        rot: Math.random() * Math.PI * 2,
        spin: (Math.random() - 0.5) * 0.012,
        alpha: 0.34 + Math.random() * 0.34,
        tint: Math.floor(Math.random() * 4)
      });
    }

    // 微光
    sparks = [];
    for (var k = 0; k < 8; k++) {
      sparks.push({
        x: Math.random() * W,
        y: Math.random() * H,
        size: 2.4 + Math.random() * 3,
        phase: Math.random() * Math.PI * 2,
        speed: 0.35 + Math.random() * 0.5
      });
    }
  }

  function resize() {
    var dpr = Math.min(window.devicePixelRatio || 1, 2);
    W = window.innerWidth;
    H = window.innerHeight;
    canvas.width = Math.floor(W * dpr);
    canvas.height = Math.floor(H * dpr);
    canvas.style.width = W + "px";
    canvas.style.height = H + "px";
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    seed();
  }

  function drawSpark(x, y, s, color) {
    ctx.beginPath();
    ctx.moveTo(x, y - s);
    ctx.quadraticCurveTo(x, y, x + s, y);
    ctx.quadraticCurveTo(x, y, x, y + s);
    ctx.quadraticCurveTo(x, y, x - s, y);
    ctx.quadraticCurveTo(x, y, x, y - s);
    ctx.fillStyle = color;
    ctx.fill();
  }

  function draw() {
    if (!running) return;
    var i, p;

    ctx.clearRect(0, 0, W, H);
    time += 0.016;

    // 星星
    if (isDark()) {
      for (i = 0; i < stars.length; i++) {
        p = stars[i];
        var tw = 0.35 + 0.65 * Math.abs(Math.sin(time * p.speed + p.phase));
        ctx.fillStyle = "rgba(255,255,255," + (tw * 0.85).toFixed(3) + ")";
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r * (0.7 + tw * 0.6), 0, Math.PI * 2);
        ctx.fill();
      }
    }

    // 云
    for (i = 0; i < clouds.length; i++) {
      p = clouds[i];
      p.x -= p.speed;
      if (p.x + p.w < -60) {
        p.x = W + 60 + Math.random() * 200;
        p.y = H * (0.02 + Math.random() * 0.3);
      }
      ctx.globalAlpha = p.alpha;
      ctx.drawImage(p.img, p.x, p.y, p.w, p.h);
    }
    ctx.globalAlpha = 1;

    // 花瓣
    var palette = petalPalette();
    for (i = 0; i < petals.length; i++) {
      p = petals[i];
      p.y += p.speed;
      p.x += Math.sin(time * p.sway + p.phase) * 0.42;
      p.rot += p.spin;

      if (p.y - p.size > H) {
        p.y = -p.size * 2;
        p.x = Math.random() * W;
      }
      if (p.x < -40) p.x = W + 30;
      if (p.x > W + 40) p.x = -30;

      var c = palette[p.tint];
      var alpha = p.alpha * (0.75 + 0.25 * Math.sin(time * 0.8 + p.phase));

      ctx.save();
      ctx.translate(p.x, p.y);
      ctx.rotate(p.rot);
      ctx.beginPath();
      ctx.ellipse(0, 0, p.size * 0.6, p.size, 0, 0, Math.PI * 2);
      ctx.fillStyle =
        "rgba(" + c[0] + "," + c[1] + "," + c[2] + "," + alpha.toFixed(3) + ")";
      ctx.fill();
      ctx.restore();
    }

    // 微光
    for (i = 0; i < sparks.length; i++) {
      p = sparks[i];
      var twinkle = 0.5 + 0.5 * Math.sin(time * p.speed + p.phase);
      p.y -= 0.08;
      if (p.y < -12) p.y = H + 12;
      drawSpark(
        p.x,
        p.y,
        p.size * (0.6 + twinkle * 0.6),
        "rgba(255,255,255," + (0.6 * twinkle).toFixed(3) + ")"
      );
    }

    requestAnimationFrame(draw);
  }

  if (ctx && !reduceMotion) {
    resize();
    running = true;
    requestAnimationFrame(draw);

    window.addEventListener("resize", resize);
    document.addEventListener("visibilitychange", function () {
      if (document.hidden) {
        running = false;
      } else if (!running) {
        running = true;
        requestAnimationFrame(draw);
      }
    });

    // 换主题时重新配色云朵（保留位置）
    themeHooks.push(function () {
      clouds.forEach(function (cloud) {
        var rebuilt = buildCloud(cloud.w / 380);
        cloud.img = rebuilt.canvas;
        cloud.w = rebuilt.w;
        cloud.h = rebuilt.h;
      });
    });
  } else if (canvas) {
    canvas.style.display = "none";
  }

  /* ==================== 滚动出现 + 数字滚动 ==================== */
  var revealEls = document.querySelectorAll(".reveal");

  function countUp(el) {
    if (el.dataset.done) return;
    el.dataset.done = "1";
    var target = parseInt(el.getAttribute("data-count"), 10) || 0;
    var start = null;
    var duration = 1100;

    function tick(now) {
      if (start === null) start = now;
      var progress = Math.min((now - start) / duration, 1);
      var eased = 1 - Math.pow(1 - progress, 3);
      el.textContent = Math.round(target * eased);
      if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
  }

  if ("IntersectionObserver" in window && !reduceMotion) {
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (!entry.isIntersecting) return;
          entry.target.classList.add("in");
          observer.unobserve(entry.target);
          entry.target.querySelectorAll(".count").forEach(countUp);
        });
      },
      { threshold: 0.15, rootMargin: "0px 0px -50px 0px" }
    );

    revealEls.forEach(function (el) {
      observer.observe(el);
    });
  } else {
    revealEls.forEach(function (el) {
      el.classList.add("in");
    });
    document.querySelectorAll(".count").forEach(function (el) {
      el.textContent = el.getAttribute("data-count");
    });
  }

  /* ==================== 导航 / 回到顶部 ==================== */
  var nav = document.getElementById("nav");
  var toTop = document.getElementById("to-top");
  var navLinks = Array.prototype.slice.call(document.querySelectorAll(".nav-link"));
  var sections = navLinks
    .map(function (link) {
      return document.querySelector(link.getAttribute("href"));
    })
    .filter(Boolean);
  var ticking = false;

  function onScroll() {
    var y = window.pageYOffset || document.documentElement.scrollTop;

    if (nav) nav.classList.toggle("scrolled", y > 20);
    if (toTop) toTop.classList.toggle("show", y > 500);

    var current = -1;
    sections.forEach(function (section, index) {
      if (section.getBoundingClientRect().top <= window.innerHeight * 0.4) {
        current = index;
      }
    });
    navLinks.forEach(function (link, index) {
      link.classList.toggle("active", index === current);
    });

    ticking = false;
  }

  window.addEventListener(
    "scroll",
    function () {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(onScroll);
    },
    { passive: true }
  );
  onScroll();

  if (toTop) {
    toTop.addEventListener("click", function () {
      window.scrollTo({ top: 0, behavior: reduceMotion ? "auto" : "smooth" });
    });
  }

  /* ==================== 页脚年份 ==================== */
  var year = document.getElementById("year");
  if (year) year.textContent = new Date().getFullYear();
})();
