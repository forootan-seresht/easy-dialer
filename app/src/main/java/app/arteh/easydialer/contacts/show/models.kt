package app.arteh.easydialer.contacts.show

import android.content.Context
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.speed.SpeedDialEntry

data class ShowState(
    val showDelete: Boolean = false,
    val showSpeedList: Boolean = false,
    val showMyNumbers: Boolean = false,
    val showContactNumbers: Boolean = false,
    val showBlock: Boolean = false,
)

data class UIState(
    val contact: EditableContact? = null,

    val speedSlot: Int = -1,
    val speedDialMap: Map<Int, SpeedDialEntry> = emptyMap(),

    val isBLocked: Boolean = false
)

sealed interface ContactUIAction {
    data class MakeCall(val index: Int) : ContactUIAction
    data class SendSMS(val index: Int) : ContactUIAction
    data class UpdateSpeedSlot(val slot: Int) : ContactUIAction
    data class DeleteContact(val context: Context) : ContactUIAction
    data class SelectSim(val index: Int, val remember: Boolean) : ContactUIAction
    data object AddFavorite : ContactUIAction
    data object ShareContact : ContactUIAction

    //Dialogs
    data object ShowMakeCall : ContactUIAction
    data object ShowSendSMS : ContactUIAction
    data object ShowDelete : ContactUIAction
    data object ShowBlocK : ContactUIAction
}

enum class ContactAction {
    None, Call, SMS
}
