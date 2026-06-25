const discoveries = [
  { category: "Platforms", count: "5 types", description: "Platform mechanics you'll master" },
  { category: "Threats", count: "8 enemies", description: "The adversaries you'll face" },
  { category: "Rockets", count: "4 builds", description: "Technical specs and traits" },
  { category: "Artifacts", count: "5 items", description: "Rare power-ups you'll find" },
  { category: "Lore", count: "12 entries", description: "The story of the Signal" },
  { category: "Mechanics", count: "8+ systems", description: "How the game works" },
];

export default function DiscoveryArchive() {
  return (
    <section id="archive" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(213,0,249,0.14),transparent_32%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Codex</p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
            43 discoveries. One expedition.
          </h2>
          <p className="max-w-2xl text-base leading-7 text-slate-300">
            Unlock permanent discoveries as you climb. Each one expands your understanding of the ascent and the Signal.
          </p>
        </div>
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {discoveries.map((discovery) => (
            <article
              key={discovery.category}
              className="group rounded-[2rem] border border-cyan-300/10 bg-black/60 p-6 transition hover:border-cyan-400/40 hover:bg-white/5"
            >
              <p className="text-xs uppercase tracking-[0.35em] text-cyan-200/80">{discovery.category}</p>
              <h3 className="mt-4 text-lg font-semibold text-white">{discovery.count}</h3>
              <p className="mt-3 text-sm leading-6 text-slate-300 opacity-90 group-hover:text-cyan-100">
                {discovery.description}
              </p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
