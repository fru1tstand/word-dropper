package me.fru1t.worddropper.settings

import android.content.Context

/** A preference manager backed by a HashMap */
open class FakePreferenceManager(context: Context) : PreferenceManager(context) {
    private val preferences = HashMap<String, Any>()

    override fun getString(key: String, defaultValue: String): String =
            get<String>(key) ?: defaultValue

    override fun applyString(key: String, value: String) {
        preferences.put(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> get(key: String): T? = preferences[key] as T
}
