package me.fru1t.worddropper.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import lombok.Getter;
import lombok.Setter;

/**
 * A flat designed progress bar that includes text.
 */
public class ProgressBar extends View {
    private static final int ANIMATION_DURATION_BASE = 150;
    private static final int ANIMATION_DURATION_PER_DELTA = 35;

    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_PROGRESS = 0;

    private final @Getter Paint backgroundColor;
    private final @Getter Paint progressColor;
    private final @Getter Paint progressCalculatedColor;
    private final @Getter Paint textPaint;
    private @Getter @Setter int max;
    private @Getter int progress;

    private float calculatedProgressBarWidth;
    private final Rect calculatedTextBounds;

    public ProgressBar(Context context) {
        super(context);

        backgroundColor = new Paint();
        progressColor = new Paint();
        textPaint = new Paint();
        progressCalculatedColor = new Paint();
        calculatedTextBounds = new Rect();

        max = DEFAULT_MAX;
        progress = DEFAULT_PROGRESS;
        calculatedProgressBarWidth = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);

        // Fill progress bar
        canvas.drawRect(
                0,
                0,
                (float) (progress * 1.0 / max * getWidth()),
                getHeight(),
                progressCalculatedColor);
        canvas.drawRect(
                0,
                0,
                calculatedProgressBarWidth,
                getHeight(),
                progressColor);

        // Draw text
        String text = progress + " / " + max;
        textPaint.getTextBounds(text, 0, text.length(), calculatedTextBounds);
        canvas.drawText(text, 10,
                calculatedTextBounds.height() + (getHeight() - calculatedTextBounds.height()) / 2,
                textPaint);
    }

    public void animateAddProgress(int progressDelta) {
        progress = Math.min(progress + progressDelta, max);

        ValueAnimator va = ValueAnimator
                .ofFloat(calculatedProgressBarWidth, (float) (progress * 1.0 / max * getWidth()));
        va.setDuration(ANIMATION_DURATION_BASE + ANIMATION_DURATION_PER_DELTA * progressDelta);
        va.addUpdateListener(animation -> {
            calculatedProgressBarWidth = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        va.start();
    }
}
