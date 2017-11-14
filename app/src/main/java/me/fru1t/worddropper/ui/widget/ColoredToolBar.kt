package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import me.fru1t.android.content.res.ResourceManager
import me.fru1t.android.content.res.use
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.view.find
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.colortheme.AttributeMap
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxyFactory
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml

/** Emulates the Android ToolBar with a single text field. */
class ColoredToolBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    private @Inject lateinit var colorThemeViewProxyFactory: ColorThemeViewProxyFactory
    private @Inject lateinit var res: ResourceManager
    private val proxy: ColorThemeViewProxy

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        val root = LayoutInflater
                .from(context)
                .inflate(R.layout.layout_widget_colored_tool_bar, this)
        val title = root.find<TextView>(R.id.title)

        proxy = colorThemeViewProxyFactory.create(
                this,
                attrs,
                R.styleable.ColoredToolBar,
                AttributeMap(
                        R.styleable.ColoredToolBar_backgroundColorTheme,
                        ColorThemeXml.PRIMARY,
                        { setBackgroundColor(it) }),
                AttributeMap(
                        R.styleable.ColoredToolBar_textColorTheme,
                        ColorThemeXml.TEXT_ON_PRIMARY,
                        { title.setTextColor(it) }))

        context.obtainStyledAttributes(attrs, R.styleable.ColoredToolBar).use {
            title.text = it.getText(R.styleable.ColoredToolBar_toolBarText)
        }

        val paddingHorizontal = res.d(R.dimen.app_edgeSpace).toInt()
        val paddingVertical = res.d(R.dimen.app_vSpace).toInt()
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
    }
}
