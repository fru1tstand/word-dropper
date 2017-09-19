package me.fru1t.worddropper.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.settings.Difficulty;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainMenuScreen extends AppCompatActivity {
    private @Nullable LinearLayout activeMenu;
    private final SparseArray<LinearLayout> cachedMenus;

    public MainMenuScreen() {
        cachedMenus = new SparseArray<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_screen);

        openMenu(R.id.mainMenuScreenRootMenu);
    }


    private void animateOpenMenu(@IdRes int menuResourceId) {
        if (cachedMenus.get(menuResourceId) == null) {
            cachedMenus.put(menuResourceId, (LinearLayout) findViewById(menuResourceId));
        }

        if (activeMenu == null) {
            openMenu(menuResourceId);
            return;
        }

        int width = activeMenu.getWidth();

        // Set up new menu
        LinearLayout newMenu = cachedMenus.get(menuResourceId);
        LayoutParams newMenuParams = (LayoutParams) newMenu.getLayoutParams();
        newMenuParams.leftMargin = -1 * width;
        newMenuParams.rightMargin = width;
        newMenu.setLayoutParams(newMenuParams);
        ValueAnimator newMenuAnimator = ValueAnimator.ofInt(width, 0);
        newMenuAnimator.setDuration(
                getResources().getInteger(R.integer.animation_durationResponsive) / 2);
        newMenuAnimator.setInterpolator(new DecelerateInterpolator());
        newMenuAnimator.addUpdateListener(animation -> {
            newMenuParams.leftMargin = -1 * (int) animation.getAnimatedValue();
            newMenuParams.rightMargin = (int) animation.getAnimatedValue();
            newMenu.setLayoutParams(newMenuParams);
        });

        // Set up old menu
        LayoutParams oldMenuParams = (LayoutParams) activeMenu.getLayoutParams();
        ValueAnimator oldMenuAnimator = ValueAnimator.ofInt(0, width);
        oldMenuAnimator.setDuration(
                getResources().getInteger(R.integer.animation_durationResponsive) / 2);
        oldMenuAnimator.setInterpolator(new AccelerateInterpolator());
        oldMenuAnimator.addUpdateListener(animation -> {
            oldMenuParams.leftMargin = -1 * (int) animation.getAnimatedValue();
            oldMenuParams.rightMargin = (int) animation.getAnimatedValue();
            activeMenu.setLayoutParams(oldMenuParams);
        });
        oldMenuAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                activeMenu.setVisibility(View.GONE);
                activeMenu = newMenu;
                newMenu.setVisibility(View.VISIBLE);
                newMenuAnimator.start();
            }
        });

        oldMenuAnimator.start();
    }

    private void openMenu(@IdRes int menuResourceId) {
        if (cachedMenus.get(menuResourceId) == null) {
            cachedMenus.put(menuResourceId, (LinearLayout) findViewById(menuResourceId));
        }

        if (activeMenu != null) {
            activeMenu.setVisibility(View.GONE);
        }

        activeMenu = cachedMenus.get(menuResourceId);
        activeMenu.setVisibility(View.VISIBLE);
    }

    private void play(Difficulty difficulty) {
        Intent gameScreenIntent = new Intent(this, GameScreen.class);
        gameScreenIntent.putExtra(GameScreen.EXTRA_DIFFICULTY, difficulty.name());
        startActivity(gameScreenIntent);
    }

    // Shared
    @VisibleForXML
    public void openRootMenu(View v) {
        animateOpenMenu(R.id.mainMenuScreenRootMenu);
    }

    // Root Menu
    @VisibleForXML
    public void onPlayClick(View view) {
        animateOpenMenu(R.id.mainMenuScreenPlayMenu);
    }
    @VisibleForXML
    public void onStatsClick(View view) {
        animateOpenMenu(R.id.mainMenuScreenStatsMenu);
    }
    @VisibleForXML
    public void onSettingsClick(View view) {
        // TODO: Go to settings activity
    }

    // Play Menu
    @VisibleForXML
    public void onPlayEasyClick(View v) {
        play(Difficulty.EASY);
    }
    @VisibleForXML
    public void onPlayMediumClick(View v) {
        play(Difficulty.MEDIUM);
    }
    @VisibleForXML
    public void onPlayHardClick(View v) {
        play(Difficulty.HARD);
    }
    @VisibleForXML
    public void onPlayExpertClick(View v) {
        play(Difficulty.EXPERT);
    }
    @VisibleForXML
    public void onPlayZenClick(View v) {
        play(Difficulty.ZEN);
    }

    // Stats
    @VisibleForXML
    public void onStatsProfileClick(View v) {
        // TODO: Go to global profile activity
    }
    @VisibleForXML
    public void onStatsSpecialClick(View v) {
        // TODO: Go to special selectors activity
    }
    @VisibleForXML
    public void onStatsGamesClick(View v) {
        // TODO: Go to game select activity
    }
}
