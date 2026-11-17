package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.EntryType
import com.example.data.KhataRepository
import com.example.data.MpinVerifyResult
import com.example.data.ZamKhataDatabase
import com.example.util.AppLanguage
import com.example.util.AppThemeMode
import com.example.util.KhataFormatters
import com.example.util.LanguagePreferences
import com.example.util.SecurityHasher
import com.example.util.ThemePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: ZamKhataDatabase
    private lateinit var repository: KhataRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ZamKhataDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = KhataRepository(db.khataDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ZAM Khata", appName)
    }

    @Test
    fun `add friend, lend, borrow, wapsi, edit, and delete update balance accurately`() = runTest {
        val user = repository.signUpWithEmail("hamza@example.com", "secret123").getOrThrow()
        val friendId = repository.addFriend(user.id, "Hamza Ali", "03001234567").getOrThrow()
        val today = KhataFormatters.todayUtcMidnightMillis()

        val lentId = repository.addEntry(
            userId = user.id,
            friendId = friendId,
            amountPaisa = 500_000L,
            type = EntryType.UDHAAR_DIYA,
            note = "Dinner split",
            entryDateMillis = today
        ).getOrThrow()

        val borrowedId = repository.addEntry(
            userId = user.id,
            friendId = friendId,
            amountPaisa = 120_000L,
            type = EntryType.UDHAAR_LIYA,
            note = "Fuel",
            entryDateMillis = today
        ).getOrThrow()

        repository.addEntry(
            userId = user.id,
            friendId = friendId,
            amountPaisa = 180_000L,
            type = EntryType.WAPSI_RECEIVED,
            note = "JazzCash",
            entryDateMillis = today
        ).getOrThrow()

        var summary = repository.observeFriendSummaries(user.id).first().single()
        assertEquals(200_000L, summary.netBalancePaisa)
        assertEquals(3, summary.entryCount)

        val existingEntries = repository.observeEntriesForFriend(user.id, friendId).first()
        val lentEntry = existingEntries.first { it.id == lentId }
        repository.updateEntry(user.id, lentEntry.copy(amountPaisa = 600_000L)).getOrThrow()

        summary = repository.observeFriendSummaries(user.id).first().single()
        assertEquals(300_000L, summary.netBalancePaisa)

        repository.deleteEntry(user.id, borrowedId).getOrThrow()

        summary = repository.observeFriendSummaries(user.id).first().single()
        assertEquals(420_000L, summary.netBalancePaisa)
        assertEquals(2, summary.entryCount)
    }

    @Test
    fun `per-user isolation prevents seeing or editing another user's friends and entries`() = runTest {
        val userA = repository.signUpWithEmail("usera@example.com", "pass1234").getOrThrow()
        val userB = repository.signUpWithEmail("userb@example.com", "pass5678").getOrThrow()
        val today = KhataFormatters.todayUtcMidnightMillis()

        val friendAId = repository.addFriend(userA.id, "Ali", "03001112233").getOrThrow()
        val entryAId = repository.addEntry(
            userId = userA.id,
            friendId = friendAId,
            amountPaisa = 100_000L,
            type = EntryType.UDHAAR_DIYA,
            note = "Tea",
            entryDateMillis = today
        ).getOrThrow()

        assertEquals(1, repository.observeFriendSummaries(userA.id).first().size)
        assertEquals(0, repository.observeFriendSummaries(userB.id).first().size)
        assertTrue(repository.observeEntriesForFriend(userB.id, friendAId).first().isEmpty())

        val crossUserAdd = repository.addEntry(
            userId = userB.id,
            friendId = friendAId,
            amountPaisa = 50_000L,
            type = EntryType.UDHAAR_DIYA,
            note = null,
            entryDateMillis = today
        )
        assertTrue(crossUserAdd.isFailure)

        val crossUserDelete = repository.deleteEntry(userB.id, entryAId)
        assertTrue(crossUserDelete.isFailure)
    }

    @Test
    fun `mpin is stored hashed, locks for 15 minutes after 5 wrong attempts, and resets via email`() = runTest {
        val user = repository.signUpWithEmail("pinuser@example.com", "password99").getOrThrow()
        repository.setUserMpin(user.id, "4829").getOrThrow()

        val storedUser = repository.getUserById(user.id)!!
        assertNotNull(storedUser.mpinHash)
        assertNotEquals("4829", storedUser.mpinHash)
        assertFalse(storedUser.mpinHash!!.contains("4829"))
        assertTrue(SecurityHasher.verifySecret("4829", storedUser.mpinHash!!))

        val baseTime = 1_700_000_000_000L
        for (attempt in 1..4) {
            val result = repository.verifyUserMpin(user.id, "0000", nowMillis = baseTime + attempt).getOrThrow()
            assertTrue(result is MpinVerifyResult.WrongPin)
            assertEquals(5 - attempt, (result as MpinVerifyResult.WrongPin).attemptsRemaining)
        }

        // 5th wrong attempt triggers 15-minute lock
        val fifth = repository.verifyUserMpin(user.id, "0000", nowMillis = baseTime + 5).getOrThrow()
        assertTrue(fifth is MpinVerifyResult.LockedOut)
        val lockedUntil = (fifth as MpinVerifyResult.LockedOut).lockedUntilMillis
        assertEquals(baseTime + 5 + KhataRepository.LOCKOUT_DURATION_MILLIS, lockedUntil)

        // Even correct PIN is rejected while locked
        val whileLocked = repository.verifyUserMpin(user.id, "4829", nowMillis = baseTime + 60_000L).getOrThrow()
        assertTrue(whileLocked is MpinVerifyResult.LockedOut)

        // After 15 minutes pass, correct PIN succeeds
        val afterLock = repository.verifyUserMpin(user.id, "4829", nowMillis = lockedUntil + 1L).getOrThrow()
        assertEquals(MpinVerifyResult.Success, afterLock)

        // Forgot M-PIN verifies email + password and sets a new hashed M-PIN
        assertTrue(repository.verifyEmailForMpinReset(user.id, "wrong@example.com", "password99").isFailure)
        assertTrue(repository.verifyEmailForMpinReset(user.id, "pinuser@example.com", "wrongpass").isFailure)
        repository.verifyEmailForMpinReset(user.id, "PINUSER@example.com", "password99").getOrThrow()

        repository.setUserMpin(user.id, "9173").getOrThrow()
        assertEquals(MpinVerifyResult.Success, repository.verifyUserMpin(user.id, "9173").getOrThrow())
    }

    @Test
    fun `invalid inputs return failure Result cleanly`() = runTest {
        val user = repository.signUpWithEmail("test@example.com", "pass1234").getOrThrow()
        val blankFriendResult = repository.addFriend(user.id, "   ", null)
        assertTrue(blankFriendResult.isFailure)

        val friendId = repository.addFriend(user.id, "Bilal", null).getOrThrow()
        val zeroEntryResult = repository.addEntry(
            userId = user.id,
            friendId = friendId,
            amountPaisa = 0L,
            type = EntryType.UDHAAR_DIYA,
            note = null,
            entryDateMillis = KhataFormatters.todayUtcMidnightMillis()
        )
        assertTrue(zeroEntryResult.isFailure)
    }

    @Test
    fun `urdu localization and language preference switching work`() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        LanguagePreferences.setSelectedLanguage(baseContext, AppLanguage.URDU)
        val wrapped = LanguagePreferences.wrapContext(baseContext)
        assertEquals("زام کھاتہ", wrapped.getString(R.string.app_name))
        assertEquals("ادھار دیا", wrapped.getString(R.string.type_udhaar_diya))
        assertEquals("ادھار لیا", wrapped.getString(R.string.type_udhaar_liya))
        assertEquals("واپسی درج کریں", wrapped.getString(R.string.action_record_wapsi))
    }

    @Test
    fun `dark and light theme mode preference persists`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(AppThemeMode.DARK, ThemePreferences.getThemeMode(context))

        ThemePreferences.setThemeMode(context, AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, ThemePreferences.getThemeMode(context))

        ThemePreferences.setThemeMode(context, AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, ThemePreferences.getThemeMode(context))
    }
}
