package me.fru1t.worddropper.widget;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * The main game board where players create words from.
 */
public class TileBoard extends FrameLayout {
    private static final int TILE_COLUMNS = 7;
    private static final int TILE_MAX_ROWS = 8;

    // Front of linkedlist is top element
    private final ArrayList<LinkedList<Tile>> tileColumns;

    /**
     * The "effective board" is the location of the tiles as the tiles don't stretch to fit the view
     */
    private transient final Point effectiveBoardSize = new Point();
    private transient final Point effectiveBoardOffset = new Point();

    private transient int tileSize;
    private transient int tileOffrowOffset;

    private transient Tile lastTouchedTile;

    public TileBoard(Context context) {
        super(context);

        // Tiles
        tileColumns = new ArrayList<>();
        for (int col = 0; col < TILE_COLUMNS; col++) {
            tileColumns.add(new LinkedList<Tile>());

            int rows = (col % 2 == 0) ? TILE_MAX_ROWS - 1 : TILE_MAX_ROWS;
            for (int row = 0; row < rows; row++) {
                Tile t = new Tile(context);
                t.setText("A");
                tileColumns.get(col).add(t);
                addView(t);
                t.getTextPaint().setTextSize(20);
            }
        }
    }

    public void setTileBackgroundColor(int color) {
        for (LinkedList<Tile> tileColumn : tileColumns) {
            for (Tile tile : tileColumn) {
                tile.setBackgroundColor(color);
            }
        }
    }

    public void setTileTextColor(int color) {
        for (LinkedList<Tile> tileColumn : tileColumns) {
            for (Tile tile : tileColumn) {
                tile.getTextPaint().setColor(color);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) {
            return;
        }

        // Use the smallest edge for calculating
        int width = right - left;
        int height = bottom - top;

        if (width / TILE_COLUMNS > height / TILE_MAX_ROWS) {
            // Here, we're constrained by height, so we use the max rows as the divider.
            tileSize = height / TILE_MAX_ROWS;
        } else {
            // Here, we're constrained by width, so we use columns as the divider.
            tileSize = width / TILE_COLUMNS;
        }

        // We want an event tile size
        if (tileSize % 2 != 0) {
            tileSize--;
        }

        // So that the offrow offset is an even number
        tileOffrowOffset = tileSize / 2;

        // Set effective sizes
        effectiveBoardSize.set(tileSize * TILE_COLUMNS, tileSize * TILE_MAX_ROWS);
        effectiveBoardOffset.set( // This is where one can set the positioning of the board
                (width - effectiveBoardSize.x) / 2, // Middle
                (height - effectiveBoardSize.y) / 2); // Middle

        // Reposition tiles
        updateTilePosition();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionIndex() == 0 &&
                (event.getActionMasked() == MotionEvent.ACTION_DOWN
                        || event.getActionMasked() == MotionEvent.ACTION_MOVE
                        || event.getActionMasked() == MotionEvent.ACTION_UP)) {
            int col = (int) ((event.getX() - effectiveBoardOffset.x) / tileSize);
            int row = (int) ((col % 2 == 0)
                    ? (event.getY() - effectiveBoardOffset.y - tileOffrowOffset) / tileSize
                    : (event.getY() - effectiveBoardOffset.y) / tileSize);

            // Check if we're releasing
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                lastTouchedTile.onRelease();
                return true;
            }

            // Check if it's an invalid
            if ((col % 2 == 0 && row >= TILE_MAX_ROWS - 1)
                    || row >= TILE_MAX_ROWS
                    || col >= TILE_COLUMNS
                    || row < 0
                    || col < 0) {
                return true;
            }

            Tile thisTile = tileColumns.get(col).get(row);

            // Check if it's the last touched tile.
            if (thisTile == lastTouchedTile) {
                return true;
            }

            // Check if we're leaving an old tile
            if (lastTouchedTile != null) {
                lastTouchedTile.onRelease();
            }

            lastTouchedTile = thisTile;
            thisTile.onPress();

            return true;
        }

        return false;
    }

    private void updateTilePosition() {
        for (int col = 0; col < TILE_COLUMNS; col++) {
            int rows = TILE_MAX_ROWS;
            int rowYOffset = 0;
            if (col % 2 == 0) {
                rowYOffset = tileOffrowOffset;
                rows -= 1;
            }

            for (int row = 0; row < rows; row++) {
                Tile tile = tileColumns.get(col).get(row);

                tile.setX(col * tileSize + effectiveBoardOffset.x);
                tile.setY(row * tileSize + rowYOffset + effectiveBoardOffset.y);
                tile.getLayoutParams().height = tileSize;
                tile.getLayoutParams().width = tileSize;
                tile.setSize(tileSize);
                tile.setText(col + ", " + row);
            }
        }
    }
}
