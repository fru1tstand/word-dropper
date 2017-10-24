package me.fru1t.worddropper.ui.splashscreen

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.google.common.base.Strings
import me.fru1t.android.slick.Slik
import me.fru1t.android.slick.annotations.Inject

import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.settings.Dictionary
import me.fru1t.worddropper.ui.mainmenu.MainMenuActivity

/**
 * An intermediate screen shown to the user while the application is loading. This screen should
 * never be opened by any other activity and serves as the entry point to the application ensuring
 * any pre-loaded data is available for the rest of the services.
 */
class SplashScreenActivity : AppCompatActivity() {
    @Inject private lateinit var dictionary: Dictionary
    @Inject private lateinit var databaseUtils: DatabaseUtils

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Slik.get(WordDropperApplication::class).inject(this)

        root = findViewById(R.id.splashScreenRoot) as LinearLayout

        addTextView("Loading WordDropperApplication...")

        // Load dictionary
        val t = addTextView("")
        dictionary.onLoadDefer.progress({ t.text = it.toString() })
        dictionary.onLoadDefer.done {
            Toast.makeText(this, "done loading", Toast.LENGTH_SHORT).show()
        }

        // Load database
        databaseUtils.writableDatabase

        goToMainMenu(1000)
    }

    private fun addTextView(string: String?): TextView {
        val result = TextView(this)
        result.setBackgroundColor(Color.TRANSPARENT)
        result.setTextColor(Color.WHITE)
        result.textSize = 10f
        if (!Strings.isNullOrEmpty(string)) {
            result.text = string
        }
        root.addView(result)
        return result
    }

    private fun goToMainMenu(delayMs: Long) {
        addTextView("Moving to main menu in " + delayMs + "ms.")
        android.os.Handler().postDelayed({
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }, delayMs)
    }
}
