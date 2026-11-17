package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EntryType
import com.example.data.FriendEntity
import com.example.data.KhataEntryEntity
import com.example.data.KhataRepository
import com.example.util.KhataFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FriendDetailUiState(
    val friend: FriendEntity? = null,
    val entries: List<KhataEntryEntity> = emptyList(),
    val netBalancePaisa: Long = 0L,
    val totalLentPaisa: Long = 0L,
    val totalBorrowedPaisa: Long = 0L,
    val totalWapsiPaisa: Long = 0L,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val sheetError: String? = null,
    val screenError: String? = null
)

class FriendDetailViewModel(
    private val userId: Long,
    private val friendId: Long,
    private val repository: KhataRepository
) : ViewModel() {

    private val isSaving = MutableStateFlow(false)
    private val sheetError = MutableStateFlow<String?>(null)
    private val screenError = MutableStateFlow<String?>(null)

    private val friendFlow = repository.observeFriendById(userId, friendId)
        .catch { err ->
            screenError.value = err.localizedMessage ?: "Failed to load friend details."
            emit(null)
        }

    private val entriesFlow = repository.observeEntriesForFriend(userId, friendId)
        .catch { err ->
            screenError.value = err.localizedMessage ?: "Failed to load transactions."
            emit(emptyList())
        }

    val uiState: StateFlow<FriendDetailUiState> = combine(
        friendFlow,
        entriesFlow,
        isSaving,
        sheetError,
        screenError
    ) { friend, entries, saving, sErr, topErr ->
        var net = 0L
        var lent = 0L
        var borrowed = 0L
        var wapsi = 0L
        for (entry in entries) {
            net += entry.type.signedDelta(entry.amountPaisa)
            when (entry.type) {
                EntryType.UDHAAR_DIYA -> lent += entry.amountPaisa
                EntryType.UDHAAR_LIYA -> borrowed += entry.amountPaisa
                EntryType.WAPSI_RECEIVED, EntryType.WAPSI_PAID -> wapsi += entry.amountPaisa
            }
        }
        FriendDetailUiState(
            friend = friend,
            entries = entries,
            netBalancePaisa = net,
            totalLentPaisa = lent,
            totalBorrowedPaisa = borrowed,
            totalWapsiPaisa = wapsi,
            isLoading = false,
            isSaving = saving,
            sheetError = sErr,
            screenError = topErr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendDetailUiState()
    )

    fun clearSheetError() {
        sheetError.value = null
    }

    fun dismissScreenError() {
        screenError.value = null
    }

    fun saveEntry(
        editingEntry: KhataEntryEntity?,
        amountInput: String,
        type: EntryType,
        noteInput: String,
        entryDateMillis: Long,
        invalidAmountMsg: String,
        saveFailedTemplate: (String) -> String,
        onSuccess: () -> Unit
    ) {
        val paisa = KhataFormatters.parseAmountToPaisa(amountInput)
        if (paisa == null) {
            sheetError.value = invalidAmountMsg
            return
        }

        viewModelScope.launch {
            isSaving.value = true
            sheetError.value = null
            val result = if (editingEntry == null) {
                repository.addEntry(
                    userId = userId,
                    friendId = friendId,
                    amountPaisa = paisa,
                    type = type,
                    note = noteInput,
                    entryDateMillis = entryDateMillis
                ).map { Unit }
            } else {
                repository.updateEntry(
                    userId = userId,
                    entry = editingEntry.copy(
                        amountPaisa = paisa,
                        type = type,
                        note = noteInput,
                        entryDateMillis = entryDateMillis
                    )
                )
            }
            isSaving.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                val msg = saveFailedTemplate(err.localizedMessage ?: "Storage error")
                sheetError.value = msg
                screenError.value = msg
            }
        }
    }

    fun recordWapsi(
        amountInput: String,
        direction: EntryType,
        noteInput: String,
        entryDateMillis: Long,
        invalidAmountMsg: String,
        saveFailedTemplate: (String) -> String,
        onSuccess: () -> Unit
    ) {
        val wapsiType = if (direction == EntryType.WAPSI_PAID) {
            EntryType.WAPSI_PAID
        } else {
            EntryType.WAPSI_RECEIVED
        }
        saveEntry(
            editingEntry = null,
            amountInput = amountInput,
            type = wapsiType,
            noteInput = noteInput,
            entryDateMillis = entryDateMillis,
            invalidAmountMsg = invalidAmountMsg,
            saveFailedTemplate = saveFailedTemplate,
            onSuccess = onSuccess
        )
    }

    fun deleteEntry(
        entryId: Long,
        deleteFailedTemplate: (String) -> String
    ) {
        viewModelScope.launch {
            isSaving.value = true
            val result = repository.deleteEntry(userId, entryId)
            isSaving.value = false
            result.onFailure { err ->
                screenError.value = deleteFailedTemplate(err.localizedMessage ?: "Storage error")
            }
        }
    }

    class Factory(
        private val userId: Long,
        private val friendId: Long,
        private val repository: KhataRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FriendDetailViewModel(userId, friendId, repository) as T
        }
    }
}
