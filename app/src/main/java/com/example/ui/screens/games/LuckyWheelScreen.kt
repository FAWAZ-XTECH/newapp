package com.example.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
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
import com.example.ui.components.formatCoins
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

private val WHEEL_REWARDS = listOf(50, 150, 75, 500, 100, 300, 200, 1000)

@Composable
fun LuckyWheelScreen(
  account: PlayerAccount,
  config: PlatformConfig,
  history: List<GameLog>,
  animationsEnabled: Boolean,
  onRecordResult: (reward: Long, summary: String) -> Unit,
  onBack: () -> Unit,
  onOpenWallet: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val rotation = remember { Animatable(0f) }
  var isSpinning by remember { mutableStateOf(false) }
  var popupReward by remember { mutableStateOf<Int?>(null) }

  val todayUtc = remember(account.lastWheelSpinDay) {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())
  }
  val usedToday = if (account.lastWheelSpinDay == todayUtc) account.dailyWheelSpinsUsed else 0
  val remainingSpins = (config.maxDailyWheelSpins - usedToday).coerceAtLeast(0)
  val gameEnabled = config.isGameEnabled(MiniGameId.LUCKY_WHEEL.id)

  if (popupReward != null) {
    AlertDialog(
      onDismissRequest = { popupReward = null },
      containerColor = ObsidianCard,
      titleContentColor = GoldBright,
      textContentColor = TextPrimary,
      icon = { Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = GoldBright, modifier = Modifier.size(34.dp)) },
      title = { Text("Lucky Wheel Result") },
      text = {
        Text("You won +${formatCoins(popupReward!!.toLong())} virtual demo coins!")
      },
      confirmButton = {
        TextButton(onClick = { popupReward = null }, modifier = Modifier.testTag("wheel_result_ok")) {
          Text("Collect", color = GoldBright)
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
        title = "Lucky Wheel",
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
          Column {
            Text("Daily Free Spins", style = MaterialTheme.typography.titleMedium, color = GoldBright)
            Text(
              text = "$remainingSpins of ${config.maxDailyWheelSpins} remaining today",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
          Text(
            text = "FREE ENTRY",
            style = MaterialTheme.typography.labelMedium.copy(color = GoldChampagne),
          )
        }

        Spacer(Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 320.dp)
            .aspectRatio(1f)
            .align(Alignment.CenterHorizontally),
          contentAlignment = Alignment.Center,
        ) {
          val sliceColors = listOf(ObsidianElevated, GoldDeep, ObsidianCard, GoldPrimary)
          Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val sliceAngle = 360f / WHEEL_REWARDS.size

            rotate(degrees = rotation.value, pivot = center) {
              WHEEL_REWARDS.forEachIndexed { index, reward ->
                val startAngle = -90f + index * sliceAngle - sliceAngle / 2f
                val fillColor = sliceColors[index % sliceColors.size]
                drawArc(
                  color = fillColor,
                  startAngle = startAngle,
                  sweepAngle = sliceAngle,
                  useCenter = true,
                  topLeft = Offset(center.x - radius, center.y - radius),
                  size = Size(diameter, diameter),
                )
                drawArc(
                  color = GoldBright.copy(alpha = 0.65f),
                  startAngle = startAngle,
                  sweepAngle = sliceAngle,
                  useCenter = true,
                  topLeft = Offset(center.x - radius, center.y - radius),
                  size = Size(diameter, diameter),
                  style = Stroke(width = 2.5f),
                )

                val midAngleRad = Math.toRadians((startAngle + sliceAngle / 2f).toDouble())
                val textDist = radius * 0.65f
                val tx = center.x + (textDist * cos(midAngleRad)).toFloat()
                val ty = center.y + (textDist * sin(midAngleRad)).toFloat()
                drawContext.canvas.nativeCanvas.apply {
                  val paint = android.graphics.Paint().apply {
                    color = if (fillColor == GoldPrimary || fillColor == GoldDeep) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    textSize = radius * 0.14f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                  }
                  drawText("+$reward", tx, ty + paint.textSize / 3f, paint)
                }
              }
            }

            // Outer gold ring
            drawCircle(
              color = GoldBright,
              radius = radius,
              center = center,
              style = Stroke(width = 8f),
            )
            // Center hub
            drawCircle(color = ObsidianBlack, radius = radius * 0.15f, center = center)
            drawCircle(color = GoldBright, radius = radius * 0.15f, center = center, style = Stroke(width = 5f))

            // Top pointer triangle
            val pointerPath = Path().apply {
              moveTo(center.x, center.y - radius + 24f)
              lineTo(center.x - 18f, center.y - radius - 8f)
              lineTo(center.x + 18f, center.y - radius - 8f)
              close()
            }
            drawPath(pointerPath, color = GoldBright)
          }
        }

        Spacer(Modifier.height(16.dp))

        GoldGradientButton(
          text = when {
            !gameEnabled -> "Temporarily Paused by Admin"
            isSpinning -> "Spinning..."
            remainingSpins <= 0 -> "Daily Spin Limit Reached"
            else -> "Spin the Royal Wheel"
          },
          enabled = gameEnabled && !isSpinning && remainingSpins > 0,
          icon = Icons.Rounded.TrackChanges,
          testTag = "spin_wheel_button",
          onClick = {
            if (isSpinning || remainingSpins <= 0) return@GoldGradientButton
            isSpinning = true
            val targetIdx = SecureRng.nextInt(WHEEL_REWARDS.size)
            val sliceAngle = 360f / WHEEL_REWARDS.size
            // Pointer is at top (-90deg), slice i starts centered at -90 + i*sliceAngle;
            // to land slice targetIdx under pointer, rotate by -(targetIdx * sliceAngle) modulo 360
            val currentMod = ((rotation.value % 360f) + 360f) % 360f
            val desiredMod = ((360f - targetIdx * sliceAngle) % 360f + 360f) % 360f
            val delta = (desiredMod - currentMod + 360f) % 360f
            val extraTurns = if (animationsEnabled) 5 * 360f else 360f
            val finalTarget = rotation.value + extraTurns + delta

            scope.launch {
              rotation.animateTo(
                targetValue = finalTarget,
                animationSpec = tween(
                  durationMillis = if (animationsEnabled) 2600 else 400,
                  easing = FastOutSlowInEasing,
                ),
              )
              val won = WHEEL_REWARDS[targetIdx]
              onRecordResult(won.toLong(), "Landed on +$won virtual coins")
              popupReward = won
              isSpinning = false
            }
          },
        )
      }
    }

    item {
      GameHistorySection(logs = history.filter { it.gameId == MiniGameId.LUCKY_WHEEL.id })
    }

    item { ResponsibleGamingBanner() }
  }
}
