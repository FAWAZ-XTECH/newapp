package com.example.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.RubySurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class FoilCardOption(val label: String, val multiplier: Int, val symbol: String)

@Composable
fun CardRevealScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  history: List<GameLog>,
  animationsEnabled: Boolean,
  onRecordResult: (entryCoins: Long, rewardCoins: Long, outcome: String, summary: String) -> Unit,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  var selectedEntry by remember { mutableIntStateOf(50) }
  var deck by remember { mutableStateOf(generateRoundDeck()) }
  var pickedIndex by remember { mutableStateOf<Int?>(null) }
  var resultBanner by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

  val gameEnabled = config.isGameEnabled(MiniGameId.CARD_REVEAL.id)
  val canAfford = account.coinBalance >= selectedEntry

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      GameTopHeader(
        title = "Card Reveal",
        balance = account.coinBalance,
        onBack = onBack,
        onOpenWallet = onOpenWallet,
      )
    }

    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Text("Royal Three-Card Flip", style = MaterialTheme.typography.titleMedium, color = GoldBright)
        Text(
          text = if (pickedIndex == null) "Select entry coins and tap one gold-foil card to flip." else "Tap Reset Deck to play another round.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
        Spacer(Modifier.height(14.dp))

        Text("Virtual Entry Coins", style = MaterialTheme.typography.labelLarge, color = GoldChampagne)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(25, 50, 100).forEach { entry ->
            FilterChip(
              selected = selectedEntry == entry,
              enabled = pickedIndex == null,
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

        Spacer(Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          deck.forEachIndexed { idx, card ->
            val revealed = pickedIndex != null
            val isChosen = pickedIndex == idx
            val flipAngle by animateFloatAsState(
              targetValue = if (revealed) 180f else 0f,
              animationSpec = tween(durationMillis = if (animationsEnabled) 480 else 120),
              label = "cardFlip$idx",
            )
            Box(
              modifier = Modifier
                .weight(1f)
                .height(156.dp)
                .graphicsLayer {
                  rotationY = flipAngle
                  cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = gameEnabled && pickedIndex == null && canAfford) {
                  pickedIndex = idx
                  val won = card.multiplier > 0
                  val reward = selectedEntry * card.multiplier
                  onRecordResult(
                    selectedEntry.toLong(),
                    reward.toLong(),
                    if (won) "WIN" else "LOSS",
                    "Picked ${card.label} (${card.multiplier}x)",
                  )
                  resultBanner = won to if (won) {
                    "Revealed ${card.label}! Won +$reward virtual coins!"
                  } else {
                    "Revealed ${card.label}. Shuffle and try again!"
                  }
                }
                .testTag("reveal_card_$idx"),
              contentAlignment = Alignment.Center,
            ) {
              if (flipAngle <= 90f) {
                // Card back
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      Brush.verticalGradient(listOf(ObsidianElevated, ObsidianBlack))
                    )
                    .border(
                      width = 1.5.dp,
                      color = if (canAfford && gameEnabled) GoldPrimary else ObsidianBorder,
                      shape = RoundedCornerShape(16.dp),
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Style, contentDescription = "Hidden card ${idx + 1}", tint = GoldBright, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("TAP", style = MaterialTheme.typography.labelMedium, color = GoldChampagne)
                  }
                }
              } else {
                // Card front (un-mirrored horizontally because rotationY = 180)
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(if (isChosen) ObsidianElevated else ObsidianCard)
                    .border(
                      width = if (isChosen) 2.dp else 1.dp,
                      color = when {
                        isChosen && card.multiplier > 0 -> EmeraldWin
                        isChosen -> RubyLoss
                        else -> ObsidianBorder
                      },
                      shape = RoundedCornerShape(16.dp),
                    )
                    .padding(8.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(card.symbol, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                      text = "${card.multiplier}x",
                      style = MaterialTheme.typography.titleLarge.copy(
                        color = if (card.multiplier > 0) GoldBright else TextSecondary,
                        fontWeight = FontWeight.Bold,
                      ),
                    )
                    Text(
                      text = card.label,
                      style = MaterialTheme.typography.labelSmall,
                      color = if (isChosen) TextPrimary else TextSecondary,
                    )
                  }
                }
              }
            }
          }
        }

        if (resultBanner != null) {
          Spacer(Modifier.height(14.dp))
          val (won, msg) = resultBanner!!
          Surface(
            modifier = Modifier.fillMaxWidth().testTag("card_reveal_result"),
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
          text = if (!gameEnabled) "Paused by Admin" else if (!canAfford && pickedIndex == null) "Need $selectedEntry Coins" else "Shuffle New Round",
          enabled = gameEnabled && (pickedIndex != null || canAfford),
          icon = Icons.Rounded.Refresh,
          testTag = "reset_cards_button",
          onClick = {
            deck = generateRoundDeck()
            pickedIndex = null
            resultBanner = null
          },
        )
      }
    }

    item {
      GameHistorySection(logs = history.filter { it.gameId == MiniGameId.CARD_REVEAL.id })
    }

    item { ResponsibleGamingBanner() }
  }
}

private fun generateRoundDeck(): List<FoilCardOption> {
  val topCard = if (SecureRng.nextInt(4) == 0) {
    FoilCardOption("Royal Ace", 5, "👑")
  } else {
    FoilCardOption("Gold King", 3, "💎")
  }
  return SecureRng.shuffled(
    listOf(
      topCard,
      FoilCardOption("Silver Pair", 2, "⭐"),
      FoilCardOption("Empty Joker", 0, "🃏"),
    )
  )
}
