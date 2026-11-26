package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.LeaderboardRow
import com.example.ui.components.AvatarBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ObsidianCard
import com.example.ui.components.formatCoins
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaderboardScreen(
  entries: List<LeaderboardRow>,
  currentUserId: String,
) {
  var selectedPeriod by remember { mutableStateOf(LeaderboardPeriod.WEEKLY) }

  val sortedEntries = remember(entries, selectedPeriod) {
    entries.sortedByDescending { row ->
      when (selectedPeriod) {
        LeaderboardPeriod.WEEKLY -> row.weeklyPoints
        LeaderboardPeriod.MONTHLY -> row.monthlyPoints
        LeaderboardPeriod.ALL_TIME -> row.allTimePoints
      }
    }
  }

  val hasSampleEntries = remember(entries) { entries.any { it.entryType == "SAMPLE_DEMO" } }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("M9999 Leaderboard", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
        Text(
          text = "Top players ranked by virtual demo points earned.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
      }
    }

    item {
      TabRow(
        selectedTabIndex = selectedPeriod.ordinal,
        containerColor = ObsidianElevated,
        contentColor = GoldBright,
      ) {
        LeaderboardPeriod.entries.forEach { period ->
          Tab(
            selected = selectedPeriod == period,
            onClick = { selectedPeriod = period },
            modifier = Modifier.testTag("leaderboard_tab_${period.name.lowercase()}"),
            text = { Text(period.label, maxLines = 1) },
          )
        }
      }
    }

    if (hasSampleEntries) {
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = ObsidianCard,
          border = BorderStroke(1.dp, ObsidianBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(Icons.Rounded.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            Text(
              text = "Accounts marked SAMPLE DEMO are pre-seeded demo profiles for initial ranking comparison.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
        }
      }
    }

    if (sortedEntries.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Rounded.EmojiEvents,
          title = "No leaderboard entries yet",
          message = "Play any mini-game to place your name on the M9999 leaderboard.",
        )
      }
    } else {
      itemsIndexed(sortedEntries, key = { _, item -> item.userId }) { index, row ->
        val rank = index + 1
        val points = when (selectedPeriod) {
          LeaderboardPeriod.WEEKLY -> row.weeklyPoints
          LeaderboardPeriod.MONTHLY -> row.monthlyPoints
          LeaderboardPeriod.ALL_TIME -> row.allTimePoints
        }
        val isCurrentUser = row.userId == currentUserId
        ObsidianCard(
          highlighted = isCurrentUser || rank <= 3,
          modifier = Modifier.fillMaxWidth().testTag("leaderboard_row_$rank"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (rank <= 3) GoldPrimary.copy(alpha = 0.22f) else ObsidianElevated,
              border = BorderStroke(1.dp, if (rank <= 3) GoldBright else ObsidianBorder),
              modifier = Modifier.size(38.dp),
            ) {
              BoxCompatCenter {
                Text(
                  text = "#$rank",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = if (rank <= 3) GoldBright else TextSecondary,
                  ),
                )
              }
            }
            AvatarBadge(avatarKey = row.avatarKey, size = 42.dp)
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = if (isCurrentUser) "${row.username} (You)" else row.username,
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = if (isCurrentUser) GoldBright else TextPrimary,
                  ),
                )
                if (row.entryType == "SAMPLE_DEMO") {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.14f),
                  ) {
                    Text(
                      text = "SAMPLE DEMO",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldChampagne,
                        fontWeight = FontWeight.Bold,
                      ),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }
              }
              Text(
                text = "${row.totalGamesPlayed} rounds played",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = formatCoins(points),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                  color = GoldBright,
                ),
              )
              Text(
                text = "demo pts",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
              )
            }
          }
        }
      }
    }
    item { Spacer(Modifier.height(12.dp)) }
  }
}

@Composable
private fun BoxCompatCenter(content: @Composable () -> Unit) {
  androidx.compose.foundation.layout.Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}
