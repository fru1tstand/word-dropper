package me.fru1t.worddropper.widget.gameboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;

/**
 * Shows the status, metrics, move left, and current tile path in game. Essentially, the header
 * of the game board. This class isn't technically a widget as you can't inflate it, but rather,
 * a wrapper for one that is already inflated.
 */
public class GameBoardHUD extends FrameLayout {
    private @Setter Runnable onLevelClickEventListener;
    private @Setter Runnable onScrambleClickEventListener;
    private @Setter Runnable onMovesLeftClickEventListener;
    private @Setter Runnable onCurrentWordClickEventListener;

    private final WordDropperApplication app;
    private final @Getter TextView currentWordTextView;
    private final HUDStat movesRemaining;
    private final HUDStat scramblesRemaining;
    private final HUDStat currentLevel;

    public GameBoardHUD(@NonNull Context context) {
        super(context);
        app = (WordDropperApplication) context.getApplicationContext();
        int hudStatHeight = (int) getResources().getDimension(R.dimen.gameScreen_hudStatHeight);

        // Current word element
        currentWordTextView = new TextView(context);
        addView(currentWordTextView);
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

    public void updateColors() {
        setBackgroundColor(app.getColorTheme().background);
        movesRemaining.updateColors();
        scramblesRemaining.updateColors();
        currentLevel.updateColors();
        postInvalidate();
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
            currentWordTextView.setTextColor(app.getColorTheme().text);
            return;
        }

        if (app.getDictionary().isWord(s)) {
            s += " (" + app.getDictionary().getWordValue(s) + ")";
            currentWordTextView.setTextColor(app.getColorTheme().primary);
        } else {
            currentWordTextView.setTextColor(app.getColorTheme().text);
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
