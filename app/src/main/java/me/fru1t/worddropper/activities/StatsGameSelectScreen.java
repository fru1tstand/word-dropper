package me.fru1t.worddropper.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.AllArgsConstructor;
import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.database.tables.GameWord;

public class StatsGameSelectScreen extends AppCompatActivity {
    @AllArgsConstructor
    private static class GameData {
        public final long gameId;
        public final String title;
        public final String description;
    }

    private static class GameListAdapter extends ArrayAdapter<GameData> {
        private static class ViewHolder {
            TextView title;
            TextView description;
        }

        GameListAdapter(@NonNull Context context,
                @LayoutRes int resource,
                @NonNull List<GameData> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            GameData data = getItem(position);
            if (data == null) {
                throw new ArrayIndexOutOfBoundsException();
            }

            // Are we recycling?
            if (convertView == null) {
                convertView = LayoutInflater
                        .from(getContext())
                        .inflate(R.layout.layout_stats_game_select_list_element, parent, false);

                ViewHolder holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.description = (TextView) convertView.findViewById(R.id.description);

                convertView.setTag(holder);
            }

            // Set the data
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(data.title);
            holder.description.setText(data.description);

            return convertView;
        }
    }

    private static final SimpleDateFormat TITLE_DATE_FORMAT =
            new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_game_select_screen);
        WordDropperApplication app = (WordDropperApplication) getApplicationContext();

        // Fetch data
        ArrayList<GameData> data = new ArrayList<>();
        Cursor cursor = app.getDatabaseUtils().getReadableDatabase().rawQuery("SELECT "
                + Game._ID + ", "                   // 0
                + Game.COLUMN_DIFFICULTY + ", "     // 1
                + Game.COLUMN_UNIX_START + ", "     // 2
                + Game.COLUMN_STATUS + ", "         // 3
                + "agg_words.score AS score, "      // 4
                + "agg_words.words AS words "       // 5
                + "FROM " + Game.TABLE_NAME + " "
                + "INNER JOIN ("
                    + "SELECT "
                    + GameWord.COLUMN_GAME_ID + ", "
                    + "SUM(" + GameWord.COLUMN_POINT_VALUE + ") AS score, "
                    + "COUNT(*) AS words "
                    + "FROM " + GameWord.TABLE_NAME + " "
                    + "GROUP BY " + GameWord.COLUMN_GAME_ID
                + ") agg_words ON agg_words." + GameWord.COLUMN_GAME_ID + " = " + Game.TABLE_NAME + "." + Game._ID + " "
                + "ORDER BY " + Game.COLUMN_UNIX_START + " DESC",
                null);
        if (cursor.moveToFirst()) {
            do {
                data.add(new GameData(
                        cursor.getLong(0),
                        cursor.getString(1).toUpperCase()
                                + " - " + TITLE_DATE_FORMAT.format(new Date(cursor.getLong(2) * 1000)),
                        cursor.getInt(5) + " words"
                                + " - " + cursor.getInt(4) + " points"
                                + ((cursor.getInt(3) == Game.STATUS_IN_PROGRESS)
                                        ? " - in progress" : "")
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Populate list
        if (data.size() == 0) {
            findViewById(R.id.noDataWarning).setVisibility(View.VISIBLE);
        }

        ListView gameList = (ListView) findViewById(R.id.gameList);
        gameList.setAdapter(
                new GameListAdapter(this, R.layout.layout_stats_game_select_list_element, data));

        // Our action
        gameList.setOnItemClickListener((parent, view, position, id) -> {
            Intent endGameIntent = new Intent(this, EndGameScreen.class);
            endGameIntent.putExtra(EndGameScreen.EXTRA_GAME_ID,
                    ((GameData) parent.getItemAtPosition(position)).gameId);
            startActivity(endGameIntent);
        });
    }

    @VisibleForXML
    public void onBackClick(View v) {
        finish();
    }
}
