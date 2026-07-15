"use client";

export default function SignalTerminated() {
  return (
    <footer className="relative px-6 py-16">
      <div className="mx-auto max-w-lg border-t border-white/5 pt-8">
        <div className="font-mono text-[10px] text-slate-700 space-y-2 text-center">
          <p className="tracking-[0.3em] uppercase">Transmission Complete</p>
          <p className="tracking-[0.2em]">Carrier Lost</p>
          <p className="text-slate-800 pt-4 text-[9px]">
            Built with ❤️ by Ashwath AI — Building free, open-source software, AI, and games for everyone.
          </p>
          <p className="text-slate-800/60 text-[9px]">
            &copy; {new Date().getFullYear()} Ashwath AI
          </p>
        </div>

        {/* Fading waveform bar */}
        <div className="mt-8 h-[2px] bg-gradient-to-r from-transparent via-cyan-400/20 to-transparent" />
      </div>
    </footer>
  );
}
