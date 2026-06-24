"use client";

import { Colors } from "./GameColors";

interface PlatformSVGProps {
  type?: "NORMAL" | "MOVING" | "BOOST" | "ICE" | "BREAKABLE" | "PHASE" | "FUEL" | "COOLING" | "STABILITY" | "MAGNETIC";
  width?: number;
  height?: number;
  animated?: boolean;
  zone?: string;
}

export default function PlatformSVG({
  type = "NORMAL",
  width = 120,
  height = 20,
  animated = true,
  zone = "EARTH",
}: PlatformSVGProps) {
  const halfW = width / 2;
  const halfH = height / 2;

  // Base platform rendering (shared by all types)
  const BaseRect = ({ color, opacity = 1 }: { color: string; opacity?: number }) => (
    <>
      {/* Platform body */}
      <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" fill={color} fillOpacity={opacity} />
      {/* Top edge highlight */}
      <line x1={-halfW + 2} y1={-halfH} x2={halfW - 2} y2={-halfH} stroke="rgba(255,255,255,0.4)" strokeWidth="1" />
      {/* Left edge */}
      <line x1={-halfW} y1={-halfH} x2={-halfW} y2={halfH} stroke="rgba(255,255,255,0.5)" strokeWidth="2" />
      {/* Right edge */}
      <line x1={halfW} y1={-halfH} x2={halfW} y2={halfH} stroke="rgba(255,255,255,0.5)" strokeWidth="2" />
      {/* Center line divider */}
      <line x1={-halfW + 10} y1={0} x2={halfW - 10} y2={0} stroke="rgba(0,0,0,0.3)" strokeWidth="1" />
      {/* Outer border */}
      <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="1.5" />
    </>
  );

  switch (type) {
    case "NORMAL":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiWhite} opacity={0.5} />
        </svg>
      );

    case "MOVING":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height} className={animated ? "animate-drift" : ""}>
          <BaseRect color={Colors.SciFiCyan} opacity={0.6} />
          {/* Direction arrows */}
          {[0.25, 0.5, 0.75].map((pos) => (
            <polygon
              key={pos}
              points={`${-halfW + width * pos - 6},${-2} ${-halfW + width * pos + 6},${0} ${-halfW + width * pos - 6},${2}`}
              fill="rgba(255,255,255,0.4)"
            />
          ))}
        </svg>
      );

    case "BOOST":
      return (
        <svg viewBox={`${-halfW} ${-halfH - 20} ${width} ${height + 20}`} width={width} height={height + 20}>
          <BaseRect color={Colors.SciFiGold} opacity={0.6} />
          {/* Upward flame gradient */}
          <rect x={-halfW + 4} y={-halfH - 15} width={width - 8} height={15} rx="2" fill={Colors.SciFiGold} fillOpacity={animated ? 0.3 : 0.1}>
            {animated && <animate attributeName="opacity" values="0.3;0.6;0.3" dur="1s" repeatCount="indefinite" />}
          </rect>
          {/* Upward arrow */}
          <polygon
            points={`0,${-halfH - 8} ${-8},${-halfH + 2} ${8},${-halfH + 2}`}
            fill={Colors.SciFiWhite}
            fillOpacity={animated ? 0.8 : 0.5}
            className={animated ? "animate-boost" : ""}
          />
        </svg>
      );

    case "ICE":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiCyan} opacity={0.3} />
          {/* Ice shimmer overlay */}
          <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" fill="url(#iceShimmer)" fillOpacity={0.3} />
          <defs>
            <linearGradient id="iceShimmer" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stopColor="#FFFFFF" stopOpacity={animated ? 0.4 : 0.2} />
              <stop offset="50%" stopColor="#FFFFFF" stopOpacity="0" />
              <stop offset="100%" stopColor="#FFFFFF" stopOpacity={animated ? 0.4 : 0.2} />
            </linearGradient>
          </defs>
          {/* Crystal lines */}
          {[0.2, 0.5, 0.8].map((pos, i) => (
            <line key={i} x1={-halfW + width * pos} y1={-halfH + 2} x2={-halfW + width * pos - 5} y2={halfH - 2} stroke="#FFFFFF" strokeWidth="0.8" strokeOpacity={0.3}>
              {animated && <animate attributeName="stroke-opacity" values="0.2;0.5;0.2" dur={`${1.5 + i * 0.5}s`} repeatCount="indefinite" />}
            </line>
          ))}
          {/* Sparkle */}
          <circle cx={0} cy={0} r={1.5} fill="#FFFFFF" fillOpacity={animated ? 0.6 : 0.3} className={animated ? "animate-twinkle" : ""} />
        </svg>
      );

    case "BREAKABLE":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiRed} opacity={0.5} />
          {/* Hazard stripes */}
          <clipPath id={`breakable-${width}`}>
            <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" />
          </clipPath>
          <g clipPath={`url(#breakable-${width})`}>
            {Array.from({ length: Math.ceil(width / 30) }).map((_, i) => (
              <polygon
                key={i}
                points={`${-halfW + i * 30},${-halfH} ${-halfW + (i + 1) * 30},${-halfH} ${-halfW + i * 30 + 15},${halfH} ${-halfW + (i - 1) * 30 + 15},${halfH}`}
                fill="rgba(0,0,0,0.2)"
              />
            ))}
          </g>
          {/* Cracks */}
          <line x1={-halfW + width * 0.3} y1={-halfH} x2={-halfW + width * 0.15} y2={halfH} stroke="#FFFFFF" strokeWidth="1.5" strokeOpacity={0.6} />
          <line x1={-halfW + width * 0.7} y1={-halfH} x2={-halfW + width * 0.85} y2={halfH} stroke="#FFFFFF" strokeWidth="1.5" strokeOpacity={0.6} />
        </svg>
      );

    case "PHASE":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height} className={animated ? "animate-phase" : ""}>
          <BaseRect color={Colors.SciFiPurple} opacity={0.5} />
          {/* Scan line */}
          <line x1={-halfW} y1={-halfH + 5} x2={halfW} y2={-halfH + 5} stroke="#FFFFFF" strokeWidth="1" strokeOpacity={0.3} className={animated ? "scan-line" : ""} />
        </svg>
      );

    case "FUEL":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiGreen} opacity={0.5} />
          {/* Glowing border */}
          <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" fill="none" stroke={Colors.SciFiGreen} strokeWidth="3" strokeOpacity={animated ? 0.5 : 0.3} className={animated ? "animate-pulse" : ""} />
          {/* Corner dots */}
          <rect x={-halfW + 3} y={-halfH + 3} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={halfW - 7} y={-halfH + 3} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={-halfW + 3} y={halfH - 7} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={halfW - 7} y={halfH - 7} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          {/* FUEL label */}
          <text x="0" y="5" textAnchor="middle" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="monospace" letterSpacing="2">FUEL</text>
        </svg>
      );

    case "COOLING":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiCyan} opacity={0.5} />
          <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" fill="none" stroke={Colors.SciFiCyan} strokeWidth="3" strokeOpacity={animated ? 0.5 : 0.3} className={animated ? "animate-pulse" : ""} />
          <rect x={-halfW + 3} y={-halfH + 3} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={halfW - 7} y={-halfH + 3} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={-halfW + 3} y={halfH - 7} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={halfW - 7} y={halfH - 7} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <text x="0" y="5" textAnchor="middle" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="monospace" letterSpacing="2">COOL</text>
        </svg>
      );

    case "STABILITY":
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiWhite} opacity={0.4} />
          <rect x={-halfW} y={-halfH} width={width} height={height} rx="2" fill="none" stroke="#FFFFFF" strokeWidth="3" strokeOpacity={animated ? 0.5 : 0.3} className={animated ? "animate-pulse" : ""} />
          <rect x={-halfW + 3} y={-halfH + 3} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={halfW - 7} y={-halfH + 3} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={-halfW + 3} y={halfH - 7} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <rect x={halfW - 7} y={halfH - 7} width={4} height={4} fill="#FFFFFF" fillOpacity={0.7} />
          <text x="0" y="5" textAnchor="middle" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="monospace" letterSpacing="2">STAB</text>
        </svg>
      );

    case "MAGNETIC":
      return (
        <svg viewBox={`${-halfW - 20} ${-halfH - 20} ${width + 40} ${height + 40}`} width={width + 40} height={height + 40}>
          <BaseRect color={Colors.SciFiPurple} opacity={0.5} />
          {/* Expanding ring pulses */}
          {animated && [0, 1, 2, 3].map((i) => (
            <circle
              key={i}
              cx="0"
              cy="0"
              r={25}
              fill="none"
              stroke={Colors.SciFiPurple}
              strokeWidth="2"
              strokeOpacity={0.3}
              className="animate-pulse"
              style={{ animationDelay: `${i * 0.6}s` }}
            >
              <animate attributeName="r" values="15;60;15" dur={`${2 + i * 0.3}s`} repeatCount="indefinite" begin={`${i * 0.5}s`} />
              <animate attributeName="stroke-opacity" values="0.4;0;0.4" dur={`${2 + i * 0.3}s`} repeatCount="indefinite" begin={`${i * 0.5}s`} />
            </circle>
          ))}
          {/* Inner white stripe */}
          <rect x={-halfW + width * 0.25} y={-halfH + 3} width={width * 0.5} height={height - 6} fill="#FFFFFF" fillOpacity="0.2" />
        </svg>
      );

    default:
      return (
        <svg viewBox={`${-halfW} ${-halfH} ${width} ${height}`} width={width} height={height}>
          <BaseRect color={Colors.SciFiWhite} opacity={0.5} />
        </svg>
      );
  }
}