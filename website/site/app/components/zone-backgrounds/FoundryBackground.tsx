"use client";

import { motion } from "framer-motion";
import { useParallax } from "../../hooks/useParallax";

export default function FoundryBackground({ altitude }: { altitude: number }) {
  const bgParallax = useParallax(0.3);
  const midParallax = useParallax(0.18);
  const foreParallax = useParallax(0.08);

  return (
    <div className="absolute inset-0 overflow-hidden bg-[#050102]">
      {/* 1. Base Gradient with Parallax */}
      <motion.div
        className="absolute inset-0"
        style={{
          background: "linear-gradient(180deg, #080203 0%, #2c0a0c 50%, #080203 100%)",
          y: bgParallax,
        }}
      />

      {/* 2. Background Scaffolding & Pillars (Deep Purple-Gray Silhouette) */}
      <motion.div
        className="absolute inset-0 opacity-20 pointer-events-none"
        style={{ y: midParallax }}
      >
        <svg viewBox="0 0 1000 1000" className="w-full h-full text-[#1e1014] fill-current">
          {/* Vertical steel beams */}
          <rect x="150" y="0" width="40" height="1000" />
          <rect x="550" y="0" width="30" height="1000" />
          <rect x="850" y="0" width="50" height="1000" />
          {/* Diagonal braces */}
          <line x1="150" y1="200" x2="550" y2="400" stroke="currentColor" strokeWidth="15" />
          <line x1="550" y1="200" x2="150" y2="400" stroke="currentColor" strokeWidth="15" />
          <line x1="550" y1="600" x2="850" y2="800" stroke="currentColor" strokeWidth="15" />
          <line x1="850" y1="600" x2="550" y2="800" stroke="currentColor" strokeWidth="15" />
          {/* Huge structural ring / gear silhouette */}
          <circle cx="500" cy="500" r="180" fill="none" stroke="currentColor" strokeWidth="30" strokeDasharray="40 20" />
        </svg>
      </motion.div>

      {/* 3. Floating Amber Sparks / Embers */}
      <div className="absolute inset-0 opacity-40 pointer-events-none">
        {[...Array(12)].map((_, i) => {
          const size = 2 + (i % 3) * 1.5;
          const left = 5 + (i * 8.3) % 90;
          const delay = i * 0.4;
          const duration = 4 + (i % 4) * 2;
          return (
            <motion.div
              key={i}
              className="absolute rounded-full bg-gradient-to-t from-amber-500 to-red-500 shadow-[0_0_10px_#f59e0b]"
              style={{
                width: size,
                height: size,
                left: `${left}%`,
                bottom: "-5%",
              }}
              animate={{
                y: [-50, -1200],
                x: [0, Math.sin(i) * 60],
                opacity: [0, 0.8, 0.8, 0],
              }}
              transition={{
                duration: duration,
                repeat: Infinity,
                delay: delay,
                ease: "easeOut",
              }}
            />
          );
        })}
      </div>

      {/* 4. Foreground Scaffolding & Support Framework */}
      <motion.div
        className="absolute inset-0 opacity-30 pointer-events-none"
        style={{ y: foreParallax }}
      >
        <svg viewBox="0 0 1000 1000" className="w-full h-full text-[#0d0406] fill-current">
          {/* Massive heavy columns */}
          <rect x="50" y="0" width="80" height="1000" />
          <rect x="900" y="0" width="80" height="1000" />
          {/* Heavy horizontal lintel */}
          <rect x="0" y="100" width="1000" height="40" />
          <rect x="0" y="700" width="1000" height="50" />
          {/* Girders detail */}
          <line x1="50" y1="100" x2="900" y2="700" stroke="currentColor" strokeWidth="25" />
          <line x1="900" y1="100" x2="50" y2="700" stroke="currentColor" strokeWidth="25" />
        </svg>
      </motion.div>

      {/* 5. Laser Safety Grid Gridline (Horizontal glow lines) */}
      <div className="absolute inset-0 opacity-20 pointer-events-none">
        <motion.div 
          className="absolute w-full h-[2px] bg-red-500 shadow-[0_0_8px_#ef4444]"
          style={{ top: "30%" }}
          animate={{ opacity: [0.3, 0.6, 0.3] }}
          transition={{ duration: 3, repeat: Infinity }}
        />
        <motion.div 
          className="absolute w-full h-[2px] bg-red-500 shadow-[0_0_8px_#ef4444]"
          style={{ top: "75%" }}
          animate={{ opacity: [0.5, 0.8, 0.5] }}
          transition={{ duration: 4, repeat: Infinity, delay: 1.5 }}
        />
      </div>

      {/* 6. Dark Vignette */}
      <div
        className="absolute inset-0"
        style={{
          background: "radial-gradient(ellipse at center, transparent 20%, rgba(0,0,0,0.92) 100%)",
        }}
      />
    </div>
  );
}
