package me.fru1t.worddropper.ui.game

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.settings.Dictionary
import java.util.ArrayList
import java.util.Random

/**
 * Represents the distribution that letters should be created at taken from
 * https://en.wikipedia.org/wiki/Letter_frequency
 */
private enum class LetterFrequency constructor(val relativeFrequency: Int) {
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
    companion object {
        val CUMULATIVE_FREQUENCY = 99999
    }
}

/** The types of change events that can occur. */
enum class ChangeEventType {
    SUCCESSFUL_SUBMIT, FAILED_SUBMIT, CHANGE
}

/** Represents a selected tile from the board. */
private data class PathElement(val row: Int, val col: Int, val tile: Tile)

/** The main game board where players create words from. */
class TileBoard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    // Injection
    private @Inject lateinit var dictionary: Dictionary

    // Left to right organization of columns.
    private val tileColumns = ArrayList<TileBoardColumn>()

    // Board sizing
    private val effectiveBoardSize = Point()
    private val effectiveBoardOffset = Point()
    private var tileSize = 0
    private var tileOffrowOffset = 0

    // Touch pathing
    private val currentPath = ArrayList<PathElement>()
    private var touchDownRow = -1
    private var touchDownCol = -1
    var enableTouching = true
        set(value) {
            field = value
            if (!value) {
                currentPath.forEach { element -> element.tile.release() }
                currentPath.clear()
                touchDownRow = -1
                touchDownCol = -1
            }
        }

    /** Called on submit or change of the tileboard passing the type and the current word. */
    var eventListener: ((ChangeEventType, String?) -> Unit)? = null

    init {
        Slik.get(WordDropperApplication::class).inject(this)

        // Create tiles
        for (col in 0 until TILE_COLUMNS) {
            tileColumns.add(TileBoardColumn())

            val rows = if (col % 2 == 0) TILE_MAX_ROWS - 1 else TILE_MAX_ROWS
            for (row in 0 until rows) {
                val t = Tile(context)
                tileColumns[col].addToTop(t)
                addView(t)
            }
        }
    }

    /** Retrieves a serialized version of the board */
    fun getBoardState(): String {
        val result = StringBuilder()
        forEachTile({ result.append(it.letter) })
        return result.toString()
    }

    /** Scrambles all tiles */
    fun scramble() {
        for (column in tileColumns) {
            for (i in 0 until column.size) {
                val t = column.get(i)!!
                t.y = (-1 * (column.size - i) * (t.size + SCRAMBLE_SPACING)).toFloat()
                t.letter = generateNewTileLetter()
                t.postInvalidate()
            }
        }

        updateTilePosition()
    }

    /**
     * Sets this board to [boardState] returning if the operation succeeded or not. A failed
     * operation may lead to the board being in an unknown state.
     */
    fun setBoardState(boardState: String): Boolean {
        if (boardState.isEmpty()) {
            return false
        }

        var i = 0
        var didSucceed = true
        forEachTile({
            if (i > boardState.length) {
                didSucceed = false
                return@forEachTile
            }
            it.letter = boardState[i++]
        })

        return didSucceed
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // Use the smallest edge for calculating
        val width = right - left
        val height = bottom - top

        if (width / TILE_COLUMNS > height / TILE_MAX_ROWS) {
            // Here, we're constrained by height, so we use the max rows as the divider.
            tileSize = height / TILE_MAX_ROWS
        } else {
            // Here, we're constrained by width, so we use columns as the divider.
            tileSize = width / TILE_COLUMNS
        }

        // We want an even tile size
        if (tileSize % 2 != 0) {
            tileSize--
        }

        // So that the offrow offset is an even number
        tileOffrowOffset = tileSize / 2

        // Set effective sizes
        effectiveBoardSize.set(tileSize * TILE_COLUMNS, tileSize * TILE_MAX_ROWS)
        effectiveBoardOffset.set( // This is where one can set the positioning of the board
                (width - effectiveBoardSize.x) / 2, // Middle
                (height - effectiveBoardSize.y) / 2) // Middle

        updateTilePosition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Don't handle anything if touching is disabled
        if (!enableTouching) {
            return false
        }

        // Don't handle anything that's not the primary touch
        if (event.actionIndex != 0) {
            return false
        }

        val col = ((event.x - effectiveBoardOffset.x) / tileSize).toInt()
        val row = (if (col % 2 == 0)
            (event.y - effectiveBoardOffset.y.toFloat() - tileOffrowOffset.toFloat()) / tileSize
        else
            (event.y - effectiveBoardOffset.y) / tileSize).toInt()

        // Cancel everything if the touch slips to invalid territory.
        if (col % 2 == 0 && row >= TILE_MAX_ROWS - 1
                || row >= TILE_MAX_ROWS
                || col >= TILE_COLUMNS
                || row < 0
                || col < 0) {
            touchDownRow = -1
            touchDownCol = -1
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownRow = row
                touchDownCol = col
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (row == touchDownRow && col == touchDownCol) {
                    doActionForTile(row, col)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchDownRow != row || touchDownCol != col) {
                    touchDownRow = -1
                    touchDownCol = -1
                }
                return true
            }

            else -> return false
        }
    }

    /**
     * Calculates all tiles positions based onWrapEventListener their positions within the data
     * structures.
     */
    private fun updateTilePosition() {
        if (tileSize == 0) {
            return
        }

        var tileToAnimate = 0

        for (col in 0 until TILE_COLUMNS) {
            var rows = TILE_MAX_ROWS
            var rowYOffset = 0
            if (col % 2 == 0) {
                rowYOffset = tileOffrowOffset
                rows -= 1
            }

            for (row in 0 until rows) {
                val tile = tileColumns[col].get(row)!!
                tile.x = (col * tileSize + effectiveBoardOffset.x).toFloat()
                tile.layoutParams.height = tileSize
                tile.layoutParams.width = tileSize
                tile.size = tileSize

                // Don't animate if there's no change
                val newY = (row * tileSize + rowYOffset + effectiveBoardOffset.y).toFloat()
                if (tile.y == newY.toFloat()) {
                    continue
                }

                tile.clearAnimation()
                val animation = ObjectAnimator.ofFloat(tile, "y", newY)
                animation.duration =
                        resources.getInteger(R.integer.animation_durationGameBoardTileDrop).toLong()
                Handler().postDelayed(
                        { animation.start() },
                        (tileToAnimate * resources.getInteger(
                                R.integer.animation_tileBoardSequentialTileDropDelay)).toLong())
                ++tileToAnimate
            }
        }
    }

    /** Logic for when a tile is pressed given the tile's row and column. */
    private fun doActionForTile(row: Int, col: Int) {
        val currentTile = tileColumns[col].get(row)!!

        // Check if the tile is the first in the list, if so, clear the current path
        if (currentPath.size != 0 && currentTile === currentPath[0].tile) {
            currentPath.forEach { pathElement -> pathElement.tile.release() }
            currentPath.clear()
            eventListener?.invoke(ChangeEventType.CHANGE, null)
            return
        }

        // Check if the tile is the last in the list, if so, submit, reset tiles used, and clear
        // the path
        if (currentPath.size != 0 && currentTile === currentPath[currentPath.size - 1].tile) {
            var eventType = ChangeEventType.FAILED_SUBMIT
            val currentWord = getCurrentPathString()

            if (dictionary.isWord(currentWord)) {
                currentPath.forEach { pathElement ->
                    pathElement.tile.letter = generateNewTileLetter()
                    pathElement.tile.release()
                    tileColumns[pathElement.col].reset(pathElement.tile)
                    pathElement.tile.y = (-1 * pathElement.tile.size).toFloat()
                }
                currentPath.clear()
                eventType = ChangeEventType.SUCCESSFUL_SUBMIT
                updateTilePosition()
            }

            eventListener?.invoke(eventType, currentWord)
            return
        }

        // Check if the tile exists in the path already, if so, cut selected tiles through to the
        // tapped tile.
        val tileIndex =
                (1 until currentPath.size).firstOrNull { currentPath[it].tile === currentTile }
        if (tileIndex != null) {
            val cutList = currentPath.subList(tileIndex + 1, currentPath.size)
            cutList.forEach { it.tile.release() }
            cutList.clear()
            eventListener?.invoke(ChangeEventType.CHANGE, getCurrentPathString())
            return
        }

        // Are we currently in a path?
        if (!currentPath.isEmpty()) {
            // Check if the tile is adjacent to the end of the path, if so, add it to the path.
            val lastPathElement = currentPath[currentPath.size - 1]
            if (col == lastPathElement.col) {
                // Same column
                if (row + 1 != lastPathElement.row && row - 1 != lastPathElement.row) {
                    return
                }
            } else if (col - 1 == lastPathElement.col || col + 1 == lastPathElement.col) {
                // Adjacent column
                if (col % 2 == 0) {
                    // Current col is short
                    if (row != lastPathElement.row && row + 1 != lastPathElement.row) {
                        return
                    }
                } else {
                    // Current col is long
                    if (row - 1 != lastPathElement.row && row != lastPathElement.row) {
                        return
                    }
                }
            } else {
                // Not even close
                return
            }
        }

        currentPath.add(PathElement(row, col, currentTile))
        currentTile.press()
        eventListener?.invoke(ChangeEventType.CHANGE, getCurrentPathString())
    }

    /** Performs an [action] for all tiles in the board */
    private fun forEachTile(action: (Tile) -> Unit) = tileColumns.forEach { it.forEachTile(action) }

    /** Retrieves the currently selected string of characters. */
    private fun getCurrentPathString(): String  {
        val sb = StringBuilder()
        currentPath.forEach { pathElement -> sb.append(pathElement.tile.letter) }
        return sb.toString().toLowerCase()
    }

    /** Creates a new letter in accordance to the letter frequency distribution. */
    private fun generateNewTileLetter(): Char {
        val nextSeed = Random().nextInt(LetterFrequency.CUMULATIVE_FREQUENCY)
        var cumulative = 0
        for (letter in LetterFrequency.values()) {
            cumulative += letter.relativeFrequency
            if (cumulative > nextSeed) {
                return letter.name[0]
            }
        }
        return LetterFrequency.E.name[0]
    }

    companion object {
        private val SCRAMBLE_SPACING = 50

        // Measurements
        private val TILE_COLUMNS = 7
        private val TILE_MAX_ROWS = 8
    }
}
