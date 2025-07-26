package com.example.vetconnect.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.vetconnect.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthDataStore @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    
    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_DATA = stringPreferencesKey("user_data")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }
    
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCESS_TOKEN]
    }
    
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFRESH_TOKEN]
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }
    
    val userData: Flow<User?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_DATA]?.let { userJson ->
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }
    
    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        user: User
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = accessToken
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
            preferences[PreferencesKeys.USER_ID] = user.id
            preferences[PreferencesKeys.USER_DATA] = gson.toJson(user)
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }
    
    suspend fun updateUserData(user: User) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_DATA] = gson.toJson(user)
        }
    }
    
    suspend fun updateAccessToken(accessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = accessToken
        }
    }
    
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun getAccessTokenSync(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN]
        }.first()
    }
    
    suspend fun getRefreshTokenSync(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN]
        }.first()
    }
} 