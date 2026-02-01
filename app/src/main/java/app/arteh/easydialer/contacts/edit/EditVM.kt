package app.arteh.easydialer.contacts.edit

import android.app.Activity
import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import app.arteh.easydialer.contacts.ContactRP
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.UIState
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditVM(savedStateHandle: SavedStateHandle, application: Application) :
    AndroidViewModel(application) {

    val contactID: Long = savedStateHandle.get<Long>("id") ?: error("Contact ID is required")
    val rp = ContactRP(application)

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val _contact = MutableStateFlow(EditableContact())
    val contact = _contact.asStateFlow()

    var selectedPhoneIDX: Int = 0

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _contact.emit(rp.findContactByID(contactID))
        }

        viewModelScope.launch {
            rp.speedDialMap
                .collect { map ->

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

    fun updateName(name: String) {
        _contact.value = _contact.value.copy(fullName = name)
    }

    fun updateFirstName(name: String) {
        _contact.value = _contact.value.copy(firstName = name)
    }

    fun updateLastName(name: String) {
        _contact.value = _contact.value.copy(lastName = name)
    }

    fun updatePhone(index: Int, newNumber: String) {
        val phones = _contact.value.phones.toMutableList()

        val contactNumber = phones[index]
        phones[index] = contactNumber.copy(number = newNumber)

        _contact.value = _contact.value.copy(phones = phones)
    }

    fun showAddPhone() {
        _uiState.update { it.copy(showAdd = true) }
    }

    fun showSpeedDial(phoneIDX: Int) {
        selectedPhoneIDX = phoneIDX

        _uiState.update { it.copy(showSpeedList = true) }
    }

    fun addPhoneNumber(number: String) {
        val phones = _contact.value.phones.toMutableList()
        phones.add(ContactPhone(0, number, 0))
        _contact.value = _contact.value.copy(phones = phones)

        dismissPopup()
    }

    fun removePhone(index: Int) {
        val phones = _contact.value.phones.toMutableList()

        val contactNumber = phones[index]
        if (contactNumber.phoneID > 0)
            phones[index] = contactNumber.copy(isDeleted = true)
        else phones.removeAt(index)

        _contact.value = _contact.value.copy(phones = phones)
    }

    fun setPhoto(uri: Uri?) {
        _contact.value = _contact.value.copy(photoUri = uri)
    }

    fun showDelete() {
        _uiState.update { it.copy(showDelete = true) }
    }

    fun dismissPopup() {
        _uiState.update { it.copy(showDelete = false, showAdd = false, showSpeedList = false) }
    }

    fun deleteContact(context: Context) {
        val uri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactID
        )

        context.contentResolver.delete(uri, null, null)

        (context as Activity).finish()
    }

    fun saveContact(context: Context) {
        val ops = ArrayList<ContentProviderOperation>()

        val rawContactID = contact.value.rawContactID

        // Update name
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                    arrayOf(
                        rawContactID.toString(),
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                    contact.value.firstName
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    contact.value.lastName
                )
                .build()
        )

        // Phones update / insert
        contact.value.phones.forEach {
            if (it.phoneID == 0L) {
                // New number
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, it.number)
                        .build()
                )
            }
            else {
                if (it.isDeleted) {
                    val uri =
                        ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, it.phoneID)

                    context.contentResolver.delete(uri, null, null)
                }
                else
                // Update existing
                    ops.add(
                        ContentProviderOperation.newUpdate(
                            ContentUris.withAppendedId(
                                ContactsContract.Data.CONTENT_URI, it.phoneID
                            )
                        ).withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, it.number)
                            .build()
                    )
            }
        }

        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
    }

    fun updateSpeedSlot(slot: Int) {
        viewModelScope.launch {
            val contactPhone = contact.value.phones[selectedPhoneIDX]

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
                contact.value.fullName
            )

            rp.updateSpeedDial(slot, oldSlot, entry)
        }

        dismissPopup()
    }

    class Factory(
        val application: Application,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val savedStateHandle = extras.createSavedStateHandle()

            return EditVM(savedStateHandle, application) as T
        }
    }
}