package com.example.homemedicinechest.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_session")

class UserSession(private val context: Context) {

    companion object {
        private val USER_ID = longPreferencesKey("user_id")
    }

    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    // Получить ID пользователя (Flow, можно собирать в UI)
    val userId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}