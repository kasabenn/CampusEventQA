package com.campusevent.qa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface EventApiService {

    @GET("api/v1/events")
    suspend fun getEvents(): List<Event>

    companion object {
        val DEFAULT_EVENTS = listOf(
            Event(
                eventId = "EVT-001",
                title = "Workshop UI/UX Design",
                category = "Design",
                speaker = "Sabrina Putri",
                location = "Lab Komputer 2",
                date = "2026-10-15",
                quota = 50,
                registeredCount = 32
            ),
            Event(
                eventId = "EVT-002",
                title = "Seminar Artificial Intelligence",
                category = "Technology",
                speaker = "Andika Pratama",
                location = "Auditorium Kampus",
                date = "2026-10-20",
                quota = 100,
                registeredCount = 75
            ),
            Event(
                eventId = "EVT-003",
                title = "Android Development Class",
                category = "Programming",
                speaker = "Raka Prasetyo",
                location = "Lab Mobile",
                date = "2026-10-25",
                quota = 30,
                registeredCount = 30 // Intentionally full for edge-case validation
            )
        )

        fun create(baseUrl: String): EventApiService {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EventApiService::class.java)
        }
    }
}
