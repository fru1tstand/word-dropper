package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.colortheme.ColorThemeViewProxy;
import me.fru1t.worddropper.settings.colortheme.ColorThemeXml;

/**
 * Emulates the Android ToolBar with solely plain text.
 */
public class ColoredToolBar extends LinearLayout {
    private final WordDropperApplication app;
    private final ColorThemeViewProxy proxy;

    public ColoredToolBar(Context context) {
        this(context, null);
    }

    public ColoredToolBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColoredToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_colored_tool_bar, this);
        TextView title = (TextView) root.findViewById(R.id.title);

        app = (WordDropperApplication) context.getApplicationContext();
        proxy = new ColorThemeViewProxy(this, attrs, R.styleable.ColoredToolBar,
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredToolBar_backgroundColorTheme,
                        ColorThemeXml.PRIMARY,
                        this::setBackgroundColor
                ),
                new ColorThemeViewProxy.AttributeMap(
                        R.styleable.ColoredToolBar_textColorTheme,
                        ColorThemeXml.TEXT_ON_PRIMARY,
                        title::setTextColor
                ));

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ColoredToolBar);
        title.setText(styledAttrs.getText(R.styleable.ColoredToolBar_toolBarText));
        styledAttrs.recycle();

        int paddingHorizontal = (int) getResources().getDimension(R.dimen.app_edgeSpace);
        int paddingVertical = (int) getResources().getDimension(R.dimen.app_vSpace);
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
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
