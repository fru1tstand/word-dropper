package me.fru1t.worddropper.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout

import me.fru1t.worddropper.R
import me.fru1t.worddropper.ui.widget.ColoredTextView

/**
 * Packages a title and a divider together to form a settings header.
 */
class SettingsHeaderLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_settings_header, this)

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.SettingsHeaderLayout)
        (findViewById(R.id.title) as ColoredTextView).text = styledAttrs.getText(R.styleable.SettingsHeaderLayout_settingsHeaderText)
        styledAttrs.recycle()
    }
}
