package me.fru1t.worddropper.ui.game

import android.util.SparseArray
import java.util.*

/** A data class that represents a node in a doubly-linked list. */
private data class Element(
        var tile: Tile,
        var above: Element? = null,
        var below: Element? = null,
        var index: Int? = null
)

/**
 * A single column within the tile board. It's a doubly linked list and hash map combination. This
 * implementation uses terms with respect to a vertical column where the rows are elements from top
 * to bottom. When iterating over the elements, the iterable interface gives tiles from the top to
 * the bottom (eg. in the order of a stack: FILO).
 *
 * This class prioritises fetching over inserts or "resets". Any reorganization of elements within
 * the column has an O(n) cost; however, fetching is always O(1). This is useful to us as the user
 * may select/deselect tiles rapidly (requiring a fetch), but performs a submit (causing a
 * reorganize) far less frequently.
 */
class TileBoardColumn {
    private val elements = HashMap<Tile, Element>()
    private val indexedElements = SparseArray<Element>()
    private var top: Element? = null
    private var bottom: Element? = null

    var size: Int = 0
        private set

    /** Calls [action] for each tile from top to bottom. */
    fun forEachTile(action: (Tile) -> Unit) {
        var e = top
        while (e != null) {
            action(e.tile)
            e = e.below
        }
    }

    /**
     * Adds a [tile] to the top of the column. If the given tile already exists in this column, it
     * is ignored. Adding is a O(n) operation.
     */
    fun addToTop(tile: Tile) {
        if (elements.containsKey(tile)) {
            return
        }

        val e = Element(tile)
        elements.put(tile, e)
        ++size

        // Case 1: The column was empty.
        if (elements.size == 1) {
            top = e
            bottom = e
            reindex()
            return
        }

        // Case 2: The column had 1 or more elements.
        top!!.above = e
        e.below = top
        top = e
        reindex()
    }

    /**
     * Resets a tile in this column by moving it to the top. Resetting is an O(n) operation.
     * @param tile The tile to remove.
     */
    fun reset(tile: Tile) {
        // Do we even own this tile?
        val e = elements[tile] ?: return

        // Case 1: The column only contains this single tile (covered by case 2)
        // Case 2: The tile is the top element
        if (e === top) {
            return // Do nothing
        }

        // Case 3: The tile is the bottom element
        if (e === bottom) {
            bottom!!.above!!.below = null
            bottom = bottom!!.above
            top!!.above = e
            e.below = top
            top = e
            reindex()
            return
        }

        // Case 4: The tile is somewhere in the middle
        e.above!!.below = e.below
        e.below!!.above = e.above
        top!!.above = e
        e.below = top
        top = e
        reindex()
    }

    /**
     * Retrieves the tile at [index] or null if it doesn't exist. Index 0 is the top of the column.
     * Fetching is O(1).
     */
    fun get(index: Int): Tile? = indexedElements.get(index)?.tile

    /** Reindexes the indexedElements structure. */
    private fun reindex() {
        indexedElements.clear()
        var e = top
        var i = 0
        while (e != null) {
            e.index = i
            indexedElements.put(i, e)
            e = e.below
            ++i
        }
    }
}
