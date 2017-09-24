package me.fru1t.worddropper.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;

/**
 * Displays the name and sample colors for a specific color theme.
 */
public class ColorThemeListElement extends LinearLayout {
    public ColorThemeListElement(Context context, ColorTheme colorTheme) {
        super(context);

        LayoutParams layout = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layout);

        int hPadding = (int) getResources().getDimension(R.dimen.app_edgeSpace);
        int vPadding = (int) getResources().getDimension(R.dimen.app_vSpace);
        setPadding(hPadding, vPadding, hPadding, vPadding);

        WordDropperApplication app = (WordDropperApplication) context.getApplicationContext();
        setOnClickListener(
                v -> app.putStringPreference(R.string.pref_colorTheme, colorTheme.name()));

        setOrientation(VERTICAL);
        setClickable(true);

        LayoutInflater.from(context).inflate(R.layout.layout_color_theme_list_element, this);

        // Style elements
        setBackgroundColor(colorTheme.background);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(colorTheme.displayName);
        title.setTextColor(colorTheme.text);

        findViewById(R.id.colorThemePrimary).setBackgroundColor(colorTheme.primary);
        findViewById(R.id.colorThemePrimaryDark).setBackgroundColor(colorTheme.primaryDark);
        findViewById(R.id.colorThemePrimaryLight).setBackgroundColor(colorTheme.primaryLight);
        findViewById(R.id.colorThemePrimaryBackgroundLight)
                .setBackgroundColor(colorTheme.backgroundLight);
        findViewById(R.id.colorThemeTextBlend).setBackgroundColor(colorTheme.textBlend);
    }
}
