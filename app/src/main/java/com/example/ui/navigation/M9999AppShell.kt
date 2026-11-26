package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.DataMode
import com.example.data.model.MiniGameId
import com.example.data.model.PlayerAccount
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.M9999LogoMark
import com.example.ui.components.ObsidianCard
import com.example.ui.screens.games.CardRevealScreen
import com.example.ui.screens.games.DiceGameScreen
import com.example.ui.screens.games.LuckyNumberScreen
import com.example.ui.screens.games.LuckyWheelScreen
import com.example.ui.screens.games.MemoryMatchScreen
import com.example.ui.screens.games.SlotMachineScreen
import com.example.ui.screens.main.DailyRewardsScreen
import com.example.ui.screens.main.GamesHubScreen
import com.example.ui.screens.main.HomeDashboardScreen
import com.example.ui.screens.main.LeaderboardScreen
import com.example.ui.screens.main.ProfileScreen
import com.example.ui.screens.secondary.AboutUsScreen
import com.example.ui.screens.secondary.AdminDashboardScreen
import com.example.ui.screens.secondary.GameHistoryScreen
import com.example.ui.screens.secondary.HelpSupportScreen
import com.example.ui.screens.secondary.NotificationsScreen
import com.example.ui.screens.secondary.PrivacyPolicyScreen
import com.example.ui.screens.secondary.SettingsScreen
import com.example.ui.screens.secondary.TermsConditionsScreen
import com.example.ui.screens.secondary.WalletScreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DataState
import com.example.ui.viewmodel.M9999ViewModel

private data class TopLevelNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val TOP_LEVEL_ITEMS = listOf(
  TopLevelNavItem(NavRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
  TopLevelNavItem(NavRoutes.GAMES, "Games", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
  TopLevelNavItem(NavRoutes.REWARDS, "Rewards", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard),
  TopLevelNavItem(NavRoutes.LEADERBOARD, "Rank", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
  TopLevelNavItem(NavRoutes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun M9999AppShell(
  viewModel: M9999ViewModel,
  onLogout: () -> Unit,
) {
  val navController = rememberNavController()
  val navBackStack by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStack?.destination?.route ?: NavRoutes.HOME

  val profileState by viewModel.profileState.collectAsStateWithLifecycle()
  val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
  val historyState by viewModel.gameHistoryState.collectAsStateWithLifecycle()
  val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
  val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
  val announcementsState by viewModel.announcementsState.collectAsStateWithLifecycle()
  val config by viewModel.configState.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val isAdminVerified by viewModel.isAdminVerified.collectAsStateWithLifecycle()
  val adminUsersState by viewModel.adminUsersState.collectAsStateWithLifecycle()
  val activityLogsState by viewModel.activityLogsState.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
  val errorBanner by viewModel.errorBanner.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(statusMessage, errorBanner) {
    val msg = errorBanner ?: statusMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearMessages()
    }
  }

  val transactions = (transactionsState as? DataState.Success)?.data.orEmpty()
  val history = (historyState as? DataState.Success)?.data.orEmpty()
  val notifications = (notificationsState as? DataState.Success)?.data.orEmpty()
  val leaderboard = (leaderboardState as? DataState.Success)?.data.orEmpty()
  val announcements = (announcementsState as? DataState.Success)?.data.orEmpty()
  val adminUsers = (adminUsersState as? DataState.Success)?.data.orEmpty()
  val activityLogs = (activityLogsState as? DataState.Success)?.data.orEmpty()
  val unreadNotifications = notifications.count { it.unread }

  val isTopLevel = TOP_LEVEL_ITEMS.any { it.route == currentRoute }

  fun navigateTopLevel(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
  ) {
    when (val pState = profileState) {
      is DataState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GoldBright)
            Spacer(Modifier.height(12.dp))
            Text("Loading M9999 Lounge...", color = TextSecondary)
          }
        }
      }
      is DataState.Error -> {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
          ObsidianCard(modifier = Modifier.widthIn(max = 440.dp)) {
            Text("Unable to load account", style = MaterialTheme.typography.titleLarge, color = RubyLoss)
            Spacer(Modifier.height(6.dp))
            Text(pState.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(14.dp))
            GoldGradientButton(text = "Sign Out", onClick = onLogout, testTag = "error_logout_button")
          }
        }
      }
      is DataState.Success -> {
        val account = pState.data
        if (account == null) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldBright)
          }
        } else if (account.accountStatus == "DISABLED") {
          DisabledAccountGate(
            account = account,
            mode = viewModel.mode,
            isAdminVerified = isAdminVerified,
            onOpenAdmin = {
              // Only reachable if verified admin disabled their own demo account
              viewModel.adminToggleAccountStatus(account.userId, account.accountStatus)
            },
            onLogout = onLogout,
          )
        } else {
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
              containerColor = ObsidianBlack,
              contentWindowInsets = WindowInsets.safeDrawing,
              snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
              bottomBar = {
                if (!useNavigationRail && isTopLevel) {
                  NavigationBar(
                    containerColor = ObsidianSurface,
                    contentColor = GoldBright,
                  ) {
                    TOP_LEVEL_ITEMS.forEach { item ->
                      val selected = currentRoute == item.route
                      NavigationBarItem(
                        selected = selected,
                        onClick = { navigateTopLevel(item.route) },
                        modifier = Modifier.testTag("bottom_nav_${item.route}"),
                        icon = {
                          Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                          )
                        },
                        label = { Text(item.label, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                          selectedIconColor = ObsidianBlack,
                          selectedTextColor = GoldBright,
                          indicatorColor = GoldPrimary,
                          unselectedIconColor = TextSecondary,
                          unselectedTextColor = TextSecondary,
                        ),
                      )
                    }
                  }
                }
              },
            ) { innerPadding ->
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
              ) {
                if (useNavigationRail) {
                  NavigationRail(
                    containerColor = ObsidianSurface,
                    contentColor = GoldBright,
                    modifier = Modifier.fillMaxHeight().statusBarsPadding().navigationBarsPadding(),
                    header = {
                      M9999LogoMark(iconSize = 32.dp, modifier = Modifier.padding(vertical = 12.dp))
                    },
                  ) {
                    TOP_LEVEL_ITEMS.forEach { item ->
                      val selected = currentRoute == item.route
                      NavigationRailItem(
                        selected = selected,
                        onClick = { navigateTopLevel(item.route) },
                        modifier = Modifier.testTag("rail_nav_${item.route}"),
                        icon = {
                          Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                          )
                        },
                        label = { Text(item.label, maxLines = 1) },
                        colors = NavigationRailItemDefaults.colors(
                          selectedIconColor = ObsidianBlack,
                          selectedTextColor = GoldBright,
                          indicatorColor = GoldPrimary,
                          unselectedIconColor = TextSecondary,
                          unselectedTextColor = TextSecondary,
                        ),
                      )
                    }
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  contentAlignment = Alignment.TopCenter,
                ) {
                  NavHost(
                    navController = navController,
                    startDestination = NavRoutes.HOME,
                    modifier = Modifier
                      .fillMaxSize()
                      .widthIn(max = 840.dp),
                  ) {
                    composable(NavRoutes.HOME) {
                      HomeDashboardScreen(
                        account = account,
                        mode = viewModel.mode,
                        config = config,
                        announcements = announcements,
                        recentGames = history,
                        unreadNotifications = unreadNotifications,
                        onOpenGame = { navController.navigate(NavRoutes.routeForGame(it.id)) },
                        onOpenRewards = { navigateTopLevel(NavRoutes.REWARDS) },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                        onOpenNotifications = { navController.navigate(NavRoutes.NOTIFICATIONS) },
                        onOpenHistory = { navController.navigate(NavRoutes.GAME_HISTORY) },
                        onOpenProfile = { navigateTopLevel(NavRoutes.PROFILE) },
                        onOpenAllGames = { navigateTopLevel(NavRoutes.GAMES) },
                      )
                    }

                    composable(NavRoutes.GAMES) {
                      GamesHubScreen(
                        config = config,
                        onOpenGame = { navController.navigate(NavRoutes.routeForGame(it.id)) },
                      )
                    }

                    composable(NavRoutes.REWARDS) {
                      DailyRewardsScreen(
                        account = account,
                        config = config,
                        transactions = transactions,
                        onClaimDaily = { onResult -> viewModel.claimDailyReward(onResult) },
                      )
                    }

                    composable(NavRoutes.LEADERBOARD) {
                      LeaderboardScreen(
                        entries = leaderboard,
                        currentUserId = account.userId,
                      )
                    }

                    composable(NavRoutes.PROFILE) {
                      ProfileScreen(
                        account = account,
                        mode = viewModel.mode,
                        isAdminVerified = isAdminVerified,
                        gameHistory = history,
                        unreadNotifications = unreadNotifications,
                        onEditProfile = { full, user, avatar, onDone ->
                          viewModel.updateProfile(full, user, avatar, onDone)
                        },
                        onChangeDemoPassword = { curr, next, onDone ->
                          viewModel.changeDemoPassword(curr, next, onDone)
                        },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                        onOpenNotifications = { navController.navigate(NavRoutes.NOTIFICATIONS) },
                        onOpenHistory = { navController.navigate(NavRoutes.GAME_HISTORY) },
                        onOpenAdmin = { navController.navigate(NavRoutes.ADMIN_DASHBOARD) },
                        onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) },
                        onOpenHelp = { navController.navigate(NavRoutes.HELP_SUPPORT) },
                        onOpenAbout = { navController.navigate(NavRoutes.ABOUT_US) },
                        onOpenPrivacy = { navController.navigate(NavRoutes.PRIVACY_POLICY) },
                        onOpenTerms = { navController.navigate(NavRoutes.TERMS_CONDITIONS) },
                        onLogout = onLogout,
                      )
                    }

                    composable(NavRoutes.WALLET) {
                      WalletScreen(
                        account = account,
                        transactions = transactions,
                        onOpenRewards = { navigateTopLevel(NavRoutes.REWARDS) },
                        onBack = { navController.popBackStack() },
                      )
                    }

                    composable(NavRoutes.NOTIFICATIONS) {
                      NotificationsScreen(
                        notifications = notifications,
                        onMarkRead = { viewModel.markNotificationRead(it) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() },
                        onBack = { navController.popBackStack() },
                      )
                    }

                    composable(NavRoutes.GAME_HISTORY) {
                      GameHistoryScreen(
                        history = history,
                        onBack = { navController.popBackStack() },
                      )
                    }

                    composable(NavRoutes.SETTINGS) {
                      SettingsScreen(
                        mode = viewModel.mode,
                        settings = settings,
                        onToggleSound = { viewModel.setSoundFeedback(it) },
                        onToggleAnimations = { viewModel.setAnimationsEnabled(it) },
                        onBack = { navController.popBackStack() },
                      )
                    }

                    composable(NavRoutes.HELP_SUPPORT) {
                      HelpSupportScreen(onBack = { navController.popBackStack() })
                    }

                    composable(NavRoutes.ABOUT_US) {
                      AboutUsScreen(onBack = { navController.popBackStack() })
                    }

                    composable(NavRoutes.PRIVACY_POLICY) {
                      PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                    }

                    composable(NavRoutes.TERMS_CONDITIONS) {
                      TermsConditionsScreen(onBack = { navController.popBackStack() })
                    }

                    composable(NavRoutes.ADMIN_DASHBOARD) {
                      AdminDashboardScreen(
                        mode = viewModel.mode,
                        isAdminVerified = isAdminVerified,
                        settings = settings,
                        users = adminUsers,
                        config = config,
                        announcements = announcements,
                        activityLogs = activityLogs,
                        onClaimLocalAdmin = { pass, res -> viewModel.claimLocalDemoAdminPasscode(pass, res) },
                        onVerifyLocalAdmin = { pass, res -> viewModel.verifyLocalDemoAdminPasscode(pass, res) },
                        onLockLocalAdmin = { viewModel.lockLocalDemoAdmin() },
                        onToggleAccountStatus = { uid, status -> viewModel.adminToggleAccountStatus(uid, status) },
                        onGrantCoins = { user, bonus -> viewModel.adminGrantCoins(user, bonus) },
                        onSaveConfig = { viewModel.adminUpdateConfig(it) },
                        onPublishAnnouncement = { t, m, b, done -> viewModel.adminPublishAnnouncement(t, m, b, done) },
                        onDeleteAnnouncement = { viewModel.adminDeleteAnnouncement(it) },
                        onSeedCloudLeaderboard = { viewModel.adminSeedCloudSampleLeaderboard() },
                        onBack = { navController.popBackStack() },
                      )
                    }

                    // 6 Playable Mini-Games
                    composable(NavRoutes.GAME_LUCKY_WHEEL) {
                      LuckyWheelScreen(
                        account = account,
                        config = config,
                        history = history,
                        animationsEnabled = settings.animationsEnabled,
                        onRecordResult = { reward, summary ->
                          viewModel.recordGameRound(
                            gameId = MiniGameId.LUCKY_WHEEL,
                            entryCoins = 0L,
                            rewardCoins = reward,
                            outcome = "REWARD",
                            summary = summary,
                            isWheelSpin = true,
                          )
                        },
                        onBack = { navController.popBackStack() },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                      )
                    }

                    composable(NavRoutes.GAME_SLOT_MACHINE) {
                      SlotMachineScreen(
                        account = account,
                        config = config,
                        history = history,
                        animationsEnabled = settings.animationsEnabled,
                        onRecordResult = { entry, reward, outcome, summary ->
                          viewModel.recordGameRound(MiniGameId.SLOT_MACHINE, entry, reward, outcome, summary)
                        },
                        onBack = { navController.popBackStack() },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                      )
                    }

                    composable(NavRoutes.GAME_DICE) {
                      DiceGameScreen(
                        account = account,
                        config = config,
                        history = history,
                        animationsEnabled = settings.animationsEnabled,
                        onRecordResult = { entry, reward, outcome, summary ->
                          viewModel.recordGameRound(MiniGameId.DICE_GAME, entry, reward, outcome, summary)
                        },
                        onBack = { navController.popBackStack() },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                      )
                    }

                    composable(NavRoutes.GAME_CARD_REVEAL) {
                      CardRevealScreen(
                        account = account,
                        config = config,
                        history = history,
                        animationsEnabled = settings.animationsEnabled,
                        onRecordResult = { entry, reward, outcome, summary ->
                          viewModel.recordGameRound(MiniGameId.CARD_REVEAL, entry, reward, outcome, summary)
                        },
                        onBack = { navController.popBackStack() },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                      )
                    }

                    composable(NavRoutes.GAME_LUCKY_NUMBER) {
                      LuckyNumberScreen(
                        account = account,
                        config = config,
                        history = history,
                        animationsEnabled = settings.animationsEnabled,
                        onRecordResult = { entry, reward, outcome, summary ->
                          viewModel.recordGameRound(MiniGameId.LUCKY_NUMBER, entry, reward, outcome, summary)
                        },
                        onBack = { navController.popBackStack() },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                      )
                    }

                    composable(NavRoutes.GAME_MEMORY_MATCH) {
                      MemoryMatchScreen(
                        account = account,
                        config = config,
                        history = history,
                        onRecordResult = { reward, summary ->
                          viewModel.recordGameRound(MiniGameId.MEMORY_MATCH, 0L, reward, "WIN", summary)
                        },
                        onBack = { navController.popBackStack() },
                        onOpenWallet = { navController.navigate(NavRoutes.WALLET) },
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DisabledAccountGate(
  account: PlayerAccount,
  mode: DataMode,
  isAdminVerified: Boolean,
  onOpenAdmin: () -> Unit,
  onLogout: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    ObsidianCard(modifier = Modifier.widthIn(max = 440.dp)) {
      Text("Account Disabled", style = MaterialTheme.typography.headlineLarge, color = RubyLoss)
      Spacer(Modifier.height(8.dp))
      Text(
        text = "The account @${account.username} is currently disabled by an administrator.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextPrimary,
      )
      Spacer(Modifier.height(14.dp))
      if (isAdminVerified && mode == DataMode.LOCAL_DEMO) {
        GoldGradientButton(
          text = "Re-enable Account (Admin)",
          onClick = onOpenAdmin,
          testTag = "reenable_self_demo_button",
        )
        Spacer(Modifier.height(10.dp))
      }
      GoldGradientButton(
        text = "Sign Out",
        onClick = onLogout,
        testTag = "disabled_logout_button",
      )
    }
  }
}
