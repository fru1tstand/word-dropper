package me.fru1t.worddropper.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import me.fru1t.android.database.Row
import me.fru1t.android.slick.annotations.Named
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication.Companion.DELETE_DATABASE_ON_DEBUG
import me.fru1t.worddropper.WordDropperApplication.Companion.IS_DEBUGGING
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.database.tables.GameWord
import me.fru1t.worddropper.settings.Difficulty

/** The standard database utils implementation backed by Android's SQLite database. */
class DatabaseUtilsImpl(
        private @Named(IS_DEBUGGING) val isDebugging: Boolean,
        private @Named(DELETE_DATABASE_ON_DEBUG) val deleteOnDebug: Boolean,
        private val context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), DatabaseUtils {

    companion object {
        private val LOG_TAG = "DatabaseUtils"

        private val CONVERT_FORMATS = intArrayOf(
                R.string.bytes,
                R.string.kilobytes,
                R.string.megabytes,
                R.string.gigabytes
        )

        private val DATABASE_NAME = "word_dropper.db"
        private val DATABASE_VERSION = 1

        private val DROP_TABLE = "DROP TABLE IF EXISTS "
    }

    /* SQLiteOpenHelper */
    override fun onCreate(db: SQLiteDatabase?) {
        createTables(db)
        Log.i(LOG_TAG, "Initial database creation successful")
    }

    override fun onOpen(db: SQLiteDatabase?) {
        if (isDebugging && deleteOnDebug) {
            createTables(db)
            Log.d(LOG_TAG, "Deleted database for debugging")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) =
            Unit // Do nothing

    /* DatabaseUtils */
    override fun getDatabaseSize(): String {
        val f = context.getDatabasePath(DATABASE_NAME)
        var size = f.length().toDouble()
        CONVERT_FORMATS.forEach {
            if (size < 1024) {
                return context.getString(it, size)
            }
            size /= 1024
        }
        return context.getString(CONVERT_FORMATS[CONVERT_FORMATS.size - 1], size)
    }

    override fun startGame(
            difficulty: Difficulty, boardState: String, movesEarned: Int, scramblesEarned: Int)
            : Long {
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

    override fun endGame(gameId: Long) {
        val values = ContentValues()
        values.put(Game.COLUMN_STATUS, Game.STATUS_COMPLETED)
        Game.updateById(writableDatabase, values, gameId)
    }

    override fun updateGame(gameId: Long, update: (ContentValues) -> Unit) {
        val values = ContentValues()
        update(values)
        Game.updateById(writableDatabase, values, gameId)
    }

    override fun addGameMove(
            gameId: Long, word: String, pointValue: Int, score: Int, newBoardState: String)
            : Boolean {
        // Insert into GameWord
        val gameWordValues = ContentValues()
        gameWordValues.put(GameWord.COLUMN_GAME_ID, gameId)
        gameWordValues.put(GameWord.COLUMN_POINT_VALUE, pointValue)
        gameWordValues.put(GameWord.COLUMN_WORD, word)
        if (writableDatabase.insert(GameWord.TABLE_NAME, null, gameWordValues) == -1L) {
            return false
        }

        // Update board state in Game
        val gameValues = ContentValues()
        gameValues.put(Game.COLUMN_BOARD_STATE, newBoardState)
        Game.updateById(writableDatabase, gameValues, gameId)

        return true
    }

    override fun getGameMoves(gameId: Long): ArrayList<String> {
        val result = ArrayList<String>()

        val c = readableDatabase.query(
                GameWord.TABLE_NAME,
                arrayOf(GameWord.COLUMN_WORD),
                GameWord.COLUMN_GAME_ID + " = ?",
                arrayOf(gameId.toString() + ""),
                null, null,
                GameWord._ID + " ASC")

        if (c.moveToFirst()) {
            do {
                result.add(c.getString(0))
            } while (c.moveToNext())
        }
        c.close()
        return result
    }

    override fun getRowFromId(tableName: String, id: Long, columns: Array<String>): Row? {
        val c = readableDatabase.query(
                tableName,
                columns,
                BaseColumns._ID + " = ?",
                arrayOf(id.toString() + ""),
                null, null, null)
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

    override fun forEachResult(
            query: String, args: Array<String>?, action: (Cursor) -> Unit): Boolean =
            readableDatabase.rawQuery(query, args).use { cursor ->
                if (!cursor.moveToFirst()) {
                    return false
                }
                do {
                    action(cursor)
                } while (cursor.moveToNext())
                return true
            }

    /** Re-creates all tables in the database, deleting any information in the process. */
    private fun createTables(existingDb: SQLiteDatabase?) {
        val db = existingDb ?: writableDatabase!!
        db.execSQL(DROP_TABLE + Game.TABLE_NAME)
        db.execSQL(DROP_TABLE + GameWord.TABLE_NAME)
        db.execSQL(Game.CREATE_TABLE)
        db.execSQL(GameWord.CREATE_TABLE)
    }
}
