// @ts-nocheck
export function createStarryEngine(canvasId: string) {
    var $=function(id){return document.getElementById(id)};
    var canvas=document.getElementById(canvasId),ctx=canvas.getContext('2d');
    var TS=4096,SIN=new Float32Array(TS);
    for(var i=0;i<TS;i++)SIN[i]=Math.sin(i/TS*Math.PI*2);
    function fs(x){return SIN[((x/(Math.PI*2)%1+1)%1*TS)|0]}
    function hr(h){return parseInt(h.slice(1,3),16)+','+parseInt(h.slice(3,5),16)+','+parseInt(h.slice(5,7),16)}
    function rrect(x,y,w,h,r){ctx.beginPath();ctx.moveTo(x+r,y);ctx.lineTo(x+w-r,y);ctx.arcTo(x+w,y,x+w,y+r,r);ctx.lineTo(x+w,y+h-r);ctx.arcTo(x+w,y+h,x+w-r,y+h,r);ctx.lineTo(x+r,y+h);ctx.arcTo(x,y+h,x,y+h-r,r);ctx.lineTo(x,y+r);ctx.arcTo(x,y,x+r,y,r);ctx.closePath();ctx.fill()}
    function dst(a,b,c,d){return Math.hypot(c-a,d-b)}
    function lerp(a,b,t){return a+(b-a)*t}

    var W=0,H=0,hY=0,bSY=0,mX=0,mY=0;
    var skyOC=document.createElement('canvas'),skyX=skyOC.getContext('2d');
    var moonOC=document.createElement('canvas'),moonX=moonOC.getContext('2d');
    var twStars=[],clouds=[],meteors=[];
    var refls=[],bios=[],wLayers=[];
    var shells=[],crabs=[],wShells=[];
    var thrownB=[],splashes=[],ripples=[];
    var sandGrains=[];
    var waterBPos=[],sandBPos=[];
    var mx=-999,my=-999;
    var curTide=0,hintOn=true,scrollTmr=null;
    var aCtx=null,aGain=null,aOn=false;
    var pickMode=false,pickingAnim=null;
    var REFL_STEPS=35;

    var wishes=[
      "愿你被这个世界温柔以待","星河滚烫，你是人间理想",
      "但愿日子清净，抬头遇见的都是柔情","愿所有美好都不期而遇",
      "生活明朗，万物可爱","山高水长，来日方长",
      "愿你眼中有星辰大海","此去繁花似锦，再相逢依旧如故",
      "愿你所求皆如愿，所行化坦途","世间美好与你环环相扣",
      "风会带走一切，也会送来一切","海的那边是什么，是另一个开始",
      "把心事装进瓶子，让大海替你保管","今夜的星星很亮，就像你的眼睛",
      "愿你历尽千帆，归来仍是少年","向着星辰与深渊，勇敢前行",
      "愿你拥有超能力，活成自己喜欢的样子","每一个不曾起舞的日子，都是对生命的辜负",
      "你要悄悄拔尖，然后惊艳所有人","我有所念人，隔在远远乡",
      "岁月不饶人，我亦未曾饶过岁月","愿你成为自己的太阳，无需凭借谁的光",
      "在所有物是人非的景色中，我最喜欢你","愿你的故事被人记住，愿你的名字被人念起"
    ];

    function makeWaves(){
      wLayers=[
        {yo:0,a:2,f:.022,s:.25,p:0,r:10,g:18,b:48,al:.75},    
        {yo:.025,a:3,f:.016,s:.35,p:1.3,r:12,g:22,b:56,al:.65},  
        {yo:.05,a:4,f:.012,s:.45,p:2.5,r:14,g:26,b:65,al:.55},  
        {yo:.09,a:6,f:.009,s:.55,p:3.7,r:17,g:32,b:74,al:.48},  
        {yo:.14,a:8,f:.007,s:.68,p:4.9,r:20,g:38,b:84,al:.42},  
        {yo:.20,a:10,f:.005,s:.82,p:6.1,r:24,g:44,b:95,al:.36}, 
        {yo:.26,a:13,f:.004,s:1.0,p:7.3,r:28,g:52,b:108,al:.30},
        {yo:.32,a:16,f:.003,s:1.2,p:8.5,r:32,g:60,b:120,al:.25} 
      ];
    }
    function wY(x,t,L){
      var base=hY+L.yo*H;
      return base
        +L.a*fs(x*L.f+t*L.s+L.p)
        +L.a*.45*fs(x*L.f*2.2+t*L.s*1.4+L.p*1.6)
        +L.a*.22*fs(x*L.f*3.5+t*L.s*.75+L.p*2.2);
    }

    function initTwStars(){
      twStars=[];
      for(var i=0;i<80;i++){
        twStars.push({x:Math.random()*W,y:Math.random()*hY*1.05,r:Math.random()*1.8+.6,sp:Math.random()*2.5+.5,ph:Math.random()*6.28,b:Math.random()*.5+.4});
      }
    }

    function initSkyCanvas(){
      skyOC.width=W;skyOC.height=Math.ceil(hY)+5;
      var sc=skyX;
      var g=sc.createLinearGradient(0,0,0,hY+5);
      g.addColorStop(0,'#000510');g.addColorStop(0.35,'#060d24');
      g.addColorStop(0.65,'#0a1535');g.addColorStop(1,'#0e1c42');
      sc.fillStyle=g;sc.fillRect(0,0,W,hY+5);
      for(var i=0;i<900;i++){
        var p=Math.random(),cx=W*(.05+p*.9),cy=H*(.04+p*.32);
        var ang=Math.atan2(H*.32,W*.9),sp=(Math.random()-.5)*H*.07;
        var sx=cx-Math.sin(ang)*sp,sy=cy+Math.cos(ang)*sp;
        var sr=Math.random()*1.1+.15,sa=Math.random()*.35+.08;
        sc.beginPath();sc.arc(sx,sy,sr,0,6.28);
        sc.fillStyle='rgba(195,205,255,'+sa.toFixed(3)+')';sc.fill();
      }
      var cnt=Math.min(500,Math.floor(W*H/2500));
      for(var i=0;i<cnt;i++){
        var sx2=Math.random()*W,sy2=Math.random()*hY*1.05;
        var sr2=Math.random()*1+.2,sb=Math.random()*.35+.15;
        sc.beginPath();sc.arc(sx2,sy2,sr2,0,6.28);
        sc.fillStyle='rgba(255,255,245,'+sb.toFixed(3)+')';sc.fill();
      }
    }

    function initMoonCanvas(){
      var mR=Math.max(12,H*.025);
      var sz=Math.ceil(mR*16);moonOC.width=moonOC.height=sz;
      var mc=moonX,cx=sz/2,cy=sz/2;
      var g1=mc.createRadialGradient(cx,cy,mR,cx,cy,mR*6);
      g1.addColorStop(0,'rgba(200,215,235,0.15)');g1.addColorStop(0.4,'rgba(160,180,210,0.04)');g1.addColorStop(1,'rgba(120,140,180,0)');
      mc.fillStyle=g1;mc.beginPath();mc.arc(cx,cy,mR*6,0,6.28);mc.fill();
      var g2=mc.createRadialGradient(cx,cy,mR*.8,cx,cy,mR*2.2);
      g2.addColorStop(0,'rgba(235,230,215,0.25)');g2.addColorStop(1,'rgba(210,215,230,0)');
      mc.fillStyle=g2;mc.beginPath();mc.arc(cx,cy,mR*2.2,0,6.28);mc.fill();
      mc.beginPath();mc.arc(cx,cy,mR,0,6.28);mc.fillStyle='#e8e4d8';mc.fill();
      mc.globalCompositeOperation='destination-out';
      mc.beginPath();mc.arc(cx+mR*.45,cy-mR*.1,mR*.82,0,6.28);mc.fill();
      mc.globalCompositeOperation='source-over';
    }

    function initClouds(){
      clouds=[];
      for(var i=0;i<5;i++){
        var bs=[];var n=3+Math.floor(Math.random()*4);
        for(var j=0;j<n;j++)bs.push({ox:(Math.random()-.5)*70,oy:(Math.random()-.5)*14,rx:22+Math.random()*45,ry:7+Math.random()*12});
        clouds.push({x:Math.random()*W*1.4-W*.2,y:H*.06+Math.random()*H*.28,sp:.08+Math.random()*.12,bs:bs,al:.025+Math.random()*.03});
      }
    }

    function initReflections(){
      refls=[];
      for(var i=0;i<65;i++)refls.push({x:Math.random()*W,y:hY+Math.random()*(bSY-hY),w:Math.random()*16+5,h:Math.random()*1.6+.8,a:Math.random()*.22+.08,sp:Math.random()*1.2+.4,ph:Math.random()*6.28});
    }

    function initBio(){
      bios=[];
      for(var i=0;i<35;i++)bios.push({x:Math.random()*W,y:bSY-Math.random()*40-5,r:Math.random()*1.5+.5,sp:Math.random()*1.5+.5,ph:Math.random()*6.28,b:Math.random()*.4+.2});
    }

    function initSandGrains(){
      sandGrains=[];
      for(var i=0;i<500;i++)sandGrains.push({x:Math.random()*W,y:bSY+Math.random()*(H-bSY),r:Math.random()*1.1+.3,a:Math.random()*.1+.04});
    }

    var sPals=[
      {b:'#d4b896',e:'#a08060',i:'#f0e0c8'},{b:'#c8a882',e:'#8a6e50',i:'#e8d4b8'},
      {b:'#bfa070',e:'#907048',i:'#e0ccaa'},{b:'#dcc8a8',e:'#b09878',i:'#f5ece0'},
      {b:'#c0a078',e:'#886848',i:'#e8dcc4'},{b:'#e0c8a0',e:'#b8a078',i:'#f8f0e4'}
    ];
    var shellTypes=['fan','spiral','clam','cone','flat'];

    function initShells(){
      shells=[];
      var n=Math.max(10,Math.floor(W/110));
      for(var i=0;i<n;i++){
        shells.push({x:Math.random()*W,y:bSY+10+Math.random()*(H-bSY-25),t:shellTypes[Math.floor(Math.random()*5)],sc:.45+Math.random()*.85,an:(Math.random()-.5)*.8,p:sPals[Math.floor(Math.random()*sPals.length)],fl:Math.random()>.5});
      }
    }

    function drawFanShell(p){
      var ribs=9;ctx.beginPath();ctx.moveTo(0,5);
      for(var i=0;i<=ribs;i++){var a=-Math.PI*.85+(Math.PI*1.7/ribs)*i;ctx.lineTo(Math.cos(a)*14,Math.sin(a)*14-4)}
      ctx.closePath();
      var fg=ctx.createRadialGradient(0,-4,2,0,-4,14);fg.addColorStop(0,p.i);fg.addColorStop(0.6,p.b);fg.addColorStop(1,p.e);
      ctx.fillStyle=fg;ctx.fill();ctx.strokeStyle=p.e;ctx.lineWidth=.6;ctx.stroke();
      for(var i=1;i<ribs;i++){var a2=-Math.PI*.85+(Math.PI*1.7/ribs)*i;ctx.beginPath();ctx.moveTo(0,5);ctx.lineTo(Math.cos(a2)*12,Math.sin(a2)*12-4);ctx.strokeStyle='rgba('+hr(p.e)+',0.3)';ctx.lineWidth=.5;ctx.stroke()}
      ctx.beginPath();ctx.ellipse(0,5,4,2,0,0,6.28);ctx.fillStyle=p.e;ctx.fill();
    }
    function drawSpiralShell(p){
      ctx.beginPath();
      for(var a=0;a<Math.PI*5;a+=.1){var r=1.5+a*1.1;var x=Math.cos(a)*r,y=-Math.sin(a)*r+4;a===0?ctx.moveTo(x,y):ctx.lineTo(x,y)}
      ctx.strokeStyle=p.b;ctx.lineWidth=3;ctx.lineCap='round';ctx.stroke();
      ctx.strokeStyle='rgba('+hr(p.i)+',0.4)';ctx.lineWidth=1.2;ctx.stroke();
      ctx.beginPath();ctx.arc(0,4,2,0,6.28);ctx.fillStyle=p.e;ctx.fill();
    }
    function drawClamShell(p){
      ctx.beginPath();ctx.ellipse(0,-2,10,7,0,Math.PI,0);
      var g=ctx.createLinearGradient(-10,-9,10,-2);g.addColorStop(0,p.e);g.addColorStop(0.5,p.b);g.addColorStop(1,p.i);
      ctx.fillStyle=g;ctx.fill();ctx.strokeStyle=p.e;ctx.lineWidth=.7;ctx.stroke();
      ctx.beginPath();ctx.ellipse(0,-1,10,6,0,0,Math.PI);
      var g2=ctx.createLinearGradient(-10,-1,10,5);g2.addColorStop(0,p.b);g2.addColorStop(0.5,p.i);g2.addColorStop(1,p.b);
      ctx.fillStyle=g2;ctx.fill();ctx.strokeStyle=p.e;ctx.lineWidth=.7;ctx.stroke();
      for(var i=-3;i<=3;i++){ctx.beginPath();ctx.moveTo(i*2.5,-2);ctx.lineTo(i*3.5,-8);ctx.strokeStyle='rgba('+hr(p.e)+',0.2)';ctx.lineWidth=.4;ctx.stroke()}
      ctx.beginPath();ctx.moveTo(-10,-2);ctx.lineTo(10,-2);ctx.strokeStyle=p.e;ctx.lineWidth=.8;ctx.stroke();
    }
    function drawConeShell(p){
      ctx.beginPath();ctx.moveTo(0,12);ctx.lineTo(-8,-10);ctx.quadraticCurveTo(0,-14,8,-10);ctx.lineTo(0,12);ctx.closePath();
      var g=ctx.createLinearGradient(-8,0,8,0);g.addColorStop(0,p.e);g.addColorStop(0.4,p.b);g.addColorStop(0.6,p.i);g.addColorStop(1,p.e);
      ctx.fillStyle=g;ctx.fill();ctx.strokeStyle=p.e;ctx.lineWidth=.7;ctx.stroke();
      for(var i=0;i<6;i++){var t=i/5,y=12-t*24,hw=8*(1-t*.85);ctx.beginPath();ctx.moveTo(-hw,y);ctx.lineTo(hw,y);ctx.strokeStyle='rgba('+hr(p.e)+',0.22)';ctx.lineWidth=.5;ctx.stroke()}
      ctx.beginPath();ctx.ellipse(0,-10,3.5,1.8,0,0,6.28);ctx.fillStyle='rgba('+hr(p.i)+',0.5)';ctx.fill();ctx.strokeStyle=p.e;ctx.lineWidth=.5;ctx.stroke();
    }
    function drawFlatShell(p){
      ctx.beginPath();ctx.ellipse(0,0,11,7,0,0,6.28);
      var g=ctx.createRadialGradient(-2,-1,1,0,0,11);g.addColorStop(0,p.i);g.addColorStop(0.5,p.b);g.addColorStop(1,p.e);
      ctx.fillStyle=g;ctx.fill();ctx.strokeStyle=p.e;ctx.lineWidth=.6;ctx.stroke();
      var sp=[[-4,-2,1.8],[3,-1.5,1.5],[-1,2.5,1.3],[5,1,1.2],[-6,.5,1],[1,-3.5,1.1]];
      for(var i=0;i<sp.length;i++){ctx.beginPath();ctx.arc(sp[i][0],sp[i][1],sp[i][2],0,6.28);ctx.fillStyle='rgba('+hr(p.e)+',0.28)';ctx.fill()}
      ctx.beginPath();ctx.moveTo(-10,0);ctx.lineTo(10,0);ctx.strokeStyle='rgba('+hr(p.e)+',0.12)';ctx.lineWidth=.4;ctx.stroke();
    }
    function drawShell(s){
      ctx.save();ctx.translate(s.x,s.y);ctx.rotate(s.an);
      if(s.fl)ctx.scale(-1,1);ctx.scale(s.sc,s.sc);
      switch(s.t){
        case'fan':drawFanShell(s.p);break;case'spiral':drawSpiralShell(s.p);break;
        case'clam':drawClamShell(s.p);break;case'cone':drawConeShell(s.p);break;case'flat':drawFlatShell(s.p);break;
      }
      ctx.restore();
    }

    function spawnWashShell(){
      wShells.push({x:Math.random()*W*.8+W*.1,y:bSY+2,ty:bSY+15+Math.random()*30,vy:0,t:shellTypes[Math.floor(Math.random()*5)],sc:.4+Math.random()*.7,an:(Math.random()-.5)*.6,p:sPals[Math.floor(Math.random()*sPals.length)],fl:Math.random()>.5,done:false});
    }

    var cCols=[{bd:'#8b4513',sh:'#a0522d',lg:'#6b3410',cl:'#b5651d'},{bd:'#6b3a1f',sh:'#7a4428',lg:'#52290f',cl:'#8b5a2b'},{bd:'#9c5a30',sh:'#b06838',lg:'#7a4420',cl:'#c07840'}];

    function initCrabs(){
      crabs=[];
      crabs.push({x:W*.22,by:bSY+(H-bSY)*.3,d:1,sp:.35,wp:0,pt:0,pa:false,cp:0,sc:1,c:cCols[0],fl:0});
      crabs.push({x:W*.7,by:bSY+(H-bSY)*.5,d:-1,sp:.28,wp:Math.PI,pt:2,pa:true,cp:1.5,sc:.72,c:cCols[1],fl:0});
      crabs.push({x:W*.48,by:bSY+(H-bSY)*.7,d:1,sp:.42,wp:Math.PI*1.5,pt:0,pa:false,cp:Math.PI,sc:.58,c:cCols[2],fl:0});
    }

    function drawClaw(ox,oy,op,col,side){
      ctx.save();ctx.translate(ox,oy);
      ctx.strokeStyle=col.lg;ctx.lineWidth=2;ctx.lineCap='round';
      ctx.beginPath();ctx.moveTo(0,0);ctx.lineTo(side*6,-3);ctx.stroke();
      ctx.translate(side*6,-3);
      ctx.beginPath();ctx.moveTo(0,0);ctx.quadraticCurveTo(side*4,-4-op*4,side*7,-2-op*3);ctx.strokeStyle=col.cl;ctx.lineWidth=2.5;ctx.stroke();
      ctx.beginPath();ctx.moveTo(0,0);ctx.quadraticCurveTo(side*4,2+op*3,side*7,1+op*2);ctx.stroke();
      ctx.beginPath();ctx.arc(side*7,-2-op*3,1,0,6.28);ctx.fillStyle=col.cl;ctx.fill();
      ctx.beginPath();ctx.arc(side*7,1+op*2,1,0,6.28);ctx.fill();
      ctx.restore();
    }

    function drawCrab(c){
      ctx.save();ctx.translate(c.x,c.by);ctx.scale(c.d*c.sc,c.sc);
      var col=c.c,co=.3+.25*Math.sin(c.cp);
      ctx.strokeStyle=col.lg;ctx.lineWidth=1.3;ctx.lineCap='round';
      for(var s=-1;s<=1;s+=2){
        for(var i=0;i<3;i++){
          var po=i*Math.PI*.7+(s>0?Math.PI:0);
          var sw=c.pa?0:fs(c.wp+po)*.2;
          var bx=s*6,by=1+i*2.5;
          var mx2=bx+s*7,my=by+5+sw*5;
          var tx=mx2+s*6,ty=my+3+Math.abs(sw)*3;
          ctx.beginPath();ctx.moveTo(bx,by);ctx.quadraticCurveTo(mx2,my-2,mx2,my);ctx.quadraticCurveTo(mx2+s,my+2,tx,ty);ctx.stroke();
        }
      }
      ctx.beginPath();ctx.ellipse(1,2,10,7,0,0,6.28);ctx.fillStyle='rgba(0,0,0,0.12)';ctx.fill();
      ctx.beginPath();ctx.ellipse(0,0,10,7,0,0,6.28);
      var bg=ctx.createRadialGradient(-2,-2,1,0,0,10);bg.addColorStop(0,col.sh);bg.addColorStop(0.7,col.bd);bg.addColorStop(1,col.lg);
      ctx.fillStyle=bg;ctx.fill();ctx.strokeStyle=col.lg;ctx.lineWidth=.8;ctx.stroke();
      var bumps=[[-3,-2],[0,-3],[3,-1.5],[-1,1],[2,1.5],[-4,.5]];
      for(var i=0;i<bumps.length;i++){ctx.beginPath();ctx.arc(bumps[i][0],bumps[i][1],.7,0,6.28);ctx.fillStyle='rgba('+hr(col.sh)+',0.5)';ctx.fill()}
      drawClaw(-10,-2,co,col,-1);drawClaw(10,-2,co,col,1);
      ctx.strokeStyle=col.lg;ctx.lineWidth=1;
      ctx.beginPath();ctx.moveTo(-3,-6);ctx.quadraticCurveTo(-4,-11,-3.5,-12);ctx.stroke();
      ctx.beginPath();ctx.moveTo(3,-6);ctx.quadraticCurveTo(4,-11,3.5,-12);ctx.stroke();
      ctx.beginPath();ctx.arc(-3.5,-12,1.5,0,6.28);ctx.fillStyle='#1a0a00';ctx.fill();
      ctx.beginPath();ctx.arc(-3.5,-12,.5,0,6.28);ctx.fillStyle='rgba(255,255,255,0.65)';ctx.fill();
      ctx.beginPath();ctx.arc(3.5,-12,1.5,0,6.28);ctx.fillStyle='#1a0a00';ctx.fill();
      ctx.beginPath();ctx.arc(3.5,-12,.5,0,6.28);ctx.fillStyle='rgba(255,255,255,0.65)';ctx.fill();
      ctx.restore();
    }

    function drawBottle(cx,cy,sc,an,gl){
      ctx.save();ctx.translate(cx,cy);ctx.rotate(an);ctx.scale(sc,sc);
      if(gl){
        var rg=ctx.createRadialGradient(0,-12,2,0,-12,30);
        rg.addColorStop(0,'rgba(100,180,240,0.12)');rg.addColorStop(1,'rgba(100,180,240,0)');
        ctx.fillStyle=rg;ctx.beginPath();ctx.arc(0,-12,30,0,6.28);ctx.fill();
      }
      var bw=16,bh=28,nw=6,nh=9;
      ctx.beginPath();
      ctx.moveTo(-bw/2,0);ctx.quadraticCurveTo(-bw/2,bh*.38,0,bh*.38);
      ctx.quadraticCurveTo(bw/2,bh*.38,bw/2,0);ctx.lineTo(bw/2,-bh);
      ctx.quadraticCurveTo(bw/2,-bh-5,nw/2,-bh-5);ctx.lineTo(nw/2,-bh-5-nh);
      ctx.lineTo(-nw/2,-bh-5-nh);ctx.lineTo(-nw/2,-bh-5);
      ctx.quadraticCurveTo(-bw/2,-bh-5,-bw/2,-bh);ctx.closePath();
      var gg=ctx.createLinearGradient(-bw/2,0,bw/2,0);
      gg.addColorStop(0,'rgba(95,165,210,0.18)');gg.addColorStop(0.3,'rgba(165,215,240,0.32)');
      gg.addColorStop(0.7,'rgba(130,195,225,0.26)');gg.addColorStop(1,'rgba(85,155,200,0.18)');
      ctx.fillStyle=gg;ctx.fill();ctx.strokeStyle='rgba(175,218,240,0.4)';ctx.lineWidth=.7;ctx.stroke();
      ctx.beginPath();ctx.ellipse(0,-bh*.3,3.5,10,0,0,6.28);ctx.fillStyle='rgba(240,228,195,0.5)';ctx.fill();
      ctx.strokeStyle='rgba(200,188,155,0.25)';ctx.lineWidth=.4;ctx.stroke();
      ctx.fillStyle='#8a7350';rrect(-nw/2+.5,-bh-5-nh-3.5,nw-1,4.5,1);
      ctx.fillStyle='rgba(255,255,255,0.18)';ctx.fillRect(-bw/2+2.5,-bh+3,2.2,bh-7);
      ctx.fillStyle='rgba(255,255,255,0.12)';ctx.fillRect(-nw/2+1,-bh-5-nh,nw-2,nh);
      ctx.restore();
    }

    var waterBottles=[
      {xr:.14,li:2,alive:true},{xr:.36,li:3,alive:true},
      {xr:.56,li:1,alive:true},{xr:.76,li:4,alive:true},{xr:.92,li:3,alive:true}
    ];
    var sandBottles=[
      {xr:.08,yr:.79,an:-.12,sc:1.05,alive:true},{xr:.40,yr:.83,an:.18,sc:.9,alive:true},
      {xr:.65,yr:.77,an:-.22,sc:1.1,alive:true},{xr:.88,yr:.85,an:.08,sc:.85,alive:true}
    ];

    function throwBottle(tx,ty){
      thrownB.push({x:tx,y:ty-120,vx:(Math.random()-.5)*.3,vy:0,state:'fall',li:2+Math.floor(Math.random()*3),timer:0,sc:1,an:0});
    }

    function enterPickMode(){
      if(!waterBottles.some(function(b){return b.alive})&&!sandBottles.some(function(b){return b.alive}))return;
      if(!aCtx)initAudio();
      pickMode=true;
      if($('pickBtn')){
          $('pickBtn').innerHTML='<i class="fa-solid fa-xmark" style="margin-right:6px;font-size:12px"></i>取消拾取';
          $('pickBtn').classList.add('active');
      }
      canvas.style.cursor='crosshair';
      if(hintOn){hintOn=false;if($('hint'))$('hint').classList.add('hide');}
    }
    function exitPickMode(){
      pickMode=false;
      if($('pickBtn')){
          $('pickBtn').innerHTML='<i class="fa-solid fa-hand-pointer" style="margin-right:6px;font-size:12px"></i>拾取漂流瓶';
          $('pickBtn').classList.remove('active');
      }
      canvas.style.cursor='default';
      checkPickable();
    }
    function checkPickable(){
      var has=waterBottles.some(function(b){return b.alive})||sandBottles.some(function(b){return b.alive});
      if($('pickBtn'))$('pickBtn').disabled=!has;
      if(!has&&pickMode)exitPickMode();
    }
    function tryPickAt(cx,cy){
      for(var i=0;i<waterBPos.length;i++){if(dst(cx,cy,waterBPos[i].x,waterBPos[i].y)<waterBPos[i].r)return waterBPos[i]}
      for(var i=0;i<sandBPos.length;i++){if(dst(cx,cy,sandBPos[i].x,sandBPos[i].y)<sandBPos[i].r)return sandBPos[i]}
      return null;
    }
    function doPick(bottle){
      bottle.ref.alive=false;
      pickingAnim={x:bottle.x,y:bottle.y,t:0,wish:wishes[Math.floor(Math.random()*wishes.length)]};
    }
    function drawPicking(dt){
      if(!pickingAnim)return;
      pickingAnim.t+=dt;
      var dur=.7,p=Math.min(pickingAnim.t/dur,1);
      var ease=1-Math.pow(1-p,3);
      if(p>=1){
        showPaper(pickingAnim.wish);
        pickingAnim=null;exitPickMode();checkPickable();return;
      }
      ctx.save();ctx.globalAlpha=1-ease;
      drawBottle(pickingAnim.x,pickingAnim.y-ease*25,1-ease*.6,0,true);
      ctx.restore();
      var rg=ctx.createRadialGradient(pickingAnim.x,pickingAnim.y-10,0,pickingAnim.x,pickingAnim.y-10,30+ease*20);
      rg.addColorStop(0,'rgba(180,220,255,'+(ease*.2).toFixed(3)+')');rg.addColorStop(1,'rgba(180,220,255,0)');
      ctx.fillStyle=rg;ctx.beginPath();ctx.arc(pickingAnim.x,pickingAnim.y-10,30+ease*20,0,6.28);ctx.fill();
    }
    function drawHighlights(t){
      if(!pickMode)return;
      var pulse=.4+.6*fs(t*3.5);
      var all=waterBPos.concat(sandBPos);
      for(var i=0;i<all.length;i++){
        var b=all[i];
        ctx.beginPath();ctx.arc(b.x,b.y,b.r+6,0,6.28);
        ctx.strokeStyle='rgba(180,215,255,'+(pulse*.3).toFixed(3)+')';ctx.lineWidth=1.5;ctx.stroke();
        ctx.beginPath();ctx.arc(b.x,b.y,b.r+6,0,6.28);
        ctx.strokeStyle='rgba(140,190,255,'+(pulse*.1).toFixed(3)+')';ctx.lineWidth=4;ctx.stroke();
      }
    }

    function drawTwinkle(t){
      for(var i=0;i<twStars.length;i++){
        var s=twStars[i];var a=s.b*(.3+.7*fs(t*s.sp+s.ph));
        ctx.beginPath();ctx.arc(s.x,s.y,s.r,0,6.28);
        ctx.fillStyle='rgba(255,255,245,'+a.toFixed(3)+')';ctx.fill();
      }
    }
    function drawMoonImg(){ctx.drawImage(moonOC,mX-moonOC.width/2,mY-moonOC.height/2)}
    function drawCloudsF(t){
      for(var i=0;i<clouds.length;i++){
        var c=clouds[i];c.x+=c.sp;if(c.x>W+120)c.x=-120;
        for(var j=0;j<c.bs.length;j++){
          var b=c.bs[j];ctx.beginPath();ctx.ellipse(c.x+b.ox,c.y+b.oy,b.rx,b.ry,0,0,6.28);
          ctx.fillStyle='rgba(160,175,210,'+c.al.toFixed(4)+')';ctx.fill();
        }
      }
    }
    function updateMeteors(t){
      if(Math.random()<.003&&meteors.length<2){
        meteors.push({x:Math.random()*W*.7+W*.1,y:Math.random()*H*.25,vx:3.5+Math.random()*4,vy:1.5+Math.random()*2.5,life:1,dc:.013+Math.random()*.01,len:50+Math.random()*40});
      }
      for(var i=meteors.length-1;i>=0;i--){
        var m=meteors[i];m.x+=m.vx;m.y+=m.vy;m.life-=m.dc;
        if(m.life<=0){meteors.splice(i,1);continue}
        var ex=m.x-m.vx/5*m.len,ey=m.y-m.vy/5*m.len;
        var g=ctx.createLinearGradient(m.x,m.y,ex,ey);
        g.addColorStop(0,'rgba(255,255,240,'+m.life.toFixed(3)+')');g.addColorStop(1,'rgba(255,255,240,0)');
        ctx.beginPath();ctx.moveTo(m.x,m.y);ctx.lineTo(ex,ey);ctx.strokeStyle=g;ctx.lineWidth=1.5;ctx.stroke();
      }
    }
    function drawIslands(){
      ctx.fillStyle='#080e1e';
      ctx.beginPath();ctx.moveTo(W*.03,hY+2);ctx.quadraticCurveTo(W*.1,hY-28,W*.17,hY-18);ctx.quadraticCurveTo(W*.22,hY-8,W*.26,hY+2);ctx.fill();
      ctx.beginPath();ctx.moveTo(W*.77,hY+2);ctx.quadraticCurveTo(W*.81,hY-22,W*.855,hY-14);ctx.quadraticCurveTo(W*.9,hY-20,W*.94,hY+2);ctx.fill();
      ctx.beginPath();ctx.moveTo(W*.96,hY+2);ctx.quadraticCurveTo(W*.975,hY-9,W*.995,hY+2);ctx.fill();
    }
    function drawLighthouse(t){
      var lx=W*.855,ly=hY-14;
      ctx.fillStyle='#12182e';ctx.fillRect(lx-2.5,ly-16,5,16);
      ctx.beginPath();ctx.arc(lx,ly-16,2.5,0,6.28);ctx.fillStyle='rgba(255,240,200,0.85)';ctx.fill();
      var ba=t*(6.28/28);ctx.save();ctx.translate(lx,ly-16);ctx.rotate(ba);
      var bL=Math.max(W,H)*.7,bw2=.07;
      ctx.beginPath();ctx.moveTo(0,0);ctx.lineTo(bL,-bL*Math.tan(bw2));ctx.lineTo(bL,bL*Math.tan(bw2));ctx.closePath();
      var bg=ctx.createLinearGradient(0,0,bL,0);
      bg.addColorStop(0,'rgba(255,240,200,0.12)');bg.addColorStop(0.25,'rgba(255,235,180,0.04)');bg.addColorStop(1,'rgba(255,230,160,0)');
      ctx.fillStyle=bg;ctx.fill();ctx.restore();
    }
    function drawHorizonGlow(){
      var rg=ctx.createRadialGradient(W/2,hY,0,W/2,hY,W*.5);
      rg.addColorStop(0,'rgba(25,45,80,0.22)');rg.addColorStop(0.4,'rgba(18,32,60,0.1)');rg.addColorStop(1,'rgba(10,18,38,0)');
      ctx.fillStyle=rg;ctx.fillRect(0,hY-H*.08,W,H*.16);
    }
    function drawSeaBase(){
      var g=ctx.createLinearGradient(0,hY,0,bSY);
      g.addColorStop(0,'#070c22');g.addColorStop(0.35,'#0a1338');g.addColorStop(0.7,'#0d1848');g.addColorStop(1,'#101e58');
      ctx.fillStyle=g;ctx.fillRect(0,hY-3,W,bSY-hY+8);
    }
    function drawWavesF(t){
      var sy=bSY+curTide;
      for(var li=0;li<wLayers.length;li++){
        var L=wLayers[li];
        ctx.beginPath();ctx.moveTo(-2,sy+25);
        for(var x=-2;x<=W+2;x+=2)ctx.lineTo(x,wY(x,t,L)+curTide);
        ctx.lineTo(W+2,sy+25);ctx.closePath();
        ctx.fillStyle='rgba('+L.r+','+L.g+','+L.b+','+L.al+')';ctx.fill();
        ctx.beginPath();
        for(var x=-2;x<=W+2;x+=2){var y=wY(x,t,L)+curTide;x===-2?ctx.moveTo(x,y):ctx.lineTo(x,y)}
        ctx.strokeStyle='rgba(70,130,195,'+(L.al*.4).toFixed(3)+')';ctx.lineWidth=.7;ctx.stroke();
      }
      if(Math.random()<.1){ 
        var x=Math.random()*W;
        var y=wY(x,t,wLayers[wLayers.length-1])+curTide;
        var size=2+Math.random()*3;
        var alpha=.3+Math.random()*.2;
        ctx.fillStyle='rgba(200,220,240,'+alpha+')';
        ctx.beginPath();ctx.arc(x,y,size,0,6.28);ctx.fill();
      }
    }
    function drawMoonReflect(t){
      var sy=bSY+curTide;
      for(var i=0;i<REFL_STEPS;i++){
        var f=i/REFL_STEPS,y=hY+f*(sy-hY),w=2+f*28;
        var xo=fs(y*.03+t*.5)*(2+f*7),a=.12*(1-f*.65);
        ctx.fillStyle='rgba(210,215,230,'+a.toFixed(3)+')';ctx.fillRect(mX-w/2+xo,y,w,(sy-hY)/REFL_STEPS+1);
      }
    }
    function drawBioF(t){
      var sy=bSY+curTide;
      for(var i=0;i<bios.length;i++){
        var b=bios[i];var by=b.y+curTide;if(by<hY)continue;
        var a=b.b*(.3+.7*fs(t*b.sp+b.ph));if(a<.05)continue;
        var glow=ctx.createRadialGradient(b.x,by,0,b.x,by,b.r*3);
        glow.addColorStop(0,'rgba(80,220,180,'+a.toFixed(3)+')');glow.addColorStop(1,'rgba(60,200,160,0)');
        ctx.fillStyle=glow;ctx.beginPath();ctx.arc(b.x,by,b.r*3,0,6.28);ctx.fill();
        ctx.beginPath();ctx.arc(b.x,by,b.r,0,6.28);ctx.fillStyle='rgba(120,240,200,'+(a*1.5).toFixed(3)+')';ctx.fill();
      }
    }
    function drawReflsF(t){
      for(var i=0;i<refls.length;i++){
        var r=refls[i];var s=.4+.6*fs(t*r.sp+r.ph);var ox=fs(t*.7+r.ph)*3;
        ctx.fillStyle='rgba(190,215,255,'+(r.a*s).toFixed(3)+')';ctx.fillRect(r.x+ox,r.y,r.w*s,r.h);
      }
    }
    function drawFoamF(t){
      var sy=bSY+curTide;
      for(var x=0;x<W;x+=3){
        var wo=10*fs(x*.009+t*.75)+4*fs(x*.025+t*1.1);
        var y=sy-4+wo+curTide,a=.12+.08*fs(x*.04+t*1.3),w=7+4*fs(x*.02+t*6);
        ctx.fillStyle='rgba(195,215,235,'+a.toFixed(3)+')';ctx.fillRect(x,y,w,1.5);
      }
    }
    function drawBeachF(){
      var sy=bSY+curTide;
      var sg=ctx.createLinearGradient(0,sy-3,0,H);
      sg.addColorStop(0,'#4a4030');sg.addColorStop(0.1,'#5e5240');sg.addColorStop(0.3,'#7a6d55');
      sg.addColorStop(0.6,'#9a8a68');sg.addColorStop(1,'#b09a74');
      ctx.fillStyle=sg;ctx.fillRect(0,sy-3,W,H-sy+3);
      for(var i=0;i<sandGrains.length;i++){
        var g=sandGrains[i];ctx.fillStyle='rgba(175,160,125,'+g.a.toFixed(3)+')';ctx.fillRect(g.x,g.y,g.r,g.r);
      }
    }
    function drawWaterBottlesF(t){
      waterBPos=[];
      for(var i=0;i<waterBottles.length;i++){
        var b=waterBottles[i];if(!b.alive)continue;
        var x=b.xr*W,L=wLayers[b.li],y=wY(x,t,L)+curTide;
        var an=fs(t*.7+b.xr*12)*.1,sc=1+fs(t*1.1+b.xr*6)*.025;
        drawBottle(x,y-2,sc,an,true);
        waterBPos.push({x:x,y:y-2,r:20,ref:b});
      }
    }
    function drawSandBottlesF(){
      sandBPos=[];
      for(var i=0;i<sandBottles.length;i++){
        var b=sandBottles[i];if(!b.alive)continue;
        var x=b.xr*W,y=b.yr*H;
        drawBottle(x,y,b.sc,b.an,false);
        sandBPos.push({x:x,y:y,r:22,ref:b});
      }
    }
    function updateThrown(dt){
      var now=performance.now()/1000;
      for(var i=thrownB.length-1;i>=0;i--){
        var b=thrownB[i];
        if(b.state==='fall'){
          b.vy+=.35;b.y+=b.vy;b.x+=b.vx;
          var surfY=wY(b.x,now,wLayers[b.li])+curTide;
          if(b.y>=surfY-5){
            b.y=surfY-5;b.state='splash';b.timer=0;
            for(var j=0;j<10;j++)splashes.push({x:b.x+(Math.random()-.5)*10,y:surfY,vx:(Math.random()-.5)*2,vy:-2-Math.random()*3,life:1,dc:.02+Math.random()*.015,r:1+Math.random()*1.5});
          }
        }else if(b.state==='splash'){
          b.timer+=dt;if(b.timer>.5)b.state='float';
        }
        if(b.state==='float'){
          var L=wLayers[b.li],y2=wY(b.x,now,L)+curTide;
          b.y=lerp(b.y,y2,.1);b.an=fs(now*.7+b.x*.01)*.08;
        }
        if(b.x<-50||b.x>W+50)thrownB.splice(i,1);
      }
    }
    function drawThrownF(){for(var i=0;i<thrownB.length;i++){var b=thrownB[i];drawBottle(b.x,b.y,b.sc,b.an,b.state!=='fall')}}
    function updateSplashes(dt){
      for(var i=splashes.length-1;i>=0;i--){
        var s=splashes[i];s.vy+=.15;s.x+=s.vx;s.y+=s.vy;s.life-=s.dc;
        if(s.life<=0)splashes.splice(i,1);
      }
    }
    function drawSplashes(){
      for(var i=0;i<splashes.length;i++){
        var s=splashes[i];ctx.beginPath();ctx.arc(s.x,s.y,s.r,0,6.28);
        ctx.fillStyle='rgba(200,220,240,'+(s.life*.6).toFixed(3)+')';ctx.fill();
      }
    }
    function updateRipples(dt){
      for(var i=ripples.length-1;i>=0;i--){
        var r=ripples[i];r.rad+=1.2;r.life-=.018;
        if(r.life<=0)ripples.splice(i,1);
      }
    }
    function drawRipples(){
      for(var i=0;i<ripples.length;i++){
        var r=ripples[i];
        ctx.beginPath();ctx.arc(r.x,r.y,r.rad,0,6.28);
        ctx.strokeStyle='rgba(150,200,240,'+(r.life*.3).toFixed(3)+')';ctx.lineWidth=1;ctx.stroke();
        if(r.rad>10){ctx.beginPath();ctx.arc(r.x,r.y,r.rad*.6,0,6.28);ctx.strokeStyle='rgba(150,200,240,'+(r.life*.15).toFixed(3)+')';ctx.lineWidth=.6;ctx.stroke()}
      }
    }
    function updateWShells(dt){
      for(var i=wShells.length-1;i>=0;i--){
        var s=wShells[i];
        if(!s.done){s.vy+=.3;s.y+=s.vy;if(s.y>=s.ty){s.y=s.ty;s.done=true;shells.push({x:s.x,y:s.y,t:s.t,sc:s.sc,an:s.an,p:s.p,fl:s.fl})}}
      }
      if(Math.random()<.001&&wShells.filter(function(s){return!s.done}).length<2)spawnWashShell();
      if(shells.length>30)shells.splice(0,shells.length-25);
    }
    function drawWShellsF(){for(var i=0;i<wShells.length;i++){var s=wShells[i];if(!s.done)drawShell(s)}}
    function updateCrabsF(dt){
      for(var i=0;i<crabs.length;i++){
        var c=crabs[i];var d=dst(mx,my,c.x,c.by);
        if(d<90&&!c.fl){c.fl=true;c.fs=3+Math.random()*2;c.d=mx<c.x?1:-1;c.pa=false}
        if(c.fl&&d>150)c.fl=false;
        var sp=c.fl?c.fs:c.sp;
        if(c.pa){c.pt-=dt;if(c.pt<=0){c.pa=false;c.d*=(Math.random()>.5?1:-1)}c.cp+=dt*1.5}
        else{c.x+=c.d*sp*60*dt;c.wp+=sp*8*dt;c.cp+=dt*3;
          if(c.x<W*.04||c.x>W*.96){c.pa=true;c.pt=1.5+Math.random()*2;c.d*=-1}
          if(!c.fl&&Math.random()<.001){c.pa=true;c.pt=2+Math.random()*3}
        }
      }
    }

    function initAudio(){
      if(aCtx)return;
      aCtx=new(window.AudioContext||window.webkitAudioContext)();
      var len=aCtx.sampleRate*3,buf=aCtx.createBuffer(2,len,aCtx.sampleRate);
      for(var ch=0;ch<2;ch++){var d=buf.getChannelData(ch);var last=0;for(var i=0;i<len;i++){last=(last+.02*(Math.random()*2-1))/1.02;d[i]=last*3.5}}
      var src=aCtx.createBufferSource();src.buffer=buf;src.loop=true;
      var flt=aCtx.createBiquadFilter();flt.type='lowpass';flt.frequency.value=350;flt.Q.value=.7;
      aGain=aCtx.createGain();aGain.gain.value=0;
      var lfo=aCtx.createOscillator();lfo.frequency.value=.07;var lg=aCtx.createGain();lg.gain.value=.04;lfo.connect(lg);lg.connect(aGain.gain);lfo.start();
      var lfo2=aCtx.createOscillator();lfo2.frequency.value=.15;var lg2=aCtx.createGain();lg2.gain.value=.02;lfo2.connect(lg2);lg2.connect(aGain.gain);lfo2.start();
      src.connect(flt);flt.connect(aGain);aGain.connect(aCtx.destination);src.start();
    }
    function toggleAudio(){
      if(!aCtx)initAudio();if(aCtx.state==='suspended')aCtx.resume();
      aOn=!aOn;aGain.gain.linearRampToValueAtTime(aOn?.12:0,aCtx.currentTime+.3);
      if($('soundBtn')){
        $('soundBtn').innerHTML=aOn?'<i class="fa-solid fa-volume-high"></i>':'<i class="fa-solid fa-volume-xmark"></i>';
        $('soundBtn').classList.toggle('on',aOn);
      }
    }

    function showPaper(text){
      var el=$('paper');if(!el)return;
      if($('paperText'))$('paperText').textContent=text;
      el.className='paper show';
      if(scrollTmr)clearTimeout(scrollTmr);
      scrollTmr=setTimeout(function(){el.className='paper hide';setTimeout(function(){el.className='paper'},350);scrollTmr=null},4500);
    }
    function showPanel(){if($('panel'))$('panel').classList.add('show');if($('wishText'))$('wishText').value='';setTimeout(function(){if($('wishText'))$('wishText').focus()},100)}
    function hidePanel(){if($('panel'))$('panel').classList.remove('show')}

    var lastT=0;
    var isRunning = false;
    var reqId = null;

    function frameWrapper(ts){
      if(!isRunning)return;
      var dt=Math.min((ts-lastT)/1000,.05);lastT=ts;
      var t=ts/1000;
      curTide=fs(t*.02)*8;
      ctx.clearRect(0,0,W,H);

      ctx.drawImage(skyOC,0,0);
      drawTwinkle(t);
      drawMoonImg();
      drawCloudsF(t);
      updateMeteors(t);
      drawIslands();
      drawHorizonGlow();
      drawSeaBase();
      drawWavesF(t);
      drawMoonReflect(t);
      drawBioF(t);
      drawReflsF(t);
      drawLighthouse(t);
      drawWaterBottlesF(t);
      updateThrown(dt);drawThrownF();
      updateSplashes(dt);drawSplashes();
      updateRipples(dt);drawRipples();
      drawFoamF(t);
      drawBeachF();
      for(var i=0;i<shells.length;i++)drawShell(shells[i]);
      drawWShellsF();updateWShells(dt);
      drawSandBottlesF();
      updateCrabsF(dt);
      for(var i=0;i<crabs.length;i++)drawCrab(crabs[i]);
      drawHighlights(t);
      drawPicking(dt);

      reqId=requestAnimationFrame(frameWrapper);
    }

    function init(){
      W=canvas.width=window.innerWidth;H=canvas.height=window.innerHeight;
      hY=H*.44;bSY=H*.71;mX=W*.15;mY=H*.12;
      makeWaves();initSkyCanvas();initMoonCanvas();initTwStars();initClouds();
      initReflections();initBio();initSandGrains();
      initShells();initCrabs();
      thrownB=[];splashes=[];ripples=[];wShells=[];meteors=[];waterBPos=[];sandBPos=[];
      pickMode=false;pickingAnim=null;hintOn=true;
      if($('hint'))$('hint').classList.remove('hide');
      checkPickable();
    }

    var onClick = function(e){
      if(!pickMode)return;
      var bottle=tryPickAt(e.clientX,e.clientY);
      if(bottle)doPick(bottle);else exitPickMode();
    };
    var onTouch = function(e){
      if(!pickMode)return;e.preventDefault();
      var touch=e.touches[0];
      var bottle=tryPickAt(touch.clientX,touch.clientY);
      if(bottle)doPick(bottle);else exitPickMode();
    };
    var onMove = function(e){
      mx=e.clientX;my=e.clientY;
      var sy=bSY+curTide;var onW=(my>hY&&my<sy+10);
      if(pickMode){
        var b=tryPickAt(mx,my);
        canvas.style.cursor=b?'pointer':'crosshair';
      }else{
        canvas.style.cursor=onW?'pointer':'default';
        if(onW&&Math.random()<.12)ripples.push({x:mx,y:my,rad:1,life:.6});
      }
    };
    var onPickBtn = function(){if(pickMode)exitPickMode();else enterPickMode()};
    var onWishBtn = function(){showPanel()};
    var onPanelBg = function(){hidePanel()};
    var onCancelBtn = function(){hidePanel()};
    var onSendBtn = function(){
      var txt=$('wishText')?$('wishText').value.trim():'';hidePanel();if(!txt)return;
      if(!aCtx)initAudio();
      var tx=W*.3+Math.random()*W*.4,ty=hY+(bSY-hY)*.3+Math.random()*(bSY-hY)*.35;
      throwBottle(tx,ty);
      showPaper('"'+txt+'" 已随浪远去...');
    };
    var onSoundBtn = function(){toggleAudio()};
    var onResize = function(){init()};

    return {
      mount: function() {
        isRunning = true;
        init();
        canvas.addEventListener('click', onClick);
        canvas.addEventListener('touchstart', onTouch, {passive:false});
        canvas.addEventListener('mousemove', onMove);
        window.addEventListener('resize', onResize);

        if($('pickBtn')) $('pickBtn').addEventListener('click', onPickBtn);
        if($('wishBtn')) $('wishBtn').addEventListener('click', onWishBtn);
        if($('panelBg')) $('panelBg').addEventListener('click', onPanelBg);
        if($('cancelBtn')) $('cancelBtn').addEventListener('click', onCancelBtn);
        if($('sendBtn')) $('sendBtn').addEventListener('click', onSendBtn);
        if($('soundBtn')) $('soundBtn').addEventListener('click', onSoundBtn);

        lastT = performance.now();
        reqId = requestAnimationFrame(frameWrapper);
      },
      unmount: function() {
        isRunning = false;
        if (reqId) cancelAnimationFrame(reqId);
        canvas.removeEventListener('click', onClick);
        canvas.removeEventListener('touchstart', onTouch);
        canvas.removeEventListener('mousemove', onMove);
        window.removeEventListener('resize', onResize);

        if($('pickBtn')) $('pickBtn').removeEventListener('click', onPickBtn);
        if($('wishBtn')) $('wishBtn').removeEventListener('click', onWishBtn);
        if($('panelBg')) $('panelBg').removeEventListener('click', onPanelBg);
        if($('cancelBtn')) $('cancelBtn').removeEventListener('click', onCancelBtn);
        if($('sendBtn')) $('sendBtn').removeEventListener('click', onSendBtn);
        if($('soundBtn')) $('soundBtn').removeEventListener('click', onSoundBtn);

        if (aCtx && aCtx.state !== 'suspended') {
            aCtx.suspend();
            aOn = false;
            var btn = $('soundBtn');
            if (btn) {
                btn.classList.remove('on');
                btn.innerHTML = '<i class="fa-solid fa-volume-xmark"></i>';
            }
        }
      }
    };
}