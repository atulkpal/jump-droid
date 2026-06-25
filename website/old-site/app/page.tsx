import AltitudeSidebar from "./components/AltitudeSidebar";
import BossShowcase from "./components/BossShowcase";
import DiscoveryArchive from "./components/DiscoveryArchive";
import GameplayExplained from "./components/GameplayExplained";
import HeroSection from "./components/HeroSection";
import MissionControl from "./components/MissionControl";
import ProgressionSystems from "./components/ProgressionSystems";
import RocketShowcase from "./components/RocketShowcase";
import StickyNav from "./components/StickyNav";

export default function Home() {
  return (
    <div className="relative min-h-screen overflow-hidden bg-black text-white">
      <StickyNav />
      <AltitudeSidebar />
      <main className="relative">
        <HeroSection />
        <GameplayExplained />
        <section id="ascent" className="relative overflow-hidden py-24 sm:py-32">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.08),transparent_20%)]" />
          <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
            <div className="mb-12 max-w-2xl space-y-4">
              <p className="text-sm uppercase tracking-[0.35em] text-cyan-300/90">The Zones</p>
              <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
                Six atmospheres. Escalating challenge.
              </h2>
              <p className="max-w-2xl text-base leading-7 text-slate-300">
                Each zone brings new hazards, enemies, and trials. Difficulty multiplies as you climb toward the Signal.
              </p>
            </div>
            <div className="grid gap-6 lg:grid-cols-2 xl:grid-cols-3">
              {[
                {
                  zone: "Earth",
                  descriptor: "The Launch",
                  detail: "Gravity pulls hard. Learn the basics.",
                },
                {
                  zone: "Cloud Layer",
                  descriptor: "The Storm",
                  detail: "Lightning and turbulence test your thrust.",
                },
                {
                  zone: "Orbit",
                  descriptor: "The Boundary",
                  detail: "Bosses guard the path forward.",
                },
                {
                  zone: "Deep Space",
                  descriptor: "The Expanse",
                  detail: "New threats emerge. Reality shifts.",
                },
                {
                  zone: "The Void",
                  descriptor: "The Source",
                  detail: "Where the Signal originates.",
                },
                {
                  zone: "The Signal",
                  descriptor: "The Truth",
                  detail: "The final frontier awaits.",
                },
              ].map((item) => (
                <article
                  key={item.zone}
                  className="rounded-[2rem] border border-cyan-300/10 bg-white/5 p-6 shadow-[0_0_60px_rgba(0,229,255,0.06)] transition hover:border-cyan-400/30 hover:bg-white/[0.08]"
                >
                  <p className="text-xs uppercase tracking-[0.35em] text-cyan-200/80">{item.descriptor}</p>
                  <h3 className="mt-3 text-lg font-semibold text-white">{item.zone}</h3>
                  <p className="mt-3 text-sm leading-6 text-slate-300">{item.detail}</p>
                </article>
              ))}
            </div>
          </div>
        </section>
        <BossShowcase />
        <RocketShowcase />
        <DiscoveryArchive />
        <ProgressionSystems />
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
