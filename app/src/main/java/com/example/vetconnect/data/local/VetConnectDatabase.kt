package com.example.vetconnect.data.local

import androidx.room.*
import com.example.vetconnect.data.model.Appointment
import com.example.vetconnect.data.model.Pet
import com.example.vetconnect.data.model.User

@Database(
    entities = [User::class, Pet::class, Appointment::class],
    version = 1,
    exportSchema = false
)
abstract class VetConnectDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun appointmentDao(): AppointmentDao
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface PetDao {
    @Query("SELECT * FROM pets")
    suspend fun getAllPets(): List<Pet>
    
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId")
    suspend fun getPetsByOwner(ownerId: String): List<Pet>
    
    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: String): Pet?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<Pet>)
    
    @Update
    suspend fun updatePet(pet: Pet)
    
    @Delete
    suspend fun deletePet(pet: Pet)
    
    @Query("DELETE FROM pets")
    suspend fun deleteAllPets()
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointments(): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE ownerId = :ownerId")
    suspend fun getAppointmentsByOwner(ownerId: String): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE petId = :petId")
    suspend fun getAppointmentsByPet(petId: String): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: String): Appointment?
    
    @Query("SELECT * FROM appointments WHERE date >= :date ORDER BY date ASC, time ASC")
    suspend fun getUpcomingAppointments(date: String): List<Appointment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<Appointment>)
    
    @Update
    suspend fun updateAppointment(appointment: Appointment)
    
    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
    
    @Query("DELETE FROM appointments")
    suspend fun deleteAllAppointments()
} 