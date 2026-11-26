package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PlatformConfig
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
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DailyRewardsScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  transactions: List<WalletTx>,
  onClaimDaily: (onResult: (Result<Long>) -> Unit) -> Unit,
) {
  var isClaiming by remember { mutableStateOf(false) }
  var rewardPopupCoins by remember { mutableStateOf<Long?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  val todayUtc = remember(account.lastDailyClaimDay) {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())
  }
  val alreadyClaimedToday = account.lastDailyClaimDay == todayUtc

  // Live countdown to next UTC day
  var millisUntilNextUtc by remember { mutableLongStateOf(computeMillisToNextUtcMidnight()) }
  LaunchedEffect(Unit) {
    while (true) {
      millisUntilNextUtc = computeMillisToNextUtcMidnight()
      delay(1000L)
    }
  }

  val nextStreak = if (alreadyClaimedToday) account.loginStreak else account.loginStreak + 1
  val nextBonus = (config.dailyBaseBonus + (nextStreak - 1).coerceAtLeast(0) * config.dailyStreakIncrement).toLong()
  val dailyHistory = remember(transactions) { transactions.filter { it.type == "DAILY_BONUS" } }

  if (rewardPopupCoins != null) {
    AlertDialog(
      onDismissRequest = { rewardPopupCoins = null },
      containerColor = ObsidianCard,
      titleContentColor = GoldBright,
      textContentColor = TextPrimary,
      icon = { Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = GoldBright, modifier = Modifier.size(36.dp)) },
      title = { Text("Daily Reward Claimed!") },
      text = {
        Text(
          "You received +${formatCoins(rewardPopupCoins!!)} virtual demo coins for Day ${account.loginStreak} streak!",
          style = MaterialTheme.typography.bodyLarge,
        )
      },
      confirmButton = {
        TextButton(onClick = { rewardPopupCoins = null }, modifier = Modifier.testTag("daily_reward_popup_ok")) {
          Text("Awesome", color = GoldBright)
        }
      },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Daily Rewards Vault", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
        Text(
          text = "Collect consecutive daily virtual coin drops. One claim per UTC day.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
      }
    }

    // Hero Vault Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_promo_daily),
          contentDescription = "Daily rewards vault",
          modifier = Modifier.matchParentSize(),
          contentScale = ContentScale.Crop,
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                listOf(ObsidianBlack.copy(alpha = 0.65f), ObsidianBlack.copy(alpha = 0.94f))
              )
            )
            .padding(18.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = GoldBright)
                Text(
                  text = "${account.loginStreak}-Day Streak",
                  style = MaterialTheme.typography.titleLarge.copy(color = GoldBright),
                )
              }
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = ObsidianElevated,
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Icon(Icons.Rounded.Schedule, contentDescription = null, tint = GoldChampagne, modifier = Modifier.size(14.dp))
                  Text(
                    text = formatCountdown(millisUntilNextUtc),
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldChampagne),
                  )
                }
              }
            }

            Text(
              text = if (alreadyClaimedToday) {
                "Today's bonus collected! Next drop unlocks in ${formatCountdown(millisUntilNextUtc)}."
              } else {
                "Day $nextStreak Bonus Ready: +${formatCoins(nextBonus)} Virtual Coins"
              },
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )

            if (errorMsg != null) {
              Text(errorMsg!!, color = RubyLoss, style = MaterialTheme.typography.bodyMedium)
            }

            GoldGradientButton(
              text = when {
                isClaiming -> "Claiming..."
                alreadyClaimedToday -> "Claimed Today"
                else -> "Claim +${formatCoins(nextBonus)} Virtual Coins"
              },
              enabled = !alreadyClaimedToday && !isClaiming,
              icon = if (alreadyClaimedToday) Icons.Rounded.CheckCircle else Icons.Rounded.CardGiftcard,
              testTag = "claim_daily_bonus_button",
              onClick = {
                errorMsg = null
                isClaiming = true
                onClaimDaily { res ->
                  isClaiming = false
                  res.onSuccess { bonus -> rewardPopupCoins = bonus }
                  res.onFailure { err -> errorMsg = err.localizedMessage ?: "Unable to claim right now." }
                }
              },
            )
          }
        }
      }
    }

    // 7-Day Streak Ladder
    item {
      Text("Consecutive Streak Ladder", style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(8.dp))
      val currentCycleDay = (account.loginStreak % 7).let { if (it == 0 && account.loginStreak > 0 && alreadyClaimedToday) 7 else it }
      LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items((1..7).toList()) { day ->
          val rewardForDay = config.dailyBaseBonus + (day - 1) * config.dailyStreakIncrement
          val isCompleted = day <= currentCycleDay
          val isNext = !alreadyClaimedToday && day == currentCycleDay + 1
          Column(
            modifier = Modifier
              .size(width = 92.dp, height = 104.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(if (isNext) ObsidianElevated else ObsidianCard)
              .border(
                width = if (isNext) 1.5.dp else 1.dp,
                color = when {
                  isNext -> GoldBright
                  isCompleted -> EmeraldWin.copy(alpha = 0.6f)
                  else -> ObsidianBorder
                },
                shape = RoundedCornerShape(14.dp),
              )
              .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "DAY $day",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isNext) GoldBright else TextSecondary,
                fontWeight = FontWeight.Bold,
              ),
            )
            Icon(
              imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.CardGiftcard,
              contentDescription = null,
              tint = if (isCompleted) EmeraldWin else GoldPrimary,
              modifier = Modifier.size(24.dp),
            )
            Text(
              text = "+$rewardForDay",
              style = MaterialTheme.typography.labelMedium.copy(
                color = if (isCompleted) EmeraldWin else GoldChampagne,
              ),
            )
          }
        }
      }
    }

    // Claim History
    item {
      Text("Claim History", style = MaterialTheme.typography.titleLarge)
    }

    if (dailyHistory.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Rounded.CardGiftcard,
          title = "No daily claims yet",
          message = "Tap Claim above to collect your first daily streak reward.",
        )
      }
    } else {
      items(dailyHistory, key = { it.id }) { tx ->
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(tx.title, style = MaterialTheme.typography.titleMedium)
              Text(formatTimestamp(tx.createdAtMillis), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Text(
              text = "+${formatCoins(tx.amount)}",
              style = MaterialTheme.typography.labelMedium.copy(color = EmeraldWin),
            )
          }
        }
      }
    }

    item {
      ResponsibleGamingBanner()
    }
  }
}

private fun computeMillisToNextUtcMidnight(): Long {
  val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
  val next = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    timeInMillis = now.timeInMillis
    add(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  return (next.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
}

private fun formatCountdown(millis: Long): String {
  val totalSec = millis / 1000L
  val hrs = totalSec / 3600L
  val mins = (totalSec % 3600L) / 60L
  val secs = totalSec % 60L
  return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}
