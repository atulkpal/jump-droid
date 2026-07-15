"use client";

import { useScroll, useTransform } from "framer-motion";

export function useParallax(speed: number) {
  const { scrollY } = useScroll();
  return useTransform(scrollY, [0, 1000], [0, speed * 1000]);
}