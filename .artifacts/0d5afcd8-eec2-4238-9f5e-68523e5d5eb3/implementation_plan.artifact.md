# Implementation Plan - Remote Announcement System

This plan details how to implement a flexible notification system that allows adding announcements via the Firebase Console (Firestore) without rebuilding the app.

## User Review Required

> [!IMPORTANT]
> This system supplements Firebase Cloud Messaging (FCM). While FCM is great for "pushing" to backgrounded users, this system ensures that any active player receives important news (e.g., "New Update Available" or "Weekend Event") via the in-game HUD or a system notification triggered on app launch.

## Proposed Changes

### [app](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app)

#### [MODIFY] [RemoteConfigManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/RemoteConfigManager.kt)
- Update the Firestore document listener to check for an `announcement` object.
- The object will contain `title`, `message`, `priority`, and a unique `id` (or `revision`).
- Implement logic to show this announcement as an in-game notification (via `NotificationManager`) or a system notification if it hasn't been seen before.

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- Pass a callback to `RemoteConfigManager` to handle incoming announcements.
- When an announcement is received, use `notificationManager.showImmediately` to display it to the player.

#### [MODIFY] [JumpDroidFirebaseMessagingService.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/JumpDroidFirebaseMessagingService.kt)
- (Cleanup) Ensure the `PendingIntent` and channel configuration are robust for all Android versions.

## Verification Plan

### Automated Tests
- Build verification using `assembleDebug`.

### Manual Verification
1.  **Direct Push**: Send a test message from Firebase Cloud Messaging console. Verify notification appears.
2.  **Remote Announcement**: Update the `server_config/remote_config` document in Firestore with a new announcement message.
3.  Launch the app and verify the announcement appears in the HUD notification area.
