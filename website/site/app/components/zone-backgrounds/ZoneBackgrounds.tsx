"use client";

import { useState, useEffect } from "react";
import dynamic from "next/dynamic";

const EarthBackground = dynamic(() => import("./EarthBackground"));
const CloudLayerBackground = dynamic(() => import("./CloudLayerBackground"));
const UpperAtmosphereBackground = dynamic(() => import("./UpperAtmosphereBackground"));
const OrbitBackground = dynamic(() => import("./OrbitBackground"));
const DeepSpaceBackground = dynamic(() => import("./DeepSpaceBackground"));
const VoidBackground = dynamic(() => import("./VoidBackground"));

const ZONES = [
  { name: "Earth", threshold: 0 },
  { name: "Cloud Layer", threshold: 500 },
  { name: "Upper Atmosphere", threshold: 1500 },
  { name: "Orbit", threshold: 4000 },
  { name: "Deep Space", threshold: 8000 },
  { name: "The Void", threshold: 15000 },
];

interface ZoneBackgroundsProps {
  altitude: number;
}

export default function ZoneBackgrounds({ altitude }: ZoneBackgroundsProps) {
  let zoneName = "Earth";
  for (const z of ZONES) { if (altitude >= z.threshold) zoneName = z.name; }

  const backgrounds: Record<string, React.ReactNode> = {
    "Earth": <EarthBackground />,
    "Cloud Layer": <CloudLayerBackground />,
    "Upper Atmosphere": <UpperAtmosphereBackground />,
    "Orbit": <OrbitBackground />,
    "Deep Space": <DeepSpaceBackground />,
    "The Void": <VoidBackground />,
  };

  return (
    <div className="fixed inset-0 z-0 pointer-events-none">
      {backgrounds[zoneName]}
    </div>
  );
}