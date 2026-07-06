package app.arteh.easydialer.contacts.show

import android.content.Context
import app.arteh.easydialer.contacts.edit.EditableContact
import app.arteh.easydialer.contacts.speed.SpeedDialEntry

data class ShowState(
    val showDelete: Boolean = false,
    val showSpeedList: Boolean = false,
    val showBlock: Boolean = false,
    val showShare: Boolean = false,
)

data class UIState(
    val contact: EditableContact? = null,

    val speedSlot: Int = -1,
    val speedDialMap: Map<Int, SpeedDialEntry> = emptyMap(),

    val isBLocked: Boolean = false
)

data class ShareChecks(
    val name: Boolean = true,
    val phones: Boolean = true,
    val email: Boolean = false,
    val asFile: Boolean = false,
    val jobCompany: Boolean = false,
    val note: Boolean = false,
)

sealed interface ContactUIAction {
    data class MakeCall(val index: Int) : ContactUIAction
    data class SendSMS(val index: Int) : ContactUIAction
    data class UpdateSpeedSlot(val slot: Int) : ContactUIAction
    data class DeleteContact(val context: Context) : ContactUIAction
    data class ShareContact(
        val shareChecks: ShareChecks,
        val asFile: Boolean,
        val context: Context
    ) : ContactUIAction

    data object AddFavorite : ContactUIAction
    data object OpenEmail : ContactUIAction
    data object ReloadData : ContactUIAction

    //Dialogs
    data object ShowShareContact : ContactUIAction
    data object ShowMakeCall : ContactUIAction
    data object ShowSendSMS : ContactUIAction
    data object ShowDelete : ContactUIAction
    data object ShowBlocK : ContactUIAction

    data object ReloadContact : ContactUIAction
    data object BlockNumbers : ContactUIAction
}

enum class ContactAction {
    None, Call, SMS
}
