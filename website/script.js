const canvas = document.querySelector("#ascent-field");
const ctx = canvas.getContext("2d");
const altitudeValue = document.querySelector("#altitude-value");
const zoneValue = document.querySelector("#zone-value");
const fuelMeter = document.querySelector("#fuel-meter");
const heatMeter = document.querySelector("#heat-meter");
const shieldMeter = document.querySelector("#shield-meter");
const hullMeter = document.querySelector("#hull-meter");

const zoneData = {
  earth: { label: "EARTH", altitude: 0, color: "#00e676", sky: ["#1a237e", "#2196f3", "#bbdefb"], density: 0.35 },
  cloud: { label: "CLOUD LAYER", altitude: 500, color: "#00e5ff", sky: ["#42a5f5", "#90caf9", "#0d001a"], density: 0.52 },
  atmosphere: { label: "UPPER ATMOSPHERE", altitude: 1500, color: "#d500f9", sky: ["#0d001a", "#1a0033", "#311b92"], density: 0.74 },
  orbit: { label: "ORBIT", altitude: 4000, color: "#ffd700", sky: ["#000411", "#0d001a", "#000000"], density: 1.0 },
  space: { label: "DEEP SPACE", altitude: 8000, color: "#8b6dff", sky: ["#000000", "#050010", "#000000"], density: 1.22 },
  void: { label: "THE VOID", altitude: 15000, color: "#ff1744", sky: ["#000000", "#050005", "#000000"], density: 1.45 }
};

let activeZone = "earth";
let targetAltitude = 0;
let shownAltitude = 0;
let width = 0;
let height = 0;
let stars = [];
let structures = [];
let lastTime = 0;
let simTime = 0;

function resize() {
  const ratio = window.devicePixelRatio || 1;
  width = window.innerWidth;
  height = window.innerHeight;
  canvas.width = Math.floor(width * ratio);
  canvas.height = Math.floor(height * ratio);
  canvas.style.width = `${width}px`;
  canvas.style.height = `${height}px`;
  ctx.setTransform(ratio, 0, 0, ratio, 0, 0);

  stars = Array.from({ length: Math.min(220, Math.floor(width * height / 6000)) }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    size: Math.random() * 1.8 + 0.4,
    speed: Math.random() * 18 + 7,
    alpha: Math.random() * 0.55 + 0.16,
    phase: Math.random() * Math.PI * 2
  }));

  structures = Array.from({ length: 12 }, (_, index) => ({
    x: Math.random() * width,
    y: Math.random() * height,
    w: 38 + Math.random() * 120,
    h: 8 + Math.random() * 24,
    spin: Math.random() * Math.PI,
    drift: Math.random() * 0.5 + 0.2,
    type: index % 4
  }));
}

function updateActiveZone(zone) {
  if (!zoneData[zone] || activeZone === zone) return;
  activeZone = zone;
  document.body.className = `zone-${zone}`;
  targetAltitude = zoneData[zone].altitude;
}

const observedSections = document.querySelectorAll("[data-zone]");
const observer = new IntersectionObserver(
  (entries) => {
    const visible = entries
      .filter((entry) => entry.isIntersecting)
      .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];

    if (visible) updateActiveZone(visible.target.dataset.zone);
  },
  { threshold: [0.28, 0.45, 0.62] }
);

observedSections.forEach((section) => observer.observe(section));

function drawRocket(x, y, tilt, flame, zone) {
  ctx.save();
  ctx.translate(x, y);
  ctx.rotate(tilt);

  const pulse = 0.7 + Math.sin(simTime * 4) * 0.12;
  ctx.strokeStyle = zone === "void" ? "rgba(255, 23, 68, 0.55)" : "rgba(0, 229, 255, 0.55)";
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(0, 0, 58 * pulse, 0, Math.PI * 2);
  ctx.stroke();

  const flameGradient = ctx.createLinearGradient(0, 28, 0, 95);
  flameGradient.addColorStop(0, `rgba(255, 215, 0, ${0.85 * flame})`);
  flameGradient.addColorStop(0.44, `rgba(255, 23, 68, ${0.58 * flame})`);
  flameGradient.addColorStop(1, "rgba(255, 23, 68, 0)");
  ctx.fillStyle = flameGradient;
  ctx.beginPath();
  ctx.moveTo(-10, 25);
  ctx.quadraticCurveTo(0, 72 + Math.sin(simTime * 15) * 11, 10, 25);
  ctx.closePath();
  ctx.fill();

  ctx.fillStyle = zone === "space" ? "#cfd4ff" : "#ffffff";
  ctx.fillRect(-13, -18, 26, 48);

  ctx.fillStyle = "#2d3138";
  ctx.beginPath();
  ctx.moveTo(-13, -18);
  ctx.lineTo(0, -42);
  ctx.lineTo(13, -18);
  ctx.closePath();
  ctx.fill();

  ctx.fillStyle = "#00e5ff";
  ctx.beginPath();
  ctx.arc(0, -4, 6.5, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = "#ff1744";
  ctx.beginPath();
  ctx.moveTo(-13, 12);
  ctx.lineTo(-24, 31);
  ctx.lineTo(-13, 31);
  ctx.closePath();
  ctx.fill();
  ctx.beginPath();
  ctx.moveTo(13, 12);
  ctx.lineTo(24, 31);
  ctx.lineTo(13, 31);
  ctx.closePath();
  ctx.fill();
  ctx.restore();
}

function drawStructure(obj, zoneInfo) {
  const y = (obj.y + simTime * 24 * obj.drift) % (height + 120) - 60;
  const x = (obj.x + Math.sin(simTime * obj.drift) * 18 + width) % width;
  const active = activeZone === "orbit" || activeZone === "space" || activeZone === "void";
  if (!active && obj.type > 0) return;

  ctx.save();
  ctx.translate(x, y);
  ctx.rotate(obj.spin + simTime * 0.08 * obj.drift);
  ctx.globalAlpha = active ? 0.34 : 0.18;
  ctx.strokeStyle = zoneInfo.color;
  ctx.fillStyle = "rgba(255,255,255,0.16)";
  ctx.shadowColor = zoneInfo.color;
  ctx.shadowBlur = activeZone === "void" ? 18 : 8;

  if (activeZone === "void" && obj.type === 3) {
    ctx.globalAlpha = 0.28;
    for (let i = 0; i < 4; i += 1) {
      ctx.rotate(Math.PI / 4);
      ctx.strokeRect(-obj.w / 2, -obj.w / 2, obj.w, obj.w);
    }
  } else if (obj.type === 2) {
    ctx.beginPath();
    ctx.arc(0, 0, obj.w * 0.28, 0, Math.PI * 2);
    ctx.stroke();
    ctx.fillRect(-obj.w / 2, -3, obj.w, 6);
  } else {
    ctx.fillRect(-obj.w / 2, -obj.h / 2, obj.w, obj.h);
    ctx.strokeRect(-obj.w / 2, -obj.h / 2, obj.w, obj.h);
  }

  ctx.restore();
}

function drawBackground(zoneInfo) {
  const gradient = ctx.createLinearGradient(0, 0, 0, height);
  gradient.addColorStop(0, zoneInfo.sky[0]);
  gradient.addColorStop(0.52, zoneInfo.sky[1]);
  gradient.addColorStop(1, zoneInfo.sky[2]);
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, width, height);

  if (activeZone === "cloud") {
    ctx.fillStyle = "rgba(255,255,255,0.08)";
    for (let i = 0; i < 7; i += 1) {
      const x = (i * width * 0.2 + simTime * 16) % (width + 220) - 110;
      const y = height * (0.18 + (i % 4) * 0.15);
      ctx.beginPath();
      ctx.ellipse(x, y, 140, 26, 0, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  if (activeZone === "atmosphere" || activeZone === "space") {
    const nebula = ctx.createRadialGradient(width * 0.74, height * 0.25, 0, width * 0.74, height * 0.25, width * 0.55);
    nebula.addColorStop(0, "rgba(213, 0, 249, 0.16)");
    nebula.addColorStop(1, "rgba(213, 0, 249, 0)");
    ctx.fillStyle = nebula;
    ctx.fillRect(0, 0, width, height);
  }

  if (activeZone === "orbit") {
    ctx.strokeStyle = "rgba(33, 150, 243, 0.22)";
    ctx.lineWidth = Math.max(1, width * 0.002);
    ctx.beginPath();
    ctx.arc(width * 0.5, height + width * 1.65, width * 1.9, 0, Math.PI * 2);
    ctx.stroke();
  }

  if (activeZone === "void") {
    const distortion = ctx.createRadialGradient(width * 0.62, height * 0.42, 0, width * 0.62, height * 0.42, width * 0.48);
    distortion.addColorStop(0, "rgba(255, 23, 68, 0.18)");
    distortion.addColorStop(0.28, "rgba(213, 0, 249, 0.08)");
    distortion.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = distortion;
    ctx.fillRect(0, 0, width, height);
  }
}

function render(time) {
  const dt = Math.min(0.035, (time - lastTime) / 1000 || 0);
  lastTime = time;
  simTime += dt;

  const zoneInfo = zoneData[activeZone];
  shownAltitude += (targetAltitude - shownAltitude) * Math.min(1, dt * 2.5);
  const altitudeNoise = activeZone === "void" ? Math.sin(simTime * 10) * 13 : Math.sin(simTime * 1.4) * 4;
  const telemetryAltitude = Math.max(0, Math.round(shownAltitude + altitudeNoise));

  altitudeValue.textContent = telemetryAltitude.toString().padStart(5, "0");
  zoneValue.textContent = zoneInfo.label;
  zoneValue.style.color = zoneInfo.color;
  fuelMeter.value = Math.max(8, 88 - zoneInfo.density * 22 + Math.sin(simTime * 0.8) * 9);
  heatMeter.value = Math.min(98, 16 + zoneInfo.density * 34 + Math.sin(simTime * 1.1) * 12);
  shieldMeter.value = Math.max(10, 85 - zoneInfo.density * 25 + Math.sin(simTime * 0.6 + 2) * 11);
  hullMeter.value = Math.max(18, 96 - zoneInfo.density * 15 + Math.sin(simTime * 0.4 + 1) * 5);

  drawBackground(zoneInfo);

  for (const star of stars) {
    star.y += star.speed * dt * zoneInfo.density;
    if (star.y > height) {
      star.y = -4;
      star.x = Math.random() * width;
    }
    const twinkle = 0.72 + Math.sin(simTime * 2 + star.phase) * 0.28;
    ctx.fillStyle = `rgba(255, 255, 255, ${star.alpha * zoneInfo.density * twinkle})`;
    ctx.fillRect(star.x, star.y, star.size, star.size);
  }

  for (const structure of structures) {
    drawStructure(structure, zoneInfo);
  }

  const rocketX = width * 0.5 + Math.sin(simTime * 1.2) * Math.min(70, width * 0.09);
  const rocketY = height * 0.58 + Math.sin(simTime * 2.1) * 13;
  const tilt = Math.sin(simTime * 1.2) * 0.16;
  const flame = 0.72 + Math.sin(simTime * 14) * 0.22;
  drawRocket(rocketX, rocketY, tilt, flame, activeZone);

  if (activeZone === "void") {
    ctx.save();
    ctx.globalAlpha = 0.2 + Math.sin(simTime * 5) * 0.08;
    ctx.strokeStyle = "#ff1744";
    ctx.lineWidth = 1;
    for (let r = 120; r < Math.max(width, height) * 0.75; r += 92) {
      ctx.beginPath();
      ctx.arc(width * 0.62, height * 0.42, r + Math.sin(simTime * 2 + r) * 5, 0, Math.PI * 2);
      ctx.stroke();
    }
    ctx.restore();
  }

  requestAnimationFrame(render);
}

resize();
targetAltitude = zoneData.earth.altitude;
window.addEventListener("resize", resize);
requestAnimationFrame(render);
