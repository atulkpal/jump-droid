# Reddit Post Templates

**Last Updated:** 2026-07-14

---

## Template 1: Launch / Release Announcement

**Subreddit:** r/AndroidGaming, r/IndieGaming, r/opensource, r/Kotlin

**Title:** I built a vertical rocket exploration game entirely with Kotlin and Jetpack Compose — and it's open source 🚀

**Body:**

Hey everyone!

I've been working on Jump Droid — a precision vertical exploration game where you pilot a droid-piloted rocket through 12 hostile atmospheric zones.

**What's in the game:**
- 12 atmospheric zones (Earth → The Singularity) with parallax backgrounds
- 11 unique bosses with multi-phase fights
- 26+ enemy and hazard types
- 4 rocket classes + 17 modules for deep customization
- 12-track mission system with tier progression
- Prestige system and Eternal Mode for endgame
- Secret missions, hidden lore, artifact set bonuses

**Technical:**
Built entirely with Kotlin and Jetpack Compose Canvas — no game engine. The complete source is open source under MIT.

**Download:**
Latest APK: [link to GitHub release]
Source code: [link to GitHub repo]

Happy to answer any questions about the development process, Compose Canvas game architecture, or anything else!

---

## Template 2: Showcase / Technical Deep Dive

**Subreddit:** r/androiddev, r/Kotlin, r/gamedev

**Title:** Building a full game with Jetpack Compose Canvas — what I learned

**Body:**

I built Jump Droid, a vertical rocket exploration game, entirely with Jetpack Compose Canvas — no Unity, no libGDX, just pure Kotlin and Compose.

**Some technical highlights:**
- 4 sub-steps per frame physics loop for collision reliability
- 26 threat renderers extracted from a single God object (90% size reduction)
- Crossfading audio engine with SoundPool + MediaPlayer
- Observable Compose state for the entire game loop
- 46 production OGG audio assets
- Architecture: component-based with extracted managers

**The good:**
- Compose Canvas is surprisingly capable for 2D games
- Hot reload is a game-changer for UI iteration
- Kotlin's sealed classes and functional style map perfectly to game state management

**The challenging:**
- No built-in physics engine — everything is custom collision math
- Performance tuning for Canvas draw calls requires attention
- State management across game loop vs. Compose recomposition can be tricky

The entire project is open source (MIT): [GitHub link]

Happy to discuss architecture decisions or answer questions!

---

## Template 3: Community Update

**Subreddit:** r/AndroidGaming

**Title:** Jump Droid v1.5.1 is out — open source rocket exploration game

**Body:**

Just published a hotfix for Jump Droid that fixes two audio-related crashes. The game is completely free and open source.

**v1.5.1 Hotfix:**
- Fixed crash on game over from MediaPlayer race condition
- Fixed crash during music crossfade from zone changes

If you haven't tried it yet — it's a vertical rocket game with 12 zones, 11 bosses, modular ship builds, and a full endgame. Free download, no ads (except optional rewarded continues), no pay-to-win.

[GitHub link]

---

## Posting Guidelines

- Always include `[OC]` tag for original content
- Reply to comments within 24 hours if possible
- Do not post more than once per week in the same subreddit
- Include download link and GitHub link in every post
- Respect subreddit rules about self-promotion (10:1 ratio)
