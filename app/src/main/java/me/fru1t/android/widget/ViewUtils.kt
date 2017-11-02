package me.fru1t.android.widget

import android.view.View
import android.view.ViewGroup
import java.util.ArrayList
import kotlin.reflect.KClass

/** Utility methods for manipulating XML layouts. */
object ViewUtils {
    /** Recursively retrieves all [T] children from [root]. Ignores inheritance. */
    inline fun <reified T : View> getElementsByTagName(root: ViewGroup): List<T> =
            getElementsByTagName(root, T::class, true)

    /** Retrieves all [T] from [root] optionally [includeChildren]. Ignores inheritance. */
    inline fun <reified T : View> getElementsByTagName(
            root: ViewGroup, includeChildren: Boolean): List<T> =
        getElementsByTagName(root, T::class, includeChildren)

    /** Retrieves all [tag]s from [root] optionally [includeChildren]. Ignores inheritance. */
    fun <T : View> getElementsByTagName(
            root: ViewGroup, tag: KClass<T>, includeChildren: Boolean): List<T> {
        val result = ArrayList<T>()
        for (i in root.childCount - 1 downTo 0) {
            val v = root.getChildAt(i)
            if (v::class == tag) {
                @Suppress("UNCHECKED_CAST")
                result.add(v as T)
            }

            if (includeChildren && v is ViewGroup) {
                result.addAll(getElementsByTagName(v, tag, true))
            }
        }

        return result
    }
}
