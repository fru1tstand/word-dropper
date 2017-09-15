package me.fru1t.worddropper.settings.colortheme;

import android.graphics.Color;

import java.util.function.Function;

import me.fru1t.worddropper.settings.ColorTheme;

/**
 * Maps the XML ColorTheme enum values to {@link ColorTheme} fields.
 * Keep in sync with attrs.xml's textColorTheme and backgroundColorTheme.
 */
public enum ColorThemeXml {
    TRANSPARENT(0, colorTheme -> Color.TRANSPARENT),
    PRIMARY(1, colorTheme -> colorTheme.primary),
    PRIMARY_DARK(2, colorTheme -> colorTheme.primaryDark),
    PRIMARY_LIGHT(3, colorTheme -> colorTheme.primaryLight),
    BACKGROUND(4, colorTheme -> colorTheme.background),
    BACKGROUND_LIGHT(5, colorTheme -> colorTheme.backgroundLight),
    TEXT(6, colorTheme -> colorTheme.text),
    TEXT_BLEND(7, colorTheme -> colorTheme.textBlend),
    TEXT_ON_PRIMARY(8, colorTheme -> colorTheme.textOnPrimary),
    TEXT_ON_PRIMARY_LIGHT(9, colorTheme -> colorTheme.textOnPrimaryLight),
    TEXT_ON_PRIMARY_DARK(10, colorTheme -> colorTheme.textOnPrimaryDark);

    public final int xmlValue;
    public final Function<ColorTheme, Integer> colorMap;
    ColorThemeXml(int xmlValue, Function<ColorTheme, Integer> colorMap) {
        this.xmlValue = xmlValue;
        this.colorMap = colorMap;
    }

    /**
     * Retrieves the corresponding ColorThemeXml enum from its XML value.
     */
    public static ColorThemeXml getColorThemeXmlFromValue(int xmlValue) {
        for (ColorThemeXml colorThemeXml : ColorThemeXml.values()) {
            if (colorThemeXml.xmlValue == xmlValue) {
                return colorThemeXml;
            }
        }
        return TRANSPARENT;
    }
}
