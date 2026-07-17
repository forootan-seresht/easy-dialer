package app.arteh.easydialer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDefaultsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cd: ContactDefaults): Long

    @Query("Update contact_defaults set numberID = :numberID where contactID = :contactID")
    suspend fun updateNumber(contactID: Long, numberID: Long): Int

    @Query("Delete from contact_defaults where contactID = :personID")
    suspend fun deleteByID(personID: Long)

    @Query("Select * from contact_defaults where contactID = :contactID")
    suspend fun getByID(contactID: Long): ContactDefaults?
}