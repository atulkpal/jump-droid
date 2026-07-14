import Carousel from "./Carousel";
import PlatformSVG from "./game/PlatformSVG";

const PLATFORMS = [
  { type: "NORMAL" as const, name: "Normal", desc: "Standard landing platform. Reliable and consistent." },
  { type: "MOVING" as const, name: "Moving", desc: "Shifts horizontally. Timing is everything." },
  { type: "BOOST" as const, name: "Boost", desc: "Launches rocket upward at high velocity." },
  { type: "ICE" as const, name: "Ice", desc: "Slippery surface. Reduced friction control." },
  { type: "BREAKABLE" as const, name: "Breakable", desc: "Cracks under weight. Land and launch quickly." },
  { type: "PHASE" as const, name: "Phase", desc: "Flickers in and out of reality. Unpredictable." },
  { type: "FUEL" as const, name: "Fuel", desc: "Replenishes fuel reserves on contact." },
  { type: "COOLING" as const, name: "Cooling", desc: "Reduces rocket heat buildup." },
  { type: "STABILITY" as const, name: "Stability", desc: "Grants temporary shield stabilization." },
  { type: "MAGNETIC" as const, name: "Magnetic", desc: "Attracts rocket from nearby range." },
  { type: "CONVEYOR" as const, name: "Conveyor", desc: "Forces horizontal drift. Adjust steering or use velocity." },
  { type: "MIMIC" as const, name: "Mimic", desc: "Disguised as standard. Shatters on landing, dealing damage." },
];

export default function PlatformsSection() {
  return (
    <section id="platforms" className="relative min-h-dvh flex flex-col justify-center overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(0,229,255,0.06),transparent_60%)]" />

      <Carousel
        label="Platforms"
        items={PLATFORMS.map((p) => (
          <div
            key={p.type}
            className="rounded-2xl border border-white/10 bg-white/[0.03] p-6 flex flex-col items-center gap-4 h-full md:p-8"
          >
            <div className="flex items-center justify-center w-full py-6">
              <PlatformSVG type={p.type} width={160} height={24} animated={true} />
            </div>
            <p className="text-lg font-bold text-white">{p.name}</p>
            <p className="text-sm text-slate-400 text-center leading-relaxed">{p.desc}</p>
          </div>
        ))}
      />
    </section>
  );
}
