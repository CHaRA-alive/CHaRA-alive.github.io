# 个人主页（GitHub Pages 版）

纯静态的单页个人主页。不需要服务器、不需要构建工具、不加载任何外部库，
双击 `index.html` 就能看。全部动效都是手写的原生 JavaScript 和 CSS。

风格是清淡的二次元天空：浅粉浅蓝的天空、缓慢飘动的云、落下的花瓣与微光，
内容放在磨砂玻璃面板上。

## 文件说明

| 文件 | 作用 |
| --- | --- |
| `index.html` | 页面结构和全部文案 |
| `style.css` | 配色、排版、毛玻璃、动画关键帧 |
| `script.js` | 天空场景（云 / 花瓣 / 星星 / 微光）、打字机、滚动出现、日夜间切换 |

## ★ 换成你自己的二次元背景图

现在背景是用代码画的天空。想换成一张真实的插画，两步：

1. 把图片放进本文件夹，命名成 `background.jpg`（建议 1920×1080 以上）。
2. 打开 `style.css`，找到最上面这一行，把 `none` 换成图片地址：

   ```css
   --hero-image: url("background.jpg");
   ```

毛玻璃会自动把这张图磨掉一部分 —— 想更糊就调大 `style.css` 里 `.glass`
的 `blur(12px)`，想更透就调小 `--glass` 那一行的透明度。

> 注意：背景图请用你自己画的、买的或明确免费可商用的素材。

## 页面里有哪些动效

- **天空场景**：canvas 上同时画云、花瓣、微光；夜空主题还会多一层星星。
  云用离屏 canvas 预渲染，不会拖慢滚动。
- **毛玻璃面板**：`backdrop-filter` 磨砂，边框是高光描边，鼠标移上去会浮起来。
- **打字机**：首屏副标题一行行打字、删除、换下一行。
- **滚动出现**：每个区块滚进视口时淡入上移；统计数字从 0 滚到目标值。
- **日夜切换**：右上角按钮。日间是浅粉浅蓝的天空，夜间是深紫的星空，
  选择会记在浏览器里。

觉得动效太多，删掉 `script.js` 里对应的一段即可；把系统设置里的「减少动态效果」
打开，页面也会自动安静下来。

## 要改的地方

打开 `index.html`，带注释的位置都是要替换的：

1. `<title>` 和 `<meta name="description">` —— 标签页标题和搜索描述。
2. 首屏头像：现在是引用 GitHub 头像。想换成自己的图，把 `src` 改成 `avatar.jpg`，
   再把图片放进同一个文件夹。
3. 首屏名字、`hero-hi` 那行问候语。
4. 「关于我」三段文字。
5. 「技能」标签（改 `<li>` 内容，多加少加都行）。
6. 「项目」三张卡片：标题、说明、链接。侧边小圆点用 `dot-1 / dot-2 / dot-3`
   控制颜色。
7. 「联系我」的邮箱和链接。

打字机那几行文字在 `script.js` 顶部的 `LINES` 数组里改。

## 换配色

`style.css` 最上面的 `:root` 是日间，`[data-theme="dark"]` 是夜间。
主要改这几个：

```css
--sky-1 / --sky-2 / --sky-3   /* 天空从上到下的三段颜色 */
--pink  --blue  --lav  --mint /* 四个粉彩点缀色 */
--ink                          /* 正文颜色 */
--glass                        /* 毛玻璃的白度 / 透明度 */
```

花瓣颜色在 `script.js` 的 `PETAL_LIGHT` / `PETAL_DARK` 里。

## 本地预览

直接双击 `index.html` 就行。想用本地服务器（更接近线上环境）：

```powershell
npx --yes http-server -p 8124
```

然后访问 http://127.0.0.1:8124

## 部署到 GitHub Pages

1. 在 GitHub 新建仓库，**仓库名必须是 `<你的用户名>.github.io`**。
   你的用户名是 `CHaRA-alive`，所以仓库名就是 `CHaRA-alive.github.io`。
2. 在本文件夹执行：

   ```powershell
   git init
   git add .
   git commit -m "Add personal homepage"
   git branch -M main
   git remote add origin https://github.com/CHaRA-alive/CHaRA-alive.github.io.git
   git push -u origin main
   ```

3. 等 1～2 分钟，访问 https://chara-alive.github.io 就能看到了。

如果打开是 404：去仓库 **Settings → Pages**，确认 Source 是 `Deploy from a branch`，
分支选 `main`、目录选 `/ (root)`，保存后再等一两分钟。

## 补一个 GitHub 个人主页（可选，和上面是两件事）

`github.com/CHaRA-alive` 首页那块想好看，需要新建一个名字也叫 `CHaRA-alive`
的仓库，在里面放 `README.md`，内容会显示在主页上。需要的话我可以另外做一份。

## 绑定自己的域名（可选）

在仓库 Settings → Pages → Custom domain 填域名，然后到域名服务商加一条 CNAME
记录指向 `chara-alive.github.io`。HTTPS 证书 GitHub 会自动签发。
