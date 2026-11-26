package com.example.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.GameLog
import com.example.ui.components.CoinBalancePill
import com.example.ui.components.ObsidianCard
import com.example.ui.components.formatCoins
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.TextSecondary

@Composable
fun GameTopHeader(
  title: String,
  balance: Long,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.weight(1f),
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.size(48.dp).testTag("game_back_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "Back",
          tint = GoldBright,
        )
      }
      Text(title, style = MaterialTheme.typography.headlineLarge, color = GoldBright)
    }
    CoinBalancePill(balance = balance, onClick = onOpenWallet)
  }
}

@Composable
fun GameHistorySection(logs: List<GameLog>) {
  Text("Recent Round History", style = MaterialTheme.typography.titleLarge)
  Spacer(Modifier.height(6.dp))
  if (logs.isEmpty()) {
    ObsidianCard(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "No rounds recorded in this game yet. Play a round to see your virtual results here.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
      )
    }
  } else {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      logs.take(8).forEach { log ->
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(log.summary, style = MaterialTheme.typography.titleMedium)
              Text(formatTimestamp(log.createdAtMillis), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            val net = log.rewardCoins - log.entryCoins
            Text(
              text = if (net >= 0) "+${formatCoins(net)}" else formatCoins(net),
              style = MaterialTheme.typography.labelMedium.copy(
                color = if (net >= 0) EmeraldWin else RubyLoss
              ),
            )
          }
        }
      }
    }
  }
}
