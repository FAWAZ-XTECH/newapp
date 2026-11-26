package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AvatarOption
import com.example.data.model.DataMode
import com.example.data.model.GameLog
import com.example.data.model.MiniGameId
import com.example.data.model.PlayerAccount
import com.example.ui.components.AdminShieldBadge
import com.example.ui.components.AvatarBadge
import com.example.ui.components.DataModeBadge
import com.example.ui.components.ObsidianCard
import com.example.ui.components.formatCoins
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
  account: PlayerAccount,
  mode: DataMode,
  isAdminVerified: Boolean,
  gameHistory: List<GameLog>,
  unreadNotifications: Int,
  onEditProfile: (fullName: String, username: String, avatarKey: String, onDone: (Result<Unit>) -> Unit) -> Unit,
  onChangeDemoPassword: (current: String, next: String, onDone: (Result<Unit>) -> Unit) -> Unit,
  onOpenWallet: () -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenAdmin: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  onOpenAbout: () -> Unit,
  onOpenPrivacy: () -> Unit,
  onOpenTerms: () -> Unit,
  onLogout: () -> Unit,
) {
  var showEditDialog by remember { mutableStateOf(false) }
  var showPasswordDialog by remember { mutableStateOf(false) }

  val regDate = remember(account.createdAtMillis) {
    SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(account.createdAtMillis))
  }
  val winCount = remember(gameHistory) { gameHistory.count { it.outcome == "WIN" || it.outcome == "REWARD" } }

  if (showEditDialog) {
    EditProfileDialog(
      account = account,
      onDismiss = { showEditDialog = false },
      onSave = { full, user, avatar, onErr ->
        onEditProfile(full, user, avatar) { res ->
          res.onSuccess { showEditDialog = false }
          res.onFailure { onErr(it.localizedMessage ?: "Failed to save profile.") }
        }
      },
    )
  }

  if (showPasswordDialog) {
    ChangePasswordDialog(
      onDismiss = { showPasswordDialog = false },
      onSubmit = { curr, next, onErr ->
        onChangeDemoPassword(curr, next) { res ->
          res.onSuccess { showPasswordDialog = false }
          res.onFailure { onErr(it.localizedMessage ?: "Password update failed.") }
        }
      },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Profile Header Card
    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          AvatarBadge(avatarKey = account.avatarKey, size = 68.dp)
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(account.fullName, style = MaterialTheme.typography.titleLarge)
              if (isAdminVerified) AdminShieldBadge()
            }
            Text("@${account.username}", style = MaterialTheme.typography.bodyMedium, color = GoldBright)
            Text(account.email.ifBlank { "No email attached" }, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              DataModeBadge(mode = mode)
              Text("Joined $regDate", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
          }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("edit_profile_button"),
            border = BorderStroke(1.dp, GoldPrimary),
          ) {
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text("Edit Profile", color = GoldChampagne)
          }
          if (mode == DataMode.LOCAL_DEMO) {
            OutlinedButton(
              onClick = { showPasswordDialog = true },
              modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("change_password_button"),
              border = BorderStroke(1.dp, ObsidianBorder),
            ) {
              Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
              Spacer(Modifier.size(6.dp))
              Text("Password", color = TextPrimary)
            }
          }
        }
      }
    }

    // Statistics Summary
    item {
      ObsidianCard(modifier = Modifier.fillMaxWidth()) {
        Text("Player Statistics", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          StatPill("Coins", formatCoins(account.coinBalance), Modifier.weight(1f))
          StatPill("Games", account.totalGamesPlayed.toString(), Modifier.weight(1f))
          StatPill("Rewards", formatCoins(account.totalRewardsEarned), Modifier.weight(1f))
          StatPill("Wins", winCount.toString(), Modifier.weight(1f))
        }
        if (gameHistory.isNotEmpty()) {
          Spacer(Modifier.height(12.dp))
          val counts = MiniGameId.entries.mapNotNull { g ->
            val n = gameHistory.count { it.gameId == g.id }
            if (n > 0) "${g.title}: $n" else null
          }
          if (counts.isNotEmpty()) {
            Text(
              text = "Recent Breakdown: ${counts.joinToString(" • ")}",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
        }
      }
    }

    // Account & App Links
    item {
      Text("Account & Services", style = MaterialTheme.typography.titleLarge)
    }

    item {
      ObsidianCard(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuRow(
          icon = Icons.Rounded.AccountBalanceWallet,
          title = "Virtual Wallet",
          subtitle = "Balance, demo points & coin activity",
          onClick = onOpenWallet,
          tag = "menu_wallet",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.Notifications,
          title = "Notification Center",
          subtitle = "Rewards, announcements & achievements",
          badgeCount = unreadNotifications,
          onClick = onOpenNotifications,
          tag = "menu_notifications",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.History,
          title = "Game History",
          subtitle = "Full round-by-round play results",
          onClick = onOpenHistory,
          tag = "menu_game_history",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.AdminPanelSettings,
          title = "Admin Dashboard",
          subtitle = if (isAdminVerified) "Manage users, games, bonuses & logs" else "Protected administrator console",
          onClick = onOpenAdmin,
          tag = "menu_admin_dashboard",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.Settings,
          title = "Settings",
          subtitle = "Feedback, motion & storage info",
          onClick = onOpenSettings,
          tag = "menu_settings",
        )
      }
    }

    item {
      Text("Support & Legal", style = MaterialTheme.typography.titleLarge)
    }

    item {
      ObsidianCard(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuRow(
          icon = Icons.AutoMirrored.Rounded.HelpOutline,
          title = "Help & Support",
          subtitle = "FAQs, fair play & RNG guide",
          onClick = onOpenHelp,
          tag = "menu_help_support",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.Info,
          title = "About Us",
          subtitle = "About M9999 Demo Play",
          onClick = onOpenAbout,
          tag = "menu_about_us",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.PrivacyTip,
          title = "Privacy Policy",
          subtitle = "Data protection & demo isolation",
          onClick = onOpenPrivacy,
          tag = "menu_privacy_policy",
        )
        ProfileMenuRow(
          icon = Icons.Rounded.Description,
          title = "Terms & Conditions",
          subtitle = "Virtual-only rules & responsible gaming",
          onClick = onOpenTerms,
          tag = "menu_terms_conditions",
        )
      }
    }

    item {
      Button(
        onClick = onLogout,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 50.dp)
          .testTag("logout_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = RubyLoss.copy(alpha = 0.16f),
          contentColor = RubyLoss,
        ),
        border = BorderStroke(1.dp, RubyLoss.copy(alpha = 0.5f)),
      ) {
        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text("Log Out", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(horizontal = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium.copy(
        fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
        color = GoldChampagne,
      ),
    )
    Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
  }
}

@Composable
private fun ProfileMenuRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  badgeCount: Int = 0,
  onClick: () -> Unit,
  tag: String,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 56.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 4.dp)
      .testTag(tag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleMedium)
      Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
    if (badgeCount > 0) {
      Badge(containerColor = GoldBright, contentColor = ObsidianBlack) {
        Text(badgeCount.toString())
      }
    }
    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
  }
}

@Composable
private fun EditProfileDialog(
  account: PlayerAccount,
  onDismiss: () -> Unit,
  onSave: (fullName: String, username: String, avatarKey: String, onError: (String) -> Unit) -> Unit,
) {
  var fullName by remember { mutableStateOf(account.fullName) }
  var username by remember { mutableStateOf(account.username) }
  var avatarKey by remember { mutableStateOf(account.avatarKey) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ObsidianCard,
    titleContentColor = GoldBright,
    textContentColor = TextPrimary,
    title = { Text("Edit Profile") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_fullname_input"),
        )
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_username_input"),
        )
        Text("Select Avatar Emblem", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          items(AvatarOption.entries) { option ->
            val selected = avatarKey == option.key
            Surface(
              onClick = { avatarKey = option.key },
              shape = RoundedCornerShape(12.dp),
              color = if (selected) ObsidianElevated else ObsidianBlack,
              border = BorderStroke(1.5.dp, if (selected) GoldBright else ObsidianBorder),
            ) {
              Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                AvatarBadge(avatarKey = option.key, size = 38.dp)
                Spacer(Modifier.height(4.dp))
                Text(option.label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
              }
            }
          }
        }
        if (error != null) {
          Text(error!!, color = RubyLoss, style = MaterialTheme.typography.bodyMedium)
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onSave(fullName, username, avatarKey) { error = it } },
        modifier = Modifier.testTag("save_profile_button"),
      ) {
        Text("Save", color = GoldBright)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
  )
}

@Composable
private fun ChangePasswordDialog(
  onDismiss: () -> Unit,
  onSubmit: (current: String, next: String, onError: (String) -> Unit) -> Unit,
) {
  var currentPass by remember { mutableStateOf("") }
  var newPass by remember { mutableStateOf("") }
  var confirmPass by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ObsidianCard,
    titleContentColor = GoldBright,
    textContentColor = TextPrimary,
    title = { Text("Change Demo Password") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = currentPass,
          onValueChange = { currentPass = it },
          label = { Text("Current Password") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("change_pass_current"),
        )
        OutlinedTextField(
          value = newPass,
          onValueChange = { newPass = it },
          label = { Text("New Password (min 6 chars)") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("change_pass_new"),
        )
        OutlinedTextField(
          value = confirmPass,
          onValueChange = { confirmPass = it },
          label = { Text("Confirm New Password") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("change_pass_confirm"),
        )
        if (error != null) {
          Text(error!!, color = RubyLoss, style = MaterialTheme.typography.bodyMedium)
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (newPass != confirmPass) {
            error = "Passwords do not match."
          } else {
            onSubmit(currentPass, newPass) { error = it }
          }
        },
        modifier = Modifier.testTag("confirm_change_pass_button"),
      ) {
        Text("Update", color = GoldBright)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
  )
}
