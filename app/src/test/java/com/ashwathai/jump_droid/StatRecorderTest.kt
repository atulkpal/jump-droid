package com.ashwathai.jump_droid

import android.content.SharedPreferences
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StatRecorderTest {
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private lateinit var statRecorder: StatRecorder

    @Before
    fun setup() {
        every { sharedPrefs.edit() } returns editor
        statRecorder = StatRecorder(sharedPrefs)
    }

    @Test
    fun `commitSessionStats saves top runs correctly when count is less than 3`() {
        val stats = createStats(1500)

        statRecorder.commitSessionStats(stats, 0)

        // Verify that top_run_1 is saved
        verify { editor.putInt("top_run_1", 1500) }
        verify { editor.putInt("top_run_2", 0) }
        verify { editor.putInt("top_run_3", 0) }
    }

    @Test
    fun `commitSessionStats maintains top 3 runs`() {
        val stats1 = createStats(1000)
        val stats2 = createStats(2000)
        val stats3 = createStats(1500)
        val stats4 = createStats(2500)

        statRecorder.commitSessionStats(stats1, 0)
        statRecorder.commitSessionStats(stats2, 0)
        statRecorder.commitSessionStats(stats3, 0)
        statRecorder.commitSessionStats(stats4, 0)

        assertEquals(listOf(2500, 2000, 1500), statRecorder.topRuns.toList())
        
        verify { editor.putInt("top_run_1", 2500) }
        verify { editor.putInt("top_run_2", 2000) }
        verify { editor.putInt("top_run_3", 1500) }
    }

    private fun createStats(score: Int) = GameStats(
        totalFlightTime = 0f, totalPlatformTime = 0f, bossesDefeated = 0,
        hazardHitsSurvived = 0, artifactsCollected = 0, platformLandings = 0,
        maxAltitudeMeters = 0, maxCombo = 0, totalScore = score,
        combosOver15 = 0, continuesUsed = 0, wasNearDeath = false
    )
}
