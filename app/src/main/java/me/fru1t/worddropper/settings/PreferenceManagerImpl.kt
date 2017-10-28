package me.fru1t.worddropper.settings

import android.content.Context
import android.content.SharedPreferences
import me.fru1t.android.slick.annotations.Inject
import me.fru1t.android.slick.annotations.Named
import me.fru1t.android.slick.annotations.Singleton

/** The standard preference manager backed by the Android shared preferences. */
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
