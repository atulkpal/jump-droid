# Jump Droid — Community & Growth Platform

**Portable, modular platform for beta testing, recruitment, outreach, and community analytics.**

Designed to be forked into other projects with minimal variable changes — swap the theme tokens, sender profile, and email templates, and the entire platform works as-is.

---

## 1. Platform Overview

Two independent pipelines intersect only by email address:

```

    INBOUND (Applicants)                  OUTBOUND (Outreach)
    ─────────────────────                 ─────────────────────
    /beta-info                            CSV Import / Manual Entry
         │                                       │
         ▼                                       ▼
    BetaRegistrationForm              recruitmentContacts/{email}
         │                                       │
         ▼                                       ▼
    POST /api/recruitment/register    Campaign Automation Engine
         │                              (Vercel cron every 8h)
         ▼                                       │
    betaUsers/{email}                           ▼
         │                              fetchEligibleContacts()
         ▼                                       │
    Admin Review                         5-call invite sequence
         │                              via Gmail API
         ▼                                       │
    Approve → Welcome Email              incrementInviteCount()
         │                              set nextEligibleAt
         ▼                                       │
    status: "active"                     status: "converted"
                                         (if already in betaUsers)
```

### Pipeline 1 — Inbound Applicants

| Step | Component / API | Firestore Collection |
|------|----------------|---------------------|
| Register | `BetaRegistrationForm` → `POST /api/recruitment/register` | `betaUsers/{email}` |
| Send acknowledgement | Gmail API → `renderTemplate("acknowledgement")` | `emailLog` |
| Review | `RecruitmentSidePanel` in admin dashboard | `betaUsers/{email}` |
| Approve | `approveApplicant(email)` → status: `"approved"` | `betaUsers/{email}` |
| Send welcome | Gmail API → `renderTemplate("welcome")` | `emailLog` |
| Activate | `activateApplicant(email)` → status: `"active"` | `betaUsers/{email}` |
| Activity audit | `logEvent()` on every state change | `activityLog` |

### Pipeline 2 — Outbound Outreach

| Step | Component / API | Firestore Collection |
|------|----------------|---------------------|
| Import CSV | `OutreachImportCsv` → `importContacts()` | `recruitmentContacts/{email}` |
| Manual add | `AddManualContact` → `addManualContact()` | `recruitmentContacts/{email}` |
| Duplicate check | `checkDuplicate(email)` — checks 4 collections | — |
| Campaign process | `processCampaign()` via Vercel cron (every 8h) | — |
| Bounce detection | `detectBounces()` — polls Gmail DSN inbox | `appConfig/bounceDetection` |
| Send invite | Gmail API → `renderTemplate("invitation-N")` | `emailLog` |
| Track | `incrementInviteCount()` → inviteCount++ | `recruitmentContacts/{email}` |

---

## 2. Routing Architecture

All routes live under `website/site/app/` (Next.js App Router):

### Public Routes

| Route | File | Purpose |
|-------|------|---------|
| `/` | `page.tsx` | Marketing homepage — HeroSignal → Footer |
| `/beta` | `app/beta/page.tsx` | **Tester Portal** — profile selector, stats, feedback |
| `/beta-info` | `app/beta-info/page.tsx` | **Beta Info + Registration** — public landing with registration form + FAQ accordion |
| `/privacy` | `app/privacy/page.tsx` | Privacy policy |

### Admin Routes

| Route | File | Purpose |
|-------|------|---------|
| `/beta-dashboard` | `app/beta-dashboard/page.tsx` | **Admin Dashboard** — overview KPI cards, tester table, sessions, daily summary, feedback, config modal |
| `/beta-dashboard/recruitment` | `app/beta-dashboard/recruitment/page.tsx` | **Recruitment Dashboard** — Applicants tab (table + side panel) + Outreach tab |

### OAuth Routes

| Route | File | Purpose |
|-------|------|---------|
| `/gmail/callback` | `app/gmail/callback/page.tsx` | Gmail OAuth redirect handler — exchanges code for tokens |

### API Routes

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/recruitment/register` | POST | Register beta user → Firestore → acknowledgement email |
| `/api/campaign/process` | GET | Trigger campaign automation (Vercel cron every 8h) |
| `/api/gmail/send` | POST | Send email(s) via Gmail API (bulk or single) |
| `/api/gmail/status` | GET | Check Gmail OAuth authentication status |
| `/api/gmail/exchange` | POST | Exchange OAuth authorization code for refresh/access tokens |
| `/api/gmail/disconnect` | POST | Delete Gmail OAuth tokens from Firestore |

---

## 3. Component Map

All beta components live in `website/site/components/beta/` — 27 components total.

### Beta Portal (`/beta`) — Tester-facing

| Component | File | Purpose |
|-----------|------|---------|
| `TesterSelector` | `TesterSelector.tsx` | Dropdown to select a tester profile from Firestore `testers/` |
| `ProgressCard` | `ProgressCard.tsx` | Daily playtime progress bar (today vs goal from config) |
| `EligibilityCard` | `EligibilityCard.tsx` | Eligible days count + qualification status |
| `StatisticsCard` | `StatisticsCard.tsx` | Total gameplay time, sessions, highest score, last seen |
| `Leaderboard` | `Leaderboard.tsx` | Top 5 testers by highest score |
| `CommunityProgress` | `CommunityProgress.tsx` | Total testers, community play time, total games |
| `FeedbackSection` | `FeedbackSection.tsx` | Submit rating/category/comment + list previous feedback |
| `BetaRulesCard` | `BetaRulesCard.tsx` | Static beta rewards info + credits recognition |

### Admin Dashboard (`/beta-dashboard`) — Admin-facing

| Component | File | Purpose |
|-----------|------|---------|
| `OverviewCards` | `OverviewCards.tsx` | 8 KPI cards: total testers, today active, sessions, games, play time, impressions, ads, revenue |
| `TesterTable` | `TesterTable.tsx` | All testers table (sorted by last seen, per-tester revenue) |
| `RecentSessions` | `RecentSessions.tsx` | Recent 500 sessions table |
| `DailySummaryTable` | `DailySummaryTable.tsx` | Per-day aggregated stats |
| `FeedbackTable` | `FeedbackTable.tsx` | All feedback across all testers |
| `ConfigurationCard` | `ConfigurationCard.tsx` | Modal: edit beta dates, required days/minutes, revenue eCPM, USD→INR rate |

### Recruitment Dashboard (`/beta-dashboard/recruitment`) — Admin-facing

#### Applicants Tab

| Component | File | Purpose |
|-----------|------|---------|
| `RecruitmentSummaryCards` | `RecruitmentSummaryCards.tsx` | Clickable filter cards (pending/approved/active/email failed/rejected) |
| `RecruitmentTable` | `RecruitmentTable.tsx` | Searchable table of all applicants |
| `RecruitmentSidePanel` | `RecruitmentSidePanel.tsx` | Modal: applicant details, approve flow, reject, delete, notes, activity timeline |

#### Outreach Tab

| Component | File | Purpose |
|-----------|------|---------|
| `OutreachDashboardCards` | `OutreachDashboardCards.tsx` | 6 KPI cards: total, pending, invited, registered, converted, no response |
| `OutreachContactsTable` | `OutreachContactsTable.tsx` | Searchable, selectable table with invite tracking |
| `OutreachImportCsv` | `OutreachImportCsv.tsx` | Modal: CSV file upload, parse, preview, import |
| `AddManualContact` | `AddManualContact.tsx` | Modal: add single contact with duplicate checking |
| `OutreachTab` | `OutreachTab.tsx` | Full outreach section orchestrator + Gmail auth status + Send Invitations |

### Shared

| Component | File | Purpose |
|-----------|------|---------|
| `StatusBadge` | `StatusBadge.tsx` | Color-coded pill badge for applicant status |
| `ActivityTimeline` | `ActivityTimeline.tsx` | Vertical event timeline for recruitment audit trail |
| `BetaRegistrationForm` | `BetaRegistrationForm.tsx` | Public registration form with validation + success state |
| `BetaAccordion` | `BetaAccordion.tsx` | FAQ accordion (Why Join, Rewards, Expectations, Code Jam) |

---

## 4. Firestore Data Model

### Collections

| Collection | Document ID | Key Fields | Used By |
|------------|-------------|------------|---------|
| `testers` | `{docId}` | email, name, lastSeen, totalGameplayTime, todayGameplayTime, highestScore, totalSessions, rewardAdsWatched, bannerImpressions | Beta Portal, Admin Dashboard |
| `testers/{docId}/sessions` | auto-ID | testerEmail, sessionStart/End, gameplayTime, bannerImpressions, rewardAdsWatched, gamesPlayed, finalScore, outcome | Eligibility, RecentSessions, DailySummary |
| `testers/{docId}/testerFeedback` | auto-ID | rating, category, comment, createdAt | FeedbackSection, FeedbackTable |
| `betaUsers` | `{email}` | email, name, phone, codeJam, status, source, notes, registeredAt, approvedAt, invitedAt, emailStatus, acknowledgementSent | Recruitment Dashboard, Registration |
| `recruitmentContacts` | `{email}` | name, phone, status, source, inviteCount, lastInviteAt, nextEligibleAt, campaignId, emailStatus, stoppedReason | OutreachTab, Campaign Engine |
| `activityLog` | auto-ID | applicantEmail, eventType, details, createdAt | ActivityTimeline |
| `emailLog` | auto-ID | recipient, template, campaign, status, sentAt, failedAt, providerMessageId, failureReason | Email audit trail |
| `dashboardConfig` | `settings` | beta.startDate, beta.endDate, beta.requiredDays, beta.requiredMinutes, revenue.bannerEcpmUsd, revenue.rewardedEcpmUsd, revenue.usdToInr | Admin Dashboard Config |
| `campaignConfig` | `default` | delayDays, maxInvites, delayBetweenEmailsMs, batchSize, maxEmailsPerHour | Campaign Engine |
| `gmailAuth` | `tokens` | refreshToken, accessToken, expiryDate | Gmail API auth |
| `senderProfiles` | `default` | name, email | Email sender identity |
| `appConfig` | `bounceDetection` | lastCheckAt, processedBounceIds[] | Bounce detection |

### Composite Indexes (deployed via `firestore.indexes.json`)

| Collection | Fields | Purpose |
|------------|--------|---------|
| `recruitmentContacts` | `inviteCount ASC, nextEligibleAt ASC` | Fetch eligible contacts for campaign |
| `activityLog` | `applicantEmail ASC, createdAt ASC` | Timeline for a specific applicant |
| `emailLog` | `recipient ASC, sentAt DESC` | Recent emails by recipient |

### Default Config Values

**Campaign Config** (`campaignConfig/default`):
```json
{
  "delayDays": 4,
  "maxInvites": 5,
  "delayBetweenEmailsMs": 5000,
  "batchSize": 10,
  "maxEmailsPerHour": 100
}
```

**Dashboard Config** (`dashboardConfig/settings`):
```json
{
  "beta": { "startDate": "2026-07-01", "endDate": "2026-09-30", "requiredDays": 15, "requiredMinutes": 30 },
  "revenue": { "bannerEcpmUsd": 0.50, "rewardedEcpmUsd": 5.00, "usdToInr": 83.0 }
}
```

**Sender Profile** (`senderProfiles/default`):
```json
{ "name": "Ashwath AI", "email": "ashwathai.dev@gmail.com" }
```

---

## 5. Gmail OAuth Integration

### Auth Flow

```
User clicks "Connect Gmail" in OutreachTab
  → getAuthUrl() → redirects to Google OAuth consent screen
  → User grants gmail.send + gmail.readonly scopes
  → Google redirects to /gmail/callback?code={authorizationCode}
  → /gmail/callback calls POST /api/gmail/exchange
  → Server exchanges code for refresh + access tokens
  → Tokens stored in Firestore gmailAuth/tokens
  → Redirects back to /beta-dashboard/recruitment?tab=outreach
```

### API Endpoints

| Endpoint | What It Does |
|----------|-------------|
| `GET /api/gmail/status` | Checks if `gmailAuth/tokens` doc exists → `{ authenticated: boolean }` |
| `POST /api/gmail/exchange` | Exchanges OAuth code using `googleapis` → stores tokens in Firestore |
| `POST /api/gmail/send` | Builds MIME message → sends via Gmail API `users.messages.send` → logs to `emailLog` |
| `POST /api/gmail/disconnect` | Deletes `gmailAuth/tokens` Firestore doc → "Switch Account" |

### Token Refresh

`getAccessToken()` in `lib/emailService.ts` automatically refreshes tokens when within 60s of expiry:
```
if (tokenData.expiryDate - Date.now() < 60000)
  → refreshAccessToken(refreshToken)
  → store new tokens in Firestore
```

### Bounce Detection

Gmail API does NOT expose delivery events. Bounces are detected by polling the inbox:

```
detectBounces() in lib/emailService.ts:
  1. Query inbox: q=from:mailer-daemon@googlemail.com after:{lastCheckAt}
  2. Idempotency: skip messageIds already in appConfig/bounceDetection.processedBounceIds
  3. Fetch each DSN with format=full, extract bounced email from body
  4. Update emailLog → status: "failed", failureReason: "permanent_bounce"
  5. Update recruitmentContacts/{email} → stoppedReason: "permanent_bounce"
  6. Update betaUsers/{email} → emailStatus: "failed"
  7. Log invitation_failed event to activityLog
  8. Record messageId in processedBounceIds array

Called at the start of every processCampaign() run (every 8h via cron).
```

---

## 6. Campaign Automation

### Engine Pipeline

```
Vercel Cron (every 8h)
  → GET /api/campaign/process
    → detectBounces()                     # Clean failed emails
    → fetchEligibleContacts(maxInvites)   # Firestore query
    → For each contact (up to batchSize=10):
        → Check if in betaUsers → markConverted(), skip
        → Check if maxInvites reached → markNoResponse(), skip
        → renderTemplate("invitation-N", name)
        → getAccessToken() → buildMimeMessage() → send via Gmail API
        → If success: incrementInviteCount(), recur nextEligibleAt = now + delayDays
        → If failed: markEmailFailed(), log to activityLog
    → Respect maxEmailsPerHour=100, delayBetweenEmailsMs=5000
```

### 5-Call Campaign Sequence

| Invite # | Subject |
|----------|---------|
| 1 | "You're Invited to Join the Jump Droid Beta Program!" |
| 2 | "Reminder: Join the Jump Droid Beta Program" |
| 3 | "Don't Miss Out on Jump Droid Beta" |
| 4 | "Last Few Spots - Jump Droid Beta" |
| 5 | "Final Invitation - Jump Droid Beta" |

---

## 7. Email Templates

All templates in `lib/emailTemplates/`:

| File | Function | Template Name |
|------|----------|---------------|
| `layout.ts` | — | Shared HTML shell (dark theme, branding, footer) |
| `acknowledgement.ts` | `renderAcknowledgement(name)` | "We received your application" — sent on registration |
| `welcome.ts` | `renderWelcome(name)` | "You're approved!" — sent on admin approval |
| `invitation-1.ts` | `renderInvitation1(name)` | First campaign invite |
| `invitation-2.ts` | `renderInvitation2(name)` | Reminder |
| `invitation-3.ts` | `renderInvitation3(name)` | Follow-up |
| `invitation-4.ts` | `renderInvitation4(name)` | Last few spots |
| `invitation-5.ts` | `renderInvitation5(name)` | Final call |
| `index.ts` | `renderTemplate(template, name, inviteNumber?)` | Registry — dispatches to the correct renderer |

Template rendering:
- All templates are **TypeScript functions** returning `{ html: string; subject: string }`
- `layout.ts` provides a shared branded HTML shell with inline CSS (dark theme, cyan accents, mono font)
- `renderTemplate()` also generates a plaintext version via `stripHtml()`
- Emails are sent as `multipart/alternative` (HTML + plaintext fallback)

---

## 8. Duplicate Detection (Outreach Add)

`checkDuplicate(email)` in `lib/firebase/outreachService.ts` checks in order:

1. `recruitmentContacts/{email}` exists → `"Already in Outreach"`
2. `betaUsers/{email}` with status `pending` → `"Already an Applicant"`
3. `betaUsers/{email}` with status `approved` → `"Already Approved"`
4. `betaUsers/{email}` with status `active` → `"Already an Active Member"`

Returns strongly-typed `DuplicateCheckResult`:
```typescript
{ status: "ok" }
{ status: "exists_in_outreach" }
{ status: "already_applicant" }
{ status: "already_approved" }
{ status: "already_active" }
```

---

## 9. TypeScript Type System

All type definitions in `website/site/types/`:

| File | Types |
|------|-------|
| `betaUser.ts` | `BetaUserRegistration`, `BetaUserDoc` |
| `tester.ts` | `Tester`, `TesterSession`, `TimestampData` |
| `config.ts` | `DashboardConfig` |
| `recruitment.ts` | `RecruitmentStatus`, `EmailStatus`, `RecruitmentApplicant`, `VisibleStatus`, `RecruitmentSummary` |
| `recruitmentContacts.ts` | `OutreachStatus`, `OutreachContact`, `CsvRow`, `DuplicateCheckResult` |
| `campaign.ts` | `CampaignConfig`, `CampaignContactFields`, `EmailQueueItem`, `CampaignProcessResult` |
| `feedback.ts` | `FeedbackCategory`, `Feedback` |
| `stats.ts` | `DashboardStats`, `DailySummary`, `EligibilityInfo` |
| `activityLog.ts` | `ActivityEventType` (13 event types), `ActivityLogEntry` |
| `emailLog.ts` | `EmailTemplate` (8 templates), `EmailLogEntry` |
| `senderProfile.ts` | `SenderProfile` |

---

## 10. Reuse Checklist

To repurpose this platform for another project, change:

### Required Changes

| What | Where | Notes |
|------|-------|-------|
| Firebase project | `.env.local` → 6 `NEXT_PUBLIC_FIREBASE_*` vars | Create new Firebase project |
| Sender profile | Firestore `senderProfiles/default` OR change `DEFAULT_SENDER` in `lib/emailService.ts:4-7` | Name + email for automated emails |
| OAuth redirect URI | Google Cloud Console + `NEXT_PUBLIC_APP_URL` env var | Must match exactly |
| Email templates | `lib/emailTemplates/*.ts` | Rewrite HTML content, keep layout |
| Gmail scopes | Google Cloud Console | `gmail.send` + `gmail.readonly` |
| Theme colors | All components use Tailwind classes | Search/replace `cyan`, `slate` tokens |

### Optional Changes

| What | Where | Notes |
|------|-------|-------|
| Brand name | Text in templates, layout, site-content | "Jump Droid" → your project |
| Firestore collection names | Service files in `lib/firebase/` | `betaUsers`, `recruitmentContacts`, etc. |
| Campaign config | Firestore `campaignConfig/default` | Delay, batch size, max invites |
| Dashboard config | Firestore `dashboardConfig/settings` | Beta dates, playtime requirements |
| Email send delay | `campaignEngine.ts:line-9` | `sleep(5000)` between sends |
| Cron schedule | `vercel.json` → `crons[0].schedule` | Default `0 */8 * * *` (every 8h) |
| OAuth scopes | `/api/gmail/exchange` route | Currently `gmail.send` + `gmail.readonly` |

---

## 11. Setup Guide

### Prerequisites

- Node.js 18+
- Firebase project with Firestore (Native mode)
- Google Cloud Console project with Gmail API enabled
- Vercel account (Pro for cron, Hobby with external pinger)

### Environment Variables

Create `.env.local` in `website/site/`:

```bash
NEXT_PUBLIC_FIREBASE_API_KEY=
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=
NEXT_PUBLIC_FIREBASE_PROJECT_ID=
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=
NEXT_PUBLIC_FIREBASE_APP_ID=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
NEXT_PUBLIC_GOOGLE_CLIENT_ID=     # Same as GOOGLE_CLIENT_ID
NEXT_PUBLIC_APP_URL=               # Required in production
```

### Firestore Indexes

```bash
cd website/site
npx firebase login
npx firebase deploy --only firestore:indexes --project your-project-id
```

### Vercel Deployment

| Setting | Value |
|---------|-------|
| Root Directory | `website/site` |
| Production Branch | `master` |
| Framework Preset | Next.js |
| Cron (Pro) | Dashboard → Cron Jobs: `0 */8 * * *` → `https://your-site.vercel.app/api/campaign/process` |

On Hobby plan, use an external cron service (e.g., cron-job.org) to ping the endpoint.

### Gmail OAuth Registration

1. Go to Google Cloud Console → APIs & Services → Credentials
2. Create OAuth 2.0 Client ID (Web application)
3. Add redirect URI: `https://your-site.vercel.app/gmail/callback`
4. Scopes: `https://www.googleapis.com/auth/gmail.send`, `https://www.googleapis.com/auth/gmail.readonly`
5. Use the Client ID/Secret in environment variables above

---

## 12. File Layout

```
website/site/
├── app/
│   ├── beta/page.tsx                    # Tester Portal
│   ├── beta-info/page.tsx               # Beta Info + Registration
│   ├── beta-dashboard/page.tsx          # Admin Dashboard
│   ├── beta-dashboard/recruitment/page.tsx  # Recruitment Dashboard
│   ├── gmail/callback/page.tsx          # OAuth callback
│   └── api/
│       ├── recruitment/register/route.ts
│       ├── campaign/process/route.ts
│       └── gmail/{send,status,exchange,disconnect}/route.ts
├── components/beta/                     # 27 beta components
├── lib/
│   ├── firebase/
│   │   ├── config.ts                    # Firebase init singleton
│   │   ├── configService.ts             # DashboardConfig CRUD
│   │   ├── testers.ts                   # Tester data fetching
│   │   ├── sessions.ts                  # Session data fetching
│   │   ├── analytics.ts                 # Dashboard stats computation
│   │   ├── revenue.ts                   # Revenue calculation
│   │   ├── exchangeRate.ts              # Live USD→INR rate
│   │   ├── feedback.ts                  # Feedback CRUD
│   │   ├── recruitment.ts               # Client-side registration helper
│   │   ├── recruitmentService.ts        # Server-side applicant CRUD
│   │   ├── outreachService.ts           # Outreach contacts CRUD
│   │   ├── campaignService.ts           # Campaign automation
│   │   ├── gmailService.ts              # Client-side Gmail OAuth
│   │   └── activityService.ts           # Activity log
│   ├── campaignEngine.ts                # Campaign processing engine
│   ├── emailService.ts                  # Gmail API: tokens, send, bounce
│   └── emailTemplates/                  # 7 email templates + layout + registry
├── types/                               # 12 type definition files
├── firestore.indexes.json               # 3 composite indexes
└── vercel.json                          # Cron configuration
```
