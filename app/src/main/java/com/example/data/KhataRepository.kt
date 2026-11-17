package com.example.data

import com.example.util.SecurityHasher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

sealed interface AuthFailure {
    data object EmailAlreadyExists : AuthFailure
    data object InvalidCredentials : AuthFailure
    data object EmailMismatch : AuthFailure
}

class AuthException(val failure: AuthFailure, message: String) : IllegalStateException(message)

sealed interface MpinVerifyResult {
    data object Success : MpinVerifyResult
    data class WrongPin(val attemptsRemaining: Int) : MpinVerifyResult
    data class LockedOut(val lockedUntilMillis: Long) : MpinVerifyResult
    data object NoMpinSet : MpinVerifyResult
}

class KhataRepository(private val dao: KhataDao) {

    companion object {
        const val MAX_MPIN_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MILLIS = 15L * 60L * 1000L
    }

    // ---- Auth & M-PIN ----

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)

    suspend fun getUserById(userId: Long): UserEntity? = dao.getUserById(userId)

    suspend fun signUpWithEmail(email: String, password: String): Result<UserEntity> {
        return runDbCatching {
            val normalizedEmail = SecurityHasher.normalizeEmail(email)
            require(SecurityHasher.isValidEmail(normalizedEmail)) { "Invalid email address." }
            require(password.length >= 6) { "Password must be at least 6 characters." }

            val existing = dao.getUserByEmail(normalizedEmail)
            if (existing != null) {
                throw AuthException(AuthFailure.EmailAlreadyExists, "Account already exists.")
            }
            val passwordHash = SecurityHasher.hashSecret(password)
            val newUser = UserEntity(
                email = normalizedEmail,
                passwordHash = passwordHash
            )
            val insertedId = dao.insertUser(newUser)
            if (insertedId <= 0L) error("Database rejected user creation.")
            newUser.copy(id = insertedId)
        }
    }

    suspend fun logInWithEmail(email: String, password: String): Result<UserEntity> {
        return runDbCatching {
            val normalizedEmail = SecurityHasher.normalizeEmail(email)
            if (!SecurityHasher.isValidEmail(normalizedEmail) || password.isEmpty()) {
                throw AuthException(AuthFailure.InvalidCredentials, "Invalid email or password.")
            }
            val user = dao.getUserByEmail(normalizedEmail)
                ?: throw AuthException(AuthFailure.InvalidCredentials, "Invalid email or password.")
            if (!SecurityHasher.verifySecret(password, user.passwordHash)) {
                throw AuthException(AuthFailure.InvalidCredentials, "Invalid email or password.")
            }
            user
        }
    }

    suspend fun setUserMpin(userId: Long, pin: String): Result<Unit> {
        return runDbCatching {
            require(SecurityHasher.isValidMpin(pin)) { "M-PIN must be exactly 4 digits." }
            val hashedPin = SecurityHasher.hashSecret(pin)
            val updated = dao.updateUserMpin(userId, hashedPin)
            if (updated <= 0) error("Account not found.")
        }
    }

    suspend fun verifyUserMpin(
        userId: Long,
        pin: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Result<MpinVerifyResult> {
        return runDbCatching {
            val user = dao.getUserById(userId) ?: error("Account not found.")
            val storedHash = user.mpinHash ?: return@runDbCatching MpinVerifyResult.NoMpinSet

            // Check active lockout
            if (user.mpinLockedUntilMillis > nowMillis) {
                return@runDbCatching MpinVerifyResult.LockedOut(user.mpinLockedUntilMillis)
            }

            // If lockout previously expired, treat prior attempts as reset
            val currentAttempts = if (user.mpinLockedUntilMillis in 1L..nowMillis) {
                dao.updateMpinLockState(userId, 0, 0L)
                0
            } else {
                user.failedMpinAttempts
            }

            if (SecurityHasher.verifySecret(pin, storedHash)) {
                if (currentAttempts != 0 || user.mpinLockedUntilMillis != 0L) {
                    dao.updateMpinLockState(userId, 0, 0L)
                }
                MpinVerifyResult.Success
            } else {
                val nextAttempts = currentAttempts + 1
                if (nextAttempts >= MAX_MPIN_ATTEMPTS) {
                    val lockedUntil = nowMillis + LOCKOUT_DURATION_MILLIS
                    dao.updateMpinLockState(userId, nextAttempts, lockedUntil)
                    MpinVerifyResult.LockedOut(lockedUntil)
                } else {
                    dao.updateMpinLockState(userId, nextAttempts, 0L)
                    MpinVerifyResult.WrongPin(MAX_MPIN_ATTEMPTS - nextAttempts)
                }
            }
        }
    }

    suspend fun clearExpiredLockoutIfNeeded(
        userId: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Result<Unit> {
        return runDbCatching {
            val user = dao.getUserById(userId) ?: return@runDbCatching
            if (user.mpinLockedUntilMillis in 1L..nowMillis) {
                dao.updateMpinLockState(userId, 0, 0L)
            }
        }
    }

    suspend fun verifyEmailForMpinReset(
        userId: Long,
        emailInput: String,
        passwordInput: String
    ): Result<Unit> {
        return runDbCatching {
            val user = dao.getUserById(userId) ?: error("Account not found.")
            val normalizedInput = SecurityHasher.normalizeEmail(emailInput)
            if (normalizedInput != user.email) {
                throw AuthException(AuthFailure.EmailMismatch, "Email does not match signed-in account.")
            }
            if (!SecurityHasher.verifySecret(passwordInput, user.passwordHash)) {
                throw AuthException(AuthFailure.InvalidCredentials, "Incorrect password.")
            }
        }
    }

    // ---- User-Scoped Friends & Entries ----

    fun observeFriendSummaries(userId: Long): Flow<List<FriendSummaryRow>> =
        dao.observeFriendSummaries(userId)

    fun observeFriendById(userId: Long, friendId: Long): Flow<FriendEntity?> =
        dao.observeFriendById(userId, friendId)

    fun observeEntriesForFriend(userId: Long, friendId: Long): Flow<List<KhataEntryEntity>> =
        dao.observeEntriesForFriend(userId, friendId)

    suspend fun addFriend(userId: Long, name: String, normalizedPhone: String?): Result<Long> {
        return runDbCatching {
            require(userId > 0L) { "User must be signed in." }
            val cleanName = name.trim()
            require(cleanName.isNotEmpty()) { "Friend name is required." }
            val entity = FriendEntity(
                userId = userId,
                name = cleanName,
                phone = normalizedPhone?.takeIf { it.isNotBlank() }
            )
            val id = dao.insertFriend(entity)
            if (id <= 0L) error("Database rejected friend insert.")
            id
        }
    }

    suspend fun addEntry(
        userId: Long,
        friendId: Long,
        amountPaisa: Long,
        type: EntryType,
        note: String?,
        entryDateMillis: Long
    ): Result<Long> {
        return runDbCatching {
            require(amountPaisa > 0L) { "Amount must be greater than Rs 0." }
            val ownerId = dao.getFriendOwnerId(friendId)
            require(ownerId == userId) { "Friend does not belong to current account." }
            val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }
            val entity = KhataEntryEntity(
                friendId = friendId,
                amountPaisa = amountPaisa,
                type = type,
                note = cleanNote,
                entryDateMillis = entryDateMillis
            )
            val id = dao.insertEntry(entity)
            if (id <= 0L) error("Database rejected entry insert.")
            id
        }
    }

    suspend fun updateEntry(userId: Long, entry: KhataEntryEntity): Result<Unit> {
        return runDbCatching {
            require(entry.amountPaisa > 0L) { "Amount must be greater than Rs 0." }
            val ownerId = dao.getEntryOwnerId(entry.id)
            require(ownerId == userId) { "Entry does not belong to current account." }
            val cleanEntry = entry.copy(
                note = entry.note?.trim()?.takeIf { it.isNotEmpty() }
            )
            val affected = dao.updateEntry(cleanEntry)
            if (affected <= 0) error("Entry no longer exists in database.")
        }
    }

    suspend fun deleteEntry(userId: Long, entryId: Long): Result<Unit> {
        return runDbCatching {
            val ownerId = dao.getEntryOwnerId(entryId)
            require(ownerId == userId) { "Entry does not belong to current account." }
            val affected = dao.deleteEntryById(entryId)
            if (affected <= 0) error("Entry was already deleted.")
        }
    }

    private suspend inline fun <T> runDbCatching(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
