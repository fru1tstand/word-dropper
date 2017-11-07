package me.fru1t.worddropper.ui.statsgameselect

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import me.fru1t.worddropper.R
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.ui.gamestats.GameStatsActivity
import me.fru1t.worddropper.ui.widget.GameData
import me.fru1t.worddropper.ui.widget.GameListView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shows the user their list of games played in order to navigate to that game's more detailed
 * stats.
 */
class StatsGameSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_game_select)

        // Set up how things look
        val gameList = findViewById(R.id.gameList) as GameListView
        gameList.titleFunction = {
            (_, difficulty, unixStart) ->
                difficulty.toUpperCase() + " - " + TITLE_DATE_FORMAT.format(Date(unixStart * 1000))
        }
        gameList.descriptionFunction = {
            (_, _, _, gameStatus, score, words) ->
                (words.toString() + " words - " + score + " points"
                        + if (gameStatus == Game.STATUS_IN_PROGRESS) " - in progress" else "")
        }

        // Our action
        gameList.setOnItemClickListener {
            parent, _, position, _ ->
                val endGameIntent = Intent(this, GameStatsActivity::class.java)
                endGameIntent.putExtra(
                        GameStatsActivity.EXTRA_GAME_ID,
                        (parent.getItemAtPosition(position) as GameData).gameId)
                startActivity(endGameIntent)
                finish()
        }

        // Populate list view
        if (!gameList.populate()) {
            findViewById(R.id.noDataWarning).visibility = View.VISIBLE
        }
    }

    companion object {
        private val TITLE_DATE_FORMAT = SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US)
    }
}
