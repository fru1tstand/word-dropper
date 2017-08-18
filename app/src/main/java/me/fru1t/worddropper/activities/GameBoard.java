package me.fru1t.worddropper.activities;

import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import me.fru1t.android.widget.ProgressBar;
import me.fru1t.worddropper.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameBoard extends AppCompatActivity {
    private static final int TILE_COLUMNS = 7;
    private static final int TILE_MAX_ROWS = 8;

    private ArrayList<LinkedList<TextView>> tileColumns;

    private FrameLayout root;
    private Point screenSize = new Point();
    private int tileSize;
    private ProgressBar userLevelProgress;
    private FrameLayout tileWrapper;
    private Point tileWrapperBottomRight = new Point();

    public GameBoard() {
        tileColumns = new ArrayList<>();
        for (int i = 0; i < TILE_COLUMNS; i++) {
            tileColumns.add(new LinkedList<TextView>());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        root = (FrameLayout) findViewById(R.id.gameBoardRoot);

        // Get some information
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        tileSize = screenSize.x / TILE_COLUMNS;
        //  Force tile size to be an even value
        if (tileSize % 2 != 0) {
            tileSize--;
        }
        System.out.println("Tile size: " + tileSize);

        // Get references to our elements
        tileWrapper = (FrameLayout) findViewById(R.id.gameBoardTileWrapper);

        // Create progress bar
        userLevelProgress = new ProgressBar(this);
        root.addView(userLevelProgress);

        repackFrame();
    }

    /**
     * Generates, positions, and updates the game board to the start state.
     */
    private void repackFrame() {
        // User level
        userLevelProgress.setX(0);
        userLevelProgress.setY(screenSize.y
                - getResources().getDimension(R.dimen.gameBoardUserLevelProgressHeight));
        userLevelProgress.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        userLevelProgress.getLayoutParams().height =
                (int) getResources().getDimension(R.dimen.gameBoardUserLevelProgressHeight);

        // Tile board
        //  Set up wrapper
        ViewGroup.LayoutParams wrapperLayout = tileWrapper.getLayoutParams();
        wrapperLayout.height = tileSize * TILE_MAX_ROWS;
        wrapperLayout.width = tileSize * TILE_COLUMNS;
        tileWrapper.setY(screenSize.y
                - getResources().getDimension(R.dimen.gameBoardUserLevelProgressHeight)
                - wrapperLayout.height);
        tileWrapper.setX((screenSize.x - wrapperLayout.width) / 2);
        tileWrapperBottomRight.set(
                (int) (tileWrapper.getX() + wrapperLayout.width),
                (int) (tileWrapper.getY() + wrapperLayout.height));

        tileWrapper.setBackgroundColor(Color.RED); // TODO(debug): Remove after debug

        //  Add tiles
        for (int column = 0; column < TILE_COLUMNS; column++) {
            int rows = TILE_MAX_ROWS;
            int tileYOffset = 0;
            if (column % 2 == 0) {
                rows -= 1;
                tileYOffset = tileSize / 2;
            }
            for (int row = 0; row < rows; row++) {
                TextView textView = new TextView(this);
                textView.setText("A");
                textView.setX(column * tileSize);
                textView.setY(row * tileSize + tileYOffset);
                textView.setBackgroundColor(Color.CYAN);
                textView.setGravity(Gravity.CENTER);
                tileWrapper.addView(textView);
                textView.getLayoutParams().height = tileSize;
                textView.getLayoutParams().width = tileSize;

                tileColumns.get(column).add(textView);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Verify touch event is within the tile wrapper
        if (event.getX() < tileWrapper.getX() // left of
                || event.getY() < tileWrapper.getY() // above
                || event.getX() > tileWrapperBottomRight.x // right of
                || event.getY() > tileWrapperBottomRight.y) { // below
            return false;
        }

        // No multitouch
        if (event.getActionIndex() > 0) {
            return false;
        }

        //
        return false;
    }
}
