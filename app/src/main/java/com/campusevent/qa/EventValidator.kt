package com.campusevent.qa

/**
 * Pure Kotlin validator for Campus Event registration.
 *
 * CRITICAL: This class must NOT depend on any Android Framework classes
 * (such as android.text.TextUtils or android.util.Patterns) so that it can be
 * executed quickly on pure JVM without mock environments.
 */
class EventValidator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * Validates participant name:
     * - Must not be empty or blank (whitespace only is rejected)
     * - Minimum 3 characters
     * - Maximum 50 characters
     */
    fun isValidParticipantName(name: String): Boolean {
        if (name.isBlank()) return false
        val trimmed = name.trim()
        return trimmed.length in 3..50
    }

    /**
     * Validates participant email:
     * - Must not be empty or blank
     * - Must have simple valid email structure containing '@' and valid domain with TLD
     * - e.g. "mahasiswa@" or "user@domain" is rejected
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val trimmed = email.trim()
        return emailRegex.matches(trimmed)
    }

    /**
     * Validates participant count:
     * - Minimum 1 participant
     * - Maximum 5 participants
     * - Negative or 0 values are strictly rejected
     */
    fun isValidParticipantCount(count: Int): Boolean {
        return count in 1..5
    }

    /**
     * Checks quota availability:
     * - Requested count must be > 0
     * - Quota must be > 0
     * - registeredCount + requestedCount must not exceed total quota
     */
    fun isQuotaAvailable(
        quota: Int,
        registeredCount: Int,
        requestedCount: Int
    ): Boolean {
        if (quota <= 0) return false
        if (requestedCount <= 0) return false
        if (registeredCount < 0) return false
        return (registeredCount + requestedCount) <= quota
    }
}
