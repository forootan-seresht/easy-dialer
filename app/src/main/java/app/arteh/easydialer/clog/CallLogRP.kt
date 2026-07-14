package app.arteh.easydialer.clog

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import androidx.core.net.toUri
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.clog.models.LogStatus
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.LocaleHelper
import app.arteh.easydialer.utility.Roozh
import app.arteh.easydialer.utility.SimCardHR
import java.time.Instant
import java.time.ZoneId
import kotlin.math.min

class CallLogRP(val context: Context) {

    var lazyKey = 0
    val simCardRP = SimCardHR(context)
    val roozh = Roozh()

    val GSMonthName: List<String> = listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )
    val PSMonthName: List<String> = listOf(
        "فرو",
        "ارد",
        "خرد",
        "تیر",
        "مرد",
        "شهر",
        "مهر",
        "آبا",
        "آذر",
        "دی",
        "بهم",
        "اسف"
    )

    @SuppressLint("Range")
    fun loadCallLog(phone: String, selectedStatus: LogStatus): Map<String, List<Clog>> {
        val logMList = mutableListOf<Clog>()

        val selection = if (selectedStatus == LogStatus.All) null
        else
            when (selectedStatus) {
                LogStatus.Incoming -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}"
                LogStatus.Outgoing -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}"
                LogStatus.Missed -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}"
                LogStatus.Rejected -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE} OR " +
                        "${CallLog.Calls.TYPE} = ${CallLog.Calls.BLOCKED_TYPE}"

                else -> " ${CallLog.Calls.TYPE} != ${CallLog.Calls.INCOMING_TYPE} AND " +
                        " ${CallLog.Calls.TYPE} != ${CallLog.Calls.OUTGOING_TYPE} AND " +
                        " ${CallLog.Calls.TYPE} != ${CallLog.Calls.MISSED_TYPE} AND " +
                        " ${CallLog.Calls.TYPE} != ${CallLog.Calls.BLOCKED_TYPE} AND " +
                        " ${CallLog.Calls.TYPE} != ${CallLog.Calls.REJECTED_TYPE}"
            }

        try {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NORMALIZED_NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.CACHED_NAME
            )

            val sort = CallLog.Calls.DATE + " Desc"
            val allCalls = "content://call_log/calls".toUri()

            val cursor = if (phone.isEmpty())
                context.contentResolver.query(allCalls, projection, selection, null, sort)
            else
                context.contentResolver.query(
                    allCalls, projection,
                    CallLog.Calls.NUMBER + " like ?", arrayOf("%$phone%"), sort
                )

            var currentDate = ""

            if (cursor != null) {
                val count = cursor.count
                val min = min(count, 100)

                for (i in 0..<count) {
                    cursor.moveToPosition(i)

                    val dateTime =
                        getDateTime(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)))

                    if (currentDate != dateTime.first) {
                        if (i > min)
                            break

                        currentDate = dateTime.first
                    }

                    val cachedNumber =
                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NORMALIZED_NUMBER))
                    val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))

                    val contact = Holder.contactRP.getContactByNumber(cachedNumber)
                    val simdID =
                        simCardRP.getSimSlot(cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)))

                    val typeInt = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))

                    val status = when (typeInt) {
                        CallLog.Calls.INCOMING_TYPE -> LogStatus.Incoming
                        CallLog.Calls.OUTGOING_TYPE -> LogStatus.Outgoing
                        CallLog.Calls.MISSED_TYPE -> LogStatus.Missed
                        CallLog.Calls.REJECTED_TYPE, CallLog.Calls.BLOCKED_TYPE -> LogStatus.Rejected
                        else -> LogStatus.Other
                    }

                    logMList.add(
                        Clog(
                            contact, number, status, dateTime.first, dateTime.second,
                            simdID, lazyKey++
                        )
                    )
                }

                cursor.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return logMList.groupBy { clog -> clog.date }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTime(millis: Long): Pair<String, String> {
        val localDateTime =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()

        val time = "${localDateTime.hour}:${localDateTime.minute}"

        if (LocaleHelper.lang == "fa") {
            roozh.gregorianToPersian(
                localDateTime.year,
                localDateTime.monthValue,
                localDateTime.dayOfMonth
            )

            return "${roozh.year} ، ${PSMonthName[roozh.month - 1]} ${roozh.day}" to time
        }
        else
            return "${GSMonthName[localDateTime.monthValue]} ${localDateTime.dayOfMonth}, ${localDateTime.year}" to time
    }
}