"use client";

import { motion } from "framer-motion";
import { Colors } from "./GameColors";

interface RocketSVGProps {
  type?: "BALANCED" | "SCOUT" | "TANK" | "EXPERIMENTAL" | "STEALTH" | "REFLECTOR";
  size?: number;
  thrusting?: boolean;
  tilt?: number;
  heat?: number;
  shield?: number;
  maxShield?: number;
  showFlame?: boolean;
}

const ROCKET_W = 40;
const ROCKET_H = 70;
const HALF_W = ROCKET_W / 2;
const HALF_H = ROCKET_H / 2;

const rocketBodyColor = (type: string) => {
  switch (type) {
    case "SCOUT": return Colors.RocketScout;
    case "TANK": return Colors.RocketTank;
    case "EXPERIMENTAL": return Colors.RocketExperimental;
    case "STEALTH": return "#1c1c22"; // Matte black stealth hull
    case "REFLECTOR": return "#546e7a"; // Metallic steel armor
    default: return Colors.RocketBalanced;
  }
};

export default function RocketSVG({
  type = "BALANCED",
  size = 120,
  thrusting = true,
  tilt = 0,
  heat = 0,
  shield = 50,
  maxShield = 50,
  showFlame = true,
}: RocketSVGProps) {
  const scale = size / 100;
  const bodyColor = rocketBodyColor(type);
  const heatRatio = Math.min(heat / 100, 1);
  const shieldRatio = shield / maxShield;
  const isOverheated = heat >= 100;

  const plateCount =
    shieldRatio >= 0.9 ? 8 :
    shieldRatio >= 0.7 ? 6 :
    shieldRatio >= 0.4 ? 4 :
    shieldRatio >= 0.15 ? 2 :
    1;

  return (
    <motion.svg
      viewBox={`${-HALF_W - 10} ${-HALF_H - 10} ${ROCKET_W + 20} ${ROCKET_H + 30}`}
      width={size}
      height={size * 1.1}
      animate={{ rotate: tilt }}
      transition={{ type: "spring", stiffness: 150, damping: 20 }}
      className="overflow-visible"
    >
      <defs>
        <linearGradient id="flameOuter" x1="0" y1="1" x2="0" y2="0">
          <stop offset="0%" stopColor={Colors.SciFiGold} stopOpacity="0" />
          <stop offset="100%" stopColor={Colors.SciFiGold} stopOpacity="0.8" />
        </linearGradient>
        <linearGradient id="flameInner" x1="0" y1="1" x2="0" y2="0">
          <stop offset="0%" stopColor={Colors.SciFiCyan} stopOpacity={showFlame ? "0" : "0"} />
          <stop offset="100%" stopColor="#FFFFFF" stopOpacity="1" />
        </linearGradient>
        <radialGradient id="heatGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor={Colors.SciFiRed} stopOpacity="0.3" />
          <stop offset="100%" stopColor={Colors.SciFiRed} stopOpacity="0" />
        </radialGradient>
      </defs>

      {/* Shield arcs */}
      {shield > 0 && (
        <g>
          {Array.from({ length: plateCount }).map((_, i) => {
            const angle = (i * (360 / plateCount));
            const floatOffset = Math.cos(i * 1.5) * 3;
            const radius = HALF_W + 18 + floatOffset;
            return (
              <g key={i} transform={`rotate(${angle})`}>
                <path
                  d={`M ${-radius} 0 A ${radius} ${radius} 0 0 1 ${radius} 0`}
                  fill="none"
                  stroke={Colors.SciFiCyan}
                  strokeWidth={shieldRatio > 0.7 ? 6 : 4}
                  strokeOpacity={0.5 + 0.3 * shieldRatio}
                  className="origin-center"
                />
              </g>
            );
          })}
        </g>
      )}

      {/* Heat glow overlay */}
      {heatRatio > 0.3 && (
        <ellipse
          cx="0" cy="0"
          rx={HALF_W + 10}
          ry={HALF_H + 10}
          fill="url(#heatGlow)"
        />
      )}

      {/* ===== ROCKET FUSELAGE ===== */}
      <g>
        <rect
          x={-HALF_W + 5}
          y={-HALF_H + 15}
          width={ROCKET_W - 10}
          height={ROCKET_H - 15}
          fill={isOverheated ? Colors.SciFiRed : bodyColor}
          rx="2"
        />
        <line x1={-HALF_W + 10} y1={-HALF_H + 27} x2={HALF_W - 10} y2={-HALF_H + 27} stroke="rgba(0,0,0,0.15)" strokeWidth="1" />
        <line x1={-HALF_W + 10} y1={-HALF_H + 42} x2={HALF_W - 10} y2={-HALF_H + 42} stroke="rgba(0,0,0,0.15)" strokeWidth="1" />
        <line x1={-HALF_W + 10} y1={-HALF_H + 57} x2={HALF_W - 10} y2={-HALF_H + 57} stroke="rgba(0,0,0,0.15)" strokeWidth="1" />
        <line x1={HALF_W - 6} y1={-HALF_H + 17} x2={HALF_W - 6} y2={-HALF_H + 68} stroke="rgba(255,255,255,0.2)" strokeWidth="1.5" />
      </g>

      {/* Spikes for Kinetic Reflector (REFLECTOR) */}
      {type === "REFLECTOR" && (
        <g fill={Colors.SciFiGold} stroke="#FFFFFF" strokeWidth="0.8">
          <polygon points="-15,-8 -23,-2 -15,4" />
          <polygon points="-15,12 -23,18 -15,24" />
          <polygon points="15,-8 23,-2 15,4" />
          <polygon points="15,12 23,18 15,24" />
        </g>
      )}

      {/* ===== ENGINE NOZZLE ===== */}
      <rect x={-7} y={-HALF_H + 68} width={14} height={6} fill={Colors.EngineNozzle} rx="1" />
      <rect x={-5} y={-HALF_H + 69} width={10} height={2} fill={Colors.EngineNozzleDark} />

      {/* ===== NOSE CONE ===== */}
      <polygon
        points={`${-HALF_W + 5},${-HALF_H + 15} 0,${-HALF_H} ${HALF_W - 5},${-HALF_H + 15}`}
        fill={isOverheated ? Colors.SciFiRed : type === "STEALTH" ? "#2a2d34" : Colors.EngineNozzle}
      />
      <line x1="0" y1={-HALF_H + 3} x2={-HALF_W + 13} y2={-HALF_H + 13} stroke="rgba(255,255,255,0.2)" strokeWidth="1" />

      {/* ===== COCKPIT ===== */}
      <circle cx="0" cy={-HALF_H + 28} r="7" fill={type === "STEALTH" ? Colors.SciFiPurple : type === "REFLECTOR" ? Colors.SciFiGold : Colors.CockpitGlow} fillOpacity="0.8" />
      <circle cx="0" cy={-HALF_H + 28} r="12" fill={type === "STEALTH" ? Colors.SciFiPurple : type === "REFLECTOR" ? Colors.SciFiGold : Colors.CockpitGlow} fillOpacity="0.15" />

      {/* ===== FINS ===== */}
      <polygon points={`${-HALF_W + 5},${-HALF_H + 55} ${-HALF_W},${-HALF_H + 68} ${-HALF_W + 5},${-HALF_H + 68}`} fill={Colors.FinColor} />
      <polygon points={`${HALF_W - 5},${-HALF_H + 55} ${HALF_W},${-HALF_H + 68} ${HALF_W - 5},${-HALF_H + 68}`} fill={Colors.FinColor} />

      {/* ===== THRUSTER FLAME ===== */}
      {thrusting && showFlame && !isOverheated && (
        <g className="thruster-flame">
          <path
            d={`M -14 ${-HALF_H + 72} Q 0 ${-HALF_H + 120} 14 ${-HALF_H + 72} Z`}
            fill="url(#flameOuter)"
            className="animate-flicker"
          />
          <path
            d={`M -6 ${-HALF_H + 72} Q 0 ${-HALF_H + 100} 6 ${-HALF_H + 72} Z`}
            fill="url(#flameInner)"
            className="animate-flicker-inner"
          />
          <circle cx="0" cy={-HALF_H + 72} r="11" fill="#FFFFFF" fillOpacity="0.3" className="animate-pulse" />
          <circle cx="0" cy={-HALF_H + 72} r="16" fill={Colors.SciFiGold} fillOpacity="0.15" className="animate-pulse" />
        </g>
      )}

      {/* Overheat indicator */}
      {isOverheated && (
        <text x="0" y={-HALF_H + 85} textAnchor="middle" fill={Colors.SciFiRed} fontSize="10" fontWeight="bold" className="animate-pulse">
          OVERHEATED
        </text>
      )}
    </motion.svg>
  );
}
