package me.fru1t.worddropper.ui.mainmenu

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import me.fru1t.android.annotations.VisibleForXML
import me.fru1t.worddropper.R
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.settings.Difficulty
import me.fru1t.worddropper.ui.game.GameActivity
import me.fru1t.worddropper.ui.settings.SettingsActivity
import me.fru1t.worddropper.ui.statsgameselect.StatsGameSelectActivity
import me.fru1t.worddropper.ui.widget.ColoredFrameLayout
import me.fru1t.worddropper.ui.widget.GameData
import me.fru1t.worddropper.ui.widget.GameListView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This is the base screen "main activity" of the game, when all other activities are disposed of.
 * This screen has links to all other parts of the game (the actual game, high scores, settings,
 * etc). This activity is never disposed of internally, unless acted upon by the OS itself.
 */
class MainMenuActivity : AppCompatActivity() {
    private val cachedMenus = SparseArray<LinearLayout>()
    private lateinit var root: ColoredFrameLayout
    private lateinit var resumeGameList: GameListView
    private lateinit var resumeGameButton: TextView

    private var activeMenu: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Resume Game Logic
        resumeGameButton = findViewById(R.id.mainMenuScreenResumeButton) as TextView
        resumeGameList = findViewById(R.id.mainMenuScreenResumeGameList) as GameListView
        resumeGameList.titleFunction = { (_, difficulty, unixStart) ->
            (difficulty.toUpperCase() + " - "
                    + RESUME_GAME_DATE_FORMAT.format(Date(unixStart * 1000)))
        }
        resumeGameList.descriptionFunction = { (_, _, _, _, score, words) ->
            (words.toString() + " words - " + score + " points")
        }
        resumeGameList.setOnItemClickListener { parent, _, position, _ ->
            val gameIntent = Intent(this, GameActivity::class.java)
            gameIntent.putExtra(
                    GameActivity.EXTRA_GAME_ID,
                    (parent.getItemAtPosition(position) as GameData).gameId)
            startActivity(gameIntent)
        }

        root = findViewById(R.id.mainMenuScreenRoot) as ColoredFrameLayout
        root.post { resumeGameList.maxHeight = root.height / 2 }
    }

    override fun onResume() {
        super.onResume()
        openMenu(R.id.mainMenuScreenRootMenu)

        // Do we have any games to resume?
        if (resumeGameList.populate(
                arrayOf(Game.COLUMN_STATUS),
                arrayOf(Game.STATUS_IN_PROGRESS.toString() + ""))) {
            resumeGameButton.visibility = View.VISIBLE
        } else {
            resumeGameButton.visibility = View.GONE
        }
    }

    private fun animateOpenMenu(@IdRes menuResourceId: Int) {
        if (cachedMenus.get(menuResourceId) == null) {
            cachedMenus.put(menuResourceId, findViewById(menuResourceId) as LinearLayout)
        }

        if (activeMenu == null) {
            openMenu(menuResourceId)
            return
        }

        val width = activeMenu!!.width

        // Set up new menu
        val newMenu = cachedMenus.get(menuResourceId)
        val newMenuParams = newMenu.layoutParams as LayoutParams
        newMenuParams.leftMargin = -1 * width
        newMenuParams.rightMargin = width
        newMenu.layoutParams = newMenuParams
        val newMenuAnimator = ValueAnimator.ofInt(width, 0)
        newMenuAnimator.duration = (resources.getInteger(R.integer.animation_durationResponsive) / 2).toLong()
        newMenuAnimator.interpolator = DecelerateInterpolator()
        newMenuAnimator.addUpdateListener { animation ->
            newMenuParams.leftMargin = -1 * animation.animatedValue as Int
            newMenuParams.rightMargin = animation.animatedValue as Int
            newMenu.layoutParams = newMenuParams
        }

        // Set up old menu
        val oldMenuParams = activeMenu!!.layoutParams as LayoutParams
        val oldMenuAnimator = ValueAnimator.ofInt(0, width)
        oldMenuAnimator.duration = (resources.getInteger(R.integer.animation_durationResponsive) / 2).toLong()
        oldMenuAnimator.interpolator = AccelerateInterpolator()
        oldMenuAnimator.addUpdateListener { animation ->
            oldMenuParams.leftMargin = -1 * animation.animatedValue as Int
            oldMenuParams.rightMargin = animation.animatedValue as Int
            activeMenu!!.layoutParams = oldMenuParams
        }
        oldMenuAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                activeMenu!!.visibility = View.GONE
                activeMenu = newMenu
                newMenu.visibility = View.VISIBLE
                newMenuAnimator.start()
            }
        })

        oldMenuAnimator.start()
    }

    private fun openMenu(@IdRes menuResourceId: Int) {
        if (cachedMenus.get(menuResourceId) == null) {
            cachedMenus.put(menuResourceId, findViewById(menuResourceId) as LinearLayout)
        }

        if (activeMenu != null) {
            activeMenu!!.visibility = View.GONE
        }

        activeMenu = cachedMenus.get(menuResourceId)
        val layout = activeMenu!!.layoutParams as LayoutParams
        layout.leftMargin = 0
        layout.rightMargin = 0
        activeMenu!!.layoutParams = layout
        activeMenu!!.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (activeMenu == null || activeMenu!!.id == R.id.mainMenuScreenRootMenu) {
            super.onBackPressed()
            return
        }

        animateOpenMenu(R.id.mainMenuScreenRootMenu)
    }

    private fun play(difficulty: Difficulty) {
        val gameScreenIntent = Intent(this, GameActivity::class.java)
        gameScreenIntent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty.name)
        gameScreenIntent.putExtra(GameActivity.EXTRA_GAME_ID, GameActivity.NEW_GAME)
        startActivity(gameScreenIntent)
    }

    // Root Menu
    @VisibleForXML
    fun onResumeClick(view: View) {
        animateOpenMenu(R.id.mainMenuScreenResumeMenu)
    }

    @VisibleForXML
    fun onPlayClick(view: View) {
        animateOpenMenu(R.id.mainMenuScreenPlayMenu)
    }

    @VisibleForXML
    fun onStatsClick(view: View) {
        animateOpenMenu(R.id.mainMenuScreenStatsMenu)
    }

    @VisibleForXML
    fun onSettingsClick(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    // Play Menu
    @VisibleForXML
    fun onPlayEasyClick(v: View) {
        play(Difficulty.EASY)
    }

    @VisibleForXML
    fun onPlayMediumClick(v: View) {
        play(Difficulty.MEDIUM)
    }

    @VisibleForXML
    fun onPlayHardClick(v: View) {
        play(Difficulty.HARD)
    }

    @VisibleForXML
    fun onPlayExpertClick(v: View) {
        play(Difficulty.EXPERT)
    }

    @VisibleForXML
    fun onPlayZenClick(v: View) {
        play(Difficulty.ZEN)
    }

    // Stats
    @VisibleForXML
    fun onStatsProfileClick(v: View) {
    }

    @VisibleForXML
    fun onStatsSpecialClick(v: View) {
    }

    @VisibleForXML
    fun onStatsGamesClick(v: View) {
        startActivity(Intent(this, StatsGameSelectActivity::class.java))
    }

    companion object {
        private val RESUME_GAME_DATE_FORMAT = SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US)
    }
}
