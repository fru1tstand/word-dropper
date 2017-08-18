package me.fru1t.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import lombok.Getter;

/**
 * A flat designed progress bar that includes text.
 */
public class ProgressBar extends View {
    private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final int DEFAULT_PROGRESS_COLOR = Color.BLACK;
    private static final int DEFAULT_TEXT_COLOR = Color.GRAY;
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_PROGRESS = 33;

    private final Paint backgroundColor = new Paint();
    private final Paint progressColor = new Paint();
    private final Paint textColor = new Paint();
    private @Getter long max;
    private @Getter long progress;

    public ProgressBar(Context context) {
        super(context);
        backgroundColor.setColor(DEFAULT_BACKGROUND_COLOR);
        progressColor.setColor(DEFAULT_PROGRESS_COLOR);
        textColor.setColor(DEFAULT_TEXT_COLOR);
        max = DEFAULT_MAX;
        progress = DEFAULT_PROGRESS;
    }

    public void setMax(long max) {
        this.max = max;
        invalidate();
    }

    public void setProgress(long progress) {
        this.progress = progress;
        invalidate();
    }

    public void setBackgroundColor(int color) {
        backgroundColor.setColor(color);
        invalidate();
    }

    public void setProgressColor(int color) {
        progressColor.setColor(color);
        invalidate();
    }

    public void setTextColor(int color) {
        textColor.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);

        // Fill progress bar
        canvas.drawRect(0, 0, (float) ((1.0 * progress / max) * getWidth()), getHeight(), progressColor);

        // Draw text
        canvas.drawText(max + "/" + progress, 0, 0, textColor);
    }
}
