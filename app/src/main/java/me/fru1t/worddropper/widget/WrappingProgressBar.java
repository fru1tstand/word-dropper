package me.fru1t.worddropper.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import lombok.Getter;
import lombok.Setter;

/**
 * A minimalist-designed progress bar that shows the progress via text. On hitting the maximum, the
 * progress bar will wrap and start at zero again.
 */
public class WrappingProgressBar extends View {
    public interface NextMaximumFunction {
        /**
         * Determines the next maximum value the progress bar should take on given the number of
         * wraps it has completed.
         */
        long next(int wraps);
    }

    public interface WrappingProgressBarEventListener {
        /**
         * Called when the progress bar wraps.
         * @param wraps The number of wraps the progress bar has gone through.
         * @param newMax The new Max value for this wrap.
         */
        void onWrap(int wraps, long newMax);
    }

    private static final int ANIMATION_DURATION_BASE = 150;
    private static final int ANIMATION_DURATION_PER_DELTA = 35;
    private static final int ANIMATION_DURATION_MAX = 1000;

    private final @Getter Paint backgroundColor;
    private final @Getter Paint progressColor;
    private final @Getter Paint progressCalculatedColor;
    private final @Getter Paint textPaint;
    private @Getter long max;
    private @Getter long progress;
    private @Getter int wraps;
    private NextMaximumFunction nextMaximumFunction;
    private @Setter WrappingProgressBarEventListener eventWrappingProgressBarEventListener;

    private float calculatedProgressBarWidth;
    private final Rect calculatedTextBounds;

    public WrappingProgressBar(Context context) {
        super(context);

        backgroundColor = new Paint();
        progressColor = new Paint();
        textPaint = new Paint();
        progressCalculatedColor = new Paint();
        calculatedTextBounds = new Rect();

        wraps = 0;
        max = 1;
        progress = 0;
        calculatedProgressBarWidth = 0;
        nextMaximumFunction = null;
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
        String text = "Level: " + wraps + " (" + progress + " / " + max + ")";
        textPaint.getTextBounds(text, 0, text.length(), calculatedTextBounds);
        canvas.drawText(text, 10,
                calculatedTextBounds.height() + (getHeight() - calculatedTextBounds.height()) / 2,
                textPaint);
    }

    public void setNextMaximumFunction(NextMaximumFunction nextMaximumFunction) {
        this.nextMaximumFunction = nextMaximumFunction;
        setMax(nextMaximumFunction.next(wraps));
    }

    public void setMax(long max) {
        this.max = max;
        animateAddProgress(0);
    }

    /**
     * Adds the given progress to the current progress amount. Animates overflow by filling the bar
     * and recursively calling animateAddProgress until all progressDelta is consumed.
     */
    public void animateAddProgress(long progressDelta) {
        long progressRemainder = progressDelta + progress - max;
        if (progressRemainder > 0) {
            progressDelta = max - progress;
        }
        progress = progress + progressDelta;

        ValueAnimator va = ValueAnimator.ofFloat(
                calculatedProgressBarWidth, (float) (progress * 1.0 / max * getWidth()));
        va.setDuration(Math.min(
                ANIMATION_DURATION_BASE + ANIMATION_DURATION_PER_DELTA * progressDelta,
                ANIMATION_DURATION_MAX));
        va.addUpdateListener(animation -> {
            calculatedProgressBarWidth = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (progressRemainder < 0) {
                    return;
                }

                ++wraps;
                progress = 0;
                calculatedProgressBarWidth = 0;
                if (nextMaximumFunction != null) {
                    max = nextMaximumFunction.next(wraps);
                }
                if (eventWrappingProgressBarEventListener != null) {
                    eventWrappingProgressBarEventListener.onWrap(wraps, max);
                }
                animateAddProgress(progressRemainder);
            }
        });

        va.start();
    }
}
