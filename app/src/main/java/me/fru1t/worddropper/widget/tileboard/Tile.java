package me.fru1t.worddropper.widget.tileboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.WordDropper;

/**
 * A single interactable tile within the tile board
 */
public class Tile extends View {
    private static final int TEXT_SIZE = 60;

    private @Getter @Setter int size;
    private @NonNull @Getter @Setter String text;

    private final @Getter Paint textPaint;
    private final Paint backgroundColor;
    private final Rect textBounds;

    public Tile(Context context) {
        super(context);

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);

        textBounds = new Rect();
        backgroundColor = new Paint();
        text = "";
    }

    public void press() {
        backgroundColor.setColor(WordDropper.colorTheme.primary);
        textPaint.setColor(WordDropper.colorTheme.textOnPrimary);
        postInvalidate();
    }

    public void release() {
        backgroundColor.setColor(WordDropper.colorTheme.background);
        textPaint.setColor(WordDropper.colorTheme.text);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, size, size, backgroundColor);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text,
                size / 2 - textBounds.centerX(),
                size / 2 - textBounds.centerY(),
                textPaint);
    }
}
