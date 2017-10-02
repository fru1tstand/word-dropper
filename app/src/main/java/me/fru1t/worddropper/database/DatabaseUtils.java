package me.fru1t.worddropper.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

import java.util.ArrayList
import java.util.function.Consumer

import me.fru1t.android.database.Row
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.database.tables.GameWord
import me.fru1t.worddropper.settings.Difficulty

import me.fru1t.worddropper.WordDropperApplication.LOG_TAG

/**
 * Methods to manipulate and retrieve data from the database.
 */
class DatabaseUtils(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val app: WordDropperApplication

    init {
        app = context.applicationContext as WordDropperApplication
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Game.CREATE_TABLE)
        db.execSQL(GameWord.CREATE_TABLE)
        Log.i(LOG_TAG, "Initial database creation successful")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    override fun onOpen(db: SQLiteDatabase) {
        if (app.isDebugging) {
            resetDatabase()
            Log.d(LOG_TAG, "Cleared database for debug")
        }
    }

    /**
     * Deletes all data off of the tables by dropping them and re-creating.
     */
    fun resetDatabase() {
        val db = writableDatabase
        db.execSQL(DROP_TABLE + Game.TABLE_NAME)
        db.execSQL(DROP_TABLE + GameWord.TABLE_NAME)
        db.execSQL(Game.CREATE_TABLE)
        db.execSQL(GameWord.CREATE_TABLE)
    }

    /**
     * Builds the necessary rows to start a new game within the database.
     * @return The game ID.
     */
    fun startGame(difficulty: Difficulty, boardState: String, movesEarned: Int,
                  scramblesEarned: Int): Long {
        val values = ContentValues()

        // We store phone time here, be sure to sanitize onWrapEventListener server
        values.put(Game.COLUMN_UNIX_START, System.currentTimeMillis() / 1000)
        values.put(Game.COLUMN_STATUS, Game.STATUS_IN_PROGRESS)
        values.put(Game.COLUMN_DIFFICULTY, difficulty.toString())
        values.put(Game.COLUMN_BOARD_STATE, boardState)
        values.put(Game.COLUMN_MOVES_EARNED, movesEarned)
        values.put(Game.COLUMN_SCRAMBLES_EARNED, scramblesEarned)
        values.put(Game.COLUMN_SCRAMBLES_USED, 0)
        values.put(Game.COLUMN_LEVEL, 1)

        return writableDatabase.insert(Game.TABLE_NAME, null, values)
    }

    /**
     * End a game.
     * @param gameId The game to end.
     */
    fun endGame(gameId: Long) {
        val values = ContentValues()
        values.put(Game.COLUMN_STATUS, Game.STATUS_COMPLETED)
        Game.updateById(writableDatabase, values, gameId)
    }

    /**
     * Performs an update on the given game.
     */
    fun updateGame(gameId: Long, update: Consumer<ContentValues>) {
        val gameValues = ContentValues()
        update.accept(gameValues)
        Game.updateById(writableDatabase, gameValues, gameId)
    }

    /**
     * Adds a move to a game.
     * @param gameId The game to add the move to.
     * @param word The word that was played.
     * @param pointValue The point value of the given word.
     * @param score The total game score.
     * @param newBoardState The new board state of the game.
     * @return Whether or not the database was updated.
     */
    fun addGameMove(gameId: Long, word: String, pointValue: Int, score: Int,
                    newBoardState: String): Boolean {
        // Insert into GameWord
        val gameWordValues = ContentValues()
        gameWordValues.put(GameWord.COLUMN_GAME_ID, gameId)
        gameWordValues.put(GameWord.COLUMN_POINT_VALUE, pointValue)
        gameWordValues.put(GameWord.COLUMN_WORD, word)
        if (writableDatabase.insert(GameWord.TABLE_NAME, null, gameWordValues) == -1) {
            return false
        }

        // Update board state in Game
        val gameValues = ContentValues()
        gameValues.put(Game.COLUMN_BOARD_STATE, newBoardState)
        Game.updateById(writableDatabase, gameValues, gameId)

        return true
    }

    /**
     * Retrieves all moves performed, in order, within the game.
     * @param gameId The game to fetch.
     * @return A list of moves, in order, performed in the game.
     */
    fun getGameMoves(gameId: Long): ArrayList<String> {
        val result = ArrayList<String>()

        val c = readableDatabase.query(
                GameWord.TABLE_NAME,
                arrayOf(GameWord.COLUMN_WORD),
                GameWord.COLUMN_GAME_ID + " = ?",
                arrayOf(gameId.toString() + ""), null, null,
                GameWord._ID + " ASC")

        if (c.moveToFirst()) {
            do {
                result.add(c.getString(0))
            } while (c.moveToNext())
        }
        c.close()
        return result
    }

    /**
     * Retrieves a single row from a table.
     * @param tableName The table to SELECT from. This table definition must extend BaseColumns
     * @param id The id of the row to SELECT.
     * @param columns The columns to fetch.
     * @return A map keyed by the column name or null if the row wasn't found.
     */
    fun getRowFromId(tableName: String, id: Long, columns: Array<String>): Row? {
        val c = readableDatabase.query(
                tableName,
                columns,
                BaseColumns._ID + " = ?",
                arrayOf(id.toString() + ""), null, null, null)
        if (!c.moveToFirst()) {
            return null
        }

        val result = Row()
        for (column in columns) {
            result.put(column, c.getString(c.getColumnIndex(column)))
        }

        c.close()
        return result
    }

    /**
     * Performs a query executing an action for every result. Returns false if the query returned
     * no results.
     * @param query The raw SQL query.
     * @param args Any selection arguments within the raw query.
     * @param action What to do with the row.
     * @return False if the query returned no results; otherwise, true.
     */
    fun forEachResult(query: String, args: Array<String>?, action: Consumer<Cursor>): Boolean {
        var c: Cursor? = null
        try {
            c = readableDatabase.rawQuery(query, args)
            if (!c!!.moveToFirst()) {
                return false
            }
            do {
                action.accept(c)
            } while (c.moveToNext())
            return true
        } finally {
            if (c != null) {
                c.close()
            }
        }
    }

    companion object {

        private val DATABASE_NAME = "word_dropper.db"
        private val DATABASE_VERSION = 1

        private val DROP_TABLE = "DROP TABLE IF EXISTS "
    }
}
