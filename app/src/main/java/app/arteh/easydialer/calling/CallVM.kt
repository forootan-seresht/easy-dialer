package app.arteh.easydialer.calling

import android.app.Application
import android.media.Ringtone
import android.media.RingtoneManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.calling.models.CallState
import app.arteh.easydialer.calling.models.UIState
import app.arteh.easydialer.calling.service.MyInCallService
import app.arteh.easydialer.contacts.ContactRP
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallVM(application: Application) : AndroidViewModel(application) {

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private var ringtone: Ringtone? = null
    private lateinit var call: Call

    val rp = ContactRP(application)

    var isFirstTime = true

    init {
        viewModelScope.launch {
            MyInCallService.callState.collect { info ->
                if (info != null) {
                    if (isFirstTime) {
                        call = info.call

                        _uiState.update { it.copy(phoneNumber = info.number) }

                        getContact(info.number.takeLast(9))
                    }

                    when (info.state) {
                        Call.STATE_RINGING -> {
                            _uiState.update { it.copy(state = CallState.Incoming) }
                            startRingtone()
                        }

                        Call.STATE_ACTIVE,
                        Call.STATE_DISCONNECTED -> {
                            _uiState.update { it.copy(state = CallState.Rejected) }
                            stopRingtone()
                        }
                    }
                }
            }
        }
    }

    fun getContact(normalizedNumber: String) {
        val contact = rp.getContactByNumber(normalizedNumber)
        _uiState.update { it.copy(contact = contact) }
    }

    private fun startRingtone() {
        if (ringtone?.isPlaying == true) return

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(getApplication(), uri)
        ringtone?.isLooping = true
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    fun answer() {
        call.answer(VideoProfile.STATE_AUDIO_ONLY)
        speaker(true)
    }

    fun reject() {
        call.disconnect()
    }

    fun hangUp() {
        call.disconnect()
    }

    fun mute(enabled: Boolean) {
        MyInCallService.instance?.setMuted(enabled)
    }

    fun speaker(enabled: Boolean) {
        MyInCallService.instance?.setAudioRoute(
            if (enabled)
                CallAudioState.ROUTE_SPEAKER
            else
                CallAudioState.ROUTE_EARPIECE
        )
    }
}