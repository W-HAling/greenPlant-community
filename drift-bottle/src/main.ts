// src/main.ts
import './style.css'

// 定义四个风格的主题配置，包含核心的 HTML 模板片段
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
  },
  {
    id: 'starry',
    name: '星空海滩',
    className: 'theme-starry',
    html: `
      <!-- 1. 绚丽星空 -->
      <div class="starry-sky">
        <div class="milky-way"></div>
        <div class="stars-layer-1"></div>
        <div class="stars-layer-2"></div>
      </div>
      
      <!-- 2. 海洋与动态扑岸浪花 -->
      <div class="starry-sea">
        <div class="sea-reflection"></div>
        <div class="surf-wave surf-1"></div>
        <div class="surf-wave surf-2"></div>
        <div class="surf-wave surf-3"></div>
        <div class="surf-wave surf-4"></div>
        
        <!-- 海中漂流瓶 -->
        <div class="sea-bottle">
          <div class="bottle-paper"></div>
          <div class="bottle-highlight"></div>
        </div>
      </div>
      
      <!-- 3. 湿润沙滩与贝壳 -->
      <div class="starry-beach">
        <div class="wet-sand"></div>
        <div class="shell shell-1"></div>
        <div class="shell shell-2"></div>
        
        <!-- 沙滩搁浅瓶 -->
        <div class="beach-bottle">
          <div class="bottle-paper"></div>
          <div class="bottle-highlight"></div>
        </div>
      </div>
      
      <!-- 4. 文字与操作按钮 -->
      <div class="desc-text" style="z-index: 10; text-shadow: 0 2px 4px rgba(0,0,0,0.8);">
        <h2>星空与海滩</h2>
        <p>漫天星辰下，听海浪拍打沙滩的声音</p>
        <div style="display: flex; gap: 10px; justify-content: center;">
          <button class="action-btn" onclick="alert('你捡起了海面上的瓶子！')">捞起海中瓶</button>
          <button class="action-btn" onclick="alert('你捡起了沙滩上的瓶子！')">捡起沙滩瓶</button>
        </div>
      </div>
    `
  }
];

// 默认切换到第四个风格（星空海滩）
let currentTheme = 3;

/**
 * 核心渲染函数
 * 根据当前的主题索引，渲染对应的 HTML 结构并绑定事件
 */
function render() {
  const app = document.querySelector<HTMLDivElement>('#app');
  // 提前返回，避免空指针（符合编码规范：避免多层嵌套，提前返回）
  if (!app) return;

  const theme = themes[currentTheme];
  
  // 切换外层容器的样式类，触发背景过渡
  app.className = theme.className;
  
  // 动态生成顶部 Tab 按钮
  const tabsHtml = themes.map((t, index) => `
    <button class="tab-btn ${index === currentTheme ? 'active' : ''}" data-index="${index}">
      ${t.name}
    </button>
  `).join('');

  // 注入 DOM
  app.innerHTML = `
    <div class="tab-container">
      ${tabsHtml}
    </div>
    <div class="content">
      ${theme.html}
    </div>
  `;

  // 重新绑定 Tab 点击事件
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const target = e.target as HTMLButtonElement;
      const indexStr = target.dataset.index;
      if (indexStr !== undefined) {
        currentTheme = parseInt(indexStr, 10);
        render();
      }
    });
  });
}

// 初始渲染
render();
