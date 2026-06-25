"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import PlatformSVG from "./game/PlatformSVG";

const PLATFORMS = [
  { type: "NORMAL" as const, name: "Normal", desc: "Standard landing platform. Reliable and consistent." },
  { type: "MOVING" as const, name: "Moving", desc: "Shifts horizontally. Timing is everything." },
  { type: "BOOST" as const, name: "Boost", desc: "Launches rocket upward at high velocity." },
  { type: "ICE" as const, name: "Ice", desc: "Slippery surface. Reduced friction control." },
  { type: "BREAKABLE" as const, name: "Breakable", desc: "Cracks under weight. Land and launch quickly." },
  { type: "PHASE" as const, name: "Phase", desc: "Flickers in and out of reality. Unpredictable." },
  { type: "FUEL" as const, name: "Fuel", desc: "Replenishes fuel reserves on contact." },
  { type: "COOLING" as const, name: "Cooling", desc: "Reduces rocket heat buildup." },
  { type: "STABILITY" as const, name: "Stability", desc: "Grants temporary shield stabilization." },
  { type: "MAGNETIC" as const, name: "Magnetic", desc: "Attracts rocket from nearby range." },
  { type: "CONVEYOR" as const, name: "Conveyor", desc: "Forces horizontal drift. Adjust steering or use velocity." },
  { type: "MIMIC" as const, name: "Mimic", desc: "Disguised as standard. Shatters on landing, dealing damage." },
];

export default function PlatformShowcase() {
  const [hovered, setHovered] = useState<number | null>(null);

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-6">
      {PLATFORMS.map((p, i) => (
        <motion.div
          key={p.type}
          className="relative flex flex-col items-center"
          onHoverStart={() => setHovered(i)}
          onHoverEnd={() => setHovered(null)}
          whileHover={{ scale: 1.05 }}
          transition={{ type: "spring", stiffness: 300, damping: 20 }}
        >
          <div className="mb-3">
            <PlatformSVG type={p.type} width={100} height={16} animated={hovered === i} />
          </div>
          <p className="text-xs font-bold uppercase tracking-widest text-cyan-300">{p.name}</p>
          <AnimatePresence>
            {hovered === i && (
              <motion.p
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 5 }}
                className="mt-2 text-[10px] text-slate-400 text-center leading-relaxed"
              >
                {p.desc}
              </motion.p>
            )}
          </AnimatePresence>
        </motion.div>
      ))}
    </div>
  );
}