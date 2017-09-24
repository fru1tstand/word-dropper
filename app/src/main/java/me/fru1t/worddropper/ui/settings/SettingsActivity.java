package me.fru1t.worddropper.ui.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.settings.ColorTheme;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Add color themes
        LinearLayout colorThemeList = (LinearLayout) findViewById(R.id.colorThemeList);
        for (ColorTheme colorTheme : ColorTheme.values()) {
            colorThemeList.addView(new ColorThemeListElement(this, colorTheme));
        }
    }
}
