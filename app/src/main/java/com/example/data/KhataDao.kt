package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataDao {

    // User & M-PIN Queries
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET mpinHash = :mpinHash, failedMpinAttempts = 0, mpinLockedUntilMillis = 0 WHERE id = :userId")
    suspend fun updateUserMpin(userId: Long, mpinHash: String): Int

    @Query("UPDATE users SET failedMpinAttempts = :attempts, mpinLockedUntilMillis = :lockedUntilMillis WHERE id = :userId")
    suspend fun updateMpinLockState(userId: Long, attempts: Int, lockedUntilMillis: Long): Int

    // User-Scoped Friends & Entries Queries
    @Query(
        """
        SELECT f.id, f.name, f.phone, f.createdAt,
          COALESCE(SUM(
            CASE e.type
              WHEN 'UDHAAR_DIYA' THEN e.amountPaisa
              WHEN 'WAPSI_PAID' THEN e.amountPaisa
              WHEN 'UDHAAR_LIYA' THEN -e.amountPaisa
              WHEN 'WAPSI_RECEIVED' THEN -e.amountPaisa
              ELSE 0
            END
          ), 0) AS netBalancePaisa,
          COUNT(e.id) AS entryCount,
          MAX(e.entryDateMillis) AS lastActivityMillis
        FROM friends f
        LEFT JOIN khata_entries e ON f.id = e.friendId
        WHERE f.userId = :userId
        GROUP BY f.id
        ORDER BY COALESCE(MAX(e.createdAt), f.createdAt) DESC
        """
    )
    fun observeFriendSummaries(userId: Long): Flow<List<FriendSummaryRow>>

    @Query("SELECT * FROM friends WHERE id = :friendId AND userId = :userId LIMIT 1")
    fun observeFriendById(userId: Long, friendId: Long): Flow<FriendEntity?>

    @Query("SELECT userId FROM friends WHERE id = :friendId LIMIT 1")
    suspend fun getFriendOwnerId(friendId: Long): Long?

    @Query(
        """
        SELECT e.* FROM khata_entries e
        INNER JOIN friends f ON e.friendId = f.id
        WHERE e.friendId = :friendId AND f.userId = :userId
        ORDER BY e.entryDateMillis DESC, e.createdAt DESC, e.id DESC
        """
    )
    fun observeEntriesForFriend(userId: Long, friendId: Long): Flow<List<KhataEntryEntity>>

    @Query(
        """
        SELECT f.userId FROM khata_entries e
        INNER JOIN friends f ON e.friendId = f.id
        WHERE e.id = :entryId
        LIMIT 1
        """
    )
    suspend fun getEntryOwnerId(entryId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entry: KhataEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: KhataEntryEntity): Int

    @Query("DELETE FROM khata_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long): Int
}
