package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.annotation.AttrRes
import android.support.annotation.Px
import android.util.AttributeSet
import android.widget.ListView
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.colortheme.AttributeMap
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxyFactory
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml

/** A ListView that's automatically colored by the color theme. */
open class ColoredListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0)
    : ListView(context, attrs, defStyleAttr) {
    private @Inject lateinit var factory: ColorThemeViewProxyFactory
    private val proxy: ColorThemeViewProxy

    private var defaultHeight: Int? = null
    @Px var maxHeight: Int = 0

    init {
        Slik.get(WordDropperApplication::class).inject(this, ColoredListView::class)
        proxy = factory.create(
                this,
                attrs,
                R.styleable.ColoredListView,
                AttributeMap(
                        R.styleable.ColoredListView_backgroundColorTheme,
                        ColorThemeXml.TEXT_BLEND, {
                            this@ColoredListView.divider = ColorDrawable(it)
                            this@ColoredListView.dividerHeight = 1
                        }))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (maxHeight == NO_MAX_HEIGHT) {
            return
        }

        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (height == maxHeight) {
            return
        }

        if (height > maxHeight) {
            // Store initial height as default
            if (defaultHeight == null) {
                defaultHeight = layoutParams.height
            }
            layoutParams.height = maxHeight
            requestLayout()
        } else {
            if (defaultHeight != null) {
                layoutParams.height = defaultHeight!!
            }
        }
    }

    companion object {
        private val NO_MAX_HEIGHT = -1
    }
}
