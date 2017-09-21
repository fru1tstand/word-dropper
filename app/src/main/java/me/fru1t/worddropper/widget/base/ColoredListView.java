package me.fru1t.worddropper.widget.base;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ListView;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy;
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml;

/**
 * A ListView that's automatically colored by the color theme.
 */
public class ColoredListView extends ListView {
    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    public ColoredListView(@NonNull Context context) {
        this(context, null);
    }

    public ColoredListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColoredListView(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();
        proxy = new ColorThemeViewProxy(this, attrs, R.styleable.ColoredListView,
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredListView_backgroundColorTheme,
                        ColorThemeXml.TEXT_BLEND,
                        color -> {
                            ColoredListView.this.setDivider(new ColorDrawable(color));
                            ColoredListView.this.setDividerHeight(1);
                        }
                ));
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
