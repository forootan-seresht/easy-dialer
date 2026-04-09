package app.arteh.easydialer.clog.models

import app.arteh.easydialer.R

enum class LogStatus(val fullName: String, val icon: Int, val code: Int) {
    All("All", R.drawable.all_logs, 0),
    Incoming("Incoming", R.drawable.call_received, 1),
    Outgoing("Outgoing", R.drawable.call_made, 2),
    Missed("Missed", R.drawable.call_missed, 3),
    Rejected("Rejected", R.drawable.call_rejected, 5),
    Other("Others", R.drawable.local_phone, -1);

    companion object {
        fun fromCode(code: Int): LogStatus {
            return LogStatus.entries.find { it.code == code } ?: Other
        }
    }
}