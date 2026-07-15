"use client";

import { PLAY_STORE_URL } from "@/lib/constants";
import SectionWrapper from "./SectionWrapper";
import SectionHeader from "./SectionHeader";

export default function DownloadSection() {
  return (
    <SectionWrapper id="download">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom,_rgba(0,229,255,0.08),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="grid gap-12 lg:grid-cols-2 items-center">
          <div>
            <SectionHeader
              pill="Download"
              title="Begin Your Ascent"
              description="Jump Droid is available now on Android. Download the latest release and start your vertical expedition."
            />

            <div className="mt-8 space-y-6">
              <a
                href={PLAY_STORE_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-3 rounded-full bg-cyan-400 px-8 py-4 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 hover:shadow-[0_0_30px_rgba(0,229,255,0.4)]"
                aria-label="Download Jump Droid on Google Play"
              >
                <svg className="h-6 w-6" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3.609 1.814L13.792 12 3.61 22.186a.996.996 0 01-.61-.92V2.734a1 1 0 01.609-.92zm10.89 10.893l2.302 2.302-10.937 6.333 8.635-8.635zm3.199-3.199l2.807 1.626a1 1 0 010 1.732l-2.807 1.626L15.206 12l2.492-2.492zM5.864 2.658L16.8 8.99l-2.302 2.302-8.634-8.634z" />
                </svg>
                Google Play
              </a>

              <div className="rounded-2xl border border-cyan-300/10 bg-slate-950/60 p-5">
                <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Requirements</p>
                <ul className="mt-3 space-y-1 text-sm text-slate-300">
                  <li>• Android 8.0 (API 24) or later</li>
                  <li>• 150 MB free storage</li>
                  <li>• Internet connection for ads & analytics</li>
                </ul>
              </div>

              <p className="text-xs text-slate-500">
                Version 1.5.1 &middot; Free to play &middot; No account required
              </p>
            </div>
          </div>

          {/* App preview mockup */}
          <div className="flex justify-center lg:justify-end">
            <div className="relative rounded-[2.5rem] border-2 border-slate-700 bg-black p-3 shadow-[0_0_80px_rgba(0,229,255,0.1)]">
              <div className="aspect-[9/19] w-64 rounded-[2rem] bg-gradient-to-b from-cyan-900 via-slate-900 to-black flex items-center justify-center overflow-hidden">
                <div className="text-center p-6">
                  <div className="mx-auto h-12 w-12 rounded-full bg-cyan-400 flex items-center justify-center mb-4">
                    <svg className="h-6 w-6 text-slate-950" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
                    </svg>
                  </div>
                  <p className="text-lg font-bold text-white">Jump Droid</p>
                  <p className="text-xs text-cyan-300 mt-1">The Signal From the Void</p>
                </div>
              </div>
              <div className="mx-auto mt-2 h-1 w-24 rounded-full bg-slate-700" />
            </div>
          </div>
        </div>
      </div>
    </SectionWrapper>
  );
}
