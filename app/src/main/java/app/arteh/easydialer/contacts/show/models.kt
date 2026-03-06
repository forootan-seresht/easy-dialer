package app.arteh.easydialer.contacts.show

import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.speed.SpeedDialEntry

data class UIState(
    val contact: EditableContact? = null,

    val showDelete: Boolean = false,
    val showSpeedList: Boolean = false,
    val speedSlot: Int = -1,
    val speedDialMap: Map<Int, SpeedDialEntry> = emptyMap()
)

sealed interface ContactAction{
    data class MakeCall(val index: Int): ContactAction
    data object ShowMakeCall: ContactAction
    data class SendSMS(val index: Int): ContactAction
    data object ShowSendSMS: ContactAction
    data object ShowDelete: ContactAction
    data object ShareContact: ContactAction
    data object BlocKContact: ContactAction
}