package app.arteh.easydialer.calling.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.VideoProfile

class CallActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_ANSWER = "app.arteh.easydialer.ACTION_ANSWER"
        const val ACTION_REJECT = "app.arteh.easydialer.ACTION_REJECT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val call = MyInCallService.callState.value?.call ?: return

        when (intent.action) {
            ACTION_ANSWER -> call.answer(VideoProfile.STATE_AUDIO_ONLY)
            ACTION_REJECT -> call.disconnect()
        }
    }
}