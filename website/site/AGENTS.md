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

## Community & Growth Platform

### Two Independent Pipelines (intersect by email only)

**Flow 1 — Applicants (Inbound):**
Registration form → `/api/recruitment/register` → Firestore `betaUsers/{email}` → Acknowledgement email sent via Gmail API → Admin Review (`RecruitmentSidePanel`) → Approval → Welcome Email → Active

**Flow 2 — Outreach (Outbound):**
`recruitmentContacts/{email}` — imported via CSV or manual entry — with campaign automation fields (`inviteCount`, `nextEligibleAt`, `campaignId`, `emailStatus`, `stoppedReason`)

### Campaign Automation

- **Engine**: `lib/campaignEngine.ts` — called by `GET /api/campaign/process`
- **Cron**: Every 8h via `vercel.json` cron: `0 */8 * * *`
- **Config**: Firestore doc `campaignConfig/default` — defaults: delayDays=4, maxInvites=5, delayBetweenEmailsMs=5000, batchSize=10, maxEmailsPerHour=100
- **Flow per contact**: Check applicant exists (→ converted) → check maxInvites (→ no_response) → send invite via Gmail API → increment inviteCount, set nextEligibleAt = now + delayDays

### Email Templates

All in `lib/emailTemplates/`:
- `layout.ts` — shared HTML shell (branding, footer, dark theme)
- `acknowledgement.ts` — sent on registration, says "review within 48hrs"
- `invitation-1.ts` through `invitation-5.ts` — 5-call campaign sequence
- `welcome.ts` — sent on approval
- `index.ts` — registry with `renderTemplate(template, name, inviteNumber?)`

### Gmail API

- OAuth tokens in Firestore `gmailAuth/tokens` — scoped to `gmail.send` + `gmail.metadata`
- `POST /api/gmail/disconnect` — deletes `gmailAuth/tokens` doc (used by "Switch Account" in OutreachTab)
- Server-side functions in `lib/emailService.ts`: `getAccessToken()`, `buildMimeMessage()`, `logEmail()`, `detectBounces()`
- `/api/gmail/send` accepts `htmlBody` for multipart/alternative emails (backward-compatible, falls back to plain text)
- `emailLog` Firestore collection logs: queuedAt, sentAt, failedAt, retryCount, providerMessageId, providerThreadId, failureReason
- `disconnectGmail()` in `lib/firebase/gmailService.ts` — client-side helper for the disconnect endpoint

### Bounce Detection

- Gmail API does NOT expose delivery events. Bounce detection works by **polling the inbox** for DSN messages from `mailer-daemon@googlemail.com`.
- `detectBounces()` in `lib/emailService.ts`:
  1. Queries Gmail inbox with `q=from:mailer-daemon@googlemail.com after:{lastCheckAt}`
  2. **Idempotency check**: skips any message whose Gmail Message ID is already in `appConfig/bounceDetection.processedBounceIds` (Firestore `arrayUnion`)
  3. Fetches each unprocessed DSN with `format=full`, decodes body, extracts bounced email via regex
  4. Updates `emailLog` status to `failed`, sets `failureReason: "permanent_bounce"`
  5. Updates `recruitmentContacts/{email}` status to `failed`, sets `stoppedReason: "permanent_bounce"`
  6. Updates `betaUsers/{email}` emailStatus to `failed`
  7. Logs `invitation_failed` event to activityLog
  8. Records `messageId` in `processedBounceIds` array (not archived — inbox DSN left in place for audit)
- Last-check timestamp stored in `appConfig/bounceDetection` Firestore doc
- Called at the start of every `processCampaign()` run (every 8h via cron)
- Requires `gmail.readonly` scope (added alongside `gmail.send`)

### Duplicate Detection (Outreach add)
`checkDuplicate(email)` in `lib/firebase/outreachService.ts` checks:
1. `recruitmentContacts/{email}` → "Already in Outreach"
2. `betaUsers/{email}` with status pending → "Already an Applicant"
3. `betaUsers/{email}` with status approved → "Already Approved"
4. `betaUsers/{email}` with status active → "Already an Active Member"

### Firestore Indexes
Required composite indexes defined in `firestore.indexes.json`. Deploy with:
```bash
cd website/site
npx firebase login
npx firebase deploy --only firestore:indexes
```
Or create individually via the direct links in Firestore error messages.

### Critical: NEXT_PUBLIC_APP_URL
- **Required in production**. If missing, `POST /api/gmail/exchange` throws: `"NEXT_PUBLIC_APP_URL is required in production."`
- In development, falls back to `http://localhost:3000` when unset.
- Must match the **exact** OAuth redirect URI registered in Google Cloud Console (including trailing path).

### Sender Profile
- `types/senderProfile.ts` — `{ name: string; email: string }`
- Firestore doc `senderProfiles/default` — loaded by `getSenderProfile()` in `lib/emailService.ts`
- All automated emails (campaign, acknowledgement, welcome) resolve the sender profile at send time
- `buildMimeMessage(to, name, subject, html, text, sender?)` uses the profile for the From header
- `/api/gmail/send` accepts optional `sender` in request body; if absent, loads default
- Default: `{ name: "Ashwath AI", email: "ashwathai.dev@gmail.com" }`

### Types
- `types/campaign.ts` — CampaignConfig, CampaignContactFields, EmailQueueItem, CampaignProcessResult
- `types/recruitmentContacts.ts` — adds inviteCount, nextEligibleAt, campaignId, emailStatus, stoppedReason; statuses include converted, no_response
