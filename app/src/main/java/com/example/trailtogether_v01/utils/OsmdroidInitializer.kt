package com.example.trailtogether_v01.utils

import android.content.Context
import org.osmdroid.config.Configuration
import java.io.File

object OsmdroidInitializer {
    fun init(context: Context) {
        // OSMDroid stores tiles under <app-files>/osmdroid/
        val osmdroidBase = File(context.getExternalFilesDir(null), "osmdroid")
        val cacheDir    = File(osmdroidBase, "tiles")

        Configuration.getInstance().apply {
            // Load persisted preferences (cache size, etc.)
            load(context, context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE))

            osmdroidTileCache = cacheDir
            userAgentValue    = context.packageName
            tileDownloadThreads = 4
            tileFileSystemCacheMaxBytes = 150L * 1024 * 1024   // 150 MiB cache
            isDebugMode = false
        }
    }
}