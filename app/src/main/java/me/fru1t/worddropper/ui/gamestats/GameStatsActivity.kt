package me.fru1t.worddropper.ui.gamestats

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import me.fru1t.android.annotations.VisibleForXML
import me.fru1t.android.app.find
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.widget.ViewFactory
import me.fru1t.android.widget.ViewUtils
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.database.tables.Game
import me.fru1t.worddropper.database.tables.GameWord
import me.fru1t.worddropper.settings.ColorThemeManager
import me.fru1t.worddropper.settings.Difficulty
import me.fru1t.worddropper.ui.game.GameActivity
import me.fru1t.worddropper.ui.widget.ColoredTextView
import me.fru1t.worddropper.ui.widget.SummaryStatistic
import java.util.ArrayList

/** Data class containing an MPChart object and its corresponding TextView "button" */
private data class GraphAction(
        val button: TextView,
        var chart: Chart<*>? = null
)

/** Shows detailed information and graphics about a single game. */
class GameStatsActivity : AppCompatActivity() {
    private @Inject lateinit var viewFactory: ViewFactory
    private @Inject lateinit var databaseUtils: DatabaseUtils
    private @Inject lateinit var colorThemeManager: ColorThemeManager

    // Game pieces
    private var gameId: Long = 0
    private var difficulty: String? = null
    private val memoizedGraphs = SparseArray<GraphAction>()
    private val graphButtons = ArrayList<TextView>()

    /** Used to dynamically re-load the currently active graph (in the event of a recolor */
    private lateinit var activeGraphFunction: (View?) -> Unit

    // Elements
    private lateinit var graphWrapper: FrameLayout
    private lateinit var graphButtonsWrapper: LinearLayout

    init {
        Slik.get(WordDropperApplication::class).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_stats)
        gameId = intent.getLongExtra(EXTRA_GAME_ID, -1)

        graphWrapper = findViewById(R.id.graphWrapper) as FrameLayout
        graphButtonsWrapper = findViewById(R.id.graphButtonsWrapper) as LinearLayout

        // Fetch data
        val gameData = databaseUtils.getRowFromId(
                Game.TABLE_NAME,
                gameId,
                arrayOf(
                        Game.COLUMN_UNIX_START, Game.COLUMN_STATUS, Game.COLUMN_DIFFICULTY,
                        Game.COLUMN_BOARD_STATE, Game.COLUMN_MOVES_EARNED, Game.COLUMN_BOARD_STATE,
                        Game.COLUMN_MOVES_EARNED, Game.COLUMN_SCRAMBLES_USED,
                        Game.COLUMN_SCRAMBLES_EARNED, Game.COLUMN_LEVEL))
        if (gameData == null) {
            Toast.makeText(this, R.string.app_gameNotFoundError, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Yes. This can be done in the above query. But a) readability, and b) low overhead.
        val extraGameData = IntArray(2) // [0] = words; [1] = score
        databaseUtils.forEachResult(
                "SELECT"
                    + " COUNT(*) AS words,"                                 // 0
                    + " SUM(" + GameWord.COLUMN_POINT_VALUE + ") AS score"  // 1
                + " FROM " + GameWord.TABLE_NAME
                + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?",
                arrayOf(gameId.toString()),
                { cursor ->
                    extraGameData[0] = cursor.getInt(0)
                    extraGameData[1] = cursor.getInt(1)
                })

        // Handle difficulty
        difficulty = gameData.getString(Game.COLUMN_DIFFICULTY, Difficulty.ZEN.name)
        (findViewById(R.id.difficulty) as ColoredTextView).text = difficulty

        // Side by side level and score
        animateValue(gameData.getInt(Game.COLUMN_LEVEL, 0),
                findViewById(R.id.level) as TextView, 0)
        animateValue(extraGameData[1],
                findViewById(R.id.score) as TextView, 50)
        animateValue(gameData.getInt(Game.COLUMN_SCRAMBLES_EARNED, 0),
                findViewById(R.id.scramblesEarned) as TextView, 50)
        animateValue(gameData.getInt(Game.COLUMN_SCRAMBLES_USED, 0),
                findViewById(R.id.scramblesUsed) as TextView, 100)
        animateValue(extraGameData[0], findViewById(R.id.words) as TextView, 150)

        // The chart will load once the color theme has been set
        activeGraphFunction = this::loadWordLengthGraph

        // Word List
        val wordsList = findViewById(R.id.wordList) as TextView
        val gameMovesList = databaseUtils.getGameMoves(intent.getLongExtra(EXTRA_GAME_ID, -1))
        gameMovesList.forEach { s -> wordsList.append(s + ", ") }

        // Summary statistics
        databaseUtils.forResult(
                "SELECT"
                    + " IFNULL(ROUND(AVG(LENGTH("
                        + GameWord.COLUMN_WORD + ")), 1), 0) AS avg_word_length,"       // 0
                    + " IFNULL(ROUND(AVG("
                        + GameWord.COLUMN_POINT_VALUE + "), 1), 0) AS avg_word_value,"  // 1
                    + " IFNULL(SUM(LENGTH("
                        + GameWord.COLUMN_WORD + ")), 0) AS total_letters_used"         // 2
                + " FROM " + GameWord.TABLE_NAME
                + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?",
                arrayOf(gameId.toString()),
                {
                    find<SummaryStatistic>(R.id.avgWordLength).value.text = it.getString(0)
                    find<SummaryStatistic>(R.id.avgWordPoints).value.text = it.getString(1)
                    find<SummaryStatistic>(R.id.totalLettersUsed).value.text = it.getString(2)
                })

        // Color theme
        colorThemeManager.addChangeListener(this::onColorThemeChange)
    }


    override fun onDestroy() {
        super.onDestroy()
        colorThemeManager.removeChangeListener(this::onColorThemeChange)
    }

    private fun animateValue(value: Int, target: TextView, delay: Int) {
        val animator = ValueAnimator.ofInt(0, value)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = resources.getInteger(R.integer.animation_durationLag).toLong()
        animator.addUpdateListener { animation ->
            target.text = getString(R.string.integer, animation.animatedValue as Int)
        }

        if (delay > 0) {
            Handler().postDelayed({ animator.start() }, delay.toLong())
        } else {
            animator.start()
        }
    }

    private fun showGraph(action: GraphAction) {
        // Reset Graphs
        graphWrapper.removeAllViews()

        // Reset buttons
        if (graphButtons.size == 0) {
            graphButtons.addAll(
                    ViewUtils.getElementsByTagName<AppCompatTextView>(graphButtonsWrapper, false))
        }
        for (tv in graphButtons) {
            tv.setTextColor(colorThemeManager.currentColorTheme.text)
            tv.setBackgroundColor(colorThemeManager.currentColorTheme.backgroundLight)
        }

        // Activate button
        action.button.setTextColor(colorThemeManager.currentColorTheme.textOnPrimary)
        action.button.setBackgroundColor(colorThemeManager.currentColorTheme.primary)

        // Show graph
        if (action.chart != null) {
            graphWrapper.addView(action.chart)
            action.chart!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            action.chart!!.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            action.chart!!.animateX(resources.getInteger(R.integer.animation_durationLag))
        } else {
            val textView = viewFactory.create<TextView>()
            textView.setText(R.string.gameStats_graphNoData)
            textView.gravity = Gravity.CENTER
            textView.setTextColor(colorThemeManager.currentColorTheme.text)

            val view = viewFactory.create<View>()
            view.setBackgroundColor(colorThemeManager.currentColorTheme.backgroundLight)
            graphWrapper.addView(view)
            graphWrapper.addView(textView)
            view.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    @VisibleForXML
    fun loadPointDistributionGraph(v: View?) {
        activeGraphFunction = this::loadPointDistributionGraph
        if (memoizedGraphs.get(R.id.graphPointDistribution) != null) {
            showGraph(memoizedGraphs.get(R.id.graphPointDistribution))
            return
        }

        // Prepare
        val action =
                GraphAction(
                        findViewById(R.id.graphPointDistribution) as TextView)
        memoizedGraphs.put(R.id.graphPointDistribution, action)

        // Data backend
        val rawData = ArrayList<BarEntry>()
        val dataSet = BarDataSet(rawData, "")
        dataSet.color = colorThemeManager.currentColorTheme.textBlend
        dataSet.setDrawValues(false)

        // Get Data from db into chart backend
        val data = BarData(dataSet)
        databaseUtils.forEachResult(
                "SELECT"
                    + " COUNT(*) AS frequency,"
                    + " " + GameWord.COLUMN_POINT_VALUE
                + " FROM " + GameWord.TABLE_NAME
                + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?"
                + " GROUP BY " + GameWord.COLUMN_POINT_VALUE
                + " ORDER BY " + GameWord.COLUMN_POINT_VALUE + " ASC",
                arrayOf(gameId.toString()),
                {
                    data.addEntry(BarEntry(it.getInt(1).toFloat(), it.getInt(0).toFloat(), ""), 0)
                }
        )
        data.notifyDataChanged()

        // No data? No graph.
        if (rawData.size == 0) {
            action.chart = null
            showGraph(action)
            return
        }

        // Set up chart
        val chart = viewFactory.create<BarChart>()
        chart.data = data
        chart.setDrawBarShadow(false)
        chart.description.isEnabled = false
        chart.setPinchZoom(false)
        chart.setTouchEnabled(false)

        val x = chart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.granularity = 1f
        x.textColor = colorThemeManager.currentColorTheme.text
        x.gridColor = colorThemeManager.currentColorTheme.textBlend
        x.axisLineColor = colorThemeManager.currentColorTheme.textBlend
        x.axisMinimum = 0f

        val y = chart.axisLeft
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        y.textColor = colorThemeManager.currentColorTheme.text
        y.gridColor = colorThemeManager.currentColorTheme.textBlend
        y.axisLineColor = colorThemeManager.currentColorTheme.textBlend
        y.granularity = 1f
        y.axisMinimum = 0f

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false

        // Finally, show the graph
        action.chart = chart
        showGraph(action)
    }

    @VisibleForXML
    fun loadWordLengthGraph(v: View?) {
        activeGraphFunction = this::loadWordLengthGraph
        if (memoizedGraphs.get(R.id.graphWordLengths) != null) {
            showGraph(memoizedGraphs.get(R.id.graphWordLengths))
            return
        }

        // Prepare
        val action = GraphAction(findViewById(R.id.graphWordLengths) as TextView)
        memoizedGraphs.put(R.id.graphWordLengths, action)

        // Data backend
        val rawData = ArrayList<PieEntry>()
        val dataSet = PieDataSet(rawData, "")
        dataSet.color = colorThemeManager.currentColorTheme.backgroundLight
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLineColor = colorThemeManager.currentColorTheme.primary
        dataSet.sliceSpace = 2f
        val data = PieData(dataSet)
        data.setValueTextColor(colorThemeManager.currentColorTheme.text)
        data.setValueTextSize(10f)

        // Get data from db
        var total = 0
        databaseUtils.forEachResult(
                "SELECT"
                    + " COUNT(*) AS quantity,"
                    + " LENGTH(" + GameWord.COLUMN_WORD + ") AS word_length"
                + " FROM " + GameWord.TABLE_NAME
                + " WHERE " + GameWord.COLUMN_GAME_ID + " = ?"
                + " GROUP BY word_length"
                + " ORDER BY quantity ASC",
                arrayOf(gameId.toString()),
                {
                    total += it.getInt(0)
                    dataSet.addEntry(
                            PieEntry(
                                    it.getInt(0).toFloat(),
                                    getString(
                                            R.string.gameStats_graphWordLengthsLabel,
                                            it.getInt(1))))
                }
        )
        data.notifyDataChanged()

        // No data, no graph
        if (rawData.size == 0) {
            action.chart = null
            showGraph(action)
            return
        }

        // Set up chart
        val chart = viewFactory.create<PieChart>()
        chart.data = data
        chart.setUsePercentValues(false)
        chart.description.isEnabled = false
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(colorThemeManager.currentColorTheme.background)
        chart.setTransparentCircleColor(Color.TRANSPARENT)
        chart.setTransparentCircleAlpha(255)
        chart.transparentCircleRadius = 54f
        chart.holeRadius = 54f
        chart.setDrawCenterText(false)
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = false

        // Labels outside the chart
        chart.setEntryLabelColor(colorThemeManager.currentColorTheme.text)

        chart.legend.isEnabled = false

        // One last thing
        val finalTotal = total
        data.setValueFormatter { value, _, _, _ ->
            Math.round(value).toString() +
                    " (" + Math.round(1000.0 * value / finalTotal) / 10.0 + "%)"
        }

        // Finally show graph.
        action.chart = chart
        showGraph(action)
    }

    @VisibleForXML
    fun onActionPlayAgainClick(v: View) {
        val gameScreenIntent = Intent(this, GameActivity::class.java)
        gameScreenIntent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty)
        startActivity(gameScreenIntent)
        finish()
    }

    @VisibleForXML
    fun onActionMainMenuClick(v: View) {
        finish()
    }

    private fun onColorThemeChange() {
        // Re-load chart to apply colors
        activeGraphFunction(null)
    }

    companion object {

        val EXTRA_GAME_ID = "extra_game_id" // Long
    }
}
