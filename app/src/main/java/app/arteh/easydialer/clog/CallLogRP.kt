package app.arteh.easydialer.clog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.clog.models.LogStatus
import app.arteh.easydialer.clog.models.SimCard
import app.arteh.easydialer.utility.Holder
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.min

class CallLogRP(val context: Context) {

    lateinit var sim1: SimCard
    lateinit var sim2: SimCard

    var lazyKey = 0

    fun getSimCards() {
        val localSubscriptionManager = SubscriptionManager.from(context)
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        )
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                val lis = tm.callCapablePhoneAccounts

                val localList = localSubscriptionManager.getActiveSubscriptionInfoList()

                val simInfo1 = localList!![0]
                val simInfo2 = localList[1]

                sim1 = SimCard(
                    simInfo1.subscriptionId,
                    simInfo1.displayName.toString(),
                    simInfo1.carrierName.toString(),
                    tm.getPhoneAccount(lis[0]).accountHandle.id
                )
                sim2 = SimCard(
                    simInfo2.subscriptionId,
                    simInfo2.displayName.toString(),
                    simInfo2.carrierName.toString(),
                    tm.getPhoneAccount(lis[1]).accountHandle.id
                )

                try {
                    val jsonObject = JSONObject()
                    jsonObject.put("type", "SIMS")
                    jsonObject.put("sim1_id", simInfo1.subscriptionId)
                    jsonObject.put("sim1_name", simInfo1.displayName)
                    jsonObject.put("sim1_cc", simInfo1.carrierName)
                    jsonObject.put("sim2_id", simInfo2.subscriptionId)
                    jsonObject.put("sim2_name", simInfo2.displayName)
                    jsonObject.put("sim2_cc", simInfo2.carrierName)

                } catch (e: JSONException) {

                }
            }
    }

    private fun getSimSlot(accountID: String): Int {
        if (!::sim2.isInitialized) return 1
        if (::sim1.isInitialized && accountID == sim1.account_id) return 1
        if (::sim2.isInitialized && accountID == sim2.account_id) return 2
        try {
            val id = accountID.toInt()
            if (id in 1..<5) return id
        } catch (e: NumberFormatException) {
        }
        return -1
    }

    @SuppressLint("Range")
    fun loadCallLog(phone: String, selectedStatus: LogStatus): Map<String, List<Clog>> {
        val logMList = mutableListOf<Clog>()

        val selection = if (selectedStatus == LogStatus.All) null
        else
            when (selectedStatus) {
                LogStatus.Incoming -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}"
                LogStatus.Outgoing -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}"
                LogStatus.Missed -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}"
                LogStatus.Rejected -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}"
                else -> " ${CallLog.Calls.TYPE} != ${CallLog.Calls.INCOMING_TYPE} AND " +
                        " ${CallLog.Calls.TYPE} != ${CallLog.Calls.OUTGOING_TYPE} AND " +
                        " ${CallLog.Calls.TYPE} != ${CallLog.Calls.MISSED_TYPE} AND " +
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
                        getSimSlot(cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)))

                    val status = when (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))) {
                        CallLog.Calls.INCOMING_TYPE -> LogStatus.Incoming
                        CallLog.Calls.OUTGOING_TYPE -> LogStatus.Outgoing
                        CallLog.Calls.MISSED_TYPE -> LogStatus.Missed
                        CallLog.Calls.REJECTED_TYPE -> LogStatus.Rejected
                        else -> LogStatus.Other
                    }


                    logMList.add(
                        Clog(
                            contact,
                            number, status, dateTime.first, dateTime.second, simdID,
                            lazyKey++
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
        val date = Date()
        date.time = millis

        val fDate = SimpleDateFormat("yyyy-MMM-d HH:MM").format(date).split(" ")
        val splitDate = fDate[0].split("-")

        return "${splitDate[1]} ${splitDate[2]}, ${splitDate[0]}" to fDate[1]
    }
}