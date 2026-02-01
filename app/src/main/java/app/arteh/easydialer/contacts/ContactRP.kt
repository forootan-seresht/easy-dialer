package app.arteh.easydialer.contacts

import android.content.Context
import android.provider.ContactsContract
import androidx.core.net.toUri
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.show.Contact
import app.arteh.easydialer.contacts.speed.PreferencesManager
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import kotlinx.coroutines.flow.Flow

class ContactRP(val context: Context) {
    var contactMList = mutableListOf<Contact>()
    val prefs = PreferencesManager(context)

    var speedDialMap: Flow<Map<Int, SpeedDialEntry>> = prefs.loadSpeedDIal()
    var lazyKey = 0

    fun loadContacts() {
        contactMList = mutableListOf()

        val cr = context.contentResolver

        val columns: Array<String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,

            ContactsContract.RawContacts.ACCOUNT_NAME
        )

        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns, null, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )

        if (cursor != null)
            try {
                while (cursor.moveToNext()) {
                    val type = cursor.getString(5)
                    if (type != null && type.contains("sim", true))
                        continue

                    val id = cursor.getLong(0)
                    val name = cursor.getString(1)
                    val number = cursor.getString(2).replace(" ", "")
                    val date = cursor.getLong(3)
                    val photoURI = cursor.getString(4)?.toUri()

                    val contact = Contact(id, name, number, date, photoURI, lazyKey++)
                    contactMList.add(contact)
                }
            } finally {
                cursor.close()
            }

        //find and delete repeated phone
        var count = contactMList.size
        var j = 0
        while (j < count) {
            if (j + 1 < count)
                if (contactMList[j].phone == contactMList[j + 1].phone) {
                    contactMList.removeAt(j + 1)
                    j--
                    count--
                }
            j++
        }
    }

    fun findContactByID(id: Long): EditableContact {
        val cr = context.contentResolver
        var contact = EditableContact()

        val columns: Array<String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,

            // Full display name
            ContactsContract.Contacts.DISPLAY_NAME,

            // Structured name parts
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,

            // Phone info
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,

            ContactsContract.PhoneLookup.PHOTO_URI
        )

        val columnID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID

        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns,
            "$columnID = ?", arrayOf(id.toString()), null
        )

        if (cursor != null)
            try {
                val phoneList = mutableListOf<ContactPhone>()
                var flag = false

                while (cursor.moveToNext()) {
                    val phoneID = cursor.getLong(5)
                    val number = cursor.getString(6).replace(" ", "")
                    val numberType = cursor.getInt(7)

                    if (!flag) {
                        flag = true
                        val contactID = cursor.getLong(0)
                        val rawContactID = cursor.getLong(1)

                        val fullName = cursor.getString(2)
                        val firstName = cursor.getString(3) ?: ""
                        val lastName = cursor.getString(4) ?: ""

                        val photoURI = cursor.getString(8)?.toUri()

                        phoneList.add(ContactPhone(phoneID, number, numberType))

                        contact = EditableContact(
                            contactID, rawContactID,
                            firstName, lastName, fullName,
                            phoneList.toList(),
                            photoURI
                        )
                    }
                    else if (phoneList.indexOfFirst { it.number == number } == -1)
                        phoneList.add(ContactPhone(phoneID, number, numberType))
                }

                contact = contact.copy(phones = phoneList)

            } finally {
                cursor.close()
            }

        return contact
    }

    fun getContactName(number: String): Pair<String, String> {
        for (contact in contactMList)
            if (contact.phone == number) return (contact.name to contact.phone)

        return ("" to "")
    }

    suspend fun updateSpeedDial(newSlot: Int, oldSlot: Int, speedDialEntry: SpeedDialEntry) {
        prefs.saveSpeedDial(newSlot, oldSlot, speedDialEntry)
    }
}