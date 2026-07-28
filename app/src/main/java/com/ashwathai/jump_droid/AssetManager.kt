package com.ashwathai.jump_droid

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages loading and caching of ImageBitmap assets for the experimental 
 * asset-based rendering mode.
 */
object AssetManager {
    private val bitmapCache = ConcurrentHashMap<Int, ImageBitmap>()

    fun getBitmap(context: Context, resId: Int): ImageBitmap {
        return bitmapCache.getOrPut(resId) {
            BitmapFactory.decodeResource(context.resources, resId).asImageBitmap()
        }
    }

    fun clearCache() {
        bitmapCache.clear()
    }
}
