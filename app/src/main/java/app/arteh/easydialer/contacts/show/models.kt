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