"use client";

import { motion } from "framer-motion";
import { useParallax } from "../../hooks/useParallax";

export default function ChronoRiftBackground({ altitude }: { altitude: number }) {
  const bgParallax = useParallax(0.3);
  const midParallax = useParallax(0.18);
  const foreParallax = useParallax(0.08);

  return (
    <div className="absolute inset-0 overflow-hidden bg-[#0c0211]">
      {/* 1. Base Gradient with Parallax */}
      <motion.div
        className="absolute inset-0"
        style={{
          background: "linear-gradient(180deg, #180325 0%, #031d25 50%, #0c0211 100%)",
          y: bgParallax,
        }}
      />

      {/* 2. Abstract Time Rings & Coordinates (Midground Parallax) */}
      <motion.div
        className="absolute inset-0 opacity-20 pointer-events-none"
        style={{ y: midParallax }}
      >
        <svg viewBox="0 0 1000 1000" className="w-full h-full text-cyan-500 fill-none stroke-current">
          {/* Warp Grid / concentric circles */}
          <circle cx="500" cy="500" r="150" strokeWidth="2" strokeDasharray="5 10" />
          <circle cx="500" cy="500" r="280" strokeWidth="1" />
          <circle cx="500" cy="500" r="420" strokeWidth="3" strokeDasharray="30 15" />
          
          {/* Time markers / Clock divisions */}
          {Array.from({ length: 12 }).map((_, i) => {
            const angle = (i * 30 * Math.PI) / 180;
            const x1 = 500 + Math.cos(angle) * 280;
            const y1 = 500 + Math.sin(angle) * 280;
            const x2 = 500 + Math.cos(angle) * 310;
            const y2 = 500 + Math.sin(angle) * 310;
            return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} strokeWidth="2" />;
          })}

          {/* Clock Hands (Fixed but stylized) */}
          <line x1="500" y1="500" x2="680" y2="400" strokeWidth="4" strokeLinecap="round" />
          <line x1="500" y1="500" x2="420" y2="650" strokeWidth="6" strokeLinecap="round" strokeDasharray="10 5" />
        </svg>
      </motion.div>

      {/* 3. Glitch Static Lines (Floating & flicking) */}
      <div className="absolute inset-0 opacity-30 pointer-events-none">
        {[...Array(6)].map((_, i) => {
          const top = 10 + (i * 18) % 80;
          const height = 1 + (i % 3) * 2;
          const delay = i * 0.7;
          return (
            <motion.div
              key={i}
              className="absolute w-full bg-cyan-400 shadow-[0_0_8px_#00e5ff]"
              style={{
                top: `${top}%`,
                height: height,
              }}
              animate={{
                opacity: [0, 0.8, 0, 0.4, 0, 0.9, 0],
                x: [0, -10, 10, 0],
                scaleY: [1, 2, 1],
              }}
              transition={{
                duration: 4.5,
                repeat: Infinity,
                delay: delay,
                ease: "easeInOut",
              }}
            />
          );
        })}
      </div>

      {/* 4. Falling Time Orbs / Digital Dust */}
      <div className="absolute inset-0 opacity-40 pointer-events-none">
        {[...Array(10)].map((_, i) => {
          const size = 3 + (i % 3) * 2;
          const left = 10 + (i * 11) % 80;
          const delay = i * 0.5;
          const duration = 5 + (i % 3) * 3;
          return (
            <motion.div
              key={i}
              className="absolute rounded-full bg-purple-400 shadow-[0_0_8px_#d500f9]"
              style={{
                width: size,
                height: size,
                left: `${left}%`,
                top: "-5%",
              }}
              animate={{
                y: [-50, 1200],
                x: [0, Math.sin(i) * 40],
                opacity: [0, 0.7, 0.7, 0],
              }}
              transition={{
                duration: duration,
                repeat: Infinity,
                delay: delay,
                ease: "linear",
              }}
            />
          );
        })}
      </div>

      {/* 5. Foreground Scanline Ripple Grid (Slow horizontal distortion) */}
      <motion.div
        className="absolute inset-0 opacity-20 pointer-events-none"
        style={{ y: foreParallax }}
      >
        <svg viewBox="0 0 1000 1000" className="w-full h-full text-purple-600 fill-none stroke-current">
          {/* Broken grid lines */}
          <path d="M 0,200 L 1000,220 M 0,400 L 1000,380 M 0,600 L 1000,610 M 0,800 L 1000,790" strokeWidth="2" strokeDasharray="40 10 5 10" />
          <path d="M 200,0 L 220,1000 M 400,0 L 380,1000 M 600,0 L 610,1000 M 800,0 L 790,1000" strokeWidth="2" strokeDasharray="30 20" />
        </svg>
      </motion.div>

      {/* 6. Dark Vignette */}
      <div
        className="absolute inset-0"
        style={{
          background: "radial-gradient(ellipse at center, transparent 30%, rgba(0,0,0,0.95) 100%)",
        }}
      />
    </div>
  );
}
