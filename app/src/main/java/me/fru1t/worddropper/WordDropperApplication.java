package me.fru1t.worddropper;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import java.util.HashSet;

import lombok.Getter;
import me.fru1t.worddropper.database.DatabaseUtils;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.Dictionary;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * Settings and global variables (*gasp*) loaded onWrapEventListener startup.
 */
public class WordDropperApplication extends Application {

    public static final String LOG_TAG = "WordDropper";

    private final HashSet<ColorThemeEventHandler> colorThemeEventHandlers;

    private ColorTheme colorTheme;

    private @Getter SharedPreferences sharedPreferences;
    private @Getter Dictionary dictionary;
    private @Getter DatabaseUtils databaseUtils;

    public WordDropperApplication() {
        colorThemeEventHandlers = new HashSet<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up preferences
        sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.app_sharedPreferencesFileName),
                MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(
                (sharedPreferences1, key) -> updateFromSettings());
        updateFromSettings();

        // Load external heavy calls
        databaseUtils = new DatabaseUtils(getApplicationContext());
        dictionary = new Dictionary(this);
    }

    public boolean isDebugging() {
        return getResources().getBoolean(R.bool.app_debug);
    }

    /**
     * Retrieves a string preference given a key and default value.
     * @param stringId The string id within a resource file.
     * @param defaultValue The default value the setting should be if it doesn't exist.
     * @return The string preference.
     */
    public String getStringPreference(@StringRes int stringId, String defaultValue) {
        return sharedPreferences.getString(getResources().getString(stringId), defaultValue);
    }

    /**
     * Adds a color theme event handler, triggering the
     * {@link ColorThemeEventHandler#onColorThemeChange(ColorTheme)} upon successful registration.
     */
    public void addColorThemeEventHandler(ColorThemeEventHandler handler) {
        if (colorThemeEventHandlers.add(handler)) {
            handler.onColorThemeChange(colorTheme);
        }
    }

    public void removeColorThemeEventHandler(ColorThemeEventHandler handler) {
        colorThemeEventHandlers.remove(handler);
    }

    private void updateFromSettings() {
        ColorTheme newTheme = ColorTheme.valueOf(
                getStringPreference(R.string.pref_colorTheme, ColorTheme.INVERSE_ORANGE.name()));
        if (newTheme != colorTheme) {
            colorTheme = newTheme;
            colorThemeEventHandlers.forEach(handler -> handler.onColorThemeChange(newTheme));
        }
    }
}
