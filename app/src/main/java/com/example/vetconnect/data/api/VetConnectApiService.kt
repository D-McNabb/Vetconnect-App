package com.example.vetconnect.data.api

import com.example.vetconnect.data.model.*
import retrofit2.http.*

interface VetConnectApiService {
    
    // Authentication endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponse>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiResponse<Unit>
    
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse<Unit>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>
    
    // User endpoints
    @GET("users/profile")
    suspend fun getProfile(): ApiResponse<User>
    
    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<User>
    
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("role") role: String? = null
    ): PaginatedResponse<User>
    
    @PUT("users/{userId}/role")
    suspend fun updateUserRole(
        @Path("userId") userId: String,
        @Body request: UpdateUserRoleRequest
    ): ApiResponse<User>
    
    // Pet endpoints
    @GET("pets")
    suspend fun getPets(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("ownerId") ownerId: String? = null
    ): PaginatedResponse<Pet>
    
    @GET("pets/{petId}")
    suspend fun getPet(@Path("petId") petId: String): ApiResponse<Pet>
    
    @POST("pets")
    suspend fun createPet(@Body request: CreatePetRequest): ApiResponse<Pet>
    
    @PUT("pets/{petId}")
    suspend fun updatePet(
        @Path("petId") petId: String,
        @Body request: UpdatePetRequest
    ): ApiResponse<Pet>
    
    @DELETE("pets/{petId}")
    suspend fun deletePet(@Path("petId") petId: String): ApiResponse<Unit>
    
    // Appointment endpoints
    @GET("appointments")
    suspend fun getAppointments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("petId") petId: String? = null,
        @Query("ownerId") ownerId: String? = null
    ): PaginatedResponse<Appointment>
    
    @GET("appointments/{appointmentId}")
    suspend fun getAppointment(@Path("appointmentId") appointmentId: String): ApiResponse<Appointment>
    
    @POST("appointments")
    suspend fun createAppointment(@Body request: CreateAppointmentRequest): ApiResponse<Appointment>
    
    @PUT("appointments/{appointmentId}")
    suspend fun updateAppointment(
        @Path("appointmentId") appointmentId: String,
        @Body request: UpdateAppointmentRequest
    ): ApiResponse<Appointment>
    
    @DELETE("appointments/{appointmentId}")
    suspend fun deleteAppointment(@Path("appointmentId") appointmentId: String): ApiResponse<Unit>
} 