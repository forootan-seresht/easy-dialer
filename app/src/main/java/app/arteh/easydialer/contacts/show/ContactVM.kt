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
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactVM(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    var selectedPhoneIDX: Int = 0


    init {
        reloadContact()
    }

    fun reloadContact() {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = Holder.contactRP.findContactByID(contactID, getApplication())

            _uiState.update { it.copy(contact = contact) }
        }

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

    fun shareContact() {

    }

    fun makeCall(index: Int) {

    }

    fun sendSMS(index: Int) {

    }

    fun showSpeedDial(phoneIDX: Int) {
        selectedPhoneIDX = phoneIDX

        _uiState.update { it.copy(showSpeedList = true) }
    }

    fun showDelete() {
        _uiState.update { it.copy(showDelete = true) }
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
        _uiState.update { it.copy(showDelete = false, showSpeedList = false) }
    }

    val contactID: Long = savedStateHandle.get<Long>("id") ?: error("Contact ID is required")
}