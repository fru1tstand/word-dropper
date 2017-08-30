package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import lombok.Setter;
import me.fru1t.worddropper.WordDropper;
import me.fru1t.worddropper.widget.gameboard.GameBoardHUD;
import me.fru1t.worddropper.widget.WrappingProgressBar;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.widget.TileBoard;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameScreen extends AppCompatActivity {
    private static final int HUD_HEIGHT = 650;
    private static final int PROGRESS_HEIGHT = 40;

    private static final int ANIMATION_DURATION_MOVES = 650;

    private @Setter WordDropper.Difficulty difficulty;
    private int movesRemaining;
    private int scramblesRemaining;

    public GameScreen() {
        difficulty = WordDropper.Difficulty.MEDIUM;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        FrameLayout root = (FrameLayout) findViewById(R.id.gameBoardRoot);

        // Create tile board
        TileBoard tileBoard = new TileBoard(this);
        root.addView(tileBoard);
        tileBoard.setX(0);
        tileBoard.setY(HUD_HEIGHT);
        tileBoard.getLayoutParams().height = screenSize.y - HUD_HEIGHT - PROGRESS_HEIGHT;
        tileBoard.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        tileBoard.setBackgroundColor(WordDropper.COLOR_BACKGROUND);
        tileBoard.forEachTile(tile -> {
            tile.setDefaultBackgroundColor(WordDropper.COLOR_BACKGROUND);
            tile.setActiveBackgroundColor(WordDropper.COLOR_PRIMARY);
            tile.getTextPaint().setColor(WordDropper.COLOR_TEXT);
            tile.getTextPaint().setTextSize(60);
        });

        // Create progress bar
        WrappingProgressBar progressBar = new WrappingProgressBar(this);
        root.addView(progressBar);
        progressBar.setX(0);
        progressBar.setY(screenSize.y - PROGRESS_HEIGHT);
        progressBar.getLayoutParams().height = PROGRESS_HEIGHT;
        progressBar.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        progressBar.getBackgroundColor().setColor(WordDropper.COLOR_BACKGROUND);
        progressBar.getProgressColor().setColor(WordDropper.COLOR_PRIMARY_LIGHT);
        progressBar.getProgressCalculatedColor().setColor(WordDropper.COLOR_PRIMARY_DARK);
        progressBar.getTextPaint().setColor(WordDropper.COLOR_TEXT);
        progressBar.getTextPaint().setTextSize(16);
        progressBar.getTextPaint().setTypeface(Typeface.DEFAULT);
        progressBar.setNextMaximumFunction(wraps -> {
            if (wraps < 2) {
                return 80;
            }
            return (long) (80 * Math.pow(1.10409, wraps - 1));
        });

        // Creates stats
        GameBoardHUD hud = new GameBoardHUD(this);
        root.addView(hud);
        hud.setX(0);
        hud.setY(0);
        hud.setBackgroundColor(WordDropper.COLOR_BACKGROUND);
        hud.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        hud.getLayoutParams().height = HUD_HEIGHT;
        hud.setDefaultTextColor(WordDropper.COLOR_TEXT);
        hud.setActiveTextColor(WordDropper.COLOR_PRIMARY);
        hud.getCurrentWordTextView().setTextSize(22);

        tileBoard.setEventHandler((changeEventType, string) -> {
            switch (changeEventType) {
                case CHANGE:
                    hud.setCurrentWordTextView(string);
                    break;
                case SUCCESSFUL_SUBMIT:
                    progressBar.animateAddProgress(WordDropper.getWordValue(string));
                    hud.setCurrentWordTextView(null);

                    // Use up a move
                    if (difficulty.isWordAverageEnabled()) {
                        --movesRemaining;
                        if (movesRemaining <= 0) {
                            tileBoard.setEnabled(false);
                        }
                    }
                    hud.setMovesRemaining(movesRemaining + "");
                    break;
                case FAILED_SUBMIT:
                    break;
            }
        });

        // Post-creation events
        progressBar.setEventWrappingProgressBarEventListener((wraps, newMax) -> {
            int currentLevel = wraps + 1;

            if (difficulty.isScramblingAllowed()
                    && currentLevel % difficulty.levelsBeforeScramblePowerUp == 0) {
                ++scramblesRemaining;
                hud.setScramblesRemaining(scramblesRemaining + "");
            }

            if (difficulty.isWordAverageEnabled()) {
                int newMovesRemaining =
                        (int) (movesRemaining + newMax / difficulty.wordPointAverage);
                ValueAnimator va = ValueAnimator.ofInt(movesRemaining, newMovesRemaining);
                va.setDuration(ANIMATION_DURATION_MOVES);
                va.addUpdateListener(
                        animation -> hud.setMovesRemaining(animation.getAnimatedValue() + ""));
                va.start();

                movesRemaining = newMovesRemaining;
                hud.setMovesRemaining(movesRemaining + "");
            }

            hud.setCurrentLevel(currentLevel + "");
        });

        // Start game
        int currentLevel = progressBar.getWraps() + 1;
        movesRemaining = currentLevel * difficulty.wordPointAverage;
        hud.setScramblesRemaining(scramblesRemaining + "");
        hud.setMovesRemaining(movesRemaining + "");
        hud.setCurrentLevel(currentLevel + "");
    }
}
