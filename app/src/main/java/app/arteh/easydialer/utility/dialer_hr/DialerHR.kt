package app.arteh.easydialer.utility.dialer_hr

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.core.net.toUri
import app.arteh.easydialer.contacts.edit.ContactPhone
import app.arteh.easydialer.contacts.show.ContactAction
import app.arteh.easydialer.utility.SimCardHR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DialerHR(
    val simCardHR: SimCardHR,
    val context: Context,
    private val saveDefaultSim: (index: Int) -> Unit,
    private val saveDefaultNumber: (index: Int) -> Unit
) {
    private var _showState = MutableStateFlow(ShowState())
    val showState = _showState.asStateFlow()

    var currentAction = ContactAction.None
    var currentNumbers = emptyList<ContactPhone>()
    var currentNumber = ""
    var activeSim = 0


    //if contact has more than 1 phone number
    fun makeAction(
        action: ContactAction,
        defaultSimID: Int,
        numbers: List<ContactPhone>,
        phoneNumberIndex: Int = -1
    ) {
        currentAction = action
        currentNumbers = numbers

        if (defaultSimID == -1) {
            if (has2Sims(defaultSimID)) return
        }
        else activeSim = defaultSimID

        var phoneIndex = phoneNumberIndex

        if (phoneIndex == -1)
            phoneIndex = getDefaultNumber(numbers)

        if (phoneIndex == -1) return

        val defaultNumber = numbers[phoneIndex].number

        if (action == ContactAction.Call)
            dialNumber(defaultNumber)
        else openSmsDefault(defaultNumber)
    }

    //if contact has only 1 number
    fun makeAction(action: ContactAction, defaultSimID: Int, phoneNumber: String) {
        activeSim = defaultSimID
        currentAction = action
        currentNumber = phoneNumber

        if (defaultSimID == -1)
            if (has2Sims(defaultSimID)) return

        if (action == ContactAction.Call)
            dialNumber(phoneNumber)
        else openSmsDefault(phoneNumber)
    }

    @SuppressLint("MissingPermission")
    fun dialNumber(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            val uri = Uri.fromParts("tel", phoneNumber, null)

            telecomManager.placeCall(uri, Bundle())
        }
    }

    fun openSmsDefault(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:${Uri.encode(phoneNumber)}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }

    fun has2Sims(defaultSimID: Int): Boolean {
        if (simCardHR.simCardList.size > 1) {
            if (defaultSimID != -1)
                simCardHR.simCardList.forEachIndexed { index, simCard ->
                    if (simCard.id == defaultSimID) {
                        activeSim = index
                        return false
                    }
                }

            _showState.update { it.copy(showMyNumbers = true) }
            return true
        }

        return false
    }

    fun getDefaultNumber(numbers: List<ContactPhone>): Int {
        if (numbers.size > 1) {
            numbers.forEachIndexed { index, phone ->
                if (phone.isDefault) return index
            }

            _showState.update { it.copy(showContactNumbers = true) }
            return -1
        }
        else return 0
    }

    fun selectSim(index: Int, remember: Boolean) {
        activeSim = index

        dismissPopup()

        if (remember) saveDefaultSim(index)

        if (currentAction != ContactAction.None) {
            if (currentNumber != "")
                makeAction(currentAction, activeSim, currentNumber)
            else
                makeAction(currentAction, activeSim, currentNumbers)
        }
    }

    fun selectNumber(index: Int, remember: Boolean) {
        if (remember)
            saveDefaultNumber(index)

        dismissPopup()

        if (currentAction == ContactAction.Call)
            dialNumber(currentNumbers[index].number)
        else openSmsDefault(currentNumbers[index].number)
    }

    fun dismissPopup() {
        _showState.update { it.copy(showMyNumbers = false, showContactNumbers = false) }
    }

    fun reset() {
        currentNumber = ""
        currentNumbers = emptyList()
        currentAction = ContactAction.None
    }
}