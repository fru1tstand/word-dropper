package me.fru1t.worddropper.database.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.fru1t.worddropper.settings.Difficulty;
import me.fru1t.worddropper.widget.TileBoard;

/**
 * Each row represents a single game. Contains information like the start date, game state, board
 * state, and difficulty.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Game implements BaseColumns {
    public static final String TABLE_NAME = "game";

    /**
     * INTEGER: The device's current unix time (seconds).
     */
    public static final String COLUMN_UNIX_START = "unix_start";

    /**
     * INTEGER: The game's current status denoted by {@link #STATUS_COMPLETED} and
     * {@link #STATUS_IN_PROGRESS}.
     */
    public static final String COLUMN_STATUS = "status";

    /**
     * TEXT: The {@link Difficulty#name()} of the game's difficulty.
     */
    public static final String COLUMN_DIFFICULTY = "difficulty";

    /**
     * TEXT: The string serialization of the board using {@link TileBoard#getBoardState()}.
     */
    public static final String COLUMN_BOARD_STATE = "board_state";

    /**
     * INTEGER: The number of moves earned in this game
     */
    public static final String COLUMN_MOVES_EARNED = "moves_earned";

    /**
     * INTEGER: The number of scrambles used in this game.
     */
    public static final String COLUMN_SCRAMBLES_USED = "scrambles_used";

    /**
     * INTEGER: The number of scrambles earned in this game.
     */
    public static final String COLUMN_SCRAMBLES_EARNED = "scrambles_earned";

    /**
     * INTEGER: The current level (eg. wraps + 1) of the game.
     */
    public static final String COLUMN_LEVEL = "level";

    /**
     * INTEGER: The score of the game
     */
    public static final String COLUMN_SCORE = "score";

    public static final int STATUS_COMPLETED = 0;
    public static final int STATUS_IN_PROGRESS = 1;

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_UNIX_START         + " INTEGER NOT NULL,"
            + COLUMN_STATUS             + " INTEGER NOT NULL,"
            + COLUMN_MOVES_EARNED       + " INTEGER NOT NULL,"
            + COLUMN_SCRAMBLES_EARNED   + " INTEGER NOT NULL,"
            + COLUMN_SCRAMBLES_USED     + " INTEGER NOT NULL,"
            + COLUMN_LEVEL              + " INTEGER NOT NULL,"
            + COLUMN_SCORE              + " INTEGER NOT NULL,"
            + COLUMN_DIFFICULTY         + " TEXT NOT NULL,"
            + COLUMN_BOARD_STATE        + " TEXT NOT NULL"
            + ");";

    public static void updateById(SQLiteDatabase db, ContentValues values, long gameId) {
        db.update(TABLE_NAME, values, _ID + " = ?", new String[] { gameId + "" });
    }
}
