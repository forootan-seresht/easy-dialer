package app.arteh.easydialer.contacts.edit.models

import app.arteh.easydialer.contacts.speed.SpeedDialEntry

data class UIState(
    val showDelete: Boolean = false,
    val showAdd: Boolean = false,
    val showSpeedList: Boolean = false,

    val speedSlot: Int = -1,
    val speedDialMap: Map<Int, SpeedDialEntry> = emptyMap()
)
