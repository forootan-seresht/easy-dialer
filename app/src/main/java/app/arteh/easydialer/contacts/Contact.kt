package app.arteh.easydialer.contacts

import android.net.Uri

data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val thumbUri: Uri?,
    val photoUri: Uri?,
    val defaultSimID: Int,
    val key: Int
)