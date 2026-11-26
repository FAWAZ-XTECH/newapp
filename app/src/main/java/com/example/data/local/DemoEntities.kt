package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ActivityAuditItem
import com.example.data.model.AnnouncementItem
import com.example.data.model.GameLog
import com.example.data.model.LeaderboardRow
import com.example.data.model.NotificationItem
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.data.model.WalletTx

@Entity(tableName = "demo_users")
data class DemoUserEntity(
  @PrimaryKey val userId: String,
  val username: String,
  val fullName: String,
  val email: String,
  val passwordHash: String,
  val avatarKey: String,
  val coinBalance: Long,
  val dailyRewardPoints: Long,
  val totalGamesPlayed: Long,
  val totalRewardsEarned: Long,
  val loginStreak: Int,
  val lastDailyClaimDay: String,
  val lastWheelSpinDay: String,
  val dailyWheelSpinsUsed: Int,
  val accountStatus: String,
  val role: String,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
) {
  fun toDomain(): PlayerAccount = PlayerAccount(
    userId = userId,
    username = username,
    fullName = fullName,
    email = email,
    avatarKey = avatarKey,
    coinBalance = coinBalance,
    dailyRewardPoints = dailyRewardPoints,
    totalGamesPlayed = totalGamesPlayed,
    totalRewardsEarned = totalRewardsEarned,
    loginStreak = loginStreak,
    lastDailyClaimDay = lastDailyClaimDay,
    lastWheelSpinDay = lastWheelSpinDay,
    dailyWheelSpinsUsed = dailyWheelSpinsUsed,
    accountStatus = accountStatus,
    role = role,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
  )
}

@Entity(tableName = "demo_transactions")
data class DemoTransactionEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val type: String,
  val title: String,
  val amount: Long,
  val balanceAfter: Long,
  val gameId: String?,
  val createdAtMillis: Long,
) {
  fun toDomain(): WalletTx = WalletTx(
    id = id,
    userId = userId,
    type = type,
    title = title,
    amount = amount,
    balanceAfter = balanceAfter,
    gameId = gameId,
    createdAtMillis = createdAtMillis,
  )
}

@Entity(tableName = "demo_game_history")
data class DemoGameHistoryEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val gameId: String,
  val gameName: String,
  val outcome: String,
  val entryCoins: Long,
  val rewardCoins: Long,
  val summary: String,
  val createdAtMillis: Long,
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
    createdAtMillis = createdAtMillis,
  )
}

@Entity(tableName = "demo_notifications")
data class DemoNotificationEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val category: String,
  val title: String,
  val body: String,
  val readStatus: String,
  val createdAtMillis: Long,
) {
  fun toDomain(): NotificationItem = NotificationItem(
    id = id,
    userId = userId,
    category = category,
    title = title,
    body = body,
    readStatus = readStatus,
    createdAtMillis = createdAtMillis,
  )
}

@Entity(tableName = "demo_leaderboard")
data class DemoLeaderboardEntity(
  @PrimaryKey val userId: String,
  val username: String,
  val avatarKey: String,
  val weeklyPoints: Long,
  val monthlyPoints: Long,
  val allTimePoints: Long,
  val totalGamesPlayed: Long,
  val entryType: String,
) {
  fun toDomain(): LeaderboardRow = LeaderboardRow(
    userId = userId,
    username = username,
    avatarKey = avatarKey,
    weeklyPoints = weeklyPoints,
    monthlyPoints = monthlyPoints,
    allTimePoints = allTimePoints,
    totalGamesPlayed = totalGamesPlayed,
    entryType = entryType,
  )
}

@Entity(tableName = "demo_announcements")
data class DemoAnnouncementEntity(
  @PrimaryKey val id: String,
  val title: String,
  val message: String,
  val badge: String,
  val authorEmail: String,
  val createdAtMillis: Long,
) {
  fun toDomain(): AnnouncementItem = AnnouncementItem(
    id = id,
    title = title,
    message = message,
    badge = badge,
    authorEmail = authorEmail,
    createdAtMillis = createdAtMillis,
  )
}

@Entity(tableName = "demo_config")
data class DemoConfigEntity(
  @PrimaryKey val id: String = "global",
  val dailyBaseBonus: Int = 250,
  val dailyStreakIncrement: Int = 50,
  val maxDailyWheelSpins: Int = 3,
  val disabledGamesCsv: String = "",
  val maintenanceBanner: String = "",
) {
  fun toDomain(): PlatformConfig = PlatformConfig(
    dailyBaseBonus = dailyBaseBonus,
    dailyStreakIncrement = dailyStreakIncrement,
    maxDailyWheelSpins = maxDailyWheelSpins,
    disabledGames = disabledGamesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    maintenanceBanner = maintenanceBanner,
  )
}

@Entity(tableName = "demo_activity_logs")
data class DemoActivityLogEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val username: String,
  val action: String,
  val detail: String,
  val pointsDelta: Long,
  val createdAtMillis: Long,
) {
  fun toDomain(): ActivityAuditItem = ActivityAuditItem(
    id = id,
    userId = userId,
    username = username,
    action = action,
    detail = detail,
    pointsDelta = pointsDelta,
    createdAtMillis = createdAtMillis,
  )
}
