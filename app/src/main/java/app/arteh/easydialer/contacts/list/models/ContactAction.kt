package app.arteh.easydialer.contacts.list.models

import app.arteh.easydialer.contacts.Contact

sealed interface ContactAction {
    data class ShowContact(val contactID: Long) : ContactAction
    data class ShowMakeCall(val contact: Contact) : ContactAction
    data class ShowSendSMS(val contact: Contact) : ContactAction
}