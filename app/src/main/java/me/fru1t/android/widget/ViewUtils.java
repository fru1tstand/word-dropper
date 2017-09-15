package me.fru1t.android.widget;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for manipulating XML layouts.
 * TODO: Remove if not used
 */
public class ViewUtils {
    private ViewUtils() { }

    /**
     * Retrieves all child elements from a view group that are of a specific type. Recursively
     * searches through child view groups. Makes no guarantees of the order of retrieval.
     * @see #getElementsByTagName(ViewGroup, Class, boolean)
     */
    public static <T extends View> List<T> getElementsByTagName(ViewGroup root, Class<T> tag) {
        return getElementsByTagName(root, tag, true);
    }

    /**
     * Retrieves child elements from a view group that are of a specific type. This method makes no
     * guarantees of the order of elements retrieved. This method ignores inheritance and only
     * returns elements that are exactly the specified tag. For example, if <code>View.class</code>
     * is specified as the tag, this method will only return <code>&lt;View&gt;</code> elements
     * and nothing that inherits from it like <code>&lt;TextView&gt;</code>.
     * @param root The root view group to start searching at.
     * @param tag The type of tag to search for.
     * @param includeChildren Whether or not the search should recursively search through child view
     *                        groups.
     * @param <T> The type of tag to search for.
     * @return A list of the child elements of the given tag type from the root.
     */
    public static <T extends View> List<T> getElementsByTagName(ViewGroup root, Class<T> tag,
            boolean includeChildren) {
        ArrayList<T> result = new ArrayList<>();
        for (int i = root.getChildCount() - 1; i >= 0; i--) {
            View v = root.getChildAt(i);
            if (v.getClass() == tag) {
                //noinspection unchecked
                result.add((T) v);
            }

            if (includeChildren && v instanceof ViewGroup) {
                result.addAll(getElementsByTagName((ViewGroup) v, tag, true));
            }
        }

        return result;
    }
}
