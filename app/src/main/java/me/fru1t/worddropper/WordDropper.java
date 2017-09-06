package me.fru1t.worddropper;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.util.HashSet;

import me.fru1t.worddropper.settings.ColorTheme;

/**
 * Settings and global variables (*gasp*) loaded on startup.
 */
public class WordDropper {
    private enum LetterValue {
        A(1), B(3), C(3), D(2), E(1), F(4), G(2), H(4), I(1), J(8), K(5), L(1), M(3), N(1), O(1),
        P(3), Q(10), R(1), S(1), T(1), U(1), V(4), W(4), X(8), Y(4), Z(10);

        public final int value;

        LetterValue(int value) {
            this.value = value;
        }
    }
    


    public static final boolean DEBUG = false;


    public static final HashSet<String> dictionary = new HashSet<>();
    public static ColorTheme colorTheme = ColorTheme.INVERSE_ORANGE;


    /**
     * Checks if the given string is a word or not. This method does no sanitization. Make sure
     * incoming strings are lowercase.
     */
    public static boolean isWord(String s) {
        return DEBUG || dictionary.contains(s);
    }

    /**
     * Determines the point value of a given string.
     */
    public static int getWordValue(@Nullable String string) {
        if (Strings.isNullOrEmpty(string)) {
            return 0;
        }

        int result = 0;
        for (char c : string.toUpperCase().toCharArray()) {
            result += LetterValue.valueOf(c + "").value;
        }
        if (string.length() > 3) {
            result *= 1.0 + ((0.3 + 0.2 * (string.length() - 3)) * (string.length() - 3));
        }
        return result;
    }
}
