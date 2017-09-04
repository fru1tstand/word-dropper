package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
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
    private int movesEarned;
    private int movesUsed;
    private int scramblesEarned;
    private int scramblesUsed;

    public GameScreen() {
        difficulty = WordDropper.Difficulty.MEDIUM;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

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
            if (wraps < 1) {
                return 80;
            }
            return (int) (80 * Math.pow(1.10409, wraps));
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
        hud.setEventListener(new GameBoardHUD.GameBoardHUDEventListener() {
            @Override
            public void onLevelClick() {
                System.out.println("On level click");
            }

            @Override
            public void onScrambleClick() {
                if (!difficulty.isScramblingAllowed()) {
                    return;
                }

                if (difficulty.isScramblingUnlimited()) {
                    tileBoard.scramble();
                    scramblesUsed++;
                    return;
                }

                if (scramblesUsed >= scramblesEarned) {
                    return;
                }

                scramblesUsed++;
                tileBoard.scramble();
                hud.setScramblesRemaining(scramblesEarned - scramblesUsed + "");
            }

            @Override
            public void onMovesLeftClick() {
                System.out.println("moves left click");
            }
        });

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
                        ++movesUsed;
                        if (movesUsed >= movesEarned) {
                            tileBoard.setEnableTouching(false);
                        }
                    }
                    hud.setMovesRemaining((movesEarned - movesUsed) + "");
                    break;
                case FAILED_SUBMIT:
                    break;
            }
        });

        // Post-creation events
        progressBar.setEventWrappingProgressBarEventListener(new WrappingProgressBar.WrappingProgressBarEventListener() {
            @Override
            public void onWrap(int wraps, int newMax) {
                int currentLevel = wraps + 1;

                if (difficulty.isScramblingAllowed()
                        && currentLevel % difficulty.levelsBeforeScramblePowerUp == 0) {
                    ++scramblesEarned;
                    hud.setScramblesRemaining((scramblesEarned - scramblesUsed) + "");
                }

                if (difficulty.isWordAverageEnabled()) {
                    int movesToAdd = (int) (newMax / difficulty.wordPointAverage);

                    ValueAnimator va = ValueAnimator.ofInt(
                            movesEarned - movesUsed, movesEarned + movesToAdd - movesUsed);
                    va.setDuration(ANIMATION_DURATION_MOVES);
                    va.addUpdateListener(
                            animation -> hud.setMovesRemaining(animation.getAnimatedValue() + ""));
                    va.start();

                    movesEarned += movesToAdd;

                    // Edge case where the user ran out of moves upon levelling up.
                    if (!tileBoard.isEnableTouching()) {
                        tileBoard.setEnableTouching(true);
                    }
                }

                hud.setCurrentLevel(currentLevel + "");
            }

            @Override
            public void onAnimateAddEnd() {
                if (movesUsed < movesEarned) {
                    return;
                }

                Intent endGameIntent = new Intent(GameScreen.this, EndGameScreen.class);
                endGameIntent.putExtra(EndGameScreen.EXTRA_LEVEL, progressBar.getWraps() + 1);
                endGameIntent.putExtra(EndGameScreen.EXTRA_SCRAMBLES_USED, scramblesUsed);
                endGameIntent.putExtra(EndGameScreen.EXTRA_SCRAMBLES_EARNED, scramblesEarned);
                endGameIntent.putExtra(EndGameScreen.EXTRA_MOVES, movesEarned);
                endGameIntent.putExtra(EndGameScreen.EXTRA_SCORE, progressBar.getTotal());
                startActivity(endGameIntent);
            }
        });

        // Start game
        int currentLevel = progressBar.getWraps() + 1;
        movesEarned = (int) (progressBar.getMax() / difficulty.wordPointAverage);
        hud.setScramblesRemaining((scramblesEarned - scramblesUsed) + "");
        hud.setMovesRemaining((movesEarned - movesUsed) + "");
        hud.setCurrentLevel(currentLevel + "");
    }
}
