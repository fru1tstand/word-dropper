package me.fru1t.worddropper.settings;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Presets for colors within the game.
 */
public enum ColorTheme {
    ORANGE("Orange",
            Color.parseColor("#ff9800"),
            Color.parseColor("#c66900"),
            Color.parseColor("#ffc947"),
            Color.parseColor("#e1e2e1"),
            Color.WHITE,
            Color.BLACK,
            Color.parseColor("#aaaaaa"),
            Color.BLACK,
            Color.BLACK,
            Color.BLACK
    ),

    PURPLE("Purple", // name
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
            Color.parseColor("#eeeeee"), // textBlend
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
