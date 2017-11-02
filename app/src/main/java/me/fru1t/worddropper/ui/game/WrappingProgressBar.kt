package me.fru1t.worddropper.ui.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.ColorThemeManager

/**
 * A minimalist-designed progress bar that shows the progress via text. On hitting the maximum, the
 * progress bar will wrap and start at zero again.
 */
class WrappingProgressBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private @Inject lateinit var colorThemeManager: ColorThemeManager

    // Internal colors for drawing purposes
    private val backgroundColor = Paint()
    private val progressColor = Paint()
    private val progressCalculatedColor = Paint()
    private val textPaint = Paint()

    private var isAnimatingWidth = false
    private var animatedProgressWidth = 0f
    private val calculatedTextBounds = Rect()

    // Public quantities
    var max: Int = 1
        /** Sets the current wrap's maximum and animates the progress to match. */
        set(value) {
            field = value
            animateAddProgress(0)
        }
    var progress: Int = 0
        private set
    var wraps: Int = 0
        private set
    var grandTotal: Int = 0
        private set

    // Functions
    /**
     * A function to determine the maximum value the progress bar should have for the given wrap.
     * Setting to null makes the progress bar never change maximums.
     */
    var setMaximumFunction: ((wraps: Int) -> Int)? = null
        set(value) {
            field = value
            max = value?.invoke(wraps) ?: max
        }
    /** Called when the progress bar wraps. */
    var onWrapEventListener: ((wraps: Int, newMax: Int) -> Unit)? = null
    /** Called when the progress bar fully completes all onAnimateAdd animations. */
    var onAnimateAddEndEventListener: (() -> Unit)? = null

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        colorThemeManager.bindView(this, {
            backgroundColor.color = colorThemeManager.currentColorTheme.background
            progressColor.color = colorThemeManager.currentColorTheme.primaryDark
            progressCalculatedColor.color = colorThemeManager.currentColorTheme.primaryLight
            textPaint.color = colorThemeManager.currentColorTheme.textOnPrimaryDark
            postInvalidate()
        })
        textPaint.textSize = resources.getDimension(R.dimen.wrappingProgressBar_textSize)
        textPaint.typeface = TEXT_TYPE_FACE
    }

    /**
     * Wipes all progress off of this progress bar resetting wraps, total, and progress.
     * Animates current progress to zero.
     */
    fun reset() {
        wraps = 0
        grandTotal = 0
        progress = 0
        max = setMaximumFunction?.invoke(wraps) ?: max
        animateAddProgress(0)
    }

    /** Sets the [total] value within the progress bar, wrapping as necessary. */
    fun setTotal(total: Int) {
        reset()
        addProgress(total)
    }

    /** Adds [progressDelta] to the current progress amount with no animation. */
    fun addProgress(progressDelta: Int) {
        var remainingDelta = progressDelta
        grandTotal += remainingDelta
        while (remainingDelta > 0) {
            val levelRemainder = max - progress
            if (levelRemainder > remainingDelta) {
                progress += remainingDelta
                remainingDelta = 0
                animatedProgressWidth = (progress * 1.0 / max * width).toFloat()
            } else {
                remainingDelta -= levelRemainder
                ++wraps
                progress = 0
                animatedProgressWidth = 0f
                max = setMaximumFunction?.invoke(wraps) ?: max
            }
        }
        invalidate()
    }

    /**
     * Adds the given progress to the current progress amount. Animates overflow by filling the bar
     * and recursively calling animateAddProgress until all progressDelta is consumed.
     */
    fun animateAddProgress(progressDelta: Int) {
        var remainingDelta = progressDelta
        val progressRemainder = remainingDelta + progress - max
        if (progressRemainder > 0) {
            remainingDelta = max - progress
        }
        progress += remainingDelta
        grandTotal += remainingDelta

        val va = ValueAnimator.ofFloat(
                animatedProgressWidth,
                (progress * 1.0 / max * width).toFloat())
        va.interpolator = AccelerateInterpolator()
        va.duration = Math.min(
                resources.getInteger(R.integer.animation_durationWrappingProgressBarBase) + resources.getInteger(
                        R.integer.animation_durationWrappingProgressBarPerDelta) * remainingDelta,
                resources.getInteger(R.integer.animation_durationWrappingProgressBarMax)).toLong()
        va.addUpdateListener { animation ->
            animatedProgressWidth = animation.animatedValue as Float
            postInvalidate()
        }
        va.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (progressRemainder < 0) {
                    isAnimatingWidth = false
                    onAnimateAddEndEventListener?.invoke()
                    return
                }

                ++wraps
                progress = 0
                animatedProgressWidth = 0f
                max = setMaximumFunction?.invoke(wraps) ?: max
                onWrapEventListener?.invoke(wraps, max)
                animateAddProgress(progressRemainder)
            }

            override fun onAnimationStart(animation: Animator) {
                isAnimatingWidth = true
            }
        })
        va.start()
    }

    override fun onDraw(canvas: Canvas) {
        // Fill background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundColor)

        // Fill progress bar
        val progressWidth = (progress * 1.0 / max * width).toFloat()
        if (!isAnimatingWidth) {
            animatedProgressWidth = progressWidth
        }
        canvas.drawRect(
                0f,
                0f,
                progressWidth,
                height.toFloat(),
                progressCalculatedColor)
        canvas.drawRect(
                0f,
                0f,
                if (isAnimatingWidth) animatedProgressWidth else progressWidth,
                height.toFloat(),
                progressColor)

        // Draw text
        val text = progress.toString() + " / " + max + " (total: " + grandTotal + ")"
        textPaint.getTextBounds(text, 0, text.length, calculatedTextBounds)
        canvas.drawText(text, 10f,
                (calculatedTextBounds.height() + (height - calculatedTextBounds.height()) / 2).toFloat(),
                textPaint)
    }

    companion object {
        private val TEXT_TYPE_FACE = Typeface.DEFAULT
    }
}
