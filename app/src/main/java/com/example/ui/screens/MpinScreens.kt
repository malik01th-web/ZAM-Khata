package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserEntity
import com.example.ui.AuthUiState
import com.example.ui.AuthViewModel
import com.example.ui.components.ErrorBanner
import com.example.ui.components.khataTextFieldColors
import com.example.ui.theme.AccentLime
import com.example.ui.theme.LocalKhataColors
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CreateMpinScreen(
    user: UserEntity,
    isReset: Boolean,
    uiState: AuthUiState,
    viewModel: AuthViewModel
) {
    val khataColors = LocalKhataColors.current
    val context = LocalContext.current

    var firstPin by remember(user.id, isReset) { mutableStateOf("") }
    var confirmPin by remember(user.id, isReset) { mutableStateOf("") }
    var isConfirmStage by remember(user.id, isReset) { mutableStateOf(false) }
    var localMismatchError by remember(user.id, isReset) { mutableStateOf<String?>(null) }

    val activePin = if (isConfirmStage) confirmPin else firstPin
    val displayError = localMismatchError ?: uiState.mpinError

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.dp, AccentLime.copy(alpha = 0.6f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Pin,
                                contentDescription = null,
                                tint = AccentLime,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Text(
                        text = when {
                            isConfirmStage -> stringResource(R.string.mpin_confirm_title)
                            isReset -> stringResource(R.string.mpin_reset_title)
                            else -> stringResource(R.string.mpin_create_title)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = when {
                            isConfirmStage -> stringResource(R.string.mpin_confirm_subtitle)
                            isReset -> stringResource(R.string.mpin_reset_subtitle, user.email)
                            else -> stringResource(R.string.mpin_create_subtitle)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PinDotsRow(
                        length = activePin.length,
                        isError = displayError != null
                    )

                    if (displayError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        ErrorBanner(
                            message = displayError,
                            modifier = Modifier.testTag("create_mpin_error")
                        )
                    }
                }

                if (uiState.isBusy) {
                    CircularProgressIndicator(color = khataColors.positiveAccent)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NumericPinKeypad(
                        enabled = !uiState.isBusy,
                        onDigit = { digit ->
                            localMismatchError = null
                            viewModel.clearMpinError()
                            if (!isConfirmStage) {
                                if (firstPin.length < 4) {
                                    val next = firstPin + digit
                                    firstPin = next
                                    if (next.length == 4) {
                                        isConfirmStage = true
                                    }
                                }
                            } else {
                                if (confirmPin.length < 4) {
                                    val next = confirmPin + digit
                                    confirmPin = next
                                    if (next.length == 4) {
                                        if (next == firstPin) {
                                            viewModel.saveMpin(
                                                userId = user.id,
                                                pin = next,
                                                saveFailedTemplate = { reason ->
                                                    context.getString(R.string.mpin_error_save_failed, reason)
                                                }
                                            )
                                        } else {
                                            localMismatchError = context.getString(R.string.mpin_error_mismatch)
                                            firstPin = ""
                                            confirmPin = ""
                                            isConfirmStage = false
                                        }
                                    }
                                }
                            }
                        },
                        onBackspace = {
                            localMismatchError = null
                            if (!isConfirmStage) {
                                if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                            } else {
                                if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                            }
                        }
                    )

                    TextButton(
                        onClick = { viewModel.logOut() },
                        modifier = Modifier.testTag("create_mpin_switch_account")
                    ) {
                        Text(
                            text = stringResource(R.string.mpin_switch_account),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MpinLockScreen(
    user: UserEntity,
    uiState: AuthUiState,
    viewModel: AuthViewModel
) {
    val khataColors = LocalKhataColors.current
    val context = LocalContext.current

    var enteredPin by remember(user.id) { mutableStateOf("") }
    var showForgotSheet by remember { mutableStateOf(false) }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val remainingLockMillis = (user.mpinLockedUntilMillis - nowMillis).coerceAtLeast(0L)
    val isLocked = remainingLockMillis > 0L

    LaunchedEffect(user.mpinLockedUntilMillis) {
        if (user.mpinLockedUntilMillis > System.currentTimeMillis()) {
            while (true) {
                val current = System.currentTimeMillis()
                nowMillis = current
                if (current >= user.mpinLockedUntilMillis) {
                    viewModel.onLockoutTimerExpired(user.id)
                    break
                }
                delay(1000L)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLocked) khataColors.negativeContainer else MaterialTheme.colorScheme.primary,
                        border = BorderStroke(
                            1.dp,
                            if (isLocked) khataColors.negativeAccent else AccentLime.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isLocked) Icons.Rounded.LockClock else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (isLocked) khataColors.negativeAccent else AccentLime,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(
                            if (isLocked) R.string.mpin_locked_title
                            else R.string.mpin_unlock_title
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.mpin_unlock_subtitle, user.email),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PinDotsRow(
                        length = enteredPin.length,
                        isError = isLocked || uiState.mpinError != null
                    )

                    if (isLocked) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val countdown = formatCountdown(remainingLockMillis)
                        ErrorBanner(
                            message = stringResource(R.string.mpin_locked_message, countdown),
                            modifier = Modifier.testTag("mpin_lockout_banner")
                        )
                    } else if (uiState.mpinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        ErrorBanner(
                            message = uiState.mpinError,
                            modifier = Modifier.testTag("mpin_error_banner")
                        )
                    }
                }

                if (uiState.isBusy) {
                    CircularProgressIndicator(color = khataColors.positiveAccent)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NumericPinKeypad(
                        enabled = !isLocked && !uiState.isBusy,
                        onDigit = { digit ->
                            if (uiState.mpinError != null) viewModel.clearMpinError()
                            if (enteredPin.length < 4) {
                                val next = enteredPin + digit
                                enteredPin = next
                                if (next.length == 4) {
                                    val submitted = next
                                    enteredPin = ""
                                    viewModel.verifyMpin(
                                        userId = user.id,
                                        pin = submitted,
                                        oneAttemptRemainingMsg = context.getString(R.string.mpin_wrong_attempt_one_remaining),
                                        attemptsRemainingTemplate = { rem ->
                                            context.getString(R.string.mpin_wrong_attempts_remaining, rem)
                                        },
                                        storageFailedTemplate = { reason ->
                                            context.getString(R.string.error_auth_storage_failed, reason)
                                        }
                                    )
                                }
                            }
                        },
                        onBackspace = {
                            if (enteredPin.isNotEmpty()) {
                                enteredPin = enteredPin.dropLast(1)
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.logOut() },
                            modifier = Modifier.testTag("mpin_switch_account_button")
                        ) {
                            Text(
                                text = stringResource(R.string.mpin_switch_account),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.clearForgotMpinError()
                                showForgotSheet = true
                            },
                            modifier = Modifier.testTag("forgot_mpin_button")
                        ) {
                            Text(
                                text = stringResource(R.string.mpin_forgot_button),
                                color = khataColors.positiveAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForgotSheet) {
        ForgotMpinVerifySheet(
            isBusy = uiState.isBusy,
            errorMessage = uiState.forgotMpinError,
            onClearError = viewModel::clearForgotMpinError,
            onDismiss = { showForgotSheet = false },
            onVerify = { emailInput, passwordInput ->
                viewModel.verifyEmailForForgotMpin(
                    userId = user.id,
                    emailInput = emailInput,
                    passwordInput = passwordInput,
                    invalidEmailMsg = context.getString(R.string.error_invalid_email),
                    emailMismatchMsg = context.getString(R.string.error_email_mismatch_for_reset),
                    invalidCredentialsMsg = context.getString(R.string.error_invalid_credentials),
                    storageFailedTemplate = { reason ->
                        context.getString(R.string.error_auth_storage_failed, reason)
                    },
                    onVerified = { showForgotSheet = false }
                )
            }
        )
    }
}

@Composable
private fun PinDotsRow(
    length: Int,
    isError: Boolean
) {
    val khataColors = LocalKhataColors.current
    // Keep 4 dots left-to-right consistently with keypad input
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("mpin_dots_row")
        ) {
            repeat(4) { index ->
                val filled = index < length
                val dotColor = when {
                    isError && filled -> khataColors.negativeAccent
                    filled -> khataColors.positiveAccent
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
                val borderColor = when {
                    isError -> khataColors.negativeAccent.copy(alpha = 0.7f)
                    filled -> khataColors.positiveAccent
                    else -> MaterialTheme.colorScheme.outline
                }
                Surface(
                    shape = CircleShape,
                    color = dotColor,
                    border = BorderStroke(2.dp, borderColor),
                    modifier = Modifier
                        .size(20.dp)
                        .testTag("mpin_dot_$index")
                ) {}
            }
        }
    }
}

@Composable
private fun NumericPinKeypad(
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    // Numeric keypad digits always remain in standard 1-2-3 LTR layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )
            for (rowDigits in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (digit in rowDigits) {
                        KeypadDigitButton(
                            digit = digit,
                            enabled = enabled,
                            onClick = { onDigit(digit) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(72.dp))
                KeypadDigitButton(
                    digit = "0",
                    enabled = enabled,
                    onClick = { onDigit("0") }
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(enabled = enabled, onClick = onBackspace)
                        .testTag("keypad_backspace")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = stringResource(R.string.cd_backspace),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadDigitButton(
    digit: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (enabled) 2.dp else 0.dp,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("keypad_digit_$digit")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                if (enabled) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            )
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.headlineSmall,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotMpinVerifySheet(
    isBusy: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onVerify: (email: String, password: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val khataColors = LocalKhataColors.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { if (!isBusy) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.forgot_mpin_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.forgot_mpin_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (errorMessage != null) {
                ErrorBanner(message = errorMessage)
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (errorMessage != null) onClearError()
                },
                label = { Text(stringResource(R.string.auth_email_label)) },
                placeholder = { Text(stringResource(R.string.auth_email_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null,
                        tint = khataColors.positiveAccent
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_mpin_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorMessage != null) onClearError()
                },
                label = { Text(stringResource(R.string.auth_password_label)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_mpin_password_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onVerify(email, password) },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = khataColors.limeButton,
                        contentColor = khataColors.onLimeButton
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 52.dp)
                        .testTag("forgot_mpin_verify_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = khataColors.onLimeButton
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.forgot_mpin_verify_cta),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatCountdown(remainingMillis: Long): String {
    val totalSeconds = ((remainingMillis + 999L) / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
