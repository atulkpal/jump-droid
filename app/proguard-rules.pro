# Jump Droid Production R8 Rules

# Optimization is enabled in build.gradle.kts (minifyEnabled = true)
# These rules ensure critical libraries and reflected classes are preserved.

# Firebase — keep all classes (reflection used internally)
-keep class com.google.firebase.** { *; }

# Google Mobile Ads (AdMob) — keep all classes
-keep class com.google.android.gms.ads.** { *; }

# Google Play Billing Library — keep all classes
-keep class com.android.billingclient.** { *; }

# Google Play Games Services
-keep class com.google.android.gms.games.** { *; }

# BuildConfig — expose for debug/release gating
-keep class com.ashwathai.jump_droid.BuildConfig { *; }

# Jetpack Compose — Keep standard annotations and metadata
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Lifecycle
-keep class androidx.lifecycle.DefaultLifecycleObserver { *; }

# Room — preserve generated implementation classes
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# WorkManager — preserve internal implementation for reflection
-keep class androidx.work.impl.** { *; }
-dontwarn androidx.work.**
