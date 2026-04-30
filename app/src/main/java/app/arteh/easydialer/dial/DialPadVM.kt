package app.arteh.easydialer.dial

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.R
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DialPadVM(application: Application) : AndroidViewModel(application) {

    private val _number = MutableStateFlow("")
    val number: StateFlow<String> = _number

    fun onNumberClick(digit: String) {
        _number.value += digit
    }

    fun onBackspace() {
        _number.value = _number.value.dropLast(1)
    }

    @SuppressLint("MissingPermission")
    fun makeCall() {
        val context = getApplication() as Context
        val phoneNumber = _number.value

        if (phoneNumber.isNotEmpty()) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            val uri = Uri.fromParts("tel", phoneNumber, null)

            telecomManager.placeCall(uri, Bundle())
        }
    }

    fun onNumberLongPress(digit: String) {
        val context = getApplication() as Context

        viewModelScope.launch {
            val map = Holder.contactRP.speedDialMap.firstOrNull()
            if (map != null) {
                val phoneNumber = map[digit.toInt()]?.phoneNumber
                if (phoneNumber != null) {
                    _number.value = phoneNumber
                    makeCall()
                }
                else
                    Toast.makeText(
                        context, context.getString(R.string.no_speed_dial1) + "\n" +
                                context.getString(R.string.no_speed_dial2), Toast.LENGTH_LONG
                    ).show()
            }
        }
    }
}