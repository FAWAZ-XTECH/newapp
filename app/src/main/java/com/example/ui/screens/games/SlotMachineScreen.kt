package com.example.ui.screens.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameLog
import com.example.data.model.MiniGameId
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.data.repository.SecureRng
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.RubySurface
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SLOT_SYMBOLS = listOf("7", "👑", "💎", "⭐", "🔔", "🪙")

@Composable
fun SlotMachineScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  history: List<GameLog>,
  animationsEnabled: Boolean,
  onRecordResult: (entryCoins: Long, rewardCoins: Long, outcome: String, summary: String) -> Unit,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var selectedEntry by remember { mutableIntStateOf(50) }
  var reels by remember { mutableStateOf(listOf("7", "👑", "💎")) }
  var isSpinning by remember { mutableStateOf(false) }
  var lastResultBanner by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

  val gameEnabled = config.isGameEnabled(MiniGameId.SLOT_MACHINE.id)
  val canAfford = account.coinBalance >= selectedEntry

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      GameTopHeader(
        title = "Golden Slots",
        balance = account.coinBalance,
        onBack = onBack,
        onOpenWallet = onOpenWallet,
      )
    }

    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Text("Triple Obsidian Reels", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Text(
          text = "Virtual points only • Fair random reel stops",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
        Spacer(Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          reels.forEachIndexed { idx, symbol ->
            Box(
              modifier = Modifier
                .weight(1f)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.verticalGradient(listOf(ObsidianBlack, ObsidianElevated, ObsidianBlack))
                )
                .border(1.5.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .testTag("slot_reel_$idx"),
              contentAlignment = Alignment.Center,
            ) {
              AnimatedContent(targetState = symbol, label = "reelAnim$idx") { sym ->
                Text(
                  text = sym,
                  fontSize = if (sym == "7") 44.sp else 38.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldBright,
                )
              }
            }
          }
        }

        Spacer(Modifier.height(14.dp))

        if (lastResultBanner != null) {
          val (won, msg) = lastResultBanner!!
          Surface(
            modifier = Modifier.fillMaxWidth().testTag("slot_result_banner"),
            shape = RoundedCornerShape(12.dp),
            color = if (won) EmeraldSurface else RubySurface,
            border = BorderStroke(1.dp, if (won) EmeraldWin else RubyLoss),
          ) {
            Text(
              text = msg,
              color = if (won) EmeraldWin else RubyLoss,
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.padding(12.dp),
            )
          }
          Spacer(Modifier.height(12.dp))
        }

        Text("Virtual Entry Coins", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(25, 50, 100).forEach { entry ->
            val selected = selectedEntry == entry
            FilterChip(
              selected = selected,
              enabled = !isSpinning,
              onClick = { selectedEntry = entry },
              label = { Text("$entry Coins") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary,
                selectedLabelColor = ObsidianBlack,
                containerColor = ObsidianCard,
                labelColor = TextSecondary,
              ),
            )
          }
        }

        Spacer(Modifier.height(12.dp))

        GoldGradientButton(
          text = when {
            !gameEnabled -> "Paused by Admin"
            isSpinning -> "Spinning Reels..."
            !canAfford -> "Need $selectedEntry Virtual Coins"
            else -> "Spin Reels ($selectedEntry Coins)"
          },
          enabled = gameEnabled && !isSpinning && canAfford,
          icon = Icons.Rounded.AutoAwesome,
          testTag = "spin_slots_button",
          onClick = {
            if (isSpinning || !canAfford) return@GoldGradientButton
            isSpinning = true
            lastResultBanner = null
            val finalReels = listOf(
              SLOT_SYMBOLS[SecureRng.nextInt(SLOT_SYMBOLS.size)],
              SLOT_SYMBOLS[SecureRng.nextInt(SLOT_SYMBOLS.size)],
              SLOT_SYMBOLS[SecureRng.nextInt(SLOT_SYMBOLS.size)],
            )
            scope.launch {
              val cycles = if (animationsEnabled) 12 else 2
              for (step in 0 until cycles) {
                reels = listOf(
                  if (step < cycles - 4) SLOT_SYMBOLS[SecureRng.nextInt(SLOT_SYMBOLS.size)] else finalReels[0],
                  if (step < cycles - 2) SLOT_SYMBOLS[SecureRng.nextInt(SLOT_SYMBOLS.size)] else finalReels[1],
                  SLOT_SYMBOLS[SecureRng.nextInt(SLOT_SYMBOLS.size)],
                )
                delay(if (animationsEnabled) 95L else 40L)
              }
              reels = finalReels

              val reward = calculateSlotReward(finalReels, selectedEntry)
              val won = reward > 0
              val summary = "${finalReels.joinToString(" ")} -> ${if (won) "+$reward Coins" else "No Match"}"
              onRecordResult(
                selectedEntry.toLong(),
                reward.toLong(),
                if (won) "WIN" else "LOSS",
                summary,
              )
              lastResultBanner = won to if (won) {
                "Winner! ${finalReels.joinToString(" ")} awarded +$reward virtual coins!"
              } else {
                "Reels stopped at ${finalReels.joinToString(" ")}. Try another spin!"
              }
              isSpinning = false
            }
          },
        )
      }
    }

    item {
      ObsidianCard(modifier = Modifier.fillMaxWidth()) {
        Text("Virtual Payout Multipliers", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Spacer(Modifier.height(6.dp))
        Text("• 3x 7 = 15x entry   • 3x 👑 = 10x entry   • 3x 💎 = 8x entry", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text("• Any 3 matching = 5x entry   • Any 2 matching = 1.5x entry", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
      }
    }

    item {
      GameHistorySection(logs = history.filter { it.gameId == MiniGameId.SLOT_MACHINE.id })
    }

    item { ResponsibleGamingBanner() }
  }
}

private fun calculateSlotReward(reels: List<String>, entry: Int): Int {
  val (a, b, c) = reels
  return when {
    a == "7" && b == "7" && c == "7" -> entry * 15
    a == "👑" && b == "👑" && c == "👑" -> entry * 10
    a == "💎" && b == "💎" && c == "💎" -> entry * 8
    a == b && b == c -> entry * 5
    a == b || b == c || a == c -> (entry * 3) / 2
    else -> 0
  }
}
