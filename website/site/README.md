# Jump Droid Website

The official website for **Jump Droid** — a free, open-source Android arcade game built by Ashwath AI.

https://jump-droid.vercel.app

## Tech Stack

- **Framework:** Next.js (App Router)
- **Styling:** Tailwind CSS v4
- **Animations:** CSS keyframes, Framer Motion (legacy sections)
- **Fonts:** Inter (sans), JetBrains Mono (mono)
- **Deployment:** Vercel

## Getting Started

```bash
cd website/site
npm install
npm run dev
```

Open http://localhost:3000.

## Build

```bash
npm run build
```

Must pass with zero warnings, zero errors.

## Project Structure

```
website/site/
├── app/
│   ├── components/
│   │   ├── screens/        # Page sections (hero, gameplay, etc.)
│   │   ├── game/           # Game entity SVGs (PlatformSVG, RocketSVG, ThreatSVG)
│   │   └── ...
│   ├── data/
│   │   └── site-content.ts # All copy, entity data, descriptions
│   ├── transmission/       # Particle canvas, archive pages
│   └── page.tsx            # Home page layout
├── lib/
│   └── constants.ts        # URLs, screenshot paths, features
└── public/                 # Static assets (favicon, manifest, OG images)
```

## Design

Mobile-first, dark cosmic theme. Sections flow vertically with no forced full-screen panels. A crescent moon with glowing rays anchors the hero. Entity SVG thumbnails showcase game depth (14 platform types, 11 bosses, 6 rockets, 26+ threats). The ParticleCanvas provides a dark starfield background with cycling cyan/purple/gold waveforms.

## Key Sections (top to bottom)

1. **HeroSignal** — Moon + "JUMP DROID" title + tagline + description + CTAs + feature cards
2. **MysteryTransmission** — 5-line classified transmission
3. **GameplayCards** — Interactive category viewer with entity cycling & descriptions
4. **ScreenshotGallery** — Desktop thumbnail tabs + mobile dots
5. **MissionLog** — Source code card + download buttons + beta CTA
6. **Footer** — Platform links, privacy, Ashwath AI credit

## CDN Images

Screenshots are loaded from GitHub raw URLs on the `master` branch under `media/screenshots/`. The URL must point to `master` (not feature branches).
