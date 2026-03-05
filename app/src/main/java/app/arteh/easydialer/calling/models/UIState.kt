package app.arteh.easydialer.calling.models

import app.arteh.easydialer.contacts.list.models.Contact

data class UIState(
    val state: CallState = CallState.Calling,
    val showDialPad: Boolean = false,

    val phoneNumber: String = "",
    val contact: Contact? = null,

    val isMute: Boolean = false,
    val isSpeaker: Boolean = false,
)