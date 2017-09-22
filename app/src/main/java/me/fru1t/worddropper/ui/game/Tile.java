package me.fru1t.worddropper.ui.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * A single interactable tile within the tile board
 */
public class Tile extends View implements ColorThemeEventHandler {
    private @Getter @Setter int size;
    private @Getter @Setter char letter;

    private final WordDropperApplication app;
    private final @Getter Paint textPaint;
    private final Paint backgroundColor;
    private final Rect textBounds;

    private ColorTheme activeColorTheme;

    public Tile(Context context) {
        super(context);
        app = (WordDropperApplication) context.getApplicationContext();

        textPaint = new Paint();
        textPaint.setTextSize(getResources().getDimension(R.dimen.gameScreen_tileTextSize));

        textBounds = new Rect();
        backgroundColor = new Paint();
        letter = ' ';
    }

    public void press() {
        backgroundColor.setColor(activeColorTheme.primary);
        textPaint.setColor(activeColorTheme.textOnPrimary);
        postInvalidate();
    }

    public void release() {
        backgroundColor.setColor(activeColorTheme.background);
        textPaint.setColor(activeColorTheme.text);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, size, size, backgroundColor);
        textPaint.getTextBounds(letter + "", 0, 1, textBounds);
        canvas.drawText(letter + "",
                size / 2 - textBounds.centerX(),
                size / 2 - textBounds.centerY(),
                textPaint);
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
        activeColorTheme = colorTheme;
        release();
    }
}
