package com.example.data.local

import com.example.data.model.ActivityAuditItem
import com.example.data.model.AnnouncementItem
import com.example.data.model.GameLog
import com.example.data.model.LeaderboardRow
import com.example.data.model.MiniGameId
import com.example.data.model.NotificationItem
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.data.model.WalletTx
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Isolated Local Demo persistence layer backed by Room (`m9999_demo_local.db`) and DataStore.
 * Completely separated from Firebase Cloud Firestore production storage.
 */
class LocalDemoRepository(
  private val db: DemoDatabase,
  private val prefs: DemoSessionPreferences,
) {
  private fun todayUtc(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(Date())
  }

  suspend fun ensureSeedData() {
    if (db.configDao().getConfig() == null) {
      db.configDao().upsert(DemoConfigEntity())
    }
    db.leaderboardDao().insertIfAbsent(
      listOf(
        DemoLeaderboardEntity("sample_gold_king", "GoldKing_Demo", "crown", 3800, 8400, 16200, 48, "SAMPLE_DEMO"),
        DemoLeaderboardEntity("sample_royal_ace", "RoyalAce_Demo", "ace", 2950, 6700, 12900, 39, "SAMPLE_DEMO"),
        DemoLeaderboardEntity("sample_obsidian_7", "Obsidian7_Demo", "diamond", 2100, 5100, 9850, 31, "SAMPLE_DEMO"),
        DemoLeaderboardEntity("sample_luna_spin", "LunaSpin_Demo", "phoenix", 1600, 3900, 7400, 24, "SAMPLE_DEMO"),
        DemoLeaderboardEntity("sample_jade_dice", "JadeDice_Demo", "royal", 1200, 2800, 5200, 19, "SAMPLE_DEMO"),
      )
    )
    val now = System.currentTimeMillis()
    db.announcementDao().insertIfAbsent(
      listOf(
        DemoAnnouncementEntity(
          id = "ann_welcome",
          title = "Welcome to M9999 Demo Play",
          message = "Free-to-play arcade lounge! Enjoy 6 mini-games using virtual demo coins with zero monetary value.",
          badge = "WELCOME",
          authorEmail = "system@m9999demo.local",
          createdAtMillis = now - 3_600_000L,
        ),
        DemoAnnouncementEntity(
          id = "ann_streak",
          title = "7-Day Royal Streak Drops",
          message = "Log in daily and claim escalating virtual demo coin bonuses every 24 hours.",
          badge = "REWARDS",
          authorEmail = "system@m9999demo.local",
          createdAtMillis = now - 7_200_000L,
        ),
      )
    )
  }

  suspend fun signUpDemo(
    fullName: String,
    email: String,
    username: String,
    password: String,
  ): Result<PlayerAccount> {
    ensureSeedData()
    val cleanFull = fullName.trim()
    val cleanEmail = email.trim().lowercase(Locale.US)
    val cleanUser = username.trim()

    if (cleanFull.length < 2) return Result.failure(IllegalArgumentException("Enter your full name."))
    if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
      return Result.failure(IllegalArgumentException("Enter a valid email address."))
    }
    if (cleanUser.length < 3) {
      return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
    }
    if (password.length < 6) {
      return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
    }
    if (db.userDao().getUserByEmail(cleanEmail) != null) {
      return Result.failure(IllegalStateException("An account with this email already exists in Local Demo storage."))
    }
    if (db.userDao().getUserByUsername(cleanUser) != null) {
      return Result.failure(IllegalStateException("Username is already taken in Local Demo storage."))
    }

    val now = System.currentTimeMillis()
    val uid = "demo_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val entity = DemoUserEntity(
      userId = uid,
      username = cleanUser,
      fullName = cleanFull,
      email = cleanEmail,
      passwordHash = PasswordHasher.hashPassword(password),
      avatarKey = "royal",
      coinBalance = 1500L,
      dailyRewardPoints = 0L,
      totalGamesPlayed = 0L,
      totalRewardsEarned = 1500L,
      loginStreak = 0,
      lastDailyClaimDay = "",
      lastWheelSpinDay = "",
      dailyWheelSpinsUsed = 0,
      accountStatus = "ACTIVE",
      role = "PLAYER",
      createdAtMillis = now,
      updatedAtMillis = now,
    )
    db.userDao().upsertUser(entity)
    db.transactionDao().insert(
      DemoTransactionEntity(
        id = UUID.randomUUID().toString(),
        userId = uid,
        type = "WELCOME_BONUS",
        title = "M9999 Demo Welcome Pack",
        amount = 1500L,
        balanceAfter = 1500L,
        gameId = null,
        createdAtMillis = now,
      )
    )
    db.notificationDao().insert(
      DemoNotificationEntity(
        id = UUID.randomUUID().toString(),
        userId = uid,
        category = "SYSTEM",
        title = "Welcome to M9999 Local Demo!",
        body = "1,500 virtual coins added. Note: Local Demo Mode persists data on this device only.",
        readStatus = "UNREAD",
        createdAtMillis = now,
      )
    )
    db.leaderboardDao().upsert(
      DemoLeaderboardEntity(
        userId = uid,
        username = cleanUser,
        avatarKey = "royal",
        weeklyPoints = 1500L,
        monthlyPoints = 1500L,
        allTimePoints = 1500L,
        totalGamesPlayed = 0L,
        entryType = "PLAYER",
      )
    )
    db.activityLogDao().insert(
      DemoActivityLogEntity(
        id = UUID.randomUUID().toString(),
        userId = uid,
        username = cleanUser,
        action = "REGISTER_DEMO",
        detail = "Registered new local demo account",
        pointsDelta = 1500L,
        createdAtMillis = now,
      )
    )
    prefs.setDemoUserId(uid)
    return Result.success(entity.toDomain())
  }

  suspend fun loginDemo(emailOrUsername: String, password: String): Result<PlayerAccount> {
    ensureSeedData()
    val identifier = emailOrUsername.trim()
    val user = db.userDao().getUserByEmail(identifier) ?: db.userDao().getUserByUsername(identifier)
      ?: return Result.failure(IllegalArgumentException("No local demo account found for '$identifier'."))

    if (!PasswordHasher.verifyPassword(password, user.passwordHash)) {
      return Result.failure(IllegalArgumentException("Incorrect password."))
    }
    if (user.accountStatus == "DISABLED") {
      return Result.failure(IllegalStateException("This local demo account has been disabled by an administrator."))
    }
    prefs.setDemoUserId(user.userId)
    return Result.success(user.toDomain())
  }

  suspend fun startInstantGuestDemo(): Result<PlayerAccount> {
    ensureSeedData()
    val existing = db.userDao().getUserByEmail("demo.player@m9999.local")
    if (existing != null) {
      if (existing.accountStatus == "DISABLED") {
        return Result.failure(IllegalStateException("The quick demo player is disabled. Enable it in Admin or register a new demo account."))
      }
      prefs.setDemoUserId(existing.userId)
      return Result.success(existing.toDomain())
    }
    return signUpDemo(
      fullName = "Royal Demo Guest",
      email = "demo.player@m9999.local",
      username = "RoyalDemo99",
      password = UUID.randomUUID().toString(),
    )
  }

  suspend fun verifyDemoEmailForReset(email: String): Result<String> {
    val cleanEmail = email.trim().lowercase(Locale.US)
    val user = db.userDao().getUserByEmail(cleanEmail)
      ?: return Result.failure(IllegalArgumentException("No local demo account matches that email."))
    return Result.success(user.email)
  }

  suspend fun resetDemoPassword(email: String, newPassword: String): Result<Unit> {
    if (newPassword.length < 6) {
      return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
    }
    val user = db.userDao().getUserByEmail(email.trim())
      ?: return Result.failure(IllegalArgumentException("Demo account not found."))
    db.userDao().upsertUser(
      user.copy(
        passwordHash = PasswordHasher.hashPassword(newPassword),
        updatedAtMillis = System.currentTimeMillis(),
      )
    )
    return Result.success(Unit)
  }

  suspend fun changeDemoPassword(userId: String, currentPassword: String, newPassword: String): Result<Unit> {
    val user = db.userDao().getUserById(userId)
      ?: return Result.failure(IllegalStateException("Account not found."))
    if (!PasswordHasher.verifyPassword(currentPassword, user.passwordHash)) {
      return Result.failure(IllegalArgumentException("Current password is incorrect."))
    }
    if (newPassword.length < 6) {
      return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
    }
    db.userDao().upsertUser(
      user.copy(
        passwordHash = PasswordHasher.hashPassword(newPassword),
        updatedAtMillis = System.currentTimeMillis(),
      )
    )
    return Result.success(Unit)
  }

  suspend fun logoutDemo() {
    prefs.setDemoUserId(null)
  }

  fun observeUser(userId: String): Flow<PlayerAccount?> =
    db.userDao().observeUser(userId).map { it?.toDomain() }

  fun observeTransactions(userId: String): Flow<List<WalletTx>> =
    db.transactionDao().observeTransactions(userId).map { list -> list.map { it.toDomain() } }

  fun observeGameHistory(userId: String): Flow<List<GameLog>> =
    db.gameHistoryDao().observeGameHistory(userId).map { list -> list.map { it.toDomain() } }

  fun observeNotifications(userId: String): Flow<List<NotificationItem>> =
    db.notificationDao().observeNotifications(userId).map { list -> list.map { it.toDomain() } }

  fun observeLeaderboard(): Flow<List<LeaderboardRow>> =
    db.leaderboardDao().observeLeaderboard().map { list -> list.map { it.toDomain() } }

  fun observeAnnouncements(): Flow<List<AnnouncementItem>> =
    db.announcementDao().observeAnnouncements().map { list -> list.map { it.toDomain() } }

  fun observeConfig(): Flow<PlatformConfig> =
    db.configDao().observeConfig().map { it?.toDomain() ?: PlatformConfig() }

  fun observeAllUsersAdmin(): Flow<List<PlayerAccount>> =
    db.userDao().observeAllUsers().map { list -> list.map { it.toDomain() } }

  fun observeActivityLogs(): Flow<List<ActivityAuditItem>> =
    db.activityLogDao().observeAllLogs().map { list -> list.map { it.toDomain() } }

  suspend fun updateProfile(userId: String, fullName: String, username: String, avatarKey: String): Result<Unit> {
    val user = db.userDao().getUserById(userId) ?: return Result.failure(IllegalStateException("Account missing"))
    val cleanFull = fullName.trim()
    val cleanUser = username.trim()
    if (cleanFull.isEmpty()) return Result.failure(IllegalArgumentException("Full name cannot be empty."))
    if (cleanUser.length < 3) return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
    val conflict = db.userDao().getUserByUsername(cleanUser)
    if (conflict != null && conflict.userId != userId) {
      return Result.failure(IllegalStateException("Username already taken."))
    }
    val updated = user.copy(
      fullName = cleanFull,
      username = cleanUser,
      avatarKey = avatarKey,
      updatedAtMillis = System.currentTimeMillis(),
    )
    db.userDao().upsertUser(updated)
    db.leaderboardDao().upsert(
      DemoLeaderboardEntity(
        userId = userId,
        username = cleanUser,
        avatarKey = avatarKey,
        weeklyPoints = updated.totalRewardsEarned,
        monthlyPoints = updated.totalRewardsEarned,
        allTimePoints = updated.totalRewardsEarned,
        totalGamesPlayed = updated.totalGamesPlayed,
        entryType = "PLAYER",
      )
    )
    return Result.success(Unit)
  }

  suspend fun claimDailyReward(userId: String, config: PlatformConfig): Result<Long> {
    val user = db.userDao().getUserById(userId) ?: return Result.failure(IllegalStateException("Account missing"))
    if (user.accountStatus == "DISABLED") {
      return Result.failure(IllegalStateException("Account is disabled by administrator."))
    }
    val today = todayUtc()
    if (user.lastDailyClaimDay == today) {
      return Result.failure(IllegalStateException("Daily bonus already claimed for today."))
    }
    val now = System.currentTimeMillis()
    val newStreak = user.loginStreak + 1
    val bonus = (config.dailyBaseBonus + (newStreak - 1) * config.dailyStreakIncrement).toLong()
    val newBalance = user.coinBalance + bonus
    val newRewards = user.totalRewardsEarned + bonus
    val updated = user.copy(
      coinBalance = newBalance,
      dailyRewardPoints = user.dailyRewardPoints + bonus,
      totalRewardsEarned = newRewards,
      loginStreak = newStreak,
      lastDailyClaimDay = today,
      updatedAtMillis = now,
    )
    db.userDao().upsertUser(updated)
    db.transactionDao().insert(
      DemoTransactionEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        type = "DAILY_BONUS",
        title = "Day $newStreak Daily Login Bonus",
        amount = bonus,
        balanceAfter = newBalance,
        gameId = null,
        createdAtMillis = now,
      )
    )
    db.notificationDao().insert(
      DemoNotificationEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        category = "DAILY_REWARD",
        title = "Daily Bonus Claimed (+$bonus Coins)",
        body = "Day $newStreak streak unlocked! Come back tomorrow for even more virtual coins.",
        readStatus = "UNREAD",
        createdAtMillis = now,
      )
    )
    db.leaderboardDao().upsert(
      DemoLeaderboardEntity(
        userId = userId,
        username = updated.username,
        avatarKey = updated.avatarKey,
        weeklyPoints = newRewards,
        monthlyPoints = newRewards,
        allTimePoints = newRewards,
        totalGamesPlayed = updated.totalGamesPlayed,
        entryType = "PLAYER",
      )
    )
    db.activityLogDao().insert(
      DemoActivityLogEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        username = updated.username,
        action = "DAILY_REWARD",
        detail = "Claimed Day $newStreak bonus (+$bonus virtual coins)",
        pointsDelta = bonus,
        createdAtMillis = now,
      )
    )
    return Result.success(bonus)
  }

  suspend fun recordGameRound(
    userId: String,
    gameId: MiniGameId,
    entryCoins: Long,
    rewardCoins: Long,
    outcome: String,
    summary: String,
    isWheelSpin: Boolean = false,
  ): Result<Long> {
    val user = db.userDao().getUserById(userId) ?: return Result.failure(IllegalStateException("Account missing"))
    if (user.accountStatus == "DISABLED") {
      return Result.failure(IllegalStateException("Account is disabled by administrator."))
    }
    if (entryCoins > user.coinBalance) {
      return Result.failure(IllegalStateException("Insufficient virtual coins for this round."))
    }
    val now = System.currentTimeMillis()
    val today = todayUtc()
    val spinsToday = if (isWheelSpin) {
      if (user.lastWheelSpinDay == today) user.dailyWheelSpinsUsed + 1 else 1
    } else {
      if (user.lastWheelSpinDay == today) user.dailyWheelSpinsUsed else 0
    }
    val net = rewardCoins - entryCoins
    val newBalance = (user.coinBalance + net).coerceAtLeast(0L)
    val newGames = user.totalGamesPlayed + 1L
    val newRewards = user.totalRewardsEarned + rewardCoins

    val updated = user.copy(
      coinBalance = newBalance,
      totalGamesPlayed = newGames,
      totalRewardsEarned = newRewards,
      lastWheelSpinDay = if (isWheelSpin) today else user.lastWheelSpinDay,
      dailyWheelSpinsUsed = spinsToday,
      updatedAtMillis = now,
    )
    db.userDao().upsertUser(updated)
    db.gameHistoryDao().insert(
      DemoGameHistoryEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        gameId = gameId.id,
        gameName = gameId.title,
        outcome = outcome,
        entryCoins = entryCoins,
        rewardCoins = rewardCoins,
        summary = summary,
        createdAtMillis = now,
      )
    )
    val txType = when {
      isWheelSpin -> "WHEEL_SPIN"
      net >= 0 -> "GAME_WIN"
      else -> "GAME_ENTRY"
    }
    db.transactionDao().insert(
      DemoTransactionEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        type = txType,
        title = "${gameId.title}: $summary".take(80),
        amount = net,
        balanceAfter = newBalance,
        gameId = gameId.id,
        createdAtMillis = now,
      )
    )
    db.leaderboardDao().upsert(
      DemoLeaderboardEntity(
        userId = userId,
        username = updated.username,
        avatarKey = updated.avatarKey,
        weeklyPoints = newRewards,
        monthlyPoints = newRewards,
        allTimePoints = newRewards,
        totalGamesPlayed = newGames,
        entryType = "PLAYER",
      )
    )
    db.activityLogDao().insert(
      DemoActivityLogEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        username = updated.username,
        action = gameId.id.uppercase(Locale.US),
        detail = "${gameId.title} • $summary",
        pointsDelta = net,
        createdAtMillis = now,
      )
    )
    if (newGames == 1L || newGames == 10L) {
      db.notificationDao().insert(
        DemoNotificationEntity(
          id = UUID.randomUUID().toString(),
          userId = userId,
          category = "ACHIEVEMENT",
          title = "Milestone Unlocked: $newGames Games Played!",
          body = "Keep enjoying M9999 Demo Play mini-games.",
          readStatus = "UNREAD",
          createdAtMillis = now,
        )
      )
    }
    return Result.success(newBalance)
  }

  suspend fun markNotificationRead(userId: String, notifId: String) {
    db.notificationDao().markRead(userId, notifId)
  }

  suspend fun markAllNotificationsRead(userId: String) {
    db.notificationDao().markAllRead(userId)
  }

  suspend fun adminToggleAccountStatus(targetUserId: String, newStatus: String) {
    val user = db.userDao().getUserById(targetUserId) ?: return
    db.userDao().upsertUser(user.copy(accountStatus = newStatus, updatedAtMillis = System.currentTimeMillis()))
  }

  suspend fun adminGrantCoins(targetUserId: String, bonusCoins: Long) {
    val user = db.userDao().getUserById(targetUserId) ?: return
    val now = System.currentTimeMillis()
    val newBal = (user.coinBalance + bonusCoins).coerceAtLeast(0L)
    db.userDao().upsertUser(user.copy(coinBalance = newBal, updatedAtMillis = now))
    db.transactionDao().insert(
      DemoTransactionEntity(
        id = UUID.randomUUID().toString(),
        userId = targetUserId,
        type = "ADMIN_ADJUST",
        title = "Admin Virtual Bonus Adjustment",
        amount = bonusCoins,
        balanceAfter = newBal,
        gameId = null,
        createdAtMillis = now,
      )
    )
  }

  suspend fun adminUpdateConfig(config: PlatformConfig) {
    db.configDao().upsert(
      DemoConfigEntity(
        id = "global",
        dailyBaseBonus = config.dailyBaseBonus.coerceIn(50, 10000),
        dailyStreakIncrement = config.dailyStreakIncrement.coerceIn(0, 2000),
        maxDailyWheelSpins = config.maxDailyWheelSpins.coerceIn(1, 50),
        disabledGamesCsv = config.disabledGames.joinToString(","),
        maintenanceBanner = config.maintenanceBanner,
      )
    )
  }

  suspend fun adminPublishAnnouncement(title: String, message: String, badge: String) {
    db.announcementDao().upsert(
      DemoAnnouncementEntity(
        id = UUID.randomUUID().toString(),
        title = title.trim().take(100),
        message = message.trim().take(500),
        badge = badge.trim().take(30).ifBlank { "UPDATE" },
        authorEmail = "local-admin@m9999demo.local",
        createdAtMillis = System.currentTimeMillis(),
      )
    )
  }

  suspend fun adminDeleteAnnouncement(id: String) {
    db.announcementDao().deleteById(id)
  }
}
