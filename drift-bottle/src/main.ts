// src/main.ts
import './style.css'

const themes = [
  {
    id: 'deep-sea',
    name: '深海风',
    className: 'theme-deep-sea',
    html: `
      <div class="deep-sea-particles"></div>
      <div class="deep-sea-bottle">
        <div class="deep-sea-glow"></div>
      </div>
      <div class="desc-text">
        <h2>梦幻深海</h2>
        <p>适合匿名倾诉，情感树洞，氛围治愈</p>
        <button class="action-btn" onclick="alert('抛出一个发光的瓶子！')">扔一个瓶子</button>
      </div>
    `
  },
  {
    id: 'beach',
    name: '沙滩风',
    className: 'theme-beach',
    html: `
      <div class="beach-wave-2"></div>
      <div class="beach-wave"></div>
      <div class="beach-bottle">
        <div class="beach-paper"></div>
      </div>
      <div class="desc-text">
        <h2>清新沙滩</h2>
        <p>适合轻松交友，年轻化互动社交</p>
        <button class="action-btn" onclick="alert('捡起沙滩上的瓶子！')">捡一个瓶子</button>
      </div>
    `
  },
  {
    id: 'glass',
    name: '毛玻璃风',
    className: 'theme-glass',
    html: `
      <div class="glass-blob1"></div>
      <div class="glass-blob2"></div>
      <div class="glass-card">
        <div class="glass-icon"></div>
        <h2>极简毛玻璃</h2>
        <p>现代高级感，抽象视觉，前卫设计</p>
        <button class="action-btn" onclick="alert('开启极简漂流之旅！')">开启漂流</button>
      </div>
    `
  }
];

let currentTheme = 0;

function render() {
  const app = document.querySelector<HTMLDivElement>('#app')!;
  const theme = themes[currentTheme];
  
  app.className = theme.className;
  
  const tabsHtml = themes.map((t, index) => `
    <button class="tab-btn ${index === currentTheme ? 'active' : ''}" data-index="${index}">
      ${t.name}
    </button>
  `).join('');

  app.innerHTML = `
    <div class="tab-container">
      ${tabsHtml}
    </div>
    <div class="content">
      ${theme.html}
    </div>
  `;

  // 绑定事件
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const target = e.target as HTMLButtonElement;
      currentTheme = parseInt(target.dataset.index || '0', 10);
      render();
    });
  });
}

// 初始渲染
render();
