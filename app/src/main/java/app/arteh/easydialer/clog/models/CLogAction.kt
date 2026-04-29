package app.arteh.easydialer.clog.models

sealed interface CLogAction {
    data class ShowContact(val contactID: Long) : CLogAction

    data class ShowMakeCall(val clog: Clog) : CLogAction
    data class ShowSendSMS(val clog: Clog) : CLogAction
    data class ShowHistory(val index: Int) : CLogAction
}