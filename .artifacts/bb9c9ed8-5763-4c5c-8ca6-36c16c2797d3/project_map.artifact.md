# Jump Droid Project Map

A high-level overview of the three primary domains in the Jump Droid ecosystem and their inter-relationships.

```mermaid
graph TD
    subgraph "Core Game Engine (/app)"
        A1[GameEngine] --> A2[MultiplayerManager]
        A1 --> A3[ProgressionManager]
        A1 --> A4[AltitudeZones]
        A2 -- "Writes Game State" --> DB[(Firebase RTDB)]
        A3 -- "Saves Progress" --> FS[(Firestore)]
    end

    subgraph "CRM System (Beta Portal)"
        B1[Dashboard UI] --> B2[CRM API]
        B2 -- "Reads Tester Logs" --> FS
        B2 -- "Admin Outreach" --> Email[Gmail/SES API]
    end

    subgraph "Plain Website (React)"
        C1[Landing Page] --> C2[Lore Archive]
        C2 -- "Fetches Lore" --> FS
        C1 --> C3[Download Links]
    end

    FS -- "Tester Data" --> B2
    FS -- "Lore & Config" --> C2
    DB -- "Active Match" --> A2
```

## Domain Responsibilities

### [Game] Core Engine
- **Platform:** Android (Kotlin/Compose)
- **Role:** High-intensity gameplay, physics, and real-time multiplayer logic.
- **Data Flow:** Primary producer of session analytics and leaderboard scores.

### [Website] Public Landing
- **Platform:** Web (React/Next.js)
- **Role:** Marketing, lore documentation, and public-facing distribution hub.
- **Data Flow:** Consumer of game-exported lore entries and status config.

### [CRM] Beta Portal
- **Platform:** Web (Isolated React/Next.js sub-app)
- **Role:** Recruitment orchestration, tester performance auditing, and campaign management.
- **Data Flow:** Heavy reader of Firestore `testers` and `sessions` collections; orchestrates invitation emails.
