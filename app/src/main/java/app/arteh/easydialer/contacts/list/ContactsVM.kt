package app.arteh.easydialer.contacts.list

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.contacts.edit.EditContactActivity
import app.arteh.easydialer.contacts.list.models.ContactAction
import app.arteh.easydialer.contacts.list.models.ContactsUIState
import app.arteh.easydialer.contacts.show.ContactActivity
import app.arteh.easydialer.settings.SettingsActivity
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.SimCardHR
import app.arteh.easydialer.utility.dialer_hr.DialerHR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsVM(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ContactsUIState())
    val uiState = _uiState.asStateFlow()

    var loaded = false

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, {}, {})

    fun load() {
        if (!loaded) {
            loaded = true
            searchContact(uiState.value.searchText)
        }
    }

    fun onAction(action: ContactAction) {
        when (action) {
            is ContactAction.ShowContact -> goShowContact(action.contactID)
            is ContactAction.ShowMakeCall -> {
                dialerHR.reset()

                dialerHR.makeAction(
                    app.arteh.easydialer.contacts.show.ContactAction.Call,
                    action.contact.defaultSimID,
                    action.contact.phone
                )
            }

            is ContactAction.ShowSendSMS -> {
                dialerHR.reset()
                dialerHR.makeAction(
                    app.arteh.easydialer.contacts.show.ContactAction.SMS,
                    action.contact.defaultSimID,
                    action.contact.phone
                )
            }

            ContactAction.GoAddContact -> goAddContact()
            is ContactAction.UpdateSearchText -> updateSearchText(action.text)
            is ContactAction.GoSettings -> goSettings(action.context)
        }
    }

    private fun goSettings(context: Context) {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    private fun searchContact(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val map = Holder.contactRP.loadContacts(name, getApplication())
            val favorites = Holder.contactRP.getFavoriteContacts(getApplication())

            _uiState.update { it.copy(contactList = map, favorites = favorites) }
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