package me.fru1t.worddropper

import android.app.Application
import me.fru1t.android.slik.Slik
import me.fru1t.worddropper.database.DatabaseUtils
import me.fru1t.worddropper.database.DatabaseUtilsImpl
import me.fru1t.worddropper.settings.PreferenceManager
import me.fru1t.worddropper.settings.PreferenceManagerImpl

/**
 * App startup. Hands off storage and other user-specific instances to slik for injection.
 */
class WordDropperApplication : Application() {
    companion object {
        const val IS_DEBUGGING = "IsDebugging"
        const val PREF_FILE_NAME = "PrefFileName"
        const val DELETE_DATABASE_ON_DEBUG = "DeleteDatabaseOnDebug"
    }

    override fun onCreate() {
        super.onCreate()

        // Setup Slik
        Slik.get(WordDropperApplication::class)
                .bind(DatabaseUtils::class, DatabaseUtilsImpl::class)
                .bind(PreferenceManager::class, PreferenceManagerImpl::class)
                .provide(this)
                .provide(resources.getBoolean(R.bool.app_debug), IS_DEBUGGING)
                .provide(
                        resources.getBoolean(R.bool.app_deleteDatabaseOnDebug),
                        DELETE_DATABASE_ON_DEBUG)
                .provide(getString(R.string.app_sharedPreferencesFileName), PREF_FILE_NAME)
    }
}
