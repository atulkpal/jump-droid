# Documentation Inventory — Milestone: v1.5.2 — Community Platform & Hero Redesign

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
*   `docs/COMMUNITY_PLATFORM.md`: Portable website Community & Growth Platform — routing, components, Firestore data model, Gmail OAuth, campaign automation, reuse checklist, setup guide.
*   `docs/CRM_SYSTEM.md`: Full CRM & Campaign System reference — data model, types, email system, campaign engine, detection, API, UI, whitelabel catalog, file layout.

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
*   `docs/roadmap/CRM_DECOUPLE_PLAN.md`: ReachEngine extraction roadmap — variable catalog, integration contracts, 5-phase extraction plan, customization API design, migration guide.

## Marketing
*   `docs/marketing/`: Store copy, brand guide, press materials, launch checklist.

## Analytics & Ads
*   `docs/ANALYTICS.md`: Primary doc — event catalog, screen tracking, AdMob, user properties, governance.
*   `app/src/main/java/com/ashwathai/jump_droid/GameAnalytics.kt`: Domain-driven analytics interface + Firebase implementation.
*   `app/src/main/java/com/ashwathai/jump_droid/PlayerAnalyticsManager.kt`: Firestore-backed tester analytics decorator (Beta Analytics V0).
*   `app/src/main/java/com/ashwathai/jump_droid/AdConfig.kt`: Centralized AdMob unit IDs with debug/release switching.
*   `app/src/main/java/com/ashwathai/jump_droid/AdComponents.kt`: Banner ad composable + Rewarded ad helper.
*   `app/google-services.json`: Firebase project configuration.

## Website & Community Platform

### Primary Document
*   `docs/COMMUNITY_PLATFORM.md`: Portable reference — architecture, routing, component map, Firestore data model, Gmail OAuth, campaign automation, reuse checklist, setup guide.

### Routes
*   `website/site/app/beta/page.tsx`: Tester Portal — profile selector, progress, eligibility, stats, leaderboard, feedback.
*   `website/site/app/beta-info/page.tsx`: Public beta info + registration form + FAQ accordion.
*   `website/site/app/beta-dashboard/page.tsx`: Admin dashboard — 8 KPI cards, tester table, sessions, daily summary, feedback, config modal.
*   `website/site/app/beta-dashboard/recruitment/page.tsx`: Recruitment dashboard — Applicants tab + Outreach tab.
*   `website/site/app/gmail/callback/page.tsx`: Gmail OAuth redirect handler.

### API Routes
*   `website/site/app/api/recruitment/register/route.ts`: POST — Register beta user + acknowledgement email.
*   `website/site/app/api/campaign/process/route.ts`: GET — Trigger campaign automation (cron).
*   `website/site/app/api/gmail/send/route.ts`: POST — Send email(s) via Gmail API.
*   `website/site/app/api/gmail/status/route.ts`: GET — Check Gmail auth status.
*   `website/site/app/api/gmail/exchange/route.ts`: POST — Exchange OAuth code for tokens.
*   `website/site/app/api/gmail/disconnect/route.ts`: POST — Delete Gmail OAuth tokens.

### Beta Components (27 total)
*   `website/site/components/beta/ActivityTimeline.tsx`: Vertical event timeline for recruitment audit trail.
*   `website/site/components/beta/AddManualContact.tsx`: Modal — add single outreach contact with duplicate check.
*   `website/site/components/beta/BetaAccordion.tsx`: FAQ accordion (Why Join, Rewards, Expectations, Code Jam).
*   `website/site/components/beta/BetaRegistrationForm.tsx`: Public registration form with validation + success state.
*   `website/site/components/beta/BetaRulesCard.tsx`: Static rewards/credits info card.
*   `website/site/components/beta/CommunityProgress.tsx`: Aggregated community stats.
*   `website/site/components/beta/ConfigurationCard.tsx`: Admin settings modal (dates, requirements, revenue).
*   `website/site/components/beta/DailySummaryTable.tsx`: Per-day aggregated session data table.
*   `website/site/components/beta/EligibilityCard.tsx`: Tester eligibility status.
*   `website/site/components/beta/FeedbackSection.tsx`: Submit feedback + list previous.
*   `website/site/components/beta/FeedbackTable.tsx`: All feedback across all testers.
*   `website/site/components/beta/Leaderboard.tsx`: Top 5 testers by highest score.
*   `website/site/components/beta/OutreachContactsTable.tsx`: Searchable, selectable outreach contacts table.
*   `website/site/components/beta/OutreachDashboardCards.tsx`: 6 KPI summary cards for outreach.
*   `website/site/components/beta/OutreachImportCsv.tsx`: CSV upload, parse, preview, import.
*   `website/site/components/beta/OutreachTab.tsx`: Full outreach section orchestrator.
*   `website/site/components/beta/OverviewCards.tsx`: 8 KPI cards for admin dashboard.
*   `website/site/components/beta/ProgressCard.tsx`: Daily playtime progress bar.
*   `website/site/components/beta/RecentSessions.tsx`: Recent sessions table (500 latest).
*   `website/site/components/beta/RecruitmentSidePanel.tsx`: Applicant details, approve/reject/delete, notes, timeline.
*   `website/site/components/beta/RecruitmentSummaryCards.tsx`: Clickable filter cards.
*   `website/site/components/beta/RecruitmentTable.tsx`: Searchable applicants table.
*   `website/site/components/beta/StatisticsCard.tsx`: Tester statistics.
*   `website/site/components/beta/StatusBadge.tsx`: Color-coded status pill badge.
*   `website/site/components/beta/TesterSelector.tsx`: Dropdown to select a tester profile.
*   `website/site/components/beta/TesterTable.tsx`: All testers table with per-tester revenue.

### Screen Components (new)
*   `website/site/app/components/screens/BetaTagline.tsx`: Rotating rewards/taglines inside Beta CTA button.
*   `website/site/app/components/screens/PlayStoreModal.tsx`: Modal dialog when Play Store icon is clicked.

### Campaign Engine
*   `website/site/lib/campaignEngine.ts`: Campaign processing — bounce detection → eligibility → send → track.
*   `website/site/lib/emailService.ts`: Gmail API — access tokens, MIME building, send, logging, bounce detection.
*   `website/site/lib/emailTemplates/layout.ts`: Shared HTML email shell.
*   `website/site/lib/emailTemplates/acknowledgement.ts`: "We received your application" email.
*   `website/site/lib/emailTemplates/welcome.ts`: "You're approved!" email.
*   `website/site/lib/emailTemplates/invitation-1.ts` through `invitation-5.ts`: 5-call campaign sequence.
*   `website/site/lib/emailTemplates/index.ts`: Template registry with `renderTemplate()`.

### Firebase Services
*   `website/site/lib/firebase/config.ts`: Firebase app initialization singleton.
*   `website/site/lib/firebase/configService.ts`: DashboardConfig CRUD.
*   `website/site/lib/firebase/testers.ts`: Tester data fetching.
*   `website/site/lib/firebase/sessions.ts`: Session data fetching.
*   `website/site/lib/firebase/analytics.ts`: Dashboard stats computation.
*   `website/site/lib/firebase/revenue.ts`: Revenue calculation (eCPM × impressions / 1000).
*   `website/site/lib/firebase/exchangeRate.ts`: Live USD→INR exchange rate (Frankfurter API).
*   `website/site/lib/firebase/feedback.ts`: Feedback CRUD.
*   `website/site/lib/firebase/recruitment.ts`: Client-side beta registration helper.
*   `website/site/lib/firebase/recruitmentService.ts`: Server-side applicant CRUD.
*   `website/site/lib/firebase/outreachService.ts`: Outreach contacts CRUD + import + duplicate checking.
*   `website/site/lib/firebase/campaignService.ts`: Campaign automation — config, contacts, tracking.
*   `website/site/lib/firebase/gmailService.ts`: Client-side Gmail OAuth helper.
*   `website/site/lib/firebase/activityService.ts`: Activity log for recruitment audit trail.

### TypeScript Type Definitions
*   `website/site/types/betaUser.ts`: BetaUserRegistration, BetaUserDoc.
*   `website/site/types/tester.ts`: Tester, TesterSession, TimestampData.
*   `website/site/types/config.ts`: DashboardConfig.
*   `website/site/types/recruitment.ts`: RecruitmentStatus, RecruitmentApplicant, RecruitmentSummary.
*   `website/site/types/recruitmentContacts.ts`: OutreachStatus, OutreachContact, CsvRow, DuplicateCheckResult.
*   `website/site/types/campaign.ts`: CampaignConfig, CampaignContactFields, EmailQueueItem, CampaignProcessResult.
*   `website/site/types/feedback.ts`: FeedbackCategory, Feedback.
*   `website/site/types/stats.ts`: DashboardStats, DailySummary, EligibilityInfo.
*   `website/site/types/activityLog.ts`: ActivityEventType (13 types), ActivityLogEntry.
*   `website/site/types/emailLog.ts`: EmailTemplate (8 templates), EmailLogEntry.
*   `website/site/types/senderProfile.ts`: SenderProfile.

### Configuration
*   `website/site/vercel.json`: Vercel cron configuration (every 8h).
*   `website/site/firestore.indexes.json`: 3 composite indexes for campaign, activity, email queries.
*   `website/site/.env.local`: 10 environment variables (Firebase + Gmail + App URL).
