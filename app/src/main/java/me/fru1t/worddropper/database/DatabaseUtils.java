package me.fru1t.worddropper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.database.tables.GameWord;
import me.fru1t.worddropper.settings.Difficulty;

/**
 * Methods to manipulate and retrieve data from the database.
 */
public class DatabaseUtils extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "word_dropper.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseUtils(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Game.CREATE_TABLE);
        db.execSQL(GameWord.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    /**
     * Builds the necessary rows to start a new game within the database.
     * @return The game ID.
     */
    public long startGame(Difficulty difficulty, String boardState) {
        ContentValues values = new ContentValues();

        // We store phone time here, be sure to sanitize on server
        values.put(Game.COLUMN_UNIX_START, System.currentTimeMillis() / 1000);
        values.put(Game.COLUMN_STATUS, Game.STATUS_IN_PROGRESS);
        values.put(Game.COLUMN_DIFFICULTY, difficulty.toString());
        values.put(Game.COLUMN_BOARD_STATE, boardState);

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
     * Adds a move to a game.
     * @param gameId The game to add the move to.
     * @param word The word that was played.
     * @param pointValue The point value of the given word.
     * @param newBoardState The new board state of the game.
     * @return Whether or not the database was updated.
     */
    public boolean addGameMove(long gameId, String word, int pointValue, String newBoardState) {
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
}
