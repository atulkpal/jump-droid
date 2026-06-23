export default function HeroSection() {
  return (
    <section id="hero" className="relative overflow-hidden py-32 sm:py-40">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.16),_transparent_32%)]" />
      <div className="absolute left-0 top-0 h-full w-full bg-[linear-gradient(180deg,rgba(0,0,0,0.12),transparent_40%,rgba(0,0,0,0.85))]" />
      <div className="relative mx-auto flex max-w-6xl flex-col gap-16 px-6 text-center text-slate-100 sm:px-8 lg:px-12">
        <div className="space-y-6">
          <p className="text-sm uppercase tracking-[0.45em] text-cyan-300/90">The Signal From the Void</p>
          <h1 className="mx-auto max-w-4xl text-5xl font-semibold leading-[1.03] tracking-tight text-white sm:text-6xl lg:text-7xl">
            Pilot the ultimate droid explorer. Break atmosphere. Reach the void.
          </h1>
          <p className="mx-auto max-w-3xl text-lg leading-8 text-slate-300 sm:text-xl">
            Touch to thrust. Manage fuel, heat, and shield as you climb through 6 atmospheric zones. Land on platforms, build combos, face bosses, unlock new rockets, and discover the truth hidden in the void.
          </p>
        </div>
        <div className="mx-auto flex flex-col items-center justify-center gap-4 sm:flex-row">
          <a
            href="#download"
            className="inline-flex min-w-[180px] items-center justify-center rounded-full bg-cyan-400 px-6 py-3 text-sm font-semibold uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300"
          >
            Download on Google Play
          </a>
          <a
            href="#hangar"
            className="inline-flex min-w-[180px] items-center justify-center rounded-full border border-cyan-300/50 bg-white/5 px-6 py-3 text-sm font-semibold uppercase tracking-[0.2em] text-cyan-100 transition hover:bg-white/10"
          >
            Explore the Hangar
          </a>
        </div>
        <div className="grid gap-4 rounded-3xl border border-cyan-200/10 bg-white/5 p-6 text-left shadow-[0_0_80px_rgba(0,229,255,0.08)] sm:grid-cols-3">
          {[
            { label: "Exploration First", value: "Vertical odyssey over score." },
            { label: "Tactical Mastery", value: "Fuel, heat, integrity decisions." },
            { label: "Celestial Conflict", value: "Bosses with destructible weak points." },
          ].map((item) => (
            <div key={item.label}>
              <p className="text-xs uppercase tracking-[0.25em] text-cyan-200/80">{item.label}</p>
              <p className="mt-3 text-sm leading-6 text-slate-200">{item.value}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
