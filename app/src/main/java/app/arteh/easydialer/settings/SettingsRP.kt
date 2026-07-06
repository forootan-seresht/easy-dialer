package app.arteh.easydialer.settings

import android.content.Context
import app.arteh.easydialer.utility.PreferencesManager

class SettingsRP {

    suspend fun saveLanguage(lang: String, context: Context) {
        PreferencesManager(context).setLang(lang)
    }
}