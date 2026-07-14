import Carousel from "./Carousel";
import RocketSVG from "./game/RocketSVG";

const ROCKETS = [
  { type: "BALANCED" as const, name: "Explorer", unlock: "Start Here", traitName: "Sensor Array", traitDesc: "Native +20% discovery range, revealing secrets earlier.", stats: { thrust: "1.0x", fuel: "1.0x", heat: "1.0x" } },
  { type: "SCOUT" as const, name: "Striker", unlock: "2,000m", traitName: "Target Lock", traitDesc: "Enables precision target lock on enemy weak points.", stats: { thrust: "1.25x", fuel: "0.7x", heat: "0.9x" } },
  { type: "TANK" as const, name: "Heavy", unlock: "5,000m", traitName: "Kinetic Mass", traitDesc: "Triggers shockwaves on weak point destruction.", stats: { thrust: "0.85x", fuel: "1.5x", heat: "0.8x" } },
  { type: "EXPERIMENTAL" as const, name: "Prototype", unlock: "10,000m", traitName: "Overclocked Core", traitDesc: "Retain steering authority even while overheated.", stats: { thrust: "1.5x", fuel: "1.0x", heat: "1.4x" } },
  { type: "STEALTH" as const, name: "Stealth", unlock: "12,000m", traitName: "Cloaking Field", traitDesc: "Reduces enemy detection range by 40%.", stats: { thrust: "1.0x", fuel: "0.8x", heat: "1.0x" } },
  { type: "REFLECTOR" as const, name: "Reflector", unlock: "15,000m", traitName: "Reactive Armor", traitDesc: "Deals damage on physical collision.", stats: { thrust: "1.1x", fuel: "1.0x", heat: "1.2x" } },
];

function statValue(val: string): number {
  return parseFloat(val.replace("x", ""));
}

export default function RocketsSection() {
  return (
    <section id="rockets" className="relative min-h-dvh flex flex-col justify-center overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_80%,rgba(0,229,255,0.06),transparent_60%)]" />

      <Carousel
        label="Fleet"
        items={ROCKETS.map((r) => {
          const isAdvanced = r.type === "STEALTH" || r.type === "REFLECTOR";
          return (
            <div
              key={r.type}
              className={`rounded-2xl border p-6 flex flex-col items-center gap-3 h-full md:p-8 ${
                isAdvanced
                  ? "border-purple-500/20 bg-purple-500/[0.03]"
                  : "border-cyan-400/15 bg-white/[0.03]"
              }`}
            >
              <span className="text-[11px] font-black uppercase tracking-wider text-cyan-300/70">
                {r.unlock}
              </span>
              <div className="flex items-center justify-center w-full py-3">
                <RocketSVG type={r.type} size={120} thrusting={true} shield={80} heat={30} showFlame={true} />
              </div>
              <p className="text-xl font-bold text-white">{r.name}</p>

              <div className="w-full space-y-2 mt-2">
                {[
                  { label: "Thrust", value: statValue(r.stats.thrust) },
                  { label: "Fuel", value: statValue(r.stats.fuel) },
                  { label: "Heat Limit", value: statValue(r.stats.heat) },
                ].map((s) => (
                  <div key={s.label} className="flex items-center gap-3">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 w-16">
                      {s.label}
                    </span>
                    <div className="flex-1 h-2 rounded-full bg-slate-800 overflow-hidden">
                      <div
                        className="h-full rounded-full bg-cyan-400 transition-all"
                        style={{ width: `${Math.min(s.value / 1.5 * 100, 100)}%` }}
                      />
                    </div>
                    <span className="text-[11px] font-bold text-white w-8 text-right">
                      {r.stats[Object.keys(r.stats)[s.label === "Thrust" ? 0 : s.label === "Fuel" ? 1 : 2] as keyof typeof r.stats]}
                    </span>
                  </div>
                ))}
              </div>

              <div className={`w-full border rounded-xl p-3 mt-1 ${
                isAdvanced ? "bg-purple-500/[0.05] border-purple-500/10" : "bg-cyan-400/[0.05] border-cyan-400/10"
              }`}>
                <p className="text-[11px] font-black uppercase tracking-widest text-cyan-300">
                  {r.traitName}
                </p>
                <p className="text-sm text-slate-400 mt-1 leading-relaxed">{r.traitDesc}</p>
              </div>
            </div>
          );
        })}
      />
    </section>
  );
}
