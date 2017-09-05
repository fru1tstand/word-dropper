package me.fru1t.worddropper.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.WordDropper;

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
        int next(int wraps);
    }

    public interface WrappingProgressBarEventListener {
        /**
         * Called when the progress bar wraps.
         * @param wraps The number of wraps the progress bar has gone through.
         * @param newMax The new Max value for this wrap.
         */
        void onWrap(int wraps, int newMax);

        /**
         * Called when the progress bar completes all animations/triggers after animateAddProgress
         * is called.
         */
        void onAnimateAddEnd();
    }

    private static final int ANIMATION_DURATION_BASE = 150;
    private static final int ANIMATION_DURATION_PER_DELTA = 35;
    private static final int ANIMATION_DURATION_MAX = 1000;

    private static final int TEXT_SIZE = 16;
    private static final Typeface TEXT_TYPE_FACE = Typeface.DEFAULT;

    private final Paint backgroundColor;
    private final Paint progressColor;
    private final Paint progressCalculatedColor;
    private final Paint textPaint;

    private @Getter int max;
    private @Getter int progress;
    private @Getter int wraps;
    private @Getter int total;

    private NextMaximumFunction nextMaximumFunction;
    private @Setter WrappingProgressBarEventListener eventWrappingProgressBarEventListener;

    private float calculatedProgressBarWidth;
    private final Rect calculatedTextBounds;

    public WrappingProgressBar(Context context) {
        super(context);

        backgroundColor = new Paint();
        progressColor = new Paint();
        progressCalculatedColor = new Paint();

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTypeface(TEXT_TYPE_FACE);

        wraps = 0;
        max = 1;
        progress = 0;
        total = 0;

        calculatedProgressBarWidth = 0;
        calculatedTextBounds = new Rect();

        nextMaximumFunction = null;
        eventWrappingProgressBarEventListener = null;
    }

    public void updateColors() {
        backgroundColor.setColor(WordDropper.colorTheme.background);
        progressColor.setColor(WordDropper.colorTheme.primaryDark);
        progressCalculatedColor.setColor(WordDropper.colorTheme.primaryLight);
        textPaint.setColor(WordDropper.colorTheme.textOnPrimaryDark);
        postInvalidate();
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

    public void setMax(int max) {
        this.max = max;
        animateAddProgress(0);
    }

    /**
     * Adds the given progress to the current progress amount. Animates overflow by filling the bar
     * and recursively calling animateAddProgress until all progressDelta is consumed.
     */
    public void animateAddProgress(int progressDelta) {
        int progressRemainder = progressDelta + progress - max;
        if (progressRemainder > 0) {
            progressDelta = max - progress;
        }
        progress = progress + progressDelta;
        total += progressDelta;

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
                    if (eventWrappingProgressBarEventListener != null) {
                        eventWrappingProgressBarEventListener.onAnimateAddEnd();
                    }
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
