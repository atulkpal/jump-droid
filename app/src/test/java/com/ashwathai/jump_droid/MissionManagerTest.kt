package com.ashwathai.jump_droid

import android.util.Log
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MissionManagerTest {
    private val progressionService = mockk<ProgressionService>(relaxed = true)
    private lateinit var missionManager: MissionManager

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        missionManager = MissionManager(progressionService)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `refreshActiveMissions clears and refills active missions`() {
        missionManager.selectNextMission()
        val initialMissions = missionManager.activeMissions.toList()
        assertTrue(initialMissions.isNotEmpty())

        missionManager.refreshActiveMissions()
        val refreshedMissions = missionManager.activeMissions.toList()
        
        assertEquals(3, refreshedMissions.size)
        // Note: they might be the same if the pool is small, but they should be refreshed
    }

    @Test
    fun `updateProgress increments mission progress correctly`() {
        val testMissionId = "test_mission"
        val mission = Mission(
            id = testMissionId,
            name = "Test",
            description = "Desc",
            type = MissionType.EXPLORATION,
            category = MissionCategory.LANDINGS,
            tier = MissionTier.TIER_1,
            targetValue = 10,
            rewards = emptyList(),
            unlockCondition = null,
            icon = "",
            isHidden = false,
            crypticHint = "",
            debrief = "",
            initialProgress = 0
        )
        
        missionManager.allMissionInstances[testMissionId] = mission
        missionManager.activeMissions.add(mission)

        missionManager.updateProgress(MissionType.EXPLORATION, increment = 5)
        
        assertEquals(5, mission.currentProgress)
        verify { progressionService.saveMissionProgress(testMissionId, 5) }
    }
}
