package me.fru1t.worddropper.settings;

/**
 * Presets for game difficulty.
 */
public enum Difficulty {
    EASY("Easy", 8, 5),
    MEDIUM("Medium", 13, 6),
    HARD("Hard", 16, 8),
    EXPERT("Expert", 19, Integer.MAX_VALUE),

    ZEN("Zen", -1, -2);

    private static final int SCRAMBLES_UNLIMITED = -2;
    private static final int SCRAMBLES_DISABLED = -1;
    private static final int WORD_POINTS_DISABLED = -1;

    public final String displayName;
    public final int wordPointAverage;
    public final int levelsBeforeScramblePowerUp;

    Difficulty(String displayName, int wordPointAverage, int levelsBeforeScramblePowerUp) {
        this.displayName = displayName;
        this.wordPointAverage = wordPointAverage;
        this.levelsBeforeScramblePowerUp = levelsBeforeScramblePowerUp;
    }

    public boolean isScramblingAllowed() {
        return levelsBeforeScramblePowerUp != SCRAMBLES_DISABLED;
    }

    public boolean isScramblingUnlimited() {
        return levelsBeforeScramblePowerUp == SCRAMBLES_UNLIMITED;
    }

    public boolean isWordAverageEnabled() {
        return wordPointAverage != WORD_POINTS_DISABLED;
    }
}
