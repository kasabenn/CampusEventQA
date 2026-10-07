package com.campusevent.qa

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class EventRepository(
    private val apiService: EventApiService? = null,
    private val dao: RegistrationDao,
    private val validator: EventValidator = EventValidator()
) {

    private val inMemoryEvents: MutableList<Event> = EventApiService.DEFAULT_EVENTS.toMutableList()

    suspend fun getEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            if (apiService != null) {
                val remoteEvents = apiService.getEvents()
                inMemoryEvents.clear()
                inMemoryEvents.addAll(remoteEvents)
                Result.success(remoteEvents)
            } else {
                Result.success(inMemoryEvents.toList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerParticipant(
        eventId: String,
        participantName: String,
        email: String,
        participantCount: Int,
        currentQuota: Int,
        currentRegistered: Int
    ): Result<RegistrationEntity> = withContext(Dispatchers.IO) {
        if (!validator.isValidParticipantName(participantName)) {
            return@withContext Result.failure(IllegalArgumentException("Nama peserta wajib diisi (minimal 3 karakter)"))
        }

        if (!validator.isValidEmail(email)) {
            return@withContext Result.failure(IllegalArgumentException("Format email tidak valid"))
        }

        if (!validator.isValidParticipantCount(participantCount)) {
            return@withContext Result.failure(IllegalArgumentException("Jumlah peserta harus antara 1 dan 5"))
        }

        if (!validator.isQuotaAvailable(currentQuota, currentRegistered, participantCount)) {
            return@withContext Result.failure(IllegalStateException("Kuota tidak mencukupi"))
        }

        val registrationId = "REG-" + UUID.randomUUID().toString().take(8).uppercase()
        val entity = RegistrationEntity(
            registrationId = registrationId,
            eventId = eventId,
            participantName = participantName.trim(),
            email = email.trim(),
            participantCount = participantCount,
            status = Registration.STATUS_CONFIRMED
        )

        dao.insertRegistration(entity)

        val index = inMemoryEvents.indexOfFirst { it.eventId == eventId }
        if (index != -1) {
            val old = inMemoryEvents[index]
            inMemoryEvents[index] = old.copy(registeredCount = old.registeredCount + participantCount)
        }

        Result.success(entity)
    }

    suspend fun getRegistration(registrationId: String): RegistrationEntity? = withContext(Dispatchers.IO) {
        dao.getRegistrationById(registrationId)
    }

    suspend fun getRegistrationsForEvent(eventId: String): List<RegistrationEntity> = withContext(Dispatchers.IO) {
        dao.getRegistrationsByEvent(eventId)
    }

    fun getLocalEventById(eventId: String): Event? {
        return inMemoryEvents.find { it.eventId == eventId }
    }
}
