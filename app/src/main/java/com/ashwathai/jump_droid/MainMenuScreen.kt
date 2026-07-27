package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    onNavigate: (GameState) -> Unit,
    onExit: () -> Unit,
    highScore: Int = 0,
    onPrestige: () -> Unit = {},
    soundManager: SoundManager? = null,
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

    val shape = RoundedCornerShape(12.dp)
    var navExpanded by remember { mutableStateOf(true) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val analytics = LocalAnalytics.current

    Box(Modifier.fillMaxSize().background(SciFiBackground)) {
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 80, alphaRange = 0.15f..0.6f)
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val ft = System.nanoTime() / 1e9f

            drawCircle(SciFiCyan.copy(alpha = 0.04f), radius = 60f, center = Offset(w * 0.15f + sin(ft) * 8f, h * 0.12f + cos(ft * 0.7f) * 6f))
            drawCircle(SciFiPurple.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.85f + cos(ft * 0.5f) * 10f, h * 0.88f + sin(ft * 0.8f) * 8f))
            drawCircle(SciFiGold.copy(alpha = 0.02f), radius = 50f, center = Offset(w * 0.5f + sin(ft * 0.3f) * 5f, h * 0.5f + cos(ft * 0.4f) * 5f))

            repeat(12) { i ->
                val px = ((i * 137.5f) % w.toFloat())
                val py = ((i * 89.3f + ft * 20f * (0.5f + (i % 3) * 0.25f)) % h.toFloat())
                drawCircle(SciFiCyan.copy(alpha = 0.05f + sin(ft + i) * 0.03f), radius = 1.5f, center = Offset(px, py))
            }

            val cx = w / 2
            val cy = h * 0.22f
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

            val glowRadius = 6f + sin(ft * 4f) * 2f
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

            Text(
                text = "JUMP DROID",
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(SciFiCyan.copy(alpha = titleGlow * 0.6f), blurRadius = 22f)
                ),
                color = SciFiWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                fontSize = 28.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "FLEET COMMAND",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp),
                color = SciFiCyan.copy(alpha = 0.35f)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    soundManager?.playSfx("sfx_ui_confirm")
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
                    onClick = onPrestige,
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
                modifier = Modifier.fillMaxWidth().clickable { navExpanded = !navExpanded }.padding(vertical = 8.dp),
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
                    GhostButton("HANGAR", SciFiGold, borderPulse, shape, soundManager, iconRes = R.drawable.ic_btn_hangar) { onNavigate(GameState.HANGAR) }
                    Spacer(Modifier.height(8.dp))
                    GhostButton("MISSIONS", SciFiCyan, borderPulse, shape, soundManager, iconRes = R.drawable.ic_btn_missions) { onNavigate(GameState.MISSIONS) }
                    Spacer(Modifier.height(8.dp))
                    GhostButton("SHOP", SciFiGreen, borderPulse, shape, soundManager, iconRes = R.drawable.ic_btn_shop) { onNavigate(GameState.SHOP) }
                    if (loginManager != null) {
                        Spacer(Modifier.height(8.dp))
                        if (loginManager.isSignedIn) {
                            GhostButton("SIGN OUT (${loginManager.displayName?.take(10) ?: ""})", SciFiRed.copy(alpha = 0.7f), borderPulse * 0.5f, shape, soundManager) { 
                                showSignOutConfirm = true
                            }
                        } else {
                            GhostButton("SIGN IN", SciFiWhite.copy(alpha = 0.6f), borderPulse * 0.5f, shape, soundManager) { onSignIn() }
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
                StationIcon(iconRes = R.drawable.ic_station_sys, label = "SYS", color = SciFiWhite.copy(alpha = 0.6f)) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.SETTINGS)
                }
                StationIcon(iconRes = R.drawable.ic_station_trm, label = "TRM", color = SciFiOrange) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.LEADERBOARD)
                }
                StationIcon(iconRes = R.drawable.ic_station_arc, label = "ARC", color = SciFiPurple, badgeCount = archiveUnreadCount) {
                    soundManager?.playSfx("sfx_ui_click")
                    onNavigate(GameState.ARCHIVE)
                }
                StationIcon(iconRes = R.drawable.ic_station_inf, label = "INF", color = SciFiCyan) {
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
                        text = "RANK ${progressionManager.currentRank.title.split(" ").last()} \u2014 ${progressionManager.currentMasteryPoints} MP",
                        color = SciFiGold.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))

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
                        loginManager.signOut()
                        showSignOutConfirm = false
                    }) {
                        Text("SIGN OUT", color = SciFiRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutConfirm = false }) {
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
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
                        .size(16.dp)
                        .scale(scale)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(4.dp, (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$badgeCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
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
    badgeCount: Int = 0,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            soundManager?.playSfx("sfx_ui_click")
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

                val bosses = progressionManager.lifetimeBossesDefeated
                val currentRate = when {
                    bosses >= 15 -> 800
                    bosses >= 10 -> 400
                    bosses >= 5 -> 200
                    else -> 100
                }
                val canBuy = progressionManager.totalCash >= currentRate && progressionManager.creditBalance < progressionManager.maxCredits
                Button(
                    onClick = { progressionManager.cashToCredits(currentRate) },
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
                    onClick = onDismiss,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("DISCONNECT", color = SciFiRed.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 11.sp)
                }
            }
        }
    }
}
