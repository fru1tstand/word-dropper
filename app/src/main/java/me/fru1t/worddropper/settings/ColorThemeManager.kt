package me.fru1t.worddropper.settings

import android.content.Context
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Singleton
import me.fru1t.worddropper.R

/** Handles changes to the color theme and classes that may react to those changes. */
@Inject
@Singleton
class ColorThemeManager(private val preferences: PreferenceManager, context: Context) {
    private val colorThemeKey = context.getString(R.string.pref_colorTheme)
    private val colorThemeChangeListeners = HashSet<() -> Unit>()

    var currentColorTheme =
            ColorTheme.valueOf(
                    preferences.getString(R.string.pref_colorTheme, ColorTheme.INVERSE_ORANGE.name))
        private set

    init {
        // Sets the color theme if the preferences were set directly.
        preferences.addChangeListener {
            if (it != colorThemeKey) {
                return@addChangeListener
            }
            loadColorThemeFromSettings()
        }
        loadColorThemeFromSettings()
    }

    /** Sets the current color theme and saves the setting */
    fun setColorTheme(theme: ColorTheme) {
        preferences.applyString(R.string.pref_colorTheme, theme.name)
        currentColorTheme = theme
        colorThemeChangeListeners.forEach { it() }
    }

    fun addChangeListener(listener: () -> Unit): Boolean = colorThemeChangeListeners.add(listener)
    fun removeChangeListener(listener: () -> Unit): Boolean =
            colorThemeChangeListeners.remove(listener)

    private fun loadColorThemeFromSettings() {
        currentColorTheme = ColorTheme.valueOf(
                preferences.getString(R.string.pref_colorTheme, ColorTheme.INVERSE_ORANGE.name))
        colorThemeChangeListeners.forEach { it() }
    }
}
