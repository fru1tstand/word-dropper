package me.fru1t.worddropper.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jdeferred.impl.DeferredObject;

import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import lombok.Getter;
import me.fru1t.worddropper.WordDropperApplication;

/**
 * Methods for game words like existence, point values, etc.
 */
public class Dictionary {
    private enum LetterValue {
        A(1), B(3), C(3), D(2), E(1), F(4), G(2), H(4), I(1), J(8), K(5), L(1), M(3), N(1), O(1),
        P(3), Q(10), R(1), S(1), T(1), U(1), V(4), W(4), X(8), Y(4), Z(10);

        public final int value;

        LetterValue(int value) {
            this.value = value;
        }
    }

    private class DictionaryLoader extends AsyncTask<String, Integer, HashSet<String>> {
        @Override
        protected HashSet<String> doInBackground(String... params) {
            String dictionaryName = params[0];
            HashSet<String> result = new HashSet<>();

            Scanner dictionaryScanner = new Scanner(app.getResources().openRawResource(
                    app.getResources().getIdentifier(
                            dictionaryName, "raw", app.getPackageName())));
            int wordsLoaded = 0;
            while (dictionaryScanner.hasNext()) {
                result.add(dictionaryScanner.next());
                ++wordsLoaded;
                if (wordsLoaded % 1000 == 0) {
                    publishProgress(wordsLoaded);
                    wordsLoaded = 0;
                }
            }
            publishProgress(wordsLoaded);

            return result;
        }

        @Override
        protected void onPostExecute(HashSet<String> o) {
            synchronized (dictionary) {
                dictionary.addAll(o);
            }

            synchronized (loaderCdl) {
                loaderCdl.countDown();
                if (loaderCdl.getCount() == 0) {
                    isLoaded = true;
                    onLoadDefer.resolve(null);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            loadedWords += values[0];
            onLoadDefer.notify(1.0 * loadedWords / TOTAL_WORDS);
        }
    }

    private static final int TOTAL_WORDS = 369648;

    private @Getter final DeferredObject<Object, Object, Double> onLoadDefer;
    private final WordDropperApplication app;
    private final HashSet<String> dictionary;
    private final CountDownLatch loaderCdl;
    private int loadedWords;
    private @Getter boolean isLoaded;

    public Dictionary(Context context) {
        app = (WordDropperApplication) context.getApplicationContext();
        loaderCdl = new CountDownLatch(8);
        dictionary = new HashSet<>();
        onLoadDefer = new DeferredObject<>();

        loadedWords = 0;
        isLoaded = false;

        // Load dictionary
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_a");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_b");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_c");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_d");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_e");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_f");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_g");
        (new DictionaryLoader())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_h");
    }

    /**
     * Determines the point value of a given string.
     */
    public int getWordValue(@Nullable String string) {
        if (Strings.isNullOrEmpty(string)) {
            return 0;
        }

        int result = 0;
        for (char c : string.toUpperCase().toCharArray()) {
            result += LetterValue.valueOf(c + "").value;
        }
        if (string.length() > 3) {
            result *= 1.0 + ((0.3 + 0.2 * (string.length() - 3)) * (string.length() - 3));
        }
        return result;
    }

    /**
     * Checks if the given string is a word or not. This method does no sanitization. Make sure
     * incoming strings are lowercase.
     */
    public boolean isWord(String s) {
        return app.isDebugging() ||  dictionary.contains(s);
    }
}
