package me.fru1t.worddropper.widget.gameboard;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropper;

/**
 * Shows the status, metrics, move left, and current tile path in game. Essentially, the header
 * of the game board. This class isn't technically a widget as you can't inflate it, but rather,
 * a wrapper for one that is already inflated.
 */
public class GameBoardHUD {
    private int rootWidth;
    private @Getter @Setter int defaultTextColor;
    private @Getter @Setter int activeTextColor;

    private final TextView currentWord;

    public GameBoardHUD(View hud) {
        currentWord = (TextView) hud.findViewById(R.id.currentWord);

        hud.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> rootWidth = r - l);
        rootWidth = hud.getWidth();

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
