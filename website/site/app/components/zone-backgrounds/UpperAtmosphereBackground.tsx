"use client";

import { motion } from "framer-motion";

export default function UpperAtmosphereBackground() {
  return (
    <motion.div
      className="absolute inset-0"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #0D001A 0%, #1A0033 50%, #311B92 100%)",
      }}
    >
      {/* Aurora effect */}
      {[...Array(3)].map((_, i) => (
        <div
          key={i}
          className="absolute h-1/3 w-full blur-3xl"
          style={{
            top: `${15 + i * 10}%`,
            background: `linear-gradient(90deg, transparent, ${['#00E5FF','#D500F9','#FF1744'][i]}, transparent)`,
            animation: `drift ${10 + i * 3}s ease-in-out infinite`,
            animationDelay: `${i * 1.5}s`,
          }}
        />
      ))}
    </motion.div>
  );
}