package app.arteh.easydialer.clog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.clog.models.LogStatus
import app.arteh.easydialer.clog.models.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallLogVM(application: Application) : AndroidViewModel(application) {

    val rp = CallLogRP(application)

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    var loaded = false

    fun load() {
        if (!loaded) {
            loaded = true

            rp.getSimCards()
            loadCallLog("")
        }
    }

    fun loadCallLog(phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = rp.loadCallLog(phone, uiState.value.selectedStatus)

            _uiState.update { it.copy(logs = list) }
        }
    }

    fun changeLogType(type: LogStatus) {
        _uiState.update { it.copy(selectedStatus = type) }
        loadCallLog("")
    }
}