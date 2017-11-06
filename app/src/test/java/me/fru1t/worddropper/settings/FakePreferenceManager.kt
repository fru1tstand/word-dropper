package me.fru1t.worddropper.settings

import android.content.Context
import android.support.annotation.StringRes
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/** A preference manager backed by a HashMap */
open class FakePreferenceManager : PreferenceManager(mock(Context::class.java)) {
    private val preferences = HashMap<String, Any>()

    /** Stores a preference */
    fun sideload(@StringRes resId: Int, value: Any) {
        `when`(context.getString(resId)).thenReturn(resId.toString())
        preferences.put(resId.toString(), value)
    }

    override fun getString(key: String, defaultValue: String): String =
            get<String>(key) ?: defaultValue

    override fun applyString(key: String, value: String) {
        preferences.put(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> get(key: String): T? = preferences[key] as T
}
