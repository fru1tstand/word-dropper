package me.fru1t.worddropper.widget.gameboard;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.common.base.Strings;

import java.util.LinkedList;

import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * Shows the status, metrics, move left, and current tile path in game. Essentially, the header
 * of the game board. This class isn't technically a widget as you can't inflate it, but rather,
 * a wrapper for one that is already inflated.
 */
public class GameBoardHUD extends FrameLayout implements ColorThemeEventHandler {
    private static final int CHART_ELEMENTS = 30;

    private @Setter Runnable onLevelClickEventListener;
    private @Setter Runnable onScrambleClickEventListener;
    private @Setter Runnable onMovesLeftClickEventListener;
    private @Setter Runnable onCurrentWordClickEventListener;

    private final WordDropperApplication app;
    private final TextView currentWordTextView;
    private final HUDStat movesRemaining;
    private final HUDStat scramblesRemaining;
    private final HUDStat currentLevel;

    private final BarChart wordHistoryChart;
    private final BarDataSet wordHistoryDataSet;
    private final LinkedList<BarEntry> wordHistoryDataList;

    private int currentWordHorizontalPadding;
    private int currentWordVerticalPadding;
    private int currentWordActiveColor;
    private int currentWordDefaultColor;

    public GameBoardHUD(@NonNull Context context) {
        super(context);
        app = (WordDropperApplication) context.getApplicationContext();
        int hudStatHeight = (int) getResources().getDimension(R.dimen.gameScreen_hudStatHeight);
        currentWordHorizontalPadding = (int)
                getResources().getDimension(R.dimen.gameScreen_hudCurrentWordHorizontalPadding);
        currentWordVerticalPadding = (int)
                getResources().getDimension(R.dimen.gameScreen_hudCurrentWordVerticalPadding);

        // Chart
        wordHistoryChart = new BarChart(context);
        addView(wordHistoryChart);
        wordHistoryChart.setY(getResources().getDimension(R.dimen.gameScreen_hudStatHeight));
        wordHistoryChart.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        wordHistoryChart.getLayoutParams().height =
                (int) (getResources().getDimension(R.dimen.gameScreen_hudHeight)
                        - getResources().getDimension(R.dimen.gameScreen_hudStatHeight));
        wordHistoryChart.setDrawBarShadow(false);
        wordHistoryChart.setDrawValueAboveBar(true);
        wordHistoryChart.getDescription().setEnabled(false);
        wordHistoryChart.setPinchZoom(false);
        wordHistoryChart.setDrawGridBackground(false);
        wordHistoryChart.setBackgroundColor(Color.TRANSPARENT);
        wordHistoryChart.setTouchEnabled(false);
        wordHistoryChart.setViewPortOffsets(0, 0, 0, 0);

        wordHistoryDataList = new LinkedList<>();
        wordHistoryDataSet = new BarDataSet(wordHistoryDataList, "");
        wordHistoryDataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler)
                -> value == 0 ? "" : (int) value + "");
        BarData data = new BarData(wordHistoryDataSet);
        wordHistoryChart.setData(data);

        XAxis xAxis = wordHistoryChart.getXAxis();
        xAxis.setEnabled(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis yAxis = wordHistoryChart.getAxisLeft();
        yAxis.setEnabled(false);
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);

        wordHistoryChart.getLegend().setEnabled(false);
        wordHistoryChart.getAxisRight().setEnabled(false);

        // Current word element
        currentWordTextView = new TextView(context);
        addView(currentWordTextView);
        currentWordTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        currentWordTextView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        currentWordTextView.setY(
                getResources().getDimension(R.dimen.gameScreen_hudCurrentWordTopMargin));
        currentWordTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.gameScreen_hudCurrentWordTextSize));

        // Stats
        movesRemaining = new HUDStat(context);
        addView(movesRemaining);
        movesRemaining.getLayoutParams().height = hudStatHeight;
        movesRemaining.setTitle(R.string.gameScreen_hudStatMovesLeft);
        movesRemaining.setOnTouchListener(
                (v, event) -> touchListenerHandler(event, onMovesLeftClickEventListener));

        scramblesRemaining = new HUDStat(context);
        addView(scramblesRemaining);
        scramblesRemaining.getLayoutParams().height = hudStatHeight;
        scramblesRemaining.setTitle(R.string.gameScreen_hudStatScrambles);
        scramblesRemaining.setOnTouchListener(
                (v, event) -> touchListenerHandler(event, onScrambleClickEventListener));

        currentLevel = new HUDStat(context);
        addView(currentLevel);
        currentLevel.getLayoutParams().height = hudStatHeight;
        currentLevel.setTitle(R.string.gameScreen_hudStatLevel);
        currentLevel.setOnTouchListener(
                (v, event) -> touchListenerHandler(event, onLevelClickEventListener));

        // Set up touch listening
        setOnTouchListener((v, event) -> {
            if (onCurrentWordClickEventListener == null
                    || event.getActionIndex() != 0
                    || event.getY() < hudStatHeight) {
                return false;
            }

            if (event.getActionMasked() != MotionEvent.ACTION_UP) {
                return true;
            }

            onCurrentWordClickEventListener.run();
            return true;
        });
    }

    private boolean touchListenerHandler(MotionEvent event, @Nullable Runnable action) {
        if (event.getActionIndex() != 0
                || action == null
                || event.getY() > getResources().getDimension(R.dimen.gameScreen_hudStatHeight)) {
            return false;
        }

        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }

        action.run();
        return true;
    }

    public void setMovesRemaining(String moves) {
        movesRemaining.setValue(moves);
    }

    public void setMovesRemaining(int moves) {
        setMovesRemaining(moves + "");
    }

    public void setScramblesRemaining(String scrambles) {
        scramblesRemaining.setValue(scrambles);
    }

    public void setScramblesRemaining(int scrambles) {
        setScramblesRemaining(scrambles + "");
    }

    public void setCurrentLevel(String level) {
        currentLevel.setValue(level);
    }

    public void setCurrentLevel(int level) {
        setCurrentLevel(level + "");
    }

    public void setCurrentWordTextView(@Nullable String s) {
        if (Strings.isNullOrEmpty(s)) {
            currentWordTextView.setText("");
            currentWordTextView.setTextColor(currentWordDefaultColor);
            currentWordTextView
                    .setPadding(0, currentWordVerticalPadding, 0, currentWordVerticalPadding);
            return;
        }

        if (app.getDictionary().isWord(s)) {
            s += " (" + app.getDictionary().getWordValue(s) + ")";
            currentWordTextView.setTextColor(currentWordActiveColor);
        } else {
            currentWordTextView.setTextColor(currentWordDefaultColor);
        }

        s = s.substring(0, 1).toUpperCase() + s.substring(1);
        float elementSize = currentWordTextView.getPaint().measureText(s)
                + currentWordHorizontalPadding * 2;
        currentWordTextView.setText(s);
        currentWordTextView.setX(getWidth() / 2 - elementSize / 2);
        currentWordTextView.setPadding(currentWordHorizontalPadding, currentWordVerticalPadding,
                currentWordHorizontalPadding, currentWordVerticalPadding);
    }

    public void addWordToGraph(String word, int value) {
        BarData data = wordHistoryChart.getData();
        data.addEntry(new BarEntry(wordHistoryDataList.peekLast().getX() + 1, value, word), 0);
        data.removeEntry(wordHistoryDataList.peekFirst(), 0);
        data.notifyDataChanged();
        wordHistoryChart.notifyDataSetChanged();
        wordHistoryChart.setVisibleXRangeMaximum(CHART_ELEMENTS);
        wordHistoryChart.moveViewToX(data.getEntryCount());
    }

    public void clearGraph() {
        wordHistoryDataList.clear();
        for (int i = 0; i < CHART_ELEMENTS; i++) {
            wordHistoryDataList.add(new BarEntry(i, 0, ""));
        }
        wordHistoryChart.getData().notifyDataChanged();
        wordHistoryChart.notifyDataSetChanged();
        wordHistoryChart.invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int hudStatsWidth = getWidth() / 3;

        currentLevel.setX(0);
        scramblesRemaining.setX(hudStatsWidth);
        movesRemaining.setX(hudStatsWidth * 2);

        movesRemaining.getLayoutParams().width = hudStatsWidth;
        scramblesRemaining.getLayoutParams().width = hudStatsWidth;
        currentLevel.getLayoutParams().width = hudStatsWidth;
        if (changed) {
            movesRemaining.post(movesRemaining::requestLayout);
            scramblesRemaining.post(scramblesRemaining::requestLayout);
            currentLevel.post(scramblesRemaining::requestLayout);
        }
    }

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        setBackgroundColor(colorTheme.background);
        currentWordDefaultColor = colorTheme.text;
        currentWordActiveColor = colorTheme.primary;
        currentWordTextView.setBackgroundColor(colorTheme.background);

        wordHistoryChart.getData().setValueTextColor(colorTheme.textBlend);
        wordHistoryDataSet.setColor(colorTheme.textBlend);
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.addColorThemeEventHandler(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.removeColorThemeEventHandler(this);
    }
}
