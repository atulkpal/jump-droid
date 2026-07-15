export default function MissionControl() {
  return (
    <section id="mission-control" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(255,234,0,0.14),transparent_30%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="grid gap-12 lg:grid-cols-[1.4fr_1fr]">
          <div className="space-y-6">
            <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">Mission Control</p>
            <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
              Join the expedition. Keep the signal alive.
            </h2>
            <p className="max-w-2xl text-base leading-7 text-slate-300">
              Stay connected with development updates, community missions, and exclusive launch briefings.
            </p>
            <div className="grid gap-4 sm:grid-cols-2">
              {[
                { label: "Discord", value: "Real-time crew chatter" },
                { label: "Newsletter", value: "Mission briefings and updates" },
              ].map((item) => (
                <div key={item.label} className="rounded-3xl border border-cyan-300/10 bg-black/60 p-5">
                  <p className="text-xs uppercase tracking-[0.3em] text-cyan-200/80">{item.label}</p>
                  <p className="mt-3 text-sm text-slate-300">{item.value}</p>
                </div>
              ))}
            </div>
          </div>
          <div className="rounded-[2rem] border border-cyan-300/10 bg-white/5 p-8 shadow-[0_0_60px_rgba(0,229,255,0.1)]">
            <p className="uppercase tracking-[0.35em] text-cyan-200/80">Join the Expedition</p>
            <h3 className="mt-4 text-3xl font-semibold text-white">Newsletter Protocol</h3>
            <p className="mt-4 text-sm leading-7 text-slate-300">
              Receive top-secret updates, event notifications, and launch news directly in your inbox.
            </p>
            <form className="mt-8 space-y-4">
              <label className="block text-sm text-slate-300">
                Email address
                <input
                  type="email"
                  placeholder="you@signal.network"
                  className="mt-3 w-full rounded-3xl border border-slate-700 bg-slate-950/90 px-4 py-3 text-sm text-white outline-none transition focus:border-cyan-400/60 focus:ring-2 focus:ring-cyan-400/15"
                />
              </label>
              <button
                type="submit"
                className="w-full rounded-full bg-cyan-400 px-5 py-3 text-sm font-semibold uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300"
              >
                Subscribe
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>
  );
}
