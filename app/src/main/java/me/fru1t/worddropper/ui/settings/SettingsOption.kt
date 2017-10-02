package me.fru1t.worddropper.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.layout_settings_option.view.*
import me.fru1t.worddropper.R

/**
 * An option within the settings activity.
 */
class SettingsOption @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    init {
        orientation = VERTICAL
        isClickable = true
        LayoutInflater.from(context).inflate(R.layout.layout_settings_option, this)

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.SettingsOption)
        title.text = styledAttrs.getText(R.styleable.SettingsOption_settingsButtonTitle)
        description.text = styledAttrs.getText(R.styleable.SettingsOption_settingsButtonDescription)
        styledAttrs.recycle()
    }
}
