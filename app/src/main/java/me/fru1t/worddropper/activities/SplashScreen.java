package me.fru1t.worddropper.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import me.fru1t.worddropper.R;
import me.fru1t.worddropper.WordDropper;

public class SplashScreen extends AppCompatActivity {
    private class DictionaryLoader extends AsyncTask<String, Integer, HashSet<String>> {
        private TextView text;
        private String dictionaryName;
        private final CountDownLatch cdl;

        public DictionaryLoader(String dictionaryName, TextView text, CountDownLatch cdl) {
            this.text = text;
            this.dictionaryName = dictionaryName;
            this.cdl = cdl;

            text.setText("Loading dictionary " + dictionaryName + "...");
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected HashSet<String> doInBackground(String... params) {
            HashSet<String> result = new HashSet<>();

            Scanner dictionaryScanner = new Scanner(getResources().openRawResource(
                    getResources().getIdentifier(dictionaryName, "raw", getPackageName())));
            int wordsLoaded = 0;
            while (dictionaryScanner.hasNext()) {
                result.add(dictionaryScanner.next());
                ++wordsLoaded;
                if (wordsLoaded % 1000 == 0) {
                    publishProgress(wordsLoaded);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(HashSet<String> o) {
            text.append(" Adding to global...");
            synchronized (WordDropper.dictionary) {
                WordDropper.dictionary.addAll(o);
            }
            text.append(" Done.");

            synchronized (cdl) {
                cdl.countDown();
                if (cdl.getCount() == 0) {
//                    goToMainMenu(1000);
                    Toast.makeText(SplashScreen.this, "Dictionary loaded", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            text.setText("Loading dictionary " + dictionaryName
                    + "... " + values[0] + " words loaded...");
        }
    }

    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        root = (LinearLayout) findViewById(R.id.splashScreenRoot);

        addTextView("Loading WordDropper...");

        CountDownLatch cdl = new CountDownLatch(8);
        (new DictionaryLoader("english_dictionary_a", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_b", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_c", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_d", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_e", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_f", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_g", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new DictionaryLoader("english_dictionary_h", addTextView(null), cdl))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        (new android.os.Handler()).postDelayed(
                () -> startActivity(new Intent(SplashScreen.this, MainMenuScreen.class)), delayMs);
    }
}
