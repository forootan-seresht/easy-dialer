package app.arteh.easydialer.clog.models

import app.arteh.easydialer.R

enum class LogStatus(val fullName: Int, val icon: Int, val code: Int) {
    All(R.string.all, R.drawable.all_logs, 0),
    Incoming(R.string.incoming, R.drawable.call_received, 1),
    Outgoing(R.string.outgoing, R.drawable.call_made, 2),
    Missed(R.string.missed, R.drawable.call_missed, 3),
    Rejected(R.string.rejected, R.drawable.call_rejected, 5),
    Other(R.string.others, R.drawable.local_phone, -1);

    companion object {
        fun fromCode(code: Int): LogStatus {
            return LogStatus.entries.find { it.code == code } ?: Other
        }
    }
}