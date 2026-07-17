package app.arteh.easydialer.dial

import android.content.Context
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.contacts.models.Contact

data class DialUIState(
    val dialedNumber: String = "",
    val isBigDial: Boolean = false,
    val contactList: List<Contact> = emptyList(),
    val dialedList: List<Clog> = emptyList(),
    val showDial: Boolean = true,
    val showContactList: Boolean = false,
)

sealed interface DialAction {
    data class NumberCLicked(val digit: String) : DialAction
    data class NumberLongCLicked(val digit: String) : DialAction
    data class ShowMakeCall(val number: String) : DialAction
    data class ShowMakeCallContact(val contact: Contact) : DialAction
    data class ShowSendSMS(val number: String) : DialAction
    data class ShowSendSMSContact(val contact: Contact) : DialAction
    data class ShowContact(val contactID: Long) : DialAction

    data class AddNewContact(val context: Context) : DialAction
    data class AddToContact(val context: Context) : DialAction
    data class SelectContact(val context: Context, val contactID: Long) : DialAction
    data class GoSendMessage(val context: Context) : DialAction

    data object BackSpace : DialAction
    data object LongBackSpace : DialAction
    data object ChangeFold : DialAction
    data object DismissContactList : DialAction
}