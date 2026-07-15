"use client";

export default function MoonGlow() {
  return (
    <div className="absolute top-4 right-4 sm:top-8 sm:right-8 z-0 pointer-events-none select-none">
      {/* Light rays */}
      {[0, 1, 2, 3].map((i) => (
        <div
          key={i}
          className="absolute -top-12 -right-12 w-64 h-64 sm:w-96 sm:h-96 animate-moon-ray"
          style={{
            background: `linear-gradient(${220 + i * 12}deg, rgba(200,220,255,0.03) 0%, rgba(0,229,255,0.01) 40%, transparent 70%)`,
            clipPath: `polygon(${40 + i * 12}% 0%, 100% ${10 + i * 8}%, 100% 100%, ${50 + i * 8}% 100%)`,
            animationDelay: `${i * 1.5}s`,
            animationDuration: "6s",
          }}
        />
      ))}

      {/* Glow aura */}
      <div
        className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-40 h-40 sm:w-56 sm:h-56 rounded-full animate-moon-pulse"
        style={{
          background: "radial-gradient(circle, rgba(200,220,255,0.15) 0%, rgba(0,229,255,0.06) 40%, transparent 70%)",
          boxShadow: "0 0 60px rgba(200,220,255,0.08), 0 0 120px rgba(0,229,255,0.04)",
        }}
      />

      {/* Crescent moon SVG */}
      <div className="relative w-16 h-16 sm:w-24 sm:h-24 md:w-36 md:h-36 animate-moon-float">
        <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-[0_0_20px_rgba(200,220,255,0.3)]">
          <defs>
            <radialGradient id="moonSurface" cx="40%" cy="40%" r="60%">
              <stop offset="0%" stopColor="#f8fbff" />
              <stop offset="60%" stopColor="#dce6f5" />
              <stop offset="100%" stopColor="#b0c4de" />
            </radialGradient>
            <mask id="crescentMask">
              <rect width="100" height="100" fill="white" />
              <circle cx="62" cy="38" r="36" fill="black" />
            </mask>
          </defs>
          <circle cx="50" cy="50" r="40" fill="url(#moonSurface)" mask="url(#crescentMask)" />
        </svg>
      </div>

      {/* Stars */}
      <div className="absolute inset-0">
        {[
          { x: 15, y: 10, d: 1.5, delay: 0 },
          { x: 22, y: 65, d: 1, delay: 1.2 },
          { x: 78, y: 15, d: 1.2, delay: 0.6 },
          { x: 85, y: 55, d: 0.8, delay: 2.1 },
          { x: 8, y: 38, d: 1.3, delay: 1.8 },
        ].map((star, i) => (
          <div
            key={i}
            className="absolute rounded-full bg-white animate-star-twinkle"
            style={{
              left: `${star.x}%`,
              top: `${star.y}%`,
              width: `${star.d}px`,
              height: `${star.d}px`,
              animationDelay: `${star.delay}s`,
              boxShadow: `0 0 ${star.d * 3}px rgba(200,220,255,0.6)`,
            }}
          />
        ))}
      </div>
    </div>
  );
}
