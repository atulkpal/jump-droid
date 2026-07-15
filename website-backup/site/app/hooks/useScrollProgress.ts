"use client";

import { useState, useEffect, useCallback } from "react";

export function useScrollProgress(containerRef: React.RefObject<HTMLElement | null>) {
  const [scrollTop, setScrollTop] = useState(0);
  const [scrollHeight, setScrollHeight] = useState(0);
  const [clientHeight, setClientHeight] = useState(0);

  const handleScroll = useCallback(() => {
    const el = containerRef.current;
    if (!el) return;
    setScrollTop(el.scrollTop);
    setScrollHeight(el.scrollHeight);
    setClientHeight(el.clientHeight);
  }, [containerRef]);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    setScrollHeight(el.scrollHeight);
    setClientHeight(el.clientHeight);
    setScrollTop(el.scrollTop);

    el.addEventListener("scroll", handleScroll, { passive: true });
    return () => el.removeEventListener("scroll", handleScroll);
  }, [handleScroll]);

  const maxScroll = Math.max(scrollHeight - clientHeight, 1);
  const progress = Math.min(scrollTop / maxScroll, 1);

  return { scrollTop, progress };
}