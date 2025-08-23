package com.example.homemedicinechest.data.repo

import com.example.homemedicinechest.data.db.Profile
import com.example.homemedicinechest.data.db.ProfileDao
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val dao: ProfileDao) {

    fun observe(userId: Long): Flow<Profile?> = dao.observe(userId)

    suspend fun loadOrCreate(userId: Long): Profile {
        return dao.get(userId) ?: Profile(userId = userId).also { dao.insert(it) }
    }

    suspend fun save(profile: Profile) {
        dao.insert(profile) // REPLACE
    }
}
