const rockets = [
  {
    name: "Balanced",
    description: "Adaptable ascent with even thrust, fuel, and heat.",
    stats: [
      { label: "Thrust", value: "Balanced" },
      { label: "Fuel", value: "Moderate" },
      { label: "Heat", value: "Controlled" },
    ],
  },
  {
    name: "Scout",
    description: "Fast, agile, and built for atmosphere control.",
    stats: [
      { label: "Thrust", value: "High" },
      { label: "Fuel", value: "Light" },
      { label: "Heat", value: "Sensitive" },
    ],
  },
  {
    name: "Tank",
    description: "Durable hull and heavy shields for extended conflict.",
    stats: [
      { label: "Thrust", value: "Steady" },
      { label: "Fuel", value: "Heavy" },
      { label: "Heat", value: "Slow Rise" },
    ],
  },
  {
    name: "Experimental",
    description: "Unpredictable systems for players who dare the void.",
    stats: [
      { label: "Thrust", value: "Variable" },
      { label: "Fuel", value: "Reactive" },
      { label: "Heat", value: "Volatile" },
    ],
  },
];

export default function RocketShowcase() {
  return (
    <section id="hangar" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.14),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Hangar</p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
            Choose your build. Find your ascent.
          </h2>
          <p className="max-w-2xl text-base leading-7 text-slate-300">
            The four rocket classes change how you climb, fight, and survive the void.
          </p>
        </div>
        <div className="grid gap-6 lg:grid-cols-2">
          {rockets.map((rocket) => (
            <article key={rocket.name} className="rounded-[2rem] border border-cyan-300/10 bg-slate-950/70 p-6 transition hover:border-cyan-400/50 hover:bg-slate-900/95">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-xs uppercase tracking-[0.35em] text-cyan-200/80">{rocket.name}</p>
                  <h3 className="mt-3 text-2xl font-semibold text-white">{rocket.description}</h3>
                </div>
                <div className="rounded-full border border-cyan-400/20 bg-cyan-400/10 px-4 py-2 text-xs uppercase tracking-[0.25em] text-cyan-100">
                  Build
                </div>
              </div>
              <div className="mt-6 grid gap-3 sm:grid-cols-3">
                {rocket.stats.map((stat) => (
                  <div key={stat.label} className="rounded-3xl bg-black/70 p-4">
                    <p className="text-xs uppercase tracking-[0.35em] text-cyan-300/80">{stat.label}</p>
                    <p className="mt-2 text-sm font-semibold text-white">{stat.value}</p>
                  </div>
                ))}
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
