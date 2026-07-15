export default function AltitudeSidebar() {
  const marks = ["Earth", "Cloud", "Orbit", "Deep Space", "The Void", "Signal"];

  return (
    <aside className="pointer-events-none fixed right-6 top-1/2 hidden h-[70vh] -translate-y-1/2 xl:block">
      <div className="flex h-full w-16 flex-col items-center justify-between rounded-3xl border border-cyan-300/20 bg-black/60 p-4 text-xs text-cyan-100/80 shadow-[0_0_80px_rgba(0,229,255,0.1)]">
        <div className="space-y-2 text-right">
          {marks.map((mark) => (
            <p key={mark} className="font-mono text-[0.65rem] uppercase tracking-[0.18em]">
              {mark}
            </p>
          ))}
        </div>
        <div className="relative h-full w-[2px] rounded-full bg-white/10">
          <span className="absolute left-1/2 top-8 h-3 w-3 -translate-x-1/2 rounded-full bg-cyan-400 shadow-[0_0_16px_rgba(0,229,255,0.75)]" />
        </div>
      </div>
    </aside>
  );
}
