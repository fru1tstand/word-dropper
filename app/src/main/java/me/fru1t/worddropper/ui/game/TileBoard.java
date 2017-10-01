package me.fru1t.worddropper.ui.game;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;

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
         * Triggered when the user performs an action onWrapEventListener the board (eg. making a word, submitting
         * a word, etc).
         * @param changeEventType The type of change that occurred.
         * @param string The current word onWrapEventListener the board.
         */
        void onChange(ChangeEventType changeEventType, @Nullable String string);
    }

    // https://en.wikipedia.org/wiki/Letter_frequency
    private enum LetterFrequency {
        A(8167),
        B(1492),
        C(2782),
        D(4253),
        E(12702),
        F(2228),
        G(2015),
        H(6094),
        I(6966),
        J(153),
        K(772),
        L(4025),
        M(2406),
        N(6749),
        O(7507),
        P(1929),
        Q(95),
        R(5987),
        S(6327),
        T(9056),
        U(2758),
        V(978),
        W(2360),
        X(150),
        Y(1974),
        Z(74);

        public static final int CUMULATIVE_FREQUENCY = 99999;

        private @Getter int relativeFrequency;

        LetterFrequency(int relativeFrequency) {
            this.relativeFrequency = relativeFrequency;
        }
    }

    @AllArgsConstructor
    private static class PathElement {
        int row;
        int col;
        Tile tile;
    }

    private static final int SCRAMBLE_SPACING = 50;

    // Measurements
    private static final int TILE_COLUMNS = 7;
    private static final int TILE_MAX_ROWS = 8;

    // Left to right organization of columns.
    private final ArrayList<TileBoardColumn> tileColumns;
    private final WordDropperApplication app;
    private @Getter @Setter TileBoardEvents eventHandler;

    // Board sizing
    private final Point effectiveBoardSize = new Point();
    private final Point effectiveBoardOffset = new Point();
    private int tileSize;
    private int tileOffrowOffset;

    // Touch pathing
    private final ArrayList<PathElement> currentPath;
    private int touchDownRow;
    private int touchDownCol;
    private @Getter boolean enableTouching;

    public TileBoard(Context context) {
        this(context, null);
    }

    public TileBoard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileBoard(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();

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
                tileColumns.get(col).addToTop(t);
                addView(t);
            }
        }

        // Pathing
        currentPath = new ArrayList<>();
        touchDownRow = -1;
        touchDownCol = -1;
        enableTouching = true;
    }

    /**
     * Performs the given action for each tile onWrapEventListener this board.
     * @param action The action to perform onWrapEventListener each tile.
     */
    public void forEachTile(Consumer<Tile> action) {
        tileColumns.forEach(tileBoardColumn -> tileBoardColumn.forEachTile(action));
    }

    /**
     * Sets whether or not the board should respond to touch events.
     */
    public void setEnableTouching(boolean enableTouching) {
        this.enableTouching = enableTouching;
        if (!enableTouching) {
            currentPath.forEach(element -> element.tile.release());
            currentPath.clear();
            touchDownRow = -1;
            touchDownCol = -1;
        }
    }

    /**
     * Scrambles all tiles
     */
    public void scramble() {
        for (TileBoardColumn column : tileColumns) {
            for (int i = 0; i < column.getSize(); i++) {
                Tile t = column.get(i);
                t.setY(-1 * (column.getSize() - i) * (t.getSize() + SCRAMBLE_SPACING));
                t.setLetter(generateNewTileLetter());
                t.postInvalidate();
            }
        }

        updateTilePosition();
    }

    /**
     * @return Retrieves a serialized version of the board
     */
    public String getBoardState() {
        StringBuilder result = new StringBuilder();
        forEachTile(tile -> result.append(tile.getLetter()));
        return result.toString();
    }

    /**
     * Sets the current board state. If the operation fails, the board may be left in an unknown
     * state (between the old and new states).
     * @param boardState The board state.
     * @return Whether or not the operation was successful.
     */
    public boolean setBoardState(String boardState) {
        if (Strings.isNullOrEmpty(boardState)) {
            return false;
        }
        final int[] i = {0};
        final boolean[] didSucceed = {true};
        forEachTile(tile -> {
            if (i[0] > boardState.length()) {
                didSucceed[0] = false;
                return;
            }
            tile.setLetter(boardState.charAt(i[0]++));
        });
        return didSucceed[0];
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

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

        // We want an even tile size
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

        updateTilePosition();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Don't handle anything if touching is disabled
        if (!enableTouching) {
            return false;
        }

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
     * Calculates all tiles positions based onWrapEventListener their positions within the data
     * structures.
     */
    private void updateTilePosition() {
        if (tileSize == 0) {
            return;
        }

        int tileToAnimate = 0;

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
                tile.getLayoutParams().height = tileSize;
                tile.getLayoutParams().width = tileSize;
                tile.setSize(tileSize);

                // Don't animate if there's no change
                int newY = row * tileSize + rowYOffset + effectiveBoardOffset.y;
                if (tile.getY() == newY) {
                    continue;
                }

                tile.clearAnimation();
                ObjectAnimator animation = ObjectAnimator.ofFloat(tile, "y", newY);
                animation.setDuration(
                        getResources().getInteger(R.integer.animation_durationGameBoardTileDrop));
                (new Handler()).postDelayed(animation::start, tileToAnimate * getResources()
                        .getInteger(R.integer.animation_tileBoardSequentialTileDropDelay));
                tileToAnimate++;
            }
        }
    }

    /**
     * Logic for when a tile is pressed given the tile's row and column.
     */
    private void doActionForTile(int row, int col) {
        Tile currentTile = tileColumns.get(col).get(row);

        // Check if the tile is the first in the list, if so, clear the current path
        if (currentPath.size() != 0 && currentTile == currentPath.get(0).tile) {
            currentPath.forEach(pathElement -> pathElement.tile.release());
            currentPath.clear();
            onChange(ChangeEventType.CHANGE, null);
            return;
        }

        // Check if the tile is the last in the list, if so, submit, reset tiles used, and clear
        // the path
        if (currentPath.size() != 0
                && currentTile == currentPath.get(currentPath.size() - 1).tile) {
            ChangeEventType eventType = ChangeEventType.FAILED_SUBMIT;
            String currentWord = getCurrentPathString();

            if (app.getDictionary().isWord(currentWord)) {
                currentPath.forEach(pathElement -> {
                    pathElement.tile.setLetter(generateNewTileLetter());
                    pathElement.tile.release();
                    tileColumns.get(pathElement.col).reset(pathElement.tile);
                    pathElement.tile.setY(-1 * pathElement.tile.getSize());
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
        int tileIndex = -1;
        for (int i = 1; i < currentPath.size() - 1; i++) {
            if (currentPath.get(i).tile == currentTile) {
                tileIndex = i;
                break;
            }
        }
        if (tileIndex != -1) {
            List<PathElement> cutList = currentPath.subList(tileIndex + 1, currentPath.size());
            cutList.forEach(pe -> pe.tile.release());
            cutList.clear();
            onChange(ChangeEventType.CHANGE, getCurrentPathString());
            return;
        }

        // Are we currently in a path?
        if (!currentPath.isEmpty()) {
            // Check if the tile is adjacent to the end of the path, if so, add it to the path.
            PathElement lastPathElement = currentPath.get(currentPath.size() - 1);
            if (col == lastPathElement.col) {
                // Same column
                if (row + 1 != lastPathElement.row && row - 1 != lastPathElement.row) {
                    return;
                }
            } else if (col - 1 == lastPathElement.col || col + 1 == lastPathElement.col) {
                // Adjacent column
                if (col % 2 == 0) {
                    // Current col is short
                    if (row != lastPathElement.row && row + 1 != lastPathElement.row) {
                        return;
                    }
                } else {
                    // Current col is long
                    if (row - 1 != lastPathElement.row && row != lastPathElement.row) {
                        return;
                    }
                }
            } else {
                // Not even close
                return;
            }
        }

        currentPath.add(new PathElement(row, col, currentTile));
        currentTile.press();
        onChange(ChangeEventType.CHANGE, getCurrentPathString());
    }

    private String getCurrentPathString() {
        StringBuilder sb = new StringBuilder();
        currentPath.forEach(pathElement -> sb.append(pathElement.tile.getLetter()));
        return sb.toString().toLowerCase();
    }

    /**
     * Pass-through for eventHandler.onChange with a prerequisite null check.
     */
    private void onChange(ChangeEventType changeEventType, @Nullable String string) {
        if (eventHandler != null) {
            eventHandler.onChange(changeEventType, string);
        }
    }

    private char generateNewTileLetter() {
        int nextSeed = (new Random()).nextInt(LetterFrequency.CUMULATIVE_FREQUENCY);
        int cumulative = 0;
        for (LetterFrequency letter : LetterFrequency.values()) {
            cumulative += letter.getRelativeFrequency();
            if (cumulative > nextSeed) {
                return letter.name().charAt(0);
            }
        }
        return LetterFrequency.E.name().charAt(0);
    }
}