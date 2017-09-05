package me.fru1t.worddropper.widget.gameboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import me.fru1t.worddropper.WordDropper;

/**
 * The pause menu that's brought up
 */
public class PauseMenu extends FrameLayout {
    public interface PauseMenuEventListener {
        void onEndGame();
        void onClose();
    }

    @Data
    @AllArgsConstructor
    private static class MenuOption {
        private final String name;
        private final Consumer<PauseMenuEventListener> action;
    }

    public static final int WIDTH = 450;
    public static final int HEIGHT = 1000;

    private static final int FONT_SIZE = 16;

    private static final MenuOption[] MENU_OPTIONS = {
            new MenuOption("End Game", PauseMenuEventListener::onEndGame),
            new MenuOption("Close Menu", PauseMenuEventListener::onClose)
    };

    private @Setter PauseMenuEventListener eventListener;
    private final ArrayList<TextView> menuOptions;

    public PauseMenu(@NonNull Context context) {
        super(context);

        int optionHeight = HEIGHT / MENU_OPTIONS.length;

        menuOptions = new ArrayList<>();
        int position = 0;
        for (MenuOption menuOption : MENU_OPTIONS) {
            TextView textView = new TextView(context);
            textView.setText(menuOption.getName());
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(FONT_SIZE);

            textView.setY(position * optionHeight);
            textView.setX(0);

            textView.setClickable(true);
            textView.setOnClickListener(v -> {
                if (PauseMenu.this.eventListener != null) {
                    menuOption.action.accept(PauseMenu.this.eventListener);
                }
            });

            addView(textView);
            menuOptions.add(textView);

            textView.getLayoutParams().width = WIDTH;
            textView.getLayoutParams().height = optionHeight;

            ++position;
        }

        updateColors();
    }

    public void updateColors() {
        setBackgroundColor(WordDropper.colorTheme.backgroundLight);
        for (TextView textView : menuOptions) {
            textView.setTextColor(WordDropper.colorTheme.text);
        }
    }
}
