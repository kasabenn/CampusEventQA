package com.campusevent.qa

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Level 3 - UI Automation Testing with AndroidX Espresso.
 *
 * CRITICAL QA PRINCIPLE:
 * - Adheres strictly to the standard Espresso triad formula:
 *     onView(matcher).perform(action).check(assertion)
 * - Verifies real user interaction flows: text typing, keyboard dismissing, clicking.
 * - Confirms both positive (happy path) and negative (validation & quota errors) states.
 */
@RunWith(AndroidJUnit4::class)
class CampusEventEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(EventDetailActivity::class.java)

    @Test
    fun TEST_1_isiFormPendaftaranLengkap_danTekanDaftar_harusMenampilkanStatusBerhasil() {
        // 1. Ketik Nama Lengkap
        onView(withId(R.id.etParticipantName))
            .perform(clearText(), typeText("Ahmad Dahlan"), closeSoftKeyboard())

        // 2. Ketik Email Valid
        onView(withId(R.id.etParticipantEmail))
            .perform(clearText(), typeText("ahmad@gmail.com"), closeSoftKeyboard())

        // 3. Ketik Jumlah Peserta
        onView(withId(R.id.etParticipantCount))
            .perform(clearText(), typeText("2"), closeSoftKeyboard())

        // 4. Klik Tombol "Daftar Event"
        onView(withId(R.id.btnRegister))
            .perform(click())

        // 5. Verifikasi bahwa status pendaftaran berhasil muncul di layar
        onView(withId(R.id.cardRegistrationResult))
            .check(matches(isDisplayed()))

        onView(withId(R.id.tvRegistrationStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("Pendaftaran Berhasil")))
    }

    @Test
    fun TEST_2_formKosong_danTekanDaftar_harusMenampilkanErrorValidasi_danStatusSuksesTidakMuncul() {
        // Kosongkan seluruh input
        onView(withId(R.id.etParticipantName)).perform(clearText(), closeSoftKeyboard())
        onView(withId(R.id.etParticipantEmail)).perform(clearText(), closeSoftKeyboard())
        onView(withId(R.id.etParticipantCount)).perform(clearText(), closeSoftKeyboard())

        // Klik tombol submit
        onView(withId(R.id.btnRegister)).perform(click())

        // Verifikasi error validation muncul
        onView(withId(R.id.tvRegistrationStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("Pendaftaran Gagal")))

        onView(withId(R.id.tvRegistrationMessage))
            .check(matches(isDisplayed()))
            .check(matches(withText("Nama peserta wajib diisi (minimal 3 karakter)")))

        // Verifikasi bahwa status sukses TIDAK muncul
        onView(withId(R.id.tvRegistrationStatus))
            .check(matches(not(withText("Pendaftaran Berhasil"))))
    }

    @Test
    fun TEST_3_emailFormatTidakValid_harusMenampilkanErrorEmail_danTransaksiDitolak() {
        // Isi nama valid
        onView(withId(R.id.etParticipantName))
            .perform(clearText(), typeText("Ahmad Dahlan"), closeSoftKeyboard())

        // Isi email tidak valid (mahasiswa@ tanpa domain)
        onView(withId(R.id.etParticipantEmail))
            .perform(clearText(), typeText("ahmad@"), closeSoftKeyboard())

        // Isi jumlah 1
        onView(withId(R.id.etParticipantCount))
            .perform(clearText(), typeText("1"), closeSoftKeyboard())

        // Klik tombol daftar
        onView(withId(R.id.btnRegister)).perform(click())

        // Verifikasi error email muncul
        onView(withId(R.id.tvRegistrationStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("Pendaftaran Gagal")))

        onView(withId(R.id.tvRegistrationMessage))
            .check(matches(isDisplayed()))
            .check(matches(withText("Format email tidak valid")))
    }

    @Test
    fun TEST_4_jumlahPesertaMelebihiBatasAtauKuota_harusMenampilkanPesanErrorKuota() {
        // Isi nama valid
        onView(withId(R.id.etParticipantName))
            .perform(clearText(), typeText("Ahmad Dahlan"), closeSoftKeyboard())

        // Isi email valid
        onView(withId(R.id.etParticipantEmail))
            .perform(clearText(), typeText("ahmad@gmail.com"), closeSoftKeyboard())

        // Isi jumlah peserta tidak valid (> 5)
        onView(withId(R.id.etParticipantCount))
            .perform(clearText(), typeText("9"), closeSoftKeyboard())

        // Klik tombol daftar
        onView(withId(R.id.btnRegister)).perform(click())

        // Verifikasi error jumlah peserta
        onView(withId(R.id.tvRegistrationStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("Pendaftaran Gagal")))

        onView(withId(R.id.tvRegistrationMessage))
            .check(matches(isDisplayed()))
            .check(matches(withText("Jumlah peserta harus antara 1 dan 5")))
    }
}
