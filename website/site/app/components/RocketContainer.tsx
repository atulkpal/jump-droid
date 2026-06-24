"use client";

import { motion } from "framer-motion";
import RocketSVG from "./game/RocketSVG";

interface RocketContainerProps {
  scrollTop: number;
}

export default function RocketContainer({ scrollTop }: RocketContainerProps) {
  const sway = Math.sin(scrollTop / 180) * 2;

  return (
    <div className="fixed left-1/2 top-1/2 z-20">
      <motion.div
        animate={{ rotate: sway }}
        transition={{ type: "spring", stiffness: 150, damping: 20 }}
        style={{ transform: "translate(-50%, -50%)" }}
      >
        <RocketSVG type="BALANCED" size={90} thrusting />
      </motion.div>
    </div>
  );
}