package me.fru1t.worddropper.activities;

import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import me.fru1t.worddropper.WordDropper;
import me.fru1t.worddropper.activities.gameboard.StatsView;
import me.fru1t.worddropper.widget.WrappingProgressBar;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.widget.TileBoard;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameScreen extends AppCompatActivity {
    private static final int STATS_HEIGHT = 650;
    private static final int PROGRESS_HEIGHT = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        FrameLayout root = (FrameLayout) findViewById(R.id.gameBoardRoot);

        // Create tile board
        TileBoard tileBoard = new TileBoard(this);
        root.addView(tileBoard);
        tileBoard.setX(0);
        tileBoard.setY(STATS_HEIGHT);
        tileBoard.getLayoutParams().height = screenSize.y - STATS_HEIGHT - PROGRESS_HEIGHT;
        tileBoard.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        tileBoard.setBackgroundColor(WordDropper.COLOR_BACKGROUND);
        tileBoard.forEachTile(tile -> {
            tile.setDefaultBackgroundColor(WordDropper.COLOR_BACKGROUND);
            tile.setActiveBackgroundColor(WordDropper.COLOR_PRIMARY);
            tile.getTextPaint().setColor(WordDropper.COLOR_TEXT);
            tile.getTextPaint().setTextSize(52);
        });

        // Create progress bar
        WrappingProgressBar progressBar = new WrappingProgressBar(this);
        root.addView(progressBar);
        progressBar.setX(0);
        progressBar.setY(screenSize.y - PROGRESS_HEIGHT);
        progressBar.getLayoutParams().height = PROGRESS_HEIGHT;
        progressBar.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        progressBar.getBackgroundColor().setColor(WordDropper.COLOR_BACKGROUND);
        progressBar.getProgressColor().setColor(WordDropper.COLOR_PRIMARY_LIGHT);
        progressBar.getProgressCalculatedColor().setColor(WordDropper.COLOR_PRIMARY_DARK);
        progressBar.getTextPaint().setColor(WordDropper.COLOR_TEXT);
        progressBar.getTextPaint().setTextSize(16);
        progressBar.getTextPaint().setTypeface(Typeface.DEFAULT);
        progressBar.setNextMaximumFunction(wraps -> {
            if (wraps < 1) {
                return 80;
            }
            return (long) (80 * Math.pow(1.10409, wraps));
        });

        // Creates stats
        View statsView = getLayoutInflater().inflate(R.layout.view_game_board_stats, root, false);
        StatsView stats = new StatsView(statsView);
        root.addView(statsView);
        statsView.setX(0);
        statsView.setY(0);
        statsView.setBackgroundColor(WordDropper.COLOR_BACKGROUND);
        statsView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        stats.setDefaultTextColor(WordDropper.COLOR_TEXT);
        stats.setActiveTextColor(WordDropper.COLOR_PRIMARY);

        tileBoard.setEventHandler((changeEventType, string) -> {
            switch (changeEventType) {
                case CHANGE:
                    stats.setCurrentWord(string);
                    break;
                case SUCCESSFUL_SUBMIT:
                    progressBar.animateAddProgress(WordDropper.getWordValue(string));
                    stats.setCurrentWord(null);
                    break;
                case FAILED_SUBMIT:
                    break;
            }
        });
    }
}
