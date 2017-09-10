package me.fru1t.worddropper.database.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Each row represents a single game. Contains information like the start date, game state, board
 * state, and difficulty.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Game implements BaseColumns {
    public static final String TABLE_NAME = "game";

    public static final String COLUMN_UNIX_START = "unix_start";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_BOARD_STATE = "board_state";

    public static final int STATUS_COMPLETED = 0;
    public static final int STATUS_IN_PROGRESS = 1;

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UNIX_START     + " INTEGER NOT NULL, "
            + COLUMN_STATUS         + " INTEGER NOT NULL, "
            + COLUMN_DIFFICULTY     + " TEXT NOT NULL, "
            + COLUMN_BOARD_STATE    + " TEXT NOT NULL"
            + ");";

    public static void updateById(SQLiteDatabase db, ContentValues values, long gameId) {
        db.update(TABLE_NAME, values, _ID + "=?", new String[] { gameId + "" });
    }
}
