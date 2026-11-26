package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AnnouncementItem
import com.example.data.model.DataMode
import com.example.data.model.GameCategory
import com.example.data.model.GameLog
import com.example.data.model.MiniGameId
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.ui.components.AvatarBadge
import com.example.ui.components.CoinBalancePill
import com.example.ui.components.DataModeBadge
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.M9999LogoMark
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.components.formatCoins
import com.example.ui.components.formatTimestamp
import com.example.ui.components.gameIconFor
import com.example.ui.theme.AmberInfo
import com.example.ui.theme.AmberSurface
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

@Composable
fun HomeDashboardScreen(
  account: PlayerAccount,
  mode: DataMode,
  config: PlatformConfig,
  announcements: List<AnnouncementItem>,
  recentGames: List<GameLog>,
  unreadNotifications: Int,
  onOpenGame: (MiniGameId) -> Unit,
  onOpenRewards: () -> Unit,
  onOpenWallet: () -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenProfile: () -> Unit,
  onOpenAllGames: () -> Unit,
) {
  var selectedCategory by remember { mutableStateOf(GameCategory.ALL) }
  val allGames = MiniGameId.entries
  val filteredGames = remember(selectedCategory) {
    if (selectedCategory == GameCategory.ALL) allGames
    else allGames.filter { it.category == selectedCategory }
  }
  val featuredGames = remember(filteredGames) { filteredGames.filter { it.isFeatured }.ifEmpty { filteredGames } }
  val popularGames = remember(filteredGames) { filteredGames.filter { it.isPopular }.ifEmpty { filteredGames } }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Top Header: Logo, Mode Badge, Coin Balance, Notifications, Avatar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        M9999LogoMark(iconSize = 38.dp)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CoinBalancePill(balance = account.coinBalance, onClick = onOpenWallet)
          IconButton(
            onClick = onOpenNotifications,
            modifier = Modifier.size(48.dp).testTag("home_notifications_button"),
          ) {
            BadgedBox(
              badge = {
                if (unreadNotifications > 0) {
                  Badge(containerColor = GoldBright, contentColor = ObsidianBlack) {
                    Text(unreadNotifications.coerceAtMost(99).toString())
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = GoldChampagne,
              )
            }
          }
          AvatarBadge(
            avatarKey = account.avatarKey,
            size = 40.dp,
            modifier = Modifier
              .clickable(onClick = onOpenProfile)
              .testTag("home_profile_avatar"),
          )
        }
      }
    }

    // Welcome & Balance Overview Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_hero_banner),
          contentDescription = "M9999 Arcade lounge banner",
          modifier = Modifier.matchParentSize(),
          contentScale = ContentScale.Crop,
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                colors = listOf(ObsidianBlack.copy(alpha = 0.92f), ObsidianBlack.copy(alpha = 0.65f))
              )
            )
            .padding(18.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Welcome back, ${account.username}",
                style = MaterialTheme.typography.titleLarge.copy(color = GoldBright),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              DataModeBadge(mode = mode)
            }
            Text(
              text = "${formatCoins(account.coinBalance)} Virtual Demo Coins",
              style = MaterialTheme.typography.displayMedium.copy(color = TextPrimary),
            )
            Text(
              text = "${account.totalGamesPlayed} rounds played • ${account.loginStreak}-day streak",
              style = MaterialTheme.typography.bodyMedium.copy(color = GoldChampagne.copy(alpha = 0.85f)),
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              GoldGradientButton(
                text = "Daily Bonus",
                icon = Icons.Rounded.CardGiftcard,
                onClick = onOpenRewards,
                modifier = Modifier.weight(1f),
                testTag = "home_daily_bonus_button",
              )
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 48.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable(onClick = onOpenWallet)
                  .testTag("home_wallet_button"),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianElevated.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
              ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                  Text(
                    text = "Virtual Wallet",
                    style = MaterialTheme.typography.labelLarge.copy(color = GoldChampagne),
                  )
                }
              }
            }
          }
        }
      }
    }

    // Optional Admin Maintenance Banner
    if (config.maintenanceBanner.isNotBlank()) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = AmberSurface,
          border = BorderStroke(1.dp, AmberInfo.copy(alpha = 0.6f)),
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = AmberInfo)
            Text(
              text = config.maintenanceBanner,
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimary,
            )
          }
        }
      }
    }

    // Promotional Banner for Free Virtual Rewards
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(126.dp)
          .clip(RoundedCornerShape(18.dp))
          .clickable(onClick = onOpenRewards)
          .testTag("home_promo_banner"),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_promo_daily),
          contentDescription = "Free virtual rewards treasure chest banner",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                colors = listOf(ObsidianBlack.copy(alpha = 0.9f), ObsidianBlack.copy(alpha = 0.28f))
              )
            )
            .padding(16.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary,
            ) {
              Text(
                text = "FREE VIRTUAL REWARDS",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = ObsidianBlack,
                  fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
            Text(
              text = "Claim Up to +${formatCoins((config.dailyBaseBonus + 6 * config.dailyStreakIncrement).toLong())} Demo Coins",
              style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
            )
            Text(
              text = "Tap to collect daily streak bonus & Lucky Wheel spins",
              style = MaterialTheme.typography.bodyMedium.copy(color = GoldChampagne),
            )
          }
        }
      }
    }

    // Game Categories Filter
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Game Categories", style = MaterialTheme.typography.titleLarge)
          TextButton(onClick = onOpenAllGames, modifier = Modifier.testTag("home_view_all_games")) {
            Text("View All (6)", color = GoldPrimary)
          }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(GameCategory.entries) { category ->
            val selected = selectedCategory == category
            FilterChip(
              selected = selected,
              onClick = { selectedCategory = category },
              label = { Text(category.label) },
              modifier = Modifier.heightIn(min = 48.dp).testTag("category_chip_${category.name.lowercase()}"),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary,
                selectedLabelColor = ObsidianBlack,
                containerColor = ObsidianCard,
                labelColor = TextSecondary,
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = ObsidianBorder,
                selectedBorderColor = GoldBright,
              ),
            )
          }
        }
      }
    }

    // Featured Games Horizontal Carousel
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Featured Games", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          items(featuredGames, key = { it.id }) { game ->
            val enabled = config.isGameEnabled(game.id)
            FeaturedGameTile(
              game = game,
              enabled = enabled,
              onClick = { onOpenGame(game) },
            )
          }
        }
      }
    }

    // Popular Games List
    item {
      Text("Popular Games", style = MaterialTheme.typography.titleLarge)
    }
    items(popularGames, key = { "pop_${it.id}" }) { game ->
      val enabled = config.isGameEnabled(game.id)
      CompactGameRowCard(
        game = game,
        enabled = enabled,
        onClick = { onOpenGame(game) },
      )
    }

    // Recently Played Games
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Recently Played", style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = onOpenHistory, modifier = Modifier.testTag("home_view_history_button")) {
          Icon(Icons.Rounded.History, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Full History", color = GoldPrimary)
        }
      }
    }
    if (recentGames.isEmpty()) {
      item {
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "No rounds played yet. Tap any game above to launch your first virtual session!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
        }
      }
    } else {
      items(recentGames.take(4), key = { "rec_${it.id}" }) { log ->
        ObsidianCard(
          modifier = Modifier.fillMaxWidth(),
          onClick = { MiniGameId.fromId(log.gameId)?.let(onOpenGame) },
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(log.gameName, style = MaterialTheme.typography.titleMedium)
              Text(
                text = "${log.summary} • ${formatTimestamp(log.createdAtMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
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

    // Announcements
    if (announcements.isNotEmpty()) {
      item {
        Text("Announcements", style = MaterialTheme.typography.titleLarge)
      }
      items(announcements.take(3), key = { "ann_${it.id}" }) { item ->
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = GoldBright, modifier = Modifier.size(20.dp))
            Text(item.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.16f),
            ) {
              Text(
                text = item.badge,
                style = MaterialTheme.typography.labelSmall.copy(color = GoldBright),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }
          Spacer(Modifier.height(6.dp))
          Text(item.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
      }
    }

    item {
      ResponsibleGamingBanner()
      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
private fun FeaturedGameTile(
  game: MiniGameId,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  ObsidianCard(
    highlighted = enabled,
    onClick = if (enabled) onClick else null,
    modifier = Modifier
      .width(210.dp)
      .testTag("featured_game_${game.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(GoldPrimary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = gameIconFor(game.id),
          contentDescription = game.title,
          tint = GoldBright,
          modifier = Modifier.size(24.dp),
        )
      }
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) GoldPrimary.copy(alpha = 0.18f) else ObsidianElevated,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(Icons.Rounded.Star, contentDescription = null, tint = GoldBright, modifier = Modifier.size(12.dp))
          Text(
            text = if (enabled) "Up to ${game.maxPotentialCoins}" else "Paused",
            style = MaterialTheme.typography.labelSmall.copy(color = GoldChampagne),
          )
        }
      }
    }
    Spacer(Modifier.height(12.dp))
    Text(
      text = game.title,
      style = MaterialTheme.typography.titleMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = game.subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(12.dp))
    GoldGradientButton(
      text = if (enabled) "Play Now" else "Disabled by Admin",
      enabled = enabled,
      icon = Icons.Rounded.PlayArrow,
      onClick = onClick,
      testTag = "play_featured_${game.id}",
    )
  }
}

@Composable
fun CompactGameRowCard(
  game: MiniGameId,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  ObsidianCard(
    onClick = if (enabled) onClick else null,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("game_card_${game.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(
            Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.25f), ObsidianElevated))
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = gameIconFor(game.id),
          contentDescription = game.title,
          tint = GoldBright,
          modifier = Modifier.size(26.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(game.title, style = MaterialTheme.typography.titleMedium)
        Text(
          text = if (enabled) game.subtitle else "Temporarily paused by administrator",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
          text = if (game.defaultEntryCoins == 0) "Free Entry • Virtual Rewards" else "Entry: ${game.defaultEntryCoins} Coins • Max ${game.maxPotentialCoins}",
          style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary),
        )
      }
      Surface(
        modifier = Modifier
          .heightIn(min = 48.dp)
          .clip(RoundedCornerShape(12.dp))
          .clickable(enabled = enabled, onClick = onClick)
          .testTag("launch_game_${game.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) GoldPrimary else ObsidianElevated,
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = if (enabled) "Play" else "Off",
            style = MaterialTheme.typography.labelLarge.copy(
              color = if (enabled) ObsidianBlack else TextSecondary,
              fontWeight = FontWeight.Bold,
            ),
          )
        }
      }
    }
  }
}
