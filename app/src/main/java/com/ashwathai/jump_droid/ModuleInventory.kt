package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

class ModuleInventory(private val sharedPrefs: SharedPreferences) {
    var ownedModuleIds by mutableStateOf<Set<String>>(emptySet())
        private set

    init {
        loadModules()
    }

    private fun loadModules() {
        ownedModuleIds = sharedPrefs.getStringSet("owned_modules", emptySet()) ?: emptySet()
    }

    fun grantModule(moduleId: String): Boolean {
        if (ownedModuleIds.contains(moduleId)) return false
        ownedModuleIds = ownedModuleIds + moduleId
        sharedPrefs.edit {
            putStringSet("owned_modules", ownedModuleIds)
        }
        return true
    }

    fun isModuleOwned(moduleId: String): Boolean {
        return ownedModuleIds.contains(moduleId)
    }

    fun clear() {
        ownedModuleIds = emptySet()
    }
}
