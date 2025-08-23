package com.example.homemedicinechest.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert suspend fun insert(user: User): Long
    @Query("SELECT * FROM User WHERE email=:email LIMIT 1")
    suspend fun getByEmail(email: String): User?
    @Query("SELECT * FROM User WHERE id=:id")
    suspend fun getById(id: Long): User?
    @Update suspend fun update(user: User)


}

@Dao
interface FamilyDao {
    @Insert suspend fun insert(member: FamilyMember): Long
    @Query("SELECT * FROM FamilyMember WHERE userId=:userId")
    fun getByUser(userId: Long): Flow<List<FamilyMember>>
}

@Dao
interface MedicineDao {
    @Insert suspend fun insert(m: Medicine): Long
    @Update suspend fun update(m: Medicine)
    @Delete suspend fun delete(m: Medicine)

    @Query("SELECT * FROM Medicine WHERE userId=:userId ORDER BY nameNorm ASC, name ASC")
    fun observeAll(userId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM Medicine WHERE id=:id")
    suspend fun get(id: Long): Medicine?

    @Query("SELECT * FROM Medicine WHERE expiresAt IS NOT NULL AND expiresAt < :ts AND userId=:userId")
    suspend fun expired(userId: Long, ts: Long): List<Medicine>

    @Query("SELECT * FROM Medicine WHERE expiresAt IS NOT NULL AND expiresAt BETWEEN :from AND :to AND userId=:userId")
    suspend fun expiringBetween(userId: Long, from: Long, to: Long): List<Medicine>
}

@Dao
interface DosePlanDao {
    @Insert suspend fun insert(p: DosePlan): Long
    @Query("SELECT * FROM DosePlan WHERE medicineId=:medicineId")
    suspend fun plansForMedicine(medicineId: Long): List<DosePlan>
}

@Dao
interface IntakeDao {
    @Insert suspend fun insert(e: IntakeEvent): Long
    @Update suspend fun update(e: IntakeEvent)

    @Query("SELECT * FROM IntakeEvent WHERE dosePlanId IN (:planIds) ORDER BY scheduledAt DESC")
    fun observeForPlans(planIds: List<Long>): Flow<List<IntakeEvent>>

    @Query("SELECT COUNT(*) FROM IntakeEvent WHERE status='taken' AND scheduledAt BETWEEN :from AND :to")
    suspend fun countTaken(from: Long, to: Long): Int
}


@Dao
interface ProfileDao {

    @Query("SELECT * FROM Profile WHERE userId = :userId LIMIT 1")
    fun observe(userId: Long): Flow<Profile?>

    @Query("SELECT * FROM Profile WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: Long): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile)

    @Update
    suspend fun update(profile: Profile)
}