package me.fru1t.worddropper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;

/**
 * A wrapper for a color-able view to act like a divider.
 */
public class Divider extends View {
    private static final int INVALID_COLOR_THEME = -1;

    private final WordDropperApplication app;
    private final int xmlColor;

    public Divider(Context context) {
        this(context, null);
    }

    public Divider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Divider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs == null) {
            throw new RuntimeException("Divider requires the colorTheme attribute, but no " +
                    "attributes were given");
        }
        app = (WordDropperApplication) context.getApplicationContext();

        TypedArray styledAttributes =
                context.obtainStyledAttributes(attrs, R.styleable.Divider, defStyleAttr, 0);
        xmlColor = styledAttributes.getInt(R.styleable.Divider_colorTheme, INVALID_COLOR_THEME);
        styledAttributes.recycle();

        if (xmlColor == INVALID_COLOR_THEME) {
            throw new RuntimeException("Divider requires a valid colorTheme enum value from the" +
                    " colorTheme attr defined within attrs.xml");
        }
    }

    /**
     * Updates this view's colors determined by the colorTheme.
     */
    public void updateColor() {
        setBackgroundColor(app.getColorTheme().getColorFromXmlEnum(xmlColor));
    }
}
