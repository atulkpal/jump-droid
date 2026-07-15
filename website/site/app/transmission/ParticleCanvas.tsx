"use client";

import { useMemo, useRef, useEffect } from "react";

const PARTICLE_COUNT = 60;

const PALETTE = [
  { r: 0, g: 229, b: 255 },   // cyan
  { r: 213, g: 0, b: 249 },   // purple
  { r: 255, g: 215, b: 0 },   // gold
];

function lerpColor(c1: { r: number; g: number; b: number }, c2: { r: number; g: number; b: number }, t: number) {
  return {
    r: Math.round(c1.r + (c2.r - c1.r) * t),
    g: Math.round(c1.g + (c2.g - c1.g) * t),
    b: Math.round(c1.b + (c2.b - c1.b) * t),
  };
}

function samplePalette(cycle: number): { r: number; g: number; b: number } {
  const len = PALETTE.length;
  const idx = Math.floor(cycle) % len;
  const lerp = cycle - Math.floor(cycle);
  const c1 = PALETTE[idx];
  const c2 = PALETTE[(idx + 1) % len];
  return lerpColor(c1, c2, lerp);
}

function generateParticles() {
  return Array.from({ length: PARTICLE_COUNT }, () => ({
    x: Math.random() * 100,
    y: Math.random() * 100,
    size: Math.random() * 2 + 0.5,
    speed: 0.3 + Math.random() * 0.7,
    phase: Math.random() * Math.PI * 2,
    hueOffset: Math.random(),
  }));
}

function generateWaveformPath(width: number, height: number, amplitude: number, frequency: number, phase: number): string {
  const points: string[] = [];
  const steps = Math.floor(width / 3);
  for (let i = 0; i <= steps; i++) {
    const x = (i / steps) * width;
    const y = height / 2 + Math.sin((i / steps) * frequency * Math.PI * 2 + phase) * amplitude;
    points.push(`${i === 0 ? "M" : "L"}${x.toFixed(1)},${y.toFixed(1)}`);
  }
  return points.join("");
}

export default function ParticleCanvas({ strength }: { strength: number }) {
  const particles = useMemo(() => generateParticles(), []);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const phaseRef = useRef(0);
  const burstTimerRef = useRef(0);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animId: number;

    const resize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };
    resize();
    window.addEventListener("resize", resize);

    const draw = () => {
      phaseRef.current += 0.02;
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const cycleSpeed = 0.006 + strength * 0.014;
      const cycle = (phaseRef.current * cycleSpeed) % PALETTE.length;
      const color = samplePalette(cycle);

      const amp = 8 + strength * 16;
      const freq = 1.5 + strength * 2;
      const alpha = 0.08 + strength * 0.2;

      // Spectral burst lines
      burstTimerRef.current += 1;
      if (Math.random() < 0.008 + strength * 0.025) {
        const burstCycle = (phaseRef.current * (0.006 + Math.random() * 0.02)) % PALETTE.length;
        const burstColor = samplePalette(burstCycle);
        const burstY = Math.random() * canvas.height;
        ctx.strokeStyle = `rgba(${burstColor.r},${burstColor.g},${burstColor.b},${0.06 + strength * 0.25})`;
        ctx.lineWidth = Math.random() * 2 + 0.5;
        ctx.beginPath();
        ctx.moveTo(0, burstY);
        for (let x = 0; x <= canvas.width; x += 20) {
          ctx.lineTo(x, burstY + Math.sin(x * 0.01 + burstCycle) * 3);
        }
        ctx.stroke();
      }

      // Waveform
      const complementCycle = (cycle + 1.5) % PALETTE.length;
      const compColor = samplePalette(complementCycle);
      ctx.strokeStyle = `rgba(${color.r},${color.g},${color.b},${alpha})`;
      ctx.lineWidth = 1;
      ctx.beginPath();
      const path = generateWaveformPath(canvas.width, canvas.height * 0.5, amp, freq, phaseRef.current);
      const segments = path.split(/(?=[ML])/);
      for (const seg of segments) {
        const cmd = seg[0];
        const coords = seg.slice(1).split(",");
        if (cmd === "M" && coords.length >= 2) {
          ctx.moveTo(parseFloat(coords[0]), canvas.height - parseFloat(coords[1]));
        } else if (cmd === "L" && coords.length >= 2) {
          ctx.lineTo(parseFloat(coords[0]), canvas.height - parseFloat(coords[1]));
        }
      }
      ctx.stroke();

      // Second waveform (inverse, complement color)
      const alpha2 = 0.04 + strength * 0.1;
      ctx.strokeStyle = `rgba(${compColor.r},${compColor.g},${compColor.b},${alpha2})`;
      ctx.lineWidth = 0.5;
      ctx.beginPath();
      const path2 = generateWaveformPath(canvas.width, canvas.height * 0.3, amp * 0.6, freq * 1.5, phaseRef.current + 1);
      const segments2 = path2.split(/(?=[ML])/);
      for (const seg of segments2) {
        const cmd = seg[0];
        const coords = seg.slice(1).split(",");
        if (cmd === "M" && coords.length >= 2) {
          ctx.moveTo(parseFloat(coords[0]), canvas.height - parseFloat(coords[1]) - 20);
        } else if (cmd === "L" && coords.length >= 2) {
          ctx.lineTo(parseFloat(coords[0]), canvas.height - parseFloat(coords[1]) - 20);
        }
      }
      ctx.stroke();

      // Particles with individual hue offsets
      for (const p of particles) {
        const px = (p.x / 100) * canvas.width;
        const py = (p.y / 100) * canvas.height;
        const flicker = Math.sin(phaseRef.current * p.speed + p.phase) * 0.5 + 0.5;
        const opacity = flicker * (0.15 + strength * 0.5);

        const particleCycle = (cycle + p.hueOffset) % PALETTE.length;
        const pc = samplePalette(particleCycle);
        ctx.fillStyle = `rgba(${pc.r},${pc.g},${pc.b},${opacity})`;
        ctx.beginPath();
        ctx.arc(px, py, p.size, 0, Math.PI * 2);
        ctx.fill();
      }

      animId = requestAnimationFrame(draw);
    };
    draw();

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", resize);
    };
  }, [strength, particles]);

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 z-0 pointer-events-none"
      aria-hidden="true"
    />
  );
}
