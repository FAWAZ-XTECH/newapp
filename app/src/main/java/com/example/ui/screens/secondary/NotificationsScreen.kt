package com.example.ui.screens.secondary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.NotificationItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ObsidianCard
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NotificationsScreen(
  notifications: List<NotificationItem>,
  onMarkRead: (String) -> Unit,
  onMarkAllRead: () -> Unit,
  onBack: () -> Unit,
) {
  val unreadCount = notifications.count { it.unread }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("notifications_back_button")) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = GoldBright)
          }
          Column {
            Text("Notification Center", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
            Text("$unreadCount unread", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          }
        }
        if (unreadCount > 0) {
          TextButton(
            onClick = onMarkAllRead,
            modifier = Modifier.testTag("mark_all_read_button"),
          ) {
            Icon(Icons.Rounded.DoneAll, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(4.dp))
            Text("Mark All Read", color = GoldBright)
          }
        }
      }
    }

    if (notifications.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Rounded.NotificationsNone,
          title = "All caught up!",
          message = "Daily reward alerts, game milestones, and platform announcements appear here.",
        )
      }
    } else {
      items(notifications, key = { it.id }) { item ->
        val icon = when (item.category) {
          "DAILY_REWARD" -> Icons.Rounded.CardGiftcard
          "NEW_GAME" -> Icons.Rounded.SportsEsports
          "ACHIEVEMENT" -> Icons.Rounded.EmojiEvents
          else -> Icons.Rounded.Campaign
        }
        ObsidianCard(
          highlighted = item.unread,
          onClick = { if (item.unread) onMarkRead(item.id) },
          modifier = Modifier.fillMaxWidth().testTag("notification_item_${item.id}"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(icon, contentDescription = item.category, tint = GoldBright, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = TextPrimary,
                  modifier = Modifier.weight(1f),
                )
                if (item.unread) {
                  Box(
                    modifier = Modifier
                      .size(9.dp)
                      .clip(CircleShape)
                      .background(GoldBright)
                  )
                }
              }
              Spacer(Modifier.height(4.dp))
              Text(item.body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
              Spacer(Modifier.height(4.dp))
              Text(
                text = "${item.category.replace("_", " ")} • ${formatTimestamp(item.createdAtMillis)}",
                style = MaterialTheme.typography.labelSmall,
                color = GoldPrimary,
              )
            }
          }
        }
      }
    }
  }
}
