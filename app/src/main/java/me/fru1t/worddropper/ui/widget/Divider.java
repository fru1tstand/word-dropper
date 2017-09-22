package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy;
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml;

/**
 * A wrapper for a color-able view to act like a divider.
 */
public class Divider extends View {
    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    public Divider(Context context) {
        this(context, null);
    }

    public Divider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Divider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs == null) {
            throw new RuntimeException("Divider requires the backgroundColorTheme attribute, but " +
                    "no attributes were given");
        }

        app = (WordDropperApplication) context.getApplicationContext();
        proxy = new ColorThemeViewProxy(this, attrs, R.styleable.Divider,
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.Divider_backgroundColorTheme,
                        ColorThemeXml.TEXT,
                        this::setBackgroundColor));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.removeColorThemeEventHandler(proxy);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.addColorThemeEventHandler(proxy);
    }
}
