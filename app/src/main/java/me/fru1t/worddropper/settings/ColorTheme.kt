package me.fru1t.worddropper.settings

import android.graphics.Color

/** Presets for colors within the game. */
enum class ColorTheme(
        val displayName: String,
        val primary: Int,
        val primaryDark: Int,
        val primaryLight: Int,
        val background: Int,
        val backgroundLight: Int,
        val text: Int,
        val textBlend: Int,
        val textOnPrimary: Int,
        val textOnPrimaryLight: Int,
        val textOnPrimaryDark: Int) {
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
            Color.WHITE, // textOnPrimaryLight
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
    )

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
}
