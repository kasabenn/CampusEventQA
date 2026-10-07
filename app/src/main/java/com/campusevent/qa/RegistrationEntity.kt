package com.campusevent.qa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registrations")
data class RegistrationEntity(
    @PrimaryKey
    val registrationId: String,
    val eventId: String,
    val participantName: String,
    val email: String,
    val participantCount: Int,
    val status: String
)
