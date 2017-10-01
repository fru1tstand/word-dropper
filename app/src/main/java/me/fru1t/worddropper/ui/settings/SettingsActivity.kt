package me.fru1t.worddropper.ui.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

import me.fru1t.worddropper.R
import me.fru1t.worddropper.settings.ColorTheme

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Add color themes
        val colorThemeList = findViewById(R.id.colorThemeList) as LinearLayout
        for (colorTheme in ColorTheme.values()) {
            colorThemeList.addView(ColorThemeListElement(this, colorTheme))
        }
    }
}
