"use client";

import { ReactNode, useRef, useEffect } from "react";

interface ScrollContainerProps {
  children: ReactNode;
  className?: string;
  onScroll?: (scrollTop: number) => void;
  throttleMs?: number;
}

export default function ScrollContainer({ children, className = "", onScroll, throttleMs = 16 }: ScrollContainerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const lastScrollTop = useRef(0);
  const ticking = useRef(false);

  const handleScroll = () => {
    if (!containerRef.current) return;
    lastScrollTop.current = containerRef.current.scrollTop;

    if (!ticking.current) {
      ticking.current = true;
      requestAnimationFrame(() => {
        if (onScroll) onScroll(lastScrollTop.current);
        ticking.current = false;
      });
    }
  };

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    el.addEventListener("scroll", handleScroll, { passive: true });
    // Touch support — ensure smooth scrolling on mobile
    (el.style as any).webkitOverflowScrolling = "touch";
    return () => el.removeEventListener("scroll", handleScroll);
  }, [onScroll]);

  return (
    <div
      ref={containerRef}
      className={`h-screen w-full overflow-y-auto overflow-x-hidden ${className}`}
    >
      {children}
    </div>
  );
}
