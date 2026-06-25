"use client";

import { useState, useEffect } from "react";
import dynamic from "next/dynamic";

const EarthBackground = dynamic(() => import("./EarthBackground"));
const CloudLayerBackground = dynamic(() => import("./CloudLayerBackground"));
const UpperAtmosphereBackground = dynamic(() => import("./UpperAtmosphereBackground"));
const OrbitBackground = dynamic(() => import("./OrbitBackground"));
const FoundryBackground = dynamic(() => import("./FoundryBackground"));
const DeepSpaceBackground = dynamic(() => import("./DeepSpaceBackground"));
const ChronoRiftBackground = dynamic(() => import("./ChronoRiftBackground"));
const VoidBackground = dynamic(() => import("./VoidBackground"));

const ZONES = [
  { name: "Earth", threshold: 0 },
  { name: "Cloud Layer", threshold: 500 },
  { name: "Upper Atmosphere", threshold: 1500 },
  { name: "Orbit", threshold: 4000 },
  { name: "The Foundry", threshold: 5000 },
  { name: "Deep Space", threshold: 8000 },
  { name: "Chrono-Rift", threshold: 13000 },
  { name: "The Void", threshold: 15000 },
];

interface ZoneBackgroundsProps {
  altitude: number;
}

const backgrounds: Record<string, React.ComponentType<{ altitude: number }>> = {
  "Earth": EarthBackground,
  "Cloud Layer": CloudLayerBackground,
  "Upper Atmosphere": UpperAtmosphereBackground,
  "Orbit": OrbitBackground,
  "The Foundry": FoundryBackground,
  "Deep Space": DeepSpaceBackground,
  "Chrono-Rift": ChronoRiftBackground,
  "The Void": VoidBackground,
};

export default function ZoneBackgrounds({ altitude }: ZoneBackgroundsProps) {
  let zoneIdx = 0;
  for (let i = 0; i < ZONES.length; i++) {
    if (altitude >= ZONES[i].threshold) zoneIdx = i;
  }

  const currentZone = ZONES[zoneIdx];
  const nextZone = zoneIdx < ZONES.length - 1 ? ZONES[zoneIdx + 1] : null;

  let transitionProgress = 0;
  if (nextZone) {
    const range = nextZone.threshold - currentZone.threshold;
    const transitionWindow = Math.min(300, range * 0.4);
    const transitionStart = nextZone.threshold - transitionWindow;

    if (altitude >= transitionStart) {
      transitionProgress = Math.min(1, Math.max(0, (altitude - transitionStart) / transitionWindow));
    }
  }

  return (
    <div className="fixed inset-0 z-0 pointer-events-none bg-black">
      {ZONES.map((z, idx) => {
        let opacity = 0;
        if (idx === zoneIdx) {
          opacity = 1 - transitionProgress;
        } else if (nextZone && idx === zoneIdx + 1) {
          opacity = transitionProgress;
        }

        if (opacity <= 0) return null;

        const BgComp = backgrounds[z.name];
        return (
          <div 
            key={z.name} 
            className="absolute inset-0 transition-opacity duration-300 ease-out" 
            style={{ opacity }}
          >
            <BgComp altitude={altitude} />
          </div>
        );
      })}
    </div>
  );
}