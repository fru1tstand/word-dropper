package me.fru1t.worddropper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import lombok.Getter;
import lombok.Setter;

/**
 * A single interactable tile within the tile board
 */
public class Tile extends View {
    private final Paint backgroundColor;
    private final @Getter Paint textPaint;

    private @Getter @Setter int size;
    private @Getter @Setter String text;

    private transient final Paint activeBackgroundColor;

    public Tile(Context context) {
        super(context);

        backgroundColor = new Paint();
        textPaint = new Paint();

        activeBackgroundColor = new Paint();
    }

    public void setBackgroundColor(int color) {
        backgroundColor.setColor(color);
        activeBackgroundColor.setColor(color);
    }

    public void onPress() {
        activeBackgroundColor.setColor(Color.GRAY);
        postInvalidate();
    }

    public void onRelease() {
        activeBackgroundColor.set(backgroundColor);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, size, size, activeBackgroundColor);
        canvas.drawText(text, 20, 20, textPaint); // TODO: Middle align
    }
}
