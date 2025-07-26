# VetConnect Android App

A comprehensive veterinary clinic management Android application built with modern Android development practices.

## 🏥 Project Overview

VetConnect is a native Android app that connects to a Node.js/Express + MongoDB REST API for veterinary clinic management. The app provides role-based access for pet owners and administrators, with features for managing pets, appointments, and clinic operations.

## ✨ Features

### 🔐 Authentication
- **Login/Register**: Secure JWT-based authentication
- **Forgot Password**: Email-based password reset flow
- **Role-based Access**: Pet owners, veterinarians, and admin roles

### 📱 Dashboard
- **Role-aware Interface**: Different views for pet owners and admins
- **Quick Actions**: Book appointments, view pets, admin panel access
- **Upcoming Appointments**: Real-time appointment tracking

### 🐾 Pet Management
- **Pet Records**: Complete clinical chart-style forms
- **Medical History**: Vaccinations, medications, allergies tracking
- **CRUD Operations**: Create, read, update, delete pet records

### 📅 Appointment System
- **Appointment Booking**: Date/time picker with pet selection
- **Status Tracking**: Scheduled, confirmed, in-progress, completed
- **Type Classification**: Checkup, vaccination, surgery, emergency, etc.

### 👨‍⚕️ Admin Panel
- **User Management**: List, promote, demote users
- **Data Overview**: All appointments and pet records
- **System Administration**: Complete clinic management

### 🔔 Notifications
- **Local Push Notifications**: Upcoming appointment reminders
- **Real-time Updates**: Status changes and new appointments

## 🛠 Technical Stack

### Architecture
- **MVVM Architecture**: ViewModel + State management
- **Repository Pattern**: Clean separation of data sources
- **Dependency Injection**: Hilt for DI management

### UI/UX
- **Jetpack Compose**: Modern declarative UI
- **Material Design 3**: Latest Material Design guidelines
- **Navigation Compose**: Type-safe navigation

### Data & Networking
- **Retrofit**: REST API communication
- **Room Database**: Local data persistence
- **DataStore**: Secure token storage
- **Coroutines**: Asynchronous programming

### Testing
- **Unit Tests**: ViewModel and Repository testing
- **Instrumentation Tests**: UI component testing

## 🎨 Design System

### Color Scheme
- **Primary**: Indigo 600 (#4F46E5)
- **Secondary**: Teal 500 (#14B8A6)
- **Background**: Light Gray (#F3F4F6)
- **Surface**: White (#FFFFFF)

### Typography
- **Headlines**: Large, Medium, Small variants
- **Body**: Regular and Medium weights
- **Labels**: Small and Medium sizes

## 📋 Prerequisites

- **Android Studio**: Arctic Fox or later
- **JDK**: Version 11 or higher
- **Android SDK**: API level 26+ (Android 8.0)
- **Kotlin**: Version 1.9.0 or higher

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd vetconnect-android
```

### 2. Configure API Endpoint
Update the API base URL in `app/src/main/java/com/example/vetconnect/di/NetworkModule.kt`:

```kotlin
.baseUrl("https://your-vetconnect-api.com/api/") // Replace with your actual API URL
```

### 3. Environment Configuration
Create a `.env` file in the project root (optional for local development):

```env
# API Configuration
API_BASE_URL=https://your-vetconnect-api.com/api/
API_TIMEOUT=30

# App Configuration
APP_NAME=VetConnect
APP_VERSION=1.0.0
MIN_SDK_VERSION=26
TARGET_SDK_VERSION=36
```

### 4. Build and Run
```bash
# Clean and build the project
./gradlew clean build

# Run on connected device or emulator
./gradlew installDebug
```

### 5. Run Tests
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

## 📱 App Structure

```
app/src/main/java/com/example/vetconnect/
├── data/
│   ├── api/                 # API service interfaces
│   ├── local/               # Room database and DAOs
│   ├── model/               # Data models and entities
│   └── repository/          # Repository implementations
├── di/                      # Dependency injection modules
├── ui/
│   ├── components/          # Reusable UI components
│   ├── navigation/          # Navigation setup
│   ├── screens/             # Screen implementations
│   │   ├── auth/           # Authentication screens
│   │   ├── dashboard/      # Dashboard screen
│   │   ├── pets/           # Pet management screens
│   │   ├── appointments/   # Appointment screens
│   │   └── admin/          # Admin panel screens
│   ├── state/              # UI state management
│   ├── theme/              # Material 3 theme
│   └── viewmodel/          # ViewModels
└── VetConnectApplication.kt # Application class
```

## 🔧 Configuration

### API Configuration
The app is configured to work with a REST API that provides the following endpoints:

- **Authentication**: `/auth/login`, `/auth/register`, `/auth/forgot-password`
- **Users**: `/users/profile`, `/users` (admin)
- **Pets**: `/pets`, `/pets/{id}`
- **Appointments**: `/appointments`, `/appointments/{id}`

### Database Configuration
- **Room Database**: `vetconnect_database`
- **DataStore**: `auth_preferences`
- **Migration Strategy**: Version-based migrations

## 🧪 Testing

### Unit Tests
- **ViewModels**: Test business logic and state management
- **Repositories**: Test data operations and API calls
- **Use Cases**: Test application logic

### UI Tests
- **Screen Navigation**: Test navigation flows
- **User Interactions**: Test form submissions and validations
- **State Management**: Test UI state changes

### Test Coverage
Run coverage report:
```bash
./gradlew jacocoTestReport
```

## 📦 Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with optimizations

## 🔒 Security

- **JWT Authentication**: Secure token-based authentication
- **Data Encryption**: Sensitive data encrypted in DataStore
- **Network Security**: HTTPS-only API communication
- **Input Validation**: Client-side form validation

## 🚀 Deployment

### Release Build
```bash
# Generate release APK
./gradlew assembleRelease

# Generate release AAB (for Play Store)
./gradlew bundleRelease
```

### Play Store Deployment
1. Generate signed APK/AAB
2. Update version code and name
3. Test on multiple devices
4. Upload to Google Play Console

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Version History

- **v1.0.0**: Initial release with core features
- **v1.1.0**: Added admin panel and notifications
- **v1.2.0**: Enhanced UI/UX and performance improvements

---

**Built with ❤️ for veterinary clinics worldwide** 