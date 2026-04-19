const { chromium, devices } = require('playwright');
const iPhone = devices['iPhone 13'];

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext({ ...iPhone });
  const page = await context.newPage();
  
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  
  await page.goto('http://localhost:3001/#/pages/drift-bottle/index', { waitUntil: 'networkidle' });
  await page.waitForTimeout(2000);
  
  const dims = await page.evaluate(() => {
    const wrapper = document.getElementById('bg-canvas');
    const canvas = wrapper.tagName.toLowerCase() === 'uni-canvas' ? wrapper.querySelector('canvas') : wrapper;
    return {
      wrapper: wrapper ? { w: wrapper.offsetWidth, h: wrapper.offsetHeight, tag: wrapper.tagName } : null,
      canvas: canvas ? { w: canvas.width, h: canvas.height, cw: canvas.clientWidth, ch: canvas.clientHeight } : null,
      window: { w: window.innerWidth, h: window.innerHeight, dpr: window.devicePixelRatio }
    };
  });
  
  console.log('DIMS:', JSON.stringify(dims, null, 2));
  await page.screenshot({ path: 'screenshot.png' });
  await browser.close();
})();
