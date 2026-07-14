import Link from "next/link";
import { SOCIAL_LINKS } from "@/lib/constants";

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-white/10 bg-black/90 py-12 text-slate-400">
      <div className="mx-auto flex flex-col gap-8 px-6 sm:px-8 lg:px-12">
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
          {/* Brand */}
          <div className="space-y-3">
            <p className="text-sm font-bold text-white tracking-wide">Jump Droid</p>
            <p className="text-xs leading-relaxed text-slate-500">
              The Signal From the Void. A vertical expedition game built with Kotlin and Jetpack Compose Canvas.
            </p>
          </div>

          {/* Game */}
          <div className="space-y-3">
            <p className="text-xs font-bold uppercase tracking-widest text-slate-300">Game</p>
            <ul className="space-y-2 text-sm">
              <li>
                <a href="#features" className="transition hover:text-cyan-200">Features</a>
              </li>
              <li>
                <a href="#screenshots" className="transition hover:text-cyan-200">Screenshots</a>
              </li>
              <li>
                <a href="#download" className="transition hover:text-cyan-200">Download</a>
              </li>
            </ul>
          </div>

          {/* Community */}
          <div className="space-y-3">
            <p className="text-xs font-bold uppercase tracking-widest text-slate-300">Community</p>
            <ul className="space-y-2 text-sm">
              <li>
                <a
                  href={SOCIAL_LINKS.github}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="transition hover:text-cyan-200"
                >
                  GitHub
                </a>
              </li>
              <li>
                <a
                  href={SOCIAL_LINKS.itchIo}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="transition hover:text-cyan-200"
                >
                  itch.io
                </a>
              </li>
              <li>
                <a
                  href={SOCIAL_LINKS.email}
                  className="transition hover:text-cyan-200"
                >
                  Contact
                </a>
              </li>
            </ul>
          </div>

          {/* Legal */}
          <div className="space-y-3">
            <p className="text-xs font-bold uppercase tracking-widest text-slate-300">Legal</p>
            <ul className="space-y-2 text-sm">
              <li>
                <Link href={SOCIAL_LINKS.privacy} className="transition hover:text-cyan-200">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link href="/beta" className="transition hover:text-cyan-200">
                  Beta Testing
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-white/5 pt-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-xs text-slate-600">
            &copy; {year} Ashwath AI. All rights reserved.
          </p>
          <p className="text-xs text-slate-600">
            Built with Kotlin &amp; Jetpack Compose
          </p>
        </div>
      </div>
    </footer>
  );
}
