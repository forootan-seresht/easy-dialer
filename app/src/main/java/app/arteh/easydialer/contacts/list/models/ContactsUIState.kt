package app.arteh.easydialer.contacts.list.models

import app.arteh.easydialer.contacts.Contact
import app.arteh.easydialer.contacts.ContactHeader

data class ContactsUIState(
    val searchText: String = "",
    val isLoading: Boolean = true,
    val contactList: Map<ContactHeader, List<Contact>> = emptyMap(),
    val favorites: List<Contact> = emptyList()
)