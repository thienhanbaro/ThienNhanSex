package com.example.spnew2

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Lưu & khôi phục lựa chọn giao diện (sáng/tối) và ngôn ngữ (vi/en).
 * Gọi [applySaved] ở dòng đầu tiên của Activity.onCreate, trước super.onCreate().
 */
object PrefsHelper {

    private const val PREFS_NAME   = "sp_new2_prefs"
    private const val KEY_NIGHT    = "night_mode"
    private const val KEY_LOCALE   = "locale_tag"

    fun isNightMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NIGHT, false)

    fun setNightMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NIGHT, enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun getLocaleTag(context: Context): String =
        prefs(context).getString(KEY_LOCALE, "vi") ?: "vi"

    fun setLocaleTag(context: Context, tag: String) {
        prefs(context).edit().putString(KEY_LOCALE, tag).apply()
        AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(tag)
        )
    }

    /** Áp dụng theme + ngôn ngữ đã lưu. Gọi trước setContentView. */
    fun applySaved(context: Context) {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode(context)) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(getLocaleTag(context))
        )
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
