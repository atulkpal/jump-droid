package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.border
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.sin

@Composable
fun SettingsScreen(
    sharedPrefs: SharedPreferences,
    soundManager: SoundManager? = null,
    hapticManager: HapticManager? = null,
    purchaseManager: PurchaseManager? = null,
    onWipeData: () -> Unit,
    onFactoryReset: () -> Unit = onWipeData,
    onReturn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SettingsTransition")
    val pulseAlpha by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "PulseAlpha")
    val borderPulse by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "BorderPulse")

    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }

    val context = LocalContext.current
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 40, alphaRange = 0.15f..0.55f, starColor = SciFiCyan)
            Canvas(Modifier.fillMaxSize()) {
                val ft = frameTime.value / 1000f
                val w = size.width
                val h = size.height

                drawCircle(SciFiCyan.copy(alpha = 0.03f), radius = 70f, center = Offset(w * 0.8f, h * 0.3f))

                // Pulsing audio wave
                val waveBase = h * 0.48f
                val waveAmp = 4f + sin(ft * 3f) * 2f
                for (i in 0..19) {
                    val barX = w * 0.35f + (i * (w * 0.3f / 19f))
                    val barH = waveAmp * (0.3f + sin(ft * 4f + i * 0.8f) * 0.7f)
                    drawRect(
                        SciFiCyan.copy(alpha = 0.15f + barH / 20f),
                        topLeft = Offset(barX, waveBase - barH),
                        size = androidx.compose.ui.geometry.Size(w * 0.012f, barH * 2f)
                    )
                }
            }

            Column(Modifier.padding(32.dp).safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_station_sys),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "SYSTEM SETTINGS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = Shadow(SciFiCyan.copy(alpha = 0.4f), blurRadius = 12f)
                        ),
                        color = SciFiCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(32.dp))
                Text("SOUND EFFECTS", color = SciFiWhite.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 10.sp)
                Spacer(Modifier.height(8.dp))
                AudioSlider(
                    value = soundManager?.sfxVolume ?: 0.7f,
                    onValueChange = { 
                        soundManager?.sfxVolume = it
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                    },
                    accent = SciFiCyan,
                    contentDescription = "SFX Volume"
                )
                Spacer(Modifier.height(16.dp))
                Text("MUSIC", color = SciFiWhite.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 10.sp)
                Spacer(Modifier.height(8.dp))
                AudioSlider(
                    value = soundManager?.musicVolume ?: 0.5f,
                    onValueChange = { 
                        soundManager?.musicVolume = it
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                    },
                    accent = SciFiGold,
                    contentDescription = "Music Volume"
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(0.6f), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { 
                            if (soundManager != null) {
                                soundManager.isMuted = !soundManager.isMuted
                                soundManager.playSfx("sfx_ui_click")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (soundManager?.isMuted == true) SciFiRed.copy(alpha = 0.3f) else SciFiCyan.copy(alpha = 0.2f),
                            contentColor = if (soundManager?.isMuted == true) SciFiRed else SciFiCyan
                        ),
                        modifier = Modifier.height(36.dp).weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (soundManager?.isMuted == true) "MUTED" else "MUTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    val hapticEnabled = sharedPrefs.getBoolean("haptic_enabled", true)
                    Button(
                        onClick = { 
                            val newState = !hapticEnabled
                            sharedPrefs.edit { putBoolean("haptic_enabled", newState) }
                            soundManager?.playSfx("sfx_ui_click")
                            if (newState) {
                                hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                            } else {
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!hapticEnabled) SciFiRed.copy(alpha = 0.3f) else SciFiCyan.copy(alpha = 0.2f),
                            contentColor = if (!hapticEnabled) SciFiRed else SciFiCyan
                        ),
                        modifier = Modifier.height(36.dp).weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (!hapticEnabled) "HAPTIC OFF" else "HAPTIC ON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                val isPremium = purchaseManager?.isPremiumUser ?: sharedPrefs.getBoolean("premium_user", false)
                var showDebugPurchaseDialog by remember { mutableStateOf(false) }
                var showStoreDialog by remember { mutableStateOf(false) }
                var showBenefitsDialog by remember { mutableStateOf(false) }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            soundManager?.playSfx("sfx_ui_click")
                            hapticManager?.vibrate(HapticManager.HapticType.TICK)
                            if (!isPremium) {
                                purchaseManager?.launchPurchaseFlow(context as android.app.Activity) {
                                    if (BuildConfig.DEBUG) showDebugPurchaseDialog = true else showStoreDialog = true
                                }
                            }
                        },
                        enabled = !isPremium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPremium) SciFiGreen.copy(alpha = 0.2f) else SciFiGold.copy(alpha = 0.2f),
                            contentColor = if (isPremium) SciFiGreen else SciFiGold,
                            disabledContainerColor = SciFiGreen.copy(alpha = 0.15f),
                            disabledContentColor = SciFiGreen.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPremium) SciFiGreen.copy(alpha = 0.3f) else SciFiGold.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPremium) "ADS REMOVED ✓" else "UPGRADE: REMOVE ADS (\$1.99)", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { 
                            soundManager?.playSfx("sfx_ui_click")
                            hapticManager?.vibrate(HapticManager.HapticType.TICK)
                            showBenefitsDialog = true 
                        },
                        modifier = Modifier.size(40.dp).background(SciFiSurface, RoundedCornerShape(4.dp)).border(1.dp, SciFiCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    ) {
                        Text("?", color = SciFiCyan, fontWeight = FontWeight.Bold)
                    }
                }

                if (showBenefitsDialog) {
                    EliteBenefitsDialog(
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        onDismiss = { showBenefitsDialog = false }
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (showDebugPurchaseDialog) {
                    AlertDialog(
                        onDismissRequest = { showDebugPurchaseDialog = false },
                        title = { Text("Purchase Remove Ads?", color = SciFiWhite, fontWeight = FontWeight.Bold) },
                        text = { Text("Remove all ads for a one-time payment of \$1.99.", color = SciFiWhite.copy(alpha = 0.8f)) },
                        confirmButton = {
                            TextButton(onClick = {
                                soundManager?.playSfx("sfx_ui_confirm")
                                hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                                purchaseManager?.confirmPurchase()
                                showDebugPurchaseDialog = false
                            }) { Text("PURCHASE", color = SciFiGold, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                soundManager?.playSfx("sfx_ui_back")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                showDebugPurchaseDialog = false 
                            }) { Text("CANCEL", color = SciFiWhite.copy(alpha = 0.5f)) }
                        },
                        containerColor = SciFiSurface,
                        titleContentColor = SciFiWhite,
                        textContentColor = SciFiWhite.copy(alpha = 0.8f)
                    )
                }
                if (showStoreDialog) {
                    AlertDialog(
                        onDismissRequest = { showStoreDialog = false },
                        title = { Text("PLAY STORE REQUIRED", color = SciFiGold, fontWeight = FontWeight.Bold) },
                        text = { Text("Premium purchase is only available through the Google Play Store.\n\nDownload Jump Droid from the Play Store to remove ads.", color = SciFiWhite.copy(alpha = 0.8f)) },
                        confirmButton = {
                            TextButton(onClick = { 
                                soundManager?.playSfx("sfx_ui_back")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                showStoreDialog = false 
                            }) { Text("DISMISS", color = SciFiGold, fontWeight = FontWeight.Bold) }
                        },
                        containerColor = SciFiSurface,
                        titleContentColor = SciFiGold,
                        textContentColor = SciFiWhite.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                var showResetDialog by remember { mutableStateOf(false) }
                var showFactoryResetDialog by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        showResetDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiGold.copy(alpha = 0.15f), contentColor = SciFiGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESET PROGRESS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset Progress?", color = SciFiGold, fontWeight = FontWeight.Bold) },
                        text = { Text("This will clear all game data:\n• Missions & Milestones\n• Discoveries & Lore\n• Cash Balance\n• Zone Progression\n\nYour Premium Purchase will NOT be affected.", color = SciFiWhite.copy(alpha = 0.8f)) },
                        confirmButton = {
                            TextButton(onClick = {
                                soundManager?.playSfx("sfx_ui_confirm")
                                hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                                showResetDialog = false
                                onWipeData()
                            }) { Text("RESET", color = SciFiGold, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                soundManager?.playSfx("sfx_ui_back")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                showResetDialog = false 
                            }) { Text("CANCEL", color = SciFiWhite.copy(alpha = 0.5f)) }
                        },
                        containerColor = SciFiSurface,
                        titleContentColor = SciFiGold,
                        textContentColor = SciFiWhite.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        showFactoryResetDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiRed.copy(alpha = 0.15f), contentColor = SciFiRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FACTORY RESET (incl. purchases)", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 11.sp)
                }
                if (showFactoryResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showFactoryResetDialog = false },
                        title = { Text("FACTORY RESET", color = SciFiRed, fontWeight = FontWeight.Bold) },
                        text = { Text("This will clear ALL data including:\n• Premium Purchase (ads will return)\n• All game progress & cash\n• All preferences\n\nThis cannot be undone.", color = SciFiWhite.copy(alpha = 0.8f)) },
                        confirmButton = {
                            TextButton(onClick = {
                                soundManager?.playSfx("sfx_ui_confirm")
                                hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                                showFactoryResetDialog = false
                                onFactoryReset()
                            }) { Text("FACTORY RESET", color = SciFiRed, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                soundManager?.playSfx("sfx_ui_back")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                showFactoryResetDialog = false 
                            }) { Text("CANCEL", color = SciFiWhite.copy(alpha = 0.5f)) }
                        },
                        containerColor = SciFiSurface,
                        titleContentColor = SciFiRed,
                        textContentColor = SciFiWhite.copy(alpha = 0.8f)
                    )
                }
                
                if (BuildConfig.DEBUG) {
                    Spacer(Modifier.height(16.dp))
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            showTestNotification(context)
                        } else {
                            android.widget.Toast.makeText(context, "Notification permission denied", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    Button(
                        onClick = {
                            soundManager?.playSfx("sfx_ui_confirm")
                            hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                    showTestNotification(context)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                showTestNotification(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan.copy(alpha = 0.1f), contentColor = SciFiCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TRIGGER TEST NOTIFICATION", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                GlobalAdBanner()
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_back")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        onReturn()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder.copy(alpha = borderPulse))
                ) {
                    Text("RETURN", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("SYSTEM PREFERENCES // AUDIO // DATA", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp)
            }
        }
    }
}

fun showTestNotification(context: Context) {
    val channelId = "jump_droid_general"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Jump Droid Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Game updates, lore discoveries, and mission alerts"
            enableLights(true)
            lightColor = android.graphics.Color.CYAN
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("JUMP DROID // INCOMING")
        .setContentText("Tactical update received. The Singularity is approaching.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    manager.notify(System.currentTimeMillis().toInt(), notification)
}

@Composable
fun EliteBenefitsDialog(onDismiss: () -> Unit, soundManager: SoundManager? = null, hapticManager: HapticManager? = null) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SciFiSurface,
        titleContentColor = SciFiCyan,
        textContentColor = SciFiWhite.copy(alpha = 0.8f),
        title = { Text("ELITE FLEET BENEFITS", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BenefitItem("AD-FREE COMMAND", "Remove all intermittent and optional ads.", true)
                BenefitItem("ELITE IDENT", "Exclusive Supporter badge on your profile.", true)
                BenefitItem("CLOUD RELAY", "Enhanced cloud sync with offline local caching.", true)
                BenefitItem("PRIORITY ACCESS", "Early testing of experimental engine assets.", true)
                
                HorizontalDivider(color = SciFiWhite.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                
                Text(
                    "One-time purchase supports all future engine development and zone expansions.",
                    fontSize = 10.sp,
                    color = SciFiGold.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                soundManager?.playSfx("sfx_ui_back")
                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                onDismiss()
            }) {
                Text("ACKNOWLEDGED", color = SciFiCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun BenefitItem(title: String, desc: String, isPremium: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            if (isPremium) "★" else "○",
            color = if (isPremium) SciFiGold else SciFiWhite.copy(alpha = 0.3f),
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = SciFiWhite.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun AudioSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    accent: Color = SciFiCyan,
    contentDescription: String? = null
) {
    Row(
        Modifier.fillMaxWidth(0.6f).height(24.dp)
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(10) { i ->
            val step = (i + 1) / 10f
            val isActive = step <= value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isActive) accent.copy(alpha = 0.6f) else Color(0xFF333333),
                        RoundedCornerShape(3.dp)
                    )
                    .clickable { onValueChange(step) }
            )
        }
    }
}
