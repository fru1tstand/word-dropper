package me.fru1t.worddropper.layout;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
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

/**
 * Styles a menu. This is the driver to layout_menu.xml.
 */
public class MenuLayout extends RelativeLayout {
    private static final int MENU_WRAPPER_BACKGROUND_COLOR = Color.argb(128, 0, 0, 0);
    
    private final WordDropperApplication app;
    private @Getter boolean isOpen;
    private @Nullable @Setter Runnable onShowListener;
    private @Nullable @Setter Runnable onHideListener;

    private LinearLayout menu;
    private final ArrayList<TextView> menuOptions;

    public MenuLayout(Context context) {
        super(context);
        app = (WordDropperApplication) context.getApplicationContext();
        menuOptions = new ArrayList<>();
        isOpen = false;
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        app = (WordDropperApplication) context.getApplicationContext();
        menuOptions = new ArrayList<>();
        isOpen = false;
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        app = (WordDropperApplication) context.getApplicationContext();
        menuOptions = new ArrayList<>();
        isOpen = false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        menu = (LinearLayout) findViewById(R.id.menuMenu);
        menu.setBackgroundColor(app.getColorTheme().backgroundLight);

        setClickable(true);
        setOnClickListener(v -> hide());
        setBackgroundColor(MENU_WRAPPER_BACKGROUND_COLOR);
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
        result.setTextColor(app.getColorTheme().text);

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
        // Update colors
        menu.setBackgroundColor(app.getColorTheme().backgroundLight);
        menuOptions.forEach(tv -> tv.setTextColor(app.getColorTheme().text));

        if (isOpen) {
            if (onShowListener != null) {
                onShowListener.run();
            }
            return;
        }

        isOpen = true;
        AlphaAnimation aa = new AlphaAnimation(0f, 1f);
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
}
