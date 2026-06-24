"use client";

import { motion } from "framer-motion";
import RocketSVG from "./game/RocketSVG";

interface FlyingRocketProps {
  progress: number;
  rocketX: number;
}

export default function FlyingRocket({ progress, rocketX }: FlyingRocketProps) {
  // Flight path: start bottom-left, fly up to center
  const bottomPct = 12 + progress * 42; // 12% → 54%
  const swayX = Math.sin(progress * 18) * 4 + Math.sin(progress * 7) * 2;
  const rotation = Math.sin(progress * 14) * 3 + Math.cos(progress * 9) * 1.5;
  const scale = 0.85 + Math.sin(progress * 6) * 0.08;

  return (
    <motion.div
      className="fixed z-20 pointer-events-none"
      style={{
        left: `calc(${rocketX}% + ${swayX}px)`,
        bottom: `${bottomPct}%`,
        transform: `translateX(-50%) rotate(${rotation}deg) scale(${scale})`,
        transformOrigin: "center center",
      }}
      transition={{
        type: "spring",
        stiffness: 90,
        damping: 16,
      }}
    >
      <RocketSVG type="BALANCED" size={100} thrusting />
    </motion.div>
  );
}