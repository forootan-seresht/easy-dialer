package app.arteh.easydialer.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_defaults")
data class ContactDefaults(
    @PrimaryKey(autoGenerate = false)
    val contactID: Long,
    val numberID: Long,
)

@Entity(tableName = "phone_defaults")
data class PhoneNumberDefaults(
    @PrimaryKey(autoGenerate = false)
    val phoneID: Long,
    val simID: Int,
)