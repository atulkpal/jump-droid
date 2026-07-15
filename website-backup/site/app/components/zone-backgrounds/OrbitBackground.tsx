"use client";

import { motion } from "framer-motion";

export default function OrbitBackground({ altitude }: { altitude: number }) {
  const yStars = altitude * 0.05;
  const yPlanet = altitude * 0.015;
  const yCurve = altitude * 0.02;

  return (
    <motion.div
      className="absolute inset-0 overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #000411 0%, #0D001A 50%, #1A0033 100%)",
      }}
    >
      {/* Stars (drifting) */}
      <div 
        className="absolute inset-0 transition-transform duration-75 ease-out opacity-60"
        style={{
          transform: `translateY(${yStars}px)`,
          backgroundImage: `radial-gradient(1px 1px at 20% 30%, rgba(255,255,255,0.6), transparent),
            radial-gradient(1.5px 1.5px at 40% 70%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1px 1px at 60% 20%, rgba(255,255,255,0.7), transparent),
            radial-gradient(1.5px 1.5px at 80% 80%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1px 1px at 10% 90%, rgba(255,255,255,0.6), transparent),
            radial-gradient(1px 1px at 90% 10%, rgba(255,255,255,0.4), transparent)`,
        }}
      />

      {/* Earth Curvature (Orbit Curve) at the bottom */}
      <div 
        className="absolute bottom-[-150%] left-[-50%] w-[200vw] h-[200vw] rounded-full border border-yellow-500/20 bg-yellow-500/5 blur-[2px] transition-transform duration-75 ease-out"
        style={{
          transform: `translateY(${yCurve}px)`,
        }}
      />

      {/* Planet (drifting very slowly) */}
      <div 
        className="absolute top-[20%] right-[10%] h-48 w-48 rounded-full bg-gradient-to-br from-blue-900/40 to-purple-900/40 blur-sm transition-transform duration-75 ease-out"
        style={{ transform: `translateY(${yPlanet}px)` }}
      />
      <div 
        className="absolute top-[22%] right-[12%] h-40 w-40 rounded-full bg-gradient-to-br from-blue-800/30 to-transparent transition-transform duration-75 ease-out"
        style={{ transform: `translateY(${yPlanet}px)` }}
      />
    </motion.div>
  );
}