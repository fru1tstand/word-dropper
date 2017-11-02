package me.fru1t.worddropper.ui.widget

import android.content.Context
import android.provider.BaseColumns
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.database.tables.GameWord

/** Contains game data compiled from multiple tables. */
data class GameData(
        var gameId: Long,
        var difficulty: String,
        var unixStart: Long,
        var gameStatus: Int,
        var score: Int,
        var words: Int
)

/** View holder data class for this list view. */
internal data class ViewHolder(
        val title: TextView,
        val description: TextView)

/** A colored list view that handles GameData. */
class GameListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0)
    : ColoredListView(context, attrs, defStyleAttr) {
    private @Inject lateinit var databaseUtils: DatabaseUtils

    private val adapter: ArrayAdapter<GameData>

    var titleFunction: (GameData) -> String = { "" }
    var descriptionFunction: (GameData) -> String = { "" }


    init {
        Slik.get(WordDropperApplication::class).inject(this)
        adapter = object : ArrayAdapter<GameData>(
                getContext(),
                R.layout.layout_widget_game_list_element) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val data = getItem(position) ?: throw ArrayIndexOutOfBoundsException()
                val result = convertView ?: LayoutInflater
                        .from(getContext())
                        .inflate(R.layout.layout_widget_game_list_element, parent, false)

                // Is this a new convert view?
                if (convertView == null) {
                    result.tag = ViewHolder(
                            result.findViewById(R.id.title) as TextView,
                            result.findViewById(R.id.description) as TextView)
                }

                // Set the data
                val holder = result.tag as ViewHolder
                holder.title.text = titleFunction(data)
                holder.description.text = descriptionFunction(data)

                return result
            }
        }
        setAdapter(adapter)
    }

    @JvmOverloads
    fun populate(whereCols: Array<String>? = null, whereArgs: Array<String>? = null): Boolean {
        if (whereCols != null && whereArgs == null
                || whereCols == null && whereArgs != null
                || whereArgs != null && whereCols!!.size != whereArgs.size) {
            throw IllegalArgumentException(
                    "The number of columns and arguments specified must be equal")
        }

        val where = StringBuilder(" ")
        if (whereCols != null) {
            where.append("WHERE 1 = 1 ")
            for (whereCol in whereCols) {
                where.append("AND ").append(whereCol).append(" = ? ")
            }
        }

        adapter.setNotifyOnChange(false)
        adapter.clear()
        if (databaseUtils.forEachResult("SELECT "
                    + BaseColumns._ID + ", "                   // 0
                    + Game.COLUMN_DIFFICULTY + ", "     // 1
                    + Game.COLUMN_UNIX_START + ", "     // 2
                    + Game.COLUMN_STATUS + ", "         // 3
                    + "agg_words.score AS score, "      // 4
                    + "agg_words.words AS words "       // 5
                + "FROM " + Game.TABLE_NAME + " "
                + "INNER JOIN (" // Note: This inner join removes any games with no submitted words
                    + "SELECT "
                        + GameWord.COLUMN_GAME_ID + ", "
                        + "SUM(" + GameWord.COLUMN_POINT_VALUE + ") AS score, "
                        + "COUNT(*) AS words "
                    + "FROM " + GameWord.TABLE_NAME + " "
                    + "GROUP BY " + GameWord.COLUMN_GAME_ID
                + ") agg_words ON agg_words." + GameWord.COLUMN_GAME_ID + " = "
                        + Game.TABLE_NAME + "." + BaseColumns._ID
                + where.toString()
                + "ORDER BY " + Game.COLUMN_UNIX_START + " DESC",
                whereArgs,
                { cursor ->
                    adapter.add(GameData(cursor.getLong(0), cursor.getString(1),
                            cursor.getLong(2), cursor.getInt(3), cursor.getInt(4),
                            cursor.getInt(5)))
                })) {
            adapter.notifyDataSetChanged()
            return true
        }
        return false
    }
}
