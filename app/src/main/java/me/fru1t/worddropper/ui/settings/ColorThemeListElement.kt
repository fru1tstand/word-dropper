package me.fru1t.worddropper.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.settings.ColorTheme
import me.fru1t.worddropper.settings.ColorThemeManager

/** An injectable factory that creates [ColorThemeListElement]s */
@Inject
class ColorThemeListElementFactory(
        private val context: Context,
        private val colorThemeManager: ColorThemeManager) {
    fun create(colorTheme: ColorTheme): ColorThemeListElement =
            ColorThemeListElement(colorThemeManager, context, colorTheme)
}

/** Displays the value and sample colors for a specific color theme. */
class ColorThemeListElement @JvmOverloads constructor(
        private val colorThemeManager: ColorThemeManager,
        context: Context,
        colorTheme: ColorTheme? = null) : LinearLayout(context) {

    init {
        if (colorTheme == null) {
            throw Exception("ColorThemeListElement must be initialized with a color theme.")
        }

        val layout = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams = layout

        val hPadding = resources.getDimension(R.dimen.app_edgeSpace).toInt()
        val vPadding = resources.getDimension(R.dimen.app_vSpace).toInt()
        setPadding(hPadding, vPadding, hPadding, vPadding)

        setOnClickListener { colorThemeManager.setColorTheme(colorTheme) }

        orientation = LinearLayout.VERTICAL
        isClickable = true

        LayoutInflater.from(context).inflate(R.layout.layout_settings_color_theme_list_element, this)

        // Style elements
        setBackgroundColor(colorTheme.background)
        val title = findViewById(R.id.title) as TextView
        title.text = colorTheme.displayName
        title.setTextColor(colorTheme.text)

        findViewById(R.id.colorThemePrimary).setBackgroundColor(colorTheme.primary)
        findViewById(R.id.colorThemePrimaryDark).setBackgroundColor(colorTheme.primaryDark)
        findViewById(R.id.colorThemePrimaryLight).setBackgroundColor(colorTheme.primaryLight)
        findViewById(R.id.colorThemePrimaryBackgroundLight)
                .setBackgroundColor(colorTheme.backgroundLight)
        findViewById(R.id.colorThemeTextBlend).setBackgroundColor(colorTheme.textBlend)
    }
}
