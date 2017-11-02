package me.fru1t.worddropper.settings

/** Presets for game difficulty. */
enum class Difficulty (
        val displayName: String,
        val wordPointAverage: Int,
        val levelsBeforeScramblePowerUp: Int) {
    EASY("Easy", 8, 5),
    MEDIUM("Medium", 13, 6),
    HARD("Hard", 16, 8),
    EXPERT("Expert", 19, Integer.MAX_VALUE),

    ZEN("Zen", -1, -2);

    fun isScramblingAllowed(): Boolean = levelsBeforeScramblePowerUp != SCRAMBLES_DISABLED
    fun isScramblingUnlimited(): Boolean = levelsBeforeScramblePowerUp == SCRAMBLES_UNLIMITED
    fun isWordAverageEnabled(): Boolean = wordPointAverage != WORD_POINTS_DISABLED

    companion object {
        private val SCRAMBLES_UNLIMITED = -2
        private val SCRAMBLES_DISABLED = -1
        private val WORD_POINTS_DISABLED = -1
    }
}
