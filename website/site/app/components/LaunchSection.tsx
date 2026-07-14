import { PLAY_STORE_URL, SOCIAL_LINKS } from "@/lib/constants";
import Link from "next/link";

const links = [
  { href: SOCIAL_LINKS.github, label: "GitHub" },
  { href: SOCIAL_LINKS.itchIo, label: "itch.io" },
  { href: SOCIAL_LINKS.privacy, label: "Privacy" },
  { href: SOCIAL_LINKS.email, label: "Contact" },
  { href: "/beta", label: "Beta" },
];

export default function LaunchSection() {
  return (
    <section id="launch" className="relative min-h-dvh flex flex-col items-center justify-center px-6">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(0,229,255,0.06),transparent_60%)]" />

      <div className="relative flex flex-col items-center gap-8 text-center">
        <p className="text-xs font-bold uppercase tracking-[0.35em] text-cyan-300">
          Launch
        </p>
        <h2 className="text-4xl font-black tracking-tight text-white md:text-5xl">
          Ready to Ascend?
        </h2>
        <p className="text-base text-slate-400">Free. No account. 150MB.</p>

        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex h-14 items-center justify-center rounded-full bg-cyan-400 px-10 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 hover:shadow-[0_0_40px_rgba(0,229,255,0.4)] md:h-16 md:px-12 md:text-base"
        >
          Get It on Google Play
        </a>

        <p className="text-sm text-slate-500">v1.5.1 &middot; Android 8.0+</p>

        <nav className="pt-4" aria-label="Footer navigation">
          <ul className="flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm font-semibold uppercase tracking-[0.15em] text-slate-400">
            {links.map((link) => (
              <li key={link.href}>
                {link.href.startsWith("/") ? (
                  <Link href={link.href} className="transition hover:text-cyan-300">
                    {link.label}
                  </Link>
                ) : (
                  <a
                    href={link.href}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="transition hover:text-cyan-300"
                  >
                    {link.label}
                  </a>
                )}
              </li>
            ))}
          </ul>
        </nav>

        <p className="text-xs text-slate-600">
          &copy; {new Date().getFullYear()} Ashwathai
        </p>
      </div>
    </section>
  );
}
