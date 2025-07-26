package com.example.vetconnect.di

import android.content.Context
import androidx.room.Room
import com.example.vetconnect.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideVetConnectDatabase(
        @ApplicationContext context: Context
    ): VetConnectDatabase {
        return Room.databaseBuilder(
            context,
            VetConnectDatabase::class.java,
            "vetconnect_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: VetConnectDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun providePetDao(database: VetConnectDatabase): PetDao {
        return database.petDao()
    }
    
    @Provides
    @Singleton
    fun provideAppointmentDao(database: VetConnectDatabase): AppointmentDao {
        return database.appointmentDao()
    }
} 