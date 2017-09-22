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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.fru1t.android.annotations.VisibleForXML;
import me.fru1t.worddropper.R;
import me.fru1t.worddropper.database.tables.Game;
import me.fru1t.worddropper.settings.Difficulty;
import me.fru1t.worddropper.widget.GameListView;
import me.fru1t.worddropper.widget.base.ColoredFrameLayout;

/**
 * This is the base screen "main activity" of the game, when all other activities are disposed of.
 * This screen has links to all other parts of the game (the actual game, high scores, settings,
 * etc). This activity is never disposed of internally, unless acted upon by the OS itself.
 */
public class MainMenuScreen extends AppCompatActivity {
    private static final SimpleDateFormat RESUME_GAME_DATE_FORMAT =
            new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US);

    private ColoredFrameLayout root;
    private GameListView resumeGameList;
    private TextView resumeGameButton;

    private @Nullable LinearLayout activeMenu;
    private final SparseArray<LinearLayout> cachedMenus;

    public MainMenuScreen() {
        cachedMenus = new SparseArray<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_screen);

        // Resume Game Logic
        resumeGameButton = (TextView) findViewById(R.id.mainMenuScreenResumeButton);
        resumeGameList = (GameListView) findViewById(R.id.mainMenuScreenResumeGameList);
        resumeGameList.setTitleFunction(data -> data.difficulty.toUpperCase() + " - "
                + RESUME_GAME_DATE_FORMAT.format(new Date(data.unixStart * 1000)));
        resumeGameList.setDescriptionFunction(data -> data.words + " words - "
                + data.score + " points");
        resumeGameList.setOnItemClickListener((parent, view, position, id) -> {
            Intent gameIntent = new Intent(this, GameScreen.class);
            gameIntent.putExtra(GameScreen.EXTRA_GAME_ID,
                    ((GameListView.GameData) parent.getItemAtPosition(position)).gameId);
            startActivity(gameIntent);
        });

        root = (ColoredFrameLayout) findViewById(R.id.mainMenuScreenRoot);
        root.post(() -> resumeGameList.setMaxHeight(root.getHeight() / 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        openMenu(R.id.mainMenuScreenRootMenu);

        // Do we have any games to resume?
        if (resumeGameList.populate(
                new String[] { Game.COLUMN_STATUS },
                new String[] { Game.STATUS_IN_PROGRESS + "" })) {
            resumeGameButton.setVisibility(View.VISIBLE);
        } else {
            resumeGameButton.setVisibility(View.GONE);
        }
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
        LayoutParams layout = (LayoutParams) activeMenu.getLayoutParams();
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        activeMenu.setLayoutParams(layout);
        activeMenu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (activeMenu == null || activeMenu.getId() == R.id.mainMenuScreenRootMenu) {
            super.onBackPressed();
            return;
        }

        animateOpenMenu(R.id.mainMenuScreenRootMenu);
    }

    private void play(Difficulty difficulty) {
        Intent gameScreenIntent = new Intent(this, GameScreen.class);
        gameScreenIntent.putExtra(GameScreen.EXTRA_DIFFICULTY, difficulty.name());
        gameScreenIntent.putExtra(GameScreen.EXTRA_GAME_ID, GameScreen.NEW_GAME);
        startActivity(gameScreenIntent);
    }

    // Root Menu
    @VisibleForXML
    public void onResumeClick(View view) {
        animateOpenMenu(R.id.mainMenuScreenResumeMenu);
    }
    @VisibleForXML
    public void onPlayClick(View view) {
        animateOpenMenu(R.id.mainMenuScreenPlayMenu);
    }
    @VisibleForXML
    public void onStatsClick(View view) {
        animateOpenMenu(R.id.mainMenuScreenStatsMenu);
    }
    @VisibleForXML
    public void onSettingsClick(View view) { }

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
    public void onStatsProfileClick(View v) { }
    @VisibleForXML
    public void onStatsSpecialClick(View v) { }
    @VisibleForXML
    public void onStatsGamesClick(View v) {
        startActivity(new Intent(this, StatsGameSelectScreen.class));
    }
}
