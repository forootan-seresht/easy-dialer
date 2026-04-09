package app.arteh.easydialer.contacts

import android.content.Context
import android.provider.ContactsContract
import androidx.core.net.toUri
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.PhoneType
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.PreferencesManager
import kotlinx.coroutines.flow.Flow

class ContactRP {
    private lateinit var prefs: PreferencesManager
    var contactList = listOf<Contact>()

    lateinit var speedDialMap: Flow<Map<Int, SpeedDialEntry>>
    var lazyKey = 0

    fun initialize(context: Context) {
        prefs = PreferencesManager(context)
        contactList = queryContacts("", context)
        speedDialMap = prefs.loadSpeedDIal()
    }

    fun loadContacts(name: String, context: Context): Map<ContactHeader, List<Contact>> {
        val contactMList = queryContacts(name, context)

        return contactMList.sortedBy { it.name }.groupBy { contact ->
            val firstChar = contact.name.firstOrNull()?.uppercaseChar() ?: '#'

            // Logic to pick a color based on the character
            val headerColor = Holder.colors[firstChar.toInt() % 7]

            ContactHeader(char = firstChar, color = headerColor)
        }
    }

    fun queryContacts(name: String, context: Context): List<Contact> {
        val contactMList = mutableListOf<Contact>()

        val cr = context.contentResolver

        val columns: Array<String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,

            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,

            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
            ContactsContract.PhoneLookup.PHOTO_URI,
        )

        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            columns,
            ContactsContract.Contacts.DISPLAY_NAME + " Like '%$name%'",
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )

        cursor?.use { cursor ->
            val IDIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val accountIndex =
                cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val thumbIndex =
                cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
            val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

            while (cursor.moveToNext()) {

                val type = cursor.getString(accountIndex)
                if (type != null && type.contains("sim", true))
                    continue

                val id = cursor.getLong(IDIndex)
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex).replace(" ", "")
                val thumbURI = cursor.getString(thumbIndex)?.toUri()
                val photoURI = cursor.getString(photoIndex)?.toUri()

                val contact = Contact(id, name, number, thumbURI, photoURI, lazyKey++)
                contactMList.add(contact)
            }
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

        return contactMList
    }

    fun findContactByID(id: Long, context: Context): EditableContact {
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

            ContactsContract.CommonDataKinds.Organization.TITLE,
            ContactsContract.CommonDataKinds.Organization.COMPANY,

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

        cursor?.use { cursor ->
            val phoneList = mutableListOf<ContactPhone>()
            var flag = false

            while (cursor.moveToNext()) {
                val phoneID = cursor.getLong(7)
                val number = cursor.getString(8).replace(" ", "")
                val numberType = when (cursor.getInt(9)) {
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> PhoneType.Home
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> PhoneType.Mobile
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> PhoneType.Work
                    else -> PhoneType.Other
                }

                if (!flag) {
                    flag = true
                    val contactID = cursor.getLong(0)
                    val rawContactID = cursor.getLong(1)

                    val fullName = cursor.getString(2)
                    val firstName = cursor.getString(3) ?: ""
                    val lastName = cursor.getString(4) ?: ""

                    val job = cursor.getString(5) ?: ""
                    val company = cursor.getString(6) ?: ""

                    val photoURI = cursor.getString(10)?.toUri()

                    phoneList.add(ContactPhone(phoneID, number, numberType))

                    contact = EditableContact(
                        contactID, rawContactID, firstName, lastName, job, company, fullName,
                        phones = phoneList.toList(), photoUri = photoURI
                    )
                }
                else if (phoneList.indexOfFirst { it.number == number } == -1)
                    phoneList.add(ContactPhone(phoneID, number, numberType))
            }

            contact = contact.copy(phones = phoneList)
        }

        return contact
    }

    fun getContactByNumber(normalizedNumber: String): Contact? {
        for (contact in contactList)
            if (contact.phone.endsWith(normalizedNumber)) return contact

        return null
    }

    suspend fun updateSpeedDial(newSlot: Int, oldSlot: Int, speedDialEntry: SpeedDialEntry) {
        prefs.saveSpeedDial(newSlot, oldSlot, speedDialEntry)
    }
}