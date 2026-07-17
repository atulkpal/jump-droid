"use client";

import { useState, useEffect } from "react";
import { HERO } from "@/app/data/site-content";

export default function BetaTagline() {
  const [index, setIndex] = useState(0);
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const interval = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setIndex((i) => (i + 1) % HERO.taglines.length);
        setVisible(true);
      }, 400);
    }, 4500);
    return () => clearInterval(interval);
  }, []);

  return (
    <p
      className="font-mono text-sm tracking-[0.1em] text-cyan-300/90 font-semibold transition-opacity duration-500 text-center"
      style={{ opacity: visible ? 1 : 0 }}
    >
      {HERO.taglines[index]}
    </p>
  );
}
