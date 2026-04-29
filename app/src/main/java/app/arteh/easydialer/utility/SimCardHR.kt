package app.arteh.easydialer.utility

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import app.arteh.easydialer.clog.models.SimCard

class SimCardHR(context: Context) {
    val simCardList = mutableListOf<SimCard>()

    init {
        fetchSimCards(context)
    }

    /** because we check/get permissions at the beginning of app, permission is granted in most of the time*/
    fun fetchSimCards(context: Context) {
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                as SubscriptionManager
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        val permissionStatus = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        )

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            val accounts = telecom.callCapablePhoneAccounts

            if (subManager.getActiveSubscriptionInfoCount() > 0) {
                val localList = subManager.getActiveSubscriptionInfoList() ?: emptyList()

                for (info in localList)
                    simCardList.add(
                        SimCard(
                            info.subscriptionId,
                            info.displayName.toString(),
                            info.carrierName.toString(),
                            telecom.getPhoneAccount(accounts[0]).accountHandle.id,
                        )
                    )
            }
        }
    }

    fun getSimSlot(accountID: String): Int {
        if (simCardList.size == 1) return 1

        if (accountID == simCardList[0].accountID) return 1

        if (accountID == simCardList[1].accountID) return 2

        try {
            val id = accountID.toInt()
            if (id in 1..<5) return id
        } catch (e: NumberFormatException) {
        }
        return -1
    }
}