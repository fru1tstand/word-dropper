package me.fru1t.worddropper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropper;
import me.fru1t.worddropper.settings.ColorTheme;

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

        root.setBackgroundColor(WordDropper.colorTheme.background);
        ColorTheme.set(TextView::setTextColor, WordDropper.colorTheme.primary,
                titleWord, titleDropper);
        ColorTheme.set(TextView::setTextColor, WordDropper.colorTheme.text, options);
    }

    public void onOptionPlayClick(View view) {
        startActivity(new Intent(this, GameScreen.class));
    }

    public void onOptionStatsClick(View view) {

    }

    public void onOptionSettingsClick(View view) {

    }
}
