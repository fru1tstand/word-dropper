package me.fru1t.worddropper.widget.gameboard;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * A single statistic within the HUD
 */
public class HUDStat extends FrameLayout implements ColorThemeEventHandler {
    private final WordDropperApplication app;

    private final TextView titleTextView;
    private final TextView valueTextView;

    private final Rect titleSize;

    public HUDStat(@NonNull Context context) {
        super(context);
        app = (WordDropperApplication) context.getApplicationContext();

        titleTextView = new TextView(context);
        addView(titleTextView);
        titleTextView.setY(getResources().getDimension(R.dimen.gameScreen_hudStatTitleTopPadding));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.gameScreen_hudStatTitleTextSize));

        valueTextView = new TextView(context);
        addView(valueTextView);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.gameScreen_hudStatValueTextSize));

        titleSize = new Rect();
    }

    public void setTitle(@StringRes int stringId) {
        titleTextView.setText(app.getResources().getString(stringId));
        relayout();
    }

    public void setValue(String value) {
        valueTextView.setText(value);
        relayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        relayout();
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

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        titleTextView.setTextColor(colorTheme.textBlend);
        valueTextView.setTextColor(colorTheme.textBlend);
        postInvalidate();
    }

    private void relayout() {
        int rootWidth = getWidth();

        // Center title
        String titleText = titleTextView.getText().toString();
        titleTextView.getPaint().getTextBounds(titleText, 0, titleText.length(), titleSize);
        titleTextView.setX(rootWidth / 2 - titleSize.centerX());

        // Center value
        valueTextView.setY(titleSize.height()
                + getResources().getDimension(R.dimen.gameScreen_hudStatTitleTopPadding)
                + getResources().getDimension(R.dimen.gameScreen_hudStatValueTopMargin));
        valueTextView.setX(rootWidth / 2
                - valueTextView.getPaint().measureText(valueTextView.getText().toString()) / 2);
    }
}
