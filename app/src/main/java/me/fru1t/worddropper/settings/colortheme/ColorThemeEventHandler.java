package me.fru1t.worddropper.settings.colortheme;

import me.fru1t.worddropper.settings.ColorTheme;

/**
 * Triggered when the color theme is changed.
 */
@FunctionalInterface
public interface ColorThemeEventHandler {
    void onColorThemeChange(ColorTheme colorTheme);
}
