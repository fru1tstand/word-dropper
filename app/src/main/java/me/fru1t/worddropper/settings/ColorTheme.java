package me.fru1t.worddropper.settings;

import android.graphics.Color;

import java.util.function.BiConsumer;

/**
 * Presets for colors within the game.
 */
public enum ColorTheme {
    ORANGE(
            "Orange", // name
            Color.parseColor("#ff9800"),
            Color.parseColor("#c66900"),
            Color.parseColor("#ffc947"),
            Color.parseColor("#e1e2e1"),
            Color.WHITE,
            Color.parseColor("#333333"), // text
            Color.parseColor("#aaaaaa"),
            Color.BLACK, // textOnPrimary
            Color.BLACK, // textOnPrimaryLight
            Color.BLACK // textOnPrimaryDark
    ),

    PURPLE(
            "Purple", // name
            Color.parseColor("#673ab7"), // primary
            Color.parseColor("#320b86"), // primaryDark
            Color.parseColor("#9a67ea"), // primaryLight
            Color.parseColor("#fafafa"), // background
            Color.parseColor("#9a67ea"), // backgroundLight
            Color.BLACK, // text
            Color.parseColor("#aaaaaa"), // textBlend
            Color.WHITE, // textOnPrimary
            Color.BLACK, // textOnPrimaryLight
            Color.WHITE // textOnPrimaryDark
    ),

    INVERSE_ORANGE(
            "Inverse Orange", // name
            Color.parseColor("#ff9800"), // primary
            Color.parseColor("#c66900"), // primaryDark
            Color.parseColor("#ffc947"), // primaryLight
            Color.parseColor("#303030"), // background
            Color.parseColor("#424242"), // backgroundLight
            Color.WHITE, // text
            Color.parseColor("#999999"), // textBlend
            Color.WHITE, // textOnPrimary
            Color.WHITE, // textOnPrimaryLight
            Color.WHITE // textOnPrimaryDark
    );

    /*
        "", // name
        Color.parseColor("#"), // primary
        Color.parseColor("#"), // primaryDark
        Color.parseColor("#"), // primaryLight
        Color.parseColor("#"), // background
        Color.parseColor("#"), // backgroundLight
        Color.parseColor("#"), // text
        Color.parseColor("#"), // textBlend
        Color.parseColor("#"), // textOnPrimary
        Color.parseColor("#"), // textOnPrimaryLight
        Color.parseColor("#") // textOnPrimaryDark
    */
    
    // Keep synced with attrs.xml
    private static final int ENUM_PRIMARY = 0;
    private static final int ENUM_PRIMARY_DARK = 1;
    private static final int ENUM_PRIMARY_LIGHT = 2;
    private static final int ENUM_BACKGROUND = 3;
    private static final int ENUM_BACKGROUND_LIGHT = 4;
    private static final int ENUM_TEXT = 5;
    private static final int ENUM_TEXT_BLEND = 6;
    private static final int ENUM_TEXT_ON_PRIMARY = 7;
    private static final int ENUM_TEXT_ON_PRIMARY_LIGHT = 8;
    private static final int ENUM_TEXT_ON_PRIMARY_DARK = 9;
    
    public final String displayName;
    public final int primary;
    public final int primaryDark;
    public final int primaryLight;
    public final int background;
    public final int backgroundLight;
    public final int text;
    public final int textBlend;
    public final int textOnPrimary;
    public final int textOnPrimaryLight;
    public final int textOnPrimaryDark;

    ColorTheme(String displayName, int primary, int primaryDark, int primaryLight,
               int background, int backgroundLight, int text, int textBlend, int textOnPrimary,
               int textOnPrimaryLight, int textOnPrimaryDark) {
        this.displayName = displayName;
        this.primary = primary;
        this.primaryDark = primaryDark;
        this.primaryLight = primaryLight;
        this.background = background;
        this.backgroundLight = backgroundLight;
        this.text = text;
        this.textBlend = textBlend;
        this.textOnPrimary = textOnPrimary;
        this.textOnPrimaryLight = textOnPrimaryLight;
        this.textOnPrimaryDark = textOnPrimaryDark;
    }

    /**
     * Retrieves the color associated to the enum value specified in attrs.xml. Quietly fails any
     * invalid enum values by returning the primary color.
     */
    public int getColorFromXmlEnum(int enumValue) {
        switch (enumValue) {
            case ENUM_PRIMARY_DARK:
                return primaryDark;
            case ENUM_PRIMARY_LIGHT:
                return primaryLight;
            case ENUM_BACKGROUND:
                return background;
            case ENUM_BACKGROUND_LIGHT:
                return backgroundLight;
            case ENUM_TEXT:
                return text;
            case ENUM_TEXT_BLEND:
                return textBlend;
            case ENUM_TEXT_ON_PRIMARY:
                return textOnPrimary;
            case ENUM_TEXT_ON_PRIMARY_LIGHT:
                return textOnPrimaryLight;
            case ENUM_TEXT_ON_PRIMARY_DARK:
                return textOnPrimaryDark;
            case ENUM_PRIMARY:
            default:
                return primary;
        }
    }

    /**
     * Performs an action, passing the given color to the given targets. For example, when setting
     * multiple TextViews' text color, one can do the following:
     * <code>ColorTheme.set(TextView::setTextColor, Color.WHITE, textView1, textView2...);</code>
     * @param action Usually passed as a method reference, but can be made arbitrarily. To pass as a
     *               method reference, the method given must only accept a single integer parameter.
     * @param color The color to pass the action.
     * @param targets The instances to target.
     * @param <T> The type of the target.
     */
    public static <T> void set(BiConsumer<T, Integer> action, int color, T... targets) {
        for (T target : targets) {
            action.accept(target, color);
        }
    }
}
