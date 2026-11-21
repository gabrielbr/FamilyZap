// node shoot.js <scene> <w> <h> <out.png> [extra query]
const { chromium } = require('playwright') // any Playwright with Chromium;
(async () => {
  const [scene, w, h, out, extra = ''] = process.argv.slice(2);
  const b = await chromium.launch({ args: ['--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: +w, height: +h } });
  p.on('console', (m) => console.log('console:', m.text()));
  p.on('pageerror', (e) => console.log('error:', e.message));
  await p.goto(`http://127.0.0.1:8765/render.html?scene=${scene}&w=${w}&h=${h}${extra}`);
  await p.waitForFunction(() => window.__done === true, null, { timeout: 240000 });
  await p.locator('canvas').screenshot({ path: out, omitBackground: true });
  await b.close();
})();
