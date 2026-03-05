package app.arteh.easydialer.contacts.edit.models

enum class PhoneType {
    Mobile, Home, Work, Other
}

data class ContactPhone(
    val phoneID: Long = 0,
    val number: String,
    val type: PhoneType,
    val isDeleted: Boolean = false,
)