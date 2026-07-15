export default function ProgressionSystems() {
  return (
    <section className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(255,234,0,0.08),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Systems</p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
            Why you climb again.
          </h2>
          <p className="max-w-2xl text-base leading-7 text-slate-300">
            Every expedition brings new challenges, unlocks, and discoveries.
          </p>
        </div>
        <div className="grid gap-6 lg:grid-cols-3">
          {[
            {
              title: "Rocket Progression",
              points: [
                "Explorer — Your first climb",
                "Striker — Unlocked at 2,000 meters",
                "Heavy — Unlocked at 5,000 meters",
                "Prototype — Unlocked at 10,000 meters",
              ],
              cta: "Each rocket feels completely different.",
            },
            {
              title: "Discovery & Codex",
              points: [
                "Platforms (5 types)",
                "Threats (8 enemy types)",
                "Rockets (technical specs)",
                "Lore (12 signal entries)",
                "And 18 more discoveries...",
              ],
              cta: "43 total entries. Unlock them all.",
            },
            {
              title: "Missions & Goals",
              points: [
                "Exploration — Reach new zones",
                "Platforming — Master your combos",
                "Survival — Push your limits",
                "Each run offers new objectives.",
              ],
              cta: "Every expedition is different.",
            },
          ].map((system) => (
            <article key={system.title} className="rounded-[2rem] border border-cyan-300/10 bg-white/5 p-6">
              <p className="text-xs uppercase tracking-[0.25em] text-cyan-200/80">{system.title}</p>
              <ul className="mt-4 space-y-2">
                {system.points.map((point) => (
                  <li key={point} className="text-sm text-slate-300">
                    • {point}
                  </li>
                ))}
              </ul>
              <p className="mt-4 text-sm italic text-slate-400">{system.cta}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
