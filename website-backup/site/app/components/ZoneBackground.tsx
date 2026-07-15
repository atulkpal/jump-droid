"use client";

import { motion } from "framer-motion";
import { useParallax } from "../hooks/useParallax";

const ZONES = [
  { name: "Earth", threshold: 0, bg: "#0a0a12", mid: "#1a1a2e" },
  { name: "Cloud Layer", threshold: 500, bg: "#0a0a18", mid: "#1a0a2e" },
  { name: "Upper Atmosphere", threshold: 1500, bg: "#080510", mid: "#1f0f3a" },
  { name: "Orbit", threshold: 4000, bg: "#050308", mid: "#150a20" },
  { name: "Deep Space", threshold: 8000, bg: "#020205", mid: "#0a0510" },
  { name: "The Void", threshold: 15000, bg: "#010103", mid: "#050308" },
];

const TOTAL_ALT = 16000;

interface ZoneBackgroundProps {
  altitude: number;
}

export default function ZoneBackground({ altitude }: ZoneBackgroundProps) {
  const bgParallax = useParallax(0.3);
  const starParallax = useParallax(0.15);

  let zone = ZONES[0];
  for (const z of ZONES) { if (altitude >= z.threshold) zone = z; }

  let zoneIdx = 0;
  for (let i = ZONES.length - 1; i >= 0; i--) {
    if (altitude >= ZONES[i].threshold) { zoneIdx = i; break; }
  }
  const cur = ZONES[zoneIdx];
  const nxt = ZONES[Math.min(zoneIdx + 1, ZONES.length - 1)];
  const zt = (altitude - cur.threshold) / Math.max(nxt.threshold - cur.threshold, 1);

  const lerpColor = (a: string, b: string, t: number) => {
    const p = (c: string) => [parseInt(c.slice(1,3),16), parseInt(c.slice(3,5),16), parseInt(c.slice(5,7),16)];
    const [r1,g1,b1] = p(a), [r2,g2,b2] = p(b);
    return `rgb(${Math.round(r1+(r2-r1)*t)},${Math.round(g1+(g2-g1)*t)},${Math.round(b1+(b2-b1)*t)})`;
  };

  const bgColor = lerpColor(cur.bg, nxt.bg, zt);
  const midColor = lerpColor(cur.mid, nxt.mid, zt);

  return (
    <div className="fixed inset-0 z-0 pointer-events-none">
      {/* Main background with parallax */}
      <motion.div
        className="absolute inset-0"
        style={{
          background: `linear-gradient(180deg, ${bgColor} 0%, ${midColor} 50%, ${bgColor} 100%)`,
          y: bgParallax,
        }}
      />

      {/* Stars — optimized, fewer gradients */}
      <motion.div
        className="absolute inset-0 opacity-40"
        style={{
          backgroundImage: `radial-gradient(1px 1px at 20% 30%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1.5px 1.5px at 50% 70%, rgba(255,255,255,0.4), transparent),
            radial-gradient(1px 1px at 80% 20%, rgba(255,255,255,0.6), transparent)`,
          y: starParallax,
        }}
      />

      {/* Vignette overlay */}
      <div
        className="absolute inset-0"
        style={{
          background: "radial-gradient(ellipse at center, transparent 30%, rgba(0,0,0,0.9) 100%)",
        }}
      />
    </div>
  );
}