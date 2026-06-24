"use client";

import { Colors } from "./GameColors";

interface ThreatSVGProps {
  type: "SURVEYOR_PROBE" | "SKY_RAY" | "AEROSOL_SWARM" | "DEFENSE_NODE" | "DERELICT_ECHO" | "VOID_TRACKER" | "VOID_WHALE" | "VOID_WRAITH" | "COMMAND_CRUISER" | "THE_GATEKEEPER" | "VOID_ENGINE" | "THE_LEVIATHAN" | "THE_SIGNAL" | "STAR_EATER";
  size?: number;
  threat?: boolean;
}

export default function ThreatSVG({ type, size = 60, threat = false }: ThreatSVGProps) {
  const baseSize = size;
  const half = baseSize / 2;

  switch (type) {
    // Surveyor Probe — Autonomous drone guarding lower altitudes (ENEMY, Tier 1)
    case "SURVEYOR_PROBE":
      return (
        <svg viewBox="0 0 60 60" width={size} height={size} className="overflow-visible">
          <g className="animate-hover-rotate">
            {/* Main hull — diamond shape */}
            <polygon
              points="30,5 50,30 30,55 10,30"
              fill={Colors.SciFiCyan}
              fillOpacity="0.6"
              stroke={Colors.SciFiCyan}
              strokeWidth="1.5"
            />
            {/* Inner core */}
            <circle cx="30" cy="30" r="8" fill={Colors.SciFiCyan} fillOpacity="0.3" />
            <circle cx="30" cy="30" r="4" fill="#FFFFFF" fillOpacity="0.8" className="animate-pulse" />
            {/* Sensor eye */}
            <circle cx="30" cy="25" r="3" fill="#FFFFFF" fillOpacity="0.9" />
            <circle cx="30" cy="25" r="1.5" fill={Colors.SciFiRed} />
            {/* Antenna */}
            <line x1="30" y1="5" x2="30" y2="0" stroke={Colors.SciFiCyan} strokeWidth="1.5" />
            <circle cx="30" cy="0" r="2" fill={Colors.SciFiCyan} className="animate-pulse" />
            {/* Thruster glow */}
            <ellipse cx="30" cy="56" rx="6" ry="3" fill={Colors.SciFiCyan} fillOpacity="0.4" className="animate-pulse" />
          </g>
        </svg>
      );

    // Sky Ray — Biological organism adapted to high-altitude flight (ENEMY, Tier 1)
    case "SKY_RAY":
      return (
        <svg viewBox="0 0 80 40" width={size * 1.3} height={size * 0.7} className="overflow-visible">
          <g className="animate-drift">
            {/* Body */}
            <ellipse cx="40" cy="20" rx="20" ry="6" fill={Colors.SciFiPurple} fillOpacity="0.5" stroke={Colors.SciFiPurple} strokeWidth="1" />
            {/* Wings */}
            <path d="M 25 20 Q 10 5 5 15 Q 15 20 25 20" fill={Colors.SciFiPurple} fillOpacity="0.3" stroke={Colors.SciFiPurple} strokeWidth="0.8" />
            <path d="M 55 20 Q 70 5 75 15 Q 65 20 55 20" fill={Colors.SciFiPurple} fillOpacity="0.3" stroke={Colors.SciFiPurple} strokeWidth="0.8" />
            {/* Eye */}
            <circle cx="35" cy="18" r="2" fill="#FFFFFF" fillOpacity="0.8" />
            {/* Trail */}
            <path d="M 40 26 Q 45 30 50 28" fill="none" stroke={Colors.SciFiPurple} strokeWidth="0.8" strokeOpacity="0.3" className="animate-pulse" />
          </g>
        </svg>
      );

    // Aerosol Swarm — Floating nano-colonies (ENEMY, Tier 2)
    case "AEROSOL_SWARM":
      return (
        <svg viewBox="0 0 60 60" width={size} height={size} className="overflow-visible">
          <g className="animate-float">
            {/* Swarm particles */}
            {[...Array(8)].map((_, i) => {
              const angle = (i / 8) * Math.PI * 2;
              const dist = 12 + Math.sin(i * 2.5) * 5;
              const x = 30 + Math.cos(angle) * dist;
              const y = 30 + Math.sin(angle) * dist;
              const r = 2 + Math.sin(i * 1.7) * 1;
              return (
                <circle key={i} cx={x} cy={y} r={r} fill={Colors.SciFiGreen} fillOpacity={0.6 + Math.sin(i) * 0.2} className="animate-pulse" style={{ animationDelay: `${i * 0.3}s` }} />
              );
            })}
            {/* Core */}
            <circle cx="30" cy="30" r="6" fill={Colors.SciFiGreen} fillOpacity="0.2" stroke={Colors.SciFiGreen} strokeWidth="0.5" />
            <circle cx="30" cy="30" r="2" fill="#FFFFFF" fillOpacity="0.6" className="animate-pulse" />
          </g>
        </svg>
      );

    // Defense Node — Ancient orbital defense (ENEMY, Tier 2)
    case "DEFENSE_NODE":
      return (
        <svg viewBox="0 0 60 60" width={size} height={size} className="overflow-visible">
          <g className="animate-hover">
            {/* Outer ring */}
            <circle cx="30" cy="30" r="22" fill="none" stroke={Colors.SciFiGold} strokeWidth="2" strokeOpacity="0.5" className="animate-shield-rotate" />
            {/* Inner ring */}
            <circle cx="30" cy="30" r="14" fill="none" stroke={Colors.SciFiGold} strokeWidth="1.5" strokeOpacity="0.3" />
            {/* Core */}
            <polygon
              points="30,10 36,24 50,24 38,34 42,48 30,38 18,48 22,34 10,24 24,24"
              fill={Colors.SciFiGold}
              fillOpacity="0.4"
              stroke={Colors.SciFiGold}
              strokeWidth="1"
            />
            {/* Center */}
            <circle cx="30" cy="30" r="4" fill={Colors.SciFiGold} fillOpacity="0.7" className="animate-pulse" />
            {/* Weapon ports */}
            <circle cx="30" cy="8" r="2" fill={Colors.SciFiRed} className="animate-warning" />
            <circle cx="30" cy="52" r="2" fill={Colors.SciFiRed} className="animate-warning" style={{ animationDelay: '0.25s' }} />
          </g>
        </svg>
      );

    // Derelict Echo — Ghostly remains of failed ascents (ENEMY, Tier 2)
    case "DERELICT_ECHO":
      return (
        <svg viewBox="0 0 50 60" width={size * 0.85} height={size} className="overflow-visible">
          <g className="animate-hover" opacity="0.6">
            {/* Damaged hull */}
            <rect x="12" y="10" width="26" height="40" rx="2" fill={Colors.EngineNozzle} fillOpacity="0.4" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.5" />
            {/* Hull damage */}
            <rect x="18" y="20" width="8" height="6" fill={Colors.SciFiBackground} opacity="0.6" />
            <rect x="26" y="35" width="6" height="8" fill={Colors.SciFiBackground} opacity="0.6" />
            {/* Ghost cockpit */}
            <circle cx="25" cy="15" r="4" fill={Colors.SciFiRed} fillOpacity="0.3" className="animate-pulse" />
            {/* Debris trail */}
            <circle cx="8" cy="50" r="2" fill={Colors.EngineNozzle} opacity="0.3" className="animate-float" />
            <circle cx="42" cy="45" r="1.5" fill={Colors.EngineNozzle} opacity="0.2" className="animate-float" style={{ animationDelay: '1s' }} />
            {/* Spark effects */}
            <circle cx="30" cy="28" r="1" fill={Colors.SciFiGold} className="animate-pulse" style={{ animationDelay: '0.5s' }} />
          </g>
        </svg>
      );

    // Void Tracker — Hunts by thermal signatures (ENEMY, Tier 3)
    case "VOID_TRACKER":
      return (
        <svg viewBox="0 0 50 60" width={size * 0.85} height={size} className="overflow-visible">
          <g className="animate-hover-rotate">
            {/* Menacing hull */}
            <path d="M 25 5 L 45 25 L 40 55 L 10 55 L 5 25 Z" fill={Colors.SciFiRed} fillOpacity="0.3" stroke={Colors.SciFiRed} strokeWidth="1.5" />
            {/* Thermal eye */}
            <circle cx="25" cy="25" r="8" fill={Colors.SciFiRed} fillOpacity="0.2" />
            <circle cx="25" cy="25" r="4" fill={Colors.SciFiGold} className="animate-pulse" />
            <circle cx="25" cy="25" r="2" fill="#FFFFFF" className="animate-warning" />
            {/* Sensor array */}
            <line x1="10" y1="35" x2="40" y2="35" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.4" />
            {/* Threat indicator */}
            <circle cx="25" cy="5" r="3" fill={Colors.SciFiRed} className="animate-warning" />
          </g>
        </svg>
      );

    // Cosmic Leviathan — Massive ethereal being (ENEMY, Tier 3)
    case "VOID_WHALE":
      return (
        <svg viewBox="0 0 100 40" width={size * 1.7} height={size * 0.7} className="overflow-visible">
          <g className="animate-drift" opacity="0.7">
            {/* Massive body */}
            <ellipse cx="50" cy="20" rx="40" ry="12" fill={Colors.SciFiPurple} fillOpacity="0.15" stroke={Colors.SciFiPurple} strokeWidth="1" strokeOpacity="0.3" />
            {/* Ethereal glow */}
            <ellipse cx="50" cy="20" rx="35" ry="9" fill={Colors.SciFiPurple} fillOpacity="0.08" className="animate-pulse" />
            {/* Eyes */}
            <circle cx="30" cy="17" r="3" fill={Colors.SciFiCyan} fillOpacity="0.6" className="animate-pulse" />
            <circle cx="30" cy="17" r="1.5" fill="#FFFFFF" />
            <circle cx="42" cy="17" r="2.5" fill={Colors.SciFiCyan} fillOpacity="0.5" className="animate-pulse" style={{ animationDelay: '0.5s' }} />
            {/* Dorsal fins */}
            <path d="M 50 8 Q 55 2 60 8" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.4" />
            <path d="M 65 10 Q 70 4 75 10" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.3" />
            {/* Tail */}
            <path d="M 85 20 Q 92 15 95 20 Q 92 25 85 20" fill={Colors.SciFiPurple} fillOpacity="0.2" stroke={Colors.SciFiPurple} strokeWidth="0.8" strokeOpacity="0.3" />
            {/* Star particles */}
            {[...Array(5)].map((_, i) => (
              <circle key={i} cx={20 + i * 15} cy={8 + Math.sin(i * 2) * 5} r={0.8} fill="#FFFFFF" opacity={0.4 + Math.sin(i) * 0.2} className="animate-twinkle" style={{ animationDelay: `${i * 0.7}s` }} />
            ))}
          </g>
        </svg>
      );

    // Shadow Entity — Void horror (ENEMY, Tier 3)
    case "VOID_WRAITH":
      return (
        <svg viewBox="0 0 40 60" width={size * 0.7} height={size} className="overflow-visible">
          <g className="animate-hover-rotate" opacity="0.5">
            {/* Shadow form */}
            <path d="M 20 5 Q 30 15 28 30 Q 25 45 35 55 Q 20 50 20 55 Q 20 50 5 55 Q 15 45 12 30 Q 10 15 20 5" fill={Colors.SciFiPurple} fillOpacity="0.2" stroke={Colors.SciFiRed} strokeWidth="0.8" strokeOpacity="0.3" />
            {/* Red eyes */}
            <circle cx="16" cy="20" r="2" fill={Colors.SciFiRed} className="animate-warning" />
            <circle cx="24" cy="20" r="2" fill={Colors.SciFiRed} className="animate-warning" style={{ animationDelay: '0.15s' }} />
            {/* Distortion ripple */}
            <circle cx="20" cy="35" r="10" fill="none" stroke={Colors.SciFiRed} strokeWidth="0.5" strokeOpacity="0.3" className="animate-pulse" />
          </g>
        </svg>
      );

    // Command Cruiser — Mini-boss that jams platforms (MINI_BOSS, Tier 4)
    case "COMMAND_CRUISER":
      return (
        <svg viewBox="0 0 120 80" width={size * 1.6} height={size * 1.1} className="overflow-visible">
          <g className="animate-hover-rotate">
            {/* Hull — elongated hexagonal */}
            <path d="M 20 40 L 30 20 L 90 20 L 100 40 L 90 60 L 30 60 Z" fill="#1a1a2e" stroke={Colors.SciFiRed} strokeWidth="1.5" />
            {/* Red pulse core */}
            <circle cx="60" cy="40" r="10" fill={Colors.SciFiRed} fillOpacity="0.3" className="animate-pulse" />
            <circle cx="60" cy="40" r="4" fill={Colors.SciFiRed} fillOpacity="0.8" />
            {/* Jammer antennae */}
            <line x1="30" y1="20" x2="20" y2="5" stroke={Colors.SciFiRed} strokeWidth="1" />
            <line x1="90" y1="20" x2="100" y2="5" stroke={Colors.SciFiRed} strokeWidth="1" />
            <circle cx="20" cy="5" r="2" fill={Colors.SciFiRed} className="animate-warning" />
            <circle cx="100" cy="5" r="2" fill={Colors.SciFiRed} className="animate-warning" style={{ animationDelay: '0.2s' }} />
            {/* Engine glow */}
            <ellipse cx="60" cy="65" rx="15" ry="5" fill={Colors.SciFiRed} fillOpacity="0.2" className="animate-pulse" />
          </g>
        </svg>
      );

    // The Gatekeeper — Rotating shield boss (BOSS, Tier 5)
    case "THE_GATEKEEPER":
      return (
        <svg viewBox="0 0 120 120" width={size * 1.6} height={size * 1.6} className="overflow-visible">
          <g className="animate-shield-rotate">
            {/* Outer shield ring */}
            <circle cx="60" cy="60" r="50" fill="none" stroke={Colors.SciFiGold} strokeWidth="3" strokeOpacity="0.4" />
            {/* Shield segments */}
            {[...Array(6)].map((_, i) => (
              <path key={i} d={`M 60 60 L ${60 + Math.cos((i / 6) * Math.PI * 2) * 45} ${60 + Math.sin((i / 6) * Math.PI * 2) * 45} A 45 45 0 0 1 ${60 + Math.cos(((i + 1) / 6) * Math.PI * 2) * 45} ${60 + Math.sin(((i + 1) / 6) * Math.PI * 2) * 45} Z`} fill={Colors.SciFiGold} fillOpacity="0.1" stroke={Colors.SciFiGold} strokeWidth="1" />
            ))}
            {/* Core */}
            <circle cx="60" cy="60" r="18" fill="#1a1a2e" stroke={Colors.SciFiGold} strokeWidth="2" />
            <circle cx="60" cy="60" r="8" fill={Colors.SciFiGold} fillOpacity="0.3" className="animate-pulse" />
            <circle cx="60" cy="60" r="3" fill="#FFFFFF" fillOpacity="0.9" />
          </g>
        </svg>
      );

    // Void Engine — Reality warping machine (BOSS, Tier 5)
    case "VOID_ENGINE":
      return (
        <svg viewBox="0 0 100 100" width={size * 1.4} height={size * 1.4} className="overflow-visible">
          <g className="animate-pulse">
            {/* Core reactor */}
            <circle cx="50" cy="50" r="30" fill="none" stroke={Colors.SciFiPurple} strokeWidth="2" strokeOpacity="0.3" />
            <circle cx="50" cy="50" r="20" fill={Colors.SciFiPurple} fillOpacity="0.15" />
            {/* Warp rings */}
            <ellipse cx="50" cy="50" rx="35" ry="12" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.6" transform="rotate(0 50 50)" />
            <ellipse cx="50" cy="50" rx="35" ry="12" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.6" transform="rotate(60 50 50)" />
            <ellipse cx="50" cy="50" rx="35" ry="12" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.6" transform="rotate(120 50 50)" />
            {/* Core */}
            <circle cx="50" cy="50" r="8" fill={Colors.SciFiPurple} fillOpacity="0.5" className="animate-pulse" />
            <circle cx="50" cy="50" r="3" fill="#FFFFFF" fillOpacity="0.8" />
          </g>
        </svg>
      );

    // The Leviathan — Gigantic void creature (BOSS, Tier 5)
    case "THE_LEVIATHAN":
      return (
        <svg viewBox="0 0 140 60" width={size * 2.2} height={size * 0.95} className="overflow-visible">
          <g className="animate-drift" opacity="0.8">
            {/* Massive body */}
            <ellipse cx="70" cy="30" rx="55" ry="16" fill="#1a1a2e" stroke={Colors.SciFiRed} strokeWidth="1.5" strokeOpacity="0.5" />
            {/* Inner glow */}
            <ellipse cx="70" cy="30" rx="45" ry="10" fill={Colors.SciFiRed} fillOpacity="0.1" className="animate-pulse" />
            {/* Head */}
            <circle cx="25" cy="28" r="10" fill="#1a1a2e" stroke={Colors.SciFiRed} strokeWidth="1" />
            {/* Eyes */}
            <circle cx="20" cy="25" r="3" fill={Colors.SciFiRed} className="animate-warning" />
            <circle cx="30" cy="25" r="3" fill={Colors.SciFiRed} className="animate-warning" style={{ animationDelay: '0.3s' }} />
            {/* Tail */}
            <path d="M 120 30 Q 130 25 135 30 Q 130 35 120 30" fill={Colors.SciFiRed} fillOpacity="0.3" />
            {/* Tentacles */}
            <path d="M 50 42 Q 55 55 50 58" fill="none" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.4" />
            <path d="M 70 44 Q 75 58 70 60" fill="none" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.4" />
            <path d="M 90 42 Q 95 55 90 58" fill="none" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.4" />
          </g>
        </svg>
      );

    // The Signal — Void serpent / final boss (BOSS, Tier 5)
    case "THE_SIGNAL":
      return (
        <svg viewBox="0 0 80 100" width={size * 1.2} height={size * 1.5} className="overflow-visible">
          <g className="animate-hover-rotate">
            {/* Serpent body */}
            <path d="M 40 10 Q 60 30 40 50 Q 20 70 40 90" fill="none" stroke={Colors.SciFiPurple} strokeWidth="4" strokeOpacity="0.6" />
            <path d="M 40 10 Q 20 30 40 50 Q 60 70 40 90" fill="none" stroke={Colors.SciFiCyan} strokeWidth="2" strokeOpacity="0.3" />
            {/* Head */}
            <circle cx="40" cy="10" r="8" fill={Colors.SciFiPurple} fillOpacity="0.4" stroke={Colors.SciFiPurple} strokeWidth="1.5" />
            {/* Eyes */}
            <circle cx="37" cy="8" r="2" fill={Colors.SciFiCyan} className="animate-pulse" />
            <circle cx="43" cy="8" r="2" fill={Colors.SciFiCyan} className="animate-pulse" style={{ animationDelay: '0.2s' }} />
            {/* Signal waves */}
            <circle cx="40" cy="10" r="14" fill="none" stroke={Colors.SciFiCyan} strokeWidth="0.5" strokeOpacity="0.3" className="animate-pulse" />
            <circle cx="40" cy="10" r="22" fill="none" stroke={Colors.SciFiCyan} strokeWidth="0.5" strokeOpacity="0.2" className="animate-pulse" style={{ animationDelay: '0.3s' }} />
          </g>
        </svg>
      );

    // Star-Eater — Cosmic organism (BOSS, Tier 5)
    case "STAR_EATER":
      return (
        <svg viewBox="0 0 120 100" width={size * 1.8} height={size * 1.5} className="overflow-visible">
          <g className="animate-pulse">
            {/* Core mouth */}
            <circle cx="60" cy="50" r="28" fill="#0a0a12" stroke={Colors.SciFiGold} strokeWidth="2" />
            {/* Inner darkness */}
            <circle cx="60" cy="50" r="18" fill="#000000" />
            {/* Absorption ring */}
            <circle cx="60" cy="50" r="35" fill="none" stroke={Colors.SciFiGold} strokeWidth="1" strokeOpacity="0.4" strokeDasharray="4 4" className="animate-shield-rotate" />
            {/* Tentacles / appendages */}
            {[...Array(8)].map((_, i) => {
              const angle = (i / 8) * Math.PI * 2;
              const x1 = 60 + Math.cos(angle) * 25;
              const y1 = 50 + Math.sin(angle) * 25;
              const x2 = 60 + Math.cos(angle) * 45;
              const y2 = 50 + Math.sin(angle) * 45;
              return <path key={i} d={`M ${x1} ${y1} Q ${(x1 + x2) / 2} ${(y1 + y2) / 2 + 5} ${x2} ${y2}`} fill="none" stroke={Colors.SciFiGold} strokeWidth="1" strokeOpacity="0.4" />;
            })}
            {/* Core eye */}
            <circle cx="60" cy="50" r="4" fill={Colors.SciFiGold} fillOpacity="0.6" className="animate-pulse" />
          </g>
        </svg>
      );

    default:
      return null;
  }
}
