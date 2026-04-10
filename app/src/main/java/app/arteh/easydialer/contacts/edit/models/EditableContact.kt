package app.arteh.easydialer.contacts.edit.models

import android.net.Uri

data class EditableContact(
    val contactID: Long = 0,
    val rawContactID: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val job: String = "",
    val company: String = "",
    val fullName: String = "",
    val isStarred: Boolean = false,
    val phones: List<ContactPhone> = listOf(),
    val photoUri: Uri? = null,
    val defaultSimID: Int = -1,
)