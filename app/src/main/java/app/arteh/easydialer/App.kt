package app.arteh.easydialer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import app.arteh.easydialer.utility.LocaleHelper
import app.arteh.easydialer.utility.PreferencesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

class App : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        val context = this

        runBlocking {
            val prefs = PreferencesManager(context)
            initLanguage(prefs)
        }
    }

    private suspend fun initLanguage(prefs: PreferencesManager) {
        var lang = prefs.getLang()


        if (lang == "") {
            lang = LocaleHelper.phoneLang()
            prefs.setLang(lang)
        }

        LocaleHelper.lang = lang

        // Check if we actually need to update the application locales.
        // AppCompatDelegate persists this internally, so checking prevents a redundant refresh.
        val currentLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentLocales != lang) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}