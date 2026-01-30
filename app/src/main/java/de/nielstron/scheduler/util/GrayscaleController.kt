package de.nielstron.scheduler.util

import android.content.Context
import android.provider.Settings

object GrayscaleController {
    private const val DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled"
    private const val DALTONIZER_MODE = "accessibility_display_daltonizer"
    private const val MONOCHROMACY = 0

    fun isGrayscaleEnabled(context: Context): Boolean? {
        return try {
            val resolver = context.contentResolver
            val enabled = Settings.Secure.getInt(resolver, DALTONIZER_ENABLED, 0) == 1
            val mode = Settings.Secure.getInt(resolver, DALTONIZER_MODE, MONOCHROMACY)
            enabled && mode == MONOCHROMACY
        } catch (exception: SecurityException) {
            null
        }
    }

    fun setGrayscale(context: Context, enabled: Boolean): Boolean {
        return try {
            val resolver = context.contentResolver
            val modeSet = Settings.Secure.putInt(resolver, DALTONIZER_MODE, MONOCHROMACY)
            val enabledSet =
                Settings.Secure.putInt(resolver, DALTONIZER_ENABLED, if (enabled) 1 else 0)
            modeSet && enabledSet
        } catch (exception: SecurityException) {
            false
        }
    }
}
