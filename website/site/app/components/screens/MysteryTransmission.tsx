import { TRANSMISSION_LINES } from "@/app/data/site-content";

export default function MysteryTransmission() {
  return (
    <section className="flex min-h-dvh w-full items-center justify-center px-6">
      <div className="mx-auto max-w-lg">
        <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-8 text-center">
          Incoming Transmission
        </p>
        <div className="space-y-3">
          {TRANSMISSION_LINES.map((line, i) => (
            <p
              key={i}
              className="font-mono text-sm sm:text-base leading-relaxed text-slate-400 opacity-0 animate-fade-in-up"
              style={{
                animationDelay: `${0.3 + i * 0.25}s`,
                animationFillMode: "forwards",
              }}
            >
              <span className="text-cyan-400/30 mr-2">&#62;</span>
              {line}
            </p>
          ))}
        </div>
      </div>
    </section>
  );
}
