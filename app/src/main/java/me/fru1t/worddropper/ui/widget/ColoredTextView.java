package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy;
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml;

/**
 * A text view wrapper that automatically updates its colors on colorTheme change.
 */
public class ColoredTextView extends AppCompatTextView {
    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    public ColoredTextView(Context context) {
        this(context, null);
    }

    public ColoredTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColoredTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();
        proxy = new ColorThemeViewProxy(this, attrs, R.styleable.ColoredTextView,
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredTextView_textColorTheme,
                        ColorThemeXml.TEXT,
                        this::setTextColor),
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredTextView_backgroundColorTheme,
                        ColorThemeXml.TRANSPARENT,
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
