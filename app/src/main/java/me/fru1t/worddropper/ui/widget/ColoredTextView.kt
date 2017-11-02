package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.colortheme.AttributeMap
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxyFactory
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml

/**
 * A text view wrapper that automatically updates its colors on colorTheme change.
 */
class ColoredTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatTextView(context, attrs, defStyleAttr) {
    private @Inject lateinit var factory: ColorThemeViewProxyFactory
    private val proxy: ColorThemeViewProxy

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        proxy = factory.create(
                this,
                attrs,
                R.styleable.ColoredTextView,
                AttributeMap(
                        R.styleable.ColoredTextView_textColorTheme,
                        ColorThemeXml.TEXT,
                        { setTextColor(it) }),
                AttributeMap(
                        R.styleable.ColoredTextView_backgroundColorTheme,
                        ColorThemeXml.TRANSPARENT,
                        { setBackgroundColor(it) }))
    }
}
