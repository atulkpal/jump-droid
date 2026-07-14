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
    <section id="launch" className="px-6 py-24 md:py-32">
      <div className="mx-auto max-w-md text-center">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.35em] text-slate-500">
          Launch
        </p>
        <h2 className="mb-4 text-4xl font-black tracking-tight text-white md:text-5xl">
          Ready to Ascend?
        </h2>
        <p className="mb-8 text-base text-slate-500">
          Free. No account. 150MB.
        </p>

        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex h-14 items-center justify-center rounded-full bg-cyan-400 px-10 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 md:h-16 md:px-12 md:text-base"
        >
          Get It on Google Play
        </a>

        <p className="mt-4 text-sm text-slate-600">
          v1.5.1 &middot; Android 8.0+
        </p>

        <nav className="mt-12" aria-label="Footer">
          <ul className="flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm font-semibold uppercase tracking-[0.15em] text-slate-500">
            {links.map((link) => (
              <li key={link.href}>
                {link.href.startsWith("/") ? (
                  <Link href={link.href} className="transition hover:text-cyan-300">
                    {link.label}
                  </Link>
                ) : (
                  <a href={link.href} target="_blank" rel="noopener noreferrer" className="transition hover:text-cyan-300">
                    {link.label}
                  </a>
                )}
              </li>
            ))}
          </ul>
        </nav>

        <p className="mt-8 text-xs text-slate-700">
          &copy; {new Date().getFullYear()} Ashwathai
        </p>
      </div>
    </section>
  );
}
