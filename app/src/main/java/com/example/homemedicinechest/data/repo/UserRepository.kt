package com.example.homemedicinechest.data.repo

import com.example.homemedicinechest.data.db.User
import com.example.homemedicinechest.data.db.UserDao
import java.security.MessageDigest

class UserRepository(private val dao: UserDao) {

    suspend fun create(email: String, password: String): Long {
        val user = User(email = email, passwordHash = hash(password))
        return dao.insert(user)
    }

    suspend fun findByEmail(email: String) = dao.getByEmail(email)

    fun verifyPassword(user: User, password: String): Boolean =
        user.passwordHash == hash(password)

    // Учебный сброс: секретный код "0000"
    suspend fun resetPassword(email: String, newPassword: String, secretCode: String): Boolean {
        if (secretCode != "0000") return false
        val u = dao.getByEmail(email) ?: return false
        dao.update(u.copy(passwordHash = hash(newPassword)))
        return true
    }

    private fun hash(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
