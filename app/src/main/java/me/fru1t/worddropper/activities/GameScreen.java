package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.layout.MenuLayout;
import me.fru1t.worddropper.settings.Difficulty;
import me.fru1t.worddropper.widget.TileBoard;
import me.fru1t.worddropper.widget.WrappingProgressBar;
import me.fru1t.worddropper.widget.gameboard.GameBoardHUD;
import me.fru1t.worddropper.widget.tileboard.Tile;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameScreen extends AppCompatActivity {
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    private WordDropperApplication app;

    private Difficulty difficulty;
    private int movesEarned;
    private int movesUsed;
    private int scramblesEarned;
    private int scramblesUsed;

    private FrameLayout root;
    private TileBoard tileBoard;
    private WrappingProgressBar progressBar;
    private GameBoardHUD hud;
    private MenuLayout pauseMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        app = (WordDropperApplication) getApplicationContext();

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        root = (FrameLayout) findViewById(R.id.gameBoardRoot);
        difficulty = Difficulty.valueOf(getIntent().getStringExtra(EXTRA_DIFFICULTY));

        // Create Pause Menu
        pauseMenu = (MenuLayout) getLayoutInflater().inflate(R.layout.layout_menu, root, false);

        // Create tile board
        tileBoard = new TileBoard(this);
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
        hud.setOnLevelClickEventListener(() -> System.out.println("On level click"));
        hud.setOnMovesLeftClickEventListener(() -> System.out.println("moves left click"));
        hud.setOnScrambleClickEventListener(() -> {
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
            hud.setScramblesRemaining(scramblesEarned - scramblesUsed);
        });
        hud.setOnCurrentWordClickEventListener(() -> {
            if (pauseMenu.isOpen()) {
                return;
            }

            pauseMenu.setVisibility(View.VISIBLE);
            pauseMenu.show();
        });

        // Post-creation
        tileBoard.setEventHandler((changeEventType, string) -> {
            switch (changeEventType) {
                case CHANGE:
                    hud.setCurrentWordTextView(string);
                    break;
                case SUCCESSFUL_SUBMIT:
                    progressBar.animateAddProgress(app.getDictionary().getWordValue(string));
                    hud.setCurrentWordTextView(null);
                    ++movesUsed;

                    // Use up a move
                    if (difficulty.isWordAverageEnabled()) {
                        if (movesUsed >= movesEarned) {
                            tileBoard.setEnableTouching(false);
                        }
                        hud.setMovesRemaining(movesEarned - movesUsed);
                    }
                    break;
                case FAILED_SUBMIT:
                    break;
            }
        });

        progressBar.setOnWrapEventListener((wraps, newMax) -> {
            int currentLevel = wraps + 1;

            if (difficulty.isScramblingAllowed()
                    && !difficulty.isScramblingUnlimited()
                    && currentLevel % difficulty.levelsBeforeScramblePowerUp == 0) {
                ++scramblesEarned;
                hud.setScramblesRemaining(scramblesEarned - scramblesUsed);
            }

            if (difficulty.isWordAverageEnabled()) {
                int movesToAdd = (int) Math.round(1.0 * newMax / difficulty.wordPointAverage);

                ValueAnimator va = ValueAnimator.ofInt(
                        movesEarned - movesUsed, movesEarned + movesToAdd - movesUsed);
                va.setInterpolator(new AccelerateDecelerateInterpolator());
                va.setDuration(getResources().getInteger(R.integer.animation_durationEffect));
                va.addUpdateListener(
                        animation -> hud.setMovesRemaining((Integer) animation.getAnimatedValue()));
                va.start();

                movesEarned += movesToAdd;

                // Edge case where the user ran out of moves upon levelling up.
                if (!tileBoard.isEnableTouching()) {
                    tileBoard.setEnableTouching(true);
                }
            }

            hud.setCurrentLevel(currentLevel + "");
        });
        progressBar.setOnAnimateAddEndEventListener(() -> {
            if (movesUsed < movesEarned || !difficulty.isWordAverageEnabled()) {
                return;
            }

            endGame();
        });

        // Pause menu comes last so it's onWrapEventListener top
        root.addView(pauseMenu);
        root.post(() -> {
            pauseMenu.getLayoutParams().width = root.getWidth();
            pauseMenu.getLayoutParams().height = root.getHeight();
        });
        pauseMenu.setVisibility(View.GONE);
        pauseMenu.setOnHideListener(() -> pauseMenu.setVisibility(View.GONE));

        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuSaveAndQuit, false, () -> {});
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuSettings, true, () -> {});
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuRestartOption, true, this::restart);
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuEndGameOption, false, this::endGame);

        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuCloseMenuOption, false,
                pauseMenu::hide);

        root.post(this::restart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int hudHeight = (int) getResources().getDimension(R.dimen.gameScreen_hudHeight);
        int progressHeight = (int) getResources().getDimension(R.dimen.gameScreen_progressHeight);

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        if (tileBoard != null) {
            tileBoard.setY(hudHeight);
            tileBoard.getLayoutParams().height = screenSize.y - hudHeight - progressHeight;
            tileBoard.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            tileBoard.setBackgroundColor(app.getColorTheme().background);
            tileBoard.forEachTile(Tile::release); // Essentially, updateColors.
        }

        if (progressBar != null) {
            progressBar.setY(screenSize.y - progressHeight);
            progressBar.getLayoutParams().height = progressHeight;
            progressBar.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            progressBar.updateColors();
        }

        if (hud != null) {
            hud.setX(0);
            hud.setY(0);
            hud.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            hud.getLayoutParams().height = hudHeight;
            hud.updateColors();
        }
    }

    private void restart() {
        progressBar.reset();
        int currentLevel = progressBar.getWraps() + 1;

        // Set initial values
        scramblesUsed = 0;
        scramblesEarned = 0;
        movesEarned = (int) Math.round(1.0 * progressBar.getMax() / difficulty.wordPointAverage);
        movesUsed = 0;

        // Update hud
        if (!difficulty.isScramblingAllowed()) {
            hud.setScramblesRemaining(0);
        } else if (difficulty.isScramblingUnlimited()) {
            hud.setScramblesRemaining(getResources().getString(R.string.gameScreen_infiniteValue));
        } else {
            hud.setScramblesRemaining(scramblesEarned - scramblesUsed);
        }

        if (!difficulty.isWordAverageEnabled()) {
            hud.setMovesRemaining(getResources().getString(R.string.gameScreen_infiniteValue));
        } else {
            hud.setMovesRemaining(movesEarned - movesUsed);
        }

        hud.setCurrentLevel(currentLevel);

        // Update tile board
        tileBoard.scramble();
    }

    private void endGame() {
        Intent endGameIntent = new Intent(GameScreen.this, EndGameScreen.class);
        endGameIntent.putExtra(EndGameScreen.EXTRA_LEVEL, progressBar.getWraps() + 1);
        endGameIntent.putExtra(EndGameScreen.EXTRA_SCRAMBLES_USED, scramblesUsed);
        endGameIntent.putExtra(EndGameScreen.EXTRA_SCRAMBLES_EARNED, scramblesEarned);
        endGameIntent.putExtra(EndGameScreen.EXTRA_MOVES, movesUsed);
        endGameIntent.putExtra(EndGameScreen.EXTRA_SCORE, progressBar.getTotal());
        endGameIntent.putExtra(EndGameScreen.EXTRA_DIFFICULTY, difficulty.name());

        startActivity(endGameIntent);
    }
}
