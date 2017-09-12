package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;

public class EndGameScreen extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_MOVES = "extra_moves";
    public static final String EXTRA_SCRAMBLES_EARNED = "extra_scrambles_earned";
    public static final String EXTRA_SCRAMBLES_USED = "extra_scrambles_used";
    public static final String EXTRA_LEVEL = "extra_level";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    private static final int[] UNIMPORTANT_TEXT_VIEW_IDS = {
            R.id.endGameScreenGlobalTitle,
            R.id.endGameScreenGlobalSubtitle,
            R.id.endGameScreenLevelTitle,
            R.id.endGameScreenScoreTitle,
            R.id.endGameScreenScramblesEarnedTitle,
            R.id.endGameScreenScramblesUsedTitle,
            R.id.endGameScreenWordsTitle,

            R.id.endGameScreenActionMainMenu,
            R.id.endGameScreenActionPlayAgain
    };

    private static final String STAT_FORMAT_STRING = "%s";

    private WordDropperApplication app;

    private LinearLayout root;
    private LinearLayout actionsWrapper;

    private TextView score;
    private TextView level;
    private TextView scramblesUsed;
    private TextView scramblesEarned;
    private TextView words;
    private View actionsSplitter;
    private String difficulty;

    private final ArrayList<TextView> unimportantTextViews;

    public EndGameScreen() {
        unimportantTextViews = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game_screen);
        app = (WordDropperApplication) getApplicationContext();

        root = (LinearLayout) findViewById(R.id.endGameScreenRoot);
        actionsWrapper = (LinearLayout) root.findViewById(R.id.endGameScreenActionsWrapper);
        actionsSplitter = actionsWrapper.findViewById(R.id.endGameScreenActionsSplitter);

        // Populate data
        // TODO: Show difficulty somewhere.
        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        score = (TextView) root.findViewById(R.id.endGameScreenScore);
        level = (TextView) root.findViewById(R.id.endGameScreenLevel);
        scramblesUsed = (TextView) root.findViewById(R.id.endGameScreenScramblesUsed);
        scramblesEarned = (TextView) root.findViewById(R.id.endGameScreenScramblesEarned);
        words = (TextView) root.findViewById(R.id.endGameScreenWords);

        animateValue(EXTRA_LEVEL, level, 0);
        animateValue(EXTRA_SCORE, score, 50);
        animateValue(EXTRA_SCRAMBLES_EARNED, scramblesEarned, 50);
        animateValue(EXTRA_SCRAMBLES_USED, scramblesUsed, 100);
        animateValue(EXTRA_MOVES, words, 150);

        // Find all other textviews.
        unimportantTextViews.clear();
        unimportantTextViews.addAll(findAllById(UNIMPORTANT_TEXT_VIEW_IDS));
    }

    @Override
    protected void onResume() {
        super.onResume();

        root.setBackgroundColor(app.getColorTheme().background);
        actionsWrapper.setBackgroundColor(app.getColorTheme().backgroundLight);
        actionsSplitter.setBackgroundColor(app.getColorTheme().background);
        ColorTheme.set(TextView::setTextColor, app.getColorTheme().text,
                score, level, scramblesEarned, scramblesUsed, words);
        ColorTheme.set(TextView::setTextColor, app.getColorTheme().text,
                unimportantTextViews.toArray(new TextView[unimportantTextViews.size()]));
    }

    private ArrayList<TextView> findAllById(int... ids) {
        ArrayList<TextView> result = new ArrayList<>();
        for (int id : ids) {
            result.add((TextView) root.findViewById(id));
        }
        return result;
    }

    private void animateValue(String intentExtraName, TextView target, int delay) {
        ValueAnimator animator =
                ValueAnimator.ofInt(0, getIntent().getIntExtra(intentExtraName, 0));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(getResources().getInteger(R.integer.animation_durationLag));
        animator.addUpdateListener(animation -> target.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));

        if (delay > 0) {
            (new Handler()).postDelayed(animator::start, delay);
        } else {
            animator.start();
        }
    }

    @VisibleForXML
    public void onActionPlayAgainClick(View v) {
        Intent gameScreenIntent = new Intent(this, GameScreen.class);
        gameScreenIntent.putExtra(GameScreen.EXTRA_DIFFICULTY, difficulty);

        startActivity(gameScreenIntent);
    }

    @VisibleForXML
    public void onActionMainMenuClick(View v) {
        startActivity(new Intent(this, MainMenuScreen.class));
    }
}
