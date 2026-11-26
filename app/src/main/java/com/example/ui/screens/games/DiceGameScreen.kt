package com.example.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.RubySurface
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class DicePrediction(val label: String, val multiplierText: String) {
  UNDER_7("Under 7", "2x"),
  LUCKY_7("Exact 7", "4x"),
  OVER_7("Over 7", "2x"),
  DOUBLES("Doubles", "5x"),
}

@Composable
fun DiceGameScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  history: List<GameLog>,
  animationsEnabled: Boolean,
  onRecordResult: (entryCoins: Long, rewardCoins: Long, outcome: String, summary: String) -> Unit,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var dieOne by remember { mutableIntStateOf(6) }
  var dieTwo by remember { mutableIntStateOf(1) }
  var selectedPrediction by remember { mutableStateOf(DicePrediction.OVER_7) }
  var selectedEntry by remember { mutableIntStateOf(40) }
  var isRolling by remember { mutableStateOf(false) }
  var resultBanner by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
  val shakeRotation = remember { Animatable(0f) }

  val gameEnabled = config.isGameEnabled(MiniGameId.DICE_GAME.id)
  val canAfford = account.coinBalance >= selectedEntry

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      GameTopHeader(
        title = "Royal Dice",
        balance = account.coinBalance,
        onBack = onBack,
        onOpenWallet = onOpenWallet,
      )
    }

    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Text("Twin Obsidian Dice", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Text("Predict the roll total or doubles", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(18.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { rotationZ = shakeRotation.value },
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          DieFaceCanvas(value = dieOne, modifier = Modifier.size(104.dp))
          DieFaceCanvas(value = dieTwo, modifier = Modifier.size(104.dp))
        }

        Spacer(Modifier.height(10.dp))
        Text(
          text = "Current Total: ${dieOne + dieTwo}",
          style = MaterialTheme.typography.titleMedium.copy(color = GoldChampagne),
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        if (resultBanner != null) {
          Spacer(Modifier.height(12.dp))
          val (won, msg) = resultBanner!!
          Surface(
            modifier = Modifier.fillMaxWidth().testTag("dice_result_banner"),
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
        Text("Pick Prediction", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        Spacer(Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          DicePrediction.entries.forEach { pred ->
            val selected = selectedPrediction == pred
            FilterChip(
              selected = selected,
              enabled = !isRolling,
              onClick = { selectedPrediction = pred },
              label = { Text("${pred.label} (${pred.multiplierText})") },
              modifier = Modifier.weight(1f),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary,
                selectedLabelColor = ObsidianBlack,
                containerColor = ObsidianCard,
                labelColor = TextSecondary,
              ),
            )
          }
        }

        Spacer(Modifier.height(10.dp))
        Text("Virtual Entry Coins", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(20, 40, 80).forEach { entry ->
            FilterChip(
              selected = selectedEntry == entry,
              enabled = !isRolling,
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

        Spacer(Modifier.height(14.dp))
        GoldGradientButton(
          text = when {
            !gameEnabled -> "Paused by Admin"
            isRolling -> "Rolling Dice..."
            !canAfford -> "Need $selectedEntry Virtual Coins"
            resultBanner != null -> "Play Again ($selectedEntry Coins)"
            else -> "Roll Twin Dice ($selectedEntry Coins)"
          },
          enabled = gameEnabled && !isRolling && canAfford,
          icon = Icons.Rounded.Casino,
          testTag = "roll_dice_button",
          onClick = {
            if (isRolling || !canAfford) return@GoldGradientButton
            isRolling = true
            resultBanner = null
            val finalOne = SecureRng.nextIntRange(1, 6)
            val finalTwo = SecureRng.nextIntRange(1, 6)
            scope.launch {
              val steps = if (animationsEnabled) 10 else 2
              for (i in 0 until steps) {
                dieOne = SecureRng.nextIntRange(1, 6)
                dieTwo = SecureRng.nextIntRange(1, 6)
                shakeRotation.animateTo(
                  if (i % 2 == 0) 9f else -9f,
                  animationSpec = tween(55),
                )
                delay(25L)
              }
              shakeRotation.animateTo(0f, tween(80))
              dieOne = finalOne
              dieTwo = finalTwo

              val total = finalOne + finalTwo
              val won = when (selectedPrediction) {
                DicePrediction.UNDER_7 -> total < 7
                DicePrediction.LUCKY_7 -> total == 7
                DicePrediction.OVER_7 -> total > 7
                DicePrediction.DOUBLES -> finalOne == finalTwo
              }
              val multiplier = when (selectedPrediction) {
                DicePrediction.UNDER_7 -> 2
                DicePrediction.LUCKY_7 -> 4
                DicePrediction.OVER_7 -> 2
                DicePrediction.DOUBLES -> 5
              }
              val reward = if (won) selectedEntry * multiplier else 0
              val summary = "Rolled $finalOne + $finalTwo = $total (${selectedPrediction.label})"
              onRecordResult(
                selectedEntry.toLong(),
                reward.toLong(),
                if (won) "WIN" else "LOSS",
                summary,
              )
              resultBanner = won to if (won) {
                "Nice roll! $finalOne & $finalTwo (Total $total) won +$reward virtual coins!"
              } else {
                "Rolled $finalOne & $finalTwo (Total $total). Tap Play Again to roll again."
              }
              isRolling = false
            }
          },
        )
      }
    }

    item {
      GameHistorySection(logs = history.filter { it.gameId == MiniGameId.DICE_GAME.id })
    }

    item { ResponsibleGamingBanner() }
  }
}

@Composable
private fun DieFaceCanvas(value: Int, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    drawRoundRect(
      color = ObsidianElevated,
      cornerRadius = CornerRadius(24f, 24f),
    )
    drawRoundRect(
      color = GoldBright,
      cornerRadius = CornerRadius(24f, 24f),
      style = Stroke(width = 5f),
    )

    val pipRadius = w * 0.085f
    val left = w * 0.27f
    val centerX = w * 0.5f
    val right = w * 0.73f
    val top = h * 0.27f
    val centerY = h * 0.5f
    val bottom = h * 0.73f

    val pips = when (value) {
      1 -> listOf(Offset(centerX, centerY))
      2 -> listOf(Offset(left, top), Offset(right, bottom))
      3 -> listOf(Offset(left, top), Offset(centerX, centerY), Offset(right, bottom))
      4 -> listOf(Offset(left, top), Offset(right, top), Offset(left, bottom), Offset(right, bottom))
      5 -> listOf(Offset(left, top), Offset(right, top), Offset(centerX, centerY), Offset(left, bottom), Offset(right, bottom))
      else -> listOf(
        Offset(left, top), Offset(right, top),
        Offset(left, centerY), Offset(right, centerY),
        Offset(left, bottom), Offset(right, bottom),
      )
    }
    pips.forEach { pos ->
      drawCircle(color = GoldBright, radius = pipRadius, center = pos)
    }
  }
}
