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

    @Query("UPDATE `User` SET passwordHash = :newPassword WHERE id = :id")
    suspend fun updatePasswordHash(id: Long, newPassword: String): Int
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

    @Query("SELECT * FROM Medicine WHERE userId=:userId ORDER BY nameNorm, name")
    fun observeByUser(userId: Long): Flow<List<Medicine>>
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

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM MedicationSchedule WHERE userId=:userId AND enabled=1")
    fun observeAll(userId: Long): Flow<List<MedicationSchedule>>

    @Query("SELECT * FROM MedicationSchedule WHERE medicineId=:medicineId")
    fun observeForMedicine(medicineId: Long): Flow<List<MedicationSchedule>>

    @Insert
    suspend fun insert(s: MedicationSchedule): Long

    @Update
    suspend fun update(s: MedicationSchedule)

    @Delete
    suspend fun delete(s: MedicationSchedule)

    @Query("SELECT * FROM MedicationSchedule WHERE id=:id LIMIT 1")
    suspend fun getById(id: Long): MedicationSchedule?
}

@Dao
interface IntakeLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: IntakeLog): Long

    // История (с фильтром по препарату и периоду)
    @Query("""
        SELECT * FROM IntakeLog
        WHERE userId = :userId
          AND (:medicineId IS NULL OR medicineId = :medicineId)
          AND plannedAt BETWEEN :from AND :to
        ORDER BY plannedAt DESC
    """)
    fun observeLogs(
        userId: Long,
        medicineId: Long?,
        from: Long,
        to: Long
    ): Flow<List<IntakeLog>>

    // Сводка по дням: day = epochDay (UTC, через деление на миллисекунды в сутках)
    @Query("""
        SELECT (plannedAt / 86400000) AS day,
               SUM(CASE WHEN status = 'TAKEN'  THEN 1 ELSE 0 END) AS taken,
               SUM(CASE WHEN status = 'SKIPPED' THEN 1 ELSE 0 END) AS skipped
        FROM IntakeLog
        WHERE userId = :userId
          AND (:medicineId IS NULL OR medicineId = :medicineId)
          AND plannedAt BETWEEN :from AND :to
        GROUP BY day
        ORDER BY day ASC
    """)
    fun observeDailyStats(
        userId: Long,
        medicineId: Long?,
        from: Long,
        to: Long
    ): Flow<List<DailyRow>>
}

// DTO для группировки по дню
data class DailyRow(
    val day: Long,     // epochDay = floor(ms / 86400000)
    val taken: Int,
    val skipped: Int
)