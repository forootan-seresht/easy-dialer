package app.arteh.easydialer.clog.models

import app.arteh.easydialer.contacts.show.Contact

data class Clog(
    val contact: Contact?,
    val number: String,
    val status: Int,
    val date: String,
    val simID: Int,
    val key: Int
)