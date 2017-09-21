package me.fru1t.worddropper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.widget.GameListView;

/**
 * Shows the user their list of games played in order to navigate to that game's more detailed
 * stats.
 */
public class StatsGameSelectScreen extends AppCompatActivity {
    private static final SimpleDateFormat TITLE_DATE_FORMAT =
            new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_game_select_screen);

        // Set up how things look
        GameListView gameList = (GameListView) findViewById(R.id.gameList);
        gameList.setTitleFunction(data -> data.difficulty.toUpperCase() + " - "
                + TITLE_DATE_FORMAT.format(new Date(data.unixStart * 1000)));
        gameList.setDescriptionFunction(data -> data.words + " words - " + data.score + " points"
                + ((data.gameStatus == Game.STATUS_IN_PROGRESS) ? " - in progress" : ""));

        // Our action
        gameList.setOnItemClickListener((parent, view, position, id) -> {
            Intent endGameIntent = new Intent(this, EndGameScreen.class);
            endGameIntent.putExtra(EndGameScreen.EXTRA_GAME_ID,
                    ((GameListView.GameData) parent.getItemAtPosition(position)).gameId);
            startActivity(endGameIntent);
            finish();
        });

        // Populate list view
        if (!gameList.populate()) {
            findViewById(R.id.noDataWarning).setVisibility(View.VISIBLE);
        }
    }
}
