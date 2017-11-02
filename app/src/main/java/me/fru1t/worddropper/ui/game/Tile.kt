package me.fru1t.worddropper.ui.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.ColorThemeManager

/** A single interactable tile within the tile board */
class Tile(context: Context) : View(context) {
    private @Inject lateinit var colorThemeManager: ColorThemeManager

    private val textPaint = Paint()
    private val backgroundColor = Paint()
    private val textBounds = Rect()

    var size = 0
    var letter = ' '

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        colorThemeManager.bindView(this, this::release)
        textPaint.textSize = resources.getDimension(R.dimen.gameScreen_tileTextSize)
    }

    fun press() {
        backgroundColor.color = colorThemeManager.currentColorTheme.primary
        textPaint.color = colorThemeManager.currentColorTheme.textOnPrimary
        postInvalidate()
    }

    fun release() {
        backgroundColor.color = colorThemeManager.currentColorTheme.background
        textPaint.color = colorThemeManager.currentColorTheme.text
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), backgroundColor)
        textPaint.getTextBounds(letter + "", 0, 1, textBounds)
        canvas.drawText(letter + "",
                (size / 2 - textBounds.centerX()).toFloat(),
                (size / 2 - textBounds.centerY()).toFloat(),
                textPaint)
    }
}
