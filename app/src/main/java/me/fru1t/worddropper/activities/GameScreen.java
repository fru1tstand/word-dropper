package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import lombok.Setter;
import me.fru1t.worddropper.WordDropper;
import me.fru1t.worddropper.settings.Difficulty;
import me.fru1t.worddropper.widget.gameboard.GameBoardHUD;
import me.fru1t.worddropper.widget.WrappingProgressBar;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.widget.TileBoard;
import me.fru1t.worddropper.widget.gameboard.PauseMenu;
import me.fru1t.worddropper.widget.tileboard.Tile;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameScreen extends AppCompatActivity {
    private static final int HUD_HEIGHT = 650;
    private static final int PROGRESS_HEIGHT = 40;

    private static final int ANIMATION_DURATION_MOVES = 650;

    private @Setter Difficulty difficulty;
    private int movesEarned;
    private int movesUsed;
    private int scramblesEarned;
    private int scramblesUsed;

    private @Nullable FrameLayout root;
    private @Nullable TileBoard tileBoard;
    private @Nullable WrappingProgressBar progressBar;
    private @Nullable GameBoardHUD hud;
    private @Nullable PauseMenu pauseMenu;

    public GameScreen() {
        difficulty = Difficulty.MEDIUM;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        if (tileBoard != null) {
            tileBoard.setX(0);
            tileBoard.setY(HUD_HEIGHT);
            tileBoard.getLayoutParams().height = screenSize.y - HUD_HEIGHT - PROGRESS_HEIGHT;
            tileBoard.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            tileBoard.setBackgroundColor(WordDropper.colorTheme.background);
            tileBoard.forEachTile(Tile::release); // Essentially, updateColors.
        }

        if (progressBar != null) {
            progressBar.setX(0);
            progressBar.setY(screenSize.y - PROGRESS_HEIGHT);
            progressBar.getLayoutParams().height = PROGRESS_HEIGHT;
            progressBar.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            progressBar.updateColors();
        }

        if (hud != null) {
            hud.setX(0);
            hud.setY(0);
            hud.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            hud.getLayoutParams().height = HUD_HEIGHT;
            hud.updateColors();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_screen);

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        root = (FrameLayout) findViewById(R.id.gameBoardRoot);

        // Create Pause Menu
        pauseMenu = new PauseMenu(this);

        // Create tile board
        tileBoard = new TileBoard(this);
        assert root != null;
        root.addView(tileBoard);

        // Create progress bar
        progressBar = new WrappingProgressBar(this);
        root.addView(progressBar);
        progressBar.setNextMaximumFunction(wraps -> {
            if (wraps < 1) {
                return 80;
            }
            return (int) (80 * Math.pow(1.10409, wraps));
        });

        // Creates stats
        hud = new GameBoardHUD(this);
        root.addView(hud);
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

            @Override
            public void onCurrentWordClick() {
                root.addView(pauseMenu);
                pauseMenu.setX(0);
                pauseMenu.setY(0);
                pauseMenu.getLayoutParams().height = PauseMenu.HEIGHT;
                pauseMenu.getLayoutParams().width = PauseMenu.WIDTH;
            }
        });

        // Post-creation events
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

        progressBar.setEventWrappingProgressBarEventListener(
                new WrappingProgressBar.WrappingProgressBarEventListener() {
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

                endGame();
            }
        });

        pauseMenu.setEventListener(new PauseMenu.PauseMenuEventListener() {
            @Override
            public void onEndGame() {
                endGame();
            }

            @Override
            public void onClose() {
                root.removeView(pauseMenu);
            }
        });

        // Start game
        int currentLevel = progressBar.getWraps() + 1;
        movesEarned = (int) Math.round(1.0 * progressBar.getMax() / difficulty.wordPointAverage);
        hud.setScramblesRemaining((scramblesEarned - scramblesUsed) + "");
        hud.setMovesRemaining((movesEarned - movesUsed) + "");
        hud.setCurrentLevel(currentLevel + "");
    }

    private void endGame() {
        assert progressBar != null;
        Intent endGameIntent = new Intent(GameScreen.this, EndGameScreen.class);
        endGameIntent.putExtra(EndGameScreen.EXTRA_LEVEL, progressBar.getWraps() + 1);
        endGameIntent.putExtra(EndGameScreen.EXTRA_SCRAMBLES_USED, scramblesUsed);
        endGameIntent.putExtra(EndGameScreen.EXTRA_SCRAMBLES_EARNED, scramblesEarned);
        endGameIntent.putExtra(EndGameScreen.EXTRA_MOVES, movesUsed);
        endGameIntent.putExtra(EndGameScreen.EXTRA_SCORE, progressBar.getTotal());
        startActivity(endGameIntent);
    }
}
