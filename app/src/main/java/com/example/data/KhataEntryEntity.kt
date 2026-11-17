package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "khata_entries",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["friendId"]),
        Index(value = ["entryDateMillis"])
    ]
)
data class KhataEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val friendId: Long,
    val amountPaisa: Long,
    val type: EntryType,
    val note: String? = null,
    val entryDateMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)
