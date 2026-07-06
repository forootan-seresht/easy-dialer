package app.arteh.easydialer.settings

import android.content.Context

data class SettingsUIState(
    val language: AppLanguage = AppLanguage("en", "English"),
)

data class AppLanguage(
    val code: String,
    val displayName: String
)

sealed interface SettingsAction {
    data class UpdateLanguage(val index: Int, val context: Context) : SettingsAction
}
