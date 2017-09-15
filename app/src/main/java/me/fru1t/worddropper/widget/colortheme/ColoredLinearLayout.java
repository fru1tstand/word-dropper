package me.fru1t.worddropper.widget.colortheme;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy;
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml;

/**
 * An automatically colored LinearLayout
 */
public class ColoredLinearLayout extends LinearLayout {
    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    public ColoredLinearLayout(Context context) {
        this(context, null, 0);
    }

    public ColoredLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColoredLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();
        proxy = new ColorThemeViewProxy(this, attrs, R.styleable.ColoredLinearLayout,
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredLinearLayout_backgroundColorTheme,
                        ColorThemeXml.BACKGROUND,
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
