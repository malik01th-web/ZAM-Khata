package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.EntryType
import com.example.data.KhataEntryEntity
import com.example.ui.FriendDetailViewModel
import com.example.ui.components.DeleteEntryDialog
import com.example.ui.components.EntryEditorSheet
import com.example.ui.components.ErrorBanner
import com.example.ui.components.RecordWapsiSheet
import com.example.ui.components.entryTypeDisplayName
import com.example.ui.components.formatLocalizedRs
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.theme.LocalKhataColors
import com.example.util.KhataFormatters
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    viewModel: FriendDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val khataColors = LocalKhataColors.current

    var showEntryEditor by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<KhataEntryEntity?>(null) }
    var showWapsiSheet by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<KhataEntryEntity?>(null) }

    LaunchedEffect(state.screenError) {
        val message = state.screenError
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissScreenError()
        }
    }

    val friend = state.friend

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = friend?.name ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val formattedPhone = KhataFormatters.formatPhoneForDisplay(friend?.phone)
                        if (formattedPhone != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phone,
                                    contentDescription = null,
                                    tint = khataColors.positiveAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = formattedPhone,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDirection = TextDirection.Ltr
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!state.isLoading && friend == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.friend_not_found_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.friend_not_found_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (friend != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "balance_hero") {
                        FriendBalanceHeroCard(
                            friendName = friend.name,
                            netBalancePaisa = state.netBalancePaisa,
                            totalLentPaisa = state.totalLentPaisa,
                            totalBorrowedPaisa = state.totalBorrowedPaisa,
                            totalWapsiPaisa = state.totalWapsiPaisa
                        )
                    }

                    item(key = "action_buttons") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.clearSheetError()
                                    editingEntry = null
                                    showEntryEditor = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp)
                                    .testTag("add_entry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.action_add_entry),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.clearSheetError()
                                    showWapsiSheet = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = khataColors.limeButton,
                                    contentColor = khataColors.onLimeButton
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp)
                                    .testTag("record_wapsi_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.action_record_wapsi),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (state.screenError != null) {
                        item(key = "screen_error") {
                            ErrorBanner(message = state.screenError!!)
                        }
                    }

                    item(key = "history_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.history_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${state.entries.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = khataColors.positiveAccent
                            )
                        }
                    }

                    if (state.entries.isEmpty()) {
                        item(key = "empty_history") {
                            EmptyHistoryCard()
                        }
                    } else {
                        items(
                            items = state.entries,
                            key = { it.id }
                        ) { entry ->
                            EntryHistoryCard(
                                entry = entry,
                                onEdit = {
                                    viewModel.clearSheetError()
                                    editingEntry = entry
                                    showEntryEditor = true
                                },
                                onDelete = { pendingDeleteEntry = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEntryEditor) {
        EntryEditorSheet(
            editingEntry = editingEntry,
            isSaving = state.isSaving,
            errorMessage = state.sheetError,
            onClearError = viewModel::clearSheetError,
            onDismiss = {
                showEntryEditor = false
                editingEntry = null
            },
            onSave = { amountText, type, note, dateMillis ->
                val currentEditing = editingEntry
                viewModel.saveEntry(
                    editingEntry = currentEditing,
                    amountInput = amountText,
                    type = type,
                    noteInput = note,
                    entryDateMillis = dateMillis,
                    invalidAmountMsg = context.getString(R.string.error_invalid_amount),
                    saveFailedTemplate = { reason ->
                        if (currentEditing == null) {
                            context.getString(R.string.error_save_entry_failed, reason)
                        } else {
                            context.getString(R.string.error_update_entry_failed, reason)
                        }
                    },
                    onSuccess = {
                        showEntryEditor = false
                        editingEntry = null
                    }
                )
            }
        )
    }

    if (showWapsiSheet && friend != null) {
        RecordWapsiSheet(
            friendName = friend.name,
            netBalancePaisa = state.netBalancePaisa,
            isSaving = state.isSaving,
            errorMessage = state.sheetError,
            onClearError = viewModel::clearSheetError,
            onDismiss = { showWapsiSheet = false },
            onSave = { amountText, direction, note, dateMillis ->
                viewModel.recordWapsi(
                    amountInput = amountText,
                    direction = direction,
                    noteInput = note,
                    entryDateMillis = dateMillis,
                    invalidAmountMsg = context.getString(R.string.error_invalid_amount),
                    saveFailedTemplate = { reason ->
                        context.getString(R.string.error_save_entry_failed, reason)
                    },
                    onSuccess = { showWapsiSheet = false }
                )
            }
        )
    }

    pendingDeleteEntry?.let { entryToDelete ->
        DeleteEntryDialog(
            entry = entryToDelete,
            onDismiss = { pendingDeleteEntry = null },
            onConfirm = {
                pendingDeleteEntry = null
                viewModel.deleteEntry(
                    entryId = entryToDelete.id,
                    deleteFailedTemplate = { reason ->
                        context.getString(R.string.error_delete_entry_failed, reason)
                    }
                )
            }
        )
    }
}

@Composable
private fun FriendBalanceHeroCard(
    friendName: String,
    netBalancePaisa: Long,
    totalLentPaisa: Long,
    totalBorrowedPaisa: Long,
    totalWapsiPaisa: Long
) {
    val khataColors = LocalKhataColors.current
    val balanceColor = when {
        netBalancePaisa > 0L -> khataColors.heroPositiveAccent
        netBalancePaisa < 0L -> khataColors.heroNegativeAccent
        else -> khataColors.heroSettledAccent
    }
    val whoOwesWhom = when {
        netBalancePaisa > 0L -> stringResource(R.string.friend_owes_you_title, friendName)
        netBalancePaisa < 0L -> stringResource(R.string.you_owe_friend_title, friendName)
        else -> stringResource(R.string.settled_hero_title)
    }
    val gradient = Brush.linearGradient(
        colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd)
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, balanceColor.copy(alpha = 0.45f)),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = whoOwesWhom,
                    style = MaterialTheme.typography.titleMedium,
                    color = khataColors.heroTextSecondary,
                    modifier = Modifier.testTag("friend_who_owes_text")
                )
                Text(
                    text = if (netBalancePaisa == 0L) {
                        formatLocalizedRs(0L)
                    } else {
                        formatLocalizedRs(abs(netBalancePaisa))
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = balanceColor,
                    modifier = Modifier.testTag("friend_balance_amount_text")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BreakdownChip(
                    label = stringResource(R.string.stat_total_lent),
                    value = formatLocalizedRs(totalLentPaisa),
                    valueColor = khataColors.heroPositiveAccent,
                    modifier = Modifier.weight(1f)
                )
                BreakdownChip(
                    label = stringResource(R.string.stat_total_borrowed),
                    value = formatLocalizedRs(totalBorrowedPaisa),
                    valueColor = khataColors.heroNegativeAccent,
                    modifier = Modifier.weight(1f)
                )
                BreakdownChip(
                    label = stringResource(R.string.stat_total_repaid),
                    value = formatLocalizedRs(totalWapsiPaisa),
                    valueColor = khataColors.heroTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BreakdownChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = khataColors.heroChipBackground,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = khataColors.heroTextSecondary.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EntryHistoryCard(
    entry: KhataEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val khataColors = LocalKhataColors.current
    val badgeColor = when (entry.type) {
        EntryType.UDHAAR_DIYA -> khataColors.positiveAccent
        EntryType.UDHAAR_LIYA -> khataColors.negativeAccent
        EntryType.WAPSI_RECEIVED -> khataColors.positiveAccent
        EntryType.WAPSI_PAID -> khataColors.settledAccent
    }
    val badgeContainer = when (entry.type) {
        EntryType.UDHAAR_DIYA -> khataColors.positiveContainer
        EntryType.UDHAAR_LIYA -> khataColors.negativeContainer
        EntryType.WAPSI_RECEIVED -> MaterialTheme.colorScheme.surfaceContainerHighest
        EntryType.WAPSI_PAID -> khataColors.settledContainer
    }
    val signedPrefix = when (entry.type) {
        EntryType.UDHAAR_DIYA, EntryType.WAPSI_PAID -> "+"
        EntryType.UDHAAR_LIYA, EntryType.WAPSI_RECEIVED -> "-"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (khataColors.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("entry_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeContainer,
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = entryTypeDisplayName(entry.type),
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "$signedPrefix${formatLocalizedRs(entry.amountPaisa)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = badgeColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            if (!entry.note.isNullOrBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = KhataFormatters.formatUtcDate(entry.entryDateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = khataColors.textMuted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.cd_edit_entry),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_entry),
                            tint = khataColors.negativeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircleOutline,
                contentDescription = null,
                tint = khataColors.positiveAccent,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = stringResource(R.string.empty_history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.empty_history_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
