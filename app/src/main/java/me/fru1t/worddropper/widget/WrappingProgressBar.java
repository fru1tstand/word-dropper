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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;

/**
 * A minimalist-designed progress bar that shows the progress via text. On hitting the maximum, the
 * progress bar will wrap and start at zero again.
 */
public class WrappingProgressBar extends View {
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

    private float calculatedProgressBarWidth;
    private final Rect calculatedTextBounds;

    public WrappingProgressBar(Context context) {
        super(context);
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

        calculatedProgressBarWidth = 0;
        calculatedTextBounds = new Rect();

        nextMaximumFunction = null;
        onWrapEventListener = null;
    }

    public void updateColors() {
        backgroundColor.setColor(app.getColorTheme().background);
        progressColor.setColor(app.getColorTheme().primaryDark);
        progressCalculatedColor.setColor(app.getColorTheme().primaryLight);
        textPaint.setColor(app.getColorTheme().textOnPrimaryDark);
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
        va.setInterpolator(new BounceInterpolator());
        va.setDuration(Math.min(
                getResources().getInteger(R.integer.animation_durationWrappingProgressBarBase)
                        + getResources().getInteger(
                                R.integer.animation_durationWrappingProgressBarPerDelta)
                        * progressDelta,
                getResources().getInteger(R.integer.animation_durationWrappingProgressBarMax)));
        va.addUpdateListener(animation -> {
            calculatedProgressBarWidth = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (progressRemainder < 0) {
                    if (onAnimateAddEndEventListener != null) {
                        onAnimateAddEndEventListener.run();
                    }
                    return;
                }

                ++wraps;
                progress = 0;
                calculatedProgressBarWidth = 0;
                if (nextMaximumFunction != null) {
                    max = nextMaximumFunction.next(wraps);
                }
                if (onWrapEventListener != null) {
                    onWrapEventListener.onWrap(wraps, max);
                }
                animateAddProgress(progressRemainder);
            }
        });

        va.start();
    }
}
