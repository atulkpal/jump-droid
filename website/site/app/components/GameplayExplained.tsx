export default function GameplayExplained() {
  return (
    <section className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(0,229,255,0.08),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="space-y-8">
          <div className="max-w-2xl space-y-4">
            <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">How It Works</p>
            <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
              The mechanics of ascent.
            </h2>
          </div>
          <div className="grid gap-6 lg:grid-cols-2">
            {[
              {
                step: "1",
                title: "Touch to Thrust",
                description: "Hold your finger to burn fuel and climb against gravity. Release to coast and conserve.",
              },
              {
                step: "2",
                title: "Land & Combo",
                description: "Each successful platform landing builds your streak. Five consecutive touches restore your shield.",
              },
              {
                step: "3",
                title: "Manage Resources",
                description: "Fuel (how long you thrust), Heat (engine cooldown), Shield (damage buffer). Every decision matters.",
              },
              {
                step: "4",
                title: "Face Bosses",
                description: "Every 1,500 points, a boss awaits. Learn their patterns. Find their weak points. Survive the encounter.",
              },
            ].map((item) => (
              <article
                key={item.step}
                className="rounded-[2rem] border border-cyan-300/10 bg-white/5 p-6 transition hover:border-cyan-400/30 hover:bg-white/[0.08]"
              >
                <div className="flex items-start gap-4">
                  <div className="rounded-full bg-cyan-400/20 px-4 py-2 text-lg font-bold text-cyan-300">
                    {item.step}
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">{item.title}</h3>
                    <p className="mt-2 text-sm leading-6 text-slate-300">{item.description}</p>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
