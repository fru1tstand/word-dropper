package me.fru1t.worddropper.settings.colortheme

import android.support.annotation.StyleableRes
import android.util.AttributeSet
import android.view.View
import me.fru1t.android.content.res.use
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Singleton
import me.fru1t.worddropper.settings.ColorThemeManager

/** An injectable factory that creates [ColorThemeViewProxy] */
@Inject
@Singleton
class ColorThemeViewProxyFactory(private val colorThemeManager: ColorThemeManager) {
    fun create(
            view: View,
            set: AttributeSet?,
            @StyleableRes attrs: IntArray,
            vararg attributeMaps: AttributeMap) =
            ColorThemeViewProxy(colorThemeManager, view, set, attrs, *attributeMaps)
}

/**
 * Maps an [colorAttribute] (color) to an [action] falling back on [defaultColor] if the
 * attribute isn't set. For example, mapping "backgroundColorTheme" to "setBackgroundColor".
 */
data class AttributeMap(
        @StyleableRes val colorAttribute: Int,
        val defaultColor: ColorThemeXml,
        val action: (Int) -> Unit)

/**
 * Internally used data class that maps a color theme xml to an action. For example, mapping
 * ColorThemeXml.background_color to "setBackgroundColor".
 */
private data class Action(
        val action: (Int) -> Unit,
        val colorThemeXml: ColorThemeXml
)

/**
 * Automatically sets up color theming for a view by binding XML color attributes to respective
 * functions (eg. binding ColorThemeXml.background_color to setBackgroundColor).
 */
class ColorThemeViewProxy internal constructor(
        private val colorThemeManager: ColorThemeManager,
        view: View,
        set: AttributeSet?,
        @StyleableRes attrs: IntArray,
        vararg attributeMaps: AttributeMap) {
    private val actions = arrayOfNulls<Action>(attributeMaps.size)

    init {
        view.context.obtainStyledAttributes(set, attrs).use {
            for (i in attributeMaps.indices) {
                actions[i] = Action(
                        attributeMaps[i].action,
                        ColorThemeXml.getColorThemeXmlFromValue(
                                it.getInt(
                                        attributeMaps[i].colorAttribute,
                                        attributeMaps[i].defaultColor.value)))
            }
        }

        colorThemeManager.bindView(view, {
            actions.forEach {
                it!!.action(it.colorThemeXml.map(colorThemeManager.currentColorTheme))
            }
        })
    }
}
