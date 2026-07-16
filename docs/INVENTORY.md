# Documentation Inventory — Milestone: v1.5.2 Closed Beta

## Root
*   `AGENTS.md`: Authoritative governance, memory, and onboarding manual.
*   `docs/ANALYTICS.md`: Analytics & Ads — event catalog, screen tracking, AdMob config, governance.
*   `docs/ARCHITECTURE.md`: High-level system relationship map.
*   `docs/CHANGELOG.md`: Detailed chronological log of dated engineering events.
*   `docs/INVENTORY.md`: This file — complete documentation index.
*   `docs/JumpDroid_EPIC_Tracker.md`: Master high-level status for all EPICs, past and planned.
*   `docs/PRODUCTION_CHECKLIST.md`: Pre/post release tasks for new versions.
*   `docs/README.md`: High-level technical overview of the codebase.
*   `docs/RELEASES.md`: Release history — version table, key changes, artifact links.
*   `docs/VISION.md`: The creative and gameplay goal for Jump Droid.

## Architecture
*   `docs/ARCHITECTURE.md`: System architecture, component map, game loop, data flow, extension points.

## Analysis & Audits
*   `docs/analysis/ANALYTICS_AUDIT.md`: Full analytics architecture audit — events, gaps, recommendations.
*   `docs/analysis/EPIC7_VISUAL_REGRESSION_AUDIT.md`: Identifying lost UI polish during migrations.
*   `docs/analysis/EPIC8_5_MASTER_BLUEPRINT.md`: Implementation blueprint for all EPIC 8.5 sprints.
*   `docs/analysis/EPIC8_5_FINDING_COVERAGE_MATRIX.md`: Mapping audit findings to EPIC 8.5 sprints.
*   `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`: 17-finding codebase health assessment.

## Gameplay & Design Libraries
*   `docs/design/ACHIEVEMENT_LIBRARY.md`: Achievement catalog.
*   `docs/design/AREA_LIBRARY.md`: Altitude zone design catalog.
*   `docs/design/ARTIFACT_LIBRARY.md`: Registry of recoverable story items.
*   `docs/design/ENGINE_EXTENSIONS.md`: Engine extension specifications.
*   `docs/design/LORE_LIBRARY.md`: Lore entry catalog.
*   `docs/design/PLATFORM_LIBRARY.md`: Platform type catalog.
*   `docs/design/POWERUP_LIBRARY.md`: Catalog of in-run benefits and their logic.
*   `docs/design/ROCKET_LIBRARY.md`: Class trait and stat specifications.
*   `docs/design/THREAT_LIBRARY.md`: Threat (hazard/enemy/boss) design catalog.
*   `docs/gameplay/BOSS_DESIGN_BIBLE.md`: Patterns, phases, and mechanics for all 6 bosses.
*   `docs/gameplay/THREATS.md`: Comprehensive table of hazards and enemies.
*   `docs/THREAT_MASTER_TABLE.md`: Threat master table with all entity stats.

## Roadmap
*   `docs/roadmap/EPIC_10_EXECUTION_PLAN.md`: Execution plan for The Outer Reaches.
*   `docs/roadmap/EPIC_10_PLANNING.md`: Planning doc for The Outer Reaches.
*   `docs/roadmap/EPIC_11_EXECUTION_PLAN.md`: Execution plan for Ascension (The End).
*   `docs/roadmap/MONETIZATION_VISION.md`: Principles for ethical revenue generation.
*   `docs/roadmap/RELEASE_POLISH_PLAN.md`: Pre-ship sprint plan — 7 phases, 20 items.

## Marketing
*   `docs/marketing/`: Store copy, brand guide, press materials, launch checklist.

## Analytics & Ads
*   `docs/ANALYTICS.md`: Primary doc — event catalog, screen tracking, AdMob, user properties, governance.
*   `app/src/main/java/com/ashwathai/jump_droid/GameAnalytics.kt`: Domain-driven analytics interface + Firebase implementation.
*   `app/src/main/java/com/ashwathai/jump_droid/PlayerAnalyticsManager.kt`: Firestore-backed tester analytics decorator (Beta Analytics V0).
*   `app/src/main/java/com/ashwathai/jump_droid/AdConfig.kt`: Centralized AdMob unit IDs with debug/release switching.
*   `app/src/main/java/com/ashwathai/jump_droid/AdComponents.kt`: Banner ad composable + Rewarded ad helper.
*   `app/google-services.json`: Firebase project configuration.
