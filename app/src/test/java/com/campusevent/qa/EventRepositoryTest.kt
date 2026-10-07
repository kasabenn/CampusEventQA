package com.campusevent.qa

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Level 1 / Level 2 - Repository Business Logic Tests on JVM.
 */
class EventRepositoryTest {

    private lateinit var repository: EventRepository
    private lateinit var fakeDao: FakeRegistrationDao
    private lateinit var validator: EventValidator

    class FakeRegistrationDao : RegistrationDao {
        val memory = mutableMapOf<String, RegistrationEntity>()

        override fun insertRegistration(registration: RegistrationEntity) {
            memory[registration.registrationId] = registration
        }

        override fun getRegistrationById(registrationId: String): RegistrationEntity? {
            return memory[registrationId]
        }

        override fun getRegistrationsByEvent(eventId: String): List<RegistrationEntity> {
            return memory.values.filter { it.eventId == eventId }
        }

        override fun updateStatus(registrationId: String, status: String): Int {
            val existing = memory[registrationId] ?: return 0
            memory[registrationId] = existing.copy(status = status)
            return 1
        }

        override fun deleteRegistration(registrationId: String): Int {
            return if (memory.remove(registrationId) != null) 1 else 0
        }

        override fun getAllRegistrations(): List<RegistrationEntity> {
            return memory.values.toList()
        }

        override fun clearAll() {
            memory.clear()
        }
    }

    @Before
    fun setUp() {
        fakeDao = FakeRegistrationDao()
        validator = EventValidator()
        repository = EventRepository(
            apiService = null,
            dao = fakeDao,
            validator = validator
        )
    }

    @Test
    fun `pendaftaran sukses dengan data valid menyimpan data ke DAO dan mengembalikan status CONFIRMED`() = runBlocking {
        val result = repository.registerParticipant(
            eventId = "EVT-001",
            participantName = "Ahmad Dahlan",
            email = "ahmad@gmail.com",
            participantCount = 2,
            currentQuota = 50,
            currentRegistered = 32
        )

        assertThat(result.isSuccess).isTrue()
        val registration = result.getOrNull()
        assertThat(registration).isNotNull()
        assertThat(registration?.participantName).isEqualTo("Ahmad Dahlan")
        assertThat(registration?.status).isEqualTo(Registration.STATUS_CONFIRMED)

        val savedInDao = fakeDao.getRegistrationById(registration!!.registrationId)
        assertThat(savedInDao).isNotNull()
        assertThat(savedInDao?.email).isEqualTo("ahmad@gmail.com")
    }

    @Test
    fun `pendaftaran dengan nama kosong harus ditolak dan tidak disimpan di DAO`() = runBlocking {
        val result = repository.registerParticipant(
            eventId = "EVT-001",
            participantName = "",
            email = "ahmad@gmail.com",
            participantCount = 1,
            currentQuota = 50,
            currentRegistered = 32
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Nama peserta wajib diisi")
        assertThat(fakeDao.getAllRegistrations()).isEmpty()
    }

    @Test
    fun `pendaftaran dengan email invalid harus ditolak`() = runBlocking {
        val result = repository.registerParticipant(
            eventId = "EVT-001",
            participantName = "Ahmad Dahlan",
            email = "mahasiswa@",
            participantCount = 1,
            currentQuota = 50,
            currentRegistered = 32
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Format email tidak valid")
        assertThat(fakeDao.getAllRegistrations()).isEmpty()
    }

    @Test
    fun `pendaftaran dengan jumlah melebihi sisa kuota harus ditolak`() = runBlocking {
        val result = repository.registerParticipant(
            eventId = "EVT-003",
            participantName = "Ahmad Dahlan",
            email = "ahmad@gmail.com",
            participantCount = 1,
            currentQuota = 30,
            currentRegistered = 30
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Kuota tidak mencukupi")
    }
}
