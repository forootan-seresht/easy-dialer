package app.arteh.easydialer.contacts.edit.models

import app.arteh.easydialer.R

enum class PhoneType(val fullName: String, val icon: Int) {
    Mobile("Mobile", R.drawable.mobile),
    Home("Home", R.drawable.home),
    Work("Work", R.drawable.work),
    Other("Other", R.drawable.call)
}

data class ContactPhone(
    val phoneID: Long = 0,
    val number: String,
    val type: PhoneType,
    val isBLocked: Boolean = false,
    val isDeleted: Boolean = false,
    val isDefault: Boolean = false,
)