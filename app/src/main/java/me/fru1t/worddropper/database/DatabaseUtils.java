package me.fru1t.worddropper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.function.Consumer;

import me.fru1t.android.database.Row;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.database.tables.GameWord;
import me.fru1t.worddropper.settings.Difficulty;

import static me.fru1t.worddropper.WordDropperApplication.LOG_TAG;

/**
 * Methods to manipulate and retrieve data from the database.
 */
public class DatabaseUtils extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "word_dropper.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private final WordDropperApplication app;

    public DatabaseUtils(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        app = (WordDropperApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Game.CREATE_TABLE);
        db.execSQL(GameWord.CREATE_TABLE);
        Log.i(LOG_TAG, "Initial database creation successful");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (app.isDebugging()) {
            db.execSQL(DROP_TABLE + Game.TABLE_NAME);
            db.execSQL(DROP_TABLE + GameWord.TABLE_NAME);
            Log.d(LOG_TAG, "Cleared database for debug");

            db.execSQL(Game.CREATE_TABLE);
            db.execSQL(GameWord.CREATE_TABLE);
        }
    }

    /**
     * Builds the necessary rows to start a new game within the database.
     * @return The game ID.
     */
    public long startGame(Difficulty difficulty, String boardState, int movesEarned,
                          int scramblesEarned) {
        ContentValues values = new ContentValues();

        // We store phone time here, be sure to sanitize onWrapEventListener server
        values.put(Game.COLUMN_UNIX_START, System.currentTimeMillis() / 1000);
        values.put(Game.COLUMN_STATUS, Game.STATUS_IN_PROGRESS);
        values.put(Game.COLUMN_DIFFICULTY, difficulty.toString());
        values.put(Game.COLUMN_BOARD_STATE, boardState);
        values.put(Game.COLUMN_MOVES_EARNED, movesEarned);
        values.put(Game.COLUMN_SCRAMBLES_EARNED, scramblesEarned);
        values.put(Game.COLUMN_SCRAMBLES_USED, 0);
        values.put(Game.COLUMN_LEVEL, 1);

        return getWritableDatabase().insert(Game.TABLE_NAME, null, values);
    }

    /**
     * End a game.
     * @param gameId The game to end.
     */
    public void endGame(long gameId) {
        ContentValues values = new ContentValues();
        values.put(Game.COLUMN_STATUS, Game.STATUS_COMPLETED);
        Game.updateById(getWritableDatabase(), values, gameId);
    }

    /**
     * Performs an update on the given game.
     */
    public void updateGame(long gameId, Consumer<ContentValues> update) {
        ContentValues gameValues = new ContentValues();
        update.accept(gameValues);
        Game.updateById(getWritableDatabase(), gameValues, gameId);
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
    public boolean addGameMove(long gameId, String word, int pointValue, int score,
                               String newBoardState) {
        // Insert into GameWord
        ContentValues gameWordValues = new ContentValues();
        gameWordValues.put(GameWord.COLUMN_GAME_ID, gameId);
        gameWordValues.put(GameWord.COLUMN_POINT_VALUE, pointValue);
        gameWordValues.put(GameWord.COLUMN_WORD, word);
        if (getWritableDatabase().insert(GameWord.TABLE_NAME, null, gameWordValues) == -1) {
            return false;
        }

        // Update board state in Game
        ContentValues gameValues = new ContentValues();
        gameValues.put(Game.COLUMN_BOARD_STATE, newBoardState);
        Game.updateById(getWritableDatabase(), gameValues, gameId);

        return true;
    }

    /**
     * Retrieves all moves performed, in order, within the game.
     * @param gameId The game to fetch.
     * @return A list of moves, in order, performed in the game.
     */
    public ArrayList<String> getGameMoves(long gameId) {
        ArrayList<String> result = new ArrayList<>();

        Cursor c = getReadableDatabase().query(
                GameWord.TABLE_NAME,
                new String[] { GameWord.COLUMN_WORD },
                GameWord.COLUMN_GAME_ID + " = ?",
                new String[] { gameId + "" },
                null,
                null,
                GameWord._ID + " ASC");

        if (c.moveToFirst()) {
            do {
                result.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    /**
     * Retrieves a single row from a table.
     * @param tableName The table to SELECT from. This table definition must extend BaseColumns
     * @param id The id of the row to SELECT.
     * @param columns The columns to fetch.
     * @return A map keyed by the column name or null if the row wasn't found.
     */
    @Nullable
    public Row getRowFromId(String tableName, long id, String[] columns) {
        Cursor c = getReadableDatabase().query(
                tableName,
                columns,
                BaseColumns._ID + " = ?",
                new String[] { id + "" },
                null,
                null,
                null);
        if (!c.moveToFirst()) {
            return null;
        }

        Row result = new Row();
        for (String column : columns) {
            result.put(column, c.getString(c.getColumnIndex(column)));
        }

        c.close();
        return result;
    }

    /**
     * Performs a query executing an action for every result. Returns false if the query returned
     * no results.
     * @param query The raw SQL query.
     * @param args Any selection arguments within the raw query.
     * @param action What to do with the row.
     * @return False if the query returned no results; otherwise, true.
     */
    public boolean forEachResult(String query, @Nullable String[] args, Consumer<Cursor> action) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(query, args);
            if (!c.moveToFirst()) {
                return false;
            }
            do {
                action.accept(c);
            } while (c.moveToNext());
            return true;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
