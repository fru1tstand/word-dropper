package me.fru1t.worddropper.widget.gameboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.WordDropper;

/**
 * Shows the status, metrics, move left, and current tile path in game. Essentially, the header
 * of the game board. This class isn't technically a widget as you can't inflate it, but rather,
 * a wrapper for one that is already inflated.
 */
public class GameBoardHUD extends FrameLayout {
    public interface GameBoardHUDEventListener {
        /**
         * Triggered when the score stat is clicked.
         */
        void onLevelClick();

        /**
         * Triggered when the scramble stat is clicked.
         */
        void onScrambleClick();

        /**
         * Triggered when the moves left stat is clicked.
         */
        void onMovesLeftClick();

        /**
         * Triggered when the current word panel is clicked.
         */
        void onCurrentWordClick();
    }

    private static final int TEXT_SIZE = 22;

    private final @Getter TextView currentWordTextView;

    private @Setter GameBoardHUDEventListener eventListener;

    private final HUDStat movesRemaining;
    private final HUDStat scramblesRemaining;
    private final HUDStat currentLevel;

    public GameBoardHUD(@NonNull Context context) {
        super(context);

        // Current word element
        currentWordTextView = new TextView(context);
        addView(currentWordTextView);
        currentWordTextView.setY(380);
        currentWordTextView.setTextSize(TEXT_SIZE);

        // Stats
        movesRemaining = new HUDStat(context);
        addView(movesRemaining);
        movesRemaining.getLayoutParams().height = HUDStat.HEIGHT;
        movesRemaining.setTitle("Moves Left");
        movesRemaining.setY(0);
        movesRemaining.setOnTouchListener((v, event) ->
                touchListenerHandler(event, GameBoardHUDEventListener::onMovesLeftClick));

        scramblesRemaining = new HUDStat(context);
        addView(scramblesRemaining);
        scramblesRemaining.getLayoutParams().height = HUDStat.HEIGHT;
        scramblesRemaining.setTitle("Scrambles");
        scramblesRemaining.setY(0);
        scramblesRemaining.setOnTouchListener((v, event) ->
                touchListenerHandler(event, GameBoardHUDEventListener::onScrambleClick));

        currentLevel = new HUDStat(context);
        addView(currentLevel);
        currentLevel.getLayoutParams().height = HUDStat.HEIGHT;
        currentLevel.setTitle("Level");
        currentLevel.setY(0);
        currentLevel.setOnTouchListener((v, event) ->
                touchListenerHandler(event, GameBoardHUDEventListener::onLevelClick));

        // Set up touch listening
        setOnTouchListener((v, event) -> {
            if (eventListener == null
                    || event.getActionIndex() != 0
                    || event.getY() < HUDStat.HEIGHT) {
                return false;
            }

            if (event.getActionMasked() != MotionEvent.ACTION_UP) {
                return true;
            }

            eventListener.onCurrentWordClick();
            return true;
        });
    }

    public void updateColors() {
        setBackgroundColor(WordDropper.colorTheme.background);
        movesRemaining.updateColors();
        scramblesRemaining.updateColors();
        currentLevel.updateColors();
        postInvalidate();
    }

    private boolean touchListenerHandler(MotionEvent event,
                                         Consumer<GameBoardHUDEventListener> action) {
        if (event.getActionIndex() != 0
                || eventListener == null
                || event.getY() > HUDStat.HEIGHT) {
            return false;
        }

        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }

        action.accept(eventListener);
        return true;
    }

    public void setMovesRemaining(String moves) {
        movesRemaining.setValue(moves);
    }

    public void setScramblesRemaining(String scrambles) {
        scramblesRemaining.setValue(scrambles);
    }

    public void setCurrentLevel(String level) {
        currentLevel.setValue(level);
    }

    public void setCurrentWordTextView(@Nullable String s) {
        if (Strings.isNullOrEmpty(s)) {
            currentWordTextView.setText("");
            currentWordTextView.setTextColor(WordDropper.colorTheme.text);
            return;
        }

        if (WordDropper.isWord(s)) {
            s += " (" + WordDropper.getWordValue(s) + ")";
            currentWordTextView.setTextColor(WordDropper.colorTheme.primary);
        } else {
            currentWordTextView.setTextColor(WordDropper.colorTheme.text);
        }

        s = s.substring(0, 1).toUpperCase() + s.substring(1);
        float wordWidth = currentWordTextView.getPaint().measureText(s);
        currentWordTextView.setText(s);
        currentWordTextView.setX(getWidth() / 2 - wordWidth / 2);
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
}
