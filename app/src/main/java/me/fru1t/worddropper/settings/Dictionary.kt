package me.fru1t.worddropper.settings

import android.content.Context
import android.os.AsyncTask
import com.google.common.base.Strings
import me.fru1t.android.slick.Slik
import me.fru1t.android.slick.annotations.Inject
import me.fru1t.android.slick.annotations.Named
import me.fru1t.android.slick.annotations.Singleton
import me.fru1t.worddropper.WordDropperApplication
import me.fru1t.worddropper.WordDropperApplication.Companion.IS_DEBUGGING
import org.jdeferred.impl.DeferredObject
import java.util.Scanner
import java.util.Random
import java.util.concurrent.CountDownLatch

/** Handles loading and caching game words. */
@Inject
@Singleton
class Dictionary(
        private @Named(IS_DEBUGGING) val isDebugging: Boolean,
        private val context: Context) {
    companion object {
        private val TOTAL_WORDS = 369648
    }

    private val onLoadDefer: DeferredObject<Any, Any, Double>
    private val dictionary: HashSet<String>
    private val loaderCdl: CountDownLatch

    private var loadedWords: Int = 0
    var isLoaded = false
        private set

    private enum class LetterValue constructor(val value: Int) {
        A(1), B(3), C(3), D(2), E(1), F(4), G(2), H(4), I(1), J(8), K(5), L(1), M(3), N(1), O(1),
        P(3), Q(10), R(1), S(1), T(1), U(1), V(4), W(4), X(8), Y(4), Z(10)
    }

    private inner class DictionaryLoader : AsyncTask<String, Int, HashSet<String>>() {
        override fun doInBackground(vararg params: String): HashSet<String> {
            val dictionaryName = params[0]
            val result = HashSet<String>()

            val dictionaryScanner = Scanner(context.resources.openRawResource(
                    context.resources.getIdentifier(
                            dictionaryName, "raw", context.packageName)))
            var wordsLoaded = 0
            while (dictionaryScanner.hasNext()) {
                result.add(dictionaryScanner.next())
                ++wordsLoaded
                if (wordsLoaded % 1000 == 0) {
                    publishProgress(wordsLoaded)
                    wordsLoaded = 0
                }
            }
            publishProgress(wordsLoaded)

            return result
        }

        override fun onPostExecute(o: HashSet<String>) {
            synchronized(dictionary) {
                dictionary.addAll(o)
            }

            synchronized(loaderCdl) {
                loaderCdl.countDown()
                if (loaderCdl.count == 0L) {
                    isLoaded = true
                    onLoadDefer.resolve(null)
                }
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            loadedWords += values[0]!!
            onLoadDefer.notify(1.0 * loadedWords / TOTAL_WORDS)
        }
    }

    init {
        Slik.get(WordDropperApplication::class.java).inject(this)
        loaderCdl = CountDownLatch(8)
        dictionary = HashSet()
        onLoadDefer = DeferredObject()

        loadedWords = 0
        isLoaded = false

        // Load dictionary
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_a")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_b")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_c")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_d")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_e")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_f")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_g")
        DictionaryLoader()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "english_dictionary_h")
    }

    /**
     * Determines the point value of a given string.
     */
    fun getWordValue(string: String?): Int {
        if (Strings.isNullOrEmpty(string)) {
            return 0
        }

        var result = 0
        for (c in string!!.toUpperCase().toCharArray()) {
            result += LetterValue.valueOf(c + "").value
        }
        if (string.length > 3) {
            result *= (1.0 + (0.3 + 0.2 * (string.length - 3)) * (string.length - 3)).toInt()
        }
        return result
    }

    /**
     * Checks if the given string is a word or not. This method does no sanitization. Make sure
     * incoming strings are lowercase.
     */
    fun isWord(s: String): Boolean = isDebugging || dictionary.contains(s)

    /**
     * Retrieves a random word from the dictionary that's valued at or above a given value
     */
    fun getRandomWord(minWordValue: Int): String {
        val r = Random()
        while (true) {
            var i = r.nextInt(dictionary.size)
            for (s in dictionary) {
                if (i-- <= 0) {
                    return if (getWordValue(s) >= minWordValue) {
                        s
                    } else {
                        break
                    }
                }
            }
        }
    }
}
