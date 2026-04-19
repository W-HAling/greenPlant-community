const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  let errors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
      console.log('CONSOLE ERROR:', msg.text());
    }
  });
  page.on('pageerror', err => {
    errors.push(err.message);
    console.log('PAGE ERROR:', err.message);
  });
  
  await page.goto('http://localhost:3001/#/pages/drift-bottle/index', { waitUntil: 'networkidle' });
  await page.waitForTimeout(2000); // give it some time
  
  if (errors.length === 0) {
    console.log('NO ERRORS');
  }
  await browser.close();
})();
