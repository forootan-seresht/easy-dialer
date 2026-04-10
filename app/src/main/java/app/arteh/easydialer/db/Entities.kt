package app.arteh.easydialer.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_defaults")
data class ContactDefaults(
    @PrimaryKey(autoGenerate = false)
    val contactID: Long,
    val simID: Int,
    val numberID: Long,
)