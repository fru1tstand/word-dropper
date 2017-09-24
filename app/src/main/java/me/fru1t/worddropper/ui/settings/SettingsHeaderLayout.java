package me.fru1t.worddropper.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.ui.widget.ColoredTextView;

/**
 * Packages a title and a divider together to form a settings header.
 */
public class SettingsHeaderLayout extends LinearLayout {
    public SettingsHeaderLayout(Context context) {
        this(context, null);
    }

    public SettingsHeaderLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsHeaderLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.layout_settings_header, this);

        TypedArray styledAttrs =
                context.obtainStyledAttributes(attrs, R.styleable.SettingsHeaderLayout);
        ((ColoredTextView) findViewById(R.id.title))
                .setText(styledAttrs.getText(R.styleable.SettingsHeaderLayout_settingsHeaderText));
        styledAttrs.recycle();
    }
}
