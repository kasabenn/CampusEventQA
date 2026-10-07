package com.campusevent.qa

data class Event(
    val eventId: String,
    val title: String,
    val category: String,
    val speaker: String,
    val location: String,
    val date: String,
    val quota: Int,
    val registeredCount: Int
) {
    val remainingQuota: Int
        get() = (quota - registeredCount).coerceAtLeast(0)

    val isFull: Boolean
        get() = registeredCount >= quota
}

data class Registration(
    val registrationId: String,
    val eventId: String,
    val participantName: String,
    val email: String,
    val participantCount: Int,
    val status: String
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_CONFIRMED = "CONFIRMED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
