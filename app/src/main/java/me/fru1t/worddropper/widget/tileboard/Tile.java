package me.fru1t.worddropper.widget.tileboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import lombok.Getter;
import lombok.Setter;

/**
 * A single interactable tile within the tile board
 */
public class Tile extends View {
    private @Getter int defaultBackgroundColor;
    private @Getter @Setter int activeBackgroundColor;
    private @Getter @Setter int size;
    private @Getter @Setter String text;

    private final @Getter Paint textPaint;
    private final Paint backgroundColor;
    private final Rect textBounds;

    public Tile(Context context) {
        super(context);

        textPaint = new Paint();
        textBounds = new Rect();
        backgroundColor = new Paint();
    }

    public void setDefaultBackgroundColor(int color) {
        defaultBackgroundColor = color;
        backgroundColor.setColor(color);
        postInvalidate();
    }

    public void onPress() {
        backgroundColor.setColor(activeBackgroundColor);
        postInvalidate();
    }

    public void onRelease() {
        backgroundColor.setColor(defaultBackgroundColor);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, size, size, backgroundColor);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, size / 2 - textBounds.centerX(), size / 2 - textBounds.centerY(), textPaint);
    }
}
