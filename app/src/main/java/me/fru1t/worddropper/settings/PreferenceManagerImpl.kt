package me.fru1t.worddropper.settings

import android.content.Context
import android.content.SharedPreferences
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Named
import me.fru1t.android.slik.annotations.Singleton

/** The standard preference manager implementation backed by Android shared preferences. */
@Inject
@Singleton
class PreferenceManagerImpl(
        private @Named("PrefFileName") val sharedPreferencesFileName: String,
        context: Context) : PreferenceManager(context) {
    private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String =
            sharedPreferences.getString(key, defaultValue)

    override fun applyString(key: String, value: String) =
            sharedPreferences.edit().putString(key, value).apply()
}
