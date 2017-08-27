package me.fru1t.worddropper;

import android.graphics.Color;

import java.util.HashSet;

/**
 * Settings and global variables (*gasp*) loaded on startup.
 */
public class WordDropper {
    public static final HashSet<String> dictionary = new HashSet<>();

    public static final int COLOR_PRIMARY = Color.parseColor("#ff9800");
    public static final int COLOR_PRIMARY_DARK = Color.parseColor("#c66900");
    public static final int COLOR_PRIMARY_LIGHT = Color.parseColor("#ffc947");
    public static final int COLOR_BACKGROUND = Color.parseColor("#e1e2e1");
    public static final int COLOR_BACKGROUND_LIGHT = Color.WHITE;
    public static final int COLOR_TEXT = Color.BLACK;
}
