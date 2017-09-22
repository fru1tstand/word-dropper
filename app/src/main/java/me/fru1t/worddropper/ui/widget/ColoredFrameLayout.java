package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy;
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml;

/**
 * A FrameLayout that's automatically colored by the color theme.
 */
public class ColoredFrameLayout extends FrameLayout {
    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    public ColoredFrameLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public ColoredFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColoredFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();
        proxy = new ColorThemeViewProxy(this, attrs, R.styleable.ColoredFrameLayout,
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredFrameLayout_backgroundColorTheme,
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
