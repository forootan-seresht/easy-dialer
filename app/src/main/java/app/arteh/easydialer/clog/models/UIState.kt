package app.arteh.easydialer.clog.models

data class UIState(
    val selectedStatus: LogStatus = LogStatus.All,
    val logs: Map<String, List<Clog>> = emptyMap()
)
