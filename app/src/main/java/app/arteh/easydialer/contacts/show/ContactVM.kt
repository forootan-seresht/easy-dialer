package app.arteh.easydialer.contacts.show

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.PhoneType
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.dialer.DialerHR
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.SimCardHR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ContactVM(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    val contactID: Long = savedStateHandle.get<Long>("id") ?: error("Contact ID is required")

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private var _showState = MutableStateFlow(ShowState())
    val showState = _showState.asStateFlow()

    var selectedPhoneIDX: Int = 0

    val simCardHR = SimCardHR(application)
    val dialerHR = DialerHR(simCardHR, application, ::saveDefaultSim, ::saveDefaultNumber)


    init {
        reloadContact()
        viewModelScope.launch {
            Holder.contactRP.speedDialMap.collect { map ->
                var slot: Int = -1
                for (entry in map)
                    if (entry.value.contactId == contactID) {
                        slot = entry.key
                        break
                    }

                _uiState.update { it.copy(speedSlot = slot, speedDialMap = map) }
            }
        }
    }

    fun onAction(action: ContactUIAction) {
        when (action) {
            ContactUIAction.ShowBlocK -> _showState.update { it.copy(showBlock = true) }
            is ContactUIAction.MakeCall -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(
                    ContactAction.Call,
                    state.defaultSimID,
                    state.phones,
                    action.index
                )
            }

            is ContactUIAction.SendSMS -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(
                    ContactAction.SMS,
                    state.defaultSimID,
                    state.phones,
                    action.index
                )
            }

            ContactUIAction.ShowDelete -> _showState.update { it.copy(showDelete = true) }
            ContactUIAction.ShowMakeCall -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(ContactAction.Call, state.defaultSimID, state.phones)
            }

            ContactUIAction.ShowSendSMS -> {
                val state = uiState.value.contact!!
                dialerHR.makeAction(ContactAction.SMS, state.defaultSimID, state.phones)
            }

            ContactUIAction.AddFavorite -> addToFavorite()
            is ContactUIAction.DeleteContact -> deleteContact(action.context)
            is ContactUIAction.UpdateSpeedSlot -> updateSpeedSlot(action.slot)
            ContactUIAction.ReloadContact -> reloadContact()
            ContactUIAction.BlockNumbers -> blockNumbers()
            ContactUIAction.OpenEmail -> sendEmail()
            ContactUIAction.ReloadData -> reloadContact()
            is ContactUIAction.ShareContact -> shareContact(action.shareChecks, action.asFile)
            ContactUIAction.ShowShareContact -> _showState.update { it.copy(showShare = true) }
        }
    }

    fun shareContact(shareChecks: ShareChecks, asFile: Boolean) {
        val context = getApplication<Application>()
        val contact = uiState.value.contact!!

        if (asFile) {
            val message = makeText(uiState.value.contact!!, shareChecks, context)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Text from Easy Dialer ", message)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                context,
                context.getString(R.string.info_copied_to_the_clipboard), Toast.LENGTH_SHORT
            ).show()
        }
        else viewModelScope.launch(Dispatchers.IO) {
            val text = createVCard(contact, shareChecks)
            val uri = saveVCardToFile(context, contact.fullName.replace(" ", "_"), text)
            withContext(Dispatchers.Main) { shareVCard(context, uri) }
        }
    }

    fun shareVCard(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/x-vcard"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Contact"))
    }

    fun saveVCardToFile(context: Context, fileName: String, vcard: String): Uri {
        val file = File(context.filesDir, "$fileName.vcf")
        file.writeText(vcard)
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
    }

    fun sendEmail() {
        val context = getApplication<Application>()

        try {
            val address = uiState.value.contact!!.email

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:${Uri.encode(address)}".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.no_app_found_to_send_email),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun blockNumbers() {
        dismissPopup()

        viewModelScope.launch {
            val phoneNumbers = uiState.value.contact!!.phones.toMutableList()
            val context = getApplication<Application>()

            phoneNumbers.forEachIndexed { index, phone ->
                Holder.contactRP.blockNumber(context, phone.number)

                phoneNumbers[index] = phoneNumbers[index].copy(isBLocked = true)
            }

            _uiState.update { it.copy(contact = it.contact!!.copy(phones = phoneNumbers)) }
        }
    }

    fun saveDefaultSim(index: Int) {
        val id = simCardHR.simCardList[index].id

        _uiState.update { it.copy(contact = it.contact!!.copy(defaultSimID = id)) }

        viewModelScope.launch(Dispatchers.IO) {
            Holder.contactRP.saveDefaultSim(contactID, id)
        }
    }

    fun saveDefaultNumber(index: Int) {
        val phones = uiState.value.contact!!.phones.toMutableList()

        phones[index] = phones[index].copy(isDefault = true)

        _uiState.update { it.copy(contact = it.contact!!.copy(phones = phones)) }

        viewModelScope.launch(Dispatchers.IO) {
            Holder.contactRP.saveDefaultNumber(contactID, phones[index].phoneID)
        }
    }

    fun reloadContact() {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = Holder.contactRP.findContactByID(contactID, getApplication())

            var isBLocked = false
            for (phone in contact.phones)
                if (phone.isBLocked) {
                    isBLocked = true
                    break
                }

            _uiState.update { it.copy(contact = contact, isBLocked = isBLocked) }
        }
    }

    fun showSpeedDial(phoneIDX: Int) {
        selectedPhoneIDX = phoneIDX

        _showState.update { it.copy(showSpeedList = true) }
    }

    fun deleteContact(context: Context) {
        val uri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactID
        )

        context.contentResolver.delete(uri, null, null)

        (context as Activity).finish()
    }

    fun updateSpeedSlot(slot: Int) {
        viewModelScope.launch {
            val contactPhone = uiState.value.contact!!.phones[selectedPhoneIDX]

            var oldSlot = -1

            for (entry in uiState.value.speedDialMap) {
                if (entry.value.contactId == contactID) {
                    oldSlot = entry.key
                    break
                }
            }

            val entry = SpeedDialEntry(
                contactID,
                contactPhone.number,
                uiState.value.contact!!.fullName
            )

            Holder.contactRP.updateSpeedDial(slot, oldSlot, entry)
        }

        dismissPopup()
    }

    fun dismissPopup() {
        _showState.update {
            it.copy(showDelete = false, showSpeedList = false, showBlock = false, showShare = false)
        }
    }

    fun addToFavorite() {
        val isStarred = uiState.value.contact!!.isStarred
        Holder.contactRP.setFavorite(getApplication(), contactID, !isStarred)

        _uiState.update { it.copy(contact = it.contact!!.copy(isStarred = !isStarred)) }
    }

    fun makeText(contact: EditableContact, shareChecks: ShareChecks, context: Context): String {
        val builder = StringBuilder()
        if (shareChecks.name)
            builder.append(context.getString(R.string.name)).append(contact.fullName).append("\n")

        if (shareChecks.phones)
            contact.phones.forEachIndexed { index, phone ->
                builder.append("${context.getString(R.string.phone)} $index: ").append(phone.number)
                    .append("(${context.getString(phone.type.fullName)})").append("\n")
            }

        if (shareChecks.email)
            builder.append("Email: ").append(contact.email).append("\n")

        if (shareChecks.jobCompany)
            builder.append(context.getString(R.string.company_title))
                .append("${contact.company} - ${contact.job}")
                .append("\n")

        if (shareChecks.note)
            builder.append("${context.getString(R.string.note)}: ").append(contact.note)
                .append("\n")

        return builder.toString()
    }

    fun createVCard(contact: EditableContact, shareChecks: ShareChecks): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCARD\n")
        sb.append("VERSION:3.0\n")
        sb.append("FN:${contact.fullName}\n")

        // Organization (company)
        if (shareChecks.jobCompany) {
            sb.append("ORG:${contact.company}\n")
            sb.append("TITLE:${contact.job}\n")
        }

        // Phones
        if (shareChecks.phones)
            contact.phones.forEach { phone ->
                val type = when (phone.type) {
                    PhoneType.Mobile -> "CELL"
                    PhoneType.Home -> "HOME"
                    PhoneType.Work -> "WORK"
                    PhoneType.Other -> "OTHER"
                }
                sb.append("TEL;TYPE=$type:${phone.number}\n")
            }

        // Emails (multiple allowed)
        if (shareChecks.email)
            sb.append("EMAIL:${contact.email}\n")

        // Note
        if (shareChecks.note)
            sb.append("NOTE:${contact.note}\n")

        sb.append("END:VCARD\n")
        return sb.toString()
    }
}