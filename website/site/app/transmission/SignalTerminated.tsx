"use client";

export default function SignalTerminated() {
  return (
    <footer className="relative px-6 py-12">
      <div className="mx-auto max-w-lg lg:max-w-2xl border-t border-white/5 pt-6">
        <div className="font-mono text-[10px] text-slate-700 space-y-1 text-center">
          <p className="tracking-[0.3em] uppercase">Transmission Complete</p>
          <p className="tracking-[0.2em]">Carrier Lost</p>
          <p className="text-slate-800 pt-3 text-[9px]">
            Built with love by Ashwath AI
          </p>
          <p className="text-slate-800/60 text-[9px]">
            &copy; {new Date().getFullYear()} Ashwath AI
          </p>
        </div>
      </div>
    </footer>
  );
}
