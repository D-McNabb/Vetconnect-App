package com.yourorg.vetconnect.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.yourorg.vetconnect.model.*

@Database(
    entities = [
        User::class,
        Pet::class,
        Appointment::class,
        HealthRecord::class,
        VetAvailability::class,
        BlockedSlot::class,
        MedicalRecord::class,
        Vaccination::class,
        WeightRecord::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun vetAvailabilityDao(): VetAvailabilityDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun weightRecordDao(): WeightRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vetconnect_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
