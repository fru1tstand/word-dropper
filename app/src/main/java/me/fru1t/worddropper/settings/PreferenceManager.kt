package me.fru1t.worddropper.settings

import android.content.Context
import android.support.annotation.StringRes

/**
 * An interface for storing and retrieving preferences via string resource values. See
 * [PreferenceManagerImpl] for the standard implementation.
 */
abstract class PreferenceManager(private val context: Context) {
    private val changeListeners = HashSet<(String) -> Unit>()

    /** Retrieves [keyRes] from preferences or [defaultValue] if it's not defined. */
    fun getString(@StringRes keyRes: Int, defaultValue: String): String =
            getString(context.getString(keyRes), defaultValue)

    /** Stores [value] at the string resource [keyRes] asynchronously. */
    fun applyString(@StringRes keyRes: Int, value: String) {
        val key = context.getString(keyRes)
        applyString(key, value)
        changeListeners.forEach { it(key) }
    }

    /** Retrieves the preference stored at [key] or [defaultValue] if no preference is found. */
    protected abstract fun getString(key: String, defaultValue: String): String

    /** Stores [value] at [key] asynchronously */
    protected abstract fun applyString(key: String, value: String)

    fun addChangeListener(listener: (String) -> Unit): Boolean = changeListeners.add(listener)
    fun removeChangeListener(listener: (String) -> Unit): Boolean = changeListeners.remove(listener)
}
