package me.fru1t.worddropper.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.layout_settings_header.view.*

import me.fru1t.worddropper.R

/**
 * Packages a title and a divider together to form a settings header.
 */
class SettingsHeader @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_settings_header, this)

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.SettingsHeader)
        title.text = styledAttrs.getText(R.styleable.SettingsHeader_settingsHeaderText)
        styledAttrs.recycle()
    }
}
