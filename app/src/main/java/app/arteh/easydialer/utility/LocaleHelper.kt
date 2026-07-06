package app.arteh.easydialer.utility

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    var lang: String = ""

    fun changeLang() {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
    }

    //find phone language and set it as default
    fun phoneLang(): String {
        var phoneLang = "en"
        val phoneLanguage = Locale.getDefault().language
        val langs = arrayOf("en", "fr", "de", "es", "ar", "zh", "hi", "ru", "ja", "fa")
        for (s in langs)
            if (s == phoneLanguage) {
                phoneLang = phoneLanguage
            }

        return phoneLang
    }
}