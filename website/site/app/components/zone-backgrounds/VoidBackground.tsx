"use client";

import { motion } from "framer-motion";

export default function VoidBackground() {
  return (
    <motion.div
      className="absolute inset-0"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #000000 0%, #010103 50%, #020103 100%)",
      }}
    >
      {/* Red pulse / distortion */}
      <div className="absolute inset-0" style={{
        background: "radial-gradient(circle at 50% 50%, transparent 40%, rgba(255,23,68,0.05) 100%)",
      }} />

      {/* Subtle stars */}
      <div className="absolute inset-0" style={{
        backgroundImage: `radial-gradient(1px 1px at 30% 40%, rgba(255,255,255,0.3), transparent),
          radial-gradient(1.5px 1.5px at 70% 60%, rgba(255,255,255,0.2), transparent)`,
      }} />
    </motion.div>
  );
}