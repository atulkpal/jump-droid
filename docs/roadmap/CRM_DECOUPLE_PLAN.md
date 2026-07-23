# CRM Decoupling Plan — ReachEngine

**Goal**: Extract the Jump Droid CRM & Campaign System into a portable, framework-agnostic package that can be dropped into any project with minimal configuration.

---

## Table of Contents

1. [Vision](#1-vision)
2. [Variable Catalog](#2-variable-catalog)
3. [Integration Contracts](#3-integration-contracts)
4. [Extraction Phases](#4-extraction-phases)
5. [Customization API Design](#5-customization-api-design)
6. [Migration Guide Sketch](#6-migration-guide-sketch)

---

## 1. Vision

### The Package: `@reach-engine/core`

```
npm install @reach-engine/core
```

Framework-agnostic TypeScript library with Firestore Admin SDK as the only peer dependency.

### Optional Add-ons

| Package | Description |
|---------|-------------|
| `@reach-engine/react` | React hooks, providers, context |
| `@reach-engine/next` | Next.js route handlers, API helpers |
| `@reach-engine/ui` | Reference UI components (Tailwind, themeable) |

### Design Principles

1. **Zero hardcoded strings** — all collection names, document IDs, status values, and config keys are injectable.
2. **Plugin architecture** — email providers, template renderers, and detection strategies are swappable.
3. **Whitelabel by default** — brand identity is a config object, not scattered across files.
4. **Framework-agnostic core** — the engine, data layer, and types have zero React/Next.js dependencies.
5. **Backward compatible** — the existing Jump Droid codebase continues to work identically.

---

## 2. Variable Catalog

Every hardcoded value that must become a configuration parameter. Organized by module.

### 2.1 Collection Names

| Current Name | Config Key | Default | Used In |
|-------------|------------|---------|---------|
| `recruitmentContacts` | `collections.contacts` | `"contacts"` | campaignService, outreachService, bounceDetection, replyDetection, track/open, unsubscribe |
| `campaigns` | `collections.campaigns` | `"campaigns"` | campaignService, campaignEngine, all campaign routes |
| `emailLog` | `collections.emailLog` | `"emailLog"` | emailService, campaignEngine, gmailReplyDetection, bounceDetection |
| `activityLog` | `collections.activityLog` | `"activityLog"` | campaignService, activityService, campaignEngine, bounceDetection, track/open |
| `systemLogs` | `collections.systemLogs` | `"systemLogs"` | debugLogger |
| `emailTemplateOverrides` | `collections.templateOverrides` | `"emailTemplateOverrides"` | campaignEngine |
| `emailAccounts` | `collections.emailAccounts` | `"emailAccounts"` | emailService, campaignService, gmailReplyDetection, bounceDetection |
| `betaUsers` | `collections.applicants` | `"applicants"` | campaignEngine, outreachService |
| `emailAuditLog` | `collections.emailAuditLog` | `"emailAuditLog"` | auditService |
| `appConfig` | `collections.appConfig` | `"appConfig"` | bounceDetection, replyDetection |
| `campaignConfig` | `collections.campaignConfig` | `"campaignConfig"` | campaignService |

### 2.2 Document IDs

| Current ID | Config Key | Default | Used In |
|-----------|------------|---------|---------|
| `campaignConfig/default` | `docIds.campaignConfig` | `"default"` | campaignService (getGlobalCampaignConfigAdmin) |
| `appConfig/bounceDetection` | `docIds.bounceDetection` | `"bounceDetection"` | bounceDetectionAdmin |
| `appConfig/replyDetection` | `docIds.replyDetection` | `"replyDetection"` | gmailReplyDetection |
| `senderProfiles/default` | `docIds.senderProfile` | `"default"` | emailService |

### 2.3 Email Configuration

| Current Value | Config Key | Default | Used In |
|--------------|------------|---------|---------|
| `DEFAULT_SENDER.name` | `sender.defaultName` | `"Ashwath AI"` | emailService, campaignEngine |
| `DEFAULT_SENDER.email` | `sender.defaultEmail` | `"ashwathai.dev@gmail.com"` | emailService, campaignEngine |
| `DEFAULT_SENDER_EMAIL` | `sender.fallbackEmail` | `"ashwathai.dev@gmail.com"` | gmailReplyDetection |
| `"fallback-secret"` | `secrets.trackingFallback` | `"fallback-secret"` | emailService (getTrackingUrl), track/open route |

### 2.4 Campaign Defaults

| Current Value | Config Key | Default | Used In |
|--------------|------------|---------|---------|
| `delayDays: 4` | `campaignDefaults.delayDays` | `4` | campaignService (DEFAULT_CAMPAIGN_CONFIG) |
| `maxInvites: 5` | `campaignDefaults.maxInvites` | `5` | campaignService |
| `delayBetweenEmailsMs: 120000` | `campaignDefaults.delayBetweenEmailsMs` | `120000` | campaignService |
| `batchSize: 5` | `campaignDefaults.batchSize` | `5` | campaignService |
| `maxEmailsPerHour: 100` | `campaignDefaults.maxEmailsPerHour` | `100` | campaignService |
| `senderAccountId: null` | `campaignDefaults.senderAccountId` | `null` | campaignService |
| `templateSequence: []` | `campaignDefaults.templateSequence` | `[]` | campaignService |

### 2.5 Status Enums

| Current Type | Config Key | Used In |
|-------------|------------|---------|
| `OutreachStatus` (lifecycle) | `statuses.contactLifecycle` | recruitmentContacts types, campaignEngine, UI |
| Per-campaign status | `statuses.campaignContact` | campaign.ts types, campaignEngine |
| `CampaignStatus` | `statuses.campaign` | campaign.ts types |

### 2.6 Brand Text

| Surface | Config Key | Default |
|---------|-----------|---------|
| Company name | `brand.companyName` | `"Ashwath AI"` |
| Project name | `brand.projectName` | `"Jump Droid"` |
| Email footer text | `brand.emailFooter` | `"Jump Droid Beta"` |
| Beta info URL | `brand.betaInfoUrl` | `"https://jump-droid.vercel.app/beta-info"` |
| Privacy URL | `brand.privacyUrl` | `"/privacy"` |
| Support email | `brand.supportEmail` | `"ashwathai.dev@gmail.com"` |

### 2.7 Detection Configuration

| Current Value | Config Key | Default | Used In |
|--------------|------------|---------|---------|
| Bounce sender query | `detection.bounceQuery` | `"from:mailer-daemon@googlemail.com"` | bounceDetectionAdmin |
| Reply emailLog limit | `detection.replyLogLimit` | `1000` | gmailReplyDetection |
| Reply snippet max | `detection.replySnippetMaxChars` | `2000` | gmailReplyDetection |
| Rate limit retries | `detection.rateLimitRetries` | `3` | gmailReplyDetection |
| List page size | `detection.listPageSize` | `50` | gmailReplyDetection |

### 2.8 Template Keys

| Current Convention | Config Key | Default |
|-------------------|------------|---------|
| Template sequence keys | `templates.sequenceKeys` | `["invitation-1", ..., "outreach-5"]` |
| Override doc key prefix | `templates.overridePrefix` | `""` (uses template key directly) |

### 2.9 Rate Limits

| Current Value | Config Key | Default | Used In |
|--------------|------------|---------|---------|
| `batchSize: 5` | `limits.batchSize` | `5` | campaignEngine |
| `maxEmailsPerHour: 100` | `limits.maxEmailsPerHour` | `100` | campaignEngine |
| `delayBetweenEmailsMs: 120000` | `limits.delayBetweenEmailsMs` | `120000` | campaignEngine |

---

## 3. Integration Contracts

What a host project must provide to use ReachEngine.

### 3.1 Firestore Schema

Required collections and their indexes:

| Collection | Required Fields | Indexes |
|-----------|----------------|---------|
| `contacts` (configurable) | See CRM_SYSTEM.md §2.1 | `campaigns` array-contains (for eligibility queries) |
| `campaigns` (configurable) | See CRM_SYSTEM.md §2.2 | None required (reads by doc ID or status filters) |
| `emailLog` | recipient, campaign, status, sentAt, providerThreadId | `recipient ASC, sentAt DESC`, `status ASC, sentAt DESC` |
| `activityLog` | applicantEmail, eventType, createdAt, campaignId | `applicantEmail ASC, createdAt ASC` |
| `systemLogs` | timestamp, eventType, severity | Should use Firestore TTL policy (auto-delete after 7 days) |

### 3.2 Environment Variables

```bash
# Firebase (required)
NEXT_PUBLIC_FIREBASE_API_KEY=
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=
NEXT_PUBLIC_FIREBASE_PROJECT_ID=
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=
NEXT_PUBLIC_FIREBASE_APP_ID=

# Gmail OAuth (required)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
NEXT_PUBLIC_GOOGLE_CLIENT_ID=

# App URL (required in production)
NEXT_PUBLIC_APP_URL=

# Secrets (required for production)
CRON_SECRET=
TRACKING_SECRET=
```

### 3.3 Gmail OAuth Setup

1. Google Cloud Console → APIs & Services → Credentials
2. Create OAuth 2.0 Client ID (Web application)
3. Authorized redirect URI: `{NEXT_PUBLIC_APP_URL}/gmail/callback`
4. Scopes:
   - `https://www.googleapis.com/auth/gmail.send`
   - `https://www.googleapis.com/auth/gmail.readonly`
   - `https://www.googleapis.com/auth/gmail.modify` (only if deleting DSNs)
5. Set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` in env vars

### 3.4 GitHub Actions Secrets

| Secret | Used By |
|--------|---------|
| `CRON_SECRET` | `.github/workflows/campaign-processor.yml` |
| `TRACKING_SECRET` | Pixel HMAC (must match Vercel env) |

### 3.5 Vercel Deployment

| Setting | Value |
|---------|-------|
| Root Directory | Where `package.json` lives |
| Framework Preset | Next.js |
| Environment Variables | All Firebase + Google + secrets |

---

## 4. Extraction Phases

### Phase 0: Audit & Catalog (Current State)

- [x] Complete type definitions documented
- [x] Complete API surface documented
- [x] Variable catalog compiled (this document)
- [x] Whitelabel surfaces cataloged (CRM_SYSTEM.md §10)
- [ ] Identify all cross-file imports and dependencies
- [ ] Map the dependency graph between lib/ modules

### Phase 1: Core Library Package (`@reach-engine/core`)

**Goal**: Pure TypeScript package with zero framework dependencies. Can be used in any Node.js backend.

**Scope:**
- Extract `lib/campaignEngine.ts` → strip all Next.js/React imports
- Extract `lib/emailService.ts` → only server-side functions (getAccessTokenAdmin, buildMimeMessage, getTrackingUrl, resolveBaseUrl)
- Extract `lib/campaignProcessor.ts`
- Extract `lib/gmailReplyDetection.ts`
- Extract `lib/firebase/bounceDetectionAdmin.ts`
- Extract `lib/firebase/campaignService.ts` → admin SDK functions only
- Extract `lib/firebase/activityService.ts` → admin SDK functions only
- Extract `lib/firebase/auditService.ts` → admin SDK functions only
- Extract `lib/debugLogger.ts`
- Extract all types from `types/`
- Extract `lib/emailTemplates/` as optional (consumers can provide their own)

**Key changes during extraction:**
- Replace all hardcoded strings with injected configuration
- Replace `import from "@/..."` with standard npm imports
- Replace `process.env.*` with injected config object
- Add `EngineConfig` interface with all parameters from the variable catalog

**Package structure:**
```
@reach-engine/core/
├── src/
│   ├── engine/
│   │   ├── campaignEngine.ts
│   │   ├── campaignProcessor.ts
│   │   └── types.ts
│   ├── email/
│   │   ├── service.ts
│   │   ├── gmailReplyDetection.ts
│   │   └── types.ts
│   ├── detection/
│   │   ├── bounceDetection.ts
│   │   └── types.ts
│   ├── data/
│   │   ├── campaignService.ts
│   │   ├── activityService.ts
│   │   ├── auditService.ts
│   │   └── types.ts
│   ├── logging/
│   │   └── debugLogger.ts
│   ├── config.ts        # EngineConfig interface + defaults
│   └── index.ts         # Public API
├── package.json
├── tsconfig.json
└── README.md
```

### Phase 2: Next.js Route Handlers (`@reach-engine/next`)

**Goal**: Composable API route handlers that can be mounted in any Next.js App Router project.

**Scope:**
- Extract all route files from `app/api/campaigns/`, `app/api/campaign/`, `app/api/track/`, `app/api/unsubscribe/`
- Convert each into a factory function: `createCampaignHandler(config)`, `createDetectHandler(config)`, etc.
- Export as: `import { CampaignRoutes } from "@reach-engine/next"`

**Pattern:**
```typescript
// In the host app:
import { createCampaignRoutes } from "@reach-engine/next";
import { engineConfig } from "./reach-engine.config";

export const { GET, PUT, DELETE } = createCampaignRoutes(engineConfig);
```

### Phase 3: React Hooks & Providers (`@reach-engine/react`)

**Goal**: React hooks for campaign management UI.

**Scope:**
- `useCampaigns()` — list all campaigns
- `useCampaign(id)` — single campaign with contactBreakdown
- `useCampaignContacts(id)` — contacts for a campaign
- `useCampaignTimeline(id)` — activity log for a campaign
- `ReachEngineProvider` — provides config to all hooks

### Phase 4: Reference UI Components (`@reach-engine/ui`)

**Goal**: Themeable, whitelabel-ready React components that consume the hooks.

**Scope:**
- CampaignList — card-based campaign list
- CampaignWorkspace — 5-tab workspace
- CampaignSettings — template sequence, config form
- CampaignOverview — stat cards, donut, info, activity
- AudienceTable — contacts with effectiveStatus
- StatusDetailModal — per-contact details
- OutreachDashboardCards — KPI cards

**Theming**: All colors via CSS custom properties:
```css
--reach-primary: #06b6d4;      /* default cyan */
--reach-primary-text: #67e8f9;
--reach-surface: #0a0a0f;
--reach-border: rgba(255,255,255,0.05);
--reach-error: #ef4444;
--reach-success: #34d399;
--reach-warning: #fbbf24;
```

### Phase 5: Documentation & Examples

**Goal**: A new user can set up ReachEngine in 5 minutes.

**Outputs:**
- Quickstart guide (new project setup)
- Migration guide (existing Jump Droid → ReachEngine)
- Example app (Next.js + ReachEngine)
- Full API reference (auto-generated from TypeScript)
- Whitelabel tutorial (rebranding in 10 steps)

---

## 5. Customization API Design

### 5.1 Engine Configuration

```typescript
interface ReachEngineConfig {
  // Firestore collection mapping
  collections: {
    contacts: string;       // default: "contacts"
    campaigns: string;      // default: "campaigns"
    emailLog: string;       // default: "emailLog"
    activityLog: string;    // default: "activityLog"
    systemLogs: string;     // default: "systemLogs"
    templateOverrides: string; // default: "emailTemplateOverrides"
    emailAccounts: string;  // default: "emailAccounts"
    applicants: string;     // default: "applicants"
    emailAuditLog: string;  // default: "emailAuditLog"
    appConfig: string;      // default: "appConfig"
    campaignConfig: string; // default: "campaignConfig"
  };

  // Brand identity
  brand: {
    companyName: string;
    projectName: string;
    emailFooter: string;
    betaInfoUrl: string;
    supportEmail: string;
  };

  // Default sender
  sender: {
    defaultName: string;
    defaultEmail: string;
    fallbackEmail: string;
  };

  // Campaign defaults
  campaignDefaults: {
    delayDays: number;
    maxInvites: number;
    delayBetweenEmailsMs: number;
    batchSize: number;
    maxEmailsPerHour: number;
    senderAccountId: string | null;
    templateSequence: string[];
  };

  // Detection tuning
  detection: {
    bounceQuery: string;
    replyLogLimit: number;
    replySnippetMaxChars: number;
    rateLimitRetries: number;
    listPageSize: number;
  };

  // Secrets
  secrets: {
    trackingFallback: string;
  };

  // Plugin overrides
  plugins: {
    templateRenderer?: TemplateRenderer;
    emailSender?: EmailSender;
    bounceDetector?: BounceDetector;
    replyDetector?: ReplyDetector;
  };
}
```

### 5.2 Plugin Interfaces

```typescript
// Replaceable template renderer
interface TemplateRenderer {
  render(
    template: string,
    name: string,
    options: { inviteNumber?: number; campaignId?: string; unsubscribeUrl?: string }
  ): { html: string; subject: string; text: string };
}

// Replaceable email sender (default: Gmail API)
interface EmailSender {
  send(
    to: string,
    name: string,
    subject: string,
    html: string,
    text: string,
    options: { sender?: SenderProfile; senderAccountId?: string }
  ): Promise<{ success: boolean; error?: string; gmailMessageId?: string; gmailThreadId?: string }>;
}

// Replaceable bounce detection (default: Gmail DSN polling)
interface BounceDetector {
  detect(adminFirestore: Firestore): Promise<number>;
}

// Replaceable reply detection (default: Gmail thread matching)
interface ReplyDetector {
  detect(adminFirestore: Firestore, buffer: PendingWrite[]): Promise<ReplyDetectionResult>;
}
```

### 5.3 Usage Example

```typescript
import { createEngine } from "@reach-engine/core";
import { adminFirestore } from "./firebase";
import { myTemplateRenderer } from "./templates";

const engine = createEngine({
  collections: {
    contacts: "my_crm_contacts",
    campaigns: "my_outreach_campaigns",
    // ... override only what you need
  },
  brand: {
    companyName: "Acme Corp",
    projectName: "Acme Beta",
    emailFooter: "Acme Corp — All rights reserved",
    betaInfoUrl: "https://acme.com/beta-info",
    supportEmail: "help@acme.com",
  },
  sender: {
    defaultName: "Acme Team",
    defaultEmail: "noreply@acme.com",
    fallbackEmail: "admin@acme.com",
  },
  plugins: {
    templateRenderer: myTemplateRenderer,
  },
});

// Run campaigns
const result = await engine.processAllCampaigns(adminFirestore);
```

---

## 6. Migration Guide Sketch

### From Embedded to Package

For the Jump Droid codebase itself (backward compatibility):

```
Current:  import { processCampaign } from "@/lib/campaignEngine";
Future:   import { processCampaign } from "@reach-engine/core";
```

**Step 1:** Install `@reach-engine/core` as a dependency.
**Step 2:** Create `lib/reach-engine.config.ts` with all Jump Droid defaults.
**Step 3:** Replace each internal import with the package import, one module at a time.
**Step 4:** Remove the extracted files from `lib/`.
**Step 5:** Verify all routes, UI, and cron still work identically.

### From Scratch (New Project)

```bash
npm create next-app my-crm --typescript
cd my-crm
npm install @reach-engine/core @reach-engine/next @reach-engine/react @reach-engine/ui
npm install firebase-admin
```

Then follow the Quickstart guide to set up:
1. Firestore collections
2. Environment variables
3. Gmail OAuth
4. Campaign routes
5. UI components

---

## Appendix: Decision Log

| Decision | Rationale |
|----------|-----------|
| Framework-agnostic core first | Can be reused in Express, Fastify, Cloud Functions, etc. |
| Collection names configurable | Host projects may already have existing collections |
| Plugins for email/detection | Gmail is not the only provider; clients may use SendGrid, SES, etc. |
| CSS variable theming | Simplest whitelabel approach with zero build overhead |
| Per-campaign status separate from top-level | Multiple campaigns for same contact must not interfere |
| No hardcoded secrets in code | `TRACKING_SECRET` fallback is a developer convenience, not a production pattern |
