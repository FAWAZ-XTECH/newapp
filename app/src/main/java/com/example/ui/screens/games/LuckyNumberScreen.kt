package com.example.ui.screens.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LuckyNumberScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  history: List<GameLog>,
  animationsEnabled: Boolean,
  onRecordResult: (entryCoins: Long, rewardCoins: Long, outcome: String, summary: String) -> Unit,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var chosenNumber by remember { mutableIntStateOf(7) }
  var drawnNumber by remember { mutableIntStateOf(7) }
  var selectedEntry by remember { mutableIntStateOf(30) }
  var isDrawing by remember { mutableStateOf(false) }
  var resultBanner by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

  val gameEnabled = config.isGameEnabled(MiniGameId.LUCKY_NUMBER.id)
  val canAfford = account.coinBalance >= selectedEntry

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      GameTopHeader(
        title = "Lucky Number",
        balance = account.coinBalance,
        onBack = onBack,
        onOpenWallet = onOpenWallet,
      )
    }

    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Text("Golden Number Draw (1 - 10)", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Text("Exact match = 8x • Off by 1 = 1.5x virtual coins", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .size(116.dp)
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(listOf(GoldPrimary.copy(alpha = 0.28f), ObsidianBlack))
            )
            .border(2.5.dp, GoldBright, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          AnimatedContent(targetState = drawnNumber, label = "luckyNumAnim") { num ->
            Text(
              text = num.toString(),
              fontSize = 46.sp,
              fontWeight = FontWeight.Bold,
              color = GoldBright,
              fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
            )
          }
        }

        Spacer(Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Your Pick: #$chosenNumber", style = MaterialTheme.typography.titleMedium, color = GoldChampagne)
          OutlinedButton(
            onClick = { chosenNumber = SecureRng.nextIntRange(1, 10) },
            enabled = !isDrawing,
            border = BorderStroke(1.dp, GoldPrimary),
            modifier = Modifier.testTag("lucky_quick_pick_button"),
          ) {
            androidx.compose.material3.Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(4.dp))
            Text("Quick Pick", color = GoldChampagne)
          }
        }

        Spacer(Modifier.height(8.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          (1..10).forEach { num ->
            val selected = chosenNumber == num
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) GoldPrimary else ObsidianElevated)
                .border(1.dp, if (selected) GoldBright else ObsidianBorder, RoundedCornerShape(12.dp))
                .clickable(enabled = !isDrawing) { chosenNumber = num }
                .testTag("pick_number_$num"),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = num.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                  color = if (selected) ObsidianBlack else TextPrimary,
                  fontWeight = FontWeight.Bold,
                ),
              )
            }
          }
        }

        Spacer(Modifier.height(14.dp))
        Text("Virtual Entry Coins", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(20, 30, 60).forEach { entry ->
            FilterChip(
              selected = selectedEntry == entry,
              enabled = !isDrawing,
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

        if (resultBanner != null) {
          Spacer(Modifier.height(12.dp))
          val (won, msg) = resultBanner!!
          Surface(
            modifier = Modifier.fillMaxWidth().testTag("lucky_number_result"),
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
        }

        Spacer(Modifier.height(14.dp))
        GoldGradientButton(
          text = when {
            !gameEnabled -> "Paused by Admin"
            isDrawing -> "Drawing..."
            !canAfford -> "Need $selectedEntry Virtual Coins"
            else -> "Draw Lucky Number ($selectedEntry Coins)"
          },
          enabled = gameEnabled && !isDrawing && canAfford,
          icon = Icons.Rounded.ConfirmationNumber,
          testTag = "draw_lucky_number_button",
          onClick = {
            if (isDrawing || !canAfford) return@GoldGradientButton
            isDrawing = true
            resultBanner = null
            val target = SecureRng.nextIntRange(1, 10)
            scope.launch {
              val cycles = if (animationsEnabled) 12 else 2
              repeat(cycles) {
                drawnNumber = SecureRng.nextIntRange(1, 10)
                delay(if (animationsEnabled) 85L else 35L)
              }
              drawnNumber = target
              val diff = abs(target - chosenNumber)
              val reward = when (diff) {
                0 -> selectedEntry * 8
                1 -> (selectedEntry * 3) / 2
                else -> 0
              }
              val won = reward > 0
              val summary = "Picked #$chosenNumber, Drew #$target"
              onRecordResult(
                selectedEntry.toLong(),
                reward.toLong(),
                if (won) "WIN" else "LOSS",
                summary,
              )
              resultBanner = won to when (diff) {
                0 -> "JACKPOT MATCH #$target! Won +$reward virtual coins!"
                1 -> "Close hit (#$target vs #$chosenNumber)! Won +$reward virtual coins!"
                else -> "Drawn #$target vs your #$chosenNumber. Draw again!"
              }
              isDrawing = false
            }
          },
        )
      }
    }

    item {
      GameHistorySection(logs = history.filter { it.gameId == MiniGameId.LUCKY_NUMBER.id })
    }

    item { ResponsibleGamingBanner() }
  }
}
