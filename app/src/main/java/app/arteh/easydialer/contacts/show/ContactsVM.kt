package app.arteh.easydialer.contacts.show

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.contacts.ContactRP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactsVM(application: Application, val contactRP: ContactRP) :
    AndroidViewModel(application) {

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

    private val _items = MutableStateFlow<List<Contact>>(emptyList())
    val items = _items.asStateFlow()

    var loaded = false

    fun load() {
        if (!loaded) {
            loaded = true
            loadContacts()
        }
    }

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            contactRP.loadContacts()
            _items.emit(contactRP.contactMList)
        }
    }
}