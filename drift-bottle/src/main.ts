import './style.css'
import { createStarryEngine } from './starry'

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
      <div class="hint" id="hint">拾取漂流瓶，看看大海带来了什么心愿</div>
      <div class="pick-btn" id="pickBtn"><i class="fa-solid fa-hand-pointer" style="margin-right:6px;font-size:12px"></i>拾取漂流瓶</div>
      <div class="wish-btn" id="wishBtn"><i class="fa-solid fa-feather" style="margin-right:6px;font-size:12px"></i>写下心愿</div>
      <div class="sound-btn" id="soundBtn"><i class="fa-solid fa-volume-xmark"></i></div>
      
      <div class="panel-overlay" id="panel">
        <div class="panel-bg" id="panelBg"></div>
        <div class="panel-card">
          <h3>写下你的心愿</h3>
          <textarea id="wishText" placeholder="在这里写下你想对大海说的话..."></textarea>
          <div class="panel-btns">
            <button class="btn-send" id="sendBtn">投入大海</button>
            <button class="btn-cancel" id="cancelBtn">取消</button>
          </div>
        </div>
      </div>
      
      <div class="paper" id="paper">
        <div class="cap cap-t"></div>
        <div class="paper-body"><p id="paperText"></p></div>
        <div class="cap cap-b"></div>
      </div>
    `
  }
];

let currentTheme = 3;
let bgEngine: any = null;

function render() {
  const app = document.querySelector<HTMLDivElement>('#app');
  const bgCanvas = document.getElementById('bg-canvas') as HTMLCanvasElement;
  if (!app || !bgCanvas) return;

  if (bgEngine) {
    bgEngine.unmount();
  }

  const theme = themes[currentTheme];
  app.className = theme.className;

  if (theme.id === 'starry') {
    bgCanvas.style.display = 'block';
  } else {
    bgCanvas.style.display = 'none';
  }

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

  if (theme.id === 'starry') {
    if (!bgEngine) {
      bgEngine = createStarryEngine('bg-canvas');
    }
    bgEngine.mount();
  }
}

// 初始渲染
render();