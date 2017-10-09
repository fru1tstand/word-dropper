package me.fru1t.worddropper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.StringRes

import java.util.HashSet

import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.settings.ColorTheme
import me.fru1t.worddropper.settings.Dictionary
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler

/**
 * Settings and global variables (*gasp*) loaded onWrapEventListener startup.
 */
class WordDropperApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val LOG_TAG = "WordDropper"
    }

    private val colorThemeEventHandlers: HashSet<ColorThemeEventHandler> = HashSet()

    private val sharedPreferences by lazy {
        getSharedPreferences(
                resources.getString(R.string.app_sharedPreferencesFileName), Context.MODE_PRIVATE)
    }
    val dictionary: Dictionary by lazy { Dictionary(this) }
    val databaseUtils: DatabaseUtils by lazy { DatabaseUtils(applicationContext) }
    val isDebugging: Boolean
        get() = resources.getBoolean(R.bool.app_debug)
    val deleteDatabaseOnDebug: Boolean
        get() = resources.getBoolean(R.bool.app_deleteDatabaseOnDebug)

    private var colorTheme: ColorTheme? = null

    override fun onCreate() {
        super.onCreate()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        updateFromSettings()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        updateFromSettings()
    }

    /**
     * Retrieves a string preference given a key and default value.
     * @param prefKey The preference name given via string resource id.
     * @param defaultValue The default value the setting should be if it doesn't exist.
     * @return The string preference.
     */
    fun getStringPreference(@StringRes prefKey: Int, defaultValue: String): String? =
            sharedPreferences.getString(resources.getString(prefKey), defaultValue)

    /**
     * Applies a preference given its preference key and value.
     * @param prefKey The preference name given via string resource id.
     * @param value The value to store.
     */
    fun putStringPreference(@StringRes prefKey: Int, value: String) {
        sharedPreferences.edit().putString(resources.getString(prefKey), value).apply()
    }

    /**
     * Adds a color theme event handler, triggering the
     * [ColorThemeEventHandler.onColorThemeChange] upon successful registration.
     */
    fun addColorThemeEventHandler(handler: ColorThemeEventHandler) {
        if (colorThemeEventHandlers.add(handler)) {
            handler.onColorThemeChange(colorTheme!!)
        }
    }

    fun removeColorThemeEventHandler(handler: ColorThemeEventHandler) {
        colorThemeEventHandlers.remove(handler)
    }

    private fun updateFromSettings() {
        val newTheme = ColorTheme.valueOf(
                getStringPreference(R.string.pref_colorTheme, ColorTheme.INVERSE_ORANGE.name)!!)
        if (newTheme != colorTheme) {
            colorTheme = newTheme
            colorThemeEventHandlers.forEach { handler -> handler.onColorThemeChange(newTheme) }
        }
    }
}
