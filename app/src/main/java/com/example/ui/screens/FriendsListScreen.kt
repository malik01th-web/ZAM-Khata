package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.FriendSummaryRow
import com.example.ui.FriendsListViewModel
import com.example.ui.components.AddFriendSheet
import com.example.ui.components.ErrorBanner
import com.example.ui.components.formatLocalizedRs
import com.example.ui.components.khataTextFieldColors
import com.example.ui.theme.AccentLime
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.theme.LocalKhataColors
import com.example.util.AppLanguage
import com.example.util.AppThemeMode
import com.example.util.KhataFormatters
import kotlin.math.abs

@Composable
fun FriendsListScreen(
    viewModel: FriendsListViewModel,
    userEmail: String,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit,
    onLogOut: () -> Unit,
    onFriendClick: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val khataColors = LocalKhataColors.current
    var showAddFriendSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.bannerError) {
        val message = state.bannerError
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissBannerError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.clearAddFriendError()
                    showAddFriendSheet = true
                },
                containerColor = khataColors.limeButton,
                contentColor = khataColors.onLimeButton,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.add_friend_cta),
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("add_friend_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "header") {
                    BrandHeader(
                        userEmail = userEmail,
                        friendCount = state.allFriends.size,
                        currentLanguage = currentLanguage,
                        onSelectLanguage = onSelectLanguage,
                        themeMode = themeMode,
                        onToggleTheme = onToggleTheme,
                        onRequestLogout = { showLogoutDialog = true }
                    )
                }

                item(key = "summary_card") {
                    OverallSummaryCard(
                        netBalancePaisa = state.netBalancePaisa,
                        toReceivePaisa = state.totalToReceivePaisa,
                        toPayPaisa = state.totalToPayPaisa
                    )
                }

                if (state.bannerError != null) {
                    item(key = "error_banner") {
                        ErrorBanner(message = state.bannerError!!)
                    }
                }

                if (state.allFriends.isNotEmpty()) {
                    item(key = "search_bar") {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_friends_placeholder),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = khataColors.positiveAccent
                                )
                            },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.onSearchQueryChange("") },
                                        modifier = Modifier.testTag("clear_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Clear,
                                            contentDescription = stringResource(R.string.cd_clear_search),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = khataTextFieldColors(),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_friends_input")
                        )
                    }

                    item(key = "friends_heading") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.friends_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${state.filteredFriends.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = khataColors.positiveAccent
                            )
                        }
                    }
                }

                if (state.allFriends.isEmpty() && !state.isLoading) {
                    item(key = "empty_friends") {
                        EmptyFriendsCard()
                    }
                } else if (state.filteredFriends.isEmpty() && !state.isLoading) {
                    item(key = "empty_search") {
                        EmptySearchCard()
                    }
                } else {
                    items(
                        items = state.filteredFriends,
                        key = { it.id }
                    ) { friend ->
                        FriendRowCard(
                            friend = friend,
                            onClick = { onFriendClick(friend.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddFriendSheet) {
        AddFriendSheet(
            isSaving = state.isSavingFriend,
            errorMessage = state.addFriendError,
            onClearError = viewModel::clearAddFriendError,
            onDismiss = { showAddFriendSheet = false },
            onSave = { name, phone ->
                viewModel.addFriend(
                    nameInput = name,
                    phoneInput = phone,
                    requiredNameMsg = context.getString(R.string.error_friend_name_required),
                    invalidPhoneMsg = context.getString(R.string.error_invalid_pakistan_phone),
                    saveFailedTemplate = { reason ->
                        context.getString(R.string.error_save_friend_failed, reason)
                    },
                    onSuccess = { showAddFriendSheet = false }
                )
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = stringResource(R.string.logout_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_dialog_message, userEmail),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_logout),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun BrandHeader(
    userEmail: String,
    friendCount: Int,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit,
    onRequestLogout: () -> Unit
) {
    val khataColors = LocalKhataColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.labelMedium.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThemeToggleButton(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                LanguageTogglePill(
                    currentLanguage = currentLanguage,
                    onSelectLanguage = onSelectLanguage
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onRequestLogout)
                        .testTag("logout_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = stringResource(R.string.cd_logout),
                            tint = khataColors.negativeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.dp, AccentLime.copy(alpha = 0.6f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = AccentLime,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = if (friendCount > 0) {
                    val countLabel = if (friendCount == 1) {
                        stringResource(R.string.friend_count_singular)
                    } else {
                        stringResource(R.string.friend_count_plural, friendCount)
                    }
                    "${stringResource(R.string.app_subtitle)} • $countLabel"
                } else {
                    stringResource(R.string.app_subtitle)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = khataColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun ThemeToggleButton(
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit
) {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onToggleTheme)
            .testTag("theme_toggle_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (themeMode.isDark) {
                    Icons.Rounded.LightMode
                } else {
                    Icons.Rounded.DarkMode
                },
                contentDescription = stringResource(
                    if (themeMode.isDark) R.string.cd_switch_to_light
                    else R.string.cd_switch_to_dark
                ),
                tint = if (themeMode.isDark) AccentLime else khataColors.settledAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun LanguageTogglePill(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.testTag("language_toggle_pill")
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageOptionChip(
                label = "EN",
                selected = currentLanguage == AppLanguage.ENGLISH,
                onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                modifier = Modifier.testTag("language_option_en")
            )
            LanguageOptionChip(
                label = "اردو",
                selected = currentLanguage == AppLanguage.URDU,
                onClick = { onSelectLanguage(AppLanguage.URDU) },
                modifier = Modifier.testTag("language_option_ur")
            )
        }
    }
}

@Composable
private fun LanguageOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val khataColors = LocalKhataColors.current
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) khataColors.limeButton else Color.Transparent,
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) khataColors.onLimeButton else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OverallSummaryCard(
    netBalancePaisa: Long,
    toReceivePaisa: Long,
    toPayPaisa: Long
) {
    val khataColors = LocalKhataColors.current
    val gradient = Brush.linearGradient(
        colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd)
    )
    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AccentLime.copy(alpha = 0.4f)),
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
                    text = stringResource(R.string.net_balance_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = khataColors.heroTextSecondary
                )
                val netColor = when {
                    netBalancePaisa > 0L -> khataColors.heroPositiveAccent
                    netBalancePaisa < 0L -> khataColors.heroNegativeAccent
                    else -> khataColors.heroTextPrimary
                }
                val netPrefix = when {
                    netBalancePaisa > 0L -> "+"
                    netBalancePaisa < 0L -> "-"
                    else -> ""
                }
                Text(
                    text = "$netPrefix${formatLocalizedRs(netBalancePaisa)}",
                    style = MaterialTheme.typography.displaySmall,
                    color = netColor,
                    modifier = Modifier.testTag("overall_net_balance_text")
                )
                val netHint = when {
                    netBalancePaisa > 0L -> stringResource(R.string.net_hint_positive)
                    netBalancePaisa < 0L -> stringResource(R.string.net_hint_negative)
                    else -> stringResource(R.string.net_hint_settled)
                }
                Text(
                    text = netHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = khataColors.heroTextSecondary.copy(alpha = 0.9f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatPill(
                    label = stringResource(R.string.total_to_receive_label),
                    amountText = formatLocalizedRs(toReceivePaisa),
                    accentColor = khataColors.heroPositiveAccent,
                    containerColor = khataColors.heroPositiveContainer,
                    labelColor = khataColors.heroTextSecondary,
                    icon = Icons.Rounded.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
                MiniStatPill(
                    label = stringResource(R.string.total_to_give_label),
                    amountText = formatLocalizedRs(toPayPaisa),
                    accentColor = khataColors.heroNegativeAccent,
                    containerColor = khataColors.heroNegativeContainer,
                    labelColor = khataColors.heroTextSecondary,
                    icon = Icons.Rounded.ArrowUpward,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniStatPill(
    label: String,
    amountText: String,
    accentColor: Color,
    containerColor: Color,
    labelColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FriendRowCard(
    friend: FriendSummaryRow,
    onClick: () -> Unit
) {
    val khataColors = LocalKhataColors.current
    val balance = friend.netBalancePaisa
    val statusColor = when {
        balance > 0L -> khataColors.positiveAccent
        balance < 0L -> khataColors.negativeAccent
        else -> khataColors.settledAccent
    }
    val statusContainer = when {
        balance > 0L -> khataColors.positiveContainer
        balance < 0L -> khataColors.negativeContainer
        else -> khataColors.settledContainer
    }
    val statusLabel = when {
        balance > 0L -> stringResource(R.string.label_owes_you)
        balance < 0L -> stringResource(R.string.label_you_owe)
        else -> stringResource(R.string.label_settled)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (khataColors.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("friend_card_${friend.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.65f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = KhataFormatters.initialsForName(friend.name),
                        style = MaterialTheme.typography.titleSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedPhone = KhataFormatters.formatPhoneForDisplay(friend.phone)
                val entryText = if (friend.entryCount == 1) {
                    stringResource(R.string.label_entry_singular, 1)
                } else {
                    stringResource(R.string.label_entry_plural, friend.entryCount)
                }
                val metaLine = buildString {
                    if (formattedPhone != null) {
                        append(formattedPhone)
                        append(" • ")
                    }
                    append(entryText)
                }
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr),
                    color = khataColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusContainer
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = if (balance == 0L) formatLocalizedRs(0L) else formatLocalizedRs(abs(balance)),
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = khataColors.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EmptyFriendsCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_khata_empty),
                contentDescription = stringResource(R.string.empty_friends_title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Text(
                text = stringResource(R.string.empty_friends_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.empty_friends_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptySearchCard() {
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_search_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.empty_search_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = khataColors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
