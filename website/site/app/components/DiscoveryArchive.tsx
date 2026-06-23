const fragments = [
  { title: "Fragment 01", subtitle: "The First Signal" },
  { title: "Fragment 02", subtitle: "Cloud Pulse" },
  { title: "Fragment 03", subtitle: "Gatekeeper Coordinates" },
  { title: "Fragment 04", subtitle: "Void Whale Echo" },
  { title: "Fragment 05", subtitle: "Protocol Memory" },
  { title: "Fragment 06", subtitle: "Encrypted Theory" },
];

export default function DiscoveryArchive() {
  return (
    <section id="archive" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(213,0,249,0.14),transparent_32%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Archive</p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
            Uncover the truth hidden inside 43 discoveries.
          </h2>
          <p className="max-w-2xl text-base leading-7 text-slate-300">
            Each fragment reveals a piece of the expedition, the Signal, and the machinery that climbs toward the void.
          </p>
        </div>
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {fragments.map((fragment) => (
            <article
              key={fragment.title}
              className="group rounded-[2rem] border border-cyan-300/10 bg-black/60 p-6 transition hover:border-cyan-400/40 hover:bg-white/5"
            >
              <p className="text-xs uppercase tracking-[0.35em] text-cyan-200/80">{fragment.title}</p>
              <h3 className="mt-4 text-xl font-semibold text-white">{fragment.subtitle}</h3>
              <p className="mt-3 text-sm leading-6 text-slate-300 opacity-90 group-hover:text-cyan-100">
                Encrypted discovery details appear as the expedition continues.
              </p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
