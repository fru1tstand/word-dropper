package me.fru1t.worddropper.ui.game

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import me.fru1t.android.annotations.VisibleForXML
import me.fru1t.android.content.IntentFactory
import me.fru1t.android.content.res.ResourceManager
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Named
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.database.tables.GameWord
import me.fru1t.worddropper.settings.ColorThemeManager
import me.fru1t.worddropper.settings.Dictionary
import me.fru1t.worddropper.settings.Difficulty
import me.fru1t.worddropper.ui.gamestats.GameStatsActivity
import me.fru1t.worddropper.ui.settings.SettingsActivity
import me.fru1t.worddropper.ui.widget.MenuLayout
import java.util.LinkedList

/**
 * The interactive game that shows the tile board, level progress, etc. This activity can be started
 * anywhere so long as it's passed a gameId (use [.NEW_GAME] for a new game alongside a
 * difficulty). This activity will always dispose of itself after completion (which could be due to
 * the game finishing, or user specification).
 */
class GameActivity : AppCompatActivity() {
    // Dependencies
    private @Inject lateinit var colorThemeManager: ColorThemeManager
    private @Inject lateinit var databaseUtils: DatabaseUtils
    private @Inject lateinit var dictionary: Dictionary
    private @Inject lateinit var intentFactory: IntentFactory
    private @Inject lateinit var res: ResourceManager
    private @Inject @Named(WordDropperApplication.IS_DEBUGGING) var isDebugging: Boolean = false

    // Game-related values
    private lateinit var difficulty: Difficulty
    private var movesEarned = 0
    private var movesUsed = 0
    private var scramblesEarned = 0
    private var scramblesUsed = 0

    // Elements
    private lateinit var tileBoard: TileBoard
    private lateinit var progressBar: WrappingProgressBar
    private lateinit var pauseMenu: MenuLayout
    private lateinit var level: TextView
    private lateinit var scrambles: TextView
    private lateinit var movesLeft: TextView
    private lateinit var activeWord: TextView

    // Chart
    private lateinit var wordHistoryChart: BarChart
    private val wordHistoryDataList = LinkedList<BarEntry>()
    private val wordHistoryDataSet = BarDataSet(wordHistoryDataList, "")

    private var gameId: Long = 0

    init {
        Slik.get(WordDropperApplication::class).inject(this)
        // Data backend setup for chart
        wordHistoryDataSet.setValueFormatter { value, _, _, _ ->
            if (value == 0f) "" else value.toInt().toString() + ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Fetch elements we need to poke around with
        tileBoard = findViewById(R.id.gameScreenTileBoard) as TileBoard
        progressBar = findViewById(R.id.gameScreenProgress) as WrappingProgressBar
        pauseMenu = findViewById(R.id.gameScreenPauseMenu) as MenuLayout
        level = findViewById(R.id.gameScreenHudStatLevel) as TextView
        scrambles = findViewById(R.id.gameScreenHudStatScrambles) as TextView
        movesLeft = findViewById(R.id.gameScreenHudStatMovesLeft) as TextView
        activeWord = findViewById(R.id.gameScreenHudActiveWord) as TextView
        wordHistoryChart = findViewById(R.id.gameScreenHudChart) as BarChart

        // Set up chart
        wordHistoryChart.setDrawBarShadow(false)
        wordHistoryChart.setDrawValueAboveBar(true)
        wordHistoryChart.description.isEnabled = false
        wordHistoryChart.setPinchZoom(false)
        wordHistoryChart.setDrawGridBackground(false)
        wordHistoryChart.setBackgroundColor(Color.TRANSPARENT)
        wordHistoryChart.setTouchEnabled(false)
        wordHistoryChart.setViewPortOffsets(0f, 0f, 0f, 0f)

        val data = BarData(wordHistoryDataSet)
        wordHistoryChart.data = data
        wordHistoryChart.legend.isEnabled = false
        wordHistoryChart.axisRight.isEnabled = false

        val xAxis = wordHistoryChart.xAxis
        xAxis.isEnabled = false
        xAxis.setAvoidFirstLastClipping(true)

        val yAxis = wordHistoryChart.axisLeft
        yAxis.isEnabled = false
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(false)

        // Set up progress bar
        progressBar.setMaximumFunction = {
            if (it < 1) 80 else (80 * Math.pow(1.10409, it.toDouble())).toInt()
        }

        // Set up tile board listeners
        tileBoard.eventListener = { changeEventType, word ->
            when (changeEventType) {
                ChangeEventType.CHANGE -> setActiveWord(word)

                ChangeEventType.SUCCESSFUL_SUBMIT -> {
                    val wordValue = dictionary.getWordValue(word)
                    progressBar.animateAddProgress(wordValue)
                    ++movesUsed

                    // Use up a move
                    if (difficulty.isWordAverageEnabled()) {
                        if (movesUsed >= movesEarned) {
                            tileBoard.enableTouching = false
                        }
                        movesLeft.text = getString(R.string.integer, movesEarned - movesUsed)
                    }

                    // Update hud
                    setActiveWord("")
                    addWordToGraph(word!!, wordValue)

                    // Add to database
                    databaseUtils.addGameMove(
                            gameId,
                            word,
                            wordValue,
                            progressBar.grandTotal,
                            tileBoard.getBoardState())
                }

                ChangeEventType.FAILED_SUBMIT -> Unit // Do nothing.
            }
        }

        // On level up
        progressBar.onWrapEventListener = { wraps, newMax ->
            val currentLevel = wraps + 1
            level.text = getString(R.string.integer, currentLevel)

            if (difficulty.isScramblingAllowed()
                    && !difficulty.isScramblingUnlimited()
                    && currentLevel % difficulty.levelsBeforeScramblePowerUp == 0) {
                ++scramblesEarned
                scrambles.text = getString(R.string.integer, scramblesEarned - scramblesUsed)
            }

            if (difficulty.isWordAverageEnabled()) {
                val movesToAdd = Math.round(1.0 * newMax / difficulty.wordPointAverage).toInt()

                val va = ValueAnimator.ofInt(
                        movesEarned - movesUsed, movesEarned + movesToAdd - movesUsed)
                va.interpolator = AccelerateDecelerateInterpolator()
                va.duration = resources.getInteger(R.integer.animation_durationEffect).toLong()
                va.addUpdateListener { animation ->
                    movesLeft.text = getString(R.string.integer, animation.animatedValue as Int)
                }
                va.start()

                movesEarned += movesToAdd

                // Edge case where the user ran out of moves upon levelling up.
                if (!tileBoard.enableTouching) {
                    tileBoard.enableTouching = true
                }
            }

            // Update database
            databaseUtils.updateGame(
                    gameId,
                    { update ->
                        update.put(Game.COLUMN_LEVEL, currentLevel)
                        update.put(Game.COLUMN_MOVES_EARNED, movesEarned)
                        update.put(Game.COLUMN_SCRAMBLES_EARNED, scramblesEarned)
                        update.put(Game.COLUMN_BOARD_STATE, tileBoard.getBoardState())
                    })
        }
        progressBar.onAnimateAddEndEventListener = lambda@ {
            if (movesUsed < movesEarned || !difficulty.isWordAverageEnabled()) {
                return@lambda
            }

            endGame()
        }

        // Pause menu comes last so it's onWrapEventListener top
        pauseMenu.onHideListener = { pauseMenu.visibility = View.GONE }
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuSaveAndQuit, false, this::finish)
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuSettings, true, {
            startActivity(intentFactory.create<SettingsActivity>())
        })
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuRestartOption, true, {
            databaseUtils.endGame(gameId) // End game in db
            gameId = NEW_GAME // Prepare for new game
            startGame() // Set up and start new game
        })
        pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuEndGameOption, false, this::endGame)
        if (isDebugging) {
            pauseMenu.addMenuOption(R.string.gameScreen_pauseMenuDebugSubmitWords, true, {
                for (i in 0..39) {
                    android.os.Handler().postDelayed(
                            {
                                runOnUiThread {
                                    tileBoard.eventListener!!(
                                            ChangeEventType.SUCCESSFUL_SUBMIT,
                                            dictionary.getRandomWord(difficulty.wordPointAverage))
                                }
                            },
                            (i * 100).toLong())
                }
            })

        }

        // Colorize
        colorThemeManager.addChangeListener(this::onColorThemeChange)

        // Set GameID if we're resuming a game.
        gameId = intent.getLongExtra(EXTRA_GAME_ID, NEW_GAME)

        // Start the game
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        colorThemeManager.removeChangeListener(this::onColorThemeChange)
    }

    private fun onColorThemeChange() {
        wordHistoryChart.data.setValueTextColor(colorThemeManager.currentColorTheme.textBlend)
        wordHistoryDataSet.color = colorThemeManager.currentColorTheme.textBlend

        setActiveWord(activeWord.text.toString())
        wordHistoryChart.invalidate()
    }

    /** Starts a new game if the gameId isn't set, or resumes a game. */
    private fun startGame() {
        // Shared setup
        clearGraph()

        // Set up board
        if (gameId == NEW_GAME) {
            // Set initial values
            difficulty = Difficulty.valueOf(intent.getStringExtra(EXTRA_DIFFICULTY))
            scramblesUsed = 0
            scramblesEarned = 0
            movesEarned = Math.round(1.0 * progressBar.max / difficulty.wordPointAverage).toInt()
            movesUsed = 0

            // Reset
            progressBar.reset()
            tileBoard.scramble()

            // Create new game
            gameId = databaseUtils.startGame(
                    difficulty,
                    tileBoard.getBoardState(),
                    movesEarned,
                    scramblesEarned)
        } else {
            // Load game data
            val row = databaseUtils.getRowFromId(
                    Game.TABLE_NAME,
                    gameId,
                    arrayOf(
                            Game.COLUMN_BOARD_STATE, Game.COLUMN_STATUS,
                            Game.COLUMN_SCRAMBLES_EARNED, Game.COLUMN_SCRAMBLES_USED,
                            Game.COLUMN_DIFFICULTY, Game.COLUMN_MOVES_EARNED))
            if (row == null) {
                Toast.makeText(this, R.string.app_gameNotFoundError, Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Extra data from the game word table.
            val extraGameData = IntArray(2) // [0] = words; [1] = score
            databaseUtils.forEachResult(
                    "SELECT"
                        + " COUNT(*) AS words,"                                 // 0
                        + " SUM(" + GameWord.COLUMN_POINT_VALUE + ") AS score"  // 1
                    + " FROM " + GameWord.TABLE_NAME
                    + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?",
                    arrayOf(gameId.toString() + ""),
                    { cursor ->
                        extraGameData[0] = cursor.getInt(0)
                        extraGameData[1] = cursor.getInt(1)
                    })

            // Chart data
            databaseUtils.forEachResult(
                    "SELECT * FROM ( SELECT "
                        + BaseColumns._ID + ", "           // 0
                        + GameWord.COLUMN_WORD + ", "   // 1
                        + GameWord.COLUMN_POINT_VALUE   // 2
                    + " FROM " + GameWord.TABLE_NAME
                    + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?"
                    + " ORDER BY " + BaseColumns._ID + " DESC" // Order by newest first
                    + " LIMIT " + CHART_ELEMENTS // Grab last n words
                    + ") ORDER BY " + BaseColumns._ID + " ASC", // But digest by oldest first
                    arrayOf(gameId.toString() + ""),
                    { cursor -> addWordToGraph(cursor.getString(1), cursor.getInt(2)) })

            // Set values
            difficulty =
                    Difficulty.valueOf(row.getString(Game.COLUMN_DIFFICULTY, Difficulty.ZEN.name))
            scramblesUsed = row.getInt(Game.COLUMN_SCRAMBLES_USED, 0)
            scramblesEarned = row.getInt(Game.COLUMN_SCRAMBLES_EARNED, 0)
            movesEarned = row.getInt(Game.COLUMN_MOVES_EARNED, 0)
            movesUsed = extraGameData[0]

            // Update modules
            progressBar.setTotal(extraGameData[1])
            tileBoard.setBoardState(row.getString(Game.COLUMN_BOARD_STATE, ""))
        }

        // Update hud
        if (!difficulty.isScramblingAllowed()) {
            scrambles.text = getString(R.string.integer, 0)
        } else if (difficulty.isScramblingUnlimited()) {
            scrambles.text = resources.getString(R.string.gameScreen_infiniteValue)
        } else {
            scrambles.text = getString(R.string.integer, scramblesEarned - scramblesUsed)
        }

        if (!difficulty.isWordAverageEnabled()) {
            movesLeft.text = resources.getString(R.string.gameScreen_infiniteValue)
        } else {
            movesLeft.text = getString(R.string.integer, movesEarned - movesUsed)
        }

        level.text = getString(R.string.integer, progressBar.wraps + 1)
    }

    private fun endGame() {
        // End game in database
        databaseUtils.endGame(gameId)

        // Open end game screen
        val endGameIntent = intentFactory.create<GameStatsActivity>()
        endGameIntent.putExtra(GameStatsActivity.EXTRA_GAME_ID, gameId)
        startActivity(endGameIntent)
        finish()
    }

    private fun addWordToGraph(word: String, value: Int) {
        val data = wordHistoryChart.data
        data.addEntry(BarEntry(wordHistoryDataList.peekLast().x + 1, value.toFloat(), word), 0)
        data.removeEntry(wordHistoryDataList.peekFirst(), 0)
        data.notifyDataChanged()
        wordHistoryChart.notifyDataSetChanged()
        wordHistoryChart.setVisibleXRangeMaximum(CHART_ELEMENTS.toFloat())
        wordHistoryChart.moveViewToX(data.entryCount.toFloat())
    }

    private fun clearGraph() {
        wordHistoryDataList.clear()
        for (i in 0 until CHART_ELEMENTS) {
            wordHistoryDataList.add(BarEntry(i.toFloat(), 0f, ""))
        }
        wordHistoryChart.data.notifyDataChanged()
        wordHistoryChart.notifyDataSetChanged()
        wordHistoryChart.invalidate()
    }

    private fun setActiveWord(string: String?) {
        if (string.isNullOrEmpty()) {
            activeWord.text = ""
            activeWord.setTextColor(colorThemeManager.currentColorTheme.text)
            activeWord.setPadding(0, activeWord.paddingTop, 0, activeWord.paddingBottom)
            return
        }

        var result = string!!
        if (dictionary.isWord(result)) {
            result += " (" + dictionary.getWordValue(result) + ")"
            activeWord.setTextColor(colorThemeManager.currentColorTheme.primary)
        } else {
            activeWord.setTextColor(colorThemeManager.currentColorTheme.text)
        }

        result = result.substring(0, 1).toUpperCase() + result.substring(1)
        activeWord.text = result
        activeWord.setPadding(
                res.d(R.dimen.gameScreen_hudCurrentWordHorizontalPadding).toInt(),
                activeWord.paddingTop,
                res.d(R.dimen.gameScreen_hudCurrentWordHorizontalPadding).toInt(),
                activeWord.paddingBottom)
    }

    /** Shows the pause menu if it's not already open. */
    @VisibleForXML
    fun onGraphicClick(v: View) {
        if (pauseMenu.isOpen) {
            return
        }

        pauseMenu.visibility = View.VISIBLE
        pauseMenu.show()
    }

    /** Tries to use a scramble if there's one available. */
    @VisibleForXML
    fun onScramblesClick(v: View) {
        if (!difficulty.isScramblingAllowed()) {
            return
        }

        if (difficulty.isScramblingUnlimited()) {
            tileBoard.scramble()
            scramblesUsed++
            databaseUtils.updateGame(gameId, { it.put(Game.COLUMN_SCRAMBLES_USED, scramblesUsed) })
            return
        }

        if (scramblesUsed >= scramblesEarned) {
            return
        }

        scramblesUsed++
        tileBoard.scramble()
        scrambles.text = getString(R.string.integer, scramblesEarned - scramblesUsed)
        databaseUtils.updateGame(gameId, { it.put(Game.COLUMN_SCRAMBLES_USED, scramblesUsed) })
    }

    @VisibleForXML
    fun onLevelClick(v: View) {
        println("On level click")
    }

    @VisibleForXML
    fun onMovesLeftClick(v: View) {
        println("moves left click")
    }

    companion object {
        val EXTRA_DIFFICULTY = "extra_difficulty"
        val EXTRA_GAME_ID = "extra_game_id"

        private val CHART_ELEMENTS = 30
        val NEW_GAME: Long = -1
    }
}
