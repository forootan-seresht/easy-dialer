package app.arteh.easydialer.contacts.edit

import android.app.Activity
import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditContactAction
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.PhoneType
import app.arteh.easydialer.contacts.edit.models.UIState
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditContactVM(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    val contactID: Long = savedStateHandle.get<Long>("id") ?: error("Contact ID is required")

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val _contact = MutableStateFlow(EditableContact())
    val contact = _contact.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = Holder.contactRP.findContactByID(contactID, application)
            _contact.emit(contact)
        }
    }

    fun onAction(action: EditContactAction) {
        when (action) {
            is EditContactAction.SetPhoto -> _contact.update { it.copy(photoUri = action.uri) }
            is EditContactAction.UpdateFirstName -> _contact.update { it.copy(firstName = action.name) }
            is EditContactAction.UpdateLastName -> _contact.update { it.copy(lastName = action.lastName) }
            is EditContactAction.RemovePhone -> removePhone(action.index)
            is EditContactAction.UpdatePhone -> updatePhone(action.index, action.phone)
            is EditContactAction.UpdateCompany -> _contact.update { it.copy(company = action.company) }
            is EditContactAction.UpdateJob -> _contact.update { it.copy(job = action.job) }
            is EditContactAction.ChangeType -> updatePhoneType(action.index, action.type)
            EditContactAction.ShowAddPhone -> _uiState.update { it.copy(showAdd = true) }
            is EditContactAction.UpdateEmail -> _contact.update { it.copy(email = action.email) }
            is EditContactAction.UpdateNote -> _contact.update { it.copy(note = action.note) }
        }
    }

    fun updatePhoneType(index: Int, type: PhoneType) {
        val list = contact.value.phones.toMutableList()
        list[index] = list[index].copy(type = type)
        val updatedContact = contact.value.copy(phones = list)
        _contact.update { updatedContact }
    }

    fun updatePhone(index: Int, newNumber: String) {
        val phones = _contact.value.phones.toMutableList()

        val contactNumber = phones[index]
        phones[index] = contactNumber.copy(number = newNumber)

        _contact.value = _contact.value.copy(phones = phones)
    }

    fun addPhoneNumber(number: String, type: PhoneType) {
        val phones = _contact.value.phones.toMutableList()
        phones.add(ContactPhone(0, number, type))
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

    fun dismissPopup() {
        _uiState.update { it.copy(showAdd = false) }
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

        //Job
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                    arrayOf(
                        rawContactID.toString(),
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Organization.TITLE,
                    contact.value.job
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Organization.COMPANY,
                    contact.value.company
                )
                .build()
        )

        //Email
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                    arrayOf(
                        rawContactID.toString(),
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    contact.value.email
                )
                .build()
        )

        //Note
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                    arrayOf(
                        rawContactID.toString(),
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                    )
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Note.NOTE,
                    contact.value.note
                )
                .build()
        )

        val mimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE

        // Phones update / insert
        contact.value.phones.forEach {
            val phoneType = when (it.type) {
                PhoneType.Mobile -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                PhoneType.Home -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                PhoneType.Work -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                PhoneType.Other -> ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
            }

            if (it.phoneID == 0L) {
                // New number
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, mimeType)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
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
                            .withValue(ContactsContract.Data.MIMETYPE, mimeType)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                            .build()
                    )
            }
        }

        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

        (context as Activity).finish()
    }
}