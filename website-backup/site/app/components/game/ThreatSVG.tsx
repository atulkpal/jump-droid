"use client";

import { Colors } from "./GameColors";

interface ThreatSVGProps {
  type: 
    | "SURVEYOR_PROBE" | "SKY_RAY" | "AEROSOL_SWARM" | "DEFENSE_NODE" | "DERELICT_ECHO" | "VOID_TRACKER" | "VOID_WHALE" | "VOID_WRAITH" 
    | "COMMAND_CRUISER" | "THE_GATEKEEPER" | "VOID_ENGINE" | "THE_LEVIATHAN" | "THE_SIGNAL" | "STAR_EATER"
    | "THE_ARCHITECT" | "ENTROPY_CORE" | "THERMAL_HIVE" | "GRAVITY_ANCHOR" | "THE_FORGER" 
    | "HEAT_BAT" | "MIMIC_PLATFORM" | "VOID_HARVESTER" | "PHASE_WRAITH" | "GRAVITY_RAM" 
    | "CRYO_MIST" | "MIRROR_SHARDS" | "GRAVITY_SHEAR"
    | "ENT_SCOUT_DRONE" | "ENT_CLOUD_SKIMMER" | "ENT_SWARM_BOTS" | "ENT_ORBITAL_SENTRY" 
    | "ENT_CORRUPTED_HULL" | "ENT_STALKER" | "ENT_VOID_WHALE" | "ENT_VOID_WRAITH" 
    | "COSMIC_LEVIATHAN" | "SHADOW_ENTITY";
  size?: number;
  threat?: boolean;
}

export default function ThreatSVG({ type, size = 60, threat = false }: ThreatSVGProps) {
  const baseSize = size;
  const half = baseSize / 2;

  switch (type) {
    // Surveyor Probe — Autonomous drone guarding lower altitudes (ENEMY, Tier 1)
    case "SURVEYOR_PROBE":
    case "ENT_SCOUT_DRONE":
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
    case "ENT_CLOUD_SKIMMER":
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
    case "ENT_SWARM_BOTS":
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
    case "ENT_ORBITAL_SENTRY":
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
    case "ENT_CORRUPTED_HULL":
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
    case "ENT_STALKER":
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
    case "ENT_VOID_WHALE":
    case "COSMIC_LEVIATHAN":
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
    case "ENT_VOID_WRAITH":
    case "SHADOW_ENTITY":
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

    // R&D: The Architect (BOSS, Foundry)
    case "THE_ARCHITECT":
      return (
        <svg viewBox="0 0 100 100" width={size} height={size} className="overflow-visible">
          <g className="animate-hover">
            <rect x="25" y="25" width="50" height="50" fill="none" stroke={Colors.SciFiCyan} strokeWidth="2" strokeOpacity="0.8" />
            <rect x="35" y="35" width="30" height="30" fill="none" stroke={Colors.SciFiCyan} strokeWidth="1" strokeOpacity="0.5" />
            <circle cx="50" cy="50" r="10" fill="none" stroke={Colors.SciFiCyan} strokeWidth="1.5" strokeDasharray="3 3" className="animate-shield-rotate" />
            <circle cx="50" cy="50" r="4" fill="#FFFFFF" className="animate-pulse" />
            <circle cx="50" cy="50" r="8" fill={Colors.SciFiCyan} fillOpacity="0.2" />
            <line x1="15" y1="50" x2="25" y2="50" stroke={Colors.SciFiCyan} strokeWidth="1" />
            <line x1="75" y1="50" x2="85" y2="50" stroke={Colors.SciFiCyan} strokeWidth="1" />
            <line x1="50" y1="15" x2="50" y2="25" stroke={Colors.SciFiCyan} strokeWidth="1" />
            <line x1="50" y1="75" x2="50" y2="85" stroke={Colors.SciFiCyan} strokeWidth="1" />
          </g>
        </svg>
      );

    // R&D: Entropy Core (BOSS, Deep Space)
    case "ENTROPY_CORE":
      return (
        <svg viewBox="0 0 120 120" width={size} height={size} className="overflow-visible">
          <g className="animate-pulse">
            <circle cx="60" cy="60" r="22" fill="#1a1a2e" stroke={Colors.SciFiPurple} strokeWidth="2.5" />
            <circle cx="60" cy="60" r="12" fill={Colors.SciFiPurple} fillOpacity="0.3" />
            {[...Array(4)].map((_, i) => {
              const angle = (i * 90) * Math.PI / 180;
              const x1 = 60 + Math.cos(angle) * 22;
              const y1 = 60 + Math.sin(angle) * 22;
              const x2 = 60 + Math.cos(angle) * 45;
              const y2 = 60 + Math.sin(angle) * 45;
              return (
                <g key={i}>
                  <line x1={x1} y1={y1} x2={x2} y2={y2} stroke={Colors.SciFiPurple} strokeWidth="2.5" strokeOpacity="0.8" />
                  <circle cx={x2} cy={y2} r="5" fill={Colors.SciFiRed} className="animate-warning" />
                </g>
              );
            })}
            <circle cx="60" cy="60" r="38" fill="none" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.3" strokeDasharray="6 6" className="animate-shield-rotate" />
          </g>
        </svg>
      );

    // R&D: Thermal Hive (MINI_BOSS, Atmosphere)
    case "THERMAL_HIVE":
      return (
        <svg viewBox="0 0 100 100" width={size} height={size} className="overflow-visible">
          <g className="animate-hover">
            <polygon points="50,15 80,32 80,68 50,85 20,68 20,32" fill="#1a0a0d" stroke={Colors.SciFiRed} strokeWidth="2" />
            <polygon points="50,28 70,40 70,60 50,72 30,60 30,40" fill="none" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.6" />
            <circle cx="50" cy="35" r="5" fill={Colors.SciFiGold} className="animate-pulse" />
            <circle cx="38" cy="55" r="4" fill={Colors.SciFiGold} className="animate-pulse" style={{ animationDelay: "0.2s" }} />
            <circle cx="62" cy="55" r="4" fill={Colors.SciFiGold} className="animate-pulse" style={{ animationDelay: "0.4s" }} />
            <circle cx="50" cy="15" r="1.5" fill={Colors.SciFiGold} className="animate-float" />
            <circle cx="20" cy="32" r="1.5" fill={Colors.SciFiGold} className="animate-float" style={{ animationDelay: "0.8s" }} />
            <circle cx="80" cy="32" r="1.5" fill={Colors.SciFiGold} className="animate-float" style={{ animationDelay: "1.2s" }} />
          </g>
        </svg>
      );

    // R&D: Gravity Anchor (MINI_BOSS, Deep Space)
    case "GRAVITY_ANCHOR":
      return (
        <svg viewBox="0 0 100 100" width={size} height={size} className="overflow-visible">
          <g className="animate-hover-rotate">
            <line x1="20" y1="40" x2="80" y2="40" stroke={Colors.SciFiCyan} strokeWidth="4" />
            <line x1="50" y1="15" x2="50" y2="75" stroke={Colors.SciFiCyan} strokeWidth="4" />
            <circle cx="50" cy="20" r="10" fill="none" stroke={Colors.SciFiCyan} strokeWidth="3" />
            <path d="M 20 40 Q 20 80 50 80 Q 80 80 80 40" fill="none" stroke={Colors.SciFiCyan} strokeWidth="4" />
            <rect x="42" y="70" width="16" height="12" fill={Colors.SciFiCyan} rx="1" />
            <path d="M 30 90 Q 50 105 70 90" fill="none" stroke={Colors.SciFiCyan} strokeWidth="1.5" strokeOpacity="0.4" className="animate-pulse" />
          </g>
        </svg>
      );

    // R&D: The Forger (MINI_BOSS, Orbit)
    case "THE_FORGER":
      return (
        <svg viewBox="0 0 100 100" width={size} height={size} className="overflow-visible">
          <g className="animate-hover">
            <rect x="25" y="30" width="50" height="25" fill="#37474F" stroke={Colors.SciFiGold} strokeWidth="2" rx="2" />
            <rect x="45" y="10" width="10" height="20" fill={Colors.EngineNozzle} />
            <rect x="35" y="37" width="30" height="10" fill={Colors.SciFiGold} fillOpacity="0.4" className="animate-pulse" />
            <line x1="15" y1="75" x2="85" y2="75" stroke="#37474F" strokeWidth="6" />
            <polygon points="35,72 40,60 60,60 65,72" fill="#263238" />
            <circle cx="30" cy="65" r="1.5" fill={Colors.SciFiGold} className="animate-float" />
            <circle cx="70" cy="65" r="1.5" fill={Colors.SciFiGold} className="animate-float" style={{ animationDelay: "0.5s" }} />
          </g>
        </svg>
      );

    // R&D: Heat Bat (ENEMY, Atmosphere)
    case "HEAT_BAT":
      return (
        <svg viewBox="0 0 70 50" width={size} height={size * 0.7} className="overflow-visible">
          <g className="animate-hover">
            <polygon points="35,15 42,28 35,38 28,28" fill={Colors.SciFiRed} fillOpacity="0.8" />
            <path d="M 28 28 Q 10 10 5 22 Q 18 35 28 28" fill={Colors.SciFiRed} fillOpacity="0.4" stroke={Colors.SciFiRed} strokeWidth="1" />
            <path d="M 42 28 Q 60 10 65 22 Q 52 35 42 28" fill={Colors.SciFiRed} fillOpacity="0.4" stroke={Colors.SciFiRed} strokeWidth="1" />
            <circle cx="32" cy="22" r="1.5" fill={Colors.SciFiGold} />
            <circle cx="38" cy="22" r="1.5" fill={Colors.SciFiGold} />
          </g>
        </svg>
      );

    // R&D: Mimic Platform (ENEMY, Platform Clone)
    case "MIMIC_PLATFORM":
      return (
        <svg viewBox="0 0 100 40" width={size * 1.5} height={size * 0.6} className="overflow-visible">
          <g className="animate-hover">
            <rect x="10" y="10" width="80" height="12" fill="#1a1a24" stroke={Colors.SciFiCyan} strokeWidth="1.5" rx="1" />
            <path d="M 25 10 L 32 22 L 40 10 M 60 10 L 67 22 L 72 10" fill="none" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.7" className="animate-pulse" />
            <circle cx="50" cy="16" r="2.5" fill={Colors.SciFiRed} className="animate-warning" />
          </g>
        </svg>
      );

    // R&D: Void Harvester (ENEMY, Resource Predator)
    case "VOID_HARVESTER":
      return (
        <svg viewBox="0 0 60 80" width={size * 0.75} height={size} className="overflow-visible">
          <g className="animate-hover-rotate">
            <path d="M 15,30 Q 30,5 45,30 L 40,50 L 20,50 Z" fill="#263238" stroke={Colors.SciFiPurple} strokeWidth="1.5" />
            <circle cx="30" cy="38" r="6" fill={Colors.SciFiPurple} fillOpacity="0.4" className="animate-pulse" />
            <circle cx="30" cy="38" r="2" fill="#FFFFFF" />
            <path d="M 20 50 Q 15 68 18 78" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.7" className="animate-pulse" />
            <path d="M 30 50 Q 30 70 32 78" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.7" className="animate-pulse" style={{ animationDelay: "0.2s" }} />
            <path d="M 40 50 Q 45 68 42 78" fill="none" stroke={Colors.SciFiPurple} strokeWidth="1.5" strokeOpacity="0.7" className="animate-pulse" style={{ animationDelay: "0.4s" }} />
          </g>
        </svg>
      );

    // R&D: Phase Wraith (ENEMY, Overheat Only)
    case "PHASE_WRAITH":
      return (
        <svg viewBox="0 0 50 80" width={size * 0.7} height={size * 1.1} className="overflow-visible">
          <g className="animate-pulse" opacity="0.6">
            <path d="M 25 10 Q 38 22 35 45 Q 32 60 42 75 Q 25 68 25 75 Q 25 68 8 75 Q 18 60 15 45 Q 12 22 25 10" fill="none" stroke={Colors.SciFiCyan} strokeWidth="1.5" strokeDasharray="3 3" />
            <circle cx="25" cy="35" r="10" fill={Colors.SciFiCyan} fillOpacity="0.15" />
            <circle cx="21" cy="22" r="1.5" fill={Colors.SciFiRed} className="animate-pulse" />
            <circle cx="29" cy="22" r="1.5" fill={Colors.SciFiRed} className="animate-pulse" />
          </g>
        </svg>
      );

    // R&D: Gravity Ram (ENEMY, Ambusher)
    case "GRAVITY_RAM":
      return (
        <svg viewBox="0 0 80 60" width={size * 1.3} height={size} className="overflow-visible">
          <g className="animate-hover">
            <polygon points="40,5 75,50 40,40 5,50" fill="#2c3e50" stroke={Colors.SciFiRed} strokeWidth="1.5" />
            <line x1="40" y1="5" x2="40" y2="0" stroke={Colors.SciFiRed} strokeWidth="2" />
            <line x1="15" y1="50" x2="25" y2="45" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.6" />
            <line x1="65" y1="50" x2="55" y2="45" stroke={Colors.SciFiRed} strokeWidth="1" strokeOpacity="0.6" />
          </g>
        </svg>
      );

    // R&D: Cryo-Mist (HAZARD, Atmosphere)
    case "CRYO_MIST":
      return (
        <svg viewBox="0 0 80 60" width={size * 1.3} height={size} className="overflow-visible">
          <g className="animate-float">
            <circle cx="30" cy="30" r="18" fill={Colors.SciFiCyan} fillOpacity="0.15" />
            <circle cx="50" cy="25" r="15" fill={Colors.SciFiCyan} fillOpacity="0.1" />
            <circle cx="42" cy="40" r="12" fill={Colors.SciFiCyan} fillOpacity="0.12" />
            <polygon points="28,26 32,24 30,32" fill="#FFFFFF" opacity="0.8" />
            <polygon points="48,22 52,20 50,28" fill="#FFFFFF" opacity="0.8" />
            <polygon points="40,36 44,34 42,42" fill="#FFFFFF" opacity="0.8" />
          </g>
        </svg>
      );

    // R&D: Mirror Shards (HAZARD, Deep Space)
    case "MIRROR_SHARDS":
      return (
        <svg viewBox="0 0 80 80" width={size} height={size} className="overflow-visible">
          <g className="animate-hover-rotate">
            <polygon points="30,15 45,30 38,35 25,20" fill={Colors.SciFiCyan} fillOpacity="0.25" stroke="#FFFFFF" strokeWidth="0.8" />
            <polygon points="55,45 68,52 60,60 48,50" fill={Colors.SciFiCyan} fillOpacity="0.2" stroke="#FFFFFF" strokeWidth="0.8" />
            <polygon points="25,48 35,58 28,65 18,55" fill={Colors.SciFiCyan} fillOpacity="0.3" stroke="#FFFFFF" strokeWidth="0.8" />
          </g>
        </svg>
      );

    // R&D: Gravity Shear (HAZARD, Deep Space)
    case "GRAVITY_SHEAR":
      return (
        <svg viewBox="0 0 80 80" width={size} height={size} className="overflow-visible">
          <g className="animate-pulse">
            <line x1="10" y1="40" x2="70" y2="40" stroke={Colors.SciFiPurple} strokeWidth="2" strokeDasharray="4 4" />
            <path d="M 40,15 L 40,32 M 35,22 L 40,15 L 45,22" fill="none" stroke={Colors.SciFiPurple} strokeWidth="2.5" />
            <path d="M 40,65 L 40,48 M 35,58 L 40,65 L 45,58" fill="none" stroke={Colors.SciFiPurple} strokeWidth="2.5" />
          </g>
        </svg>
      );

    default:
      return null;
  }
}
