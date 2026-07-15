"use client";

import { motion } from "framer-motion";

export default function EarthBackground({ altitude }: { altitude: number }) {
  const ySun = altitude * 0.02;
  const yFarMount = altitude * 0.10;
  const yNearMount = altitude * 0.25;

  return (
    <motion.div
      className="absolute inset-0 overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #1e3a5f 0%, #2196F3 50%, #66BB6A 100%)",
      }}
    >
      {/* Sun (Moves very slowly) */}
      <div 
        className="absolute top-[10%] right-[15%] h-32 w-32 rounded-full bg-yellow-300/80 blur-xl transition-transform duration-75 ease-out"
        style={{ transform: `translateY(${ySun}px)` }}
      />
      <div 
        className="absolute top-[12%] right-[17%] h-24 w-24 rounded-full bg-yellow-100/60 blur-lg transition-transform duration-75 ease-out"
        style={{ transform: `translateY(${ySun}px)` }}
      />

      {/* Distant mountains silhouette (Far) */}
      <div 
        className="absolute bottom-0 left-0 right-0 h-1/3 bg-gradient-to-t from-emerald-950/40 to-transparent transition-transform duration-75 ease-out"
        style={{
          transform: `translateY(${yFarMount}px)`,
          clipPath: "polygon(0% 100%, 0% 70%, 15% 55%, 30% 75%, 45% 50%, 60% 70%, 75% 45%, 90% 65%, 100% 50%, 100% 100%)",
        }}
      />

      {/* Near mountains silhouette (Near) */}
      <div 
        className="absolute bottom-0 left-0 right-0 h-1/4 bg-gradient-to-t from-black/50 to-transparent transition-transform duration-75 ease-out"
        style={{
          transform: `translateY(${yNearMount}px)`,
          clipPath: "polygon(0% 100%, 0% 80%, 10% 70%, 25% 85%, 40% 65%, 55% 80%, 70% 60%, 85% 75%, 100% 60%, 100% 100%)",
        }}
      />
    </motion.div>
  );
}