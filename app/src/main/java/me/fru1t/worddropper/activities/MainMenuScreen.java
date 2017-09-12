package me.fru1t.worddropper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.Difficulty;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainMenuScreen extends AppCompatActivity {
    private static final int[] OPTION_RESOURCE_IDS = {
            R.id.mainMenuScreenOptionPlay,
            R.id.mainMenuScreenOptionStats,
            R.id.mainMenuScreenOptionSettings
    };

    private WordDropperApplication app;

    private FrameLayout root;
    private TextView titleWord;
    private TextView titleDropper;

    private final TextView[] options;

    public MainMenuScreen() {
        options = new TextView[OPTION_RESOURCE_IDS.length];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_screen);
        app = (WordDropperApplication) getApplicationContext();

        root = (FrameLayout) findViewById(R.id.mainMenuScreenRoot);
        titleWord = (TextView) root.findViewById(R.id.mainMenuScreenTitleWord);
        titleDropper = (TextView) root.findViewById(R.id.mainMenuScreenTitleDropper);

        for (int i = 0; i < OPTION_RESOURCE_IDS.length; i++) {
            options[i] = (TextView) root.findViewById(OPTION_RESOURCE_IDS[i]);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        root.setBackgroundColor(app.getColorTheme().background);
        ColorTheme.set(TextView::setTextColor, app.getColorTheme().primary,
                titleWord, titleDropper);
        ColorTheme.set(TextView::setTextColor, app.getColorTheme().text, options);
    }

    @VisibleForXML
    public void onOptionPlayClick(View view) {
        Intent gameScreenIntent = new Intent(this, GameScreen.class);
        gameScreenIntent.putExtra(GameScreen.EXTRA_DIFFICULTY, Difficulty.MEDIUM.name());

        startActivity(gameScreenIntent);
    }

    @VisibleForXML
    public void onOptionStatsClick(View view) { }

    @VisibleForXML
    public void onOptionSettingsClick(View view) { }
}
