package me.fru1t.android.content

import android.content.Context
import android.content.Intent
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Singleton

/** A factory that creates intents. */
@Inject
@Singleton
class IntentFactory(val context: Context) {
    inline fun <reified T> create() = Intent(context, T::class.java)
}
