package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.widget.ViewFactory
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.ColorThemeManager

/** Styles a menu. This is the driver to layout_widget_menu.xml. */
class MenuLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {
    // Dependencies
    private @Inject lateinit var colorThemeManager: ColorThemeManager
    private @Inject lateinit var viewFactory: ViewFactory

    private lateinit var menu: LinearLayout
    private val menuOptions = ArrayList<TextView>()

    var isOpen: Boolean = false
        private set
    var onShowListener: (() -> Unit)? = null
    var onHideListener: (() -> Unit)? = null

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        View.inflate(context, R.layout.layout_widget_menu, this)
    }

    /**
     * Adds a menu option to this menu.
     * @param resId The display title string resource id.
     * @param closeOnSelect Whether or not to close the menu after the option was selected.
     * @param action The action to perform when the option is clicked.
     * @return The text view that represents this menu option.
     */
    fun addMenuOption(@StringRes resId: Int, closeOnSelect: Boolean, action: () -> Unit): TextView {
        val result = viewFactory.create<TextView>()
        result.text = resources.getText(resId)
        result.gravity = Gravity.CENTER
        result.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.menuLayout_textSize))
        result.isClickable = true
        result.setOnClickListener {
            if (closeOnSelect) {
                hide()
            }
            action()
        }

        menu.addView(result)
        result.layoutParams.height = resources.getDimension(R.dimen.menuLayout_optionHeight).toInt()
        result.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        menuOptions.add(result)
        return result
    }

    /**
     * Animate opens the menu if it's not already. The callback is called after the menu is fully
     * open. The callback is immediately called if the menu is already open. This method will update
     * colors.
     */
    fun show() {
        if (isOpen) {
            onShowListener?.invoke()
            return
        }

        isOpen = true
        val aa = AlphaAnimation(0f, 1f)
        aa.interpolator = AccelerateInterpolator()
        aa.duration = resources.getInteger(R.integer.animation_durationResponsive).toLong()
        aa.fillAfter = true
        aa.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                clearAnimation()
                onShowListener?.invoke()
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        })
        startAnimation(aa)
    }

    /**
     * Animate closes the menu if it's not already closed. The callback is called after the menu is
     * fully closed. The callback is immediately called if the menu is already closed.
     */
    fun hide() {
        if (!isOpen) {
            onHideListener?.invoke()
            return
        }

        isOpen = false
        val aa = AlphaAnimation(1f, 0f)
        aa.interpolator = AccelerateInterpolator()
        aa.duration = resources.getInteger(R.integer.animation_durationResponsive).toLong()
        aa.fillAfter = true
        aa.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                clearAnimation()
                onHideListener?.invoke()
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        })
        startAnimation(aa)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        menu = findViewById(R.id.menuMenu) as LinearLayout

        isClickable = true
        setOnClickListener { hide() }

        post {
            colorThemeManager.bindView(this, {
                menu.setBackgroundColor(colorThemeManager.currentColorTheme.backgroundLight)
                menuOptions.forEach { it.setTextColor(colorThemeManager.currentColorTheme.text) }
            })
        }
    }
}
