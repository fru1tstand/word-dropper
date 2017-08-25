package me.fru1t.worddropper.activities.gameboard;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;

import me.fru1t.worddropper.R;

/**
 * A wrapper for the stats view layout.
 */
public class StatsView {
    private final TextView currentWord;

    private int rootWidth;

    public StatsView(View statsView) {
        currentWord = (TextView) statsView.findViewById(R.id.currentWord);

        statsView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> rootWidth = r - l);
        rootWidth = statsView.getWidth();
    }

    public void setCurrentWord(@Nullable String s) {
        if (Strings.isNullOrEmpty(s)) {
            currentWord.setText("");
            return;
        }

        float wordWidth = currentWord.getPaint().measureText(s);
        currentWord.setText(s);
        currentWord.setX(rootWidth / 2 - wordWidth / 2);
        System.out.println("Root width: " + rootWidth + "; Word width: " + wordWidth);
    }
}
