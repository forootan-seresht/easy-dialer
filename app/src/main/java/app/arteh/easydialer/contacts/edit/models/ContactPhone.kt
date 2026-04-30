package app.arteh.easydialer.contacts.edit.models

import app.arteh.easydialer.R

enum class PhoneType(val fullName: Int, val icon: Int) {
    Mobile(R.string.mobile, R.drawable.mobile),
    Home(R.string.home, R.drawable.home),
    Work(R.string.work, R.drawable.work),
    Other(R.string.other, R.drawable.call)
}

data class ContactPhone(
    val phoneID: Long = 0,
    val number: String,
    val type: PhoneType,
    val isBLocked: Boolean = false,
    val isDeleted: Boolean = false,
    val isDefault: Boolean = false,
)