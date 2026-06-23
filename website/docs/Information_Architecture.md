# Jump Droid — Website Information Architecture (IA)

## 1. Site Map

### Level 1: Main Landing (Single Page Scroll)
*   **Hero (The Surface)**
*   **The Ascent (The Journey Sections)**
*   **The Hangar (Rocket Showcase)**
*   **The Archive (Discovery System)**
*   **Mission Control (Community/Support)**

### Level 2: Sub-pages (Static/Informational)
*   **Privacy Policy**
*   **Terms of Service**
*   **Press Kit (Assets/Logos)**
*   **Development Roadmap (Visual Tracker)**

---

## 2. Section Breakdown

### A. Hero Section
*   **H1:** "THE VOID IS CALLING."
*   **Sub-headline:** "Pilot the ultimate droid explorer. Uncover the Great Signal."
*   **CTA:** [Download on Google Play]
*   **Visual:** Low-angle shot of the Balanced Rocket on the launch pad (Earth Zone).

### B. The Ascent (Dynamic Scroll)
*   **Transition 1 (Clouds):** Text: "Master the Atmosphere." Image: Scout Rocket dodging lightning. Feature: Physics-sub-stepping.
*   **Transition 2 (Orbit):** Text: "Face the Guardians." Video loop: The Gatekeeper boss fight. Feature: Destructible Bosses.
*   **Transition 3 (Deep Space):** Text: "Beyond the Known." Image: Void Whale encounter. Feature: AI entities.

### C. The Hangar (Rocket Showcase)
*   **Interaction:** Slider to toggle between Balanced, Scout, Tank, and Experimental.
*   **Data Overlay:** Show stats (Thrust, Fuel, Heat) for each.
*   **CTA:** "Find your build."

### D. The Archive (Discovery)
*   **Visual:** Grid of "Encrypted" icons.
*   **Interaction:** Hover to reveal "Fragment 01: The First Signal".
*   **USP:** "43 Discoveries. 1 Truth."

### E. Mission Control (Footer-prev)
*   **Socials:** Discord, X, YouTube.
*   **Newsletter:** "Join the Expedition" email capture.

---

## 3. Navigation Strategy

*   **Fixed Altitude Bar:** On the right side of the screen, a vertical line showing "0m" at the top and "15000m+" at the bottom. A droid icon moves down (up in altitude) as the user scrolls.
*   **Sticky Header:** Simple [Hangar] [Archive] [Download] links that jump to sections.

---

## 4. Technical Requirements

*   **Framework:** Recommended: Next.js + Tailwind CSS + Framer Motion.
*   **Assets:** WebP/AVIF for images, Lottie for simple HUD animations.
*   **Responsive:** Extreme focus on mobile viewports (portrait), as that's where the target audience lives.
