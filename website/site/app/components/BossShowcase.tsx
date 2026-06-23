const bosses = [
  {
    name: "Command Cruiser",
    descriptor: "Platform Jammer",
    quote: "Locks your landing zones.",
  },
  {
    name: "Gatekeeper",
    descriptor: "Shield Sentinel",
    quote: "Guards the orbital boundary.",
  },
  {
    name: "Leviathan",
    descriptor: "Apex Predator",
    quote: "The serpent coils.",
  },
  {
    name: "Star-Eater",
    descriptor: "Binary Devourer",
    quote: "Hunts in pairs.",
  },
  {
    name: "Void Engine",
    descriptor: "Reality Warp",
    quote: "Inverts your controls.",
  },
  {
    name: "The Signal",
    descriptor: "The Source",
    quote: "Where truth is found.",
  },
];

export default function BossShowcase() {
  return (
    <section className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(213,0,249,0.12),transparent_30%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Encounters</p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
            Six bosses. Each with purpose.
          </h2>
          <p className="max-w-2xl text-base leading-7 text-slate-300">
            Phase-based encounters with destructible weak points. Every 1,500 points of altitude brings a new guardian.
          </p>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {bosses.map((boss) => (
            <article
              key={boss.name}
              className="group rounded-[2rem] border border-purple-400/20 bg-white/5 p-6 transition hover:border-purple-400/50 hover:bg-purple-500/5"
            >
              <div className="space-y-3">
                <div>
                  <p className="text-xs uppercase tracking-[0.25em] text-purple-300/80">{boss.descriptor}</p>
                  <h3 className="mt-2 text-xl font-semibold text-white">{boss.name}</h3>
                </div>
                <p className="italic text-sm text-slate-400">"{boss.quote}"</p>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
