package com.yourorg.vetconnect.data

import android.content.Context
import com.yourorg.vetconnect.repository.AppointmentRepository
import com.yourorg.vetconnect.repository.PetRepository

object AppRepositoryProvider {
    @Volatile
    private var appointmentRepository: AppointmentRepository? = null
    
    @Volatile
    private var petRepository: PetRepository? = null
    
    @Volatile
    private var database: AppDatabase? = null
    
    fun getAppointmentRepository(context: Context): AppointmentRepository {
        return appointmentRepository ?: synchronized(this) {
            appointmentRepository ?: AppointmentRepository(
                getDatabase(context)
            ).also { appointmentRepository = it }
        }
    }
    
    fun getPetRepository(context: Context): PetRepository {
        return petRepository ?: synchronized(this) {
            petRepository ?: PetRepository(
                getDatabase(context)
            ).also { petRepository = it }
        }
    }
    
    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: AppDatabase.getDatabase(context).also { database = it }
        }
    }
}
