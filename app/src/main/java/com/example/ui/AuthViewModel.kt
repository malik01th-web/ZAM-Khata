package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AuthException
import com.example.data.AuthFailure
import com.example.data.KhataRepository
import com.example.data.MpinVerifyResult
import com.example.data.UserEntity
import com.example.util.AuthSessionPreferences
import com.example.util.SecurityHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthGateStep {
    data object Loading : AuthGateStep
    data object Auth : AuthGateStep
    data class CreateMpin(val user: UserEntity, val isReset: Boolean = false) : AuthGateStep
    data class UnlockMpin(val user: UserEntity) : AuthGateStep
    data class Unlocked(val user: UserEntity) : AuthGateStep
}

data class AuthUiState(
    val step: AuthGateStep = AuthGateStep.Loading,
    val isBusy: Boolean = false,
    val authError: String? = null,
    val mpinError: String? = null,
    val forgotMpinError: String? = null
)

class AuthViewModel(
    private val appContext: Context,
    private val repository: KhataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val savedUserId = AuthSessionPreferences.getCurrentUserId(appContext)
            if (savedUserId <= 0L) {
                _uiState.update { it.copy(step = AuthGateStep.Auth) }
                return@launch
            }
            repository.clearExpiredLockoutIfNeeded(savedUserId)
            val user = repository.getUserById(savedUserId)
            if (user == null) {
                AuthSessionPreferences.clearSession(appContext)
                _uiState.update { it.copy(step = AuthGateStep.Auth) }
            } else if (user.mpinHash == null) {
                _uiState.update { it.copy(step = AuthGateStep.CreateMpin(user = user, isReset = false)) }
            } else {
                _uiState.update { it.copy(step = AuthGateStep.UnlockMpin(user = user)) }
            }
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun clearMpinError() {
        _uiState.update { it.copy(mpinError = null) }
    }

    fun clearForgotMpinError() {
        _uiState.update { it.copy(forgotMpinError = null) }
    }

    fun signUp(
        emailInput: String,
        passwordInput: String,
        confirmPasswordInput: String,
        invalidEmailMsg: String,
        shortPasswordMsg: String,
        passwordMismatchMsg: String,
        emailExistsMsg: String,
        storageFailedTemplate: (String) -> String
    ) {
        if (!SecurityHasher.isValidEmail(emailInput)) {
            _uiState.update { it.copy(authError = invalidEmailMsg) }
            return
        }
        if (passwordInput.length < 6) {
            _uiState.update { it.copy(authError = shortPasswordMsg) }
            return
        }
        if (passwordInput != confirmPasswordInput) {
            _uiState.update { it.copy(authError = passwordMismatchMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, authError = null) }
            val result = repository.signUpWithEmail(emailInput, passwordInput)
            result.onSuccess { user ->
                AuthSessionPreferences.setCurrentUserId(appContext, user.id)
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        authError = null,
                        mpinError = null,
                        step = AuthGateStep.CreateMpin(user = user, isReset = false)
                    )
                }
            }.onFailure { err ->
                val message = if (err is AuthException && err.failure == AuthFailure.EmailAlreadyExists) {
                    emailExistsMsg
                } else {
                    storageFailedTemplate(err.localizedMessage ?: "Storage error")
                }
                _uiState.update { it.copy(isBusy = false, authError = message) }
            }
        }
    }

    fun logIn(
        emailInput: String,
        passwordInput: String,
        invalidEmailMsg: String,
        invalidCredentialsMsg: String,
        storageFailedTemplate: (String) -> String
    ) {
        if (!SecurityHasher.isValidEmail(emailInput)) {
            _uiState.update { it.copy(authError = invalidEmailMsg) }
            return
        }
        if (passwordInput.isEmpty()) {
            _uiState.update { it.copy(authError = invalidCredentialsMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, authError = null) }
            val result = repository.logInWithEmail(emailInput, passwordInput)
            result.onSuccess { user ->
                AuthSessionPreferences.setCurrentUserId(appContext, user.id)
                val nextStep = if (user.mpinHash == null) {
                    AuthGateStep.CreateMpin(user = user, isReset = false)
                } else {
                    AuthGateStep.Unlocked(user = user)
                }
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        authError = null,
                        mpinError = null,
                        step = nextStep
                    )
                }
            }.onFailure { err ->
                val message = if (err is AuthException && err.failure == AuthFailure.InvalidCredentials) {
                    invalidCredentialsMsg
                } else {
                    storageFailedTemplate(err.localizedMessage ?: "Storage error")
                }
                _uiState.update { it.copy(isBusy = false, authError = message) }
            }
        }
    }

    fun saveMpin(
        userId: Long,
        pin: String,
        saveFailedTemplate: (String) -> String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, mpinError = null) }
            val result = repository.setUserMpin(userId, pin)
            result.onSuccess {
                val updatedUser = repository.getUserById(userId)
                if (updatedUser != null) {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mpinError = null,
                            step = AuthGateStep.Unlocked(updatedUser)
                        )
                    }
                } else {
                    AuthSessionPreferences.clearSession(appContext)
                    _uiState.update { it.copy(isBusy = false, step = AuthGateStep.Auth) }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        mpinError = saveFailedTemplate(err.localizedMessage ?: "Storage error")
                    )
                }
            }
        }
    }

    fun verifyMpin(
        userId: Long,
        pin: String,
        oneAttemptRemainingMsg: String,
        attemptsRemainingTemplate: (Int) -> String,
        storageFailedTemplate: (String) -> String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, mpinError = null) }
            val result = repository.verifyUserMpin(userId, pin)
            result.onSuccess { verifyResult ->
                val updatedUser = repository.getUserById(userId)
                when (verifyResult) {
                    MpinVerifyResult.Success -> {
                        if (updatedUser != null) {
                            _uiState.update {
                                it.copy(
                                    isBusy = false,
                                    mpinError = null,
                                    step = AuthGateStep.Unlocked(updatedUser)
                                )
                            }
                        } else {
                            logOut()
                        }
                    }

                    is MpinVerifyResult.WrongPin -> {
                        val msg = if (verifyResult.attemptsRemaining == 1) {
                            oneAttemptRemainingMsg
                        } else {
                            attemptsRemainingTemplate(verifyResult.attemptsRemaining)
                        }
                        _uiState.update {
                            it.copy(
                                isBusy = false,
                                mpinError = msg,
                                step = if (updatedUser != null) AuthGateStep.UnlockMpin(updatedUser) else it.step
                            )
                        }
                    }

                    is MpinVerifyResult.LockedOut -> {
                        _uiState.update {
                            it.copy(
                                isBusy = false,
                                mpinError = null,
                                step = if (updatedUser != null) AuthGateStep.UnlockMpin(updatedUser) else it.step
                            )
                        }
                    }

                    MpinVerifyResult.NoMpinSet -> {
                        _uiState.update {
                            it.copy(
                                isBusy = false,
                                mpinError = null,
                                step = if (updatedUser != null) {
                                    AuthGateStep.CreateMpin(updatedUser, isReset = false)
                                } else {
                                    AuthGateStep.Auth
                                }
                            )
                        }
                    }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        mpinError = storageFailedTemplate(err.localizedMessage ?: "Storage error")
                    )
                }
            }
        }
    }

    fun onLockoutTimerExpired(userId: Long) {
        viewModelScope.launch {
            repository.clearExpiredLockoutIfNeeded(userId)
            val updatedUser = repository.getUserById(userId)
            if (updatedUser != null) {
                _uiState.update {
                    it.copy(
                        mpinError = null,
                        step = AuthGateStep.UnlockMpin(updatedUser)
                    )
                }
            }
        }
    }

    fun verifyEmailForForgotMpin(
        userId: Long,
        emailInput: String,
        passwordInput: String,
        invalidEmailMsg: String,
        emailMismatchMsg: String,
        invalidCredentialsMsg: String,
        storageFailedTemplate: (String) -> String,
        onVerified: () -> Unit
    ) {
        if (!SecurityHasher.isValidEmail(emailInput)) {
            _uiState.update { it.copy(forgotMpinError = invalidEmailMsg) }
            return
        }
        if (passwordInput.isEmpty()) {
            _uiState.update { it.copy(forgotMpinError = invalidCredentialsMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, forgotMpinError = null) }
            val result = repository.verifyEmailForMpinReset(userId, emailInput, passwordInput)
            result.onSuccess {
                val user = repository.getUserById(userId)
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        forgotMpinError = null,
                        mpinError = null,
                        step = if (user != null) {
                            AuthGateStep.CreateMpin(user = user, isReset = true)
                        } else {
                            AuthGateStep.Auth
                        }
                    )
                }
                onVerified()
            }.onFailure { err ->
                val message = when {
                    err is AuthException && err.failure == AuthFailure.EmailMismatch -> emailMismatchMsg
                    err is AuthException && err.failure == AuthFailure.InvalidCredentials -> invalidCredentialsMsg
                    else -> storageFailedTemplate(err.localizedMessage ?: "Storage error")
                }
                _uiState.update { it.copy(isBusy = false, forgotMpinError = message) }
            }
        }
    }

    fun logOut() {
        AuthSessionPreferences.clearSession(appContext)
        _uiState.value = AuthUiState(step = AuthGateStep.Auth)
    }

    class Factory(
        private val appContext: Context,
        private val repository: KhataRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(appContext.applicationContext, repository) as T
        }
    }
}
