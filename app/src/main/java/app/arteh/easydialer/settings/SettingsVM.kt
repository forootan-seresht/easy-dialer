package app.arteh.easydialer.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.utility.LocaleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsVM : ViewModel() {

    private var _uiState = MutableStateFlow(SettingsUIState())
    val uiState = _uiState.asStateFlow()

    val supportedLanguages = listOf(
        AppLanguage("en", "English"),
        AppLanguage("fr", "Française"),
        AppLanguage("de", "Deutsche"),
        AppLanguage("es", "Español"),
        AppLanguage("ar", "العربية"),
        AppLanguage("zh", "中文"),
        AppLanguage("hi", "हिन्दी"),
        AppLanguage("ru", "Россия"),
        AppLanguage("ja", "日本人"),
        AppLanguage("fa", "فارسی"),
    )

    val settingsRP = SettingsRP()

    init {
        loadInitSettings()
    }

    fun loadInitSettings() {
        viewModelScope.launch {
            val lang = supportedLanguages.first { it.code == LocaleHelper.lang }
            _uiState.update { it.copy(language = lang) }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.UpdateLanguage -> updateLanguage(action.index, action.context)
        }
    }

    fun updateLanguage(index: Int, context: Context) {
        viewModelScope.launch {
            val lang = supportedLanguages[index].code
            settingsRP.saveLanguage(lang, context)

            LocaleHelper.lang = lang
            LocaleHelper.changeLang()
        }
    }
}