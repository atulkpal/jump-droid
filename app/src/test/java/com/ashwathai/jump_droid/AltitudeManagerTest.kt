package com.ashwathai.jump_droid

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AltitudeManagerTest {
    private lateinit var altitudeManager: AltitudeManager

    @Before
    fun setup() {
        altitudeManager = AltitudeManager()
    }

    @Test
    fun `updateAltitude updates zone correctly`() {
        // Starts at EARTH (0m)
        assertEquals(AltitudeZone.EARTH, altitudeManager.currentZone)

        // Move to 1000m -> CLOUD_LAYER
        altitudeManager.updateAltitude(1000)
        assertEquals(AltitudeZone.CLOUD_LAYER, altitudeManager.currentZone)

        // Move to 2500m -> UPPER_ATMOSPHERE
        altitudeManager.updateAltitude(2500)
        assertEquals(AltitudeZone.UPPER_ATMOSPHERE, altitudeManager.currentZone)
    }

    @Test
    fun `reset returns manager to initial state`() {
        altitudeManager.updateAltitude(5000)
        assertEquals(AltitudeZone.ORBIT, altitudeManager.currentZone)

        altitudeManager.reset()
        assertEquals(AltitudeZone.EARTH, altitudeManager.currentZone)
    }
}
