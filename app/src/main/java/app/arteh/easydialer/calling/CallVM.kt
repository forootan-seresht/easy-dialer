package app.arteh.easydialer.calling

import android.app.Application
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.calling.service.MyInCallService
import app.arteh.easydialer.db.AppDatabase
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallVM(application: Application) : AndroidViewModel(application) {

    private var _uiState = MutableStateFlow(CallUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var call: Call

    var isFirstTime = true

    init {
        viewModelScope.launch {
            Holder.contactRP.initialize(
                application,
                AppDatabase.getInstance(application),
                viewModelScope
            )

            MyInCallService.callState.collect { info ->
                if (info != null) {
                    if (isFirstTime) {
                        call = info.call
                        isFirstTime = false
                        _uiState.update { it.copy(phoneNumber = info.number) }

                        getContact(info.number.takeLast(9))
                    }

                    when (info.state) {
                        Call.STATE_RINGING -> {
                            _uiState.update { it.copy(state = CallState.Incoming) }
                        }

                        Call.STATE_ACTIVE -> {
                            _uiState.update { it.copy(state = CallState.Talking) }
                        }

                        Call.STATE_DISCONNECTED -> {
                            _uiState.update { it.copy(state = CallState.Rejected) }
                        }
                    }
                }
            }
        }
    }

    fun onAction(action: CallAction) {
        when (action) {
            CallAction.Answer -> answer()
            CallAction.Reject -> reject()
            CallAction.HangUp -> hangUp()
            CallAction.ToggleMute -> toggleMute()
            CallAction.ToggleSpeaker -> toggleSpeaker()
            CallAction.ShowDialPad -> showDialPad()
            CallAction.HideDialPad -> hideDialPad()
            is CallAction.SendDtmf -> sendDtmf(action.digit)
        }
    }

    private fun getContact(normalizedNumber: String) {
        val contact = Holder.contactRP.getContactByNumber(normalizedNumber)
        _uiState.update { it.copy(contact = contact) }
    }

    private fun answer() {
        if (::call.isInitialized) {
            call.answer(VideoProfile.STATE_AUDIO_ONLY)
        }
    }

    private fun reject() {
        if (::call.isInitialized) {
            call.disconnect()
        }
    }

    private fun hangUp() {
        if (::call.isInitialized) {
            call.disconnect()
        }
    }

    private fun toggleMute() {
        val enable = uiState.value.isMute

        MyInCallService.instance?.setMuted(!enable)
        _uiState.update { it.copy(isMute = !enable) }
    }

    private fun toggleSpeaker() {
        val enable = uiState.value.isSpeaker

        MyInCallService.instance?.setAudioRoute(
            if (!enable)
                CallAudioState.ROUTE_SPEAKER
            else
                CallAudioState.ROUTE_EARPIECE
        )

        _uiState.update { it.copy(isSpeaker = !enable) }
    }

    private fun showDialPad() {
        _uiState.update { it.copy(showDialPad = true) }
    }

    private fun hideDialPad() {
        _uiState.update { it.copy(showDialPad = false) }

        stopDtmf()
    }

    private fun sendDtmf(digit: String) {
        if (::call.isInitialized) {
            call.playDtmfTone(digit.toCharArray()[0])
        }
    }

    private fun stopDtmf() {
        if (::call.isInitialized) {
            call.stopDtmfTone()
        }
    }

    override fun onCleared() {
        stopDtmf()

        super.onCleared()
    }
}