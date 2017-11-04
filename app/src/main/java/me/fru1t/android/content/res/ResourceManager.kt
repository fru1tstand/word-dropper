package me.fru1t.android.content.res

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.AnimRes
import android.support.annotation.ArrayRes
import android.support.annotation.BoolRes
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.IntegerRes
import android.support.annotation.LayoutRes
import android.support.annotation.PluralsRes
import android.support.annotation.StringRes
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Singleton

/** An injectable class to fetch resources. */
@Inject
@Singleton
class ResourceManager(private val context: Context) {
    /** [Resources.getAnimation] */
    fun a(@AnimRes id: Int) = context.resources.getAnimation(id)!!

    /** [Resources.getBoolean] */
    fun b(@BoolRes id: Int) = context.resources.getBoolean(id)

    /** [Resources.getColor] */
    fun c(@ColorRes id: Int) = context.resources.getColor(id)

    /** [Resources.getColor] */
    @SuppressLint("NewApi") // TODO: Track https://youtrack.jetbrains.com/issue/KT-21099
    fun c(@ColorRes id: Int, theme: Resources.Theme? = null) =
            when {
                theme == null -> context.resources.getColor(id, theme)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->
                    context.resources.getColor(id, theme)
                else -> throw RuntimeException("Calling Resources#getColor(Int, Theme) " +
                        "requires Android ${Build.VERSION_CODES.LOLLIPOP}")
            }

    /** [Resources.getDimension] */
    fun d(@DimenRes id: Int) = context.resources.getDimension(id)

    /** [Resources.getDimensionPixelOffset] */
    fun dpo(@DimenRes id: Int) = context.resources.getDimensionPixelOffset(id)

    /** [Resources.getDimensionPixelSize] */
    fun dps(@DimenRes id: Int) = context.resources.getDimensionPixelSize(id)

    /** [Resources.displayMetrics] */
    fun dm() = context.resources.displayMetrics!!

    /** [Resources.getDrawable] and [Resources.getDrawableForDensity] */
    fun draw(@DrawableRes id: Int, density: Int? = null, theme: Resources.Theme? = null): Drawable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return if (density == null) {
                context.resources.getDrawable(id, theme)!!
            } else {
                context.resources.getDrawableForDensity(id, density, theme)!!
            }
        }

        if (density != null || theme != null) {
            throw RuntimeException("Calling Resources#getDrawable(Int, Theme) " +
                    "requires Android ${Build.VERSION_CODES.LOLLIPOP}")
        }

        return context.resources.getDrawable(id)!!
    }

    /** [Resources.getIntArray] */
    fun iArray(@ArrayRes id: Int) = context.resources.getIntArray(id)!!

    /** [Resources.getInteger] */
    fun i(@IntegerRes id: Int) = context.resources.getInteger(id)

    /** [Resources.getLayout] */
    fun l(@LayoutRes id: Int) = context.resources.getLayout(id)!!

    /** [Resources.getQuantityString] */
    fun qs(@PluralsRes id: Int, quantity: Int, vararg args: Any) =
            if (args.isEmpty()) {
                context.resources.getQuantityString(id, quantity)!!
            } else {
                context.resources.getQuantityString(id, quantity, *args)!!
            }

    /** [Resources.getQuantityText] */
    fun qt(@PluralsRes id: Int, quantity: Int) = context.resources.getQuantityText(id, quantity)!!

    /** [Resources.getString] */
    fun s(@StringRes id: Int, vararg args: Any) =
            if (args.isEmpty()) {
                context.resources.getString(id)!!
            } else {
                context.resources.getString(id, *args)!!
            }

    /** [Resources.getStringArray] */
    fun sArray(@ArrayRes id: Int) = context.resources.getStringArray(id)

    /** [Resources.getText] */
    fun t(@StringRes id: Int, default: CharSequence? = null) =
            context.resources.getText(id) ?: default

    /** [Resources.getTextArray] */
    fun tArray(@ArrayRes id: Int) = context.resources.getTextArray(id)
}
