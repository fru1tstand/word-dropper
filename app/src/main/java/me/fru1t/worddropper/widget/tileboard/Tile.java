package me.fru1t.worddropper.widget.tileboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;

/**
 * A single interactable tile within the tile board
 */
public class Tile extends View {
    private @Getter @Setter int size;
    private @Getter @Setter char letter;

    private final WordDropperApplication app;
    private final @Getter Paint textPaint;
    private final Paint backgroundColor;
    private final Rect textBounds;

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
        backgroundColor.setColor(app.getColorTheme().primary);
        textPaint.setColor(app.getColorTheme().textOnPrimary);
        postInvalidate();
    }

    public void release() {
        backgroundColor.setColor(app.getColorTheme().background);
        textPaint.setColor(app.getColorTheme().text);
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
}
