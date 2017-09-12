package me.fru1t.android.database;

import java.util.HashMap;

/**
 * A simple extension of HashMap providing a default-able #get method.
 */
public class Row extends HashMap<String, String> {
    /**
     * @return Returns the value at the key or default value if it doesn't exist.
     * @throws NumberFormatException Thrown if the row data isn't an int.
     */
    public int getInt(String key, int defaultValue) {
        String result = super.get(key);
        return (result != null) ? Integer.parseInt(result) : defaultValue;
    }

    /**
     * @return Returns the value at the key or default value if it doesn't exist.
     * @throws NumberFormatException Thrown if the row data isn't an long.
     */
    public long getLong(String key, long defaultValue) {
        String result = super.get(key);
        return (result != null) ? Long.parseLong(result) : defaultValue;
    }

    /**
     * @return Returns the value at the key or default value if it doesn't exist.
     */
    public String getString(String key, String defaultValue) {
        String result = super.get(key);
        return (result != null) ? result : defaultValue;
    }
}
