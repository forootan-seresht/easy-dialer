package app.arteh.easydialer.contacts.edit

import android.app.Activity
import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.R
import app.arteh.easydialer.utility.Holder
import com.image.cropview.CropType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditContactVM(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    val contactID: Long = savedStateHandle.get<Long>("id") ?: 0L
    val phoneNumber: String = savedStateHandle.get<String>("number") ?: ""

    private var _uiState = MutableStateFlow(EdtContUIState())
    val uiState = _uiState.asStateFlow()

    var initialContact: EditableContact = EditableContact()

    init {
        initialContact = EditableContact()
        viewModelScope.launch(Dispatchers.IO) {
            if (contactID != 0L) {
                val contact = Holder.contactRP.findContactByID(contactID, application)
                initialContact = contact
                _uiState.update { it.copy(contact = contact) }
            }
        }

        if (phoneNumber.isNotEmpty())
            _uiState.update { it.copy(showAdd = true, phoneNumber = phoneNumber) }

    }

    fun onAction(action: EditContactAction) {
        when (action) {
            is EditContactAction.SetPhoto -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        photoUri = action.uri
                    )
                )
            }

            is EditContactAction.UpdateFirstName -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        firstName = action.name
                    )
                )
            }

            is EditContactAction.UpdateLastName -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        lastName = action.lastName
                    )
                )
            }

            is EditContactAction.RemovePhone -> removePhone(action.index)
            is EditContactAction.UpdatePhone -> updatePhone(action.index, action.phone)
            is EditContactAction.UpdateBusiness -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        business = action.company
                    )
                )
            }

            is EditContactAction.UpdateJob -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        job = action.job
                    )
                )
            }

            is EditContactAction.ChangeType -> updatePhoneType(action.index, action.type)
            EditContactAction.ShowAddPhone -> _uiState.update { it.copy(showAdd = true) }
            is EditContactAction.UpdateEmail -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        email = action.email
                    )
                )
            }

            is EditContactAction.UpdateNote -> _uiState.update {
                it.copy(
                    contact = it.contact.copy(
                        note = action.note
                    )
                )
            }

            //Number
            EditContactAction.DismissPopup -> dismissPopup()
            is EditContactAction.UpdatePhoneNumber -> _uiState.update { it.copy(phoneNumber = action.number) }
            is EditContactAction.AddNumber -> addPhoneNumber(action.type)
        }
    }

    fun onImageAction(action: ImageCropAction) {
        when (action) {
            ImageCropAction.CancelCrop -> _uiState.update {
                it.copy(cropState = it.cropState.copy(isCropping = false))
            }

            is ImageCropAction.SaveCroppedImage -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val uri = saveBitmapToCache(action.context, action.cropped)
                    _uiState.update {
                        it.copy(
                            cropState = it.cropState.copy(isCropping = false),
                            contact = it.contact.copy(photoUri = uri)
                        )
                    }
                }
            }

            is ImageCropAction.ProfileImageSelected -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val bitmap = Holder.loadBitmapUri(action.context, action.uri)

                    _uiState.update {
                        it.copy(
                            cropState = it.cropState.copy(
                                isCropping = true,
                                croppingImage = bitmap,
                                cropType = CropType.PROFILE_CIRCLE
                            )
                        )
                    }
                }
            }

            is ImageCropAction.RotateImage -> {
                _uiState.update { state ->
                    val cropState = state.cropState
                    cropState.croppingImage?.let {
                        val degrees = if (action.isClockWise) 90f else -90f
                        state.copy(
                            cropState = cropState.copy(
                                croppingImage = rotateBitmap(it, degrees),
                                rotation = cropState.rotation + degrees
                            )
                        )
                    } ?: state
                }
            }

            else -> {}
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        val file = File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
        return try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    fun updatePhoneType(index: Int, type: PhoneType) {
        val list = uiState.value.contact.phones.toMutableList()
        list[index] = list[index].copy(type = type)
        _uiState.update { it.copy(contact = it.contact.copy(phones = list)) }
    }

    fun updatePhone(index: Int, newNumber: String) {
        val phones = uiState.value.contact.phones.toMutableList()

        val contactNumber = phones[index]
        phones[index] = contactNumber.copy(number = newNumber)

        _uiState.update { it.copy(contact = it.contact.copy(phones = phones)) }
    }

    fun addPhoneNumber(type: PhoneType) {
        val phoneNumber = uiState.value.phoneNumber
        val phones = uiState.value.contact.phones.toMutableList()
        phones.add(ContactPhone(0, phoneNumber, type))
        _uiState.update { it.copy(contact = it.contact.copy(phones = phones), phoneNumber = "") }

        dismissPopup()
    }

    fun removePhone(index: Int) {
        val phones = uiState.value.contact.phones.toMutableList()

        val contactNumber = phones[index]
        if (contactNumber.phoneID > 0)
            phones[index] = contactNumber.copy(isDeleted = true)
        else phones.removeAt(index)

        _uiState.update { it.copy(contact = it.contact.copy(phones = phones)) }
    }

    fun dismissPopup() {
        _uiState.update { it.copy(showAdd = false) }
    }

    fun saveContact(context: Context) {
        val contact = uiState.value.contact
        if (contact.phones.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.add_number), Toast.LENGTH_LONG)
                .show()
            return
        }
        if (contact.firstName.trim().isEmpty() && contact.lastName.trim().isEmpty()) {
            Toast.makeText(
                context,
                context.getString(R.string.name_family_field_empty), Toast.LENGTH_LONG
            )
                .show()
            return
        }

        val ops = ArrayList<ContentProviderOperation>()

        val rawContactID = contact.rawContactID

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
                        contact.firstName
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        contact.lastName
                    )
                    .build()
            )

            //Job
            if (initialContact.job.isNotEmpty() || initialContact.business.isNotEmpty())
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
                            ContactsContract.CommonDataKinds.Organization.TITLE, contact.job
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Organization.COMPANY, contact.business
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
                            ContactsContract.CommonDataKinds.Organization.TITLE, contact.job
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Organization.COMPANY, contact.business
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
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, contact.email)
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
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, contact.email)
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
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.note)
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
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.note)
                        .build()
                )

            val photoUri = contact.photoUri
            if (photoUri != null) {
                val photoBytes = uriToByteArray(context, photoUri)

                if (initialContact.photoUri != null)
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
            contact.phones.forEach {
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
                        contact.firstName
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        contact.lastName
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
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, contact.job)
                    .withValue(
                        ContactsContract.CommonDataKinds.Organization.COMPANY, contact.business
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
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, contact.email)
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
                    .withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.note)
                    .build()
            )

            val photoUri = contact.photoUri
            if (photoUri != null) {
                val photoBytes = uriToByteArray(context, photoUri)

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
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                        .build()
                )
            }

            val mimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE

            // Phones update / insert
            contact.phones.forEach {
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

        try {
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            Log.d("CONTACT_DEBUG", results.toString())
        } catch (e: Exception) {
            Log.e("CONTACT_DEBUG", "Error saving contact", e)
            Toast.makeText(context, "Error saving contact", Toast.LENGTH_SHORT).show()
        }

        (context as Activity).finish()
    }

    fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use {
            it.readBytes()
        } ?: byteArrayOf()
    }
}
