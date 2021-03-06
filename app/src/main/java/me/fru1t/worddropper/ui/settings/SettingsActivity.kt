package me.fru1t.worddropper.ui.settings

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.layout_settings_option.view.*
import me.fru1t.android.slik.Slik
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.worddropper.R
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.settings.ColorTheme

class SettingsActivity : AppCompatActivity() {
    private @Inject lateinit var colorThemeListElementFactory: ColorThemeListElementFactory
    private @Inject lateinit var databaseUtils: DatabaseUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Slik.get(WordDropperApplication::class).inject(this)

        // Add color themes
        val colorThemeList = findViewById(R.id.colorThemeList) as LinearLayout
        for (colorTheme in ColorTheme.values()) {
            colorThemeList.addView(colorThemeListElementFactory.create(colorTheme))
        }

        // Delete Data
        updateSettingsDeleteOption()
        settingsDeleteDataButton.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(R.string.settings_deleteDataDialogTitle)
                    .setMessage(R.string.settings_deleteDataDialogMessage)
                    .setPositiveButton(R.string.settings_deleteDataDialogButtonDelete,
                            { _, _ ->
                                databaseUtils.createTables()
                                Toast.makeText(
                                        this,
                                        R.string.settings_deleteDataCompleteToastMessage,
                                        Toast.LENGTH_LONG).show()
                                updateSettingsDeleteOption()
                            })
                    .setNeutralButton(R.string.settings_deleteDataDialogButtonCancel,
                            { dialog, _ -> dialog.dismiss() })
                    .create()
                    .show()
        }
    }

    private fun updateSettingsDeleteOption() {
        settingsDeleteDataButton.description.text = getString(
                R.string.settings_deleteDataOptionDescription,
                databaseUtils.getDatabaseSize())
    }
}
