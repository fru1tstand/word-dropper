package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.database.tables.GameWord;

/**
 * A colored list view that handles GameData.
 */
public class GameListView extends ColoredListView {
    /**
     * Simple java object that contains game data compiled from multiple tables.
     */
    @AllArgsConstructor
    public static class GameData {
        public long gameId;
        public String difficulty;
        public long unixStart;
        public int gameStatus;
        public int score;
        public int words;
    }

    private final ArrayAdapter<GameData> adapter;
    private final WordDropperApplication app;

    private @Nullable @Setter Function<GameData, String> titleFunction;
    private @Nullable @Setter Function<GameData, String> descriptionFunction;

    public GameListView(@NonNull Context context) {
        this(context, null);
    }

    public GameListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameListView(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();

        adapter = new ArrayAdapter<GameData>(getContext(), R.layout.layout_game_list_element) {
            class ViewHolder {
                TextView title;
                TextView description;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView,
                    @NonNull ViewGroup parent) {
                GameData data = getItem(position);
                if (data == null) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                // Are we recycling?
                if (convertView == null) {
                    convertView = LayoutInflater
                            .from(getContext())
                            .inflate(R.layout.layout_game_list_element, parent, false);

                    ViewHolder holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.description = (TextView) convertView.findViewById(R.id.description);

                    convertView.setTag(holder);
                }

                // Set the data
                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.title.setText(
                        (titleFunction != null) ? titleFunction.apply(data) : "");
                holder.description.setText(
                        (descriptionFunction != null) ? descriptionFunction.apply(data) : "");

                return convertView;
            }
        };
        setAdapter(adapter);
    }

    public boolean populate() {
        return populate(null, null);
    }

    public boolean populate(@Nullable String[] whereCols, @Nullable String[] whereArgs) {
        if ((whereCols != null && whereArgs == null)
                || (whereCols == null && whereArgs != null)
                || (whereArgs != null && whereCols.length != whereArgs.length)) {
            throw new IllegalArgumentException(
                    "The number of columns and arguments specified must be equal");
        }

        StringBuilder where = new StringBuilder(" ");
        if (whereCols != null) {
            where.append("WHERE 1 = 1 ");
            for (String whereCol : whereCols) {
                where.append("AND ").append(whereCol).append(" = ? ");
            }
        }

        adapter.setNotifyOnChange(false);
        adapter.clear();
        if (app.getDatabaseUtils().forEachResult("SELECT "
                    + Game._ID + ", "                   // 0
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
                        + Game.TABLE_NAME + "." + Game._ID
                + where.toString()
                + "ORDER BY " + Game.COLUMN_UNIX_START + " DESC",
                whereArgs,
                cursor -> adapter.add(new GameData(cursor.getLong(0), cursor.getString(1),
                        cursor.getLong(2), cursor.getInt(3), cursor.getInt(4),
                        cursor.getInt(5))))) {
            adapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }
}
