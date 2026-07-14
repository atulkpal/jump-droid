import PlatformSVG from "./game/PlatformSVG";

const PLATFORMS = [
  { type: "NORMAL" as const, name: "Normal", desc: "Reliable standard landing platform." },
  { type: "MOVING" as const, name: "Moving", desc: "Shifts horizontally. Timing is everything." },
  { type: "BOOST" as const, name: "Boost", desc: "Launches rocket upward at high velocity." },
  { type: "ICE" as const, name: "Ice", desc: "Slippery surface. Reduced friction control." },
  { type: "BREAKABLE" as const, name: "Breakable", desc: "Cracks under weight. Launch quickly." },
  { type: "PHASE" as const, name: "Phase", desc: "Flickers in and out of reality." },
  { type: "FUEL" as const, name: "Fuel", desc: "Replenishes fuel reserves on contact." },
  { type: "COOLING" as const, name: "Cooling", desc: "Reduces rocket heat buildup." },
  { type: "STABILITY" as const, name: "Stability", desc: "Grants temporary shield stabilization." },
  { type: "MAGNETIC" as const, name: "Magnetic", desc: "Attracts rocket from nearby range." },
  { type: "CONVEYOR" as const, name: "Conveyor", desc: "Forces horizontal drift." },
  { type: "MIMIC" as const, name: "Mimic", desc: "Disguised as standard. Shatters on landing." },
];

export default function PlatformsSection() {
  return (
    <section id="platforms" className="px-6 py-20">
      <div className="mx-auto max-w-6xl">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.35em] text-slate-500">
          Platforms
        </p>
        <h2 className="mb-8 text-3xl font-black tracking-tight text-white md:text-4xl">
          Twelve types. Adapt or fall.
        </h2>

        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
          {PLATFORMS.map((p) => (
            <div
              key={p.type}
              className="rounded-xl border border-white/10 bg-white/[0.03] p-4 flex flex-col items-center gap-2"
            >
              <PlatformSVG type={p.type} width={80} height={14} animated={false} />
              <p className="text-sm font-bold text-white">{p.name}</p>
              <p className="text-xs text-slate-500 text-center leading-relaxed">{p.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
