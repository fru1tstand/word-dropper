package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
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
    private static final int NO_MAX_HEIGHT = -1;

    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    private @Nullable Integer defaultHeight;
    private @Px int maxHeight;

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (maxHeight == NO_MAX_HEIGHT) {
            return;
        }

        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height == maxHeight) {
            return;
        }

        if (height > maxHeight) {
            // Store initial height as default
            if (defaultHeight == null) {
                defaultHeight = getLayoutParams().height;
            }
            getLayoutParams().height = maxHeight;
            requestLayout();
        } else {
            if (defaultHeight != null) {
                getLayoutParams().height = defaultHeight;
            }
        }
    }

    /**
     * Sets this element's maximum height.
     */
    public void setMaxHeight(@Px int maxHeight) {
        this.maxHeight = maxHeight;
    }
}
