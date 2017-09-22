package me.fru1t.worddropper.ui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * A minimalist-designed progress bar that shows the progress via text. On hitting the maximum, the
 * progress bar will wrap and start at zero again.
 */
public class WrappingProgressBar extends View implements ColorThemeEventHandler {
    @FunctionalInterface
    public interface NextMaximumFunction {
        /**
         * Determines the next maximum value the progress bar should take onWrapEventListener given the number of
         * wraps it has completed.
         */
        int next(int wraps);
    }

    @FunctionalInterface
    public interface OnWrapEventListener {
        /**
         * Called when the progress bar wraps.
         * @param wraps The number of wraps the progress bar has gone through.
         * @param newMax The new Max value for this wrap.
         */
        void onWrap(int wraps, int newMax);
    }

    private static final Typeface TEXT_TYPE_FACE = Typeface.DEFAULT;

    private final Paint backgroundColor;
    private final Paint progressColor;
    private final Paint progressCalculatedColor;
    private final Paint textPaint;

    private final WordDropperApplication app;
    private @Getter int max;
    private @Getter int progress;
    private @Getter int wraps;
    private @Getter int total;

    private NextMaximumFunction nextMaximumFunction;
    private @Setter OnWrapEventListener onWrapEventListener;
    private @Setter Runnable onAnimateAddEndEventListener;

    private boolean isAnimatingWidth;
    private float animatedProgressWidth;
    private final Rect calculatedTextBounds;

    public WrappingProgressBar(Context context) {
        this(context, null);
    }

    public WrappingProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrappingProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();

        backgroundColor = new Paint();
        progressColor = new Paint();
        progressCalculatedColor = new Paint();

        textPaint = new Paint();
        textPaint.setTextSize(getResources().getDimension(R.dimen.wrappingProgressBar_textSize));
        textPaint.setTypeface(TEXT_TYPE_FACE);

        wraps = 0;
        max = 1;
        progress = 0;
        total = 0;

        isAnimatingWidth = false;
        animatedProgressWidth = 0;
        calculatedTextBounds = new Rect();

        nextMaximumFunction = null;
        onWrapEventListener = null;
    }

    /**
     * As the progress bar wraps, this function will determine the next "maximum value" the progress
     * bar will take on.
     */
    public void setNextMaximumFunction(NextMaximumFunction nextMaximumFunction) {
        this.nextMaximumFunction = nextMaximumFunction;
        setMax(nextMaximumFunction.next(wraps));
    }

    /**
     * Straight up sets the maximum value the progress bar should have. Note, upon wrap, if the
     * nextMaximumFunction has been set, this value will be overwritten by that function.
     */
    public void setMax(int max) {
        this.max = max;
        animateAddProgress(0);
    }

    /**
     * Resets the total of this progress bar.
     */
    public void reset() {
        wraps = 0;
        total = 0;
        progress = 0;
        if (nextMaximumFunction != null) {
            max = nextMaximumFunction.next(0);
        }
        animateAddProgress(0);
    }

    /**
     * Sets the total of the progress bar, wrapping as necessary, and obtaining the resulting
     * progress.
     * @param total The total to set the progress bar to (not simply the progress for the current
     *              level).
     */
    public void setTotal(int total) {
        reset();
        addProgress(total);
    }

    /**
     * Adds progress to the current progress amount immediately.
     */
    public void addProgress(int progressDelta) {
        total += progressDelta;
        while (progressDelta > 0) {
            int levelRemainder = max - progress;
            if (levelRemainder > progressDelta) {
                progress += progressDelta;
                progressDelta = 0;
                animatedProgressWidth = (float) (progress * 1.0 / max * getWidth());
            } else {
                progressDelta -= levelRemainder;
                ++wraps;
                progress = 0;
                animatedProgressWidth = 0;
                max = (nextMaximumFunction != null) ? nextMaximumFunction.next(wraps) : max;
            }
        }
        invalidate();
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
                animatedProgressWidth,
                (float) (progress * 1.0 / max * getWidth()));
        va.setInterpolator(new AccelerateInterpolator());
        va.setDuration(Math.min(
                getResources().getInteger(R.integer.animation_durationWrappingProgressBarBase)
                        + getResources().getInteger(
                                R.integer.animation_durationWrappingProgressBarPerDelta)
                        * progressDelta,
                getResources().getInteger(R.integer.animation_durationWrappingProgressBarMax)));
        va.addUpdateListener(animation -> {
            animatedProgressWidth = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (progressRemainder < 0) {
                    isAnimatingWidth = false;
                    if (onAnimateAddEndEventListener != null) {
                        onAnimateAddEndEventListener.run();
                    }
                    return;
                }

                ++wraps;
                progress = 0;
                animatedProgressWidth = 0;
                if (nextMaximumFunction != null) {
                    max = nextMaximumFunction.next(wraps);
                }
                if (onWrapEventListener != null) {
                    onWrapEventListener.onWrap(wraps, max);
                }
                animateAddProgress(progressRemainder);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                isAnimatingWidth = true;
            }
        });
        va.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.removeColorThemeEventHandler(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.addColorThemeEventHandler(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);

        // Fill progress bar
        float progressWidth = (float) (progress * 1.0 / max * getWidth());
        if (!isAnimatingWidth) {
            animatedProgressWidth = progressWidth;
        }
        canvas.drawRect(
                0,
                0,
                progressWidth,
                getHeight(),
                progressCalculatedColor);
        canvas.drawRect(
                0,
                0,
                isAnimatingWidth ? animatedProgressWidth : progressWidth,
                getHeight(),
                progressColor);

        // Draw text
        String text = progress + " / " + max + " (total: " + total + ")";
        textPaint.getTextBounds(text, 0, text.length(), calculatedTextBounds);
        canvas.drawText(text, 10,
                calculatedTextBounds.height() + (getHeight() - calculatedTextBounds.height()) / 2,
                textPaint);
    }

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        backgroundColor.setColor(colorTheme.background);
        progressColor.setColor(colorTheme.primaryDark);
        progressCalculatedColor.setColor(colorTheme.primaryLight);
        textPaint.setColor(colorTheme.textOnPrimaryDark);
        postInvalidate();
    }
}
