package com.campusevent.qa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RegistrationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CampusEventDatabase : RoomDatabase() {

    abstract fun registrationDao(): RegistrationDao

    companion object {
        @Volatile
        private var INSTANCE: CampusEventDatabase? = null

        fun getInstance(context: Context): CampusEventDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CampusEventDatabase::class.java,
                    "campus_event_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
