package me.fru1t.worddropper.ui.mainmenu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.View
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.ColorThemeManager
import java.util.Random

/** A single falling letter containing its own speed, positioning, and drawing methods. */
private class FallingLetter(
        private val textPaint: Paint,
        private val random: Random) {
    private var s: String? = null
    var yVelocity: Float = 0f
        private set
    var y: Float = 0f
        private set
    var x: Float = 0f
        private set

    fun randomize(width: Int, height: Int) {
        s = ('A' + random.nextInt('Z' - 'A')).toString()
        yVelocity = MIN_VELOCITY + random.nextFloat() * (MAX_VELOCITY - MIN_VELOCITY)
        x = random.nextInt(width).toFloat()
        y = (-1 * (random.nextInt(height) + LETTER_HEIGHT)).toFloat()
    }

    fun draw(canvas: Canvas) {
        if (s == null) {
            y = Float.MAX_VALUE
            return
        }

        canvas.drawText(s, x, y, textPaint)

        // Update
        y += yVelocity
    }

    companion object {
        private val MIN_VELOCITY = 1f
        private val MAX_VELOCITY = 5f
        val LETTER_HEIGHT = 60
    }
}

/** An animated background for the Main Menu. */
class MainMenuBackground @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private @Inject lateinit var colorThemeManager: ColorThemeManager
    private val fallingLetters: Array<FallingLetter>
    private val random = Random()
    private val textPaint = Paint()

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        colorThemeManager.bindView(this, {
            textPaint.color = colorThemeManager.currentColorTheme.backgroundLight
        })

        fallingLetters = Array(
                FALLING_LETTERS_COUNT,
                { _ -> FallingLetter(textPaint, random) })

        // TODO: Why does this leak into Tile.java?????????
        // textPaint.setMaskFilter(new BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.NORMAL));
        textPaint.textSize = resources.getDimension(R.dimen.gameScreen_tileTextSize)

        post {
            postInvalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        fallingLetters.forEach {
            it.draw(canvas)
            if (it.y > height + FallingLetter.LETTER_HEIGHT) {
                it.randomize(width, height)
            }
        }
        invalidate()
    }

    companion object {
        private val FALLING_LETTERS_COUNT = 20
    }
}
