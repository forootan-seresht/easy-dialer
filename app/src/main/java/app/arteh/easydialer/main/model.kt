package app.arteh.easydialer.main

enum class BottomTab {
    Contact,
    Dial,
    CallLog
}

data class UIState(
    val selectedTab: BottomTab = BottomTab.Dial
)