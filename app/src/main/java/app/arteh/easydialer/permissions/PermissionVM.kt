package app.arteh.easydialer.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.XiaomiUtilities
import app.arteh.easydialer.main.MainActivity

class PermissionVM : ViewModel() {

    var next by mutableStateOf(false)
    val permissionChecker = PermissionChecker()

    @SuppressLint("InlinedApi")
    val permissions = listOf(
        PermissionRow(
            R.string.notif_permission,
            R.string.notif_permission_desc,
            Manifest.permission.POST_NOTIFICATIONS,
            checkerFunction = permissionChecker::NotificationPermission
        ),
        PermissionRow(
            R.string.read_state_permission,
            R.string.read_state_permission,
            Manifest.permission.READ_PHONE_STATE,
            checkerFunction = permissionChecker::ReadPhoneSPermission
        ),
        PermissionRow(
            R.string.make_call_permission,
            R.string.make_call_permission_desc,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            checkerFunction = permissionChecker::MakeCallPermission
        ),
        PermissionRow(
            R.string.write_contact_permission,
            R.string.write_contact_permission_desc,
            Manifest.permission.WRITE_CONTACTS,
            checkerFunction = permissionChecker::WriteContactPermission
        ),
        PermissionRow(
            R.string.read_contact_permission,
            R.string.read_contact_permission_desc,
            Manifest.permission.READ_CONTACTS,
            checkerFunction = permissionChecker::ReadContactPermission
        ),
        PermissionRow(
            R.string.read_log_permission,
            R.string.read_log_permission_desc,
            Manifest.permission.READ_CALL_LOG,
            checkerFunction = permissionChecker::ReadCallLogPermission
        ),
        PermissionRow(
            R.string.write_log_permission,
            R.string.write_log_permission_desc,
            Manifest.permission.WRITE_CALL_LOG,
            checkerFunction = permissionChecker::WriteCallLogPermission
        ),
        PermissionRow(
            R.string.def_dialer_permission,
            R.string.def_dialer_permission_desc,
            "",
            PermissionType.DefDialer,
            checkerFunction = permissionChecker::isDefaultDialer
        ),
        PermissionRow(
            R.string.miui_overlay_permission,
            R.string.miui_overlay_permission_desc,
            "",
            PermissionType.MiuiOverlay,
            checkerFunction = permissionChecker::isMiuiCanDisplayOverlay
        ),
    )

    fun checkStatus(context: Context): Boolean {
        var flag = true

        permissions.forEachIndexed { index, row ->
            if (row.checkerFunction(context))
                permissions[index].isVisible.value = false
            else {
                permissions[index].isVisible.value = true
                flag = false
            }
        }

        if (flag) next = true

        return flag
    }

    fun goNext(context: Context) {
        if (checkStatus(context)) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)

            (context as Activity).finish()
        }
    }

    fun openMiuiDisplayOverlayPermission(context: Context) {
        try {
            XiaomiUtilities().getPermissionManagerIntent(context)
        } catch (e: Exception) {
            Log.d(
                "LOG_TAG",
                "Cannot open Miui Display Pop-up window while running in background $e"
            )
        }
    }

    fun requestDefaultDialer(context: Context) {
        val roleManager = context.getSystemService(RoleManager::class.java)

        if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            context.startActivity(intent)
        }
    }

    private fun requestDefaultHandler(context: Context) {
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
        intent.putExtra(
            TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
            context.packageName
        )
        if (intent.resolveActivity(context.packageManager) != null)
            context.startActivity(intent)
        else
            throw RuntimeException("Default phone functionality not found")
    }
}