package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Intent
import kotlin.random.Random
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiOrange
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.util.Locale

@Composable
fun MainMenuScreen(
    onLaunch: () -> Unit,
    onLaunchZen: () -> Unit,
    onNavigate: (GameState) -> Unit,
    onExit: () -> Unit,
    highScore: Int = 0,
    onPrestige: () -> Unit = {},
    soundManager: SoundManager? = null,
    hapticManager: HapticManager? = null,
    hasNewEntries: Boolean = false,
    archiveUnreadCount: Int = 0,
    progressionManager: ProgressionManager? = null,
    loginManager: LoginManager? = null,
    onSignIn: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MenuTransition")
    val scanAngle by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart), label = "ScanAngle")
    val borderPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "BorderPulse")
    val titleGlow by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "TitleGlow")
    val accentPulse by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "AccentPulse")
    val rocketBob by infiniteTransition.animateFloat(0f, -8f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "RocketBob")
    val scanRingScale by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(2500, easing = androidx.compose.animation.core.LinearEasing)), label = "ScanRing")

    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }
    val ft = frameTime.value / 1000f

    val shape = RoundedCornerShape(12.dp)
    var navExpanded by remember { mutableStateOf(false) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val analytics = LocalAnalytics.current

    Box(Modifier.fillMaxSize().background(SciFiBackground)) {
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 60, alphaRange = 0.1f..0.4f, speedRange = 0.1f..0.3f)
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 30, alphaRange = 0.2f..0.6f, speedRange = 0.4f..0.8f)
        
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Use stable frame time for deterministic animations inside canvas too
            val cft = frameTime.value / 1000f

            drawCircle(SciFiCyan.copy(alpha = 0.04f), radius = 60f, center = Offset(w * 0.15f + sin(cft) * 8f, h * 0.12f + cos(cft * 0.7f) * 6f))
            drawCircle(SciFiPurple.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.85f + cos(cft * 0.5f) * 10f, h * 0.88f + sin(cft * 0.8f) * 8f))
            drawCircle(SciFiGold.copy(alpha = 0.02f), radius = 50f, center = Offset(w * 0.5f + sin(cft * 0.3f) * 5f, h * 0.5f + cos(cft * 0.4f) * 5f))

            // Drifting Debris
            repeat(15) { i ->
                val seed = i * 42L
                val prng = Random(seed)
                val speed = 10f + prng.nextFloat() * 20f
                val dx = (cft * speed + prng.nextFloat() * w) % (w + 100f) - 50f
                val dy = (cft * speed * 0.5f + prng.nextFloat() * h) % (h + 100f) - 50f
                val size = 0.5f + prng.nextFloat() * 1.5f
                drawCircle(SciFiWhite.copy(alpha = 0.1f), radius = size, center = Offset(dx, dy))
            }

            repeat(12) { i ->
                val px = ((i * 137.5f) % w.toFloat())
                val py = ((i * 89.3f + cft * 20f * (0.5f + (i % 3) * 0.25f)) % h.toFloat())
                drawCircle(SciFiCyan.copy(alpha = 0.05f + sin(cft + i) * 0.03f), radius = 1.5f, center = Offset(px, py))
            }

            val cx = w / 2
            val cy = h * 0.22f + rocketBob
            
            // Scan Rings (Expanding from center)
            repeat(3) { i ->
                val ringProgress = (scanRingScale + i * 0.33f) % 1f
                val ringRad = 40f + ringProgress * 180f
                val ringAlpha = (1f - ringRad / 220f).coerceIn(0f, 0.15f)
                drawCircle(
                    color = SciFiCyan.copy(alpha = ringAlpha),
                    radius = ringRad,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )
            }

            val scanRad = 115f
            drawArc(
                brush = Brush.sweepGradient(listOf(SciFiCyan.copy(alpha = 0.15f), Color.Transparent)),
                startAngle = scanAngle - 30f,
                sweepAngle = 60f,
                useCenter = true,
                topLeft = Offset(cx - scanRad, cy - scanRad),
                size = androidx.compose.ui.geometry.Size(scanRad * 2, scanRad * 2)
            )
            drawCircle(SciFiCyan.copy(alpha = 0.08f), radius = scanRad, center = Offset(cx, cy), style = Stroke(width = 1.2f))
            drawCircle(SciFiCyan.copy(alpha = 0.05f), radius = scanRad * 0.7f, center = Offset(cx, cy), style = Stroke(width = 0.6f))
            drawCircle(SciFiCyan.copy(alpha = 0.03f), radius = scanRad * 0.4f, center = Offset(cx, cy), style = Stroke(width = 0.4f))

            val rocketCenterX = cx
            val rocketBaseY = cy + 70f
            val rocketHeight = 45f
            val rocketWidth = 14f
            val tipY = rocketBaseY - rocketHeight
            val bodyLeft = rocketCenterX - rocketWidth * 0.4f
            val bodyRight = rocketCenterX + rocketWidth * 0.4f
            val finWidth = rocketWidth * 0.6f

            val rocketPath = Path().apply {
                moveTo(rocketCenterX, tipY)
                lineTo(bodyRight, rocketBaseY - rocketHeight * 0.35f)
                lineTo(bodyRight, rocketBaseY - rocketHeight * 0.15f)
                lineTo(rocketCenterX + finWidth, rocketBaseY)
                lineTo(bodyRight, rocketBaseY - rocketHeight * 0.05f)
                lineTo(bodyRight, rocketBaseY)
                lineTo(bodyLeft, rocketBaseY)
                lineTo(bodyLeft, rocketBaseY - rocketHeight * 0.05f)
                lineTo(rocketCenterX - finWidth, rocketBaseY)
                lineTo(bodyLeft, rocketBaseY - rocketHeight * 0.15f)
                lineTo(bodyLeft, rocketBaseY - rocketHeight * 0.35f)
                close()
            }

            drawPath(
                rocketPath,
                color = SciFiWhite.copy(alpha = 0.12f + accentPulse * 0.08f),
                style = Stroke(width = 1.5f)
            )

            val glowRadius = 6f + sin(cft * 4f) * 2f
            drawCircle(
                Brush.radialGradient(listOf(SciFiCyan.copy(alpha = 0.3f), SciFiCyan.copy(alpha = 0.05f), Color.Transparent)),
                radius = glowRadius * 3f,
                center = Offset(rocketCenterX, rocketBaseY + 4f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top status bar
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SciFiSurface,
                border = BorderStroke(0.5.dp, SciFiGold.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_currency_cr),
                            contentDescription = "Credits",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${progressionManager?.creditBalance ?: 0}",
                            color = SciFiGold, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " /${progressionManager?.maxCredits ?: 10}",
                            color = SciFiGold.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            modifier = Modifier.clickable {
                                soundManager?.playSfx("sfx_ui_click")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                showCreditDialog = true
                            },
                            color = SciFiCyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, SciFiCyan.copy(alpha = 0.3f))
                        ) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("+1", color = SciFiGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.width(2.dp))
                                Text("\u25B6", color = SciFiCyan.copy(alpha = 0.5f), fontSize = 7.sp)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%,d".format(Locale.US, progressionManager?.totalCash ?: 0),
                            color = SciFiGold, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_currency_jc),
                            contentDescription = "Cash",
                            modifier = Modifier.size(12.dp)
                        )
                        if (loginManager?.isSignedIn == true) {
                            Spacer(Modifier.width(8.dp))
                            Text("\u2502", color = SciFiWhite.copy(alpha = 0.2f), fontSize = 11.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                loginManager.displayName?.take(16) ?: "",
                                color = SciFiCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.width(3.dp))
                            Text("\u2713", color = SciFiGreen.copy(alpha = 0.7f), fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            val glitchX = if (sin(ft * 15f) > 0.9f) sin(ft * 40f) * 4f else 0f
            Text(
                text = "JUMP DROID",
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = SciFiCyan.copy(alpha = titleGlow * 0.7f),
                        offset = Offset(glitchX, 0f),
                        blurRadius = 24f
                    )
                ),
                color = SciFiWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                fontSize = 42.sp,
                modifier = Modifier.offset(x = glitchX.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "FLEET COMMAND PROTOCOL",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 5.sp),
                color = SciFiCyan.copy(alpha = 0.4f),
                fontSize = 10.sp
            )

            Spacer(Modifier.weight(1f))

            // REDESIGNED ZEN COMMAND CONSOLE
            ZenCommandConsole(
                isUnlocked = progressionManager?.isZenModeUnlocked == true,
                progressionManager = progressionManager,
                borderPulse = borderPulse,
                ft = ft,
                hapticManager = hapticManager,
                soundManager = soundManager,
                onLaunchZen = onLaunchZen
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    soundManager?.playSfx("sfx_ui_confirm")
                    hapticManager?.vibrate(HapticManager.HapticType.TICK)
                    onLaunch()
                },
                modifier = Modifier.fillMaxWidth(0.75f).height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SciFiCyan.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ),
                border = BorderStroke(2.dp, SciFiCyan.copy(alpha = borderPulse))
            ) {
                Text("LAUNCH", fontWeight = FontWeight.Black, letterSpacing = 4.sp, fontSize = 15.sp)
            }

            if (highScore >= 100000) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_confirm")
                        hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                        onPrestige()
                    },
                    modifier = Modifier.fillMaxWidth(0.5f).height(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = SciFiGold
                    ),
                    border = BorderStroke(1.dp, SciFiGold.copy(alpha = 0.4f))
                ) {
                    Text("PRESTIGE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 10.sp)
                }
            }

            // Toggle
            Row(
                modifier = Modifier.fillMaxWidth().clickable { 
                    soundManager?.playSfx("sfx_ui_click")
                    hapticManager?.vibrate(HapticManager.HapticType.TICK)
                    navExpanded = !navExpanded 
                }.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val toggleIcon = if (navExpanded) "\u25B2" else "\u25BC"
                Text(
                    text = "$toggleIcon COMMAND CENTER",
                    color = SciFiCyan.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!navExpanded && hasNewEntries) {
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(6.dp).background(SciFiPurple, CircleShape))
                }
                Text(
                    text = " $toggleIcon",
                    color = SciFiCyan.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = navExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    GhostButton("HANGAR", SciFiGold, borderPulse, shape, soundManager, hapticManager, iconRes = R.drawable.ic_btn_hangar) { onNavigate(GameState.HANGAR) }
                    Spacer(Modifier.height(8.dp))
                    GhostButton("MISSIONS", SciFiCyan, borderPulse, shape, soundManager, hapticManager, iconRes = R.drawable.ic_btn_missions) { onNavigate(GameState.MISSIONS) }
                    Spacer(Modifier.height(8.dp))
                    GhostButton("SHOP", SciFiGreen, borderPulse, shape, soundManager, hapticManager, iconRes = R.drawable.ic_btn_shop) { onNavigate(GameState.SHOP) }
                    if (loginManager != null) {
                        Spacer(Modifier.height(8.dp))
                        if (loginManager.isSignedIn) {
                            GhostButton("SIGN OUT (${loginManager.displayName?.take(10) ?: ""})", SciFiRed.copy(alpha = 0.7f), borderPulse * 0.5f, shape, soundManager, hapticManager) { 
                                showSignOutConfirm = true
                            }
                        } else {
                            GhostButton("CONNECT PILOT ID", SciFiWhite.copy(alpha = 0.6f), borderPulse * 0.5f, shape, soundManager, hapticManager) { 
                                soundManager?.playSfx("sfx_ui_click")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                loginManager.triggerPlayGamesSignIn()
                                onSignIn()
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1.2f))

            // Station Tray (Bottom Row)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StationIcon(iconRes = R.drawable.ic_station_sys, label = "SYS", color = SciFiWhite.copy(alpha = 0.6f), hapticManager = hapticManager) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.SETTINGS)
                }
                StationIcon(iconRes = R.drawable.ic_station_trm, label = "TRM", color = SciFiOrange, hapticManager = hapticManager) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.LEADERBOARD)
                }
                StationIcon(iconRes = R.drawable.ic_station_arc, label = "ARC", color = SciFiPurple, badgeCount = archiveUnreadCount, hapticManager = hapticManager) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.ARCHIVE)
                }
                StationIcon(iconRes = R.drawable.ic_station_inf, label = "INF", color = SciFiCyan, hapticManager = hapticManager) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.ABOUT)
                }
            }
            
            if (progressionManager != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AscensionInsignia(rank = progressionManager.currentRank, insigniaSize = 16.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "RANK: ${progressionManager.currentRank.title.uppercase()} \u2014 ${progressionManager.currentMasteryPoints} MP",
                        color = SciFiGold.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))

            Text(
                "POWERED BY ASHWATH.AI // 2026",
                color = SciFiWhite.copy(alpha = 0.3f),
                letterSpacing = 1.sp,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "ALL SYSTEMS NOMINAL",
                    color = SciFiCyan.copy(alpha = 0.25f),
                    fontSize = 8.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                val shareContext = LocalContext.current
                IconButton(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Explore the skies in Jump Droid! 🚀\nhttps://jump-droid.vercel.app")
                        }
                        shareContext.startActivity(Intent.createChooser(intent, "Share Jump Droid"))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("\u21E7", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SciFiCyan.copy(alpha = 0.4f))
                }
            }
        }

        if (showCreditDialog && progressionManager != null) {
            AddCreditDialog(
                progressionManager = progressionManager,
                soundManager = soundManager,
                hapticManager = hapticManager,
                analytics = analytics,
                onDismiss = { showCreditDialog = false }
            )
        }

        if (showSignOutConfirm && loginManager != null) {
            AlertDialog(
                onDismissRequest = { showSignOutConfirm = false },
                containerColor = SciFiSurface,
                titleContentColor = SciFiCyan,
                textContentColor = SciFiWhite.copy(alpha = 0.8f),
                title = { Text("SIGN OUT?", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                text = { Text("Are you sure you want to disconnect your fleet profile?") },
                confirmButton = {
                    TextButton(onClick = {
                        soundManager?.playSfx("sfx_ui_confirm")
                        hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                        loginManager.signOut()
                        showSignOutConfirm = false
                    }) {
                        Text("SIGN OUT", color = SciFiRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        soundManager?.playSfx("sfx_ui_back")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        showSignOutConfirm = false 
                    }) {
                        Text("CANCEL", color = SciFiWhite.copy(alpha = 0.5f))
                    }
                }
            )
        }
    }
}

@Composable
private fun StationIcon(
    iconRes: Int,
    label: String,
    color: Color,
    badgeCount: Int = 0,
    hapticManager: HapticManager? = null,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeTransition")
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgePulse"
    )
    
    // Occasional wiggle for ARC if unread
    val wiggleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 8000 // Every 8 seconds
                0f at 0
                0f at 7000
                10f at 7100
                -10f at 7300
                10f at 7500
                -10f at 7700
                0f at 7900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "WiggleRotation"
    )
    
    val rotation = if (label == "ARC" && badgeCount > 0) wiggleRotation else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer(rotationZ = rotation)
            .clickable { 
                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                onClick() 
            }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(SciFiSurface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            if (badgeCount > 0) {
                val scale = if (label == "ARC") badgePulse else 1.0f
                Box(
                    Modifier
                        .size(18.dp) // Slightly larger pulse ball
                        .scale(scale)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount", 
                        color = Color.White, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 1.dp) // Visual vertical centering
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = color.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
private fun GhostButton(
    label: String,
    accent: Color,
    borderPulse: Float,
    shape: RoundedCornerShape,
    soundManager: SoundManager?,
    hapticManager: HapticManager? = null,
    badgeCount: Int = 0,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            soundManager?.playSfx("sfx_ui_click")
            hapticManager?.vibrate(HapticManager.HapticType.TICK)
            onClick()
        },
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = accent
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = borderPulse * 0.5f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 13.sp)
            if (badgeCount > 0) {
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(18.dp).background(accent.copy(alpha = 0.2f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$badgeCount",
                        color = accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCreditDialog(
    progressionManager: ProgressionManager,
    soundManager: SoundManager?,
    hapticManager: HapticManager? = null,
    analytics: GameAnalytics,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "DialogTransition")
    val cornerPulse by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "CornerPulse")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                .border(1.dp, SciFiGold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(2.dp)
        ) {
            // Procedural Background
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Dark terminal gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0A0E14), Color(0xFF141D26))
                    ),
                    size = size
                )

                // Scan lines
                val lineSpacing = 6f
                for (y in 0..(h.toInt()) step lineSpacing.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.03f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(w, y.toFloat()),
                        strokeWidth = 1f
                    )
                }

                // Decorative corner brackets
                val bracketSize = 25f
                val bracketStroke = 2f
                val bracketColor = SciFiGold.copy(alpha = 0.4f * cornerPulse)
                
                // Top Left
                drawLine(bracketColor, Offset(10f, 10f), Offset(10f + bracketSize, 10f), bracketStroke)
                drawLine(bracketColor, Offset(10f, 10f), Offset(10f, 10f + bracketSize), bracketStroke)
                // Top Right
                drawLine(bracketColor, Offset(w - 10f, 10f), Offset(w - 10f - bracketSize, 10f), bracketStroke)
                drawLine(bracketColor, Offset(w - 10f, 10f), Offset(w - 10f, 10f + bracketSize), bracketStroke)
                // Bottom Left
                drawLine(bracketColor, Offset(10f, h - 10f), Offset(10f + bracketSize, h - 10f), bracketStroke)
                drawLine(bracketColor, Offset(10f, h - 10f), Offset(10f, h - 10f - bracketSize), bracketStroke)
                // Bottom Right
                drawLine(bracketColor, Offset(w - 10f, h - 10f), Offset(w - 10f - bracketSize, h - 10f), bracketStroke)
                drawLine(bracketColor, Offset(w - 10f, h - 10f), Offset(w - 10f, h - 10f - bracketSize), bracketStroke)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Terminal Header
                Text(
                    "CREDIT ACQUISITION",
                    style = MaterialTheme.typography.titleMedium,
                    color = SciFiGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    "ACCESSING: ACCOUNT_MGMT_V4",
                    style = MaterialTheme.typography.labelSmall,
                    color = SciFiGold.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = {
                        analytics.logAdClicked("rewarded", AdConfig.REWARDED_UNIT_ID)
                        soundManager?.playSfx("sfx_ui_click")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        RewardedAdHelper.show(context as Activity,
                            analytics = analytics,
                            onReward = { progressionManager.addCredits(1) },
                            onFailed = {}
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SciFiCyan.copy(alpha = 0.15f),
                        contentColor = SciFiCyan
                    ),
                    border = BorderStroke(1.5.dp, SciFiCyan.copy(alpha = 0.4f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\u25B6", fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("ESTABLISH AD-LINK  [+1 CR]", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                val currentRate = progressionManager.getCurrentCreditRate()
                val canBuy = progressionManager.totalCash >= currentRate && progressionManager.creditBalance < progressionManager.maxCredits
                Button(
                    onClick = { 
                        soundManager?.playSfx("sfx_ui_confirm")
                        hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                        progressionManager.cashToCredits(currentRate) 
                    },
                    enabled = canBuy,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SciFiGold.copy(alpha = 0.12f),
                        contentColor = SciFiGold,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = SciFiGold.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.5.dp, SciFiGold.copy(alpha = if (canBuy) 0.35f else 0.1f))
                ) {
                    Text("$currentRate JC \u2192 1 CREDIT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(Modifier.height(28.dp))

                // Data Readout Section
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("BALANCE_STATUS", color = SciFiWhite.copy(alpha = 0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("SECURE_LINK", color = SciFiGreen.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp), color = SciFiWhite.copy(alpha = 0.1f), thickness = 0.5.dp)
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("JUMP_CREDITS", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                "${progressionManager.creditBalance} / ${progressionManager.maxCredits}",
                                color = SciFiGold, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("CASH_RESERVES", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                "%,d".format(Locale.US, progressionManager.totalCash),
                                color = SciFiGold, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                TextButton(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_back")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        onDismiss()
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("DISCONNECT", color = SciFiRed.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ZenCommandConsole(
    isUnlocked: Boolean,
    progressionManager: ProgressionManager?,
    borderPulse: Float,
    ft: Float,
    hapticManager: HapticManager?,
    soundManager: SoundManager?,
    onLaunchZen: () -> Unit
) {
    val accent = if (isUnlocked) SciFiPurple else SciFiRed
    val panelAlpha = if (isUnlocked) 0.15f else 0.12f

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp),
        color = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = borderPulse * 0.5f))
    ) {
        Box(Modifier.height(IntrinsicSize.Min)) {
            // Immersive Terminal Background (Locked Only)
            if (!isUnlocked) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Matrix-style falling binary
                    repeat(15) { col ->
                        val seed = col * 99L
                        val prng = Random(seed)
                        val speed = 30f + prng.nextFloat() * 60f
                        val charX = (col * (w / 15f)) + 10f
                        val charY = (ft * speed) % (h + 40f) - 20f
                        
                        val paint = android.graphics.Paint().apply {
                            color = accent.copy(alpha = 0.15f).toArgb()
                            textSize = 24f
                            typeface = android.graphics.Typeface.MONOSPACE
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            if (prng.nextBoolean()) "0" else "1",
                            charX, charY, paint
                        )
                    }
                }
            }

            Box(Modifier.padding(12.dp).background(accent.copy(alpha = panelAlpha))) {
                // Animated Corners
                Box(Modifier.matchParentSize()) {
                    val bracketSize = 12.dp
                    val bracketAlpha = (0.4f + borderPulse * 0.4f).coerceIn(0f, 1f)
                    // Top Left
                    Box(Modifier.size(bracketSize).align(Alignment.TopStart).border(BorderStroke(2.dp, accent.copy(alpha = bracketAlpha)), RoundedCornerShape(topStart = 4.dp)))
                    // Bottom Right
                    Box(Modifier.size(bracketSize).align(Alignment.BottomEnd).border(BorderStroke(2.dp, accent.copy(alpha = bracketAlpha)), RoundedCornerShape(bottomEnd = 4.dp)))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isUnlocked) "ZEN PROTOCOL // AUTHORIZED" else "SECURE CHANNEL // ENCRYPTED",
                                color = accent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            if (!isUnlocked) {
                                Text(
                                    text = "UNAUTHORIZED PILOT DETECTED",
                                    color = accent.copy(alpha = 0.4f),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (!isUnlocked) {
                            val blink = if ((ft * 4).toInt() % 2 == 0) 1f else 0.2f
                            Text(
                                text = "DECRYPTING...",
                                color = accent.copy(alpha = 0.8f * blink),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.graphicsLayer(alpha = blink)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (isUnlocked) {
                        Button(
                            onClick = {
                                soundManager?.playSfx("sfx_ui_confirm")
                                hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                                onLaunchZen()
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.2f), contentColor = SciFiWhite),
                            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.8f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val pulse = sin(ft * 4f) * 0.2f + 0.8f
                                Box(Modifier.size(10.dp).graphicsLayer(scaleX = pulse, scaleY = pulse).background(accent, CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text("SYNC & DEPLOY ZEN MODE", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 12.sp)
                            }
                        }
                    } else {
                        val requirements = progressionManager?.getZenRequirements() ?: emptyList()
                        requirements.forEach { (label, status, progress) ->
                            val flicker = if (Random.nextFloat() > 0.95f) 0.5f else 1f
                            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp).graphicsLayer(alpha = flicker)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(label, color = SciFiWhite.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(status, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(Modifier.height(3.dp))
                                Box(Modifier.fillMaxWidth().height(4.dp).background(Color.Black.copy(alpha = 0.5f))) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(progress)
                                            .fillMaxHeight()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(accent.copy(alpha = 0.3f), accent)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "ENCRYPTION STRENGTH: ${(12.8f + sin(ft)*2f).format(1)} EB // VOLATILE",
                            color = accent.copy(alpha = 0.3f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)
