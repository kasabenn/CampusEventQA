package com.campusevent.qa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RegistrationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRegistration(registration: RegistrationEntity)

    @Query("SELECT * FROM registrations WHERE registrationId = :registrationId")
    fun getRegistrationById(registrationId: String): RegistrationEntity?

    @Query("SELECT * FROM registrations WHERE eventId = :eventId")
    fun getRegistrationsByEvent(eventId: String): List<RegistrationEntity>

    @Query("UPDATE registrations SET status = :status WHERE registrationId = :registrationId")
    fun updateStatus(registrationId: String, status: String): Int

    @Query("DELETE FROM registrations WHERE registrationId = :registrationId")
    fun deleteRegistration(registrationId: String): Int

    @Query("SELECT * FROM registrations")
    fun getAllRegistrations(): List<RegistrationEntity>

    @Query("DELETE FROM registrations")
    fun clearAll()
}
