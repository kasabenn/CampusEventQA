package com.campusevent.qa

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Level 2 - In-Memory Room Database Integration Testing.
 */
@RunWith(AndroidJUnit4::class)
class RegistrationDaoTest {

    private lateinit var db: CampusEventDatabase
    private lateinit var dao: RegistrationDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CampusEventDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.registrationDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun TEST_1_insertDanAmbilRegistrasi_harusTersimpanPersis() {
        val registrasiBaru = RegistrationEntity(
            registrationId = "REG-1001",
            eventId = "EVT-001",
            participantName = "Ahmad Dahlan",
            email = "ahmad@gmail.com",
            participantCount = 2,
            status = Registration.STATUS_PENDING
        )

        dao.insertRegistration(registrasiBaru)
        val retrieved = dao.getRegistrationById("REG-1001")

        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.registrationId).isEqualTo("REG-1001")
        assertThat(retrieved?.participantName).isEqualTo("Ahmad Dahlan")
        assertThat(retrieved?.email).isEqualTo("ahmad@gmail.com")
        assertThat(retrieved?.participantCount).isEqualTo(2)
        assertThat(retrieved?.status).isEqualTo(Registration.STATUS_PENDING)
    }

    @Test
    fun TEST_2_updateStatus_PENDING_menjadi_CONFIRMED_harusBerubah() {
        val registrasi = RegistrationEntity(
            registrationId = "REG-1002",
            eventId = "EVT-001",
            participantName = "Siti Aminah",
            email = "siti@gmail.com",
            participantCount = 1,
            status = Registration.STATUS_PENDING
        )
        dao.insertRegistration(registrasi)

        val rowsAffected = dao.updateStatus("REG-1002", Registration.STATUS_CONFIRMED)
        assertThat(rowsAffected).isEqualTo(1)

        val updated = dao.getRegistrationById("REG-1002")
        assertThat(updated).isNotNull()
        assertThat(updated?.status).isEqualTo(Registration.STATUS_CONFIRMED)
    }

    @Test
    fun TEST_3_queryRegistrasiYangTidakAda_harusMengembalikanNull() {
        val retrieved = dao.getRegistrationById("NON_EXISTING_ID")
        assertThat(retrieved).isNull()
    }

    @Test
    fun TEST_4_deleteRegistrasi_harusMenghapusDataDariDatabase() {
        val registrasi = RegistrationEntity(
            registrationId = "REG-1003",
            eventId = "EVT-002",
            participantName = "Budi Hartono",
            email = "budi@gmail.com",
            participantCount = 3,
            status = Registration.STATUS_CONFIRMED
        )
        dao.insertRegistration(registrasi)

        val rowsDeleted = dao.deleteRegistration("REG-1003")
        assertThat(rowsDeleted).isEqualTo(1)

        val retrievedAfterDelete = dao.getRegistrationById("REG-1003")
        assertThat(retrievedAfterDelete).isNull()
    }

    @Test
    fun TEST_5_insertDuaRegistrasiUntukEventYangSama_harusMengembalikanDuaData() {
        val reg1 = RegistrationEntity(
            registrationId = "REG-2001",
            eventId = "EVT-002",
            participantName = "Peserta Satu",
            email = "peserta1@kampus.ac.id",
            participantCount = 1,
            status = Registration.STATUS_CONFIRMED
        )
        val reg2 = RegistrationEntity(
            registrationId = "REG-2002",
            eventId = "EVT-002",
            participantName = "Peserta Dua",
            email = "peserta2@kampus.ac.id",
            participantCount = 2,
            status = Registration.STATUS_CONFIRMED
        )

        dao.insertRegistration(reg1)
        dao.insertRegistration(reg2)

        val registrations = dao.getRegistrationsByEvent("EVT-002")
        assertThat(registrations).hasSize(2)
        val names = registrations.map { it.participantName }
        assertThat(names).containsExactly("Peserta Satu", "Peserta Dua")
    }
}
