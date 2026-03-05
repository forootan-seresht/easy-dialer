package app.arteh.easydialer.clog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.clog.models.Clog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallLogVM(application: Application) : AndroidViewModel(application) {

    val rp = CallLogRP(application)

    private val _logsFlow = MutableStateFlow<List<Clog>>(emptyList())
    val logsFlow = _logsFlow.asStateFlow()

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
            val list = rp.loadCallLog(phone)
            _logsFlow.emit(list)
        }
    }
}