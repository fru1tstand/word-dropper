package me.fru1t.worddropper.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * Styles a menu. This is the driver to layout_menu.xml.
 */
public class MenuLayout extends RelativeLayout implements ColorThemeEventHandler {
    private final WordDropperApplication app;
    private @Getter boolean isOpen;
    private @Nullable @Setter Runnable onShowListener;
    private @Nullable @Setter Runnable onHideListener;

    private LinearLayout menu;
    private final ArrayList<TextView> menuOptions;

    public MenuLayout(Context context) {
        this(context, null);
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.layout_menu, this);

        app = (WordDropperApplication) context.getApplicationContext();
        menuOptions = new ArrayList<>();
        isOpen = false;
    }

    /**
     * Adds a menu option to this menu.
     * @param resId The display title string resource id.
     * @param closeOnSelect Whether or not to close the menu after the option was selected.
     * @param action The action to perform when the option is clicked.
     * @return The text view that represents this menu option.
     */
    public TextView addMenuOption(@StringRes int resId, boolean closeOnSelect, Runnable action) {
        TextView result = new TextView(getContext());
        result.setText(getResources().getText(resId));
        result.setGravity(Gravity.CENTER);
        result.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.menuLayout_textSize));
        result.setClickable(true);
        result.setOnClickListener(v -> {
            if (closeOnSelect) {
                hide();
            }
            action.run();
        });

        menu.addView(result);
        result.getLayoutParams().height =
                (int) getResources().getDimension(R.dimen.menuLayout_optionHeight);
        result.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

        menuOptions.add(result);
        return result;
    }

    /**
     * Animate opens the menu if it's not already. The callback is called after the menu is fully
     * open. The callback is immediately called if the menu is already open. This method will update
     * colors.
     */
    public void show() {
        if (isOpen) {
            if (onShowListener != null) {
                onShowListener.run();
            }
            return;
        }

        isOpen = true;
        AlphaAnimation aa = new AlphaAnimation(0f, 1f);
        aa.setInterpolator(new AccelerateInterpolator());
        aa.setDuration(getResources().getInteger(R.integer.animation_durationResponsive));
        aa.setFillAfter(true);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                if (onShowListener != null) {
                    onShowListener.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });
        startAnimation(aa);
    }

    /**
     * Animate closes the menu if it's not already closed. The callback is called after the menu is
     * fully closed. The callback is immediately called if the menu is already closed.
     */
    public void hide() {
        if (!isOpen) {
            if (onHideListener != null) {
                onHideListener.run();
            }
            return;
        }

        isOpen = false;
        AlphaAnimation aa = new AlphaAnimation(1f, 0f);
        aa.setInterpolator(new AccelerateInterpolator());
        aa.setDuration(getResources().getInteger(R.integer.animation_durationResponsive));
        aa.setFillAfter(true);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                if (onHideListener != null) {
                    onHideListener.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });
        startAnimation(aa);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        menu = (LinearLayout) findViewById(R.id.menuMenu);

        setClickable(true);
        setOnClickListener(v -> hide());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.addColorThemeEventHandler(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.removeColorThemeEventHandler(this);
    }

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        menu.setBackgroundColor(colorTheme.backgroundLight);
        menuOptions.forEach(tv -> tv.setTextColor(colorTheme.text));
    }
}
