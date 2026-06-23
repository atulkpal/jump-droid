import Link from "next/link";

const links = [
  { href: "#hero", label: "Surface" },
  { href: "#ascent", label: "Ascent" },
  { href: "#hangar", label: "Hangar" },
  { href: "#archive", label: "Archive" },
  { href: "#mission-control", label: "Mission" },
];

export default function StickyNav() {
  return (
    <nav className="fixed left-1/2 top-6 z-30 w-[min(90vw,960px)] -translate-x-1/2 rounded-full border border-white/10 bg-black/70 px-5 py-3 shadow-[0_0_60px_rgba(0,229,255,0.15)] backdrop-blur-xl">
      <ul className="flex flex-wrap items-center justify-center gap-3 text-xs uppercase tracking-[0.3em] text-cyan-200/90 sm:text-sm">
        {links.map((link) => (
          <li key={link.href}>
            <Link
              href={link.href}
              className="rounded-full px-4 py-2 transition hover:bg-cyan-500/10 hover:text-cyan-100"
            >
              {link.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
