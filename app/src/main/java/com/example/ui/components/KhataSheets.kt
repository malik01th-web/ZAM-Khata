package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.EntryType
import com.example.data.KhataEntryEntity
import com.example.ui.theme.LocalKhataColors
import com.example.util.KhataFormatters
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendSheet(
    isSaving: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val khataColors = LocalKhataColors.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
            Text(
                text = stringResource(R.string.add_friend_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (errorMessage != null) {
                ErrorBanner(message = errorMessage)
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (errorMessage != null) onClearError()
                },
                label = { Text(stringResource(R.string.friend_name_label)) },
                placeholder = { Text(stringResource(R.string.friend_name_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = khataColors.positiveAccent
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friend_name_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '-' || it == ' ' }.take(13)
                    phone = filtered
                    if (errorMessage != null) onClearError()
                },
                label = { Text(stringResource(R.string.friend_phone_label)) },
                placeholder = { Text(stringResource(R.string.friend_phone_placeholder)) },
                supportingText = {
                    Text(
                        text = stringResource(R.string.friend_phone_helper),
                        color = khataColors.textMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friend_phone_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("cancel_add_friend_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onSave(name, phone) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = khataColors.limeButton,
                        contentColor = khataColors.onLimeButton
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 52.dp)
                        .testTag("save_friend_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = khataColors.onLimeButton
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.action_save_friend),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorSheet(
    editingEntry: KhataEntryEntity?,
    isSaving: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (amountText: String, type: EntryType, note: String, dateMillis: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val khataColors = LocalKhataColors.current
    var amountText by remember(editingEntry) {
        mutableStateOf(editingEntry?.let { KhataFormatters.paisaToInputString(it.amountPaisa) } ?: "")
    }
    var selectedType by remember(editingEntry) {
        mutableStateOf(editingEntry?.type ?: EntryType.UDHAAR_DIYA)
    }
    var noteText by remember(editingEntry) {
        mutableStateOf(editingEntry?.note ?: "")
    }
    var selectedDateMillis by remember(editingEntry) {
        mutableLongStateOf(editingEntry?.entryDateMillis ?: KhataFormatters.todayUtcMidnightMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEditingWapsi = editingEntry?.type?.isWapsi == true

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
            Text(
                text = stringResource(
                    if (editingEntry == null) R.string.add_entry_sheet_title
                    else R.string.edit_entry_sheet_title
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (errorMessage != null) {
                ErrorBanner(message = errorMessage)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.entry_type_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isEditingWapsi) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TypeSelectorCard(
                            title = stringResource(R.string.type_udhaar_diya),
                            subtitle = stringResource(R.string.type_udhaar_diya_hint),
                            selected = selectedType == EntryType.UDHAAR_DIYA,
                            accentColor = khataColors.positiveAccent,
                            selectedContainer = khataColors.positiveContainer,
                            onClick = { selectedType = EntryType.UDHAAR_DIYA },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_udhaar_diya_button")
                        )
                        TypeSelectorCard(
                            title = stringResource(R.string.type_udhaar_liya),
                            subtitle = stringResource(R.string.type_udhaar_liya_hint),
                            selected = selectedType == EntryType.UDHAAR_LIYA,
                            accentColor = khataColors.negativeAccent,
                            selectedContainer = khataColors.negativeContainer,
                            onClick = { selectedType = EntryType.UDHAAR_LIYA },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_udhaar_liya_button")
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TypeSelectorCard(
                            title = stringResource(R.string.type_wapsi_received),
                            subtitle = stringResource(R.string.wapsi_direction_received),
                            selected = selectedType == EntryType.WAPSI_RECEIVED,
                            accentColor = khataColors.positiveAccent,
                            selectedContainer = khataColors.positiveContainer,
                            onClick = { selectedType = EntryType.WAPSI_RECEIVED },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_wapsi_received_button")
                        )
                        TypeSelectorCard(
                            title = stringResource(R.string.type_wapsi_paid),
                            subtitle = stringResource(R.string.wapsi_direction_paid),
                            selected = selectedType == EntryType.WAPSI_PAID,
                            accentColor = MaterialTheme.colorScheme.primary,
                            selectedContainer = MaterialTheme.colorScheme.surfaceContainerHighest,
                            onClick = { selectedType = EntryType.WAPSI_PAID },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_wapsi_paid_button")
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    val filtered = sanitizeDecimalInput(raw)
                    amountText = filtered
                    if (errorMessage != null) onClearError()
                },
                label = { Text(stringResource(R.string.amount_label)) },
                placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                prefix = {
                    Text(
                        text = stringResource(R.string.currency_prefix),
                        color = khataColors.positiveAccent,
                        fontWeight = FontWeight.Bold
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_amount_input")
            )

            DateSelectorField(
                dateMillis = selectedDateMillis,
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag("entry_date_button")
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.entry_note_label)) },
                placeholder = { Text(stringResource(R.string.entry_note_placeholder)) },
                maxLines = 2,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_note_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
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
                    onClick = {
                        onSave(amountText, selectedType, noteText, selectedDateMillis)
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = khataColors.limeButton,
                        contentColor = khataColors.onLimeButton
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 52.dp)
                        .testTag("save_entry_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = khataColors.onLimeButton
                        )
                    } else {
                        Text(
                            text = stringResource(
                                if (editingEntry == null) R.string.action_save_entry
                                else R.string.action_update_entry
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        KhataDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { pickedMillis ->
                selectedDateMillis = pickedMillis
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordWapsiSheet(
    friendName: String,
    netBalancePaisa: Long,
    isSaving: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (amountText: String, direction: EntryType, note: String, dateMillis: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val khataColors = LocalKhataColors.current
    val defaultDirection = if (netBalancePaisa < 0L) {
        EntryType.WAPSI_PAID
    } else {
        EntryType.WAPSI_RECEIVED
    }
    var direction by remember(netBalancePaisa) { mutableStateOf(defaultDirection) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(KhataFormatters.todayUtcMidnightMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
                    text = stringResource(R.string.wapsi_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val formattedAbs = formatLocalizedRs(abs(netBalancePaisa))
                val statusHint = when {
                    netBalancePaisa > 0L ->
                        stringResource(R.string.wapsi_status_friend_owes, friendName, formattedAbs)
                    netBalancePaisa < 0L ->
                        stringResource(R.string.wapsi_status_you_owe, friendName, formattedAbs)
                    else -> stringResource(R.string.wapsi_status_settled, friendName)
                }
                Text(
                    text = statusHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (errorMessage != null) {
                ErrorBanner(message = errorMessage)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.wapsi_direction_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TypeSelectorCard(
                        title = stringResource(R.string.wapsi_direction_received),
                        subtitle = stringResource(R.string.wapsi_reduces_their_debt),
                        selected = direction == EntryType.WAPSI_RECEIVED,
                        accentColor = khataColors.positiveAccent,
                        selectedContainer = khataColors.positiveContainer,
                        onClick = { direction = EntryType.WAPSI_RECEIVED },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wapsi_received_toggle")
                    )
                    TypeSelectorCard(
                        title = stringResource(R.string.wapsi_direction_paid),
                        subtitle = stringResource(R.string.wapsi_reduces_your_debt),
                        selected = direction == EntryType.WAPSI_PAID,
                        accentColor = khataColors.negativeAccent,
                        selectedContainer = khataColors.negativeContainer,
                        onClick = { direction = EntryType.WAPSI_PAID },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wapsi_paid_toggle")
                    )
                }
            }

            if (netBalancePaisa != 0L) {
                val fullText = formatLocalizedRs(abs(netBalancePaisa))
                AssistChip(
                    onClick = {
                        amountText = KhataFormatters.paisaToInputString(abs(netBalancePaisa))
                        if (errorMessage != null) onClearError()
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.wapsi_full_balance_chip, fullText),
                            color = khataColors.positiveAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, khataColors.positiveAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("wapsi_fill_full_chip")
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    amountText = sanitizeDecimalInput(raw)
                    if (errorMessage != null) onClearError()
                },
                label = { Text(stringResource(R.string.amount_label)) },
                placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                prefix = {
                    Text(
                        text = stringResource(R.string.currency_prefix),
                        color = khataColors.positiveAccent,
                        fontWeight = FontWeight.Bold
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wapsi_amount_input")
            )

            DateSelectorField(
                dateMillis = selectedDateMillis,
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag("wapsi_date_button")
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.entry_note_label)) },
                placeholder = { Text(stringResource(R.string.wapsi_note_placeholder)) },
                maxLines = 2,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = khataTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wapsi_note_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
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
                    onClick = {
                        onSave(amountText, direction, noteText, selectedDateMillis)
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = khataColors.limeButton,
                        contentColor = khataColors.onLimeButton
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 52.dp)
                        .testTag("save_wapsi_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = khataColors.onLimeButton
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.action_save_wapsi),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        KhataDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { pickedMillis ->
                selectedDateMillis = pickedMillis
                showDatePicker = false
            }
        )
    }
}

@Composable
fun DeleteEntryDialog(
    entry: KhataEntryEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val khataColors = LocalKhataColors.current
    val typeLabel = entryTypeDisplayName(entry.type)
    val amountLabel = formatLocalizedRs(entry.amountPaisa)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.delete_entry_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_entry_dialog_message, typeLabel, amountLabel),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_entry_button")
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = khataColors.settledAccent
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KhataDatePickerDialog(
    initialDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val khataColors = LocalKhataColors.current
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = datePickerState.selectedDateMillis ?: initialDateMillis
                    onConfirm(selected)
                }
            ) {
                Text(
                    text = stringResource(R.string.action_ok),
                    color = khataColors.positiveAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = khataColors.textMuted,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = khataColors.positiveAccent,
                selectedYearContentColor = khataColors.onLimeButton,
                selectedYearContainerColor = khataColors.limeButton,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = khataColors.onLimeButton,
                selectedDayContainerColor = khataColors.limeButton,
                todayContentColor = khataColors.positiveAccent,
                todayDateBorderColor = khataColors.positiveAccent
            )
        )
    }
}

@Composable
private fun DateSelectorField(
    dateMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.entry_date_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = khataColors.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = KhataFormatters.formatUtcDate(dateMillis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = stringResource(R.string.entry_date_label),
                tint = khataColors.positiveAccent
            )
        }
    }
}

@Composable
private fun TypeSelectorCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: Color,
    selectedContainer: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) selectedContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else khataColors.textMuted
            )
        }
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = khataColors.negativeContainer,
        border = BorderStroke(1.dp, khataColors.negativeAccent.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = khataColors.negativeSoft,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = khataColors.negativeSoft
            )
        }
    }
}

@Composable
fun formatLocalizedRs(paisa: Long): String {
    val numberPart = KhataFormatters.formatRs(paisa, withPrefix = false)
    return stringResource(R.string.currency_rs_format, numberPart)
}

@Composable
fun entryTypeDisplayName(type: EntryType): String = when (type) {
    EntryType.UDHAAR_DIYA -> stringResource(R.string.type_udhaar_diya)
    EntryType.UDHAAR_LIYA -> stringResource(R.string.type_udhaar_liya)
    EntryType.WAPSI_RECEIVED -> stringResource(R.string.type_wapsi_received)
    EntryType.WAPSI_PAID -> stringResource(R.string.type_wapsi_paid)
}

@Composable
fun khataTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedBorderColor = LocalKhataColors.current.positiveAccent,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = LocalKhataColors.current.positiveAccent,
    focusedLabelColor = LocalKhataColors.current.positiveAccent,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = LocalKhataColors.current.textMuted,
    unfocusedPlaceholderColor = LocalKhataColors.current.textMuted
)

private fun sanitizeDecimalInput(raw: String): String {
    val sb = StringBuilder()
    var hasDot = false
    var decimals = 0
    for (ch in raw) {
        if (ch.isDigit()) {
            if (hasDot) {
                if (decimals < 2) {
                    sb.append(ch)
                    decimals++
                }
            } else if (sb.length < 9) {
                sb.append(ch)
            }
        } else if (ch == '.' && !hasDot) {
            if (sb.isEmpty()) sb.append('0')
            sb.append('.')
            hasDot = true
        }
    }
    return sb.toString()
}
