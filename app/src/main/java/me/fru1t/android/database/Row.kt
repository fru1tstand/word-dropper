package me.fru1t.android.database

import java.util.HashMap

/** A simple extension of HashMap providing a default-able #get method. */
class Row : HashMap<String, String>() {
    /** Returns the int indexed at [key] or returns [defaultValue] if the key doesn't exist. */
    fun getInt(key: String, defaultValue: Int): Int {
        val result = super.get(key)
        return if (result != null) Integer.parseInt(result) else defaultValue
    }

    /** Returns the long indexed at [key] or returns [defaultValue] if the key doesn't exist */
    fun getLong(key: String, defaultValue: Long): Long {
        val result = super.get(key)
        return if (result != null) java.lang.Long.parseLong(result) else defaultValue
    }

    /** Returns the string indexed at [key] or returns [defaultValue] if the key doesn't exist */
    fun getString(key: String, defaultValue: String): String {
        val result = super.get(key)
        return result ?: defaultValue
    }
}
