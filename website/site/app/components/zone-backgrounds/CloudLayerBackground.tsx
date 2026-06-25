"use client";

import { motion } from "framer-motion";

export default function CloudLayerBackground({ altitude }: { altitude: number }) {
  return (
    <motion.div
      className="absolute inset-0 overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #00BCD4 0%, #4DD0E1 50%, #311B92 100%)",
      }}
    >
      {/* Cloud layers */}
      {[...Array(6)].map((_, i) => {
        const speed = 0.20 + (i % 3) * 0.20;
        const yTrans = altitude * speed;
        return (
          <div
            key={i}
            className="absolute rounded-full bg-white/10 blur-3xl transition-transform duration-75 ease-out"
            style={{
              width: `${200 + (i % 4) * 100}px`,
              height: `${60 + (i % 3) * 20}px`,
              left: `${(i * 25) % 100}%`,
              top: `${(i * 15) % 80}%`,
              transform: `translateY(${yTrans}px)`,
              animation: `drift ${6 + i * 2}s ease-in-out infinite`,
              animationDelay: `${i * 0.5}s`,
            }}
          />
        );
      })}
    </motion.div>
  );
}