import AltitudeSidebar from "./components/AltitudeSidebar";
import DiscoveryArchive from "./components/DiscoveryArchive";
import HeroSection from "./components/HeroSection";
import MissionControl from "./components/MissionControl";
import RocketShowcase from "./components/RocketShowcase";
import StickyNav from "./components/StickyNav";

export default function Home() {
  return (
    <div className="relative min-h-screen overflow-hidden bg-black text-white">
      <StickyNav />
      <AltitudeSidebar />
      <main className="relative">
        <HeroSection />
        <section id="ascent" className="relative overflow-hidden py-24 sm:py-32">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.08),transparent_20%)]" />
          <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
            <div className="mb-12 max-w-2xl space-y-4">
              <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Ascent</p>
              <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
                Master the atmosphere. Face the guardians. Reach the void.
              </h2>
              <p className="max-w-2xl text-base leading-7 text-slate-300">
                Glide through cloud layers with precise thrust control, confront massive bosses in orbit, and discover secrets in the void.
              </p>
            </div>
            <div className="grid gap-6 xl:grid-cols-3">
              {[
                {
                  title: "Cloud Layer",
                  description: "Physics-driven turbulence and lightning storms.",
                },
                {
                  title: "Orbit",
                  description: "Phase-based boss fights with destructible weak points.",
                },
                {
                  title: "Void",
                  description: "Cryptic discoveries and reality-warping encounters.",
                },
              ].map((item) => (
                <article
                  key={item.title}
                  className="rounded-[2rem] border border-cyan-300/10 bg-white/5 p-6 shadow-[0_0_60px_rgba(0,229,255,0.06)]"
                >
                  <p className="text-xs uppercase tracking-[0.35em] text-cyan-200/80">{item.title}</p>
                  <p className="mt-4 text-lg font-semibold text-white">{item.description}</p>
                </article>
              ))}
            </div>
          </div>
        </section>
        <RocketShowcase />
        <DiscoveryArchive />
        <MissionControl />
      </main>
      <footer className="border-t border-white/10 bg-black/90 py-8 text-slate-400">
        <div className="mx-auto flex max-w-6xl flex-col gap-6 px-6 sm:px-8 lg:px-12 lg:flex-row lg:items-center lg:justify-between">
          <p className="text-sm">Jump Droid — The Signal From the Void.</p>
          <div className="flex flex-wrap items-center gap-4 text-sm">
            <a href="#hero" className="transition hover:text-cyan-200">Back to top</a>
            <a href="#mission-control" className="transition hover:text-cyan-200">Contact</a>
            <a href="#" className="transition hover:text-cyan-200">Privacy</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
