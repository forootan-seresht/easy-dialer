package app.arteh.easydialer.calling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.InCallService
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import app.arteh.easydialer.R
import app.arteh.easydialer.calling.CallActivity
import app.arteh.easydialer.calling.CallInfo
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyInCallService : InCallService() {

    private val notificationId = 123
    private val channelId = "incoming_call_channel"

    private var ringtone: Ringtone? = null

    companion object {
        private val _callState = MutableStateFlow<CallInfo?>(null)
        val callState: StateFlow<CallInfo?> = _callState

        // We keep a reference to the service itself
        var instance: MyInCallService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        updateFlow(call)
        call.registerCallback(callCallback)

        if (call.state == Call.STATE_RINGING) {
            startRingtone()
            showCallNotification(call)
        }
        else {
            // Launch call UI
            val intent = Intent(this, CallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            Handler(Looper.getMainLooper()).post {
                startActivity(intent)
            }
        }
    }

    override fun onCallRemoved(call: Call) {
        call.unregisterCallback(callCallback)
        updateFlow(null)
        stopRingtone()

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        super.onCallRemoved(call)
    }

    private fun startRingtone() {
        if (ringtone?.isPlaying == true) return

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uri)
        ringtone?.isLooping = true
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    private fun showCallNotification(call: Call) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_description)
            // Set sound to null because we handle it manually via RingtoneManager
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)

        val number = call.details.handle?.schemeSpecificPart ?: getString(R.string.unknown)
        val contact = Holder.contactRP.findContactByNumber(this, number.takeLast(9))
        val contactName = contact?.name ?: getString(R.string.unknown)

        val intent = Intent(this, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ANSWER
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            this, 1, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_REJECT
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            this, 2, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val personBuilder = Person.Builder()
            .setName(contactName)
            .setImportant(true)

        contact?.photoUri?.let { uri ->
            val bitmap = Holder.loadBitmapUri(this, uri)
            bitmap?.let { bitmap ->
                val circularBitmap = Holder.getCircularBitmap(bitmap)
                personBuilder.setIcon(IconCompat.createWithBitmap(circularBitmap))
            }
        }

        val person = personBuilder.build()

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.call)
            .setContentTitle(getString(R.string.incoming))
            .setContentText(number)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSound(null) // Explicitly silence the builder
            .setFullScreenIntent(pendingIntent, true)
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    person,
                    rejectPendingIntent,
                    answerPendingIntent
                )
            )
            .addPerson(person)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            updateFlow(call)

            if (state == Call.STATE_ACTIVE || state == Call.STATE_DISCONNECTED) {
                stopRingtone()
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }

    fun updateFlow(call: Call?) {
        _callState.value = if (call != null)
            CallInfo(
                call,
                number = call.details.handle?.schemeSpecificPart ?: getString(R.string.unknown),
                state = call.state
            )
        else null
    }
}