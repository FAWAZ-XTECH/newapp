package com.example.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DataMode
import com.example.data.model.MiniGameId
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val GoldGradientBrush = Brush.horizontalGradient(
  colors = listOf(GoldDeep, GoldPrimary, GoldBright, GoldPrimary)
)

val ObsidianGoldBorder = Brush.linearGradient(
  colors = listOf(GoldPrimary.copy(alpha = 0.55f), ObsidianBorder, GoldDeep.copy(alpha = 0.35f))
)

fun formatCoins(amount: Long): String = NumberFormat.getNumberInstance(Locale.US).format(amount)

fun formatTimestamp(millis: Long): String {
  val fmt = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
  return fmt.format(Date(millis))
}

@Composable
fun M9999LogoMark(
  modifier: Modifier = Modifier,
  iconSize: Dp = 40.dp,
  showSubtitle: Boolean = false,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_m9999_icon),
      contentDescription = "M9999 Demo Play logo",
      modifier = Modifier
        .size(iconSize)
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
      contentScale = ContentScale.Crop,
    )
    Column {
      Text(
        text = "M9999",
        style = MaterialTheme.typography.titleLarge.copy(
          fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
          color = GoldBright,
          fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
      )
      if (showSubtitle) {
        Text(
          text = "DEMO PLAY LOUNGE",
          style = MaterialTheme.typography.labelSmall.copy(color = GoldChampagne.copy(alpha = 0.85f)),
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
fun GoldGradientButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  testTag: String = "gold_action_button",
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier
      .heightIn(min = 48.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = Color.Transparent,
      contentColor = ObsidianBlack,
      disabledContainerColor = ObsidianElevated,
      disabledContentColor = TextMuted,
    ),
    contentPadding = PaddingValues(0.dp),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .background(
          brush = if (enabled) GoldGradientBrush else Brush.horizontalGradient(listOf(ObsidianElevated, ObsidianElevated)),
          shape = RoundedCornerShape(14.dp),
        )
        .padding(horizontal = 18.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (enabled) ObsidianBlack else TextMuted,
          )
          Spacer(Modifier.width(8.dp))
        }
        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
          color = if (enabled) ObsidianBlack else TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun ObsidianCard(
  modifier: Modifier = Modifier,
  highlighted: Boolean = false,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val cardModifier = if (onClick != null) {
    modifier.clickable(onClick = onClick)
  } else {
    modifier
  }
  Card(
    modifier = cardModifier,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = if (highlighted) ObsidianElevated else ObsidianCard,
      contentColor = TextPrimary,
    ),
    border = BorderStroke(
      width = if (highlighted) 1.2.dp else 1.dp,
      brush = if (highlighted) ObsidianGoldBorder else Brush.linearGradient(listOf(ObsidianBorder, ObsidianBorder)),
    ),
  ) {
    Column(modifier = Modifier.padding(16.dp), content = content)
  }
}

@Composable
fun CoinBalancePill(
  balance: Long,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val animatedCoins by animateIntAsState(
    targetValue = balance.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
    animationSpec = tween(durationMillis = 450),
    label = "coinBalanceAnim",
  )
  Surface(
    modifier = modifier
      .heightIn(min = 48.dp)
      .clip(RoundedCornerShape(24.dp))
      .clickable(onClick = onClick)
      .testTag("coin_balance_pill"),
    shape = RoundedCornerShape(24.dp),
    color = ObsidianElevated,
    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.MonetizationOn,
        contentDescription = "Virtual coins",
        tint = GoldBright,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = formatCoins(animatedCoins.toLong()),
        style = MaterialTheme.typography.labelMedium.copy(color = GoldChampagne),
        maxLines = 1,
      )
    }
  }
}

@Composable
fun DataModeBadge(mode: DataMode, modifier: Modifier = Modifier) {
  val isCloud = mode == DataMode.FIREBASE_CLOUD
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = if (isCloud) EmeraldWin.copy(alpha = 0.14f) else GoldPrimary.copy(alpha = 0.14f),
    border = BorderStroke(
      1.dp,
      if (isCloud) EmeraldWin.copy(alpha = 0.45f) else GoldPrimary.copy(alpha = 0.45f),
    ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Icon(
        imageVector = if (isCloud) Icons.Rounded.CloudDone else Icons.Rounded.PhoneAndroid,
        contentDescription = null,
        tint = if (isCloud) EmeraldWin else GoldBright,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = if (isCloud) "CLOUD SYNC" else "LOCAL DEMO",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = if (isCloud) EmeraldWin else GoldBright,
        ),
      )
    }
  }
}

@Composable
fun AvatarBadge(
  avatarKey: String,
  size: Dp = 48.dp,
  modifier: Modifier = Modifier,
) {
  if (avatarKey == "royal") {
    Image(
      painter = painterResource(id = R.drawable.img_avatar_royal),
      contentDescription = "Player avatar",
      modifier = modifier
        .size(size)
        .clip(CircleShape)
        .border(1.5.dp, GoldPrimary, CircleShape),
      contentScale = ContentScale.Crop,
    )
  } else {
    val icon = when (avatarKey) {
      "crown" -> Icons.Rounded.MilitaryTech
      "diamond" -> Icons.Rounded.Diamond
      "ace" -> Icons.Rounded.Style
      else -> Icons.Rounded.AutoAwesome
    }
    Box(
      modifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(GoldPrimary.copy(alpha = 0.35f), ObsidianElevated)
          )
        )
        .border(1.5.dp, GoldPrimary, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = "Player avatar",
        tint = GoldBright,
        modifier = Modifier.size(size * 0.55f),
      )
    }
  }
}

fun gameIconFor(gameId: String): ImageVector = when (gameId) {
  MiniGameId.LUCKY_WHEEL.id -> Icons.Rounded.TrackChanges
  MiniGameId.SLOT_MACHINE.id -> Icons.Rounded.AutoAwesome
  MiniGameId.DICE_GAME.id -> Icons.Rounded.Casino
  MiniGameId.CARD_REVEAL.id -> Icons.Rounded.Style
  MiniGameId.LUCKY_NUMBER.id -> Icons.Rounded.ConfirmationNumber
  MiniGameId.MEMORY_MATCH.id -> Icons.Rounded.GridView
  else -> Icons.Rounded.Diamond
}

@Composable
fun ResponsibleGamingBanner(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = ObsidianCard,
    border = BorderStroke(1.dp, GoldDeep.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Shield,
        contentDescription = "Responsible gaming notice",
        tint = GoldPrimary,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = "Free-to-Play Demo Only • All coins & rewards are fictional demo points with zero real-money value. No deposits, bets, or withdrawals.",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
      )
    }
  }
}

@Composable
fun EmptyStateCard(
  icon: ImageVector,
  title: String,
  message: String,
  modifier: Modifier = Modifier,
) {
  ObsidianCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = GoldPrimary,
        modifier = Modifier.size(36.dp),
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
      )
    }
  }
}

@Composable
fun AdminShieldBadge(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = GoldPrimary.copy(alpha = 0.16f),
    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.VerifiedUser,
        contentDescription = null,
        tint = GoldBright,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = "ADMIN",
        style = MaterialTheme.typography.labelSmall.copy(
          color = GoldBright,
          fontWeight = FontWeight.Bold,
        ),
      )
    }
  }
}
