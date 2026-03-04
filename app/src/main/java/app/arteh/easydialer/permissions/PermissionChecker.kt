package app.arteh.easydialer.permissions

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import app.arteh.easydialer.utility.XiaomiUtilities

class PermissionChecker {
    fun NotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        else true
    }

    fun ReadCallLogPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }

    fun ReadPhoneSPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun MakeCallPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
    }

    fun WriteCallLogPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }

    fun WriteContactPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun ReadContactPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun isDefaultDialer(context: Context): Boolean {
        val roleManager = context.getSystemService(RoleManager::class.java)

        return roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    }

    fun isMiuiCanDisplayOverlay(context: Context): Boolean {
        val utility = XiaomiUtilities()
        if (utility.isMIUI())
            return utility.isCustomPermissionGranted(context, utility.OP_BACKGROUND_START_ACTIVITY)
        return true
    }

    private fun isDefaultTelecom(context: Context): Boolean {
        val telecomManager = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        return context.packageName == telecomManager.getDefaultDialerPackage()
    }
}