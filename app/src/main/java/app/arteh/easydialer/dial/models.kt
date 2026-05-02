package app.arteh.easydialer.dial

import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.contacts.Contact

data class UIState(
    val number: String = "",
    val isBigDial: Boolean = false,
    val contactList: List<Contact> = emptyList(),
    val dialedList: List<Clog> = emptyList(),
)

sealed interface DialAction {
    data class NumberCLicked(val digit: String) : DialAction
    data class NumberLongCLicked(val digit: String) : DialAction
    data class ShowMakeCall(val number: String) : DialAction
    data class ShowMakeCallContact(val contact: Contact) : DialAction
    data class ShowSendSMS(val number: String) : DialAction
    data class ShowSendSMSContact(val contact: Contact) : DialAction
    data class ShowContact(val contactID: Long) : DialAction

    data object BackSpace : DialAction
    data object LongBackSpace : DialAction
}