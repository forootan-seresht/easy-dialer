package app.arteh.easydialer.contacts

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.net.toUri
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.clog.models.LogStatus
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.PhoneType
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.db.AppDatabase
import app.arteh.easydialer.db.ContactDefaults
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.PreferencesManager
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date

class ContactRP {
    private lateinit var prefs: PreferencesManager
    var contactList = listOf<Contact>()
    lateinit var db: AppDatabase

    lateinit var speedDialMap: Flow<Map<Int, SpeedDialEntry>>
    var lazyKey = 0

    suspend fun initialize(context: Context, instance: AppDatabase) {
        prefs = PreferencesManager(context)
        db = instance
        contactList = queryContacts("", context)
        speedDialMap = prefs.loadSpeedDIal()
    }

    suspend fun loadContacts(name: String, context: Context): Map<ContactHeader, List<Contact>> {
        val contactMList = queryContacts(name, context)

        return contactMList.groupBy { contact ->
            val firstChar = contact.name.firstOrNull()?.uppercaseChar() ?: '#'

            // Logic to pick a color based on the character
            val headerColor = Holder.colors[firstChar.toInt() % 7]

            ContactHeader(char = firstChar, color = headerColor)
        }
    }

    suspend fun queryContacts(name: String, context: Context): List<Contact> {
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
            ContactsContract.Contacts.DISPLAY_NAME
        )

        val contactMList = processContacts(cursor)

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

    private suspend fun processContacts(cursor: Cursor?): MutableList<Contact> {
        val contactMList = mutableListOf<Contact>()

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

                val contactDefaults = db.contactDefaultsDao().getByID(id)

                val contact = Contact(
                    id, name, number, thumbURI, photoURI,
                    contactDefaults?.simID ?: -1, lazyKey++
                )
                contactMList.add(contact)
            }
        }

        return contactMList
    }

    suspend fun findContactByID(id: Long, context: Context): EditableContact {
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
                    val email = getEmail(cr, id)
                    val note = getNote(cr, id)

                    contact = EditableContact(
                        contactID, rawContactID, firstName, lastName, job, company, email, note,
                        fullName, isStarred, phones = phoneList.toList(), photoUri = photoURI
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

            val contactDefaults = db.contactDefaultsDao().getByID(contact.contactID)

            if (contactDefaults != null)
                phoneList.forEachIndexed { index, phone ->
                    if (phone.phoneID == contactDefaults.numberID)
                        phoneList[index] = phoneList[index].copy(isDefault = true)
                }

            contact = contact.copy(
                phones = phoneList,
                defaultSimID = contactDefaults?.simID ?: -1,
            )
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

    fun getEmail(cr: ContentResolver, contactId: Long): String {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)

        val cursor = cr.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                    "${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                contactId.toString(),
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            ),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) return it.getString(0) ?: ""
        }

        return ""
    }

    fun getNote(cr: ContentResolver, contactId: Long): String {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Note.NOTE)

        val cursor = cr.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                    "${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                contactId.toString(),
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
            ),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) return it.getString(0) ?: ""
        }

        return ""
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

    suspend fun getFavoriteContacts(context: Context): List<Contact> {
        val projection: Array<String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,

            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,

            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
            ContactsContract.PhoneLookup.PHOTO_URI,
        )

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts.STARRED} = 1",
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )
        return processContacts(cursor)
    }

    suspend fun saveDefaultSim(contactID: Long, simID: Int) {
        if (db.contactDefaultsDao().updateSim(contactID, simID) == 0)
            db.contactDefaultsDao().insert(ContactDefaults(contactID, simID, 0L))
    }

    suspend fun saveDefaultNumber(contactID: Long, numberID: Long) {
        if (db.contactDefaultsDao().updateNumber(contactID, numberID) == 0)
            db.contactDefaultsDao().insert(ContactDefaults(contactID, 0, numberID))
    }

    @SuppressLint("Range")
    fun searchCallLogs(number: String, context: Context): MutableList<Clog> {
        val logMList = mutableListOf<Clog>()

        try {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.CACHED_NAME
            )

            val sort = CallLog.Calls.DATE + " Desc"
            val allCalls = "content://call_log/calls".toUri()

            val cursor = context.contentResolver.query(
                allCalls, projection,
                CallLog.Calls.NUMBER + " like ?", arrayOf("%$number%"), sort
            )

            cursor?.use {
                val count = cursor.count

                for (i in 0..<count) {
                    cursor.moveToPosition(i)

                    val dateTime =
                        getDateTime(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)))

                    val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))

                    val repeatIndex = logMList.firstOrNull { it.number == number }
                    if (repeatIndex != null)
                        logMList.add(
                            Clog(
                                null, number, LogStatus.Other, dateTime.first,
                                dateTime.second, 0, lazyKey++
                            )
                        )

                    if (logMList.size == 10) {
                        cursor.close()
                        return@use
                    }
                }

                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return logMList
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTime(millis: Long): Pair<String, String> {
        val date = Date()
        date.time = millis

        val fDate = SimpleDateFormat("yyyy-MMM-d HH:MM").format(date).split(" ")
        val splitDate = fDate[0].split("-")

        return "${splitDate[1]} ${splitDate[2]}, ${splitDate[0]}" to fDate[1]
    }

    fun searchByNumber(number: String, context: Context): Pair<List<Contact>, List<Clog>> {
        if (contactList.isNotEmpty()) {
            val filteredContacts = contactList.filter { it.phone.contains(number) }.take(10)
            val allLogs = searchCallLogs(number, context)

            val contactNumbers = filteredContacts.map { it.phone }.toSet()

            val filteredLogs = allLogs.filterNot { log ->
                contactNumbers.contains(log.number)
            }

            return filteredContacts to filteredLogs
        }
        return emptyList<Contact>() to emptyList<Clog>()
    }
}