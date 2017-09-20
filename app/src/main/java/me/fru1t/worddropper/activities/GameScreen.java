package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.common.base.Strings;

import java.util.LinkedList;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.Difficulty;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;
import me.fru1t.worddropper.widget.MenuLayout;
import me.fru1t.worddropper.widget.TileBoard;
import me.fru1t.worddropper.widget.WrappingProgressBar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * TODO: Change footer buttons depending on the origin of startActivity.
 */
public class GameScreen extends AppCompatActivity implements ColorThemeEventHandler {
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    private static final int CHART_ELEMENTS = 30;
    private static final long NO_GAME = -1;

    private Difficulty difficulty;
    private int movesEarned;
    private int movesUsed;
    private int scramblesEarned;
    private int scramblesUsed;

    private WordDropperApplication app;
    private TileBoard tileBoard;
    private WrappingProgressBar progressBar;
    private MenuLayout pauseMenu;

    private TextView level;
    private TextView scrambles;
    private TextView movesLeft;
    private TextView activeWord;

    private int activeWordDefaultColor;
    private int activeWordActiveColor;
    private int activeWordHorizontalPadding;

    private BarChart wordHistoryChart;
    private final BarDataSet wordHistoryDataSet;
    private final LinkedList<BarEntry> wordHistoryDataList;

    private long gameId;

    public GameScreen() {
        gameId = NO_GAME;

        // Data backend setup for chart
        wordHistoryDataList = new LinkedList<>();
        wordHistoryDataSet = new BarDataSet(wordHistoryDataList, "");
        wordHistoryDataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler)
                -> value == 0 ? "" : (int) value + "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        app = (WordDropperApplication) getApplicationContext();

        // Fetch elements we need to poke around with
        tileBoard = (TileBoard) findViewById(R.id.gameScreenTileBoard);
        progressBar = (WrappingProgressBar) findViewById(R.id.gameScreenProgress);
        pauseMenu = (MenuLayout) findViewById(R.id.gameScreenPauseMenu);
        level = (TextView) findViewById(R.id.gameScreenHudStatLevel);
        scrambles = (TextView) findViewById(R.id.gameScreenHudStatScrambles);
        movesLeft = (TextView) findViewById(R.id.gameScreenHudStatMovesLeft);
        wordHistoryChart = (BarChart) findViewById(R.id.gameScreenHudChart);
        activeWord = (TextView) findViewById(R.id.gameScreenHudActiveWord);

        difficulty = Difficulty.valueOf(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        activeWordHorizontalPadding = (int)
                getResources().getDimension(R.dimen.gameScreen_hudCurrentWordHorizontalPadding);

        // Set up chart
        wordHistoryChart.setDrawBarShadow(false);
        wordHistoryChart.setDrawValueAboveBar(true);
        wordHistoryChart.getDescription().setEnabled(false);
        wordHistoryChart.setPinchZoom(false);
        wordHistoryChart.setDrawGridBackground(false);
        wordHistoryChart.setBackgroundColor(Color.TRANSPARENT);
        wordHistoryChart.setTouchEnabled(false);
        wordHistoryChart.setViewPortOffsets(0, 0, 0, 0);

        BarData data = new BarData(wordHistoryDataSet);
        wordHistoryChart.setData(data);
        wordHistoryChart.getLegend().setEnabled(false);
        wordHistoryChart.getAxisRight().setEnabled(false);

        XAxis xAxis = wordHistoryChart.getXAxis();
        xAxis.setEnabled(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis yAxis = wordHistoryChart.getAxisLeft();
        yAxis.setEnabled(false);
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);

        // Set up progress bar
        progressBar.setNextMaximumFunction(wraps -> {
            if (wraps < 1) {
                return 80;
            }
            return (int) (80 * Math.pow(1.10409, wraps));
        });

        // Set up tile board listeners
        tileBoard.setEventHandler((changeEventType, word) -> {
            switch (changeEventType) {
                case CHANGE:
                    setActiveWord(word);
                    break;

                case SUCCESSFUL_SUBMIT:
                    int wordValue = app.getDictionary().getWordValue(word);
                    progressBar.animateAddProgress(wordValue);
                    ++movesUsed;

                    // Use up a move
                    if (difficulty.isWordAverageEnabled()) {
                        if (movesUsed >= movesEarned) {
                            tileBoard.setEnableTouching(false);
                        }
                        movesLeft.setText(getString(R.string.integer, movesEarned - movesUsed));
                    }

                    // Update hud
                    setActiveWord("");
                    addWordToGraph(word, wordValue);

                    // Add to database
                    app.getDatabaseUtils().addGameMove(
                            gameId, word, wordValue, progressBar.getTotal(),
                            tileBoard.getBoardState());
                    break;

                case FAILED_SUBMIT:
                    // Do nothing.
                    break;
            }
        });

        // On level up
        progressBar.setOnWrapEventListener((wraps, newMax) -> {
            int currentLevel = wraps + 1;
            level.setText(getString(R.string.integer, currentLevel));

            if (difficulty.isScramblingAllowed()
                    && !difficulty.isScramblingUnlimited()
                    && currentLevel % difficulty.levelsBeforeScramblePowerUp == 0) {
                ++scramblesEarned;
                scrambles.setText(getString(R.string.integer, scramblesEarned - scramblesUsed));
            }

            if (difficulty.isWordAverageEnabled()) {
                int movesToAdd = (int) Math.round(1.0 * newMax / difficulty.wordPointAverage);

                ValueAnimator va = ValueAnimator.ofInt(
                        movesEarned - movesUsed, movesEarned + movesToAdd - movesUsed);
                va.setInterpolator(new AccelerateDecelerateInterpolator());
                va.setDuration(getResources().getInteger(R.integer.animation_durationEffect));
                va.addUpdateListener(animation -> movesLeft.setText(
                        getString(R.string.integer, (int) animation.getAnimatedValue())));
                va.start();

                movesEarned += movesToAdd;

                // Edge case where the user ran out of moves upon levelling up.
                if (!tileBoard.isEnableTouching()) {
                    tileBoard.setEnableTouching(true);
                }
            }

            // Update database
            app.getDatabaseUtils().updateGame(gameId, update -> {
                update.put(Game.COLUMN_LEVEL, currentLevel);
                update.put(Game.COLUMN_MOVES_EARNED, movesEarned);
                update.put(Game.COLUMN_SCRAMBLES_EARNED, scramblesEarned);
                update.put(Game.COLUMN_BOARD_STATE, tileBoard.getBoardState());
            });
        });
        progressBar.setOnAnimateAddEndEventListener(() -> {
            if (movesUsed < movesEarned || !difficulty.isWordAverageEnabled()) {
                return;
            }

            endGame();
        });

        // Pause menu comes last so it's onWrapEventListener top
        pauseMenu.setOnHideListener(() -> pauseMenu.setVisibility(View.GONE));
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuSaveAndQuit, false, () -> {});
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuSettings, true, () -> {});
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuRestartOption, true, this::startGame);
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuEndGameOption, false, this::endGame);
        if (app.isDebugging()) {
            pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuDebugSubmitWords, true, () -> {
                for (int i = 0; i < 40; i++) {
                    (new android.os.Handler()).postDelayed(() -> runOnUiThread(() ->
                            tileBoard.getEventHandler().onChange(
                                    TileBoard.ChangeEventType.SUCCESSFUL_SUBMIT,
                                    app.getDictionary()
                                            .getRandomWord(difficulty.wordPointAverage))),
                            i * 100);
                }
            });

        }
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuCloseMenuOption, false,
                pauseMenu::hide);

        // Post completion
        app.addColorThemeEventHandler(this);
        startGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeColorThemeEventHandler(this);
    }

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        activeWordActiveColor = colorTheme.primary;
        activeWordDefaultColor = colorTheme.text;
        wordHistoryChart.getData().setValueTextColor(colorTheme.textBlend);
        wordHistoryDataSet.setColor(colorTheme.textBlend);

        setActiveWord(activeWord.getText().toString());
        wordHistoryChart.invalidate();
    }

    private void startGame() {
        progressBar.reset();
        int currentLevel = progressBar.getWraps() + 1;

        // Set initial values
        scramblesUsed = 0;
        scramblesEarned = 0;
        movesEarned = (int) Math.round(1.0 * progressBar.getMax() / difficulty.wordPointAverage);
        movesUsed = 0;

        // Update hud
        if (!difficulty.isScramblingAllowed()) {
            scrambles.setText(getString(R.string.integer, 0));
        } else if (difficulty.isScramblingUnlimited()) {
            scrambles.setText(getResources().getString(R.string.gameScreen_infiniteValue));
        } else {
            scrambles.setText(getString(R.string.integer, scramblesEarned - scramblesUsed));
        }

        if (!difficulty.isWordAverageEnabled()) {
            movesLeft.setText(getResources().getString(R.string.gameScreen_infiniteValue));
        } else {
            movesLeft.setText(getString(R.string.integer, movesEarned - movesUsed));
        }

        level.setText(getString(R.string.integer, currentLevel));
        clearGraph();

        // Update tile board
        tileBoard.scramble();

        // Get game id from database
        gameId = app.getDatabaseUtils().startGame(difficulty, tileBoard.getBoardState(),
                movesEarned, scramblesEarned);
    }

    private void endGame() {
        // End game in database
        app.getDatabaseUtils().endGame(gameId);

        // Open end game screen
        Intent endGameIntent = new Intent(this, EndGameScreen.class);
        endGameIntent.putExtra(EndGameScreen.EXTRA_GAME_ID, gameId);
        startActivity(endGameIntent);
        finish();
    }

    private void addWordToGraph(String word, int value) {
        BarData data = wordHistoryChart.getData();
        data.addEntry(new BarEntry(wordHistoryDataList.peekLast().getX() + 1, value, word), 0);
        data.removeEntry(wordHistoryDataList.peekFirst(), 0);
        data.notifyDataChanged();
        wordHistoryChart.notifyDataSetChanged();
        wordHistoryChart.setVisibleXRangeMaximum(CHART_ELEMENTS);
        wordHistoryChart.moveViewToX(data.getEntryCount());
    }

    private void clearGraph() {
        wordHistoryDataList.clear();
        for (int i = 0; i < CHART_ELEMENTS; i++) {
            wordHistoryDataList.add(new BarEntry(i, 0, ""));
        }
        wordHistoryChart.getData().notifyDataChanged();
        wordHistoryChart.notifyDataSetChanged();
        wordHistoryChart.invalidate();
    }

    private void setActiveWord(String s) {
        if (Strings.isNullOrEmpty(s)) {
            activeWord.setText("");
            activeWord.setTextColor(activeWordDefaultColor);
            activeWord.setPadding(0, activeWord.getPaddingTop(), 0, activeWord.getPaddingBottom());
            return;
        }

        if (app.getDictionary().isWord(s)) {
            s += " (" + app.getDictionary().getWordValue(s) + ")";
            activeWord.setTextColor(activeWordActiveColor);
        } else {
            activeWord.setTextColor(activeWordDefaultColor);
        }

        s = s.substring(0, 1).toUpperCase() + s.substring(1);
        activeWord.setText(s);
        activeWord.setPadding(activeWordHorizontalPadding, activeWord.getPaddingTop(),
                activeWordHorizontalPadding, activeWord.getPaddingBottom());
    }

    /**
     * Shows the pause menu if it's not already open.
     */
    @VisibleForXML
    public void onGraphicClick(View v) {
        if (pauseMenu.isOpen()) {
            return;
        }

        pauseMenu.setVisibility(View.VISIBLE);
        pauseMenu.show();
    }

    /**
     * Tries to use a scramble if there's one available.
     */
    @VisibleForXML
    public void onScramblesClick(View v) {
        if (!difficulty.isScramblingAllowed()) {
            return;
        }

        if (difficulty.isScramblingUnlimited()) {
            tileBoard.scramble();
            scramblesUsed++;
            app.getDatabaseUtils().updateGame(gameId,
                    update -> update.put(Game.COLUMN_SCRAMBLES_USED, scramblesUsed));
            return;
        }

        if (scramblesUsed >= scramblesEarned) {
            return;
        }

        scramblesUsed++;
        tileBoard.scramble();
        scrambles.setText(getString(R.string.integer, scramblesEarned - scramblesUsed));
        app.getDatabaseUtils().updateGame(gameId,
                update -> update.put(Game.COLUMN_SCRAMBLES_USED, scramblesUsed));
    }

    /**
     * TODO: make this action do something
     */
    @VisibleForXML
    public void onLevelClick(View v) {
        System.out.println("On level click");
    }

    /**
     * TODO: Make this action do something.
     */
    @VisibleForXML
    public void onMovesLeftClick(View v) {
        System.out.println("moves left click");
    }
}
