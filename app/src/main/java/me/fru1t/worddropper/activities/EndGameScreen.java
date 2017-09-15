package me.fru1t.worddropper.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.android.database.Row;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.database.tables.GameWord;
import me.fru1t.worddropper.settings.Difficulty;

public class EndGameScreen extends AppCompatActivity {
    public static final String EXTRA_GAME_ID = "extra_game_id"; // Long

    private static final String STAT_FORMAT_STRING = "%s";

    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game_screen);
        WordDropperApplication app = (WordDropperApplication) getApplicationContext();

        LinearLayout root = (LinearLayout) findViewById(R.id.endGameScreenRoot);

        // Fetch data
        long gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);
        Row gameData = app.getDatabaseUtils().getRowFromId(
                Game.TABLE_NAME,
                gameId,
                new String[] { Game.COLUMN_UNIX_START, Game.COLUMN_STATUS, Game.COLUMN_DIFFICULTY,
                        Game.COLUMN_BOARD_STATE, Game.COLUMN_MOVES_EARNED, Game.COLUMN_BOARD_STATE,
                        Game.COLUMN_MOVES_EARNED, Game.COLUMN_SCRAMBLES_USED,
                        Game.COLUMN_SCRAMBLES_EARNED, Game.COLUMN_LEVEL, Game.COLUMN_SCORE });
        if (gameData == null) {
            Toast.makeText(this, R.string.endGameScreen_gameNotFoundError, Toast.LENGTH_LONG)
                    .show();
            startActivity(new Intent(this, MainMenuScreen.class));
            return;
        }
        int gameDataWords = app.getDatabaseUtils().getRowCount(
                GameWord.TABLE_NAME,
                GameWord.COLUMN_GAME_ID + " = ?",
                new String[] { gameId + "" });

        // Populate data
        // TODO: Show difficulty somewhere.
        difficulty = gameData.getString(Game.COLUMN_DIFFICULTY, Difficulty.ZEN.name());
        TextView score = (TextView) root.findViewById(R.id.endGameScreenScore);
        TextView level = (TextView) root.findViewById(R.id.endGameScreenLevel);
        TextView scramblesUsed = (TextView) root.findViewById(R.id.endGameScreenScramblesUsed);
        TextView scramblesEarned = (TextView) root.findViewById(R.id.endGameScreenScramblesEarned);
        TextView words = (TextView) root.findViewById(R.id.endGameScreenWords);
        TextView wordsList = (TextView) root.findViewById(R.id.endGameScreenWordsList);

        animateValue(gameData.getInt(Game.COLUMN_LEVEL, 0), level, 0);
        animateValue(gameData.getInt(Game.COLUMN_SCORE, 0), score, 50);
        animateValue(gameData.getInt(Game.COLUMN_SCRAMBLES_EARNED, 0), scramblesEarned, 50);
        animateValue(gameData.getInt(Game.COLUMN_SCRAMBLES_USED, 0), scramblesUsed, 100);
        animateValue(gameDataWords, words, 150);

        // Populate words
        ArrayList<String> gameMovesList =
                app.getDatabaseUtils().getGameMoves(getIntent().getLongExtra(EXTRA_GAME_ID, -1));
        gameMovesList.forEach(s -> wordsList.append(s + ", "));
    }

    private void animateValue(int value, TextView target, int delay) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(getResources().getInteger(R.integer.animation_durationLag));
        animator.addUpdateListener(animation -> target.setText(
                String.format(Locale.ENGLISH, STAT_FORMAT_STRING, animation.getAnimatedValue())));

        if (delay > 0) {
            (new Handler()).postDelayed(animator::start, delay);
        } else {
            animator.start();
        }
    }

    @VisibleForXML
    public void onActionPlayAgainClick(View v) {
        Intent gameScreenIntent = new Intent(this, GameScreen.class);
        gameScreenIntent.putExtra(GameScreen.EXTRA_DIFFICULTY, difficulty);
        startActivity(gameScreenIntent);
    }

    @VisibleForXML
    public void onActionMainMenuClick(View v) {
        startActivity(new Intent(this, MainMenuScreen.class));
    }
}
