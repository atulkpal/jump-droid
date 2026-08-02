package com.ashwathai.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.ashwathai.jump_droid.ui.theme.*

enum class ZenTheme(
    val displayName: String, 
    val starColor: Color, 
    val accentColor: Color,
    val topColor: Color,
    val bottomColor: Color
) {
    NEBULA("NEBULA", SciFiPurple, SciFiPurple, Color(0xFF0D001A), Color(0xFF1A0033)),
    VOID("VOID", SciFiWhite.copy(alpha = 0.3f), SciFiCyan, Color(0xFF000411), Color.Black),
    AURORA("AURORA", Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF001A0D), Color(0xFF00331A)),
    SOLAR("SOLAR", SciFiGold, SciFiGold, Color(0xFF1A0A00), Color(0xFF3E1A00))
}

object ZenThemeManager {
    var currentTheme by mutableStateOf(ZenTheme.NEBULA)
    
    private val unlockedThemes = mutableSetOf(ZenTheme.NEBULA)
    
    fun isThemeUnlocked(theme: ZenTheme, isPremium: Boolean): Boolean {
        if (isPremium) return true
        return unlockedThemes.contains(theme)
    }
    
    fun unlockTheme(theme: ZenTheme) {
        unlockedThemes.add(theme)
    }
}
