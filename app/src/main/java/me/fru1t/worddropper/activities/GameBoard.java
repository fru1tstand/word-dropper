package me.fru1t.worddropper.activities;

import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import me.fru1t.worddropper.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameBoard extends AppCompatActivity {
    private static final int TILE_COLUMNS = 7;
    private static final int TILE_MAX_ROWS = 8;

    private Point screenSize = new Point();

    private int tileSize;

    private ProgressBar userLevelProgress;
    private FrameLayout tileWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        // Get some information
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        tileSize = screenSize.x / TILE_COLUMNS;

        // Get references to our elements
        userLevelProgress = (ProgressBar) findViewById(R.id.gameBoardUserLevel);
        tileWrapper = (FrameLayout) findViewById(R.id.gameBoardTileWrapper);

        repackFrame();
    }

    /**
     * Generates, positions, and updates the game board to the start state.
     */
    private void repackFrame() {
        // User level
        userLevelProgress.setY(screenSize.y
                - getResources().getDimension(R.dimen.gameBoardUserLevelProgressHeight));

        // Tile board
        //  Set up wrapper
        ViewGroup.LayoutParams wrapperLayout = tileWrapper.getLayoutParams();
        wrapperLayout.height = tileSize * TILE_MAX_ROWS;
        wrapperLayout.width = tileSize * TILE_COLUMNS;
        tileWrapper.setY(screenSize.y
                - getResources().getDimension(R.dimen.gameBoardUserLevelProgressHeight)
                - wrapperLayout.height);
        tileWrapper.setX((screenSize.x - wrapperLayout.width) / 2);
        tileWrapper.setBackgroundColor(Color.RED);
    }
}
