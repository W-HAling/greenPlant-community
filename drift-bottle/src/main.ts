import './style.css'

// 提取并封装 Canvas 动画类
class StarryBeachCanvas {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private W: number = 0;
  private H: number = 0;
  private horizonY: number = 0;
  private sandY: number = 0;
  private reqId: number | null = null;
  private isRunning: boolean = false;
  private lastTs: number = 0;

  // 实体数组
  private stars: any[] = [];
  private milkyStars: any[] = [];
  private meteors: any[] = [];
  private reflections: any[] = [];
  private sandGrains: any[] = [];
  private shells: any[] = [];
  private crabs: any[] = [];

  private waveLayers = [
    { yOff: 0,     amp: 2,  freq: 0.022, spd: 0.25, ph: 0,    r: 10, g: 18, b: 48, a: 0.75 },
    { yOff: 0.025, amp: 3,  freq: 0.016, spd: 0.35, ph: 1.3,  r: 12, g: 22, b: 56, a: 0.65 },
    { yOff: 0.05,  amp: 4,  freq: 0.012, spd: 0.45, ph: 2.5,  r: 14, g: 26, b: 65, a: 0.55 },
    { yOff: 0.09,  amp: 6,  freq: 0.009, spd: 0.55, ph: 3.7,  r: 17, g: 32, b: 74, a: 0.48 },
    { yOff: 0.14,  amp: 8,  freq: 0.007, spd: 0.68, ph: 4.9,  r: 20, g: 38, b: 84, a: 0.42 },
    { yOff: 0.20,  amp: 10, freq: 0.005, spd: 0.82, ph: 6.1,  r: 24, g: 44, b: 95, a: 0.36 },
    { yOff: 0.26,  amp: 13, freq: 0.004, spd: 1.0,  ph: 7.3,  r: 28, g: 52, b: 108,a: 0.30 },
  ];

  private waterBottles = [
    { xR: 0.14, li: 2 }, { xR: 0.36, li: 3 }, { xR: 0.56, li: 1 }, { xR: 0.76, li: 4 }, { xR: 0.92, li: 3 }
  ];

  private sandBottles = [
    { xR: 0.10, yR: 0.79, ang: -0.12, sc: 1.05 },
    { xR: 0.42, yR: 0.83, ang: 0.18, sc: 0.9 },
    { xR: 0.68, yR: 0.77, ang: -0.22, sc: 1.1 },
    { xR: 0.87, yR: 0.85, ang: 0.08, sc: 0.85 },
  ];

  constructor(canvasId: string) {
    this.canvas = document.getElementById(canvasId) as HTMLCanvasElement;
    this.ctx = this.canvas.getContext('2d')!;
    
    window.addEventListener('resize', this.initResize.bind(this));
  }

  // --- 初始化部分 ---
  private initResize() {
    this.W = this.canvas.width = window.innerWidth;
    this.H = this.canvas.height = window.innerHeight;
    this.horizonY = this.H * 0.44;
    this.sandY = this.H * 0.71;
    
    this.initStars();
    this.initMilkyWay();
    this.initReflections();
    this.initSandGrains();
    this.initShells();
    this.initCrabs();
  }

  private initStars() {
    this.stars = [];
    const count = Math.min(600, Math.floor(this.W * this.H / 1800));
    for (let i = 0; i < count; i++) {
        this.stars.push({
            x: Math.random() * this.W,
            y: Math.random() * this.horizonY * 1.05,
            r: Math.random() * 1.6 + 0.3,
            speed: Math.random() * 2 + 0.5,
            phase: Math.random() * Math.PI * 2,
            base: Math.random() * 0.45 + 0.3
        });
    }
  }

  private initMilkyWay() {
    this.milkyStars = [];
    for (let i = 0; i < 900; i++) {
        const p = Math.random();
        const cx = this.W * (0.05 + p * 0.9);
        const cy = this.H * (0.04 + p * 0.32);
        const angle = Math.atan2(this.H * 0.32, this.W * 0.9);
        const spread = (Math.random() - 0.5) * this.H * 0.07;
        this.milkyStars.push({
            x: cx + (-Math.sin(angle)) * spread,
            y: cy + Math.cos(angle) * spread,
            r: Math.random() * 1.1 + 0.15,
            a: Math.random() * 0.35 + 0.08
        });
    }
  }

  private initReflections() {
    this.reflections = [];
    for (let i = 0; i < 70; i++) {
        this.reflections.push({
            x: Math.random() * this.W,
            y: this.horizonY + Math.random() * (this.sandY - this.horizonY),
            w: Math.random() * 18 + 5,
            h: Math.random() * 1.8 + 0.8,
            a: Math.random() * 0.25 + 0.08,
            spd: Math.random() * 1.2 + 0.4,
            ph: Math.random() * Math.PI * 2
        });
    }
  }

  private initSandGrains() {
    this.sandGrains = [];
    for (let i = 0; i < 600; i++) {
        this.sandGrains.push({
            x: Math.random() * this.W,
            y: this.sandY + Math.random() * (this.H - this.sandY),
            r: Math.random() * 1.2 + 0.3,
            a: Math.random() * 0.12 + 0.04
        });
    }
  }

  private initShells() {
    this.shells = [];
    const shellCount = Math.max(12, Math.floor(this.W / 100));
    const types = ['fan', 'spiral', 'clam', 'cone', 'flat'];
    const palettes = [
        { body: '#d4b896', edge: '#a08060', inner: '#f0e0c8' },
        { body: '#c8a882', edge: '#8a6e50', inner: '#e8d4b8' },
        { body: '#bfa070', edge: '#907048', inner: '#e0ccaa' },
        { body: '#dcc8a8', edge: '#b09878', inner: '#f5ece0' },
        { body: '#c0a078', edge: '#886848', inner: '#e8dcc4' },
        { body: '#e0c8a0', edge: '#b8a078', inner: '#f8f0e4' },
        { body: '#b89870', edge: '#806040', inner: '#ddd0b8' },
        { body: '#c8b898', edge: '#a09068', inner: '#f0e8d8' },
    ];
    for (let i = 0; i < shellCount; i++) {
        this.shells.push({
            x: Math.random() * this.W,
            y: this.sandY + 8 + Math.random() * (this.H - this.sandY - 20),
            type: types[Math.floor(Math.random() * types.length)],
            size: 0.5 + Math.random() * 0.9,
            angle: (Math.random() - 0.5) * 0.8,
            palette: palettes[Math.floor(Math.random() * palettes.length)],
            flip: Math.random() > 0.5
        });
    }
  }

  private initCrabs() {
    this.crabs = [
        { x: this.W * 0.25, baseY: this.sandY + (this.H - this.sandY) * 0.35, dir: 1, speed: 0.35, walkPhase: 0, pauseTimer: 0, pausing: false, clawPhase: 0, size: 1, color: { body: '#8b4513', shell: '#a0522d', leg: '#6b3410', claw: '#b5651d', eye: '#1a0a00' } },
        { x: this.W * 0.72, baseY: this.sandY + (this.H - this.sandY) * 0.55, dir: -1, speed: 0.28, walkPhase: Math.PI, pauseTimer: 2, pausing: true, clawPhase: Math.PI * 0.5, size: 0.75, color: { body: '#6b3a1f', shell: '#7a4428', leg: '#52290f', claw: '#8b5a2b', eye: '#100500' } },
        { x: this.W * 0.5, baseY: this.sandY + (this.H - this.sandY) * 0.75, dir: 1, speed: 0.42, walkPhase: Math.PI * 1.5, pauseTimer: 0, pausing: false, clawPhase: Math.PI, size: 0.6, color: { body: '#9c5a30', shell: '#b06838', leg: '#7a4420', claw: '#c07840', eye: '#1a0a00' } }
    ];
  }

  // --- 绘制与更新工具方法 ---
  private hexToRgb(hex: string) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return r + ',' + g + ',' + b;
  }

  private waveY(x: number, t: number, layer: any) {
    const base = this.horizonY + layer.yOff * this.H;
    return base
        + layer.amp * Math.sin(x * layer.freq + t * layer.spd + layer.ph)
        + layer.amp * 0.45 * Math.sin(x * layer.freq * 2.2 + t * layer.spd * 1.4 + layer.ph * 1.6)
        + layer.amp * 0.22 * Math.sin(x * layer.freq * 3.5 + t * layer.spd * 0.75 + layer.ph * 2.2);
  }

  private fillRoundRect(x: number, y: number, w: number, h: number, r: number) {
    const ctx = this.ctx;
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + w - r, y);
    ctx.arcTo(x + w, y, x + w, y + r, r);
    ctx.lineTo(x + w, y + h - r);
    ctx.arcTo(x + w, y + h, x + w - r, y + h, r);
    ctx.lineTo(x + r, y + h);
    ctx.arcTo(x, y + h, x, y + h - r, r);
    ctx.lineTo(x, y + r);
    ctx.arcTo(x, y, x + r, y, r);
    ctx.closePath();
    ctx.fill();
  }

  // --- 渲染循环与核心绘制 ---
  private renderLoop(ts: number) {
    if (!this.isRunning) return;

    const dt = Math.min((ts - this.lastTs) / 1000, 0.05);
    this.lastTs = ts;
    const t = ts / 1000;
    const ctx = this.ctx;
    const W = this.W;
    const H = this.H;
    
    ctx.clearRect(0, 0, W, H);

    /* 天空 */
    const skyG = ctx.createLinearGradient(0, 0, 0, this.horizonY + 5);
    skyG.addColorStop(0, '#000510');
    skyG.addColorStop(0.35, '#060d24');
    skyG.addColorStop(0.65, '#0a1535');
    skyG.addColorStop(1, '#0e1c42');
    ctx.fillStyle = skyG;
    ctx.fillRect(0, 0, W, this.horizonY + 5);

    /* 银河与星星 */
    for (const s of this.milkyStars) {
        const f = 0.65 + 0.35 * Math.sin(t * 1.3 + s.x * 0.008 + s.y * 0.005);
        ctx.beginPath();
        ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(195,205,255,' + (s.a * f).toFixed(3) + ')';
        ctx.fill();
    }
    for (const s of this.stars) {
        const a = s.base * (0.45 + 0.55 * Math.sin(t * s.speed + s.phase));
        ctx.beginPath();
        ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(255,255,245,' + a.toFixed(3) + ')';
        ctx.fill();
    }

    /* 流星 */
    if (Math.random() < 0.0025 && this.meteors.length < 2) {
        this.meteors.push({
            x: Math.random() * W * 0.7 + W * 0.1,
            y: Math.random() * H * 0.25,
            vx: 3.5 + Math.random() * 4,
            vy: 1.5 + Math.random() * 2.5,
            life: 1,
            decay: 0.012 + Math.random() * 0.01,
            len: 50 + Math.random() * 40
        });
    }
    for (let i = this.meteors.length - 1; i >= 0; i--) {
        const m = this.meteors[i];
        m.x += m.vx; m.y += m.vy; m.life -= m.decay;
        if (m.life <= 0) { this.meteors.splice(i, 1); continue; }
        const ex = m.x - m.vx / 5 * m.len;
        const ey = m.y - m.vy / 5 * m.len;
        const g = ctx.createLinearGradient(m.x, m.y, ex, ey);
        g.addColorStop(0, 'rgba(255,255,240,' + m.life.toFixed(3) + ')');
        g.addColorStop(1, 'rgba(255,255,240,0)');
        ctx.beginPath(); ctx.moveTo(m.x, m.y); ctx.lineTo(ex, ey);
        ctx.strokeStyle = g; ctx.lineWidth = 1.5; ctx.stroke();
    }

    /* 地平线光晕 */
    const rg = ctx.createRadialGradient(W / 2, this.horizonY, 0, W / 2, this.horizonY, W * 0.55);
    rg.addColorStop(0, 'rgba(25,45,80,0.25)');
    rg.addColorStop(0.4, 'rgba(18,32,60,0.12)');
    rg.addColorStop(1, 'rgba(10,18,38,0)');
    ctx.fillStyle = rg;
    ctx.fillRect(0, this.horizonY - H * 0.08, W, H * 0.16);

    /* 海面基础 */
    const seaG = ctx.createLinearGradient(0, this.horizonY, 0, this.sandY);
    seaG.addColorStop(0, '#070c22');
    seaG.addColorStop(0.35, '#0a1338');
    seaG.addColorStop(0.7, '#0d1848');
    seaG.addColorStop(1, '#101e58');
    ctx.fillStyle = seaG;
    ctx.fillRect(0, this.horizonY - 3, W, this.sandY - this.horizonY + 8);

    /* 波浪与倒影 */
    for (let li = 0; li < this.waveLayers.length; li++) {
        const L = this.waveLayers[li];
        ctx.beginPath();
        ctx.moveTo(-2, this.sandY + 25);
        for (let x = -2; x <= W + 2; x += 2) ctx.lineTo(x, this.waveY(x, t, L));
        ctx.lineTo(W + 2, this.sandY + 25);
        ctx.closePath();
        ctx.fillStyle = 'rgba(' + L.r + ',' + L.g + ',' + L.b + ',' + L.a + ')';
        ctx.fill();

        ctx.beginPath();
        for (let x = -2; x <= W + 2; x += 2) {
            const y = this.waveY(x, t, L);
            x === -2 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
        }
        ctx.strokeStyle = 'rgba(70,130,195,' + (L.a * 0.45).toFixed(3) + ')';
        ctx.lineWidth = 0.7;
        ctx.stroke();
    }

    for (const r of this.reflections) {
        const s = 0.4 + 0.6 * Math.sin(t * r.spd + r.ph);
        const ox = Math.sin(t * 0.7 + r.ph) * 3;
        ctx.fillStyle = 'rgba(190,215,255,' + (r.a * s).toFixed(3) + ')';
        ctx.fillRect(r.x + ox, r.y, r.w * s, r.h);
    }

    /* 海上漂流瓶 */
    for (const b of this.waterBottles) {
        const x = b.xR * W;
        const L = this.waveLayers[b.li];
        const y = this.waveY(x, t, L);
        const ang = Math.sin(t * 0.7 + b.xR * 12) * 0.1;
        const sc = 1 + Math.sin(t * 1.1 + b.xR * 6) * 0.025;
        this.drawBottle(x, y - 2, sc, ang, true);
    }

    /* 海浪泡沫 */
    for (let x = 0; x < W; x += 3) {
        const wo = 10 * Math.sin(x * 0.009 + t * 0.75) + 4 * Math.sin(x * 0.025 + t * 1.1);
        const y = this.sandY - 4 + wo;
        const a = 0.12 + 0.08 * Math.sin(x * 0.04 + t * 1.3);
        const w = 7 + 4 * Math.sin(x * 0.02 + t * 0.6);
        ctx.fillStyle = 'rgba(195,215,235,' + a.toFixed(3) + ')';
        ctx.fillRect(x, y, w, 1.5);
    }

    /* 沙滩与沙粒 */
    const sg = ctx.createLinearGradient(0, this.sandY - 3, 0, H);
    sg.addColorStop(0, '#4a4030'); sg.addColorStop(0.1, '#5e5240');
    sg.addColorStop(0.3, '#7a6d55'); sg.addColorStop(0.6, '#9a8a68'); sg.addColorStop(1, '#b09a74');
    ctx.fillStyle = sg;
    ctx.fillRect(0, this.sandY - 3, W, H - this.sandY + 3);

    for (const g of this.sandGrains) {
        ctx.fillStyle = 'rgba(175,160,125,' + g.a.toFixed(3) + ')';
        ctx.fillRect(g.x, g.y, g.r, g.r);
    }

    /* 贝壳与沙滩瓶 */
    for (const s of this.shells) this.drawShell(s);
    for (const b of this.sandBottles) this.drawBottle(b.xR * W, b.yR * H, b.sc, b.ang, false);

    /* 螃蟹 (略去复杂绘制逻辑保持轻量，这里复用原逻辑但缩简) */
    this.updateAndDrawCrabs(t, dt);

    this.reqId = requestAnimationFrame(this.renderLoop.bind(this));
  }

  /* 复用绘制瓶子 */
  private drawBottle(cx: number, cy: number, scale: number, angle: number, glow: boolean) {
    const ctx = this.ctx;
    ctx.save();
    ctx.translate(cx, cy); ctx.rotate(angle); ctx.scale(scale, scale);

    if (glow) {
        const rg = ctx.createRadialGradient(0, -12, 2, 0, -12, 30);
        rg.addColorStop(0, 'rgba(100,180,240,0.12)');
        rg.addColorStop(1, 'rgba(100,180,240,0)');
        ctx.fillStyle = rg;
        ctx.beginPath(); ctx.arc(0, -12, 30, 0, Math.PI * 2); ctx.fill();
    }

    const bw = 16, bh = 28, nw = 6, nh = 9;
    ctx.beginPath();
    ctx.moveTo(-bw / 2, 0);
    ctx.quadraticCurveTo(-bw / 2, bh * 0.38, 0, bh * 0.38);
    ctx.quadraticCurveTo(bw / 2, bh * 0.38, bw / 2, 0);
    ctx.lineTo(bw / 2, -bh);
    ctx.quadraticCurveTo(bw / 2, -bh - 5, nw / 2, -bh - 5);
    ctx.lineTo(nw / 2, -bh - 5 - nh);
    ctx.lineTo(-nw / 2, -bh - 5 - nh);
    ctx.lineTo(-nw / 2, -bh - 5);
    ctx.quadraticCurveTo(-bw / 2, -bh - 5, -bw / 2, -bh);
    ctx.closePath();

    const gg = ctx.createLinearGradient(-bw / 2, 0, bw / 2, 0);
    gg.addColorStop(0, 'rgba(95,165,210,0.18)');
    gg.addColorStop(0.3, 'rgba(165,215,240,0.32)');
    gg.addColorStop(0.7, 'rgba(130,195,225,0.26)');
    gg.addColorStop(1, 'rgba(85,155,200,0.18)');
    ctx.fillStyle = gg; ctx.fill();
    ctx.strokeStyle = 'rgba(175,218,240,0.4)'; ctx.lineWidth = 0.7; ctx.stroke();

    ctx.beginPath(); ctx.ellipse(0, -bh * 0.3, 3.5, 10, 0, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(240,228,195,0.5)'; ctx.fill();
    ctx.strokeStyle = 'rgba(200,188,155,0.25)'; ctx.lineWidth = 0.4; ctx.stroke();

    ctx.fillStyle = '#8a7350';
    this.fillRoundRect(-nw / 2 + 0.5, -bh - 5 - nh - 3.5, nw - 1, 4.5, 1);

    ctx.fillStyle = 'rgba(255,255,255,0.18)';
    ctx.fillRect(-bw / 2 + 2.5, -bh + 3, 2.2, bh - 7);
    ctx.fillStyle = 'rgba(255,255,255,0.12)';
    ctx.fillRect(-nw / 2 + 1, -bh - 5 - nh, nw - 2, nh);

    ctx.restore();
  }

  /* 简化版贝壳 */
  private drawShell(s: any) {
    const ctx = this.ctx;
    ctx.save();
    ctx.translate(s.x, s.y); ctx.rotate(s.angle);
    if (s.flip) ctx.scale(-1, 1);
    ctx.scale(s.size, s.size);
    
    // 统一画个小贝壳（为了保持代码精简，使用通用扇形贝壳替代五种复杂类型）
    const p = s.palette;
    ctx.beginPath();
    ctx.moveTo(0, 5);
    for (let i = 0; i <= 9; i++) {
        const ang = -Math.PI * 0.85 + (Math.PI * 1.7 / 9) * i;
        ctx.lineTo(Math.cos(ang) * 14, Math.sin(ang) * 14 - 4);
    }
    ctx.closePath();
    const fg = ctx.createRadialGradient(0, -4, 2, 0, -4, 14);
    fg.addColorStop(0, p.inner); fg.addColorStop(0.6, p.body); fg.addColorStop(1, p.edge);
    ctx.fillStyle = fg; ctx.fill(); ctx.strokeStyle = p.edge; ctx.lineWidth = 0.6; ctx.stroke();
    
    ctx.restore();
  }

  private updateAndDrawCrabs(t: number, dt: number) {
    for (const c of this.crabs) {
        if (c.pausing) {
            c.pauseTimer -= dt;
            if (c.pauseTimer <= 0) { c.pausing = false; c.dir *= (Math.random() > 0.5 ? 1 : -1); }
        } else {
            c.x += c.dir * c.speed * 60 * dt;
            if (c.x < this.W * 0.05 || c.x > this.W * 0.95) {
                c.pausing = true; c.pauseTimer = 1.5 + Math.random() * 2; c.dir *= -1;
            }
        }
        
        const ctx = this.ctx;
        ctx.save();
        ctx.translate(c.x, c.baseY); ctx.scale(c.dir * c.size, c.size);
        
        ctx.beginPath(); ctx.ellipse(0, 0, 10, 7, 0, 0, Math.PI * 2);
        ctx.fillStyle = c.color.shell; ctx.fill();
        
        ctx.restore();
    }
  }

  // --- 外部控制 ---
  public start() {
    if (!this.isRunning) {
        this.isRunning = true;
        this.initResize();
        this.lastTs = performance.now();
        this.reqId = requestAnimationFrame(this.renderLoop.bind(this));
    }
  }
  public stop() {
    this.isRunning = false;
    if (this.reqId) cancelAnimationFrame(this.reqId);
  }
}

// 实例化 Canvas 引擎
const bgEngine = new StarryBeachCanvas('bg-canvas');


// 定义四个风格的主题配置
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
      <!-- 背景和特效已由底层 Canvas 接管，这里仅保留交互层 -->
      <div class="desc-text">
        <h2>星空与海滩</h2>
        <p>漫天星辰下，听海浪拍打沙滩的声音</p>
        <div style="display: flex; gap: 10px; justify-content: center;">
          <button class="action-btn" onclick="alert('抛出一个瓶子！')">扔一个瓶子</button>
          <button class="action-btn" onclick="alert('从海滩捡起一个瓶子！')">捡一个瓶子</button>
        </div>
      </div>
    `
  }
];

// 默认切换到第四个风格（星空海滩）
let currentTheme = 3;

/**
 * 核心渲染函数
 */
function render() {
  const app = document.querySelector<HTMLDivElement>('#app');
  const bgCanvas = document.getElementById('bg-canvas') as HTMLCanvasElement;
  if (!app || !bgCanvas) return;

  const theme = themes[currentTheme];
  
  // 切换外层容器的样式类
  app.className = theme.className;
  
  // 控制底层的 Canvas 引擎是否渲染
  if (theme.id === 'starry') {
    bgCanvas.style.display = 'block';
    bgEngine.start();
  } else {
    bgCanvas.style.display = 'none';
    bgEngine.stop();
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
}

// 初始渲染
render();
