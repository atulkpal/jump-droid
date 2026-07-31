# Graph Report - .  (2026-07-30)

## Corpus Check
- Large corpus: 743 files ╖ ~1,880,339 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 2754 nodes · 4883 edges · 282 communities (198 shown, 84 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 244 edges (avg confidence: 0.8)
- Token cost: 1,024 input · 2,048 output

## Community Hubs (Navigation)
- Website Beta Portal & Dashboard
- Game Constants & Discovery Definitions
- Module Registry & Implementation
- Player Progression & Persistence
- Artifact Management & Set Bonuses
- Website Dependencies & Config
- Website Recruitment Components
- Audio Management & Sound Engine
- Website Contact & Email UI
- Website API: Beta & Contacts
- Encounter Spawning & AI Logic
- Website API: Campaign & Admin
- Website Auth & Admin Pages
- Website Email Templates & Invites
- Core Game Engine & State
- Community 15
- Community 16
- Community 17
- Community 18
- Community 19
- Community 20
- Community 21
- Community 22
- Community 23
- Community 24
- Community 25
- Community 26
- Community 27
- Community 28
- Community 29
- Community 30
- Community 31
- Community 32
- Community 33
- Community 34
- Community 35
- Community 36
- Community 37
- Community 38
- Community 39
- Community 40
- Community 41
- Community 42
- Community 43
- Community 44
- Community 45
- Community 46
- Community 47
- Community 48
- Community 49
- Community 50
- Community 51
- Community 52
- Community 53
- Community 54
- Community 55
- Community 56
- Community 57
- Community 58
- Community 59
- Community 60
- Community 61
- Community 62
- Community 63
- Community 64
- Community 65
- Community 66
- Community 67
- Community 68
- Community 69
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 79
- Community 80
- Community 81
- Community 82
- Community 83
- Community 84
- Community 85
- Community 86
- Community 87
- Community 88
- Community 89
- Community 90
- Community 91
- Community 92
- Community 93
- Community 94
- Community 95
- Community 96
- Community 97
- Community 98
- Community 99
- Community 100
- Community 101
- Community 102
- Community 103
- Community 104
- Community 105
- Community 106
- Community 107
- Community 108
- Community 109
- Community 110
- Community 111
- Community 112
- Community 113
- Community 114
- Community 115
- Community 116
- Community 117
- Community 118
- Community 119
- Community 120
- Community 121
- Community 122
- Community 123
- Community 124
- Community 125
- Community 126
- Community 127
- Community 128
- Community 129
- Community 130
- Community 131
- Community 132
- Community 133
- Community 134
- Community 135
- Community 136
- Community 137
- Community 138
- Community 139
- Community 140
- Community 141
- Community 142
- Community 143
- Community 144
- Community 145
- Community 146
- Community 147
- Community 148
- Community 149
- Community 150
- Community 151
- Community 152
- Community 153
- Community 154
- Community 155
- Community 156
- Community 157
- Community 158
- Community 159
- Community 160
- Community 161
- Community 162
- Community 163
- Community 164
- Community 165
- Community 166
- Community 167
- Community 168
- Community 169
- Community 170
- Community 171
- Community 172
- Community 173
- Community 174
- Community 175
- Community 176
- Community 177
- Community 178
- Community 179
- Community 180
- Community 181
- Community 182
- Community 183
- Community 184
- Community 185
- Community 186
- Community 187
- Community 188
- Community 189
- Community 190
- Community 191
- Community 192
- Community 193
- Community 194
- Community 195
- Community 198
- Community 199
- Community 200
- Community 201
- Community 202
- Community 203
- Community 204
- Community 205
- Community 206
- Community 207
- Community 208
- Community 209
- Community 210
- Community 211
- Community 232
- Community 233
- Community 234
- Community 237
- Community 238
- Community 239
- Community 240
- Community 241
- Community 242
- Community 243
- Community 244
- Community 245
- Community 250
- Community 251
- Community 252
- Community 253
- Community 254
- Community 255
- Community 256
- Community 257
- Community 259
- Community 260
- Community 261
- Community 262
- Community 263
- Community 264
- Community 265
- Community 266
- Community 267
- Community 268
- Community 269
- Community 270
- Community 271
- Community 272
- Community 273
- Community 274
- Community 275
- Community 276
- Community 278
- Community 279
- Community 280
- Community 281

## God Nodes (most connected - your core abstractions)
1. `Player` - 115 edges
2. `DiscoveryType` - 108 edges
3. `ProgressionManager` - 89 edges
4. `getFirestore()` - 87 edges
5. `AltitudeZone` - 81 edges
6. `ActiveThreat` - 60 edges
7. `SoundManager` - 48 edges
8. `ThreatRenderer` - 43 edges
9. `GameEngine` - 36 edges
10. `getAdminFirestore()` - 35 edges

## Surprising Connections (you probably didn't know these)
- `drawPlatformPreview()` --calls--> `Platform`  [INFERRED]
  app/src/main/java/com/ashwathai/jump_droid/EntityPreview.kt → app/src/main/java/com/ashwathai/jump_droid/Platform.kt
- `Jump Droid Project Overview` --references--> `System Architecture`  [EXTRACTED]
  README.md → docs/ARCHITECTURE.md
- `Contribution Guidelines` --references--> `System Architecture`  [EXTRACTED]
  CONTRIBUTING.md → docs/ARCHITECTURE.md
- `JumpDroidApp()` --calls--> `AboutScreen()`  [INFERRED]
  app/src/main/java/com/ashwathai/jump_droid/MainActivity.kt → app/src/main/java/com/ashwathai/jump_droid/AboutScreen.kt
- `drawThreatPreview()` --calls--> `ActiveThreat`  [INFERRED]
  app/src/main/java/com/ashwathai/jump_droid/EntityPreview.kt → app/src/main/java/com/ashwathai/jump_droid/ActiveThreat.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Production Readiness Framework** — plan_epic_13_readiness, doc_v2_0_0_ascension, code_firestore_rules [INFERRED 0.85]
- **Progression System Decomposition** — plan_epic_12_phase_8, code_progression_manager, code_artifact_manager [EXTRACTED 0.95]
- **Project Governance & Skill Model** — readme, contributing, opencode, agents_skill [EXTRACTED 1.00]
- **Core Game Engine Architecture** — game_engine, world_renderer, survival_manager, encounter_director [EXTRACTED 1.00]
- **Release and Milestone Documentation** — docs_releases, docs_changelog, docs_production_checklist, docs_jumpdroid_epic_tracker [EXTRACTED 1.00]
- **Atmospheric Zones** — docs_design_area_library_earth, docs_design_area_library_void [EXTRACTED 1.00]
- **Android Launcher Presence** — app_src_main_res_mipmap_hdpi_ic_launcher, app_src_main_res_mipmap_hdpi_ic_launcher_round, app_src_main_res_mipmap_mdpi_ic_launcher, app_src_main_res_mipmap_mdpi_ic_launcher_round, app_src_main_res_mipmap_xhdpi_ic_launcher, app_src_main_res_mipmap_xhdpi_ic_launcher_round, app_src_main_res_mipmap_xxhdpi_ic_launcher, app_src_main_res_mipmap_xxhdpi_ic_launcher_round, app_src_main_res_mipmap_xxxhdpi_ic_launcher, app_src_main_res_mipmap_xxxhdpi_ic_launcher_round [EXTRACTED 1.00]
- **Web Presence Branding** — website_site_app_icon, website_site_icon, website_site_public_apple_touch_icon, website_site_public_icon [EXTRACTED 1.00]
- **Jump Droid Branding Assets** — media_logos_extra_01_logo, media_logos_extra_02_logo, media_logos_extra_03_logo, media_logos_hero_banner, website_site_public_og_image, website_site_public_twitter_image [EXTRACTED 1.00]
- **Core Gameplay Ecosystem** — concept_expedition_loop, concept_game_hud, concept_data_archive [INFERRED 0.90]
- **Rocket Class Fleet Options** — media_screenshots_04_build_your_perfect_fleet_explorer_class, media_screenshots_04_build_your_perfect_fleet_striker_class, media_screenshots_04_build_your_perfect_fleet_heavy_class, media_screenshots_04_build_your_perfect_fleet_prototype_class [EXTRACTED 1.00]
- **Jump Droid Core Pillars** — media_screenshots_07_every_expedition_makes_you_stronger_sci_fi_arcade, media_screenshots_07_every_expedition_makes_you_stronger_roguelite_progression, media_screenshots_07_every_expedition_makes_you_stronger_one_hand_controls [EXTRACTED 1.00]
- **Website Public Assets** — website_site_public_file_svg, website_site_public_globe_svg, website_site_public_next_svg, website_site_public_vercel_svg, website_site_public_window_svg [EXTRACTED 1.00]

## Communities (282 total, 84 thin omitted)

### Community 0 - "Website Beta Portal & Dashboard"
Cohesion: 0.06
Nodes (77): BetaDashboardPage(), Account, CampaignConfigForm, CampaignSettingsPage(), ConfigPage(), formatTimestamp(), BetaPortalPage(), PageState (+69 more)

### Community 1 - "Game Constants & Discovery Definitions"
Cohesion: 0.02
Nodes (94): DiscoveryType, ACHIEVEMENT_GENERIC, ALTITUDE_BOOSTER, AREA_ATMOSPHERE, AREA_BEYOND, AREA_CHRONO_RIFT, AREA_CLOUDS, AREA_CONSTRUCT (+86 more)

### Community 2 - "Module Registry & Implementation"
Cohesion: 0.06
Nodes (24): ArtifactLocatorModule, AutoRepairDroneModule, BurstThrustersModule, CoolingMatrixModule, EmergencyBeaconModule, EmergencyShieldModule, FastRechargeModule, HeatSinkModule (+16 more)

### Community 4 - "Artifact Management & Set Bonuses"
Cohesion: 0.05
Nodes (18): ArtifactManager, ArtifactRecord, AscensionRank, EXPLORER_I, EXPLORER_II, EXPLORER_III, EXPLORER_IV, EXPLORER_V (+10 more)

### Community 5 - "Website Dependencies & Config"
Cohesion: 0.05
Nodes (40): firebase, firebase-admin, framer-motion, @vercel/analytics, dependencies, firebase, firebase-admin, framer-motion (+32 more)

### Community 6 - "Website Recruitment Components"
Cohesion: 0.12
Nodes (29): pct(), RecruitmentPage(), TABS, CAN_APPROVE, formatDate(), Props, RecruitmentSidePanel(), REJECT_REASONS (+21 more)

### Community 7 - "Audio Management & Sound Engine"
Cohesion: 0.11
Nodes (5): SharedPreferences, SoundManager, Job, MediaPlayer, SoundPool

### Community 8 - "Website Contact & Email UI"
Cohesion: 0.11
Nodes (29): AddManualContact(), Props, ComposeEmailDialog(), Props, OutreachImportCsv(), parseCsv(), Props, isAccountVisible() (+21 more)

### Community 9 - "Website API: Beta & Contacts"
Cohesion: 0.10
Nodes (25): POST(), POST(), GET(), HealthEntry, SystemHealthPage(), GmailCallbackPage(), decodeBounceBody(), DEFAULT_SENDER (+17 more)

### Community 10 - "Encounter Spawning & AI Logic"
Cohesion: 0.09
Nodes (16): EncounterDirector, NotificationLayer(), GameMode, STANDARD, ZEN, Color, NotificationEntry, NotificationManager (+8 more)

### Community 11 - "Website API: Campaign & Admin"
Cohesion: 0.15
Nodes (27): POST(), POST(), GET(), TRANSPARENT_GIF, createWriteBuffer(), flushWrites(), PendingWrite, pushWrite() (+19 more)

### Community 12 - "Website Auth & Admin Pages"
Cohesion: 0.12
Nodes (27): BetaDashboardLayout(), AdminLoginPage(), Admin, AdminsPage(), ROLE_OPTIONS, ROLE_STYLES, AuthContext, AuthContextValue (+19 more)

### Community 13 - "Website Email Templates & Invites"
Cohesion: 0.16
Nodes (18): renderAcknowledgement(), INVITATION_MAP, OUTREACH_MAP, renderTemplate(), stripHtml(), renderInvitation1(), renderInvitation2(), renderInvitation3() (+10 more)

### Community 14 - "Core Game Engine & State"
Cohesion: 0.15
Nodes (7): GameEngine, Color, Platform, PowerUp, FloatingText, FlyingScore, Particle

### Community 15 - "Community 15"
Cohesion: 0.06
Nodes (32): dependencies, next, react, react-dom, devDependencies, eslint, eslint-config-next, tailwindcss (+24 more)

### Community 16 - "Community 16"
Cohesion: 0.15
Nodes (26): DEFAULT_SENDER, POST(), POST(), activateScheduledCampaigns(), checkApplicant(), DEFAULT_SENDER, injectCampaignId(), processAllCampaigns() (+18 more)

### Community 17 - "Community 17"
Cohesion: 0.12
Nodes (3): Mission, SharedPreferences, PlayerAnalyticsManager

### Community 18 - "Community 18"
Cohesion: 0.13
Nodes (10): BitmapParallaxLayer, HazeParallaxLayer, androidx, DrawScope, Offset, ParallaxLayer, ParallaxManager, RepeatingParallaxLayer (+2 more)

### Community 19 - "Community 19"
Cohesion: 0.07
Nodes (28): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+20 more)

### Community 20 - "Community 20"
Cohesion: 0.11
Nodes (24): deleteAdmin(), fetchAdminByEmail(), fetchAllAdmins(), updateAdminRole(), createCampaign(), deleteCampaign(), duplicateCampaign(), getCampaign() (+16 more)

### Community 21 - "Community 21"
Cohesion: 0.07
Nodes (28): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+20 more)

### Community 22 - "Community 22"
Cohesion: 0.08
Nodes (16): CloudSkimmerRenderer, android, DrawScope, GravityShearRenderer, android, DrawScope, android, DrawScope (+8 more)

### Community 23 - "Community 23"
Cohesion: 0.18
Nodes (18): POST(), POST(), POST(), POST(), DELETE(), GET(), PUT(), GET() (+10 more)

### Community 24 - "Community 24"
Cohesion: 0.10
Nodes (22): drawBossHealthBar(), drawFlyingRewards(), drawGround(), drawImpactFlash(), drawLandingEffects(), drawParticles(), drawPowerUps(), drawProjectiles() (+14 more)

### Community 25 - "Community 25"
Cohesion: 0.13
Nodes (21): POST(), CampaignsPage(), RANDOM_NAMES, randomCampaignName(), STATUS_STYLES, addContactToCampaignAdmin(), DEFAULT_CAMPAIGN_CONFIG, incrementInviteCountAdmin() (+13 more)

### Community 26 - "Community 26"
Cohesion: 0.10
Nodes (19): AmbientManager, AmbientObject, AmbientType, AIRCRAFT, ANCIENT_STRUCTURE, ANOMALY, ASTEROID, BALLOON (+11 more)

### Community 27 - "Community 27"
Cohesion: 0.08
Nodes (24): GameState, ABOUT, ARCHIVE, ASCENSION_PROTOCOL, CONTINUE_READY, EXPEDITION_REWARDS, GAMEOVER, HANGAR (+16 more)

### Community 28 - "Community 28"
Cohesion: 0.13
Nodes (8): Platform, Player, androidx, Color, Platform, PowerUp, Module, SurvivalManager

### Community 29 - "Community 29"
Cohesion: 0.38
Nodes (6): android, Color, DrawScope, Platform, PlatformRenderer, Size

### Community 30 - "Community 30"
Cohesion: 0.12
Nodes (21): CampaignInfoModal(), Props, effectiveStatus(), EVENT_COLORS, EVENT_DOTS, EVENT_LABELS, formatDate(), formatDateTime() (+13 more)

### Community 31 - "Community 31"
Cohesion: 0.10
Nodes (3): FirebaseGameAnalytics, GameAnalytics, Mission

### Community 32 - "Community 32"
Cohesion: 0.13
Nodes (20): HapticManager, HapticType, EXPLOSION, IMPACT_HEAVY, IMPACT_LIGHT, IMPACT_MEDIUM, SUCCESS, TICK (+12 more)

### Community 33 - "Community 33"
Cohesion: 0.12
Nodes (13): AltitudeSidebar(), bosses, BossShowcase(), discoveries, DiscoveryArchive(), GameplayExplained(), HeroSection(), MissionControl() (+5 more)

### Community 34 - "Community 34"
Cohesion: 0.21
Nodes (19): EmailPage(), isAccountVisible(), Props, SendEmailDialog(), sendEmail(), getAllowedAdminsConfig(), createCustomTemplate(), deleteCustomTemplate() (+11 more)

### Community 35 - "Community 35"
Cohesion: 0.18
Nodes (6): GitHubIcon(), GooglePlayIcon(), ItchIoIcon(), Props, DOWNLOADS, SOCIAL_LINKS

### Community 36 - "Community 36"
Cohesion: 0.14
Nodes (19): CodexCard(), CompactToggle(), DevCategory, PLATFORMS, POWERUPS, THREATS, DevMenuContent(), DropdownField() (+11 more)

### Community 37 - "Community 37"
Cohesion: 0.12
Nodes (11): MultiplayerManager, GlobalBroadcast, MultiplayerRoom, PlayerMultiplayerState, RoomStatus, ACTIVE, ENDED, LOBBY (+3 more)

### Community 38 - "Community 38"
Cohesion: 0.12
Nodes (11): ZONES, BOSSES, ENEMIES, HAZARDS, ROCKETS, FlyingRocketProps, rocketBodyColor(), RocketSVG() (+3 more)

### Community 39 - "Community 39"
Cohesion: 0.18
Nodes (18): GaugeBar(), Color, Dp, Modifier, HudContext, AchievementDeck(), AltitudeDisplay(), ComboDisplay() (+10 more)

### Community 41 - "Community 41"
Cohesion: 0.10
Nodes (18): LogicOp, AND, OR, ModuleRarity, COMMON, EPIC, LEGENDARY, RARE (+10 more)

### Community 42 - "Community 42"
Cohesion: 0.16
Nodes (16): GET(), POST(), ActivityTimeline(), EVENT_CONFIG, formatTimestamp(), Props, ERROR_EVENT_TYPES, fetchCampaignErrors() (+8 more)

### Community 43 - "Community 43"
Cohesion: 0.14
Nodes (12): ActiveThreat, IntArray, Platform, PowerUp, android, DrawScope, ScoutDroneRenderer, PowerUp (+4 more)

### Community 44 - "Community 44"
Cohesion: 0.12
Nodes (15): AltitudeManager, AltitudeZone, ANCIENT_CONSTRUCT, CHRONO_RIFT, CLOUD_LAYER, DEEP_SPACE, EARTH, ORBIT (+7 more)

### Community 45 - "Community 45"
Cohesion: 0.17
Nodes (14): LeaderboardEntry, LeaderboardManager, GlobalTerminalContent(), Activity, Modifier, LeaderboardScreen(), LocalTelemetryContent(), MpStatItem() (+6 more)

### Community 46 - "Community 46"
Cohesion: 0.12
Nodes (15): BossArrivalOverlay(), Achievement, BossArrivalEvent, ChassisVariant, Discovery, EngineTrail, EngineTrailRegistry, Color (+7 more)

### Community 47 - "Community 47"
Cohesion: 0.13
Nodes (13): ACTION_COLORS, ActivityTab(), CampaignWorkspacePage(), DONUT_COLORS, DONUT_LABELS, ErrorsTab(), OverviewTab(), safeDate() (+5 more)

### Community 48 - "Community 48"
Cohesion: 0.13
Nodes (12): autoRepeat(), BOSS_DATA, BossEncounter(), BossEncounterProps, ALL_THREATS, Colors, PlatformTypes, RocketConfigs (+4 more)

### Community 49 - "Community 49"
Cohesion: 0.15
Nodes (10): PlatformSVG(), PlatformSVGProps, PLATFORMS, COORDINATE_LINES, INTRO_LINES, PacketData, PACKETS, DataPacket() (+2 more)

### Community 50 - "Community 50"
Cohesion: 0.15
Nodes (11): MoonGlow(), BetaTagline(), FooterSection(), HeroSignal(), Props, MissionLog(), MysteryTransmission(), PlayStoreModal() (+3 more)

### Community 51 - "Community 51"
Cohesion: 0.15
Nodes (3): ThreatDefinition, ThreatRegistry, ThreatSpawnRules

### Community 52 - "Community 52"
Cohesion: 0.20
Nodes (17): categoryColor(), categoryIcon(), categoryIconRes(), CosmeticsTab(), formatRequirement(), HangarScreen(), Color, SharedPreferences (+9 more)

### Community 53 - "Community 53"
Cohesion: 0.11
Nodes (18): ObjectiveType, ARTIFACTS_COLLECTED, BOSSES_DEFEATED, CODEX_UNLOCKED, COMBO_MAINTAIN_TIME, CONSECUTIVE_WINS, DASHES_PER_RUN, FUEL_PICKUPS_COLLECTED (+10 more)

### Community 54 - "Community 54"
Cohesion: 0.18
Nodes (12): metadata, BetaAccordion(), Section, SECTIONS, BetaRegistrationForm(), validateEmail(), validatePhone(), matchRegistration() (+4 more)

### Community 55 - "Community 55"
Cohesion: 0.16
Nodes (12): formatType(), GameplayCards(), visualMap, BETA, ENTITY_DESCRIPTIONS, EntitySpec, FOOTER, GAME_CATEGORIES (+4 more)

### Community 56 - "Community 56"
Cohesion: 0.20
Nodes (5): com, SharedPreferences, LoginManager, GoogleSignInAccount, Intent

### Community 57 - "Community 57"
Cohesion: 0.12
Nodes (17): MissionCategory, ALTITUDE_CLIMBER, BOOST_CHAMPION, BOSS_SLAYER, COLLECTOR, COMBO_PRO, COMBO_STREAK, DISCOVERY_HUNTER (+9 more)

### Community 58 - "Community 58"
Cohesion: 0.12
Nodes (16): Platform, PlatformType, BOOST, BREAKABLE, CONVEYOR, COOLING, FLUX, FUEL (+8 more)

### Community 59 - "Community 59"
Cohesion: 0.31
Nodes (6): android, Color, DrawScope, Offset, RocketRenderer, UnlockOverlay()

### Community 60 - "Community 60"
Cohesion: 0.13
Nodes (10): ProjectileOwner, PLAYER, THREAT, ProjectileType, BEAM, BOLT, MISSILE, WAVE (+2 more)

### Community 61 - "Community 61"
Cohesion: 0.19
Nodes (12): GlobalAdBanner(), ArchiveCard(), ArchiveScreen(), ArtifactSetCard(), CatDef, filterEntries(), Color, SharedPreferences (+4 more)

### Community 62 - "Community 62"
Cohesion: 0.19
Nodes (7): AltitudeBoost, Artifact, ComboManager, ComboReward, ComboTier, Fuel, PowerUp

### Community 63 - "Community 63"
Cohesion: 0.19
Nodes (10): ArtifactLoreOverlay(), FloatingTextsLayer(), GamePlayScreen(), HeatEdgeGlow(), HUDLayer(), Modifier, ZenMusicSelector(), ZoneTransitionOverlay() (+2 more)

### Community 64 - "Community 64"
Cohesion: 0.20
Nodes (10): AscensionOverlay(), CreditRow(), LoreSection(), ContinueReadyOverlay(), HelpOverlay(), JumpDroidApp(), LobbyView(), MultiplayerScreen() (+2 more)

### Community 65 - "Community 65"
Cohesion: 0.20
Nodes (5): DiscoveryEvent, DiscoveryManager, Generic, Zone, CodexQuickAccess()

### Community 66 - "Community 66"
Cohesion: 0.21
Nodes (7): ExpeditionRewardsOverlay(), Color, Modifier, RewardCardLarge(), SessionSummary(), SummaryStat(), GameStats

### Community 68 - "Community 68"
Cohesion: 0.29
Nodes (4): android, Color, DrawScope, ZoneBackgroundRenderer

### Community 69 - "Community 69"
Cohesion: 0.31
Nodes (12): DELETE(), GET(), campaignError(), campaignLog(), campaignSeparator(), campaignWarn(), clearCampaignLogs(), getLogPath() (+4 more)

### Community 70 - "Community 70"
Cohesion: 0.15
Nodes (12): PowerUpType, ALTITUDE_BOOSTER, ARTIFACT, EFFICIENCY_MODULE, FUEL_TANK, HEAT_SINK, HULL_REPAIR, KINETIC_BATTERY (+4 more)

### Community 71 - "Community 71"
Cohesion: 0.26
Nodes (5): SectionHeader(), SectionHeaderProps, SectionWrapper(), SectionWrapperProps, FEATURES

### Community 72 - "Community 72"
Cohesion: 0.15
Nodes (11): backgrounds, ChronoRiftBackground, CloudLayerBackground, DeepSpaceBackground, EarthBackground, FoundryBackground, OrbitBackground, UpperAtmosphereBackground (+3 more)

### Community 73 - "Community 73"
Cohesion: 0.24
Nodes (10): AscensionInsignia(), createPolygonPath(), createStarPath(), Dp, Modifier, CurrencyBadge(), FlyingUiReward, GameOverOverlay() (+2 more)

### Community 74 - "Community 74"
Cohesion: 0.23
Nodes (5): Activity, SharedPreferences, PurchaseManager, BillingClient, Purchase

### Community 75 - "Community 75"
Cohesion: 0.25
Nodes (9): GlitchText(), Modifier, HiddenSignalsCard(), Color, Mission, MissionScreen(), SummaryItem(), TimelineNode() (+1 more)

### Community 76 - "Community 76"
Cohesion: 0.24
Nodes (4): Module, LoadoutManager, moduleId, slotIndex

### Community 77 - "Community 77"
Cohesion: 0.25
Nodes (3): LoreLog, Module, UnlockService

### Community 79 - "Community 79"
Cohesion: 0.18
Nodes (5): RocketType, BALANCED, EXPERIMENTAL, SCOUT, TANK

### Community 80 - "Community 80"
Cohesion: 0.18
Nodes (11): Contribution Guidelines, System Architecture, Project Changelog, Release History, Threat Master Table, EncounterDirector, GameEngine, Jump Droid Project Overview (+3 more)

### Community 81 - "Community 81"
Cohesion: 0.36
Nodes (4): GameSimulator(), Particle, Platform, SoundSynth

### Community 82 - "Community 82"
Cohesion: 0.27
Nodes (8): AboutScreen(), ProtocolCard(), TechRow(), Color, Modifier, Star, StarfieldBackground(), ClosedFloatingPointRange

### Community 83 - "Community 83"
Cohesion: 0.33
Nodes (9): drawFallbackShape(), drawGlitchEffect(), drawPlatformPreview(), drawPowerUpPreview(), drawThreatPreview(), EntityPreview(), Color, Modifier (+1 more)

### Community 84 - "Community 84"
Cohesion: 0.33
Nodes (6): ChronoRiftBackground(), FoundryBackground(), ZoneBackground(), ZoneBackgroundProps, ZONES, useParallax()

### Community 85 - "Community 85"
Cohesion: 0.22
Nodes (8): ArtifactBonus, ArtifactSet, FuelRegen, GlobalEfficiency, HeatCooldown, HullBoost, ShieldRegen, ThrustBoost

### Community 86 - "Community 86"
Cohesion: 0.22
Nodes (8): Boss, BossBehavior, ICE_CONVERTER, ITEM_STEALER, PLATFORM_CONSUMER, PROJECTILE_SHOOTER, VOID_SERPENT, WIND_MAKER

### Community 87 - "Community 87"
Cohesion: 0.22
Nodes (6): CeremonyStage, GLOW, NONE, REPLACING, Mission, MissionUnlockCondition

### Community 88 - "Community 88"
Cohesion: 0.31
Nodes (3): Mission, MissionRegistry, MissionTrack

### Community 89 - "Community 89"
Cohesion: 0.33
Nodes (8): AudioSlider(), BenefitItem(), EliteBenefitsDialog(), Color, Context, SharedPreferences, SettingsScreen(), showTestNotification()

### Community 90 - "Community 90"
Cohesion: 0.31
Nodes (5): Account, EmailAccountsPage(), getAuthUrl(), getClientId(), SCOPES

### Community 91 - "Community 91"
Cohesion: 0.28
Nodes (6): CAMPAIGN_CARDS, cntIn(), getCd(), GLOBAL_CARDS, OutreachDashboardCards(), Props

### Community 92 - "Community 92"
Cohesion: 0.29
Nodes (5): findActivity(), Activity, com, Context, RewardedAdHelper

### Community 93 - "Community 93"
Cohesion: 0.29
Nodes (4): BetaRegistrationDialog(), MainActivity, Bundle, ComponentActivity

### Community 94 - "Community 94"
Cohesion: 0.43
Nodes (3): Platform, PowerUp, PowerUpManager

### Community 95 - "Community 95"
Cohesion: 0.46
Nodes (7): drawPentagon(), Color, Modifier, PentagonChart(), RocketStats, StatLegend(), typeColor()

### Community 96 - "Community 96"
Cohesion: 0.29
Nodes (8): Extra Logo 01, The Ascension Program, Celestial Target Portal, Stylized Rocket Ship, Extra Logo 02, The Ascension Program, Concentric Ring Portal, Stylized Rocket Ship

### Community 97 - "Community 97"
Cohesion: 0.36
Nodes (7): BOSS_ENCOUNTERS, Encounter, ENCOUNTERS, EncounterSystem(), getLocalProgress(), getScale(), getXPosition()

### Community 99 - "Community 99"
Cohesion: 0.29
Nodes (3): Offset, Offset, PlayerInputProcessor

### Community 100 - "Community 100"
Cohesion: 0.43
Nodes (3): InputBufferManager, Offset, ThrustEvent

### Community 101 - "Community 101"
Cohesion: 0.33
Nodes (3): JumpDroidFirebaseMessagingService, FirebaseMessagingService, RemoteMessage

### Community 102 - "Community 102"
Cohesion: 0.29
Nodes (6): LoreCategory, ANCIENT, EPILOGUE, SIGNAL, SURVIVOR, VOID

### Community 103 - "Community 103"
Cohesion: 0.29
Nodes (7): MissionUnlockType, COLLECT_ARTIFACT, COMPLETE_MISSION, DEFEAT_BOSS, REACH_ALTITUDE, REACH_BIOME, UNLOCK_CODEX_ENTRY

### Community 104 - "Community 104"
Cohesion: 0.29
Nodes (6): MissionType, BOSS, DISCOVERY, EXPLORATION, PLATFORMING, SURVIVAL

### Community 105 - "Community 105"
Cohesion: 0.29
Nodes (6): SpawnPosition, ABOVE_CAMERA, ABOVE_SCREEN, BELOW_RANDOM_X, RANDOM_SCREEN, SIDE_ENTRY

### Community 106 - "Community 106"
Cohesion: 0.29
Nodes (6): ThreatTier, TIER_1, TIER_2, TIER_3, TIER_4, TIER_5

### Community 107 - "Community 107"
Cohesion: 0.48
Nodes (6): generateParticles(), generateWaveformPath(), lerpColor(), PALETTE, ParticleCanvas(), samplePalette()

### Community 108 - "Community 108"
Cohesion: 0.33
Nodes (3): AssetManager, Context, ImageBitmap

### Community 109 - "Community 109"
Cohesion: 0.33
Nodes (5): BlueprintRegistry, BlueprintType, ENGINE_TRAIL_CYAN, HUD_THEME_AMBER, ROCKET_SKIN_OBSIDIAN

### Community 111 - "Community 111"
Cohesion: 0.33
Nodes (5): ThreatState, ACTIVE, DESTROYED, DORMANT, SPAWNING

### Community 112 - "Community 112"
Cohesion: 0.33
Nodes (5): ThreatType, BOSS, ENEMY, HAZARD, MINI_BOSS

### Community 113 - "Community 113"
Cohesion: 0.33
Nodes (6): Explorer Rocket Class, Heavy Rocket Class, Loadout Customization, Prototype Rocket Class, Rocket Hangar UI, Striker Rocket Class

### Community 114 - "Community 114"
Cohesion: 0.40
Nodes (5): EPIC 8.5: Refactor Execution, EPIC 9: Hidden Signals & Dynamic Unlocks, Artifact Set Bonuses, Dynamic Unlock Engine, Hidden Signals

### Community 115 - "Community 115"
Cohesion: 0.40
Nodes (3): ArchitectRenderer, android, DrawScope

### Community 116 - "Community 116"
Cohesion: 0.40
Nodes (3): CommanderRenderer, android, DrawScope

### Community 117 - "Community 117"
Cohesion: 0.40
Nodes (3): CorruptedHullRenderer, android, DrawScope

### Community 118 - "Community 118"
Cohesion: 0.40
Nodes (3): CrosswindRenderer, android, DrawScope

### Community 119 - "Community 119"
Cohesion: 0.40
Nodes (3): CryoMistRenderer, android, DrawScope

### Community 120 - "Community 120"
Cohesion: 0.40
Nodes (3): DebrisRenderer, android, DrawScope

### Community 121 - "Community 121"
Cohesion: 0.40
Nodes (3): EmpRenderer, android, DrawScope

### Community 122 - "Community 122"
Cohesion: 0.40
Nodes (3): EntropyCoreRenderer, android, DrawScope

### Community 124 - "Community 124"
Cohesion: 0.40
Nodes (3): FluxRenderer, DrawScope, Platform

### Community 125 - "Community 125"
Cohesion: 0.40
Nodes (3): ForgerRenderer, android, DrawScope

### Community 126 - "Community 126"
Cohesion: 0.40
Nodes (3): GatekeeperRenderer, android, DrawScope

### Community 127 - "Community 127"
Cohesion: 0.40
Nodes (3): GravitonRenderer, DrawScope, Platform

### Community 128 - "Community 128"
Cohesion: 0.40
Nodes (3): GravityAnchorRenderer, android, DrawScope

### Community 129 - "Community 129"
Cohesion: 0.40
Nodes (3): GravityRamRenderer, android, DrawScope

### Community 130 - "Community 130"
Cohesion: 0.40
Nodes (3): GravityRenderer, android, DrawScope

### Community 131 - "Community 131"
Cohesion: 0.40
Nodes (3): GustRenderer, android, DrawScope

### Community 132 - "Community 132"
Cohesion: 0.40
Nodes (3): HeatBatRenderer, android, DrawScope

### Community 133 - "Community 133"
Cohesion: 0.40
Nodes (3): android, DrawScope, LightningRenderer

### Community 134 - "Community 134"
Cohesion: 0.40
Nodes (3): android, DrawScope, MirrorShardsRenderer

### Community 135 - "Community 135"
Cohesion: 0.40
Nodes (5): MissionTier, TIER_1, TIER_2, TIER_3, TIER_4

### Community 136 - "Community 136"
Cohesion: 0.40
Nodes (3): android, DrawScope, OrbitalSentryRenderer

### Community 137 - "Community 137"
Cohesion: 0.40
Nodes (3): android, DrawScope, PhaseWraithRenderer

### Community 139 - "Community 139"
Cohesion: 0.40
Nodes (3): android, DrawScope, RadiationRenderer

### Community 140 - "Community 140"
Cohesion: 0.40
Nodes (3): android, DrawScope, SignalRenderer

### Community 141 - "Community 141"
Cohesion: 0.40
Nodes (3): android, DrawScope, SingularityRenderer

### Community 142 - "Community 142"
Cohesion: 0.40
Nodes (3): android, DrawScope, SolarFlareRenderer

### Community 143 - "Community 143"
Cohesion: 0.40
Nodes (3): android, DrawScope, StalkerRenderer

### Community 144 - "Community 144"
Cohesion: 0.40
Nodes (3): android, DrawScope, StarEaterRenderer

### Community 145 - "Community 145"
Cohesion: 0.40
Nodes (3): android, DrawScope, StormRenderer

### Community 146 - "Community 146"
Cohesion: 0.40
Nodes (3): android, DrawScope, SwarmBotsRenderer

### Community 147 - "Community 147"
Cohesion: 0.40
Nodes (3): android, DrawScope, ThermalHiveRenderer

### Community 148 - "Community 148"
Cohesion: 0.40
Nodes (3): android, DrawScope, ThermalRenderer

### Community 149 - "Community 149"
Cohesion: 0.60
Nodes (4): TitleDrone, TitleScreen(), TitleSilhouette, TitleStar

### Community 150 - "Community 150"
Cohesion: 0.40
Nodes (3): android, DrawScope, TurbulenceRenderer

### Community 151 - "Community 151"
Cohesion: 0.40
Nodes (3): android, DrawScope, VoidAnomalyRenderer

### Community 152 - "Community 152"
Cohesion: 0.40
Nodes (3): android, DrawScope, VoidHarvesterRenderer

### Community 153 - "Community 153"
Cohesion: 0.40
Nodes (3): android, DrawScope, VoidWhaleRenderer

### Community 154 - "Community 154"
Cohesion: 0.40
Nodes (3): android, DrawScope, VoidWraithRenderer

### Community 155 - "Community 155"
Cohesion: 0.40
Nodes (5): Game Icon Asset, Launcher Icon (hdpi), Launcher Icon (xxxhdpi), Jump Droid Visual Identity, Website App Icon

### Community 156 - "Community 156"
Cohesion: 0.40
Nodes (5): Ever-Changing Orbital Battlefield, Precision Flight, Four Survival Systems, The Ascension Program, Roguelite Progression

### Community 157 - "Community 157"
Cohesion: 0.40
Nodes (3): inter, jetbrainsMono, metadata

### Community 158 - "Community 158"
Cohesion: 0.40
Nodes (3): inter, jetbrainsMono, metadata

### Community 159 - "Community 159"
Cohesion: 0.50
Nodes (4): cleanup(), DELETE_COLLECTIONS, deleteAllDocs(), KEEP_COLLECTIONS

### Community 162 - "Community 162"
Cohesion: 0.83
Nodes (3): categoryIconRes(), formatRequirement(), LoadoutScreen()

### Community 163 - "Community 163"
Cohesion: 0.50
Nodes (4): The Sentinel (Autonomous Defense Platform), Thermal Hive (Thermal Summoner Collective), Data Archive & Lore System, Screenshot: Discover A Lost Civilization

### Community 164 - "Community 164"
Cohesion: 0.50
Nodes (4): ArtifactManager.kt, ProgressionManager.kt, EPIC 12 Phase 8: Technical Foundation Implementation Plan, Robust Reset Protocols Walkthrough

### Community 165 - "Community 165"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 166 - "Community 166"
Cohesion: 0.50
Nodes (4): Extra Logo 03, The Ascension Program, Red Celestial Sphere, Stylized Rocket Ship

### Community 167 - "Community 167"
Cohesion: 0.50
Nodes (4): Hero Banner, The Ascension Program, Stylized Rocket Ship, Celestial Target Ring

### Community 168 - "Community 168"
Cohesion: 0.67
Nodes (3): CATEGORIES, DiscoveryArchive(), LORE_LOGS

### Community 171 - "Community 171"
Cohesion: 0.50
Nodes (4): File Icon, Globe Icon, Website UI Icons, Window Icon

### Community 172 - "Community 172"
Cohesion: 0.50
Nodes (4): OG Branding Image, Detailed Droid Rocket, Twitter Branding Image, Detailed Droid Rocket

### Community 173 - "Community 173"
Cohesion: 0.67
Nodes (3): Skill: Code Review and Quality, Skill: Code Simplification, Skill: Doubt-Driven Development

### Community 176 - "Community 176"
Cohesion: 0.67
Nodes (3): firestore.rules, LoginManager.kt, EPIC 13: Production Readiness & Store Listing Plan

### Community 177 - "Community 177"
Cohesion: 0.67
Nodes (3): GameEngine.kt, RemoteConfigManager.kt, Remote Announcement System Implementation Plan

### Community 178 - "Community 178"
Cohesion: 0.67
Nodes (3): Expedition & Persistent Progression Loop, Screenshot: Every Expedition Makes You Stronger, Communication Lost (Results Screen UI)

### Community 179 - "Community 179"
Cohesion: 0.67
Nodes (3): Analytics Reference, Documentation Inventory, Production Release Checklist

### Community 180 - "Community 180"
Cohesion: 0.67
Nodes (3): Entropy Core, Void Engine, The Void

### Community 187 - "Community 187"
Cohesion: 1.00
Nodes (3): Website Branding, Next.js Logo, Vercel Logo

## Knowledge Gaps
- **666 isolated node(s):** `AdConfig`, `EARTH`, `CLOUD_LAYER`, `UPPER_ATMOSPHERE`, `ORBIT` (+661 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **84 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Player` connect `Community 28` to `Community 128`, `Community 129`, `Community 130`, `Community 131`, `Community 132`, `Community 133`, `Community 134`, `Module Registry & Implementation`, `Community 136`, `Community 137`, `Player Progression & Persistence`, `Artifact Management & Set Bonuses`, `Community 139`, `Community 140`, `Community 141`, `Community 142`, `Community 143`, `Community 144`, `Community 145`, `Community 146`, `Community 147`, `Community 148`, `Community 22`, `Community 150`, `Community 24`, `Community 151`, `Community 152`, `Community 153`, `Community 154`, `Community 40`, `Community 43`, `Community 46`, `Encounter Spawning & AI Logic`, `Community 52`, `Community 59`, `Community 60`, `Community 66`, `Community 75`, `Community 77`, `Community 83`, `Community 94`, `Community 99`, `Community 115`, `Community 116`, `Community 117`, `Community 118`, `Community 119`, `Community 120`, `Community 121`, `Community 122`, `Community 125`, `Community 126`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `AltitudeZone` connect `Community 44` to `Player Progression & Persistence`, `Artifact Management & Set Bonuses`, `Audio Management & Sound Engine`, `Encounter Spawning & AI Logic`, `Core Game Engine & State`, `Community 17`, `Community 18`, `Community 24`, `Community 26`, `Community 29`, `Community 31`, `Community 36`, `Community 39`, `Community 51`, `Community 63`, `Community 65`, `Community 68`, `Community 124`, `Community 127`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Why does `ProgressionManager` connect `Player Progression & Persistence` to `Community 32`, `Community 66`, `Community 162`, `Artifact Management & Set Bonuses`, `Community 36`, `Community 40`, `Community 73`, `Community 41`, `Community 76`, `Community 45`, `Community 109`, `Community 77`, `Community 52`, `Community 85`, `Community 61`?**
  _High betweenness centrality (0.046) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Player` (e.g. with `drawThreatPreview()` and `UnlockOverlay()`) actually correct?**
  _`Player` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `AdConfig`, `EARTH`, `CLOUD_LAYER` to the rest of the system?**
  _666 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Website Beta Portal & Dashboard` be split into smaller, more focused modules?**
  _Cohesion score 0.060228452751817235 - nodes in this community are weakly interconnected._
- **Should `Game Constants & Discovery Definitions` be split into smaller, more focused modules?**
  _Cohesion score 0.02127659574468085 - nodes in this community are weakly interconnected._