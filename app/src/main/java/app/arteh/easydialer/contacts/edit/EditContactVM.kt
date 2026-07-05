package app.arteh.easydialer.contacts.edit

import android.app.Activity
import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditContactAction
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.EdtContUIState
import app.arteh.easydialer.contacts.edit.models.PhoneType
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditContactVM(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    val contactID: Long = savedStateHandle.get<Long>("id") ?: 0L
    val phoneNumber: String = savedStateHandle.get<String>("number") ?: ""

    private var _uiState = MutableStateFlow(EdtContUIState())
    val uiState = _uiState.asStateFlow()

    private val _contact = MutableStateFlow(EditableContact())
    val contact = _contact.asStateFlow()

    lateinit var initialContact: EditableContact

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (contactID != 0L) {
                val contact = Holder.contactRP.findContactByID(contactID, application)

                initialContact = contact

                _contact.update { contact }
            }
        }

        if (phoneNumber.isNotEmpty())
            _uiState.update { it.copy(showAdd = true, phoneNumber = phoneNumber) }

    }

    fun onAction(action: EditContactAction) {
        when (action) {
            is EditContactAction.SetPhoto -> _contact.update { it.copy(photoUri = action.uri) }
            is EditContactAction.UpdateFirstName -> _contact.update { it.copy(firstName = action.name) }
            is EditContactAction.UpdateLastName -> _contact.update { it.copy(lastName = action.lastName) }
            is EditContactAction.RemovePhone -> removePhone(action.index)
            is EditContactAction.UpdatePhone -> updatePhone(action.index, action.phone)
            is EditContactAction.UpdateBusiness -> _contact.update { it.copy(business = action.company) }
            is EditContactAction.UpdateJob -> _contact.update { it.copy(job = action.job) }
            is EditContactAction.ChangeType -> updatePhoneType(action.index, action.type)
            EditContactAction.ShowAddPhone -> _uiState.update { it.copy(showAdd = true) }
            is EditContactAction.UpdateEmail -> _contact.update { it.copy(email = action.email) }
            is EditContactAction.UpdateNote -> _contact.update { it.copy(note = action.note) }

            //Number
            EditContactAction.DismissPopup -> dismissPopup()
            is EditContactAction.UpdatePhoneNumber -> _uiState.update { it.copy(phoneNumber = action.number) }
            is EditContactAction.AddNumber -> addPhoneNumber(action.type)
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

    fun addPhoneNumber(type: PhoneType) {
        val phoneNumber = uiState.value.phoneNumber
        val phones = _contact.value.phones.toMutableList()
        phones.add(ContactPhone(0, phoneNumber, type))
        _contact.value = _contact.value.copy(phones = phones)

        dismissPopup()
        _uiState.update { it.copy(phoneNumber = "") }
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
        val state = contact.value
        if (state.phones.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.add_number), Toast.LENGTH_LONG)
                .show()
            return
        }
        if (state.firstName.trim().isEmpty() && state.lastName.trim().isEmpty()) {
            Toast.makeText(
                context,
                context.getString(R.string.name_family_field_empty), Toast.LENGTH_LONG
            )
                .show()
            return
        }

        val ops = ArrayList<ContentProviderOperation>()

        val rawContactID = contact.value.rawContactID

        if (rawContactID != 0L) {
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
            if (initialContact.job.isNotEmpty() && initialContact.business.isNotEmpty())
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
                            contact.value.business
                        )
                        .build()
                )
            else
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Organization.TITLE,
                            contact.value.job
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Organization.COMPANY,
                            contact.value.business
                        )
                        .build()
                )

            //Email
            if (initialContact.email.isNotEmpty())
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
            else
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Email.ADDRESS,
                            contact.value.email
                        )
                        .build()
                )

            //Note
            if (initialContact.note.isNotEmpty())
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
            else
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Note.NOTE,
                            contact.value.note
                        )
                        .build()
                )

            if (contact.value.photoUri != null) {
                val photoBytes = uriToByteArray(context, contact.value.photoUri!!)

                if (initialContact.note.isNotEmpty())
                    ops.add(
                        ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                                arrayOf(
                                    rawContactID.toString(),
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                )
                            )
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                            .build()
                    )
                else
                    ops.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                            )
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                            .build()
                    )
            }

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
                            ContentUris.withAppendedId(
                                ContactsContract.Data.CONTENT_URI,
                                it.phoneID
                            )

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
        }
        else {
            val rawContactInsertIndex = 0

            //base insert
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            //insert name and family
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
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

            // Job
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Organization.TITLE,
                        contact.value.job
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Organization.COMPANY,
                        contact.value.business
                    )
                    .build()
            )

            //Email
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Organization.COMPANY,
                        contact.value.business
                    )
                    .build()
            )

            //Note
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Note.NOTE,
                        contact.value.business
                    )
                    .build()
            )

            if (contact.value.photoUri != null) {
                val photoBytes = uriToByteArray(context, contact.value.photoUri!!)

                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex
                        )
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Photo.PHOTO,
                            photoBytes
                        )
                        .build()
                )
            }

            val mimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE

            // Phones update / insert
            contact.value.phones.forEach {
                val phoneType = when (it.type) {
                    PhoneType.Mobile -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    PhoneType.Home -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                    PhoneType.Work -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    PhoneType.Other -> ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                }

                // New number
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex
                        )
                        .withValue(ContactsContract.Data.MIMETYPE, mimeType)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, it.number)
                        .build()
                )
            }
        }

        val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

        Log.d("CONTACT_DEBUG", results.toString())

        (context as Activity).finish()
    }

    fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)!!.use {
            it.readBytes()
        }
    }
}