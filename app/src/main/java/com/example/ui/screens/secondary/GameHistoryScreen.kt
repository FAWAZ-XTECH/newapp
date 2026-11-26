package com.example.ui.screens.secondary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.GameLog
import com.example.data.model.MiniGameId
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ObsidianCard
import com.example.ui.components.formatCoins
import com.example.ui.components.formatTimestamp
import com.example.ui.components.gameIconFor
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.TextSecondary

@Composable
fun GameHistoryScreen(
  history: List<GameLog>,
  onBack: () -> Unit,
) {
  var selectedGameId by remember { mutableStateOf<String?>(null) }

  val filtered = remember(history, selectedGameId) {
    if (selectedGameId == null) history else history.filter { it.gameId == selectedGameId }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("history_back_button")) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = GoldBright)
        }
        Text("Game History", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
      }
    }

    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          FilterChip(
            selected = selectedGameId == null,
            onClick = { selectedGameId = null },
            label = { Text("All") },
            modifier = Modifier.heightIn(min = 48.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GoldPrimary,
              selectedLabelColor = ObsidianBlack,
              containerColor = ObsidianCard,
              labelColor = TextSecondary,
            ),
            border = FilterChipDefaults.filterChipBorder(true, selectedGameId == null, borderColor = ObsidianBorder, selectedBorderColor = GoldBright),
          )
        }
        items(MiniGameId.entries) { game ->
          val sel = selectedGameId == game.id
          FilterChip(
            selected = sel,
            onClick = { selectedGameId = game.id },
            label = { Text(game.title) },
            modifier = Modifier.heightIn(min = 48.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GoldPrimary,
              selectedLabelColor = ObsidianBlack,
              containerColor = ObsidianCard,
              labelColor = TextSecondary,
            ),
            border = FilterChipDefaults.filterChipBorder(true, sel, borderColor = ObsidianBorder, selectedBorderColor = GoldBright),
          )
        }
      }
    }

    if (filtered.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Rounded.History,
          title = "No matching rounds",
          message = "Play any mini-game in the Game Center to log your virtual play history.",
        )
      }
    } else {
      items(filtered, key = { it.id }) { log ->
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(gameIconFor(log.gameId), contentDescription = log.gameName, tint = GoldBright, modifier = Modifier.size(26.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(log.gameName, style = MaterialTheme.typography.titleMedium)
              Text(log.summary, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
              Text(formatTimestamp(log.createdAtMillis), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
