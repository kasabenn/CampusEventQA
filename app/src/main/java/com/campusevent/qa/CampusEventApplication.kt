package com.campusevent.qa

import android.app.Application

class CampusEventApplication : Application() {

    lateinit var database: CampusEventDatabase
        private set

    lateinit var repository: EventRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = CampusEventDatabase.getInstance(this)
        repository = EventRepository(
            apiService = null, // In production/standalone app, uses default events with local Room persistence
            dao = database.registrationDao(),
            validator = EventValidator()
        )
    }
}
