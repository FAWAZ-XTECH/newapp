package com.example.ui.screens.secondary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.AppUserSettings
import com.example.data.model.DataMode
import com.example.ui.components.DataModeBadge
import com.example.ui.components.M9999LogoMark
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
  mode: DataMode,
  settings: AppUserSettings,
  onToggleSound: (Boolean) -> Unit,
  onToggleAnimations: (Boolean) -> Unit,
  onBack: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      SecondaryHeader(title = "Settings", onBack = onBack, tag = "settings_back_button")
    }

    item {
      ObsidianCard(modifier = Modifier.fillMaxWidth()) {
        Text("Storage & Sync Mode", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Spacer(Modifier.height(8.dp))
        DataModeBadge(mode = mode)
        Spacer(Modifier.height(6.dp))
        Text(
          text = if (mode == DataMode.FIREBASE_CLOUD) {
            "Signed in with Google. Your profile, virtual wallet, and game history sync securely to Cloud Firestore."
          } else {
            "Running in Local Demo Mode. All demo accounts, virtual coins, and admin settings are isolated in on-device Room/DataStore storage."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
      }
    }

    item {
      ObsidianCard(modifier = Modifier.fillMaxWidth()) {
        Text("Experience Preferences", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Spacer(Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Visual Celebrations & Effects", style = MaterialTheme.typography.titleMedium)
            Text("Enable wheel spins, reel motion, and card flips", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          }
          Switch(
            checked = settings.animationsEnabled,
            onCheckedChange = onToggleAnimations,
            modifier = Modifier.testTag("toggle_animations_switch"),
            colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = GoldPrimary),
          )
        }
        Spacer(Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Game Feedback Prompts", style = MaterialTheme.typography.titleMedium)
            Text("Show outcome banners and milestone alerts", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          }
          Switch(
            checked = settings.soundFeedbackEnabled,
            onCheckedChange = onToggleSound,
            modifier = Modifier.testTag("toggle_feedback_switch"),
            colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = GoldPrimary),
          )
        }
      }
    }

    item {
      ResponsibleGamingBanner()
    }
  }
}

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
  InfoPageLayout(title = "Help & Support", onBack = onBack) {
    InfoSectionCard(
      title = "How do virtual coins work?",
      body = "M9999 Demo Play uses fictional virtual demo coins only. You receive starter coins when creating an account, plus daily streak bonuses and free Lucky Wheel spins.",
    )
    InfoSectionCard(
      title = "Are mini-game outcomes fair?",
      body = "Yes. Every spin, roll, card shuffle, and number draw uses Java's cryptographically strong SecureRandom generator. No outcome is predetermined or guaranteed.",
    )
    InfoSectionCard(
      title = "Why can't I claim the Daily Bonus twice?",
      body = "To keep daily streak progression fair, each account can claim one daily bonus per UTC calendar day. Check the live countdown timer on the Rewards tab.",
    )
    InfoSectionCard(
      title = "Local Demo vs. Cloud Sync",
      body = "Google Sign-In syncs your data to Cloud Firestore. Local Demo Mode persists data on your device only using isolated local storage.",
    )
  }
}

@Composable
fun AboutUsScreen(onBack: () -> Unit) {
  InfoPageLayout(title = "About Us", onBack = onBack) {
    ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
      M9999LogoMark(iconSize = 48.dp, showSubtitle = true)
      Spacer(Modifier.height(12.dp))
      Text(
        text = "M9999 Demo Play is a free-to-play mobile arcade lounge designed around luxury obsidian-and-gold aesthetics, fair random mini-games, and transparent virtual progression.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextPrimary,
      )
    }
    InfoSectionCard(
      title = "Entertainment Only Commitment",
      body = "This application does NOT support real-money gambling, deposits, withdrawals, cash prizes, or payment integrations. All balances represent fictional demo points.",
    )
    InfoSectionCard(
      title = "Six Playable Mini-Games",
      body = "Enjoy Lucky Wheel, Golden Slots, Royal Dice, Card Reveal, Lucky Number, and Memory Match with real-time wallet tracking and leaderboards.",
    )
  }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
  InfoPageLayout(title = "Privacy Policy", onBack = onBack) {
    InfoSectionCard(
      title = "1. Data We Store",
      body = "When signed in with Google, your display name, username, email, virtual coin balance, and game activity are stored securely in Cloud Firestore scoped to your user ID. Public leaderboards exclude your email and full name.",
    )
    InfoSectionCard(
      title = "2. Local Demo Storage Isolation",
      body = "When using Local Demo Mode, account records and salted PBKDF2 password hashes remain strictly on your device in local Room and DataStore storage.",
    )
    InfoSectionCard(
      title = "3. No Financial or Payment Data",
      body = "M9999 Demo Play never collects credit cards, bank details, or payment information because no real-money transactions exist.",
    )
  }
}

@Composable
fun TermsConditionsScreen(onBack: () -> Unit) {
  InfoPageLayout(title = "Terms & Conditions", onBack = onBack) {
    InfoSectionCard(
      title = "1. Free-to-Play Virtual Demo Only",
      body = "All virtual coins, daily rewards, and leaderboard points inside M9999 Demo Play have zero monetary value and cannot be exchanged, redeemed, or withdrawn for cash or prizes.",
    )
    InfoSectionCard(
      title = "2. Fair Play & Account Integrity",
      body = "Administrators reserve the right to pause mini-games for maintenance, adjust virtual demo settings, or disable demo accounts that violate community guidelines.",
    )
    InfoSectionCard(
      title = "3. Responsible Gaming",
      body = "Take regular breaks and play purely for fun. Outcomes in random mini-games are probabilistic and never imply real-world success.",
    )
  }
}

@Composable
private fun SecondaryHeader(title: String, onBack: () -> Unit, tag: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag(tag)) {
      Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = GoldBright)
    }
    Text(title, style = MaterialTheme.typography.headlineLarge, color = GoldBright)
  }
}

@Composable
private fun InfoPageLayout(
  title: String,
  onBack: () -> Unit,
  content: @Composable () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SecondaryHeader(title = title, onBack = onBack, tag = "${title.lowercase().replace(" ", "_")}_back")
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
      }
    }
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(Icons.Rounded.Shield, contentDescription = null, tint = GoldChampagne, modifier = Modifier.size(18.dp))
        Text(
          text = "M9999 Demo Play • Verified Virtual Entertainment",
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondary,
        )
      }
    }
  }
}

@Composable
private fun InfoSectionCard(title: String, body: String) {
  ObsidianCard(modifier = Modifier.fillMaxWidth()) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = GoldBright)
    Spacer(Modifier.height(6.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
  }
}
