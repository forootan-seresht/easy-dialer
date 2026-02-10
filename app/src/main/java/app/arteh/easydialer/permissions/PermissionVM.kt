package app.arteh.easydialer.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
        if (notificationPermission(context))
            permissions[0].isVisible.value = false
        else {
            permissions[0].isVisible.value = true
            flag = false
        }

        //for READ_PHONE_STATE
        if (readPhoneSPermission(context))
            permissions[1].isVisible.value = false
        else {
            permissions[1].isVisible.value = true
            flag = false
        }

        //for PROCESS_OUTGOING_CALLS
        if (makeCallPermission(context))
            permissions[2].isVisible.value = false
        else {
            permissions[2].isVisible.value = true
            flag = false
        }

        //for WRITE_CONTACTS
        if (writeContactPermission(context))
            permissions[3].isVisible.value = false
        else {
            permissions[3].isVisible.value = true
            flag = false
        }

        //for Read_CONTACTS
        if (readContactPermission(context))
            permissions[4].isVisible.value = false
        else {
            permissions[4].isVisible.value = true
            flag = false
        }

        //for Read call log
        if (readCallLogPermission(context))
            permissions[5].isVisible.value = false
        else {
            permissions[5].isVisible.value = true
            flag = false
        }

        //for Write call log
        if (writeCallLogPermission(context))
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

    fun notificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        else true
    }

    fun createNotificationChannel(context: Context) {
        if (notificationPermission(context)) {
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            if (notificationManager.getNotificationChannel("CHANNEL_ID") != null) return

            val name: CharSequence = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_description)
            val importance: Int = NotificationManager.IMPORTANCE_DEFAULT

            val channel: NotificationChannel = NotificationChannel("10", name, importance)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun readCallLogPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }

    fun readPhoneSPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun makeCallPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
    }

    fun writeCallLogPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }

    fun writeContactPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun readContactPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
}