package me.fru1t.worddropper.activities;

import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.fru1t.worddropper.activities.gameboard.StatsView;
import me.fru1t.worddropper.widget.ProgressBar;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.widget.TileBoard;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameScreen extends AppCompatActivity {
    private static final int STATS_HEIGHT = 650;
    private static final int PROGRESS_HEIGHT = 20;

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
        tileBoard.setBackgroundColor(Color.RED);
        tileBoard.forEachTile(tile -> {
            tile.setBackgroundColor(Color.MAGENTA);
            tile.getTextPaint().setColor(Color.LTGRAY);
            tile.getTextPaint().setTextSize(52);
        });

        // Create progress bar
        ProgressBar progressBar = new ProgressBar(this);
        root.addView(progressBar);
        progressBar.setX(0);
        progressBar.setY(screenSize.y - PROGRESS_HEIGHT);
        progressBar.getLayoutParams().height = PROGRESS_HEIGHT;
        progressBar.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        progressBar.getBackgroundColor().setColor(Color.WHITE);
        progressBar.getProgressColor().setColor(Color.BLUE);
        progressBar.getTextColor().setColor(Color.BLACK);

        // Creates stats
        View statsView = getLayoutInflater().inflate(R.layout.view_game_board_stats, root, false);
        StatsView stats = new StatsView(statsView);
        root.addView(statsView);
        statsView.setX(0);
        statsView.setY(0);
        statsView.setBackgroundColor(Color.YELLOW);
        statsView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

        tileBoard.setEventHandler((changeEventType, string) -> {
            switch (changeEventType) {
                case CHANGE:
                    stats.setCurrentWord(string);
                    break;
                case SUCCESSFUL_SUBMIT:
                    stats.setCurrentWord(null);
                    break;
                case FAILED_SUBMIT:
                    break;
            }
        });
    }
}
