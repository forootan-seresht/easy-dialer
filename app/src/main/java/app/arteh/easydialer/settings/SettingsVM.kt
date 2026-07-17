package app.arteh.easydialer.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.utility.LocaleHelper
import app.arteh.easydialer.utility.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsVM(application: Application) : AndroidViewModel(application) {

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

            PreferencesManager(getApplication()).getIsBigButtons().collect { isBig ->
                _uiState.update { it.copy(isBigButtons = isBig) }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.UpdateLanguage -> updateLanguage(action.index, action.context)
            is SettingsAction.UpdateDialStyle -> updateDialStyle(action.isBig, action.context)
        }
    }

    fun updateDialStyle(isBig: Boolean, context: Context) {
        viewModelScope.launch {
            settingsRP.saveDialStyle(isBig, context)
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