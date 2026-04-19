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
    const canvas = wrapper.querySelector('canvas') || wrapper;
    return {
      wrapper: {
        w: wrapper.clientWidth,
        h: wrapper.clientHeight,
        tag: wrapper.tagName
      },
      canvas: {
        w: canvas.width,
        h: canvas.height,
        styleW: canvas.style.width,
        styleH: canvas.style.height,
        cssW: getComputedStyle(canvas).width,
        cssH: getComputedStyle(canvas).height
      },
      window: {
        w: window.innerWidth,
        h: window.innerHeight,
        dpr: window.devicePixelRatio
      }
    };
  });
  
  console.log(JSON.stringify(dims, null, 2));
  await browser.close();
})();
