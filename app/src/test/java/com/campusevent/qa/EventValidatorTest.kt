package com.campusevent.qa

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Level 1 - JVM Unit Testing for Pure Business Logic & Edge Cases.
 *
 * CRITICAL QA PRINCIPLE:
 * - Runs purely on local JVM (sub-second execution speed).
 * - Zero dependency on Android Framework classes (no TextUtils, Log, Context).
 * - Deep verification of boundary conditions, invalid inputs, and negative flows.
 */
class EventValidatorTest {

    private lateinit var validator: EventValidator

    @Before
    fun setUp() {
        validator = EventValidator()
    }

    // =========================================================================
    // 1. Participant Name Validation Tests
    // =========================================================================

    @Test
    fun `isValidParticipantName dengan nama normal harus bernilai true`() {
        val result = validator.isValidParticipantName("Ahmad Dahlan")
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidParticipantName dengan tepat batas minimum 3 karakter harus bernilai true`() {
        val result = validator.isValidParticipantName("Ali")
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidParticipantName dengan tepat batas maksimum 50 karakter harus bernilai true`() {
        val longName = "A".repeat(50)
        val result = validator.isValidParticipantName(longName)
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidParticipantName dengan string kosong harus bernilai false`() {
        val result = validator.isValidParticipantName("")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidParticipantName dengan hanya spasi harus ditolak dan bernilai false`() {
        // Edge Case: Blank whitespace name
        val result = validator.isValidParticipantName("     ")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidParticipantName dengan panjang kurang dari 3 karakter harus bernilai false`() {
        val result = validator.isValidParticipantName("Al")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidParticipantName dengan panjang melebihi 50 karakter harus bernilai false`() {
        val tooLongName = "A".repeat(51)
        val result = validator.isValidParticipantName(tooLongName)
        assertThat(result).isFalse()
    }

    // =========================================================================
    // 2. Email Validation Tests
    // =========================================================================

    @Test
    fun `isValidEmail dengan format standar email harus bernilai true`() {
        val result = validator.isValidEmail("ahmad@gmail.com")
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidEmail dengan email institusi kampus berdomain ac id harus bernilai true`() {
        val result = validator.isValidEmail("budi.santoso@kampus.ac.id")
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidEmail kosong harus bernilai false`() {
        val result = validator.isValidEmail("")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidEmail hanya berisi spasi harus bernilai false`() {
        val result = validator.isValidEmail("   ")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidEmail tanpa simbol at harus bernilai false`() {
        val result = validator.isValidEmail("ahmadgmail.com")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidEmail dengan mahasiswa@ tanpa domain harus ditolak`() {
        // Edge Case: Sesuai spesifikasi modul
        val result = validator.isValidEmail("mahasiswa@")
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidEmail dengan domain tanpa TLD harus bernilai false`() {
        val result = validator.isValidEmail("ahmad@domain")
        assertThat(result).isFalse()
    }

    // =========================================================================
    // 3. Participant Count Validation Tests
    // =========================================================================

    @Test
    fun `isValidParticipantCount bernilai 1 harus bernilai true`() {
        val result = validator.isValidParticipantCount(1)
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidParticipantCount bernilai 5 batas maksimum harus bernilai true`() {
        val result = validator.isValidParticipantCount(5)
        assertThat(result).isTrue()
    }

    @Test
    fun `isValidParticipantCount bernilai 0 harus bernilai false`() {
        val result = validator.isValidParticipantCount(0)
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidParticipantCount lebih dari 5 harus bernilai false`() {
        val result = validator.isValidParticipantCount(6)
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidParticipantCount bernilai negatif harus ditolak`() {
        // Edge Case: Nilai negatif
        val result = validator.isValidParticipantCount(-1)
        assertThat(result).isFalse()
    }

    // =========================================================================
    // 4. Quota Availability Tests & Edge Cases
    // =========================================================================

    @Test
    fun `isQuotaAvailable ketika sisa kuota masih banyak harus bernilai true`() {
        val result = validator.isQuotaAvailable(quota = 50, registeredCount = 32, requestedCount = 2)
        assertThat(result).isTrue()
    }

    @Test
    fun `isQuotaAvailable ketika permintaan tepat memenuhi kuota harus bernilai true`() {
        val result = validator.isQuotaAvailable(quota = 50, registeredCount = 48, requestedCount = 2)
        assertThat(result).isTrue()
    }

    @Test
    fun `isQuotaAvailable ketika permintaan melebihi sisa kuota harus bernilai false`() {
        val result = validator.isQuotaAvailable(quota = 50, registeredCount = 49, requestedCount = 2)
        assertThat(result).isFalse()
    }

    @Test
    fun `isQuotaAvailable dengan kuota 0 dan requested 1 harus false`() {
        val result = validator.isQuotaAvailable(quota = 0, registeredCount = 0, requestedCount = 1)
        assertThat(result).isFalse()
    }

    @Test
    fun `isQuotaAvailable ketika registeredCount sudah sama dengan kuota harus ditolak`() {
        val result = validator.isQuotaAvailable(quota = 30, registeredCount = 30, requestedCount = 1)
        assertThat(result).isFalse()
    }

    @Test
    fun `isQuotaAvailable dengan requestedCount negatif harus bernilai false`() {
        val result = validator.isQuotaAvailable(quota = 50, registeredCount = 10, requestedCount = -1)
        assertThat(result).isFalse()
    }

    @Test
    fun `isQuotaAvailable dengan kuota total negatif harus bernilai false`() {
        val result = validator.isQuotaAvailable(quota = -10, registeredCount = 0, requestedCount = 1)
        assertThat(result).isFalse()
    }
}
