package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class DataMode {
  FIREBASE_CLOUD,
  LOCAL_DEMO,
}

enum class GameCategory(val label: String) {
  ALL("All Games"),
  WHEEL_SLOTS("Wheel & Slots"),
  DICE_CARDS("Dice & Cards"),
  NUMBERS_PUZZLE("Numbers & Memory"),
}

enum class MiniGameId(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: GameCategory,
  val defaultEntryCoins: Int,
  val maxPotentialCoins: Int,
  val isFeatured: Boolean,
  val isPopular: Boolean,
) {
  LUCKY_WHEEL(
    id = "lucky_wheel",
    title = "Lucky Wheel",
    subtitle = "Spin the golden royal wheel for daily virtual coin drops",
    category = GameCategory.WHEEL_SLOTS,
    defaultEntryCoins = 0,
    maxPotentialCoins = 1000,
    isFeatured = true,
    isPopular = true,
  ),
  SLOT_MACHINE(
    id = "slot_machine",
    title = "Golden Slots",
    subtitle = "Match three royal emblems across the triple obsidian reels",
    category = GameCategory.WHEEL_SLOTS,
    defaultEntryCoins = 50,
    maxPotentialCoins = 750,
    isFeatured = true,
    isPopular = true,
  ),
  DICE_GAME(
    id = "dice_game",
    title = "Royal Dice",
    subtitle = "Predict the twin golden dice total — Under 7, Lucky 7, Over 7, or Doubles",
    category = GameCategory.DICE_CARDS,
    defaultEntryCoins = 40,
    maxPotentialCoins = 320,
    isFeatured = false,
    isPopular = true,
  ),
  CARD_REVEAL(
    id = "card_reveal",
    title = "Card Reveal",
    subtitle = "Flip one of three gold-foil cards to uncover hidden multipliers",
    category = GameCategory.DICE_CARDS,
    defaultEntryCoins = 50,
    maxPotentialCoins = 350,
    isFeatured = true,
    isPopular = false,
  ),
  LUCKY_NUMBER(
    id = "lucky_number",
    title = "Lucky Number",
    subtitle = "Pick your lucky digit (1-10) for an instant animated draw",
    category = GameCategory.NUMBERS_PUZZLE,
    defaultEntryCoins = 30,
    maxPotentialCoins = 300,
    isFeatured = false,
    isPopular = false,
  ),
  MEMORY_MATCH(
    id = "memory_match",
    title = "Memory Match",
    subtitle = "Pair matching royal tokens against the clock across 3 difficulty tiers",
    category = GameCategory.NUMBERS_PUZZLE,
    defaultEntryCoins = 0,
    maxPotentialCoins = 320,
    isFeatured = true,
    isPopular = true,
  );

  companion object {
    fun fromId(id: String): MiniGameId? = entries.firstOrNull { it.id == id }
  }
}

enum class AvatarOption(val key: String, val label: String) {
  ROYAL("royal", "Royal Lion"),
  CROWN("crown", "Gold Crown"),
  DIAMOND("diamond", "Obsidian Gem"),
  ACE("ace", "Golden Ace"),
  PHOENIX("phoenix", "Solar Crest");

  companion object {
    fun fromKey(key: String): AvatarOption = entries.firstOrNull { it.key == key } ?: ROYAL
  }
}

enum class LeaderboardPeriod(val label: String) {
  WEEKLY("Weekly"),
  MONTHLY("Monthly"),
  ALL_TIME("All-Time"),
}

// Unified UI Domain Models (mode-agnostic)

data class PlayerAccount(
  val userId: String,
  val username: String,
  val fullName: String,
  val email: String,
  val avatarKey: String,
  val coinBalance: Long,
  val dailyRewardPoints: Long,
  val totalGamesPlayed: Long,
  val totalRewardsEarned: Long,
  val loginStreak: Int,
  val lastDailyClaimDay: String,
  val lastWheelSpinDay: String,
  val dailyWheelSpinsUsed: Int,
  val accountStatus: String, // "ACTIVE" or "DISABLED"
  val role: String, // "PLAYER" or "ADMIN"
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
)

data class WalletTx(
  val id: String,
  val userId: String,
  val type: String,
  val title: String,
  val amount: Long,
  val balanceAfter: Long,
  val gameId: String?,
  val createdAtMillis: Long,
)

data class GameLog(
  val id: String,
  val userId: String,
  val gameId: String,
  val gameName: String,
  val outcome: String, // WIN, LOSS, DRAW, REWARD
  val entryCoins: Long,
  val rewardCoins: Long,
  val summary: String,
  val createdAtMillis: Long,
)

data class NotificationItem(
  val id: String,
  val userId: String,
  val category: String, // DAILY_REWARD, NEW_GAME, ANNOUNCEMENT, ACHIEVEMENT, SYSTEM
  val title: String,
  val body: String,
  val readStatus: String, // UNREAD, READ
  val createdAtMillis: Long,
) {
  val unread: Boolean get() = readStatus == "UNREAD"
}

data class LeaderboardRow(
  val userId: String,
  val username: String,
  val avatarKey: String,
  val weeklyPoints: Long,
  val monthlyPoints: Long,
  val allTimePoints: Long,
  val totalGamesPlayed: Long,
  val entryType: String, // PLAYER or SAMPLE_DEMO
)

data class AnnouncementItem(
  val id: String,
  val title: String,
  val message: String,
  val badge: String,
  val authorEmail: String,
  val createdAtMillis: Long,
)

data class PlatformConfig(
  val dailyBaseBonus: Int = 250,
  val dailyStreakIncrement: Int = 50,
  val maxDailyWheelSpins: Int = 3,
  val disabledGames: List<String> = emptyList(),
  val maintenanceBanner: String = "",
) {
  fun isGameEnabled(gameId: String): Boolean = !disabledGames.contains(gameId)
}

data class ActivityAuditItem(
  val id: String,
  val userId: String,
  val username: String,
  val action: String,
  val detail: String,
  val pointsDelta: Long,
  val createdAtMillis: Long,
)

// Firestore DTOs (1:1 parity with firestore.rules & firebase-blueprint.json)

@Keep
@IgnoreExtraProperties
data class UserProfileDto(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "",
  val fullName: String = "",
  val email: String = "",
  val avatarKey: String = "royal",
  val coinBalance: Long = 1000L,
  val dailyRewardPoints: Long = 0L,
  val totalGamesPlayed: Long = 0L,
  val totalRewardsEarned: Long = 0L,
  val loginStreak: Long = 0L,
  val lastDailyClaimDay: String = "",
  val lastWheelSpinDay: String = "",
  val dailyWheelSpinsUsed: Long = 0L,
  val accountStatus: String = "ACTIVE",
  val role: String = "PLAYER",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  fun toDomain(): PlayerAccount = PlayerAccount(
    userId = userId.ifBlank { id },
    username = username,
    fullName = fullName,
    email = email,
    avatarKey = avatarKey,
    coinBalance = coinBalance,
    dailyRewardPoints = dailyRewardPoints,
    totalGamesPlayed = totalGamesPlayed,
    totalRewardsEarned = totalRewardsEarned,
    loginStreak = loginStreak.toInt(),
    lastDailyClaimDay = lastDailyClaimDay,
    lastWheelSpinDay = lastWheelSpinDay,
    dailyWheelSpinsUsed = dailyWheelSpinsUsed.toInt(),
    accountStatus = accountStatus,
    role = role,
    createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
    updatedAtMillis = updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
  )
}

@Keep
@IgnoreExtraProperties
data class WalletTransactionDto(
  @DocumentId val id: String = "",
  val userId: String = "",
  val type: String = "GAME_WIN",
  val title: String = "",
  val amount: Long = 0L,
  val balanceAfter: Long = 0L,
  val gameId: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  fun toDomain(): WalletTx = WalletTx(
    id = id,
    userId = userId,
    type = type,
    title = title,
    amount = amount,
    balanceAfter = balanceAfter,
    gameId = gameId,
    createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
  )
}

@Keep
@IgnoreExtraProperties
data class GameHistoryDto(
  @DocumentId val id: String = "",
  val userId: String = "",
  val gameId: String = "",
  val gameName: String = "",
  val outcome: String = "WIN",
  val entryCoins: Long = 0L,
  val rewardCoins: Long = 0L,
  val summary: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  fun toDomain(): GameLog = GameLog(
    id = id,
    userId = userId,
    gameId = gameId,
    gameName = gameName,
    outcome = outcome,
    entryCoins = entryCoins,
    rewardCoins = rewardCoins,
    summary = summary,
    createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
  )
}

@Keep
@IgnoreExtraProperties
data class AppNotificationDto(
  @DocumentId val id: String = "",
  val userId: String = "",
  val category: String = "SYSTEM",
  val title: String = "",
  val body: String = "",
  val readStatus: String = "UNREAD",
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  fun toDomain(): NotificationItem = NotificationItem(
    id = id,
    userId = userId,
    category = category,
    title = title,
    body = body,
    readStatus = readStatus,
    createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
  )
}

@Keep
@IgnoreExtraProperties
data class LeaderboardEntryDto(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "",
  val avatarKey: String = "royal",
  val weeklyPoints: Long = 0L,
  val monthlyPoints: Long = 0L,
  val allTimePoints: Long = 0L,
  val totalGamesPlayed: Long = 0L,
  val entryType: String = "PLAYER",
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  fun toDomain(): LeaderboardRow = LeaderboardRow(
    userId = userId.ifBlank { id },
    username = username,
    avatarKey = avatarKey,
    weeklyPoints = weeklyPoints,
    monthlyPoints = monthlyPoints,
    allTimePoints = allTimePoints,
    totalGamesPlayed = totalGamesPlayed,
    entryType = entryType,
  )
}

@Keep
@IgnoreExtraProperties
data class AnnouncementDto(
  @DocumentId val id: String = "",
  val title: String = "",
  val message: String = "",
  val badge: String = "NEWS",
  val authorEmail: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  fun toDomain(): AnnouncementItem = AnnouncementItem(
    id = id,
    title = title,
    message = message,
    badge = badge,
    authorEmail = authorEmail,
    createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
  )
}

@Keep
@IgnoreExtraProperties
data class AppConfigDto(
  @DocumentId val id: String = "",
  val dailyBaseBonus: Long = 250L,
  val dailyStreakIncrement: Long = 50L,
  val maxDailyWheelSpins: Long = 3L,
  val disabledGames: List<String> = emptyList(),
  val maintenanceBanner: String = "",
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  fun toDomain(): PlatformConfig = PlatformConfig(
    dailyBaseBonus = dailyBaseBonus.toInt().coerceIn(50, 10000),
    dailyStreakIncrement = dailyStreakIncrement.toInt().coerceIn(0, 2000),
    maxDailyWheelSpins = maxDailyWheelSpins.toInt().coerceIn(1, 50),
    disabledGames = disabledGames,
    maintenanceBanner = maintenanceBanner,
  )
}

@Keep
@IgnoreExtraProperties
data class ActivityLogDto(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "",
  val action: String = "",
  val detail: String = "",
  val pointsDelta: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  fun toDomain(): ActivityAuditItem = ActivityAuditItem(
    id = id,
    userId = userId,
    username = username,
    action = action,
    detail = detail,
    pointsDelta = pointsDelta,
    createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
  )
}
