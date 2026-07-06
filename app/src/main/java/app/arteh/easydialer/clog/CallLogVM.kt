package app.arteh.easydialer.clog

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.clog.models.CLogAction
import app.arteh.easydialer.clog.models.LogStatus
import app.arteh.easydialer.clog.models.UIState
import app.arteh.easydialer.contacts.show.ContactAction
import app.arteh.easydialer.contacts.show.ContactActivity
import app.arteh.easydialer.utility.dialer_hr.DialerHR
import app.arteh.easydialer.utility.SimCardHR
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

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, {}, {})

    fun load() {
        if (!loaded) {
            loaded = true

            loadCallLog("")
        }
    }

    fun onAction(action: CLogAction) {
        when (action) {
            is CLogAction.ShowContact -> goShowContact(action.contactID)
            is CLogAction.ShowHistory -> TODO()
            is CLogAction.ShowMakeCall ->
                dialerHR.makeAction(
                    ContactAction.Call, action.clog.simID, action.clog.number
                )

            is CLogAction.ShowSendSMS -> dialerHR.makeAction(
                ContactAction.Call, action.clog.simID, action.clog.number
            )
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

    fun goShowContact(contactID: Long) {
        if (contactID != 0L) {
            val context = getApplication<Application>()

            val intent = Intent(context, ContactActivity::class.java)
            intent.apply {
                putExtra("id", contactID)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        }
    }
}