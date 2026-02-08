package app.arteh.easydialer.contacts.show.models

import android.net.Uri

data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val thumbUri: Uri?,
    val photoUri: Uri?,
    val key: Int
)