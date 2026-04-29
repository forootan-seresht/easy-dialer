package app.arteh.easydialer.contacts.show

import android.app.Activity
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.dialer.DialerHR
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.SimCardHR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactVM(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    val contactID: Long = savedStateHandle.get<Long>("id") ?: error("Contact ID is required")

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private var _showState = MutableStateFlow(ShowState())
    val showState = _showState.asStateFlow()

    var selectedPhoneIDX: Int = 0

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, ::saveDefaultSim, ::saveDefaultNumber)


    init {
        reloadContact()
        viewModelScope.launch {
            Holder.contactRP.speedDialMap.collect { map ->
                var slot: Int = -1
                for (entry in map)
                    if (entry.value.contactId == contactID) {
                        slot = entry.key
                        break
                    }

                _uiState.update { it.copy(speedSlot = slot, speedDialMap = map) }
            }
        }
    }

    fun onAction(action: ContactUIAction) {
        when (action) {
            ContactUIAction.ShowBlocK -> _showState.update { it.copy(showBlock = true) }
            is ContactUIAction.MakeCall -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(
                    ContactAction.Call,
                    state.defaultSimID,
                    state.phones,
                    action.index
                )
            }

            is ContactUIAction.SendSMS -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(
                    ContactAction.SMS,
                    state.defaultSimID,
                    state.phones,
                    action.index
                )
            }

            ContactUIAction.ShareContact -> shareContact()
            ContactUIAction.ShowDelete -> _showState.update { it.copy(showDelete = true) }
            ContactUIAction.ShowMakeCall -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(ContactAction.Call, state.defaultSimID, state.phones)
            }

            ContactUIAction.ShowSendSMS -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(ContactAction.SMS, state.defaultSimID, state.phones)
            }

            ContactUIAction.AddFavorite -> addToFavorite()
            is ContactUIAction.DeleteContact -> deleteContact(action.context)
            is ContactUIAction.UpdateSpeedSlot -> updateSpeedSlot(action.slot)
            ContactUIAction.ReloadContact -> reloadContact()
            ContactUIAction.BlockNumbers -> blockNumbers()
        }
    }

    fun blockNumbers() {
        dismissPopup()

        viewModelScope.launch {
            val phoneNumbers = uiState.value.contact!!.phones.toMutableList()
            val context = getApplication<Application>()

            phoneNumbers.forEachIndexed { index, phone ->
                Holder.contactRP.blockNumber(context, phone.number)

                phoneNumbers[index] = phoneNumbers[index].copy(isBLocked = true)
            }

            _uiState.update { it.copy(contact = it.contact!!.copy(phones = phoneNumbers)) }
        }
    }

    fun saveDefaultSim(index: Int) {
        val id = simCardHR.simCardList[index].id

        _uiState.update { it.copy(contact = it.contact!!.copy(defaultSimID = id)) }

        viewModelScope.launch(Dispatchers.IO) {
            Holder.contactRP.saveDefaultSim(contactID, id)
        }
    }

    fun saveDefaultNumber(index: Int) {
        val phones = uiState.value.contact!!.phones.toMutableList()

        phones[index] = phones[index].copy(isDefault = true)

        _uiState.update { it.copy(contact = it.contact!!.copy(phones = phones)) }

        viewModelScope.launch(Dispatchers.IO) {
            Holder.contactRP.saveDefaultNumber(contactID, phones[index].phoneID)
        }
    }

    fun reloadContact() {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = Holder.contactRP.findContactByID(contactID, getApplication())

            var isBLocked = false
            for (phone in contact.phones)
                if (phone.isBLocked) {
                    isBLocked = true
                    break
                }

            _uiState.update { it.copy(contact = contact, isBLocked = isBLocked) }
        }
    }

    fun shareContact() {

    }

    fun showSpeedDial(phoneIDX: Int) {
        selectedPhoneIDX = phoneIDX

        _showState.update { it.copy(showSpeedList = true) }
    }

    fun deleteContact(context: Context) {
        val uri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactID
        )

        context.contentResolver.delete(uri, null, null)

        (context as Activity).finish()
    }

    fun updateSpeedSlot(slot: Int) {
        viewModelScope.launch {
            val contactPhone = uiState.value.contact!!.phones[selectedPhoneIDX]

            var oldSlot = -1

            for (entry in uiState.value.speedDialMap) {
                if (entry.value.contactId == contactID) {
                    oldSlot = entry.key
                    break
                }
            }

            val entry = SpeedDialEntry(
                contactID,
                contactPhone.number,
                uiState.value.contact!!.fullName
            )

            Holder.contactRP.updateSpeedDial(slot, oldSlot, entry)
        }

        dismissPopup()
    }

    fun dismissPopup() {
        _showState.update { it.copy(showDelete = false, showSpeedList = false, showBlock = false) }
    }

    fun addToFavorite() {
        val isStarred = uiState.value.contact!!.isStarred
        Holder.contactRP.setFavorite(getApplication(), contactID, !isStarred)

        _uiState.update { it.copy(contact = it.contact!!.copy(isStarred = !isStarred)) }
    }
}