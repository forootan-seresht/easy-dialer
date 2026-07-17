package app.arteh.easydialer.dial

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.EditContactActivity
import app.arteh.easydialer.contacts.show.ContactAction
import app.arteh.easydialer.contacts.show.ContactActivity
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.PreferencesManager
import app.arteh.easydialer.utility.SimCardHR
import app.arteh.easydialer.utility.dialer_hr.DialerHR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DialPadVM(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DialUIState())
    val uiState = _uiState.asStateFlow()

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, {}, {})

    init {
        viewModelScope.launch {
            PreferencesManager(application).getIsBigButtons().collect { isBig ->
                _uiState.update { it.copy(isBigDial = isBig) }
            }
        }
    }

    fun onAction(action: DialAction) {
        when (action) {
            is DialAction.NumberCLicked -> numberClicked(action.digit)
            is DialAction.NumberLongCLicked -> onNumberLongPress(action.digit)
            DialAction.BackSpace -> backspaceClicked()

            DialAction.LongBackSpace -> _uiState.update {
                it.copy(dialedNumber = "", contactList = emptyList(), dialedList = emptyList())
            }

            is DialAction.ShowContact -> showContact(action.contactID)
            is DialAction.ShowMakeCall -> dialerHR.makeAction(ContactAction.Call, -1, action.number)
            is DialAction.ShowSendSMS -> dialerHR.makeAction(ContactAction.SMS, -1, action.number)
            is DialAction.ShowMakeCallContact -> dialerHR.makeAction(
                ContactAction.Call,
                action.contact.defaultSimID,
                action.contact.phone
            )

            is DialAction.ShowSendSMSContact -> dialerHR.makeAction(
                ContactAction.SMS,
                action.contact.defaultSimID,
                action.contact.phone
            )

            DialAction.ChangeFold -> _uiState.update { it.copy(showDial = !it.showDial) }
            is DialAction.AddNewContact -> goAddNew(action.context, uiState.value.dialedNumber)
            is DialAction.AddToContact -> _uiState.update { it.copy(showContactList = true) }
            is DialAction.SelectContact -> {
                goAddToContact(action.context, uiState.value.dialedNumber, action.contactID)
                _uiState.update { it.copy(showContactList = false) }
            }

            DialAction.DismissContactList -> _uiState.update { it.copy(showContactList = false) }
            is DialAction.GoSendMessage -> dialerHR.makeAction(
                app.arteh.easydialer.contacts.show.ContactAction.SMS,
                -1, uiState.value.dialedNumber
            )
        }
    }

    fun goAddNew(context: Context, dialedNumber: String) {
        val intent = Intent(context, EditContactActivity::class.java)
        intent.putExtra("number", dialedNumber)

        context.startActivity(intent)
    }

    fun goAddToContact(context: Context, dialedNumber: String, contactID: Long) {
        val intent = Intent(context, EditContactActivity::class.java)
        intent.putExtra("number", dialedNumber)
        intent.putExtra("id", contactID)

        context.startActivity(intent)
    }

    fun backspaceClicked() {
        val newNumber = uiState.value.dialedNumber.dropLast(1)
        _uiState.update { it.copy(dialedNumber = newNumber) }

        if (uiState.value.dialedNumber.isEmpty())
            _uiState.update { it.copy(contactList = emptyList(), dialedList = emptyList()) }
        else searchPhone(newNumber)
    }

    fun showContact(contactID: Long) {
        val context = getApplication<Application>()

        val intent = Intent(context, ContactActivity::class.java)
        intent.putExtra("id", contactID)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }

    fun numberClicked(digit: String) {
        val newNumber = uiState.value.dialedNumber + digit

        _uiState.update { it.copy(dialedNumber = newNumber) }

        searchPhone(newNumber)
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun searchPhone(newNumber: String) {
        val context = getApplication<Application>()

        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            Holder.contactRP.contactsList.collect { allContacts ->
                val filteredContacts = allContacts.filter { it.phone.contains(newNumber) }.take(10)
                val allLogs = Holder.contactRP.searchCallLogs(newNumber, context)

                val contactNumbers = filteredContacts.map { it.phone }.toSet()
                val filteredLogs = allLogs.filterNot { log ->
                    contactNumbers.contains(log.number)
                }

                _uiState.update {
                    it.copy(
                        contactList = filteredContacts,
                        dialedList = filteredLogs
                    )
                }
            }
        }
    }

    fun makeCall(number: String) {
        dialerHR.makeAction(ContactAction.Call, -1, number)
    }

    fun onNumberLongPress(digit: String) {
        val context = getApplication() as Context

        viewModelScope.launch {
            val map = Holder.contactRP.speedDialMap.firstOrNull()
            if (map != null) {
                val entry = map[digit.toInt()]
                if (entry != null) {
                    _uiState.update { it.copy(dialedNumber = entry.phoneNumber) }
                    val simID = Holder.contactRP.getPhoneDefaultSim(entry.phoneId)
                    dialerHR.makeAction(ContactAction.Call, simID, entry.phoneNumber)
                }
                else Toast.makeText(
                    context, context.getString(R.string.no_speed_dial1) + "\n" +
                            context.getString(R.string.no_speed_dial2), Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}