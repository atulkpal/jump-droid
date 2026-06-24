"use client";

import { motion } from "framer-motion";

export default function CloudLayerBackground() {
  return (
    <motion.div
      className="absolute inset-0"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #00BCD4 0%, #4DD0E1 50%, #311B92 100%)",
      }}
    >
      {/* Cloud layers */}
      {[...Array(5)].map((_, i) => (
        <div
          key={i}
          className="absolute rounded-full bg-white/10 blur-3xl"
          style={{
            width: `${200 + i * 100}px`,
            height: `${60 + i * 20}px`,
            left: `${(i * 25) % 100}%`,
            top: `${(i * 15) % 80}%`,
            animation: `drift ${6 + i * 2}s ease-in-out infinite`,
            animationDelay: `${i * 0.5}s`,
          }}
        />
      ))}
    </motion.div>
  );
}