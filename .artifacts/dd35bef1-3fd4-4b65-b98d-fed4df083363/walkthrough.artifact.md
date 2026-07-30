# Walkthrough - Debug Unlock Controls

I have implemented hidden debug controls in the **Tactical Command Console** to allow instant verification of mode transitions and UI layouts.

## Changes Made

### 1. Data Layer: Forced Unlocks
- **New Debug Methods**: Added `debugUnlockZen()` and `debugUnlockMultiplayer()` to `ProgressionManager.kt`. These methods instantly set the unlock flags to `true` and persist the state to `SharedPreferences`, simulating a successful completion of all requirements.

### 2. UI Layer: Hidden Debug Row
- **Conditional Visibility**: Added a tiny debug row to the top-right of the `ZenCommandConsole` box, visible **only in Debug builds** (`BuildConfig.DEBUG`).
- **Control Interface**:
    - **⚡ (Lightning)**: Instantly unlocks Zen Mode.
    - **🌐 (Globe)**: Instantly unlocks Uplink Protocol (Multiplayer).
- **Subtle Design**: These buttons are rendered at 30% opacity to avoid distracting from the main console aesthetic while still being accessible for development and testing.

## Verification Results

### Manual Verification (Debug)
- **Zen Unlock**: Tapping the lightning icon instantly transitions the console from Green/Amber to the Purple "ZEN PROTOCOL AUTHORIZED" state and surfaces the "DEPLOY ZEN MODE" button.
- **Uplink Unlock**: Tapping the globe icon instantly transitions the console to the Cyan "UPLINK PROTOCOL AUTHORIZED" state and surfaces the "DEPLOY UPLINK" button.
- **Build Integrity**: Confirmed that `gradle_build` succeeds and these controls are properly guarded by the debug build flag.
