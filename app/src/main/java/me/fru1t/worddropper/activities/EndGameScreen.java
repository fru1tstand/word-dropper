package me.fru1t.worddropper.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.fru1t.worddropper.R;

public class EndGameScreen extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_MOVES = "extra_moves";
    public static final String EXTRA_SCRAMBLES_EARNED = "extra_scrambles_earned";
    public static final String EXTRA_SCRAMBLES_USED = "extra_scrambles_used";
    public static final String EXTRA_LEVEL = "extra_level";

    private TextView score;
    private TextView level;
    private TextView scramblesUsed;
    private TextView scramblesEarned;
    private TextView words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game_screen);

        LinearLayout root = (LinearLayout) findViewById(R.id.endGameScreenRoot);

        // Populate data
        score = (TextView) root.findViewById(R.id.endGameScreenScore);
        level = (TextView) root.findViewById(R.id.endGameScreenLevel);
        scramblesUsed = (TextView) root.findViewById(R.id.endGameScreenScramblesUsed);
        scramblesEarned = (TextView) root.findViewById(R.id.endGameScreenScramblesEarned);
        words = (TextView) root.findViewById(R.id.endGameScreenWords);

        score.setText(getIntent().getStringExtra(EXTRA_SCORE));
        level.setText(getIntent().getStringExtra(EXTRA_LEVEL));
        scramblesUsed.setText(getIntent().getStringExtra(EXTRA_SCRAMBLES_USED));
        scramblesEarned.setText(getIntent().getStringExtra(EXTRA_SCRAMBLES_EARNED));
        words.setText(getIntent().getStringExtra(EXTRA_MOVES));
    }
}
