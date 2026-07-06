package app.arteh.easydialer.main

enum class BottomTab {
    Contact,
    Dial,
    CallLog
}

data class MainUIState(
    val selectedTab: BottomTab = BottomTab.Dial
)