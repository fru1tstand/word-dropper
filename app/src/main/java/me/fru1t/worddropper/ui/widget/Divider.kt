package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.colortheme.AttributeMap
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxyFactory
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml

/** A color-able view that acts like an &lt;hr /&gt; */
class Divider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private @Inject lateinit var colorThemeViewProxyFactory: ColorThemeViewProxyFactory
    private val proxy: ColorThemeViewProxy

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        if (attrs == null) {
            throw RuntimeException("Divider requires the backgroundColorTheme attribute, but "
                    + "no attributes were given")
        }

        proxy = colorThemeViewProxyFactory.create(
                this,
                attrs,
                R.styleable.Divider,
                AttributeMap(
                        R.styleable.Divider_backgroundColorTheme,
                        ColorThemeXml.TEXT,
                        { setBackgroundColor(it) }))
    }
}
