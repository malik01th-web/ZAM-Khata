package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val email: String,
    val passwordHash: String,
    val mpinHash: String? = null,
    val failedMpinAttempts: Int = 0,
    val mpinLockedUntilMillis: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
