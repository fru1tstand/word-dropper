package me.fru1t.worddropper.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import me.fru1t.android.database.Row
import me.fru1t.android.slik.annotations.ImplementedBy
import me.fru1t.worddropper.settings.Difficulty

/**
 * Provides an interface for interacting with a persistent data storage scheme. See
 * [DatabaseUtilsImpl] for the standard implementation.
 */
@ImplementedBy(DatabaseUtilsImpl::class)
interface DatabaseUtils {
    /** Verifies the database is up and running */
    fun startup()

    /** Re-creates all tables in the database, deleting any information in the process. */
    fun createTables(existingDb: SQLiteDatabase? = null)

    /** Returns the database size as a human-readable string */
    fun getDatabaseSize(): String

    /** Creates a new game entry in the database, returning the game row id */
    fun startGame(
            difficulty: Difficulty, boardState: String, movesEarned: Int, scramblesEarned: Int)
            : Long

    /** Ends the game given the game row [gameId] */
    fun endGame(gameId: Long)

    /** Arbitrarily [update] a game given its row [gameId] */
    fun updateGame(gameId: Long, update: (ContentValues) -> Unit)

    /**
     * Adds a [word] to a game given its row [gameId] storing the [pointValue] of the word as well
     * as updating the game's [score] and [newBoardState]. Returns whether or not the update was
     * successful.
     */
    fun addGameMove(
            gameId: Long, word: String, pointValue: Int, score: Int, newBoardState: String): Boolean

    /** Retrieves all moves from a game given the game's row [gameId] */
    fun getGameMoves(gameId: Long): ArrayList<String>

    /** Retrieves [columns] from [tableName] given the row's [id] */
    fun getRowFromId(tableName: String, id: Long, columns: Array<String>): Row?

    /**
     * Performs a [query] binding [args] and executes [action] for every result row. Returns true
     * if 1 or more rows returned, otherwise false.
     */
    fun forEachResult(query: String, args: Array<String>?, action: (Cursor) -> Unit): Boolean

    /**
     * Performs a [query] binding [args] and executes [action] asserting there is only a single
     * result from the query. Returns true if 1 row returned, otherwise false.
     */
    fun forResult(query: String, args: Array<String>?, action: (Cursor) -> Unit): Boolean
}
