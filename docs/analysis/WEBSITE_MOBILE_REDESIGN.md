# Website Mobile-First Redesign

**Branch:** `feature/mobile-redesign`
**Last updated:** 2026-07-14
**Git commit:** `31548ac`

---

## Architecture

### Routes

| Route | Content | Type |
|---|---|---|
| `/` | Landing page — 7 sections, conversion-focused | Server component |
| `/beta` | Beta testing info | Existing, untouched |
| `/privacy` | Privacy policy | Existing, untouched |

### Homepage Sections (in order)

1. **Portal** — Full-viewport hero. Game title, one-line tagline, single Download CTA. No competing links.
2. **Platforms** — 12 platform types in responsive grid (2→3→4→6 columns). Each card: SVG + name + one-line desc.
3. **Threats** — 19 threats with 5 filter tabs (All/Bosses/Mini/Enemies/Hazards). Grid: 2→3 columns.
4. **Fleet** — 6 rocket classes with stat bars (thrust/fuel/heat) + passive ability. Grid: 2→3 columns.
5. **Archive** — 2 tabs: Database (8 categories, 4-col grid) / Lore (6 logs, 3-col grid).
6. **Gallery** — 4 screenshots in 2×2 grid.
7. **Launch** — Download CTA + version info + footer links (GitHub, itch.io, Privacy, Contact, Beta).

### Navigation

- **Desktop:** Sticky pill nav — "Jump Droid" logo + "Download" inline CTA + CRT toggle.
- **Mobile:** Same pill nav — hamburger opens a full-width "Download on Google Play" button.
- CRT toggle persists in localStorage, applies scanline overlay.

---

## Design Principles

- **Mobile-first.** Default layout is single-column grids. Desktop gets more columns.
- **No horizontal swipe.** All content uses standard vertical scroll + CSS `grid`. No touch event handlers.
- **No fixed background layers.** Pure black `#000` background. No zone parallax, no flying rocket, no encounter system on the homepage.
- **Readable text.** Minimum `text-xs` (12px). No `text-[8px]` or `text-[9px]`.
- **One action per section.** Download CTA appears in Portal, Launch, and the StickyNav — only three places total.
- **Cards are subtle.** `rounded-xl border border-white/10 bg-white/[0.03]` — dark glass on black.
- **Section padding:** `px-6 py-20` — generous whitespace, no viewport-forcing.

---

## Key Decisions

| Decision | Rationale |
|---|---|
| Removed all fixed overlay layers | Zone backgrounds, encounters, and flying rocket created visual noise on mobile. Homepage is now a clean list. |
| No carousels | Horizontal swipe was broken on mobile (touch gesture conflicts). CSS grid is universal and reliable. |
| No `min-h-dvh` | Viewport-height forcing caused content to feel cramped. Sections now flow naturally with `py-20`. |
| No interactive sliders/thrust toggle | Removed from RocketsSection. Homepage is read-only showcase. Interactive elements belong in the game. |
| All data in component files | No shared data imports. Each section is self-contained — easy to move, delete, or reorganize. |
| page.tsx is a server component | Zero client JS for layout. Only ThreatsSection (filter) and ArchiveSection (tabs) use `"use client"`. |
| Screenshot URLs fixed | Changed from `feature/github-media-kit` branch (404) to `master` branch. |

---

## Files

### New / Rewritten

| File | Purpose |
|---|---|
| `app/page.tsx` | Landing page, server component |
| `app/components/PortalSection.tsx` | Hero with download CTA |
| `app/components/PlatformsSection.tsx` | 12 platform cards in grid |
| `app/components/ThreatsSection.tsx` | 19 threat cards with filter |
| `app/components/RocketsSection.tsx` | 6 rocket cards with stat bars |
| `app/components/ArchiveSection.tsx` | Database + Lore tabs |
| `app/components/GallerySection.tsx` | 4 screenshots in 2×2 grid |
| `app/components/LaunchSection.tsx` | Download + footer links |
| `app/components/StickyNav.tsx` | Minimal nav with Download CTA |

### Deleted

| File | Reason |
|---|---|
| `app/components/Carousel.tsx` | Carousel pattern abandoned |
| `app/components/FloatingDownload.tsx` | Single CTA per screen is sufficient |
| `app/explore/page.tsx` | All content lives on `/` now |

### Preserved (unchanged)

| File | Used By |
|---|---|
| `app/components/Footer.tsx` | Beta & Privacy pages |
| `app/components/game/*.tsx` | All SVGs (PlatformSVG, ThreatSVG, RocketSVG) |
| `app/components/FeaturesSection.tsx` | (orphaned, kept for reference) |
| `app/components/GameSimulator.tsx` | (orphaned, kept for reference) |
| `app/components/ProgressionSystems.tsx` | (orphaned, kept for reference) |
| `app/components/MissionControl.tsx` | (orphaned, kept for reference) |
| `app/components/GameplayExplained.tsx` | (orphaned, kept for reference) |
| `app/components/GameplaySection.tsx` | (orphaned, kept for reference) |
| `app/components/BetaSection.tsx` | (orphaned, kept for reference) |
| `app/components/HeroSection.tsx` | (orphaned, kept for reference) |
| `app/components/BossShowcase.tsx` | (orphaned, kept for reference) |
| `app/components/DiscoveryArchive.tsx` | (orphaned, kept for reference) |
| `app/components/DownloadSection.tsx` | (orphaned, kept for reference) |
| `app/components/ScreenshotsGallery.tsx` | (orphaned, kept for reference) |
| `app/components/FeaturesSection.tsx` | (orphaned, kept for reference) |

---

## Design Library Compliance

All gameplay content on the homepage (platforms, threats, rockets) originates from the design libraries in `docs/design/`. Platform types match `PLATFORM_LIBRARY.md`, threats match `THREAT_LIBRARY.md`, and rocket classes match `ROCKET_LIBRARY.md`.

---

## Future Work (when user returns)

1. Visual polish — the site is functional but "doesn't look good yet." Needs: custom fonts, better spacing, hover states, maybe subtle entrance animations.
2. Dedicated pages for orphaned components (GameSimulator, ProgressionSystems, MissionControl).
3. Restore immersive scroll layers on a secondary page if desired.
4. Performance optimization for screenshot images (srcSet, WebP).
