package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FriendSummaryRow
import com.example.data.KhataRepository
import com.example.util.KhataFormatters
import com.example.util.PhoneValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendsListUiState(
    val allFriends: List<FriendSummaryRow> = emptyList(),
    val filteredFriends: List<FriendSummaryRow> = emptyList(),
    val searchQuery: String = "",
    val totalToReceivePaisa: Long = 0L,
    val totalToPayPaisa: Long = 0L,
    val netBalancePaisa: Long = 0L,
    val isLoading: Boolean = true,
    val isSavingFriend: Boolean = false,
    val addFriendError: String? = null,
    val bannerError: String? = null
)

class FriendsListViewModel(
    private val userId: Long,
    private val repository: KhataRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val isSavingFriend = MutableStateFlow(false)
    private val addFriendError = MutableStateFlow<String?>(null)
    private val bannerError = MutableStateFlow<String?>(null)

    private val friendsFlow = repository.observeFriendSummaries(userId)
        .catch { throwable ->
            bannerError.value = throwable.localizedMessage ?: "Failed to load friends from database."
            emit(emptyList())
        }

    val uiState: StateFlow<FriendsListUiState> = combine(
        friendsFlow,
        searchQuery,
        isSavingFriend,
        addFriendError,
        bannerError
    ) { friends, query, saving, modalErr, topErr ->
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            friends
        } else {
            val cleanedDigits = trimmed.replace("-", "")
            friends.filter { friend ->
                friend.name.contains(trimmed, ignoreCase = true) ||
                    (friend.phone?.contains(cleanedDigits, ignoreCase = true) == true)
            }
        }
        var toReceive = 0L
        var toPay = 0L
        for (friend in friends) {
            if (friend.netBalancePaisa > 0L) {
                toReceive += friend.netBalancePaisa
            } else if (friend.netBalancePaisa < 0L) {
                toPay += -friend.netBalancePaisa
            }
        }
        FriendsListUiState(
            allFriends = friends,
            filteredFriends = filtered,
            searchQuery = query,
            totalToReceivePaisa = toReceive,
            totalToPayPaisa = toPay,
            netBalancePaisa = toReceive - toPay,
            isLoading = false,
            isSavingFriend = saving,
            addFriendError = modalErr,
            bannerError = topErr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendsListUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun clearAddFriendError() {
        addFriendError.value = null
    }

    fun dismissBannerError() {
        bannerError.value = null
    }

    fun addFriend(
        nameInput: String,
        phoneInput: String,
        requiredNameMsg: String,
        invalidPhoneMsg: String,
        saveFailedTemplate: (String) -> String,
        onSuccess: () -> Unit
    ) {
        val cleanName = nameInput.trim()
        if (cleanName.isEmpty()) {
            addFriendError.value = requiredNameMsg
            return
        }
        val phoneResult = KhataFormatters.validatePakistanPhone(phoneInput)
        if (phoneResult is PhoneValidationResult.InvalidFormat) {
            addFriendError.value = invalidPhoneMsg
            return
        }
        val normalizedPhone = (phoneResult as PhoneValidationResult.Valid).normalizedPhone

        viewModelScope.launch {
            isSavingFriend.update { true }
            addFriendError.value = null
            val result = repository.addFriend(userId, cleanName, normalizedPhone)
            isSavingFriend.update { false }
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                val msg = saveFailedTemplate(err.localizedMessage ?: "Storage error")
                addFriendError.value = msg
                bannerError.value = msg
            }
        }
    }

    class Factory(
        private val userId: Long,
        private val repository: KhataRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FriendsListViewModel(userId, repository) as T
        }
    }
}
