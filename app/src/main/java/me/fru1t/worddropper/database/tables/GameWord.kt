package me.fru1t.worddropper.database.tables

import android.provider.BaseColumns

/**
 * Each row represents a single move within a single game. Contains information like the game the
 * move was played in, the word the move contained, and the point value given for the word at that
 * time.
 */
object GameWord : BaseColumns {
    val TABLE_NAME = "game_word"

    /** INTEGER The corresponding ID to a valid game. */
    val COLUMN_GAME_ID = "game_id"
    /**
     * INTEGER
     * The point value given to the word at the time (as point values may change in the future).
     */
    val COLUMN_POINT_VALUE = "point_value"
    /** TEXT The word submitted. */
    val COLUMN_WORD = "word"

    val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_GAME_ID + " INTEGER NOT NULL, "
            + COLUMN_POINT_VALUE + " INTEGER NOT NULL, "
            + COLUMN_WORD + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + COLUMN_GAME_ID + ") "
            + "REFERENCES " + Game.TABLE_NAME + "(" + BaseColumns._ID + ")"
            + ");")
}
