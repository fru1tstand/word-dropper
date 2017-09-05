package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropper;
import me.fru1t.worddropper.settings.ColorTheme;

public class EndGameScreen extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_MOVES = "extra_moves";
    public static final String EXTRA_SCRAMBLES_EARNED = "extra_scrambles_earned";
    public static final String EXTRA_SCRAMBLES_USED = "extra_scrambles_used";
    public static final String EXTRA_LEVEL = "extra_level";

    private static final int ANIMATION_DURATION_STATS = 1100;
    private static final String STAT_FORMAT_STRING = "%s";

    private LinearLayout root;
    private TextView score;
    private TextView level;
    private TextView scramblesUsed;
    private TextView scramblesEarned;
    private TextView words;

    @Override
    protected void onResume() {
        super.onResume();

        root.setBackgroundColor(WordDropper.colorTheme.background);
        ColorTheme.set(TextView::setTextColor, WordDropper.colorTheme.text,
                score, level, scramblesEarned, scramblesUsed, words);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game_screen);

        root = (LinearLayout) findViewById(R.id.endGameScreenRoot);

        // Populate data
        score = (TextView) root.findViewById(R.id.endGameScreenScore);
        level = (TextView) root.findViewById(R.id.endGameScreenLevel);
        scramblesUsed = (TextView) root.findViewById(R.id.endGameScreenScramblesUsed);
        scramblesEarned = (TextView) root.findViewById(R.id.endGameScreenScramblesEarned);
        words = (TextView) root.findViewById(R.id.endGameScreenWords);

        ValueAnimator scoreAnimator =
                ValueAnimator.ofInt(0, getIntent().getIntExtra(EXTRA_SCORE, 0));
        ValueAnimator levelAnimator =
                ValueAnimator.ofInt(0, getIntent().getIntExtra(EXTRA_LEVEL, 0));
        ValueAnimator scramblesUsedAnimator =
                ValueAnimator.ofInt(0, getIntent().getIntExtra(EXTRA_SCRAMBLES_USED, 0));
        ValueAnimator scramblesEarnedAnimator =
                ValueAnimator.ofInt(0, getIntent().getIntExtra(EXTRA_SCRAMBLES_EARNED, 0));
        ValueAnimator wordsAnimator =
                ValueAnimator.ofInt(0, getIntent().getIntExtra(EXTRA_MOVES, 0));

        scoreAnimator.setDuration(ANIMATION_DURATION_STATS);
        levelAnimator.setDuration(ANIMATION_DURATION_STATS);
        scramblesUsedAnimator.setDuration(ANIMATION_DURATION_STATS);
        scramblesEarnedAnimator.setDuration(ANIMATION_DURATION_STATS);
        wordsAnimator.setDuration(ANIMATION_DURATION_STATS);

        scoreAnimator.addUpdateListener(animation -> score.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));
        levelAnimator.addUpdateListener(animation -> level.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));
        scramblesUsedAnimator.addUpdateListener(animation -> scramblesUsed.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));
        scramblesEarnedAnimator.addUpdateListener(animation -> scramblesEarned.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));
        wordsAnimator.addUpdateListener(animation -> words.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));

        levelAnimator.start();
        (new android.os.Handler()).postDelayed(scramblesEarnedAnimator::start, 50);
        (new android.os.Handler()).postDelayed(scoreAnimator::start, 50);
        (new android.os.Handler()).postDelayed(scramblesUsedAnimator::start, 100);
        (new android.os.Handler()).postDelayed(wordsAnimator::start, 150);
    }
}
