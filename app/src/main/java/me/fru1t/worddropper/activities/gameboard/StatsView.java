package me.fru1t.worddropper.activities.gameboard;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropper;

/**
 * A wrapper for the stats view layout.
 */
public class StatsView {
    private int rootWidth;
    private @Getter @Setter int defaultTextColor;
    private @Getter @Setter int activeTextColor;

    private final TextView currentWord;

    public StatsView(View statsView) {
        currentWord = (TextView) statsView.findViewById(R.id.currentWord);

        statsView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> rootWidth = r - l);
        rootWidth = statsView.getWidth();

        currentWord.setY(200);
    }

    public void setCurrentWord(@Nullable String s) {
        if (Strings.isNullOrEmpty(s)) {
            currentWord.setText("");
            currentWord.setTextColor(defaultTextColor);
            return;
        }

        if (WordDropper.isWord(s)) {
            s += " (" + WordDropper.getWordValue(s) + ")";
            currentWord.setTextColor(activeTextColor);
        } else {
            currentWord.setTextColor(defaultTextColor);
        }

        s = s.substring(0, 1).toUpperCase() + s.substring(1);
        float wordWidth = currentWord.getPaint().measureText(s);
        currentWord.setText(s);
        currentWord.setX(rootWidth / 2 - wordWidth / 2);
    }
}
