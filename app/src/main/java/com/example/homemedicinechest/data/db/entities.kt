package com.example.homemedicinechest.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val name: String? = null,
    val birthday: String? = null,
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val relation: String
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("expiresAt")]
)
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val nameNorm: String,
    val dosage: String,
    val form: String? = null,
    val instructions: String? = null,
    val expiresAt: Long? = null, // epoch millis
    val stockQty: Int = 0
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId"), Index("memberId")]
)
data class DosePlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val memberId: Long?, // null => общий
    val startAt: Long,
    val endAt: Long?,
    val timesPerDay: Int? = null,
    val intervalHours: Int? = null
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = DosePlan::class,
        parentColumns = ["id"],
        childColumns = ["dosePlanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dosePlanId"), Index("scheduledAt")]
)
data class IntakeEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dosePlanId: Long,
    val scheduledAt: Long,
    val takenAt: Long? = null,
    val status: String = "scheduled" // scheduled, taken, skipped
)


@Entity
data class Profile(
    @PrimaryKey val userId: Long,
    val name: String? = null,
    val birthdayMillis: Long? = null
)