package app.arteh.easydialer.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainVM() : ViewModel() {
    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    fun setPage(idx: BottomTab) {
        _uiState.update { it.copy(selectedTab = idx) }
    }
}