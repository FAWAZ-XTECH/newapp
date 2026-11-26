package com.example.ui.screens.secondary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.AppUserSettings
import com.example.data.model.ActivityAuditItem
import com.example.data.model.AnnouncementItem
import com.example.data.model.DataMode
import com.example.data.model.MiniGameId
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.ui.components.AdminShieldBadge
import com.example.ui.components.AvatarBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.ObsidianCard
import com.example.ui.components.formatCoins
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class AdminTab(val label: String) {
  OVERVIEW("Overview"),
  USERS("Users"),
  GAMES_CONFIG("Games & Bonus"),
  ANNOUNCEMENTS("Announcements"),
  LOGS("Activity Logs"),
}

@Composable
fun AdminDashboardScreen(
  mode: DataMode,
  isAdminVerified: Boolean,
  settings: AppUserSettings,
  users: List<PlayerAccount>,
  config: PlatformConfig,
  announcements: List<AnnouncementItem>,
  activityLogs: List<ActivityAuditItem>,
  onClaimLocalAdmin: (String, (Result<Unit>) -> Unit) -> Unit,
  onVerifyLocalAdmin: (String, (Boolean) -> Unit) -> Unit,
  onLockLocalAdmin: () -> Unit,
  onToggleAccountStatus: (targetUserId: String, currentStatus: String) -> Unit,
  onGrantCoins: (PlayerAccount, Long) -> Unit,
  onSaveConfig: (PlatformConfig) -> Unit,
  onPublishAnnouncement: (title: String, message: String, badge: String, onDone: (Result<Unit>) -> Unit) -> Unit,
  onDeleteAnnouncement: (String) -> Unit,
  onSeedCloudLeaderboard: () -> Unit,
  onBack: () -> Unit,
) {
  var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
  var passcodeInput by remember { mutableStateOf("") }
  var confirmPasscodeInput by remember { mutableStateOf("") }
  var authError by remember { mutableStateOf<String?>(null) }

  var userSearch by remember { mutableStateOf("") }
  var inspectedUser by remember { mutableStateOf<PlayerAccount?>(null) }

  // Config editor states synced when config changes
  var baseBonusText by remember(config) { mutableStateOf(config.dailyBaseBonus.toString()) }
  var streakIncText by remember(config) { mutableStateOf(config.dailyStreakIncrement.toString()) }
  var maxSpinsText by remember(config) { mutableStateOf(config.maxDailyWheelSpins.toString()) }
  var bannerText by remember(config) { mutableStateOf(config.maintenanceBanner) }
  var disabledGames by remember(config) { mutableStateOf(config.disabledGames.toSet()) }

  // Announcement form
  var annTitle by remember { mutableStateOf("") }
  var annBadge by remember { mutableStateOf("EVENT") }
  var annMessage by remember { mutableStateOf("") }
  var annFeedback by remember { mutableStateOf<String?>(null) }

  if (inspectedUser != null) {
    val u = inspectedUser!!
    AlertDialog(
      onDismissRequest = { inspectedUser = null },
      containerColor = ObsidianCard,
      titleContentColor = GoldBright,
      textContentColor = TextPrimary,
      title = { Text("User Profile: ${u.username}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Full Name: ${u.fullName}")
          Text("Email: ${u.email}")
          Text("Role: ${u.role} • Status: ${u.accountStatus}")
          Text("Coin Balance: ${formatCoins(u.coinBalance)}")
          Text("Daily Reward Points: ${formatCoins(u.dailyRewardPoints)}")
          Text("Total Games Played: ${u.totalGamesPlayed}")
          Text("Login Streak: ${u.loginStreak} days")
        }
      },
      confirmButton = {
        TextButton(onClick = { inspectedUser = null }) {
          Text("Close", color = GoldBright)
        }
      },
    )
  }

  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("admin_back_button")) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = GoldBright)
        }
        Text("Admin Console", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
      }
      if (isAdminVerified) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AdminShieldBadge()
          if (mode == DataMode.LOCAL_DEMO) {
            TextButton(onClick = onLockLocalAdmin, modifier = Modifier.testTag("admin_lock_button")) {
              Text("Lock", color = GoldPrimary)
            }
          }
        }
      }
    }

    if (!isAdminVerified) {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldBright, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        if (mode == DataMode.FIREBASE_CLOUD) {
          Text("Server-Side Admin Authorization Required", style = MaterialTheme.typography.titleLarge, color = GoldBright)
          Spacer(Modifier.height(6.dp))
          Text(
            text = "Cloud admin routes are protected by Firestore security rules (admins/{uid} or verified project owner email). Your signed-in Google account does not have admin privileges.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
        } else if (!settings.adminPasscodeClaimed) {
          Text("First-Run Local Admin Setup", style = MaterialTheme.typography.titleLarge, color = GoldBright)
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Claim your on-device demo admin passcode (stored as a salted PBKDF2 hash). No default password is hardcoded.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Spacer(Modifier.height(10.dp))
          OutlinedTextField(
            value = passcodeInput,
            onValueChange = { passcodeInput = it },
            label = { Text("Create Admin Passcode (min 6 chars)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_claim_passcode_input"),
          )
          Spacer(Modifier.height(8.dp))
          OutlinedTextField(
            value = confirmPasscodeInput,
            onValueChange = { confirmPasscodeInput = it },
            label = { Text("Confirm Passcode") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_claim_confirm_input"),
          )
          if (authError != null) {
            Spacer(Modifier.height(6.dp))
            Text(authError!!, color = RubyLoss, style = MaterialTheme.typography.bodyMedium)
          }
          Spacer(Modifier.height(12.dp))
          GoldGradientButton(
            text = "Claim Local Admin Access",
            testTag = "admin_claim_button",
            onClick = {
              authError = null
              if (passcodeInput != confirmPasscodeInput) {
                authError = "Passcodes do not match."
                return@GoldGradientButton
              }
              onClaimLocalAdmin(passcodeInput) { res ->
                res.onSuccess {
                  passcodeInput = ""
                  confirmPasscodeInput = ""
                }
                res.onFailure { authError = it.localizedMessage ?: "Setup failed." }
              }
            },
          )
        } else {
          Text("Admin Login (Local Demo)", style = MaterialTheme.typography.titleLarge, color = GoldBright)
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Enter your claimed local admin passcode to unlock management controls.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Spacer(Modifier.height(10.dp))
          OutlinedTextField(
            value = passcodeInput,
            onValueChange = { passcodeInput = it },
            label = { Text("Admin Passcode") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_login_passcode_input"),
          )
          if (authError != null) {
            Spacer(Modifier.height(6.dp))
            Text(authError!!, color = RubyLoss, style = MaterialTheme.typography.bodyMedium)
          }
          Spacer(Modifier.height(12.dp))
          GoldGradientButton(
            text = "Unlock Admin Dashboard",
            testTag = "admin_login_button",
            onClick = {
              authError = null
              onVerifyLocalAdmin(passcodeInput) { ok ->
                if (ok) {
                  passcodeInput = ""
                } else {
                  authError = "Incorrect admin passcode."
                }
              }
            },
          )
        }
      }
      return@Column
    }

    ScrollableTabRow(
      selectedTabIndex = selectedTab.ordinal,
      containerColor = ObsidianElevated,
      contentColor = GoldBright,
      edgePadding = 4.dp,
    ) {
      AdminTab.entries.forEach { tab ->
        Tab(
          selected = selectedTab == tab,
          onClick = { selectedTab = tab },
          modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}"),
          text = { Text(tab.label, maxLines = 1) },
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      when (selectedTab) {
        AdminTab.OVERVIEW -> {
          val totalUsers = users.size
          val activeUsers = users.count { it.accountStatus == "ACTIVE" }
          val totalGames = users.sumOf { it.totalGamesPlayed }
          val totalCoins = users.sumOf { it.coinBalance }
          val totalRewards = users.sumOf { it.totalRewardsEarned }

          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              AdminKpiCard("Registered Users", totalUsers.toString(), Modifier.weight(1f))
              AdminKpiCard("Active Accounts", activeUsers.toString(), Modifier.weight(1f))
            }
          }
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              AdminKpiCard("Total Games Played", formatCoins(totalGames), Modifier.weight(1f))
              AdminKpiCard("Virtual Coins Pool", formatCoins(totalCoins), Modifier.weight(1f))
            }
          }
          item {
            ObsidianCard(modifier = Modifier.fillMaxWidth()) {
              Text("Virtual Points Statistics", style = MaterialTheme.typography.titleMedium, color = GoldBright)
              Spacer(Modifier.height(6.dp))
              Text("Cumulative Rewards Awarded: ${formatCoins(totalRewards)} demo pts", style = MaterialTheme.typography.bodyMedium)
              Text("Active Mini-Games: ${6 - config.disabledGames.size} / 6", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
              Text("Published Announcements: ${announcements.size}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
              if (mode == DataMode.FIREBASE_CLOUD) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                  onClick = onSeedCloudLeaderboard,
                  border = BorderStroke(1.dp, GoldPrimary),
                  modifier = Modifier.testTag("admin_seed_sample_lb"),
                ) {
                  Text("Seed Sample Demo Leaderboard", color = GoldChampagne)
                }
              }
            }
          }
        }

        AdminTab.USERS -> {
          item {
            OutlinedTextField(
              value = userSearch,
              onValueChange = { userSearch = it },
              placeholder = { Text("Search users by name, username, or email...") },
              leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldPrimary) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("admin_user_search_input"),
            )
          }
          val filteredUsers = users.filter {
            val q = userSearch.trim()
            q.isEmpty() ||
              it.username.contains(q, ignoreCase = true) ||
              it.fullName.contains(q, ignoreCase = true) ||
              it.email.contains(q, ignoreCase = true)
          }
          if (filteredUsers.isEmpty()) {
            item {
              EmptyStateCard(
                icon = Icons.Rounded.AdminPanelSettings,
                title = "No users found",
                message = "Try adjusting your search query.",
              )
            }
          } else {
            items(filteredUsers, key = { it.userId }) { user ->
              val isActive = user.accountStatus == "ACTIVE"
              ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  AvatarBadge(avatarKey = user.avatarKey, size = 44.dp)
                  Column(modifier = Modifier.weight(1f)) {
                    Text("${user.username} (${user.accountStatus})", style = MaterialTheme.typography.titleMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(
                      text = "${formatCoins(user.coinBalance)} coins • ${user.totalGamesPlayed} games",
                      style = MaterialTheme.typography.labelSmall,
                      color = GoldChampagne,
                    )
                  }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedButton(
                    onClick = { inspectedUser = user },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("admin_view_user_${user.userId}"),
                    border = BorderStroke(1.dp, ObsidianBorder),
                  ) {
                    Text("Profile", color = TextPrimary)
                  }
                  OutlinedButton(
                    onClick = { onGrantCoins(user, 500L) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("admin_grant_coins_${user.userId}"),
                    border = BorderStroke(1.dp, GoldPrimary),
                  ) {
                    Text("+500 Coins", color = GoldBright)
                  }
                  OutlinedButton(
                    onClick = { onToggleAccountStatus(user.userId, user.accountStatus) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("admin_toggle_user_${user.userId}"),
                    border = BorderStroke(1.dp, if (isActive) RubyLoss else EmeraldWin),
                  ) {
                    Text(if (isActive) "Disable" else "Enable", color = if (isActive) RubyLoss else EmeraldWin)
                  }
                }
              }
            }
          }
        }

        AdminTab.GAMES_CONFIG -> {
          item {
            ObsidianCard(modifier = Modifier.fillMaxWidth()) {
              Text("Manage Game Availability", style = MaterialTheme.typography.titleMedium, color = GoldBright)
              Spacer(Modifier.height(8.dp))
              MiniGameId.entries.forEach { game ->
                val enabled = !disabledGames.contains(game.id)
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(game.title, style = MaterialTheme.typography.titleMedium)
                    Text(if (enabled) "Enabled" else "Paused", style = MaterialTheme.typography.labelSmall, color = if (enabled) EmeraldWin else RubyLoss)
                  }
                  Switch(
                    checked = enabled,
                    onCheckedChange = { isChecked ->
                      disabledGames = if (isChecked) disabledGames - game.id else disabledGames + game.id
                    },
                    modifier = Modifier.testTag("admin_toggle_game_${game.id}"),
                    colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = GoldPrimary),
                  )
                }
              }
            }
          }

          item {
            ObsidianCard(modifier = Modifier.fillMaxWidth()) {
              Text("Daily Bonus & Spin Settings", style = MaterialTheme.typography.titleMedium, color = GoldBright)
              Spacer(Modifier.height(10.dp))
              OutlinedTextField(
                value = baseBonusText,
                onValueChange = { baseBonusText = it },
                label = { Text("Daily Base Bonus (Coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_base_bonus_input"),
              )
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = streakIncText,
                onValueChange = { streakIncText = it },
                label = { Text("Streak Increment per Day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_streak_inc_input"),
              )
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = maxSpinsText,
                onValueChange = { maxSpinsText = it },
                label = { Text("Max Daily Lucky Wheel Spins") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_max_spins_input"),
              )
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = bannerText,
                onValueChange = { bannerText = it },
                label = { Text("Optional Maintenance / Notice Banner") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_banner_input"),
              )
              Spacer(Modifier.height(12.dp))
              GoldGradientButton(
                text = "Save Platform Settings",
                testTag = "admin_save_config_button",
                onClick = {
                  val newConfig = PlatformConfig(
                    dailyBaseBonus = (baseBonusText.toIntOrNull() ?: 250).coerceIn(50, 10000),
                    dailyStreakIncrement = (streakIncText.toIntOrNull() ?: 50).coerceIn(0, 2000),
                    maxDailyWheelSpins = (maxSpinsText.toIntOrNull() ?: 3).coerceIn(1, 50),
                    disabledGames = disabledGames.toList(),
                    maintenanceBanner = bannerText.trim().take(200),
                  )
                  onSaveConfig(newConfig)
                },
              )
            }
          }
        }

        AdminTab.ANNOUNCEMENTS -> {
          item {
            ObsidianCard(modifier = Modifier.fillMaxWidth()) {
              Text("Publish Announcement", style = MaterialTheme.typography.titleMedium, color = GoldBright)
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = annTitle,
                onValueChange = { annTitle = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_ann_title_input"),
              )
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = annBadge,
                onValueChange = { annBadge = it },
                label = { Text("Badge (e.g., EVENT, UPDATE, NEW)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_ann_badge_input"),
              )
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = annMessage,
                onValueChange = { annMessage = it },
                label = { Text("Message") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("admin_ann_message_input"),
              )
              if (annFeedback != null) {
                Spacer(Modifier.height(6.dp))
                Text(annFeedback!!, color = GoldChampagne, style = MaterialTheme.typography.bodyMedium)
              }
              Spacer(Modifier.height(10.dp))
              GoldGradientButton(
                text = "Publish Announcement",
                icon = Icons.Rounded.Add,
                testTag = "admin_publish_ann_button",
                onClick = {
                  if (annTitle.isBlank() || annMessage.isBlank()) {
                    annFeedback = "Enter both a title and message."
                    return@GoldGradientButton
                  }
                  onPublishAnnouncement(annTitle, annMessage, annBadge) { res ->
                    res.onSuccess {
                      annTitle = ""
                      annMessage = ""
                      annFeedback = "Announcement published!"
                    }
                    res.onFailure { annFeedback = it.localizedMessage ?: "Failed to publish." }
                  }
                },
              )
            }
          }

          items(announcements, key = { it.id }) { item ->
            ObsidianCard(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Icon(Icons.Rounded.Campaign, contentDescription = null, tint = GoldBright)
                  Column {
                    Text("${item.title} [${item.badge}]", style = MaterialTheme.typography.titleMedium)
                    Text(item.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                  }
                }
                IconButton(
                  onClick = { onDeleteAnnouncement(item.id) },
                  modifier = Modifier.size(48.dp).testTag("admin_delete_ann_${item.id}"),
                ) {
                  Icon(Icons.Rounded.Delete, contentDescription = "Delete announcement", tint = RubyLoss)
                }
              }
            }
          }
        }

        AdminTab.LOGS -> {
          if (activityLogs.isEmpty()) {
            item {
              EmptyStateCard(
                icon = Icons.Rounded.AdminPanelSettings,
                title = "No activity logs yet",
                message = "User registrations, daily rewards, and mini-game activity will stream here.",
              )
            }
          } else {
            items(activityLogs, key = { it.id }) { log ->
              ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("${log.username} • ${log.action}", style = MaterialTheme.typography.titleMedium)
                    Text(log.detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(formatTimestamp(log.createdAtMillis), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                  }
                  Text(
                    text = if (log.pointsDelta >= 0) "+${formatCoins(log.pointsDelta)}" else formatCoins(log.pointsDelta),
                    style = MaterialTheme.typography.labelMedium.copy(
                      color = if (log.pointsDelta >= 0) EmeraldWin else RubyLoss
                    ),
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

@Composable
private fun AdminKpiCard(label: String, value: String, modifier: Modifier = Modifier) {
  ObsidianCard(highlighted = true, modifier = modifier) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    Spacer(Modifier.height(4.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.headlineLarge.copy(
        fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
        color = GoldBright,
        fontWeight = FontWeight.Bold,
      ),
    )
  }
}
