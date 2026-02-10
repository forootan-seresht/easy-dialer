package app.arteh.easydialer.contacts.show

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.contacts.ContactRP
import app.arteh.easydialer.contacts.show.models.Contact
import app.arteh.easydialer.contacts.show.models.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsVM(application: Application, val contactRP: ContactRP) :
    AndroidViewModel(application) {

    private val _items = MutableStateFlow<Map<ContactHeader, List<Contact>>>(emptyMap())
    val items = _items.asStateFlow()

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    var loaded = false

    fun load() {
        if (!loaded) {
            loaded = true
            loadContacts()
        }
    }

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val map = contactRP.loadContacts()
            _items.emit(map)
        }
    }

    fun updateSearchText(text: String) {
        _uiState.update { it.copy(searchText = text) }
    }

    class Factory(
        val application: Application,
        val contactRP: ContactRP,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactsVM::class.java)) {
                return ContactsVM(application, contactRP) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}