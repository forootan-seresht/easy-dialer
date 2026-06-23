package app.arteh.easydialer.contacts.edit.models

import android.net.Uri

sealed interface EditContactAction {
    data object ShowAddPhone : EditContactAction
    data class SetPhoto(val uri: Uri?) : EditContactAction
    data class UpdateFirstName(val name: String) : EditContactAction
    data class UpdateLastName(val lastName: String) : EditContactAction
    data class UpdateJob(val job: String) : EditContactAction
    data class UpdateBusiness(val company: String) : EditContactAction
    data class UpdateEmail(val email: String) : EditContactAction
    data class UpdateNote(val note: String) : EditContactAction
    data class UpdatePhone(val index: Int, val phone: String) : EditContactAction
    data class RemovePhone(val index: Int) : EditContactAction
    data class ChangeType(val index: Int, val type: PhoneType) : EditContactAction
}