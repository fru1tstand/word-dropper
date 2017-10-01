package me.fru1t.worddropper.ui.splashscreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropperApplication;
import me.fru1t.worddropper.ui.mainmenu.MainMenuActivity;

/**
 * An intermediate screen shown to the user while the application is loading. This screen should
 * never be opened by any other activity and serves as the entry point to the application ensuring
 * any pre-loaded data is available for the rest of the services.
 */
public class SplashScreenActivity extends AppCompatActivity {

    private WordDropperApplication app;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        app = (WordDropperApplication) getApplicationContext();

        root = (LinearLayout) findViewById(R.id.splashScreenRoot);

        addTextView("Loading WordDropperApplication...");

        // Load dictionary
        TextView t = addTextView("");
        app.getDictionary().getOnLoadDefer().progress(progress -> t.setText(progress + ""));
        app.getDictionary().getOnLoadDefer().done(
                o -> Toast.makeText(this, "done loading", Toast.LENGTH_SHORT).show());

        // Load database
        app.getDatabaseUtils().getWritableDatabase();

        goToMainMenu(1000);
    }

    private TextView addTextView(@Nullable String string) {
        TextView result = new TextView(this);
        result.setBackgroundColor(Color.TRANSPARENT);
        result.setTextColor(Color.WHITE);
        result.setTextSize(10);
        if (!Strings.isNullOrEmpty(string)) {
            result.setText(string);
        }
        root.addView(result);
        return result;
    }

    private void goToMainMenu(int delayMs) {
        addTextView("Moving to main menu in " + delayMs + "ms.");
        (new android.os.Handler()).postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, MainMenuActivity.class));
            finish();
        }, delayMs);
    }
}