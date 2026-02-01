package app.arteh.easydialer.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.main.MainActivity

class PermissionVM() : ViewModel() {

    var next by mutableStateOf(false)

    data class PermissionRow(
        val title: Int,
        val body: Int,
        val permission: String,
        var isVisible: MutableState<Boolean> = mutableStateOf(false)
    )

    @SuppressLint("InlinedApi")
    val permissions = listOf<PermissionRow>(
        PermissionRow(
            R.string.notif_permission,
            R.string.notif_permission_desc,
            Manifest.permission.POST_NOTIFICATIONS,
        ),
        PermissionRow(
            R.string.read_state_permission,
            R.string.read_state_permission,
            Manifest.permission.READ_PHONE_STATE,
        ),
        PermissionRow(
            R.string.make_call_permission,
            R.string.make_call_permission_desc,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
        ),
        PermissionRow(
            R.string.write_contact_permission,
            R.string.write_contact_permission_desc,
            Manifest.permission.WRITE_CONTACTS,
        ),
        PermissionRow(
            R.string.read_contact_permission,
            R.string.read_contact_permission_desc,
            Manifest.permission.READ_CONTACTS,
        ),
        PermissionRow(
            R.string.read_log_permission,
            R.string.read_log_permission_desc,
            Manifest.permission.READ_CALL_LOG,
        ),
        PermissionRow(
            R.string.write_log_permission,
            R.string.write_log_permission_desc,
            Manifest.permission.WRITE_CALL_LOG,
        ),
    )

    fun checkStatus(context: Context): Boolean {

        var flag = true

        //for notif
        if (PermissionChecker.NotificationPermission(context))
            permissions[0].isVisible.value = false
        else {
            permissions[0].isVisible.value = true
            flag = false
        }

        //for READ_PHONE_STATE
        if (PermissionChecker.ReadPhoneSPermission(context))
            permissions[1].isVisible.value = false
        else {
            permissions[1].isVisible.value = true
            flag = false
        }

        //for PROCESS_OUTGOING_CALLS
        if (PermissionChecker.MakeCallPermission(context))
            permissions[2].isVisible.value = false
        else {
            permissions[2].isVisible.value = true
            flag = false
        }

        //for WRITE_CONTACTS
        if (PermissionChecker.WriteContactPermission(context))
            permissions[3].isVisible.value = false
        else {
            permissions[3].isVisible.value = true
            flag = false
        }

        //for Read_CONTACTS
        if (PermissionChecker.ReadContactPermission(context))
            permissions[4].isVisible.value = false
        else {
            permissions[4].isVisible.value = true
            flag = false
        }

        //for Read call log
        if (PermissionChecker.ReadCallLogPermission(context))
            permissions[5].isVisible.value = false
        else {
            permissions[5].isVisible.value = true
            flag = false
        }

        //for Write call log
        if (PermissionChecker.WriteCallLogPermission(context))
            permissions[6].isVisible.value = false
        else {
            permissions[6].isVisible.value = true
            flag = false
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
}