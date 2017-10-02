package me.fru1t.worddropper.settings.colortheme;

import android.support.annotation.NonNull;

import me.fru1t.worddropper.settings.ColorTheme;

/**
 * Triggered when the color theme is changed.
 */
@FunctionalInterface
public interface ColorThemeEventHandler {
    void onColorThemeChange(@NonNull ColorTheme colorTheme);
}
