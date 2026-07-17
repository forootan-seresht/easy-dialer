package app.arteh.easydialer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SimDefaultsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pd: PhoneNumberDefaults)

    @Query("Update phone_defaults set simID = :simID where phoneID = :phoneID")
    suspend fun updateSim(phoneID: Long, simID: Int): Int

    @Query("Delete from phone_defaults where phoneID = :phoneID")
    suspend fun deleteByID(phoneID: Long)

    @Query("Select * from phone_defaults where phoneID = :phoneID")
    suspend fun getByID(phoneID: Long): PhoneNumberDefaults?
}