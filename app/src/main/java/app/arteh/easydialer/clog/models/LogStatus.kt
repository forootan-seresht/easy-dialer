package app.arteh.easydialer.clog.models

import app.arteh.easydialer.R

enum class LogStatus(val fullName: String, val icon: Int) {
    All("All", R.drawable.all_logs),
    Incoming("Incoming", R.drawable.call_received),
    Outgoing("Outgoing" , R.drawable.call_made),
    Missed("Missed" , R.drawable.call_missed),
    Rejected("Rejected" , R.drawable.call_rejected),
    Other("Others" , R.drawable.local_phone)
}