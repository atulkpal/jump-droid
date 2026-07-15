<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

# Jump Droid Website — Agent Context

## Build Command
```bash
npm run build
```
Must pass with zero warnings, zero errors.

## Design Principles
- Mobile-first, dark cosmic theme, mono font throughout
- No forced full-screen (h-dvh) panels — use auto-height with padding
- ParticleCanvas is the fixed background (z-0), all content at z-10
- Crescent moon in hero: MoonGlow.tsx component, positioned top-right

## Page Sections (page.tsx flow)
HeroSignal → MysteryTransmission → GameplayCards → ScreenshotGallery → MissionLog → Footer

## Content
- All copy lives in `app/data/site-content.ts` — edit there, not in components
- Entity descriptions map in site-content.ts (32 entity descriptions)
- URLs (Play Store, GitHub, etc.) in `lib/constants.ts`
- Screenshot image URLs must point to `master` branch (not feature branches)

## Key Components
- `MoonGlow.tsx` — crescent moon SVG with glow aura + light rays + stars
- `ParticleCanvas.tsx` — canvas-based starfield with cycling colors
- `game/PlatformSVG.tsx`, `game/RocketSVG.tsx`, `game/ThreatSVG.tsx` — game entity SVGs (legacy from original site)
- `PlatformIcons.tsx` — Google Play, GitHub, itch.io SVG icons

## Notes
- `website/` is in .gitignore — use `git add -f` to stage website files
- Pre-existing lint errors in legacy components (BossEncounter, DiscoveryArchive, etc.) — do not fix
- Screenshots served from `raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/`
