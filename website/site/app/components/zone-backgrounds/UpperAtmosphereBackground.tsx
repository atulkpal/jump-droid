"use client";

import { motion } from "framer-motion";

export default function UpperAtmosphereBackground({ altitude }: { altitude: number }) {
  const yStars = altitude * 0.05;
  return (
    <motion.div
      className="absolute inset-0 overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      style={{
        background: "linear-gradient(180deg, #0D001A 0%, #1A0033 50%, #311B92 100%)",
      }}
    >
      {/* Stars (drifting slowly in parallax) */}
      <div 
        className="absolute inset-0 transition-transform duration-75 ease-out opacity-40"
        style={{
          transform: `translateY(${yStars}px)`,
          backgroundImage: `radial-gradient(1px 1px at 15% 25%, rgba(255,255,255,0.6), transparent),
            radial-gradient(1.5px 1.5px at 45% 65%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1px 1px at 75% 15%, rgba(255,255,255,0.7), transparent),
            radial-gradient(1px 1px at 85% 85%, rgba(255,255,255,0.5), transparent),
            radial-gradient(1.2px 1.2px at 30% 75%, rgba(255,255,255,0.6), transparent),
            radial-gradient(1.5px 1.5px at 90% 35%, rgba(255,255,255,0.5), transparent)`,
        }}
      />

      {/* Aurora effect */}
      {[...Array(3)].map((_, i) => {
        const speed = 0.08 + (i % 2) * 0.06;
        const yTrans = altitude * speed;
        return (
          <div
            key={i}
            className="absolute h-1/3 w-full blur-3xl transition-transform duration-75 ease-out"
            style={{
              top: `${15 + i * 10}%`,
              transform: `translateY(${yTrans}px)`,
              background: `linear-gradient(90deg, transparent, ${['#00E5FF','#D500F9','#FF1744'][i]}, transparent)`,
              animation: `drift ${10 + i * 3}s ease-in-out infinite`,
              animationDelay: `${i * 1.5}s`,
            }}
          />
        );
      })}
    </motion.div>
  );
}