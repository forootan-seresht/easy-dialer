package app.arteh.easydialer.calling

import android.telecom.Call
import app.arteh.easydialer.contacts.Contact

data class CallUiState(
    val state: CallState = CallState.Calling,
    val showDialPad: Boolean = false,

    val phoneNumber: String = "",
    val contact: Contact? = null,

    val isMute: Boolean = false,
    val isSpeaker: Boolean = false,

    val duration: Long = 0,
    val isBigSize: Boolean = false,
)

data class CallInfo(
    val call: Call,
    val number: String,
    val state: Int
)

enum class CallState {
    Incoming,
    Calling,
    Talking,
    Rejected
}

sealed interface CallAction {
    data object Answer : CallAction
    data object Reject : CallAction
    data object HangUp : CallAction
    data object ToggleMute : CallAction
    data object ToggleSpeaker : CallAction
    data object ShowDialPad : CallAction
    data object HideDialPad : CallAction
    data class SendDtmf(val digit: String) : CallAction
}
