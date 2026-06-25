"use client";

import { motion } from "framer-motion";

export default function VoidBackground({ altitude }: { altitude: number }) {
  const yStars = altitude * 0.05;
  const yAnomaly = altitude * 0.04;

  return (
    <motion.div
      className="absolute inset-0 overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #000000 0%, #010103 50%, #020103 100%)",
      }}
    >
      {/* Subtle stars (drifting) */}
      <div 
        className="absolute inset-0 transition-transform duration-75 ease-out opacity-40"
        style={{
          transform: `translateY(${yStars}px)`,
          backgroundImage: `radial-gradient(1px 1px at 30% 40%, rgba(255,255,255,0.4), transparent),
            radial-gradient(1.5px 1.5px at 70% 60%, rgba(255,255,255,0.3), transparent),
            radial-gradient(1px 1px at 15% 75%, rgba(255,255,255,0.3), transparent),
            radial-gradient(1.2px 1.2px at 85% 20%, rgba(255,255,255,0.4), transparent)`,
        }}
      />

      {/* Red pulse / distortion (drifting) */}
      <div 
        className="absolute inset-0 transition-transform duration-75 ease-out" 
        style={{
          transform: `translateY(${yAnomaly}px)`,
          background: "radial-gradient(circle at 50% 50%, transparent 40%, rgba(255,23,68,0.08) 100%)",
        }} 
      />
    </motion.div>
  );
}