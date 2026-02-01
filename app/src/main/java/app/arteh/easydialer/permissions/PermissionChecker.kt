package app.arteh.easydialer.permissions

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import app.arteh.easydialer.R

object PermissionChecker {

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

    fun createNotificationChannel(context: Context) {
        if (NotificationPermission(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}