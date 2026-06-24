"use client";

import { motion } from "framer-motion";

export default function DeepSpaceBackground() {
  return (
    <motion.div
      className="absolute inset-0"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #000000 0%, #020205 50%, #050308 100%)",
      }}
    >
      {/* Nebulae */}
      <div className="absolute top-[15%] left-[10%] h-1/3 w-1/2 rounded-full bg-purple-900/20 blur-[100px]" />
      <div className="absolute bottom-[25%] right-[15%] h-1/4 w-1/3 rounded-full bg-cyan-900/10 blur-[80px]" />

      {/* Debris / stars */}
      <div className="absolute inset-0" style={{
        backgroundImage: `radial-gradient(1px 1px at 25% 35%, rgba(255,255,255,0.5), transparent),
          radial-gradient(1.5px 1.5px at 55% 65%, rgba(255,255,255,0.4), transparent),
          radial-gradient(1px 1px at 75% 25%, rgba(255,255,255,0.6), transparent),
          radial-gradient(1.5px 1.5px at 35% 85%, rgba(255,255,255,0.3), transparent)`,
      }} />
    </motion.div>
  );
}