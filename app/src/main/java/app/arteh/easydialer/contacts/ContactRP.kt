package app.arteh.easydialer.contacts

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BlockedNumberContract
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
            //is favorite
            ContactsContract.Contacts.STARRED,

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

            val idIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val rawIDIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val starredIndex = cursor.getColumnIndex(ContactsContract.Contacts.STARRED)
            val phoneIDIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val phoneTypeIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

            while (cursor.moveToNext()) {
                val phoneID = cursor.getLong(phoneIDIndex)
                val number = cursor.getString(numberIndex).replace(" ", "")
                val numberType = when (cursor.getInt(phoneTypeIndex)) {
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> PhoneType.Home
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> PhoneType.Mobile
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> PhoneType.Work
                    else -> PhoneType.Other
                }

                if (!flag) {
                    flag = true
                    val contactID = cursor.getLong(idIndex)
                    val rawContactID = cursor.getLong(rawIDIndex)

                    val fullName = cursor.getString(nameIndex)
                    val isStarred = cursor.getInt(starredIndex) == 1
                    val isBlocked = isNumberBlocked(context, number)
                    val photoURI = cursor.getString(photoIndex)?.toUri()

                    phoneList.add(
                        ContactPhone(phoneID, number, numberType, isBlocked)
                    )

                    val (firstName, lastName) = getContactName(cr, id)
                    val (job, company) = getContactOrg(cr, id)

                    contact = EditableContact(
                        contactID, rawContactID, firstName, lastName, job, company, fullName,
                        isStarred, phones = phoneList.toList(), photoUri = photoURI
                    )
                }
                else if (phoneList.indexOfFirst { it.number == number } == -1)
                    phoneList.add(
                        ContactPhone(
                            phoneID,
                            number,
                            numberType,
                            isNumberBlocked(context, number)
                        )
                    )
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

    fun getContactName(cr: ContentResolver, contactId: Long): Pair<String, String> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
        )

        val cursor = cr.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                    "${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                contactId.toString(),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            ),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val given = it.getString(0) ?: ""
                val family = it.getString(1) ?: ""
                return given to family
            }
        }

        return "" to ""
    }

    fun getContactOrg(cr: ContentResolver, contactId: Long): Pair<String, String> {

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Organization.TITLE,
            ContactsContract.CommonDataKinds.Organization.COMPANY
        )

        val cursor = cr.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                    "${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                contactId.toString(),
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            ),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val title = it.getString(0) ?: ""
                val company = it.getString(1) ?: ""
                return Pair(title, company)
            }
        }

        return Pair("", "")
    }

    //Block
    fun blockNumber(context: Context, phoneNumber: String) {
        val values = ContentValues()
        values.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, phoneNumber)

        // The URI for the blocked numbers table
        context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
    }

    fun unblockNumber(context: Context, phoneNumber: String) {
        val uri = Uri.withAppendedPath(
            BlockedNumberContract.BlockedNumbers.CONTENT_URI,
            Uri.encode(phoneNumber)
        )

        context.contentResolver.delete(uri, null, null)
    }

    fun isNumberBlocked(context: Context, number: String): Boolean {
        return BlockedNumberContract.isBlocked(context, number)
    }


    //Favorite
    fun setFavorite(context: Context, contactId: Long, isFavorite: Boolean) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (isFavorite) 1 else 0)
        }

        val uri = ContactsContract.Contacts.getLookupUri(contactId, null)
            ?: ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)

        context.contentResolver.update(uri, values, null, null)
    }

    fun getFavoriteContacts(context: Context): List<Contact> {
        val favorites = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )

        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts.STARRED} = 1",
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val name = it.getString(1)
                val photo = it.getString(2)

//                favorites.add(Contact(id, name, photo))
            }
        }

        return favorites
    }
}