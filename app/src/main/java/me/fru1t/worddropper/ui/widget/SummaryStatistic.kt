package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import me.fru1t.android.content.res.use
import me.fru1t.android.view.find
import me.fru1t.worddropper.R

/** Shows a small title (eg. "Avg Word Length") and a large value underneath (eg. "5.5") */
class SummaryStatistic @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ColoredLinearLayout(context, attrs, defStyleAttr) {

    val root = LayoutInflater.from(context).inflate(R.layout.layout_summary_statistic, this)!!
    val title = root.find<ColoredTextView>(R.id.title)
    val value = root.find<ColoredTextView>(R.id.value)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SummaryStatistic).use {
            title.text = it.getText(R.styleable.SummaryStatistic_titleText)
        }
    }
}
