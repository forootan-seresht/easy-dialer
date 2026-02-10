package app.arteh.easydialer.calling.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.InCallService
import app.arteh.easydialer.calling.CallActivity
import app.arteh.easydialer.calling.models.CallInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyInCallService : InCallService() {

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

        // Launch call UI
        val intent = Intent(this, CallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Handler(Looper.getMainLooper()).post {
            startActivity(intent)
        }
    }

    override fun onCallRemoved(call: Call) {
        call.unregisterCallback(callCallback)
        updateFlow(null)

        super.onCallRemoved(call)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            updateFlow(call)
        }
    }

    fun updateFlow(call: Call?) {
        _callState.value = if (call != null)
            CallInfo(
                call,
                number = call.details.handle?.schemeSpecificPart ?: "Unknown",
                state = call.state
            )
        else null
    }
}