package me.fru1t.worddropper.settings.colortheme

import android.graphics.Color
import me.fru1t.worddropper.settings.ColorTheme

/**
 * Maps the XML ColorTheme enum values to [ColorTheme] fields.
 * Keep in sync with attrs.xml's textColorTheme and backgroundColorTheme.
 */
enum class ColorThemeXml(val value: Int, val map: (ColorTheme) -> Int) {
    TRANSPARENT(0, { Color.TRANSPARENT }),
    PRIMARY(1, { it.primary }),
    PRIMARY_DARK(2, { it.primaryDark }),
    PRIMARY_LIGHT(3, { it.primaryLight }),
    BACKGROUND(4, { it.background }),
    BACKGROUND_LIGHT(5, { it.backgroundLight }),
    TEXT(6, { it.text }),
    TEXT_BLEND(7, { it.textBlend }),
    TEXT_ON_PRIMARY(8, { it.textOnPrimary }),
    TEXT_ON_PRIMARY_LIGHT(9, { it.textOnPrimaryLight }),
    TEXT_ON_PRIMARY_DARK(10, { it.textOnPrimaryDark });

    companion object {
        /** Retrieves the corresponding ColorThemeXml enum from its XML [value]. */
        fun getColorThemeXmlFromValue(value: Int): ColorThemeXml =
            ColorThemeXml.values().firstOrNull { it.value == value } ?: TRANSPARENT
    }
}
