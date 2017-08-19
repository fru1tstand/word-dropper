package me.fru1t.worddropper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import lombok.Getter;
import lombok.Setter;

/**
 * A flat designed progress bar that includes text.
 */
public class ProgressBar extends View {
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_PROGRESS = 33;

    private final @Getter Paint backgroundColor;
    private final @Getter Paint progressColor;
    private final @Getter Paint textColor;
    private @Getter @Setter long max;
    private @Getter @Setter long progress;

    public ProgressBar(Context context) {
        super(context);

        backgroundColor = new Paint();
        progressColor = new Paint();
        textColor = new Paint();

        max = DEFAULT_MAX;
        progress = DEFAULT_PROGRESS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);

        // Fill progress bar
        canvas.drawRect(
                0,
                0,
                (float) ((1.0 * progress / max) * getWidth()),
                getHeight(), progressColor);

        // Draw text
        canvas.drawText(max + "/" + progress, 0, 0, textColor);
    }
}
