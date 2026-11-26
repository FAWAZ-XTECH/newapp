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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerAccount
import com.example.data.model.WalletTx
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.components.formatCoins
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.TextSecondary

@Composable
fun WalletScreen(
  account: PlayerAccount,
  transactions: List<WalletTx>,
  onOpenRewards: () -> Unit,
  onBack: () -> Unit,
) {
  val gameRewardTotal = remember(transactions) {
    transactions.filter { it.type == "GAME_WIN" || it.type == "WHEEL_SPIN" }.sumOf { it.amount.coerceAtLeast(0L) }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("wallet_back_button")) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = GoldBright)
        }
        Text("Virtual Wallet", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
      }
    }

    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Text("Available Demo Coins", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        Spacer(Modifier.height(4.dp))
        Text(
          text = formatCoins(account.coinBalance),
          style = MaterialTheme.typography.displayLarge.copy(color = GoldBright),
        )
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Column {
            Text("Daily Reward Points", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(formatCoins(account.dailyRewardPoints), style = MaterialTheme.typography.titleMedium)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text("Game Rewards", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(formatCoins(gameRewardTotal), style = MaterialTheme.typography.titleMedium, color = EmeraldWin)
          }
        }
        Spacer(Modifier.height(14.dp))
        GoldGradientButton(
          text = "Collect Free Daily Bonus",
          icon = Icons.Rounded.CardGiftcard,
          onClick = onOpenRewards,
          testTag = "wallet_daily_bonus_button",
        )
      }
    }

    item {
      ResponsibleGamingBanner()
    }

    item {
      Text("Coin Activity History", style = MaterialTheme.typography.titleLarge)
    }

    if (transactions.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Rounded.AccountBalanceWallet,
          title = "No transactions yet",
          message = "Your virtual coin earnings and game entries will appear here.",
        )
      }
    } else {
      items(transactions, key = { it.id }) { tx ->
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(tx.title, style = MaterialTheme.typography.titleMedium)
              Text(
                text = "${tx.type.replace("_", " ")} • ${formatTimestamp(tx.createdAtMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = if (tx.amount >= 0) "+${formatCoins(tx.amount)}" else formatCoins(tx.amount),
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (tx.amount >= 0) EmeraldWin else RubyLoss
                ),
              )
              Text(
                text = "Bal: ${formatCoins(tx.balanceAfter)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
              )
            }
          }
        }
      }
    }
  }
}
