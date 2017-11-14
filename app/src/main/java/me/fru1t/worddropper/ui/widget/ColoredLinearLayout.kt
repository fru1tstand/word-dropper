package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.colortheme.AttributeMap
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxyFactory
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml

/** An automatically colored LinearLayout  */
open class ColoredLinearLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    private @Inject lateinit var factory: ColorThemeViewProxyFactory
    private val proxy: ColorThemeViewProxy

    init {
        Slik.get(WordDropperApplication::class).inject(this, ColoredLinearLayout::class)
        proxy = factory.create(
                this,
                attrs,
                R.styleable.ColoredLinearLayout,
                AttributeMap(
                        R.styleable.ColoredLinearLayout_backgroundColorTheme,
                        ColorThemeXml.BACKGROUND,
                        { setBackgroundColor(it) }))
    }
}
