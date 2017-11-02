package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.widget.FrameLayout
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.colortheme.AttributeMap
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxyFactory
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml

/** A FrameLayout that's automatically colored by the color theme.  */
class ColoredFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private @Inject lateinit var factory: ColorThemeViewProxyFactory
    private val proxy: ColorThemeViewProxy

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        proxy = factory.create(
                this,
                attrs,
                R.styleable.ColoredFrameLayout,
                AttributeMap(
                        R.styleable.ColoredFrameLayout_backgroundColorTheme,
                        ColorThemeXml.BACKGROUND,
                        { setBackgroundColor(it) }))
    }
}
