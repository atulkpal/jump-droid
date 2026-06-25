"use client";

import { motion } from "framer-motion";

export default function DeepSpaceBackground({ altitude }: { altitude: number }) {
  const yStars = altitude * 0.05;
  const yNebula1 = altitude * 0.08;
  const yNebula2 = altitude * 0.06;
  const yDerelict = altitude * 0.03;

  return (
    <motion.div
      className="absolute inset-0 overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #000000 0%, #020205 50%, #050308 100%)",
      }}
    >
      {/* Stars (drifting) */}
      <div 
        className="absolute inset-0 transition-transform duration-75 ease-out opacity-60"
        style={{
          transform: `translateY(${yStars}px)`,
          backgroundImage: `radial-gradient(1px 1px at 25% 35%, rgba(255,255,255,0.6), transparent),
            radial-gradient(1.5px 1.5px at 55% 65%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1px 1px at 75% 25%, rgba(255,255,255,0.7), transparent),
            radial-gradient(1.5px 1.5px at 35% 85%, rgba(255,255,255,0.4), transparent),
            radial-gradient(1px 1px at 15% 15%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1.2px 1.2px at 85% 75%, rgba(255,255,255,0.6), transparent)`,
        }}
      />

      {/* Nebulae (drift at different speeds) */}
      <div 
        className="absolute top-[15%] left-[10%] h-1/3 w-1/2 rounded-full bg-purple-900/20 blur-[100px] transition-transform duration-75 ease-out" 
        style={{ transform: `translateY(${yNebula1}px)` }}
      />
      <div 
        className="absolute bottom-[25%] right-[15%] h-1/4 w-1/3 rounded-full bg-cyan-900/10 blur-[80px] transition-transform duration-75 ease-out"
        style={{ transform: `translateY(${yNebula2}px)` }}
      />

      {/* Derelict debris/structures */}
      <div 
        className="absolute inset-0 transition-transform duration-75 ease-out pointer-events-none opacity-20"
        style={{ transform: `translateY(${yDerelict}px)` }}
      >
        <div className="absolute top-[30%] left-[20%] w-12 h-6 bg-slate-800 border border-slate-700/50 rotate-[15deg] rounded" />
        <div className="absolute top-[60%] right-[30%] w-8 h-8 bg-slate-900 border border-slate-800/50 rotate-[-35deg] rounded" />
        <div className="absolute top-[80%] left-[45%] w-16 h-4 bg-slate-800 border border-slate-700/50 rotate-[50deg] rounded" />
      </div>
    </motion.div>
  );
}