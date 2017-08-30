package me.fru1t.worddropper.widget.gameboard;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.fru1t.worddropper.WordDropper;

/**
 * A single statistic within the HUD
 */
public class HUDStat extends FrameLayout {
    public static final int HEIGHT = 170;

    private static final int TEXT_COLOR = WordDropper.COLOR_TEXT_BLEND;
    private static final int TITLE_TEXT_SIZE = 16;
    private static final int VALUE_TEXT_SIZE = 30;

    private static final int TITLE_SPACING = 20;
    private static final int VALUE_SPACING = 10;

    private final TextView titleTextView;
    private final TextView valueTextView;

    private final Rect titleSize;

    public HUDStat(@NonNull Context context) {
        super(context);

        titleTextView = new TextView(context);
        addView(titleTextView);
        titleTextView.setY(TITLE_SPACING);
        titleTextView.setTextSize(TITLE_TEXT_SIZE);
        titleTextView.setTextColor(TEXT_COLOR);

        valueTextView = new TextView(context);
        addView(valueTextView);
        valueTextView.setTextSize(VALUE_TEXT_SIZE);
        valueTextView.setTextColor(TEXT_COLOR);

        titleSize = new Rect();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        relayout();
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
        relayout();
    }

    public void setValue(String value) {
        valueTextView.setText(value);
        relayout();
    }

    private void relayout() {
        int rootWidth = getWidth();

        // Center title
        String titleText = titleTextView.getText().toString();
        titleTextView.getPaint().getTextBounds(titleText, 0, titleText.length(), titleSize);
        titleTextView.setX(rootWidth / 2 - titleSize.centerX());

        // Center value
        valueTextView.setY(titleSize.height() + VALUE_SPACING + TITLE_SPACING);
        valueTextView.setX(rootWidth / 2
                - valueTextView.getPaint().measureText(valueTextView.getText().toString()) / 2);
    }
}
