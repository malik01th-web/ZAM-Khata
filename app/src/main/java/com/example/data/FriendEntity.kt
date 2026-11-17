package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friends",
    indices = [Index(value = ["userId"])]
)
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(defaultValue = "0") val userId: Long = 0L,
    val name: String,
    val phone: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
