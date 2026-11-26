package com.example.ui.screens.games

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MemoryDifficulty(val label: String, val pairs: Int, val columns: Int, val baseReward: Int) {
  EASY("Easy (4x2)", 4, 4, 100),
  MEDIUM("Medium (4x3)", 6, 4, 180),
  HARD("Hard (4x4)", 8, 4, 320),
}

private data class MemoryTile(
  val id: Int,
  val symbol: String,
  val revealed: Boolean = false,
  val matched: Boolean = false,
)

private val MEMORY_SYMBOLS = listOf("👑", "💎", "🪙", "⭐", "🎲", "🏆", "🔥", "⚡")

@Composable
fun MemoryMatchScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  history: List<GameLog>,
  onRecordResult: (rewardCoins: Long, summary: String) -> Unit,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var difficulty by remember { mutableStateOf(MemoryDifficulty.EASY) }
  var tiles by remember { mutableStateOf(createBoard(difficulty)) }
  var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
  var isCheckingPair by remember { mutableStateOf(false) }
  var moves by remember { mutableIntStateOf(0) }
  var elapsedSeconds by remember { mutableIntStateOf(0) }
  var isRunning by remember { mutableStateOf(false) }
  var completionReward by remember { mutableStateOf<Int?>(null) }

  val gameEnabled = config.isGameEnabled(MiniGameId.MEMORY_MATCH.id)
  val matchedPairs = tiles.count { it.matched } / 2

  LaunchedEffect(isRunning) {
    while (isRunning) {
      delay(1000L)
      elapsedSeconds++
    }
  }

  fun restartBoard(newDiff: MemoryDifficulty = difficulty) {
    difficulty = newDiff
    tiles = createBoard(newDiff)
    firstSelectedIndex = null
    isCheckingPair = false
    moves = 0
    elapsedSeconds = 0
    isRunning = false
    completionReward = null
  }

  if (completionReward != null) {
    AlertDialog(
      onDismissRequest = { completionReward = null },
      containerColor = ObsidianCard,
      titleContentColor = GoldBright,
      textContentColor = TextPrimary,
      icon = { Icon(Icons.Rounded.GridView, contentDescription = null, tint = GoldBright) },
      title = { Text("Memory Match Complete!") },
      text = {
        Text("Cleared ${difficulty.label} in $moves moves (${elapsedSeconds}s)! Earned +${completionReward} virtual coins.")
      },
      confirmButton = {
        TextButton(onClick = { restartBoard(difficulty) }, modifier = Modifier.testTag("memory_play_again_ok")) {
          Text("Play Again", color = GoldBright)
        }
      },
      dismissButton = {
        TextButton(onClick = { completionReward = null }) {
          Text("Close", color = TextSecondary)
        }
      },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      GameTopHeader(
        title = "Memory Match",
        balance = account.coinBalance,
        onBack = onBack,
        onOpenWallet = onOpenWallet,
      )
    }

    item {
      ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Pairs: $matchedPairs / ${difficulty.pairs}", style = MaterialTheme.typography.titleMedium, color = GoldBright)
          Text("Moves: $moves", style = MaterialTheme.typography.labelMedium, color = GoldChampagne)
          Text("Time: ${elapsedSeconds}s", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          MemoryDifficulty.entries.forEach { diff ->
            FilterChip(
              selected = difficulty == diff,
              onClick = { restartBoard(diff) },
              label = { Text(diff.label) },
              modifier = Modifier.testTag("memory_diff_${diff.name.lowercase()}"),
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

        val rows = tiles.chunked(difficulty.columns)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          rows.forEachIndexed { rIdx, rowTiles ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              rowTiles.forEachIndexed { cIdx, tile ->
                val globalIndex = rIdx * difficulty.columns + cIdx
                val showFront = tile.revealed || tile.matched
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      when {
                        tile.matched -> EmeraldSurface
                        tile.revealed -> ObsidianElevated
                        else -> ObsidianBlack
                      }
                    )
                    .border(
                      width = 1.5.dp,
                      color = when {
                        tile.matched -> EmeraldWin
                        tile.revealed -> GoldBright
                        else -> ObsidianBorder
                      },
                      shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = gameEnabled && !isCheckingPair && !showFront) {
                      if (!isRunning) isRunning = true
                      val updated = tiles.toMutableList()
                      updated[globalIndex] = tile.copy(revealed = true)
                      tiles = updated

                      val firstIdx = firstSelectedIndex
                      if (firstIdx == null) {
                        firstSelectedIndex = globalIndex
                      } else {
                        moves++
                        isCheckingPair = true
                        val firstTile = updated[firstIdx]
                        if (firstTile.symbol == tile.symbol) {
                          updated[firstIdx] = firstTile.copy(matched = true)
                          updated[globalIndex] = tile.copy(revealed = true, matched = true)
                          tiles = updated
                          firstSelectedIndex = null
                          isCheckingPair = false

                          if (updated.all { it.matched }) {
                            isRunning = false
                            val movePenalty = ((moves - difficulty.pairs) * 5).coerceAtLeast(0)
                            val reward = (difficulty.baseReward - movePenalty).coerceAtLeast(difficulty.baseReward / 2)
                            onRecordResult(
                              reward.toLong(),
                              "${difficulty.label} cleared in $moves moves (${elapsedSeconds}s)",
                            )
                            completionReward = reward
                          }
                        } else {
                          scope.launch {
                            delay(620L)
                            val reverted = tiles.toMutableList()
                            reverted[firstIdx] = reverted[firstIdx].copy(revealed = false)
                            reverted[globalIndex] = reverted[globalIndex].copy(revealed = false)
                            tiles = reverted
                            firstSelectedIndex = null
                            isCheckingPair = false
                          }
                        }
                      }
                    }
                    .testTag("memory_tile_$globalIndex"),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = if (showFront) tile.symbol else "M",
                    fontSize = if (showFront) 28.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showFront) GoldBright else GoldPrimary.copy(alpha = 0.5f),
                  )
                }
              }
            }
          }
        }

        Spacer(Modifier.height(14.dp))
        GoldGradientButton(
          text = if (!gameEnabled) "Paused by Admin" else "Restart Board",
          enabled = gameEnabled,
          icon = Icons.Rounded.Refresh,
          testTag = "memory_restart_button",
          onClick = { restartBoard(difficulty) },
        )
      }
    }

    item {
      GameHistorySection(logs = history.filter { it.gameId == MiniGameId.MEMORY_MATCH.id })
    }

    item { ResponsibleGamingBanner() }
  }
}

private fun createBoard(difficulty: MemoryDifficulty): List<MemoryTile> {
  val symbols = MEMORY_SYMBOLS.take(difficulty.pairs)
  val doubled = (symbols + symbols)
  val shuffled = SecureRng.shuffled(doubled)
  return shuffled.mapIndexed { idx, sym -> MemoryTile(id = idx, symbol = sym) }
}
