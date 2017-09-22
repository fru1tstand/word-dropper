package me.fru1t.worddropper.ui.mainmenu;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.google.common.base.Strings;

import java.util.Random;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.settings.ColorTheme;
import me.fru1t.worddropper.settings.colortheme.ColorThemeEventHandler;

/**
 * An animated background for the Main Menu.
 */
public class MainMenuBackground extends View implements ColorThemeEventHandler {
    private class FallingLetter {
        private static final float MIN_VELOCITY = 1;
        private static final float MAX_VELOCITY = 5;

        private static final int LETTER_HEIGHT = 40;

        private float yVelocity;
        private float y;
        private float x;
        String s;

        void init() {
            randomize();
            y = random.nextInt(getHeight());
        }

        void randomize() {
            s = ((char) ('A' + random.nextInt('Z' - 'A'))) + "";
            yVelocity = MIN_VELOCITY + (random.nextFloat() * (MAX_VELOCITY - MIN_VELOCITY));
            x = random.nextInt(getWidth());
            y = -1 * LETTER_HEIGHT;
        }

        void draw(Canvas canvas) {
            if (Strings.isNullOrEmpty(s)) {
                return;
            }

            canvas.drawText(s, x, y, textPaint);

            // Update
            y += yVelocity;

            // Reset if necessary
            if (y > getHeight() + LETTER_HEIGHT) {
                randomize();
            }
        }
    }

    private static final int FALLING_LETTERS_COUNT = 20;
    private static final int BLUR_RADIUS = 13;

    private final WordDropperApplication app;
    private final FallingLetter[] fallingLetters;
    private final Random random;
    private final Paint textPaint;

    public MainMenuBackground(@NonNull Context context) {
        this(context, null);
    }

    public MainMenuBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainMenuBackground(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        app = (WordDropperApplication) context.getApplicationContext();
        random = new Random();
        textPaint = new Paint();
        // TODO: Why does this leak into Tile.java?????????
//        textPaint.setMaskFilter(new BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.NORMAL));
        textPaint.setTextSize(getResources().getDimension(R.dimen.gameScreen_tileTextSize));

        fallingLetters = new FallingLetter[FALLING_LETTERS_COUNT];
        for (int i = 0; i < fallingLetters.length; i++) {
            fallingLetters[i] = new FallingLetter();
        }

        post(() -> {
            for (FallingLetter l : fallingLetters) {
                l.init();
            }
            postInvalidate();
        });
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = fallingLetters.length - 1; i >= 0; --i) {
            fallingLetters[i].draw(canvas);
        }
        invalidate();
    }

    @Override
    public void onColorThemeChange(ColorTheme colorTheme) {
        textPaint.setColor(colorTheme.backgroundLight);
    }
}
