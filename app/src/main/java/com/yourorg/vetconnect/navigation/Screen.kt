package com.yourorg.vetconnect.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main Screens
    object Dashboard : Screen("dashboard")
    
    // Pet Screens
    object PetList : Screen("pet_list")
    object AddPet : Screen("add_pet")
    object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: Long) = "pet_detail/$petId"
    }
    
    // Appointment Screens
    object AppointmentList : Screen("appointment_list")
    object BookAppointment : Screen("book_appointment")
    object AppointmentDetail : Screen("appointment_detail/{appointmentId}") {
        fun createRoute(appointmentId: Long) = "appointment_detail/$appointmentId"
    }
    object EditAppointment : Screen("edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: Long) = "edit_appointment/$appointmentId"
    }
    object AppointmentCalendar : Screen("appointment_calendar")
    
    // Health Records
    object HealthRecords : Screen("health_records/{petId}") {
        fun createRoute(petId: Long) = "health_records/$petId"
    }
    object HealthRecordDetail : Screen("health_record_detail/{recordId}") {
        fun createRoute(recordId: Long) = "health_record_detail/$recordId"
    }
    
    // Vet Screens
    object VetDashboard : Screen("vet_dashboard")
    object VetSchedule : Screen("vet_schedule")
    object VetAvailability : Screen("vet_availability")
    
    // Admin Screens
    object AdminDashboard : Screen("admin_dashboard")
    object UserManagement : Screen("user_management")
}
