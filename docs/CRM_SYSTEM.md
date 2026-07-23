# Jump Droid — CRM & Campaign System

**Portable, multi-campaign CRM with email automation, detection, and whitelabel surfaces.**

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Firestore Data Model](#2-firestore-data-model)
3. [TypeScript Type System](#3-typescript-type-system)
4. [Email System](#4-email-system)
5. [Campaign Engine](#5-campaign-engine)
6. [Detection Systems](#6-detection-systems)
7. [API Reference](#7-api-reference)
8. [UI Components](#8-ui-components)
9. [Configuration & Secrets](#9-configuration--secrets)
10. [Whitelabel Surface Catalog](#10-whitelabel-surface-catalog)
11. [Debug Logging](#11-debug-logging)
12. [File Layout](#12-file-layout)

---

## 1. Architecture Overview

### Two Pipelines

The system has two independent pipelines that intersect only by email address:

```
INBOUND (Applicants)                  OUTBOUND (Outreach)
─────────────────────                 ─────────────────────
/beta-info                            CSV Import / Manual Entry
     │                                       │
     ▼                                       ▼
BetaRegistrationForm              recruitmentContacts/{email}
     │                                       │
     ▼                                       ▼
betaUsers/{email}                 Campaign Automation Engine
     │                              (Vercel or GH Actions cron)
     ▼                                       │
Admin Review                          campaignData.{campaignId}
     │                              per-campaign tracking
     ▼                                       │
Approve → Welcome Email              bounce/reply/open detection
     │                                       │
     ▼                                       ▼
status: "active"                    status: "converted"
                                     (if already in betaUsers)
```

### Multi-Campaign Model

A single contact can belong to **multiple campaigns simultaneously**. Each campaign tracks its own state independently via `campaignData.{campaignId}`:

- A contact can be "invited" in Campaign A and "pending" in Campaign B.
- A reply in Campaign A does NOT affect Campaign B.
- Bounce detection marks `campaignData.*.status = "bounced"` for campaigns that actually sent.
- Unsubscribe marks `campaignData.*.status = "unsubscribed"` for all campaigns.
- Conversion checks `betaUsers/{email}.convertedFromCampaign` for attribution.

### Per-Campaign vs. Top-Level Status

| Scope | Field | Meaning |
|-------|-------|---------|
| Top-level | `status` | Contact lifecycle: `pending \| invited \| registered \| converted \| no_response \| deleting \| unsubscribed` |
| Per-campaign | `campaignData.{id}.status` | Campaign-specific: `pending \| invited \| replied \| converted \| no_response \| failed \| bounced \| unsubscribed` |

Top-level status is a **CRM snapshot** — it reflects the overall contact lifecycle, not campaign state.

---

## 2. Firestore Data Model

### 2.1 `recruitmentContacts/{email}` — Central CRM Record

Document ID is the contact's normalized email (lowercase, trimmed).

**Top-level fields:**

| Field | Type | Description |
|-------|------|-------------|
| `name` | `string` | Contact display name |
| `email` | `string` | Normalized email (same as doc ID) |
| `phone` | `string` | Phone number |
| `status` | `OutreachStatus` | Lifecycle status: `pending \| invited \| registered \| converted \| no_response \| deleting \| unsubscribed` |
| `source` | `string` | Import source: `"manual"`, `"campaign"`, `"csv"` |
| `importedAt` | `Timestamp` | When the contact was added |
| `importedBy` | `string` | Who added the contact |
| `registeredAt` | `Timestamp \| null` | When they registered as applicant |
| `notes` | `string` | Free-text notes |
| `campaigns` | `string[]` | Array of campaign IDs this contact belongs to |
| `campaignData` | `Record<string, CampaignContactData>` | Per-campaign tracking (keyed by campaign ID) |

**CampaignContactData** (`campaignData.{campaignId}`):

| Field | Type | Description |
|-------|------|-------------|
| `inviteCount` | `number` | How many invites sent in this campaign |
| `lastInviteAt` | `Timestamp \| null` | Last send time |
| `nextEligibleAt` | `Timestamp \| null` | When contact is eligible for next send |
| `emailStatus` | `"pending" \| "sent" \| "failed"` | Last email delivery status |
| `status` | `string` | Per-campaign status (see above) |
| `stoppedReason` | `string` | Why processing stopped for this campaign |
| `assignedSender` | `string \| null` | Gmail account that sends to this contact (for sender consistency) |
| `currentStep` | `number` | Current step in the template sequence |
| `startedAt` | `Timestamp \| null` | When this campaign first contacted this user |
| `replied` | `boolean` | Whether the contact replied in this campaign |
| `repliedAt` | `Timestamp \| null` | When the reply was detected |
| `replySnippet` | `string \| undefined` | First 2000 chars of the reply body |
| `opened` | `boolean \| undefined` | Whether the contact opened an email (tracking pixel) |
| `openedAt` | `Timestamp \| null \| undefined` | When the open was detected |

### 2.2 `campaigns/{campaignId}` — Campaign Documents

Document ID format: `jd_camp_YYYYMMDD_NNNN` (auto-generated).

| Field | Type | Description |
|-------|------|-------------|
| `id` | `string` | Same as document ID |
| `name` | `string` | Campaign name |
| `status` | `CampaignStatus` | `draft \| scheduled \| running \| paused \| completed \| failed \| cancelled` |
| `createdAt` | `Timestamp` | Creation time |
| `updatedAt` | `Timestamp` | Last update time |
| `startedAt` | `Timestamp \| null` | When campaign became running |
| `createdBy` | `string` | Creator email |
| `scheduledStartAt` | `Timestamp \| null` | Scheduled start time |
| `lastDetectedAt` | `Timestamp \| null` | Last detection run |
| `settings` | `CampaignConfig` | Campaign configuration |
| `stats` | `object` | Computed stats (see below) |

**CampaignConfig** (settings):

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `delayDays` | `number` | `4` | Days between each invite |
| `maxInvites` | `number` | `5` | Max invites per contact |
| `delayBetweenEmailsMs` | `number` | `120000` | Throttle between sends (ms) |
| `batchSize` | `number` | `5` | Max contacts processed per cron run |
| `maxEmailsPerHour` | `number` | `100` | Gmail API rate cap |
| `senderAccountId` | `string \| null` | `null` | Specific Gmail account to send from |
| `templateSequence` | `string[]` | `[]` | Template keys for each mail in the sequence |

**stats** (computed on read — not stored directly):

| Field | Type | Computation |
|-------|------|-------------|
| `totalContacts` | `number` | All contacts with this campaign ID in their `campaigns` array |
| `contacted` | `number` | Contacts with `inviteCount > 0` |
| `pending` | `number` | `totalContacts - contacted` |
| `totalSends` | `number` | Sum of all `inviteCount` values |
| `errors` | `number` | Contacts with `emailStatus === "failed"` or `status === "failed"` |
| `converted` | `number` | Contacts with per-campaign status `"converted"` |
| `noResponse` | `number` | Contacts with per-campaign status `"no_response"` |
| `bounced` | `number` | Contacts with per-campaign status `"bounced"` |

`contactBreakdown` is a separate `Record<string, number>` computed on read, grouping contacts by their per-campaign status value.

### 2.3 `emailLog/{autoId}` — Email Send History

| Field | Type | Description |
|-------|------|-------------|
| `recipient` | `string` | Recipient email |
| `recipientName` | `string` | Recipient display name |
| `template` | `string` | Template key used |
| `campaign` | `string` | Campaign ID |
| `source` | `string` | Source: `"campaign"`, `"manual"`, etc. |
| `status` | `string` | `"sent"` or `"failed"` |
| `queuedAt` | `Timestamp \| null` | When queued |
| `sentAt` | `Timestamp \| null` | When sent |
| `failedAt` | `Timestamp \| null` | When failed |
| `retryCount` | `number` | Retry attempts |
| `providerMessageId` | `string` | Gmail message ID |
| `providerThreadId` | `string` | Gmail thread ID (used for reply detection) |
| `failureReason` | `string` | Error description (truncated to 500 chars) |
| `error` | `string` | Raw error message |

### 2.4 `activityLog/{autoId}` — Audit Trail

| Field | Type | Description |
|-------|------|-------------|
| `applicantEmail` | `string` | Related email (or `"campaign:{id}"` for campaign events) |
| `eventType` | `string` | Event type key |
| `details` | `string` | Human-readable description |
| `createdAt` | `Timestamp` | When the event occurred |
| `campaignId` | `string \| undefined` | Related campaign ID |

Event types: `invitation_sent`, `invitation_failed`, `contact_converted`, `contact_added_to_campaign`, `contact_removed_from_campaign`, `replied`, `email_opened`, `email_run_completed`, `campaign_started`, `campaign_paused`, `campaign_resumed`, `campaign_cancelled`, `campaign_completed`, `campaign_failed`, `campaign_archived`, `preflight_passed`, `preflight_failed`, `system_error`, `rate_limited`, `skipped_unsubscribed`, `sender_fallback`, `unsubscribed`, `detection_check`.

### 2.5 `systemLogs/{autoId}` — Debug Logging (not shown in UI)

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | `Timestamp` | When logged |
| `eventType` | `string` | Dot-notation event type |
| `severity` | `"info" \| "warn" \| "error"` | Severity level |
| `campaignId` | `string \| null` | Related campaign |
| `contactEmail` | `string \| null` | Related contact |
| `message` | `string` | Human-readable message |
| `data` | `string \| null` | JSON stringified payload |

### 2.6 `emailTemplateOverrides/{key}` — Custom Template Overrides

Document key convention: `invitation-N` or `outreach-N` (e.g., `invitation-1`, `outreach-3`).

| Field | Type | Description |
|-------|------|-------------|
| `subject` | `string` | Email subject line |
| `htmlBody` | `string` | Full HTML body (supports `{{name}}`, `${name}`, `{betaInfoUrl}`) |
| `updatedAt` | `Timestamp` | Last modified |

### 2.7 `emailAccounts/{email}` — Connected Gmail Accounts

| Field | Type | Description |
|-------|------|-------------|
| `email` | `string` | Gmail address |
| `displayName` | `string` | Display name for From header |
| `accessToken` | `string` | OAuth access token |
| `refreshToken` | `string` | OAuth refresh token |
| `expiryDate` | `number` | Token expiry timestamp |
| `status` | `"connected" \| "expired"` | Connection status |
| `isDefault` | `boolean` | Whether this is the default sender |
| `errorMessage` | `string \| null` | Last auth error |

### 2.8 `appConfig/{config}` — Configuration Documents

**`appConfig/bounceDetection`:**

| Field | Type | Description |
|-------|------|-------------|
| `lastCheckAt` | `Timestamp` | Last bounce detection run |
| `processedBounceIds` | `string[]` | Gmail message IDs of processed bounce DSNs |

**`appConfig/replyDetection`:**

| Field | Type | Description |
|-------|------|-------------|
| `lastCheckAt` | `number` | Unix timestamp of last reply check |
| `processedReplyIds` | `string[]` | Gmail message IDs of processed replies |

### 2.9 `campaignConfig/default` — Global Campaign Defaults

```json
{
  "delayDays": 4,
  "maxInvites": 5,
  "delayBetweenEmailsMs": 120000,
  "batchSize": 5,
  "maxEmailsPerHour": 100,
  "senderAccountId": null,
  "templateSequence": []
}
```

Per-campaign settings override these values via `mergeCampaignConfig()`.

### 2.10 `betaUsers/{email}` — Applicant Records

| Field | Type | Description |
|-------|------|-------------|
| `email` | `string` | Applicant email |
| `name` | `string` | Applicant name |
| `status` | `string` | `pending \| approved \| active \| rejected` |
| `convertedFromCampaign` | `string \| undefined` | Campaign ID that led to this registration |
| `emailStatus` | `string \| undefined` | Email delivery status |

### 2.11 `emailAuditLog/{autoId}` — OAuth Audit Trail

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | `EmailAuditEventType` | `oauth_error`, `send_success`, `send_failed` |
| `accountEmail` | `string` | Related Gmail account |
| `details` | `string` | Description |
| `performedBy` | `string` | Who triggered the event |
| `createdAt` | `Timestamp` | When logged |

---

## 3. TypeScript Type System

All types in `website/site/types/`.

### 3.1 `recruitmentContacts.ts`

```typescript
type OutreachStatus = 
  | "pending"      // Contact created, not yet invited
  | "invited"      // At least one invite sent (any campaign)
  | "registered"   // Registered as applicant
  | "converted"    // Became an active beta user
  | "no_response"  // Max invites reached without response
  | "deleting"     // Soft-delete in progress
  | "unsubscribed"; // Contact unsubscribed

interface OutreachContact {
  email: string;
  name: string;
  phone: string;
  status: OutreachStatus;
  source: string;
  importedAt: Timestamp | null;
  registeredAt: Timestamp | null;
  notes: string;
  campaigns: string[];
  campaignData: Record<string, CampaignContactData>;
}
```

The top-level `OutreachStatus` is lifecycle-only. Per-campaign states (replied, failed, bounced) live in `campaignData`.

### 3.2 `campaign.ts`

```typescript
type CampaignStatus = 
  | "draft" | "scheduled" | "running" 
  | "paused" | "completed" | "failed" | "cancelled";

interface CampaignConfig {
  delayDays: number;            // Days between invites (default: 4)
  maxInvites: number;           // Max invites per contact (default: 5)
  delayBetweenEmailsMs: number; // Throttle between sends (default: 120000)
  batchSize: number;            // Contacts per cron run (default: 5)
  maxEmailsPerHour: number;     // Gmail rate limit (default: 100)
  senderAccountId: string | null; // Specific sender account
  templateSequence?: string[];  // Ordered template keys for the sequence
}

interface CampaignContactData {
  inviteCount: number;
  lastInviteAt: Timestamp | null;
  nextEligibleAt: Timestamp | null;
  emailStatus: "pending" | "sent" | "failed";
  stoppedReason: string;
  assignedSender: string | null;
  currentStep: number;
  startedAt: Timestamp | null;
  replied: boolean;
  repliedAt: Timestamp | null;
  replySnippet?: string;
  opened?: boolean;
  openedAt?: Timestamp | null;
  status?: "pending" | "invited" | "replied" | "converted" 
        | "no_response" | "failed" | "bounced" | "unsubscribed";
}

interface CampaignProcessResult {
  campaignId: string;
  processed: number;  // Contacts checked
  sent: number;       // Emails successfully sent
  failed: number;     // Emails that failed
  converted: number;  // Contacts that converted during processing
  noResponse: number; // Contacts that hit max invites
}
```

### 3.3 Helper Functions (outreachService.ts)

| Function | Purpose |
|----------|---------|
| `latestInviteAt(contact)` | Most recent `lastInviteAt` across all campaigns |
| `earliestNextEligible(contact)` | Earliest `nextEligibleAt` across all campaigns |
| `totalInvites(contact)` | Sum of `inviteCount` across all campaigns |
| `effectiveStatus(contact, campaignId?)` | Returns effective display status: `"opened"` for contacts who opened but haven't replied |

---

## 4. Email System

### 4.1 Template Registry

Located at `lib/emailTemplates/index.ts`. Two maps plus special templates:

**INVITATION_MAP** (legacy, automatically assigned by invite number):
- `1` → `renderInvitation1(name, unsubscribeUrl?)`
- `2` → `renderInvitation2(name, unsubscribeUrl?)`
- `3` → `renderInvitation3(name, unsubscribeUrl?)`
- `4` → `renderInvitation4(name, unsubscribeUrl?)`
- `5` → `renderInvitation5(name, unsubscribeUrl?)`

**OUTREACH_MAP** (for explicit template selection):
- `1` → `renderOutreach1(name, unsubscribeUrl?)`
- `2` → `renderOutreach2(name, unsubscribeUrl?)`
- `3` → `renderOutreach3(name, unsubscribeUrl?)`
- `4` → `renderOutreach4(name, unsubscribeUrl?)`
- `5` → `renderOutreach5(name, unsubscribeUrl?)`

**Special templates**: `acknowledgement`, `welcome`, `reject` — used for non-campaign emails.

### 4.2 renderTemplate() Signature

```typescript
function renderTemplate(
  template: EmailTemplate,     // Template key
  name: string,                // Recipient name
  inviteNumber?: number,       // For invitation-* resolution
  unsubscribeUrl?: string,     // Unsubscribe link
  trackingPixel?: string,      // <img> tag for open tracking
  campaignId?: string          // For betaInfoUrl injection
): { html: string; subject: string; text: string }
```

**Replacements applied** (in order):
1. `{trackingPixel}` → raw `<img>` tag
2. `{betaInfoUrl}` → `https://...beta-info?c={campaignId}`

### 4.3 Template Overrides (Firestore)

Stored in `emailTemplateOverrides/{key}` where key is `invitation-N` or `outreach-N`.

When an override exists, the campaign engine uses it instead of the code template. Override HTML is processed with:

1. `{{name}}` → recipient name
2. `${name}` → recipient name (alternative syntax)
3. `{betaInfoUrl}` → beta info link
4. Auto CTA injection: scans `<a href="...beta-info...">` links and appends `?c={campaignId}`
5. Tracking pixel appended
6. Unsubscribe link appended

### 4.4 Send-Now (Manual Send)

Endpoint: `POST /api/campaigns/{id}/send-now`

- Accepts array of contact emails in request body
- Returns NDJSON stream with per-contact progress `{"type":"progress","email":"...","status":"sent"|"failed"}`
- Override branch injects `${name}`, `{betaInfoUrl}`, auto CTA `?c=`, tracking pixel, unsubscribe link
- Sets `campaignData.{campaignId}.status = "invited"` on success, `"failed"` on failure
- No `batchSize` limit — processes all selected contacts

### 4.5 Tracking Pixel

**URL construction** (`getTrackingUrl()` in `lib/emailService.ts`):

```
{baseUrl}/api/track/open?e={email}&c={campaignId}&t={hmac}
```

- `baseUrl` = `NEXT_PUBLIC_APP_URL` → `VERCEL_URL` → `http://localhost:3000`
- `hmac` = first 12 chars of `HMAC-SHA256(TRACKING_SECRET, "{email}|{campaignId}")`

**Pixel endpoint** (`GET /api/track/open`):
1. Validates HMAC with `TRACKING_SECRET || "fallback-secret"`
2. Writes `email_opened` event to `activityLog`
3. Sets `campaignData.{campaignId}.opened = true` and `openedAt` on the contact doc
4. Returns 1×1 transparent GIF (always, even on error)

### 4.6 Unsubscribe

**Generation**: `{baseUrl}/api/unsubscribe?email={recipient}`

- Added to every campaign email as a footer link
- Also sent as `List-Unsubscribe` header (RFC 2369)
- Supports `List-Unsubscribe-Post: List-Unsubscribe=One-Click` (RFC 8058)

**Endpoint** (`GET /api/unsubscribe`):
1. Reads `email` query param
2. Sets top-level `status = "unsubscribed"` on the contact doc
3. Sets `campaignData.*.status = "unsubscribed"` for all campaigns
4. Logs `unsubscribed` event to activityLog

---

## 5. Campaign Engine

### 5.1 Entry Point: `processAllCampaigns()`

Called by cron (`POST /api/campaign/process` with `x-cron-secret` header).

**Flow:**
1. Create write buffer (batches up to 400 Firestore writes)
2. Load global campaign config from `campaignConfig/default`
3. Run **reply detection** (all accounts, all threads)
4. Run **bounce detection** (all accounts, sent-verified)
5. Log detection summary to activityLog
6. Hard-delete expired soft-deleted contacts
7. Activate scheduled campaigns whose time has come
8. For each **running** campaign: `processSingleCampaign()`
9. Flush write buffer
10. Update `lastDetectedAt` for all running campaigns
11. Log `email_run_completed` for each campaign

### 5.2 Per-Campaign Processing: `processSingleCampaign()`

**Flow per contact:**

1. **Batch limit check**: Stop if `processed >= batchSize`
2. **Hourly limit check**: Stop if `emailsSentThisHour >= maxEmailsPerHour`
3. **Unsubscribe check**: Skip if top-level `status === "unsubscribed"`
4. **Reply/conversion check**: Skip if per-campaign `status === "replied"` or `"converted"`
5. **Applicant check**: Query `betaUsers/{email}` — if found, attribute conversion to this campaign (or not, if `convertedFromCampaign` points elsewhere)
6. **Max invites check**: If `inviteCount >= maxInvites`, mark `no_response` and skip
7. **Template check**: Look up `templateSequence[inviteNumber - 1]` — skip if empty/undefined
8. **Sender assignment**: Check `assignedSender` for consistency; assign if first contact
9. **Render template**: Check overrides first, fall back to code templates
10. **Send email**: Via Gmail API with fallback sender logic
11. **Update contact**: `inviteCount++`, `lastInviteAt`, `nextEligibleAt`, `emailStatus`, `status`

### 5.3 Contact Eligibility (`fetchEligibleContactsForCampaignAdmin`)

Query: `recruitmentContacts` where `campaigns` array-contains `campaignId`.

Then filtered client-side:
- `inviteCount < maxInvites`
- `nextEligibleAt` is in the past or null
- `stoppedReason` is falsy
- Per-campaign status not `converted`, `no_response`, `deleting`, or `unsubscribed`

### 5.4 Conversion Attribution

When a contact registers via `/beta-info`, their `betaUsers/{email}` doc stores `convertedFromCampaign: campaignId` (from the `?c=` URL parameter).

The campaign engine checks:
```typescript
const applicantSource = betaUsers[email].convertedFromCampaign || null;
if (applicantSource === null || applicantSource === currentCampaignId) {
  // Attributed to this campaign
  markConverted(currentCampaignId);
}
```

This means:
- Contacts with **no** `convertedFromCampaign` (pre-attribution) are attributed to whichever campaign detects them first
- Contacts with a specific `convertedFromCampaign` are attributed only to that campaign

### 5.5 Sender Consistency & Fallback

Each contact is assigned a sender on first contact (`assignedSender`). All subsequent emails use the same sender for thread consistency.

If the assigned sender fails authentication:
1. Try the fallback sender (`DEFAULT_SENDER` — `ashwathai.dev@gmail.com`)
2. If fallback also fails, mark as `failed`
3. Log `sender_fallback` event to activityLog

---

## 6. Detection Systems

### 6.1 Bounce Detection (`detectBounces()`)

**How it works:**

1. Polls ALL connected `emailAccounts` inboxes for `from:mailer-daemon@googlemail.com`
2. Skips messages already in `appConfig/bounceDetection.processedBounceIds`
3. Fetches each DSN message body, extracts email addresses via regex
4. **Sent-verification** (critical): Queries `emailLog` for `status === "sent"` AND `recipient === bouncedEmail`
   - If no sent record found: logs warning and **skips** (prevents false positives from stale bounces)
5. Updates `emailLog` → latest sent entry gets `status: "failed"`
6. Updates `recruitmentContacts/{email}` → only marks campaigns that have sent records (not all campaigns)
7. Updates `betaUsers/{email}` → `emailStatus: "failed"`
8. Logs `invitation_failed` event to activityLog
9. Records message ID in `processedBounceIds`

**Called by**: cron (every run) and manual "Detect" button.

### 6.2 Reply Detection (`detectReplies()`)

**How it works:**

1. Loads `emailLog` (last 1000 entries, ordered by sentAt desc)
2. Builds a thread map: `threadId → { campaignId, recipient }`
3. Loads all connected sender accounts
4. Polls each inbox for `in:inbox after:{lastCheckAt}`
5. For each unprocessed message:
   - Check if `threadId` matches a known sent thread
   - Fetch the full message (`format=full`)
   - Extract sender from `From` header
   - **Verify**: sender email matches the original recipient
   - If match: store `replySnippet` (first 2000 chars of plaintext body)
6. Writes `campaignData.{campaignId}.status = "replied"` — **does not touch top-level status**
7. Logs `replied` event to activityLog

**Key design decision**: Reply detection is **campaign-specific** via thread matching. Replying in Campaign A does NOT block Campaign B.

**Called by**: cron (every run) and manual "Detect" button.

### 6.3 Open Tracking (Pixel)

**How it works:**

1. Email embeds a 1×1 transparent GIF: `{baseUrl}/api/track/open?e={email}&c={campaignId}&t={hmac}`
2. HMAC prevents spoofed pixel requests
3. When Gmail loads the image, the endpoint:
   - Validates HMAC
   - Writes `email_opened` event to activityLog
   - Sets `campaignData.{campaignId}.opened = true` on the contact doc
   - Returns transparent GIF (always, even on error)
4. The "Opened" stat counts contacts where `campaignData.{campaignId}.opened === true`
5. The "Engaged" stat counts contacts who either opened OR replied

**Limitation**: Only works when the recipient's email client loads remote images. Gmail's image proxy requires the pixel URL to be publicly accessible (not `localhost`).

---

## 7. API Reference

### 7.1 Campaign CRUD

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/campaigns` | GET | Admin | List all campaigns |
| `/api/campaigns` | POST | Admin | Create campaign |
| `/api/campaigns/{id}` | GET | Admin | Get campaign with contactBreakdown |
| `/api/campaigns/{id}` | PUT | Admin | Update campaign (name, settings, status, scheduledStartAt) |
| `/api/campaigns/{id}` | DELETE | Owner | Delete campaign + cleanup contact refs |

### 7.2 Campaign Lifecycle

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/campaigns/{id}/start` | POST | Admin | Preflight + start (draft → running) |
| `/api/campaigns/{id}/start` | GET | Admin | Preflight check only (no state change) |
| `/api/campaigns/{id}/duplicate` | POST | Admin | Clone campaign |

**Start preflight checks:**
1. Campaign exists and is not running/completed/cancelled
2. Sender account is connected (or default sender exists)
3. **Template sequence is non-empty and has no blank slots**
4. Schedule (if set) is in the future
5. At least one contact is enrolled
6. Batch size and max invites are positive

### 7.3 Sending

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/campaigns/{id}/send-now` | POST | Admin | Manual send to selected contacts (NDJSON) |
| `/api/campaigns/{id}/import-contacts` | POST | Admin | Import contacts from another campaign |
| `/api/campaigns/{id}/add-test-contact` | POST | Admin | Add a single test contact and start |

### 7.4 Detection

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/campaigns/{id}/detect` | POST | Admin | Manual detect: bounce + reply for all campaigns |
| `/api/campaign/process` | POST | Cron | Full cron run (requires `x-cron-secret` header) |

### 7.5 Public

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/track/open` | GET | Tracking pixel (returns 1×1 GIF) |
| `/api/unsubscribe` | GET | Unsubscribe handler |

### 7.6 Email Accounts

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/email-accounts` | GET | List connected accounts |
| `/api/email-accounts/set-default` | POST | Set default sender |
| `/api/gmail/status` | GET | Check auth status |
| `/api/gmail/exchange` | POST | Exchange OAuth code for tokens |
| `/api/gmail/disconnect` | POST | Delete OAuth tokens |
| `/api/gmail/send` | POST | Send single/bulk emails manually |

---

## 8. UI Components

### 8.1 Campaign List (`/beta-dashboard/campaigns`)

Each campaign card shows:
- **Pipeline column** (left): Pending, Invited, Converted, Failed
- **Engagement column** (right): Opened, Replied, Bounced, No Response
- Status badge (color-coded)
- Send progress bar
- Dates (created, last activity)
- "New Campaign" button with modal (name + optional schedule)

### 8.2 Campaign Workspace (`/beta-dashboard/campaigns/{id}`)

**Header:** Campaign name, status badge, action buttons (Start/Schedule/Pause/Cancel), Detect button

**Tabs:**

| Tab | Content |
|-----|---------|
| **Overview** | Stat cards (Total, Contacted, Replied, Opened, Converted, Bounced, No Response, Errors, Last Run), StatusDonut, Campaign Info panel, Recent Activity, Pre-flight Check, Danger Zone (delete) |
| **Audience** | OutreachTab with filter cards (Pending, Invited, Engaged, Converted, Failed, Bounced), searchable table, modals (Add, Import, StatusDetail) |
| **Settings** | Template sequence selectors (one per mail slot), Delay Days, Max Invites, Batch Size, Max Emails/Hour, Delay Between Emails (seconds), Sender selection (default or specific) |
| **Activity** | Full event timeline (paginated) |
| **Errors** | Merged error feed from emailLog + activityLog (paginated) |

### 8.3 Key UI Patterns

- **effectiveStatus()**: Returns `"opened"` for contacts who opened but haven't replied (cyan dot, "Opened" label)
- **Engaged card**: Counts `opened OR replied` per-campaign; shows sub-labels `Replied: X · Opened: Y`
- **StatusDetailModal**: Shows per-contact details by status (reply snippet, bounce reason, error message)
- **Tab-switch validation**: Leaving Settings tab triggers validation — blocks if template slots are empty (shake animation)
- **Status donut**: Color-coded by status type (bounced = `#dc2626`, opened = `#06b6d4`, etc.)

---

## 9. Configuration & Secrets

### 9.1 Environment Variables

| Variable | Required | Purpose |
|----------|----------|---------|
| `NEXT_PUBLIC_APP_URL` | Production | Base URL for tracking pixel, unsubscribe links, email links. Falls back to `VERCEL_URL` → `localhost:3000` |
| `CRON_SECRET` | Yes | HMAC for `POST /api/campaign/process`. Must match GitHub Actions secret |
| `TRACKING_SECRET` | Yes | HMAC for tracking pixel. Must match on server and in pixel route. Falls back to `"fallback-secret"` |
| `GOOGLE_CLIENT_ID` | Yes | Gmail OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Yes | Gmail OAuth client secret |
| `NEXT_PUBLIC_GOOGLE_CLIENT_ID` | Yes | Same as `GOOGLE_CLIENT_ID` (needed on client side) |
| `NEXT_PUBLIC_FIREBASE_*` | Yes | 6 Firebase config variables (apiKey, authDomain, projectId, storageBucket, messagingSenderId, appId) |

### 9.2 GitHub Actions Secrets

Set in GitHub repo → Settings → Secrets and variables → Actions:

| Secret | Value |
|--------|-------|
| `CRON_SECRET` | Same as Vercel env var |
| `TRACKING_SECRET` | Same as Vercel env var |

### 9.3 Vercel Settings

| Setting | Value |
|---------|-------|
| Root Directory | `website/site` |
| Production Branch | `website` (or `master`, depending on setup) |
| Framework Preset | Next.js |
| Environment Variables | All 10+ vars from section 9.1 |

### 9.4 Firestore Config Documents

| Document | Purpose | Defaults |
|----------|---------|----------|
| `campaignConfig/default` | Global campaign defaults | See section 2.9 |
| `senderProfiles/default` | Default sender identity | `{ name: "Ashwath AI", email: "ashwathai.dev@gmail.com" }` |
| `appConfig/bounceDetection` | Bounce detection state | `{ lastCheckAt: null, processedBounceIds: [] }` |
| `appConfig/replyDetection` | Reply detection state | `{ lastCheckAt: <7 days ago>, processedReplyIds: [] }` |

---

## 10. Whitelabel Surface Catalog

Every place that needs to change when rebranding for a client.

### 10.1 Sender Identity

| Surface | Location | Default | Client Change |
|---------|----------|---------|---------------|
| Sender name | `lib/emailService.ts` — `DEFAULT_SENDER` | `"Ashwath AI"` | Client company name |
| Sender email | `lib/emailService.ts` — `DEFAULT_SENDER` | `"ashwathai.dev@gmail.com"` | Client email |
| Fallback sender email | `lib/gmailReplyDetection.ts` — `DEFAULT_SENDER_EMAIL` | `"ashwathai.dev@gmail.com"` | Client email |
| From header | `buildMimeMessage()` uses sender profile | Dynamic | Per-campaign via `senderAccountId` |
| Default sender profile | Firestore `senderProfiles/default` | Ashwath AI | Client doc (name, email) |

### 10.2 Email Templates

| Surface | Location | Client Change |
|---------|----------|---------------|
| All invitation copy | `lib/emailTemplates/invitation-1.ts` through `-5.ts` | Full HTML rewrite |
| All outreach copy | `lib/emailTemplates/outreach-1.ts` through `-5.ts` | Full HTML rewrite |
| Acknowledgement | `lib/emailTemplates/acknowledgement.ts` | Full HTML rewrite |
| Welcome email | `lib/emailTemplates/welcome.ts` | Full HTML rewrite |
| Rejection email | `lib/emailTemplates/reject.ts` | Full HTML rewrite |
| Email layout (shell) | `lib/emailTemplates/layout.ts` | Logo, brand colors, footer, company name |
| Template overrides | Firestore `emailTemplateOverrides/{key}` | Per-client overrides (UI-managed) |

### 10.3 URLs & Domains

| Surface | Location | Default | Client Change |
|---------|----------|---------|---------------|
| Beta info landing | Hardcoded in `campaignEngine.ts:269-271`, `emailTemplates/index.ts` | `jump-droid.vercel.app/beta-info` | Client's landing page URL |
| Tracking pixel base | `resolveBaseUrl()` in `emailService.ts` | `NEXT_PUBLIC_APP_URL` → `VERCEL_URL` | Client's deployment URL |
| Unsubscribe base | Same `resolveBaseUrl()` | Same | Client's deployment URL |
| App URL env var | `NEXT_PUBLIC_APP_URL` | Set per-deployment | Client's production URL |

### 10.4 UI Branding

| Surface | Location | Default | Client Change |
|---------|----------|---------|---------------|
| Accent color | Tailwind classes across all components | `cyan` | Search/replace `cyan-*` with client brand color |
| Secondary color | Tailwind classes | `slate` | Search/replace `slate-*` |
| Success color | Tailwind classes | `green` | Search/replace `green-*` |
| Warning color | Tailwind classes | `yellow` / `amber` | Search/replace |
| Error color | Tailwind classes | `red` | Search/replace |
| Page title | Metadata in page files | "Jump Droid" | Client project name |
| Marketing home | `app/page.tsx` | Jump Droid game | Client landing page |

### 10.5 Email Accounts

| Surface | Location | Client Change |
|---------|----------|---------------|
| Connected senders | Firestore `emailAccounts/*` | Client's Gmail accounts (connected via UI) |
| Default sender | Firestore `senderProfiles/default` | Client's sender doc |
| Campaign sender | Per-campaign `settings.senderAccountId` | Set per campaign via UI |

### 10.6 Configuration Tuning

| Surface | Location | Client Change |
|---------|----------|---------------|
| Campaign defaults | `DEFAULT_CAMPAIGN_CONFIG` in `campaignService.ts` | Per-client rate limits, delays |
| Rate limits | `maxEmailsPerHour`, `delayBetweenEmailsMs` | Tune for client's Gmail quota |
| Cron schedule | GitHub Actions `.github/workflows/campaign-processor.yml` | Adjust frequency |
| OAuth scopes | Google Cloud Console | `gmail.send` + `gmail.readonly` |

---

## 11. Debug Logging

### 11.1 systemLogs Collection

Written by `logDebugAdmin()` in `lib/debugLogger.ts`. Uses the `debugLogger` utility.

### 11.2 Event Types

| Event Type | Severity | When |
|------------|----------|------|
| `cron.start` | info | Campaign cron job started |
| `cron.complete` | info | Campaign cron job completed with results |
| `cron.error` | error | Campaign cron job failed |
| `campaign_engine.start` | info | Processing run begins (lists running campaigns) |
| `campaign_engine.no_campaigns` | info | No running campaigns found |
| `campaign_engine.contacts_fetched` | info | Eligible contacts count for a campaign |
| `campaign_engine.no_contacts` | info | No eligible contacts for a campaign |
| `campaign_engine.template_missing` | warn | Contact skipped because template slot is empty |
| `bounce_detection.not_sent` | warn | Bounce found but no sent record — skipped |
| `bounce_detection.no_campaign_match` | warn | Bounce found but no campaign with sent record matched |
| `reply_detection.thread_map` | info | Number of threads loaded from emailLog |
| `reply_detection.no_threads` | info | No sent email threads — skipping reply detection |
| `reply_detection.match_found` | info | Reply matched to a campaign thread |
| `reply_detection.sender_mismatch` | info | Thread matched but sender != recipient |
| `tracking_pixel.missing_params` | warn | Pixel hit without required query params |
| `tracking_pixel.invalid_hmac` | warn | Pixel hit with invalid HMAC token |
| `tracking_pixel.opened` | info | Email open recorded successfully |
| `tracking_pixel.write_error` | error | Failed to write open event to Firestore |
| `detect.manual_complete` | info | Manual detect completed with results |
| `detect.manual_error` | error | Manual detect failed |

### 11.3 Console Log Format

```
[2026-07-23T10:30:00.123Z] [INFO ] [campaign_engine.start] [campaign_123] Processing run begins | {"runningCampaigns":2}
```

Viewable in Vercel dashboard → Deployments → {deployment} → Functions → Logs.

---

## 12. File Layout

```
website/site/
├── app/
│   ├── api/
│   │   ├── campaign/
│   │   │   └── process/route.ts              # Cron endpoint
│   │   ├── campaigns/
│   │   │   ├── route.ts                       # List / Create
│   │   │   ├── [id]/
│   │   │   │   ├── route.ts                   # Read / Update / Delete
│   │   │   │   ├── start/route.ts             # Preflight + Start
│   │   │   │   ├── send-now/route.ts          # Manual send (NDJSON)
│   │   │   │   ├── detect/route.ts            # Manual detect
│   │   │   │   ├── duplicate/route.ts         # Clone campaign
│   │   │   │   ├── import-contacts/route.ts   # Import from another campaign
│   │   │   │   └── add-test-contact/route.ts  # Add test contact + start
│   │   ├── track/
│   │   │   └── open/route.ts                  # Tracking pixel
│   │   ├── unsubscribe/route.ts               # Unsubscribe handler
│   │   ├── email-accounts/
│   │   │   ├── route.ts                       # List accounts
│   │   │   └── set-default/route.ts           # Set default sender
│   │   └── gmail/
│   │       ├── status/route.ts                # Auth status
│   │       ├── send/route.ts                  # Manual send
│   │       ├── exchange/route.ts              # OAuth exchange
│   │       └── disconnect/route.ts            # Disconnect
│   └── beta-dashboard/
│       ├── campaigns/
│       │   ├── page.tsx                       # Campaign list
│       │   └── [id]/page.tsx                  # Campaign workspace (1342 lines)
│       ├── recruitment/page.tsx               # Recruitment dashboard
│       └── settings/
│           ├── campaign/page.tsx              # Global campaign config
│           └── email-accounts/page.tsx        # Email account management
├── components/beta/
│   ├── OutreachTab.tsx                        # Outreach orchestrator + filters
│   ├── OutreachContactsTable.tsx              # Contacts table with effectiveStatus
│   ├── OutreachDashboardCards.tsx             # KPI stat cards
│   ├── OutreachImportCsv.tsx                  # CSV import modal
│   ├── AddManualContact.tsx                   # Manual add modal
│   ├── StatusDetailModal.tsx                  # Per-contact status details
│   └── ActivityTimeline.tsx                   # Event timeline
├── lib/
│   ├── campaignEngine.ts                      # Core campaign processing (632 lines)
│   ├── campaignProcessor.ts                   # Write buffer (batched Firestore writes)
│   ├── emailService.ts                        # Gmail API: tokens, send, tracking URL, bounce
│   ├── gmailReplyDetection.ts                 # Reply detection (per-campaign thread matching)
│   ├── debugLogger.ts                         # Debug logging utility (console + Firestore)
│   ├── emailTemplates/
│   │   ├── index.ts                           # Template registry (renderTemplate)
│   │   ├── layout.ts                          # Shared HTML shell
│   │   ├── acknowledgement.ts                 # Registration acknowledgement
│   │   ├── welcome.ts                         # Approval welcome
│   │   ├── reject.ts                          # Rejection notice
│   │   ├── invitation-1.ts through -5.ts      # Campaign invitation sequence
│   │   └── outreach-1.ts through -5.ts        # Campaign outreach sequence
│   └── firebase/
│       ├── campaignService.ts                 # Campaign CRUD, eligibility, preflight (708 lines)
│       ├── outreachService.ts                 # Contact CRUD, helper functions
│       ├── bounceDetectionAdmin.ts            # Bounce detection (sent-verified)
│       ├── activityService.ts                 # Activity log (client + admin)
│       ├── auditService.ts                    # Email audit log
│       ├── templateService.ts                 # Template loading
│       └── admin.ts                           # Admin Firestore initialization
├── types/
│   ├── campaign.ts                            # Campaign, CampaignConfig, CampaignContactData
│   ├── recruitmentContacts.ts                 # OutreachContact, OutreachStatus
│   ├── emailLog.ts                            # EmailTemplate, EmailLogEntry
│   ├── activityLog.ts                         # ActivityEventType
│   ├── emailAudit.ts                          # EmailAuditEventType
│   └── senderProfile.ts                       # SenderProfile
└── .github/workflows/
    └── campaign-processor.yml                 # GitHub Actions cron (hourly)
```
