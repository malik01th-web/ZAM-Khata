package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.KhataRepository
import com.example.data.ZamKhataDatabase
import com.example.ui.AuthGateStep
import com.example.ui.AuthViewModel
import com.example.ui.FriendDetailViewModel
import com.example.ui.FriendsListViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateMpinScreen
import com.example.ui.screens.FriendDetailScreen
import com.example.ui.screens.FriendsListScreen
import com.example.ui.screens.MpinLockScreen
import com.example.ui.theme.BgDeepPurple
import com.example.ui.theme.LightBgLavender
import com.example.ui.theme.LocalKhataColors
import com.example.ui.theme.ZamKhataTheme
import com.example.util.AppLanguage
import com.example.util.AppThemeMode
import com.example.util.LanguagePreferences
import com.example.util.ThemePreferences

private object ZamKhataRoutes {
    const val FRIENDS_LIST = "friends_list"
    const val FRIEND_DETAIL_PATTERN = "friend_detail/{friendId}"
    const val ARG_FRIEND_ID = "friendId"

    fun friendDetail(friendId: Long): String = "friend_detail/$friendId"
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentLanguage = LanguagePreferences.getSelectedLanguage(this)
        val initialThemeMode = ThemePreferences.getThemeMode(this)
        val database = ZamKhataDatabase.getInstance(applicationContext)
        val repository = KhataRepository(database.khataDao())

        setContent {
            var themeMode by remember { mutableStateOf(initialThemeMode) }

            LaunchedEffect(themeMode) {
                if (themeMode.isDark) {
                    val darkScrim = BgDeepPurple.toArgb()
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(darkScrim),
                        navigationBarStyle = SystemBarStyle.dark(darkScrim)
                    )
                } else {
                    val lightScrim = LightBgLavender.toArgb()
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(lightScrim, lightScrim),
                        navigationBarStyle = SystemBarStyle.light(lightScrim, lightScrim)
                    )
                }
            }

            val layoutDirection = if (currentLanguage.isRtl) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                ZamKhataTheme(darkTheme = themeMode.isDark) {
                    val authFactory = remember(applicationContext, repository) {
                        AuthViewModel.Factory(applicationContext, repository)
                    }
                    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

                    val selectLanguage: (AppLanguage) -> Unit = { selected ->
                        if (selected != currentLanguage) {
                            LanguagePreferences.setSelectedLanguage(this, selected)
                            recreate()
                        }
                    }
                    val toggleTheme: () -> Unit = {
                        val next = if (themeMode.isDark) {
                            AppThemeMode.LIGHT
                        } else {
                            AppThemeMode.DARK
                        }
                        ThemePreferences.setThemeMode(this, next)
                        themeMode = next
                    }

                    when (val step = authUiState.step) {
                        AuthGateStep.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = LocalKhataColors.current.positiveAccent
                                )
                            }
                        }

                        AuthGateStep.Auth -> {
                            AuthScreen(
                                uiState = authUiState,
                                viewModel = authViewModel,
                                currentLanguage = currentLanguage,
                                onSelectLanguage = selectLanguage,
                                themeMode = themeMode,
                                onToggleTheme = toggleTheme
                            )
                        }

                        is AuthGateStep.CreateMpin -> {
                            CreateMpinScreen(
                                user = step.user,
                                isReset = step.isReset,
                                uiState = authUiState,
                                viewModel = authViewModel
                            )
                        }

                        is AuthGateStep.UnlockMpin -> {
                            MpinLockScreen(
                                user = step.user,
                                uiState = authUiState,
                                viewModel = authViewModel
                            )
                        }

                        is AuthGateStep.Unlocked -> {
                            key(step.user.id) {
                                ZamKhataApp(
                                    userId = step.user.id,
                                    userEmail = step.user.email,
                                    repository = repository,
                                    currentLanguage = currentLanguage,
                                    onSelectLanguage = selectLanguage,
                                    themeMode = themeMode,
                                    onToggleTheme = toggleTheme,
                                    onLogOut = { authViewModel.logOut() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZamKhataApp(
    userId: Long,
    userEmail: String,
    repository: KhataRepository,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit,
    onLogOut: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ZamKhataRoutes.FRIENDS_LIST
    ) {
        composable(route = ZamKhataRoutes.FRIENDS_LIST) {
            val factory = remember(userId, repository) {
                FriendsListViewModel.Factory(userId = userId, repository = repository)
            }
            val viewModel: FriendsListViewModel = viewModel(
                key = "friends_list_$userId",
                factory = factory
            )
            FriendsListScreen(
                viewModel = viewModel,
                userEmail = userEmail,
                currentLanguage = currentLanguage,
                onSelectLanguage = onSelectLanguage,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onLogOut = onLogOut,
                onFriendClick = { friendId ->
                    navController.navigate(ZamKhataRoutes.friendDetail(friendId))
                }
            )
        }

        composable(
            route = ZamKhataRoutes.FRIEND_DETAIL_PATTERN,
            arguments = listOf(
                navArgument(ZamKhataRoutes.ARG_FRIEND_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getLong(ZamKhataRoutes.ARG_FRIEND_ID) ?: -1L
            val factory = remember(userId, friendId, repository) {
                FriendDetailViewModel.Factory(
                    userId = userId,
                    friendId = friendId,
                    repository = repository
                )
            }
            val viewModel: FriendDetailViewModel = viewModel(
                key = "friend_detail_${userId}_$friendId",
                factory = factory
            )
            FriendDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
