package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.function.Consumer;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.android.database.Row;
import me.fru1t.android.widget.ViewUtils;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.database.tables.GameWord;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.Difficulty;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

public class EndGameScreen extends AppCompatActivity implements ColorThemeEventHandler {
    private static class GraphAction {
        Chart chart;
        TextView button;
    }

    public static final String EXTRA_GAME_ID = "extra_game_id"; // Long

    private WordDropperApplication app;
    private long gameId;
    private String difficulty;
    private ColorTheme activeColorTheme;
    private final SparseArray<GraphAction> memoizedGraphs;
    private final ArrayList<TextView> graphButtons;
    private Consumer<View> activeGraphFunction;

    private FrameLayout graphWrapper;
    private LinearLayout graphButtonsWrapper;

    public EndGameScreen() {
        memoizedGraphs = new SparseArray<>();
        graphButtons = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game_screen);
        app = (WordDropperApplication) getApplicationContext();
        gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);

        graphWrapper = (FrameLayout) findViewById(R.id.endGameScreenGraphWrapper);
        graphButtonsWrapper = (LinearLayout) findViewById(R.id.endGameScreenGraphButtonsWrapper);

        // Fetch data
        Row gameData = app.getDatabaseUtils().getRowFromId(
                Game.TABLE_NAME,
                gameId,
                new String[] { Game.COLUMN_UNIX_START, Game.COLUMN_STATUS, Game.COLUMN_DIFFICULTY,
                        Game.COLUMN_BOARD_STATE, Game.COLUMN_MOVES_EARNED, Game.COLUMN_BOARD_STATE,
                        Game.COLUMN_MOVES_EARNED, Game.COLUMN_SCRAMBLES_USED,
                        Game.COLUMN_SCRAMBLES_EARNED, Game.COLUMN_LEVEL });
        if (gameData == null) {
            Toast.makeText(this, R.string.app_gameNotFoundError, Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }

        // Yes. This can be done in the above query. But a) readability, and b) low overhead.
        int[] extraGameData = new int[2]; // [0] = words; [1] = score
        app.getDatabaseUtils().forEachResult("SELECT"
                + " COUNT(*) AS words,"                                 // 0
                + " SUM(" + GameWord.COLUMN_POINT_VALUE + ") AS score"  // 1
                + " FROM " + GameWord.TABLE_NAME
                + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?",
                new String[] { gameId + "" },
                cursor -> {
                    extraGameData[0] = cursor.getInt(0);
                    extraGameData[1] = cursor.getInt(1);
                });

        // We have to set this for later
        difficulty = gameData.getString(Game.COLUMN_DIFFICULTY, Difficulty.ZEN.name());

        // Side by side level and score
        animateValue(gameData.getInt(Game.COLUMN_LEVEL, 0),
                (TextView) findViewById(R.id.endGameScreenLevel), 0);
        animateValue(extraGameData[1],
                (TextView) findViewById(R.id.endGameScreenScore), 50);
        animateValue(gameData.getInt(Game.COLUMN_SCRAMBLES_EARNED, 0),
                (TextView) findViewById(R.id.endGameScreenScramblesEarned), 50);
        animateValue(gameData.getInt(Game.COLUMN_SCRAMBLES_USED, 0),
                (TextView) findViewById(R.id.endGameScreenScramblesUsed), 100);
        animateValue(extraGameData[0], (TextView) findViewById(R.id.endGameScreenWords), 150);

        // The chart will load once the color theme has been set
        activeGraphFunction = this::loadWordLengthGraph;

        // Word List
        TextView wordsList = (TextView) findViewById(R.id.endGameScreenWordsList);
        ArrayList<String> gameMovesList =
                app.getDatabaseUtils().getGameMoves(getIntent().getLongExtra(EXTRA_GAME_ID, -1));
        gameMovesList.forEach(s -> wordsList.append(s + ", "));

        // Color theme
        app.addColorThemeEventHandler(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeColorThemeEventHandler(this);
    }

    private void animateValue(int value, TextView target, int delay) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(getResources().getInteger(R.integer.animation_durationLag));
        animator.addUpdateListener(animation -> target.setText(
                getString(R.string.integer, (int) animation.getAnimatedValue())));

        if (delay > 0) {
            (new Handler()).postDelayed(animator::start, delay);
        } else {
            animator.start();
        }
    }

    private void showGraph(GraphAction action) {
        // Reset Graphs
        graphWrapper.removeAllViews();

        // Reset buttons
        if (graphButtons.size() == 0) {
            graphButtons.addAll(ViewUtils.getElementsByTagName(graphButtonsWrapper,
                    AppCompatTextView.class, false));
        }
        for (TextView tv : graphButtons) {
            tv.setTextColor(activeColorTheme.text);
            tv.setBackgroundColor(activeColorTheme.backgroundLight);
        }

        // Activate button
        action.button.setTextColor(activeColorTheme.textOnPrimary);
        action.button.setBackgroundColor(activeColorTheme.primary);

        // Show graph
        if (action.chart != null) {
            graphWrapper.addView(action.chart);
            action.chart.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            action.chart.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            action.chart.animateX(getResources().getInteger(R.integer.animation_durationLag));
        } else {
            TextView textView = new TextView(app);
            textView.setText(R.string.endGameScreen_graphNoData);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(activeColorTheme.text);
            View view = new View(app);
            view.setBackgroundColor(activeColorTheme.backgroundLight);
            graphWrapper.addView(view);
            graphWrapper.addView(textView);
            view.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
    }

    @VisibleForXML
    public void loadPointDistributionGraph(View v) {
        activeGraphFunction = this::loadPointDistributionGraph;
        if (memoizedGraphs.get(R.id.endGameScreenGraphPointDistributionButton) != null) {
            showGraph(memoizedGraphs.get(R.id.endGameScreenGraphPointDistributionButton));
            return;
        }

        // Prepare
        GraphAction action = new GraphAction();
        action.button = (TextView) findViewById(R.id.endGameScreenGraphPointDistributionButton);
        memoizedGraphs.put(R.id.endGameScreenGraphPointDistributionButton, action);

        // Data backend
        ArrayList<BarEntry> rawData = new ArrayList<>();
        BarDataSet dataSet = new BarDataSet(rawData, "");
        dataSet.setColor(activeColorTheme.textBlend);
        dataSet.setDrawValues(false);
        BarData data = new BarData(dataSet);

        // Get Data from db into chart backend
        Cursor cursor = app.getDatabaseUtils().getReadableDatabase().rawQuery("SELECT"
                        + " COUNT(*) AS frequency,"
                        + " " + GameWord.COLUMN_POINT_VALUE
                        + " FROM " + GameWord.TABLE_NAME
                        + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?"
                        + " GROUP BY " + GameWord.COLUMN_POINT_VALUE
                        + " ORDER BY " + GameWord.COLUMN_POINT_VALUE + " ASC",
                new String[] { gameId + "" });
        if (cursor.moveToFirst()) {
            do {
                data.addEntry(new BarEntry(cursor.getInt(1), cursor.getInt(0), ""), 0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        data.notifyDataChanged();

        // No data? No graph.
        if (rawData.size() == 0) {
            action.chart = null;
            showGraph(action);
            return;
        }

        // Set up chart
        BarChart chart = new BarChart(app);
        chart.setData(data);
        chart.setDrawBarShadow(false);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setTouchEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1);
        x.setTextColor(activeColorTheme.text);
        x.setGridColor(activeColorTheme.textBlend);
        x.setAxisLineColor(activeColorTheme.textBlend);
        x.setAxisMinimum(0);

        YAxis y = chart.getAxisLeft();
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setTextColor(activeColorTheme.text);
        y.setGridColor(activeColorTheme.textBlend);
        y.setAxisLineColor(activeColorTheme.textBlend);
        y.setGranularity(1);
        y.setAxisMinimum(0);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // Finally, show the graph
        action.chart = chart;
        showGraph(action);
    }

    @VisibleForXML
    public void loadWordLengthGraph(View v) {
        activeGraphFunction = this::loadWordLengthGraph;
        if (memoizedGraphs.get(R.id.endGameScreenGraphWordLengths) != null) {
            showGraph(memoizedGraphs.get(R.id.endGameScreenGraphWordLengths));
            return;
        }

        // Prepare
        GraphAction action = new GraphAction();
        action.button = (TextView) findViewById(R.id.endGameScreenGraphWordLengths);
        memoizedGraphs.put(R.id.endGameScreenGraphWordLengths, action);

        // Data backend
        ArrayList<PieEntry> rawData = new ArrayList<>();
        PieDataSet dataSet = new PieDataSet(rawData, "");
        dataSet.setColor(activeColorTheme.backgroundLight);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineColor(activeColorTheme.primary);
        dataSet.setSliceSpace(2);
        PieData data = new PieData(dataSet);
        data.setValueTextColor(activeColorTheme.text);
        data.setValueTextSize(10);

        // Get data from db
        Cursor cursor = app.getDatabaseUtils().getReadableDatabase().rawQuery("SELECT"
                + " COUNT(*) AS quantity,"
                + " LENGTH(" + GameWord.COLUMN_WORD + ") AS word_length"
                + " FROM " + GameWord.TABLE_NAME
                + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?"
                + " GROUP BY word_length"
                + " ORDER BY quantity ASC",
                new String[] { gameId + "" });
        int total = 0;
        if (cursor.moveToFirst()) {
            do {
                total += cursor.getInt(0);
                dataSet.addEntry(new PieEntry(
                        cursor.getInt(0),
                        getString(R.string.endGameScreen_graphWordLengthsLabel, cursor.getInt(1))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        data.notifyDataChanged();
        if (rawData.size() == 0) {
            action.chart = null;
            showGraph(action);
            return;
        }

        // Set up chart
        PieChart chart = new PieChart(app);
        chart.setData(data);
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(activeColorTheme.background);
        chart.setTransparentCircleColor(Color.TRANSPARENT);
        chart.setTransparentCircleAlpha(255);
        chart.setTransparentCircleRadius(54);
        chart.setHoleRadius(54);
        chart.setDrawCenterText(false);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(false);
        chart.setEntryLabelColor(activeColorTheme.text); // Labels outside the chart

        chart.getLegend().setEnabled(false);

        // One last thing
        final int finalTotal = total;
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler)
                -> Math.round(value)
                + " (" + (Math.round(1000.0 * value / finalTotal) / 10.0) + "%)");

        // Finally show graph.
        action.chart = chart;
        showGraph(action);
    }

    @VisibleForXML
    public void onActionPlayAgainClick(View v) {
        Intent gameScreenIntent = new Intent(this, GameScreen.class);
        gameScreenIntent.putExtra(GameScreen.EXTRA_DIFFICULTY, difficulty);
        startActivity(gameScreenIntent);
        finish();
    }

    @VisibleForXML
    public void onActionMainMenuClick(View v) {
        finish();
    }

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        activeColorTheme = colorTheme;

        // Re-load chart to apply colors
        if (activeGraphFunction != null) {
            activeGraphFunction.accept(null);
        }
    }
}
