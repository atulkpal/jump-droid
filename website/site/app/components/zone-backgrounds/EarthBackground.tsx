"use client";

import { motion } from "framer-motion";

export default function EarthBackground() {
  return (
    <motion.div
      className="absolute inset-0"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #1e3a5f 0%, #2196F3 50%, #66BB6A 100%)",
      }}
    >
      {/* Sun */}
      <div className="absolute top-[10%] right-[15%] h-32 w-32 rounded-full bg-yellow-300/80 blur-xl" />
      <div className="absolute top-[12%] right-[17%] h-24 w-24 rounded-full bg-yellow-100/60 blur-lg" />

      {/* Distant mountains silhouette */}
      <div className="absolute bottom-0 left-0 right-0 h-1/4 bg-gradient-to-t from-black/40 to-transparent"
        style={{
          clipPath: "polygon(0% 100%, 0% 60%, 10% 50%, 20% 65%, 30% 45%, 40% 60%, 50% 40%, 60% 55%, 70% 35%, 80% 50%, 90% 40%, 100% 55%, 100% 100%)",
        }}
      />
    </motion.div>
  );
}