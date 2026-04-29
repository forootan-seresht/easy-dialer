package app.arteh.easydialer.contacts.list

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.contacts.Contact
import app.arteh.easydialer.contacts.ContactHeader
import app.arteh.easydialer.contacts.edit.EditContactActivity
import app.arteh.easydialer.contacts.list.models.ContactAction
import app.arteh.easydialer.contacts.list.models.UIState
import app.arteh.easydialer.contacts.show.ContactActivity
import app.arteh.easydialer.dialer.DialerHR
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.SimCardHR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsVM(application: Application) : AndroidViewModel(application) {

    private val _items = MutableStateFlow<Map<ContactHeader, List<Contact>>>(emptyMap())
    val items = _items.asStateFlow()

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    var loaded = false

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, {}, {})

    fun load() {
        if (!loaded) {
            loaded = true
            reloadContacts()
        }
    }

    fun onAction(action: ContactAction) {
        when (action) {
            is ContactAction.ShowContact -> goShowContact(action.contactID)
            is ContactAction.ShowMakeCall -> dialerHR.makeAction(
                app.arteh.easydialer.contacts.show.ContactAction.Call,
                action.contact.defaultSimID,
                action.contact.phone
            )

            is ContactAction.ShowSendSMS -> dialerHR.makeAction(
                app.arteh.easydialer.contacts.show.ContactAction.SMS,
                action.contact.defaultSimID,
                action.contact.phone
            )
        }
    }

    fun reloadContacts() = searchContact(uiState.value.searchText)

    private fun searchContact(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val map = Holder.contactRP.loadContacts(name, getApplication())
            _items.emit(map)
        }
    }

    fun updateSearchText(text: String) {
        _uiState.update { it.copy(searchText = text) }
        searchContact(text)
    }

    fun goAddContact() {
        val context: Context = getApplication()

        val intent = Intent(context, EditContactActivity::class.java)
        intent.apply {
            putExtra("id", 0L)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    fun goShowContact(contactID: Long) {
        val context: Context = getApplication()

        val intent = Intent(context, ContactActivity::class.java)
        intent.apply {
            putExtra("id", contactID)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}