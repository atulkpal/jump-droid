package com.example.jump_droid

/**
 * Sealed class defining the various passive bonuses granted by complete artifact sets.
 */
sealed class ArtifactBonus {
    abstract val description: String

    data class FuelRegen(val multiplier: Float) : ArtifactBonus() {
        override val description: String = "Fuel Recovery +${((multiplier - 1f) * 100).toInt()}%"
    }
    data class ShieldRegen(val multiplier: Float) : ArtifactBonus() {
        override val description: String = "Shield Recharge +${((multiplier - 1f) * 100).toInt()}%"
    }
    data class HeatCooldown(val multiplier: Float) : ArtifactBonus() {
        override val description: String = "Heat Dissipation +${((multiplier - 1f) * 100).toInt()}%"
    }
    data class HullBoost(val amount: Float) : ArtifactBonus() {
        override val description: String = "Max Hull Integrity +${amount.toInt()}"
    }
    data class ThrustBoost(val multiplier: Float) : ArtifactBonus() {
        override val description: String = "Thrust Force +${((multiplier - 1f) * 100).toInt()}%"
    }
    data class GlobalEfficiency(val multiplier: Float) : ArtifactBonus() {
        override val description: String = "Global Resource Efficiency +${((multiplier - 1f) * 100).toInt()}%"
    }
}

/**
 * Data model for a collection of discoveries that grant a passive bonus when all are found.
 */
data class ArtifactSet(
    val id: String,
    val name: String,
    val discoveries: List<DiscoveryType>,
    val bonus: ArtifactBonus
) {
    companion object {
        val ALL_SETS = listOf(
            ArtifactSet(
                id = "set_lore",
                name = "The Great Signal",
                discoveries = listOf(
                    DiscoveryType.LORE_ASCENSION,
                    DiscoveryType.LORE_SIGNAL,
                    DiscoveryType.LORE_LOGS
                ),
                bonus = ArtifactBonus.ThrustBoost(1.05f)
            ),
            ArtifactSet(
                id = "set_areas_low",
                name = "Planetary Ascent",
                discoveries = listOf(
                    DiscoveryType.AREA_EARTH,
                    DiscoveryType.AREA_CLOUDS,
                    DiscoveryType.AREA_ATMOSPHERE
                ),
                bonus = ArtifactBonus.FuelRegen(1.10f)
            ),
            ArtifactSet(
                id = "set_areas_high",
                name = "Deep Void",
                discoveries = listOf(
                    DiscoveryType.AREA_ORBIT,
                    DiscoveryType.AREA_SPACE,
                    DiscoveryType.AREA_VOID
                ),
                bonus = ArtifactBonus.ShieldRegen(1.15f)
            ),
            ArtifactSet(
                id = "set_efficiency",
                name = "Efficiency Protocol",
                discoveries = listOf(
                    DiscoveryType.EFFICIENCY_MODULE,
                    DiscoveryType.EFFICIENCY_SURVIVAL,
                    DiscoveryType.BOOST_PLATFORM
                ),
                bonus = ArtifactBonus.HeatCooldown(1.20f)
            ),
            ArtifactSet(
                id = "set_core",
                name = "Structural Integrity",
                discoveries = listOf(
                    DiscoveryType.FUEL_TANK,
                    DiscoveryType.HEAT_SINK,
                    DiscoveryType.TURBO_BOOSTER
                ),
                bonus = ArtifactBonus.HullBoost(25f)
            ),
            ArtifactSet(
                id = "set_ascension_final",
                name = "Grand Ascension",
                discoveries = listOf(
                    DiscoveryType.ART_PRE_SIGNAL_MAP,
                    DiscoveryType.ART_BIOMECH_SHARD
                ),
                bonus = ArtifactBonus.GlobalEfficiency(1.25f)
            )
        )
    }
}
