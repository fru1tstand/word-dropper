package me.fru1t.worddropper.database.tables

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import me.fru1t.worddropper.settings.Difficulty
import me.fru1t.worddropper.ui.game.TileBoard

/**
 * Each row represents a single game. Contains information like the start date, game state, board
 * state, and difficulty.
 */
object Game : BaseColumns {
    val TABLE_NAME = "game"

    /** INTEGER: The device's current unix time (seconds). */
    val COLUMN_UNIX_START = "unix_start"
    /**
     * INTEGER: The game's current status denoted by [STATUS_COMPLETED] and
     * [STATUS_IN_PROGRESS].
     */
    val COLUMN_STATUS = "status"
    /** TEXT: The [Difficulty.name] of the game's difficulty. */
    val COLUMN_DIFFICULTY = "difficulty"
    /** TEXT: The string serialization of the board using [TileBoard.getBoardState]. */
    val COLUMN_BOARD_STATE = "board_state"
    /** INTEGER: The number of moves earned in this game */
    val COLUMN_MOVES_EARNED = "moves_earned"
    /** INTEGER: The number of scrambles used in this game. */
    val COLUMN_SCRAMBLES_USED = "scrambles_used"
    /** INTEGER: The number of scrambles earned in this game. */
    val COLUMN_SCRAMBLES_EARNED = "scrambles_earned"
    /** INTEGER: The current level (eg. wraps + 1) of the game. */
    val COLUMN_LEVEL = "level"

    val STATUS_COMPLETED = 0
    val STATUS_IN_PROGRESS = 1

    val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_UNIX_START + " INTEGER NOT NULL,"
            + COLUMN_STATUS + " INTEGER NOT NULL,"
            + COLUMN_MOVES_EARNED + " INTEGER NOT NULL,"
            + COLUMN_SCRAMBLES_EARNED + " INTEGER NOT NULL,"
            + COLUMN_SCRAMBLES_USED + " INTEGER NOT NULL,"
            + COLUMN_LEVEL + " INTEGER NOT NULL,"
            + COLUMN_DIFFICULTY + " TEXT NOT NULL,"
            + COLUMN_BOARD_STATE + " TEXT NOT NULL"
            + ");")

    fun updateById(db: SQLiteDatabase, values: ContentValues, gameId: Long) {
        db.update(TABLE_NAME, values, BaseColumns._ID + " = ?", arrayOf(gameId.toString() + ""))
    }
}
