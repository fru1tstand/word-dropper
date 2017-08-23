package me.fru1t.worddropper.widget;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import me.fru1t.worddropper.tileboard.TileBoardColumn;

/**
 * The main game board where players create words from.
 */
public class TileBoard extends FrameLayout {
    /**
     * The types of change events that can occur.
     */
    public enum ChangeEventType {
        SUCCESSFUL_SUBMIT, FAILED_SUBMIT, CHANGE
    }

    /**
     * Events that the tileboard can create.
     */
    public interface TileBoardEvents {
        /**
         * Triggered when the user performs an action on the board (eg. making a word, submitting
         * a word, etc).
         * @param changeEventType The type of change that occurred.
         * @param string The current word on the board.
         */
        void onChange(ChangeEventType changeEventType, @Nullable String string);
    }

    // Measurements
    private static final int TILE_COLUMNS = 7;
    private static final int TILE_MAX_ROWS = 8;

    private static final int LETTERS_IN_ALPHABET = 26;
    private static final int ALPHABET_START_OFFSET = (int) 'A';

    // Front of linked list is top element
    private TileBoardEvents eventHandler;
    private final ArrayList<TileBoardColumn> tileColumns;

    // Board sizing
    private final Point effectiveBoardSize = new Point();
    private final Point effectiveBoardOffset = new Point();
    private int tileSize;
    private int tileOffrowOffset;

    // Touch pathing
    private final LinkedList<Tile> currentPath;
    private int touchDownRow;
    private int touchDownCol;

    public TileBoard(Context context) {
        super(context);

        eventHandler = null;

        // Sizing
        tileSize = 0;
        tileOffrowOffset = 0;

        // Tiles
        tileColumns = new ArrayList<>();
        for (int col = 0; col < TILE_COLUMNS; col++) {
            tileColumns.add(new TileBoardColumn());

            int rows = (col % 2 == 0) ? TILE_MAX_ROWS - 1 : TILE_MAX_ROWS;
            for (int row = 0; row < rows; row++) {
                Tile t = new Tile(context);
                t.setText(generateNewTileLetter());
                tileColumns.get(col).addToTop(t);
                addView(t);
                t.getTextPaint().setTextSize(20);
            }
        }

        // Pathing
        currentPath = new LinkedList<>();
        touchDownRow = -1;
        touchDownCol = -1;
    }

    /**
     * Performs the given action for each tile on this board.
     * @param action The action to perform on each tile.
     */
    public void forEachTile(Consumer<Tile> action) {
        tileColumns.forEach(tileBoardColumn -> tileBoardColumn.forEachTile(action));
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
        // Don't handle anything that's not the primary touch
        if (event.getActionIndex() != 0) {
            return false;
        }

        int col = (int) ((event.getX() - effectiveBoardOffset.x) / tileSize);
        int row = (int) ((col % 2 == 0)
                ? (event.getY() - effectiveBoardOffset.y - tileOffrowOffset) / tileSize
                : (event.getY() - effectiveBoardOffset.y) / tileSize);

        // Cancel everything if the touch slips to invalid territory.
        if ((col % 2 == 0 && row >= TILE_MAX_ROWS - 1)
                || row >= TILE_MAX_ROWS
                || col >= TILE_COLUMNS
                || row < 0
                || col < 0) {
            touchDownRow = -1;
            touchDownCol = -1;
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchDownRow = row;
                touchDownCol = col;
                return true;

            case MotionEvent.ACTION_UP:
                if (row == touchDownRow && col == touchDownCol) {
                    doActionForTile(row, col);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (touchDownRow != row || touchDownCol != col) {
                    touchDownRow = -1;
                    touchDownCol = -1;
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * Calculates all tiles positions based on their positions within the data structures.
     */
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
            }
        }
    }

    private void doActionForTile(int row, int col) {
        Tile currentTile = tileColumns.get(col).get(row);

        // Check if the tile is the first in the list, if so, clear the current path
        if (currentPath.size() != 0 && currentTile == currentPath.getFirst()) {
            currentPath.forEach(Tile::onRelease);
            currentPath.clear();
            onChange(ChangeEventType.CHANGE, null);
            return;
        }

        // Check if the tile is the last in the list, if so, submit and clear the path
        if (currentPath.size() != 0 && currentTile == currentPath.getLast()) {
            StringBuilder sb = new StringBuilder();
            currentPath.forEach(tile -> sb.append(tile.getText()));
            String currentWord = sb.toString();
            ChangeEventType eventType = ChangeEventType.FAILED_SUBMIT;

            System.out.println("Current word: " + currentWord);
            if (isWord(currentWord)) {
                currentPath.forEach(tile -> {
                    tile.setText(generateNewTileLetter());
                    tile.onRelease();
                    tileColumns.forEach(column -> column.reset(tile));
                });
                currentPath.clear();
                eventType = ChangeEventType.SUCCESSFUL_SUBMIT;
                updateTilePosition();
            }

            onChange(eventType, currentWord);
            return;
        }

        // Check if the tile exists in the path already, if so, cut selected tiles through to the
        // tapped tile.
        int tileIndex = currentPath.indexOf(currentTile);
        if (tileIndex != -1) {
            List<Tile> cutList = currentPath.subList(tileIndex + 1, currentPath.size());
            cutList.forEach(Tile::onRelease);
            cutList.clear();
            return;
        }

        // Otherwise, add the tile to the path
        // TODO: Add tile adjacency logic
        currentPath.add(currentTile);
        currentTile.onPress();
    }

    private boolean isWord(String string) {
        return (new Random()).nextBoolean(); // TODO: Implement
    }

    /**
     * Pass-through for eventHandler.onChange with a prerequisite null check.
     */
    private void onChange(ChangeEventType changeEventType, @Nullable String string) {
        if (eventHandler != null) {
            eventHandler.onChange(changeEventType, string);
        }
    }

    private String generateNewTileLetter() {
        // TODO: Create letters based on a distribution.
        char result = (char) (ALPHABET_START_OFFSET + (new Random()).nextInt(LETTERS_IN_ALPHABET));
        if (result == 'Q') {
            return "Qu";
        }
        return result + "";
    }
}
