package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit

/**
 * Manages the player's equipped modules and handles persistence.
 */
class LoadoutManager(private val sharedPrefs: SharedPreferences) {

    var onModuleEquipped: ((moduleId: String, slotIndex: Int) -> Unit)? = null

    private val _equippedModuleIds = mutableStateListOf<String?>(null, null) // 2 Slots
    val equippedModuleIds: List<String?> get() = _equippedModuleIds

    init {
        loadLoadout()
    }

    /**
     * Loads equipped module IDs from SharedPreferences.
     */
    private fun loadLoadout() {
        for (i in _equippedModuleIds.indices) {
            _equippedModuleIds[i] = sharedPrefs.getString("equipped_module_$i", null)
        }
    }

    /**
     * Persists the current loadout to SharedPreferences.
     */
    private fun saveLoadout() {
        sharedPrefs.edit {
            for (i in _equippedModuleIds.indices) {
                putString("equipped_module_$i", _equippedModuleIds[i])
            }
        }
    }

    /**
     * Equips a module into the specified slot.
     */
    fun equipModule(moduleId: String, slotIndex: Int) {
        if (slotIndex !in _equippedModuleIds.indices) return
        
        // Remove from other slots if already equipped (Unique modules)
        val existingIndex = _equippedModuleIds.indexOf(moduleId)
        if (existingIndex != -1) {
            _equippedModuleIds[existingIndex] = null
        }

        _equippedModuleIds[slotIndex] = moduleId
        onModuleEquipped?.invoke(moduleId, slotIndex)
        saveLoadout()
    }

    /**
     * Removes the module from the specified slot.
     */
    fun unequipModule(slotIndex: Int) {
        if (slotIndex !in _equippedModuleIds.indices) return
        _equippedModuleIds[slotIndex] = null
        saveLoadout()
    }

    /**
     * Checks if a module is unlocked based on ownership or requirements.
     */
    fun isModuleUnlocked(
        module: Module,
        progression: ProgressionManager,
        missionManager: MissionManager
    ): Boolean {
        if (progression.isModuleOwned(module.id)) return true
        return UnlockEngine.evaluate(module.unlockRequirement, progression, missionManager)
    }

    /**
     * Returns a list of active Module instances based on currently equipped IDs.
     */
    fun getActiveModules(progressionManager: ProgressionManager): List<Module> {
        return _equippedModuleIds.mapNotNull { 
            it?.let { id -> 
                if (progressionManager.isModuleOwned(id)) {
                    ModuleRegistry.getById(id)
                } else null
            }
        }
    }
}
