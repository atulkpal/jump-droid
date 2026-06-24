"use client";

import { motion } from "framer-motion";

export default function OrbitBackground() {
  return (
    <motion.div
      className="absolute inset-0"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #000411 0%, #0D001A 50%, #1A0033 100%)",
      }}
    >
      {/* Planet */}
      <div className="absolute top-[20%] right-[10%] h-48 w-48 rounded-full bg-gradient-to-br from-blue-900/40 to-purple-900/40 blur-sm" />
      <div className="absolute top-[22%] right-[12%] h-40 w-40 rounded-full bg-gradient-to-br from-blue-800/30 to-transparent" />

      {/* Stars */}
      <div className="absolute inset-0" style={{
        backgroundImage: `radial-gradient(1px 1px at 20% 30%, rgba(255,255,255,0.5), transparent),
          radial-gradient(1.5px 1.5px at 40% 70%, rgba(255,255,255,0.4), transparent),
          radial-gradient(1px 1px at 60% 20%, rgba(255,255,255,0.6), transparent),
          radial-gradient(1.5px 1.5px at 80% 80%, rgba(255,255,255,0.4), transparent)`,
      }} />
    </motion.div>
  );
}