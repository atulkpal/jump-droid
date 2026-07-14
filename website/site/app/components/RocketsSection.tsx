import RocketSVG from "./game/RocketSVG";

const ROCKETS = [
  { type: "BALANCED" as const, name: "Explorer", unlock: "Start Here", trait: "Sensor Array", traitDesc: "+20% discovery range.", stats: { thrust: 1.0, fuel: 1.0, heat: 1.0 } },
  { type: "SCOUT" as const, name: "Striker", unlock: "2,000m", trait: "Target Lock", traitDesc: "Precision target lock on weak points.", stats: { thrust: 1.25, fuel: 0.7, heat: 0.9 } },
  { type: "TANK" as const, name: "Heavy", unlock: "5,000m", trait: "Kinetic Mass", traitDesc: "Shockwaves on weak point destruction.", stats: { thrust: 0.85, fuel: 1.5, heat: 0.8 } },
  { type: "EXPERIMENTAL" as const, name: "Prototype", unlock: "10,000m", trait: "Overclocked Core", traitDesc: "Steering authority even when overheated.", stats: { thrust: 1.5, fuel: 1.0, heat: 1.4 } },
  { type: "STEALTH" as const, name: "Stealth", unlock: "12,000m", trait: "Cloaking Field", traitDesc: "40% reduced enemy detection range.", stats: { thrust: 1.0, fuel: 0.8, heat: 1.0 } },
  { type: "REFLECTOR" as const, name: "Reflector", unlock: "15,000m", trait: "Reactive Armor", traitDesc: "Damage on physical collision.", stats: { thrust: 1.1, fuel: 1.0, heat: 1.2 } },
];

const statLabels = ["Thrust", "Fuel", "Heat Limit"] as const;

export default function RocketsSection() {
  return (
    <section id="rockets" className="px-6 py-20">
      <div className="mx-auto max-w-6xl">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.35em] text-cyan-300">
          Fleet
        </p>
        <h2 className="mb-8 text-3xl font-black tracking-tight text-white md:text-4xl">
          Spaceship Evolution
        </h2>

        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {ROCKETS.map((r) => {
            const isAdvanced = r.type === "STEALTH" || r.type === "REFLECTOR";
            const stats = [r.stats.thrust, r.stats.fuel, r.stats.heat];
            const maxVal = Math.max(...stats, 1.5);

            return (
              <div
                key={r.type}
                className={`rounded-xl border p-5 flex flex-col gap-3 ${
                  isAdvanced
                    ? "border-purple-500/20 bg-purple-500/[0.03]"
                    : "border-white/10 bg-white/[0.03]"
                }`}
              >
                <span className="text-xs font-black uppercase tracking-wider text-cyan-300/70">
                  {r.unlock}
                </span>

                <div className="flex items-center justify-center py-2">
                  <RocketSVG type={r.type} size={100} thrusting={true} shield={80} heat={30} showFlame={true} />
                </div>

                <p className="text-lg font-bold text-white">{r.name}</p>

                <div className="space-y-2">
                  {statLabels.map((label, i) => (
                    <div key={label} className="flex items-center gap-2">
                      <span className="text-xs text-slate-500 w-16">{label}</span>
                      <div className="flex-1 h-2 rounded-full bg-slate-800 overflow-hidden">
                        <div
                          className="h-full rounded-full bg-cyan-400"
                          style={{ width: `${(stats[i] / maxVal) * 100}%` }}
                        />
                      </div>
                      <span className="text-xs font-bold text-white w-6 text-right">
                        {stats[i].toFixed(2)}x
                      </span>
                    </div>
                  ))}
                </div>

                <div className={`border rounded-lg p-3 ${
                  isAdvanced ? "bg-purple-500/[0.05] border-purple-500/10" : "bg-cyan-400/[0.05] border-cyan-400/10"
                }`}>
                  <p className="text-xs font-black uppercase tracking-wider text-cyan-300">{r.trait}</p>
                  <p className="text-xs text-slate-400 mt-1">{r.traitDesc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
