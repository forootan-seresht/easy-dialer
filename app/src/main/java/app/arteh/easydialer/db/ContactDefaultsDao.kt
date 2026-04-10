package app.arteh.easydialer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDefaultsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cd: ContactDefaults): Long

    @Query("Update contact_defaults set simID = :simID where contactID = :contactID")
    fun updateSim(contactID: Long, simID: Int): Int

    @Query("Update contact_defaults set numberID = :numberID where contactID = :contactID")
    fun updateNumber(contactID: Long, numberID: Long): Int

    @Query("Delete from contact_defaults where contactID = :personID")
    fun deleteByID(personID: Long)

    @Query("Select * from contact_defaults where contactID = :contactID")
    fun getByID(contactID: Long): ContactDefaults?
}