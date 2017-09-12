package me.fru1t.worddropper.widget.tileboard;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.function.Consumer;

import lombok.Getter;

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
public class TileBoardColumn {
    private static class Element {
        @Nullable Element above;
        @Nullable Element below;
        Tile tile;
        Integer index;
    }

    private final HashMap<Tile, Element> elements;
    private @Nullable Element top;
    private @Nullable Element bottom;
    private transient final SparseArray<Element> indexedElements;

    private @Getter int size;

    public TileBoardColumn() {
        elements = new HashMap<>();
        indexedElements = new SparseArray<>();
        top = null;
        bottom = null;
        size = 0;
    }

    /**
     * Performs an action onWrapEventListener each tile that belongs to this column.
     * @param action
     */
    public void forEachTile(Consumer<Tile> action) {
        Element e = top;
        while (e != null) {
            action.accept(e.tile);
            e = e.below;
        }
    }

    /**
     * Adds a tile to the top of the column. If the given tile already exists in this column, it is
     * ignored. Adding is a O(n) operation.
     * @param tile The tile to add.
     */
    public void addToTop(Tile tile) {
        if (elements.containsKey(tile)) {
            return;
        }

        Element e = new Element();
        e.tile = tile;
        elements.put(tile, e);
        ++size;

        // Case 1: The column was empty.
        if (elements.size() == 1) {
            top = e;
            bottom = e;
            reindex();
            return;
        }

        // Case 2: The column had 1 or more elements.
        assert top != null;
        top.above = e;
        e.below = top;
        top = e;
        reindex();
    }

    /**
     * Resets a tile in this column by moving it to the top. Resetting is an O(n) operation.
     * @param tile The tile to remove.
     */
    public void reset(Tile tile) {
        Element e = elements.get(tile);

        // Do we even own this tile?
        if (e == null) {
            return;
        }

        // Case 1: The column only contains this single tile (covered by case 2)
        // Case 2: The tile is the top element
        if (e == top) {
            // Do nothing
            return;
        }

        // Case 3: The tile is the bottom element
        if (e == bottom) {
            assert bottom.above != null;
            assert top != null;
            bottom.above.below = null;
            bottom = bottom.above;
            top.above = e;
            e.below = top;
            top = e;
            reindex();
            return;
        }

        // Case 4: The tile is somewhere in the middle
        assert e.above != null;
        assert e.below != null;
        assert top != null;
        e.above.below = e.below;
        e.below.above = e.above;
        top.above = e;
        e.below = top;
        top = e;
        reindex();
    }

    /**
     * Retrieves a tile from this column. Index 0 is the top of a vertical column. Fetching is an
     * O(1) operation.
     * @param index The index of the tile.
     * @return The tile at the given index.
     */
    public Tile get(int index) {
        return indexedElements.get(index).tile;
    }

    /**
     * Reindexes the indexedElements structure.
     */
    private void reindex() {
        indexedElements.clear();
        Element e = top;
        int i = 0;
        while (e != null) {
            e.index = i;
            indexedElements.put(i, e);
            e = e.below;
            ++i;
        }
    }
}
