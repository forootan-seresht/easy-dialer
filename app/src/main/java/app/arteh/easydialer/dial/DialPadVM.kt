package app.arteh.easydialer.dial

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.show.ContactAction
import app.arteh.easydialer.contacts.show.ContactActivity
import app.arteh.easydialer.dialer.DialerHR
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.SimCardHR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DialPadVM(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, {}, {})

    fun onAction(action: DialAction) {
        when (action) {
            is DialAction.NumberCLicked -> numberClicked(action.digit)
            is DialAction.NumberLongCLicked -> onNumberLongPress(action.digit)
            DialAction.BackSpace -> {
                _uiState.update { it.copy(number = it.number.dropLast(1)) }

                if (uiState.value.number.isEmpty()) {
                    _uiState.update {
                        it.copy(contactList = emptyList(), dialedList = emptyList())
                    }
                }
            }

            DialAction.LongBackSpace -> _uiState.update {
                it.copy(number = "", contactList = emptyList(), dialedList = emptyList())
            }

            is DialAction.ShowContact -> showContact(action.contactID)
            is DialAction.ShowMakeCall -> makeCall(action.number)
            is DialAction.ShowSendSMS -> showSendSMS(action.number)
        }
    }

    fun showContact(contactID: Long) {
        val context = getApplication<Application>()

        val intent = Intent(context, ContactActivity::class.java)
        intent.putExtra("id", contactID)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }

    fun showSendSMS(number: String) {
        dialerHR.makeAction(ContactAction.SMS, -1, number)
    }

    fun numberClicked(digit: String) {
        val newNumber = uiState.value.number + digit
        val context = getApplication<Application>()

        _uiState.update { it.copy(number = newNumber) }

        viewModelScope.launch(Dispatchers.IO) {
            val (contactList, dialedList) = Holder.contactRP.searchByNumber(newNumber, context)

            _uiState.update { it.copy(contactList = contactList, dialedList = dialedList) }
        }
    }

    @SuppressLint("MissingPermission")
    fun makeCall(number: String) {
        dialerHR.makeAction(ContactAction.Call, -1, number)
    }

    fun onNumberLongPress(digit: String) {
        val context = getApplication() as Context

        if (digit == "0")
            _uiState.update { it.copy(number = it.number + "0") }
        else
            viewModelScope.launch {
                val map = Holder.contactRP.speedDialMap.firstOrNull()
                if (map != null) {
                    val phoneNumber = map[digit.toInt()]?.phoneNumber
                    if (phoneNumber != null) {
                        _uiState.update { it.copy(number = phoneNumber) }
                        makeCall(phoneNumber)
                    }
                    else Toast.makeText(
                        context, context.getString(R.string.no_speed_dial1) + "\n" +
                                context.getString(R.string.no_speed_dial2), Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}