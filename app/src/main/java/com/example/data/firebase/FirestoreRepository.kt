package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.ActivityAuditItem
import com.example.data.model.ActivityLogDto
import com.example.data.model.AnnouncementDto
import com.example.data.model.AnnouncementItem
import com.example.data.model.AppConfigDto
import com.example.data.model.AppNotificationDto
import com.example.data.model.GameHistoryDto
import com.example.data.model.GameLog
import com.example.data.model.LeaderboardEntryDto
import com.example.data.model.LeaderboardRow
import com.example.data.model.MiniGameId
import com.example.data.model.NotificationItem
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.data.model.UserProfileDto
import com.example.data.model.WalletTransactionDto
import com.example.data.model.WalletTx
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FirestoreRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun todayUtc(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(Date())
  }

  suspend fun checkIsAdmin(userId: String): Boolean {
    val user = auth.currentUser ?: return false
    if (user.email == BOOTSTRAP_ADMIN_EMAIL && user.isEmailVerified) return true
    return try {
      val doc = db.collection("admins").document(userId).get().await()
      doc.exists()
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      false
    }
  }

  suspend fun ensureUserInitialized(
    uid: String,
    displayName: String?,
    email: String?,
  ): Result<PlayerAccount> {
    val userPath = "users/$uid"
    return try {
      val currentUid = requireUserId()
      check(currentUid == uid) { "Signed-in user mismatch." }
      val userRef = db.collection("users").document(uid)
      val snap = userRef.get().await()
      if (snap.exists()) {
        val existing = snap.toObject(UserProfileDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: error("Unable to deserialize existing user profile.")
        ensurePublicLeaderboardExists(existing.toDomain())
        return Result.success(existing.toDomain())
      }

      val cleanEmail = (email ?: "").take(120)
      val cleanFull = (displayName?.takeIf { it.isNotBlank() } ?: cleanEmail.substringBefore("@").ifBlank { "M9999 Player" }).take(60)
      val baseHandle = cleanFull.replace(Regex("[^a-zA-Z0-9_]"), "").take(18).ifBlank { "Player" }
      val handle = if (baseHandle.length < 3) "${baseHandle}999" else baseHandle

      val payload = mapOf(
        "userId" to uid,
        "username" to handle,
        "fullName" to cleanFull,
        "email" to cleanEmail,
        "avatarKey" to "royal",
        "coinBalance" to 1000L,
        "dailyRewardPoints" to 0L,
        "totalGamesPlayed" to 0L,
        "totalRewardsEarned" to 1000L,
        "loginStreak" to 0L,
        "lastDailyClaimDay" to "",
        "lastWheelSpinDay" to "",
        "dailyWheelSpinsUsed" to 0L,
        "accountStatus" to "ACTIVE",
        "role" to "PLAYER",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      userRef.set(payload).await()

      // Record welcome bonus transaction & welcome notification
      val txPayload = mapOf(
        "userId" to uid,
        "type" to "WELCOME_BONUS",
        "title" to "M9999 Welcome Virtual Coins",
        "amount" to 1000L,
        "balanceAfter" to 1000L,
        "createdAt" to FieldValue.serverTimestamp(),
      )
      userRef.collection("transactions").document().set(txPayload).await()

      val notifPayload = mapOf(
        "userId" to uid,
        "category" to "SYSTEM",
        "title" to "Welcome to M9999 Demo Play!",
        "body" to "You received 1,000 free virtual demo coins. Explore 6 mini-games and daily streak drops.",
        "readStatus" to "UNREAD",
        "createdAt" to FieldValue.serverTimestamp(),
      )
      userRef.collection("notifications").document().set(notifPayload).await()

      val lbPayload = mapOf(
        "userId" to uid,
        "username" to handle,
        "avatarKey" to "royal",
        "weeklyPoints" to 1000L,
        "monthlyPoints" to 1000L,
        "allTimePoints" to 1000L,
        "totalGamesPlayed" to 0L,
        "entryType" to "PLAYER",
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      db.collection("leaderboard").document(uid).set(lbPayload).await()

      val createdSnap = userRef.get().await()
      val created = createdSnap.toObject(UserProfileDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.toDomain()
        ?: PlayerAccount(
          userId = uid,
          username = handle,
          fullName = cleanFull,
          email = cleanEmail,
          avatarKey = "royal",
          coinBalance = 1000L,
          dailyRewardPoints = 0L,
          totalGamesPlayed = 0L,
          totalRewardsEarned = 1000L,
          loginStreak = 0,
          lastDailyClaimDay = "",
          lastWheelSpinDay = "",
          dailyWheelSpinsUsed = 0,
          accountStatus = "ACTIVE",
          role = "PLAYER",
          createdAtMillis = System.currentTimeMillis(),
          updatedAtMillis = System.currentTimeMillis(),
        )
      Result.success(created)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, userPath)
      Result.failure(e)
    }
  }

  private suspend fun ensurePublicLeaderboardExists(account: PlayerAccount) {
    try {
      val lbRef = db.collection("leaderboard").document(account.userId)
      if (!lbRef.get().await().exists()) {
        lbRef.set(
          mapOf(
            "userId" to account.userId,
            "username" to account.username,
            "avatarKey" to account.avatarKey,
            "weeklyPoints" to account.totalRewardsEarned.coerceAtLeast(account.coinBalance),
            "monthlyPoints" to account.totalRewardsEarned.coerceAtLeast(account.coinBalance),
            "allTimePoints" to account.totalRewardsEarned.coerceAtLeast(account.coinBalance),
            "totalGamesPlayed" to account.totalGamesPlayed,
            "entryType" to "PLAYER",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      // Non-fatal leaderboard sync
    }
  }

  fun observeProfile(userId: String): Flow<PlayerAccount?> = flow {
    val uid = requireUserId()
    val path = "users/$uid"
    emitAll(
      db.collection("users").document(uid)
        .snapshots()
        .map { snap ->
          snap.toObject(UserProfileDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDomain()
        }
        .catch { error ->
          logFlowError(error, OperationType.GET, path, uid)
          throw error
        }
    )
  }

  fun observeTransactions(userId: String): Flow<List<WalletTx>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/transactions"
    emitAll(
      db.collection("users").document(uid).collection("transactions")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(60)
        .snapshots()
        .map { snap ->
          snap.toObjects(WalletTransactionDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeGameHistory(userId: String): Flow<List<GameLog>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/gameHistory"
    emitAll(
      db.collection("users").document(uid).collection("gameHistory")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(80)
        .snapshots()
        .map { snap ->
          snap.toObjects(GameHistoryDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeNotifications(userId: String): Flow<List<NotificationItem>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/notifications"
    emitAll(
      db.collection("users").document(uid).collection("notifications")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(60)
        .snapshots()
        .map { snap ->
          snap.toObjects(AppNotificationDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeLeaderboard(): Flow<List<LeaderboardRow>> = flow {
    val uid = requireUserId()
    val path = "leaderboard"
    emitAll(
      db.collection("leaderboard")
        .limit(100)
        .snapshots()
        .map { snap ->
          snap.toObjects(LeaderboardEntryDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeAnnouncements(): Flow<List<AnnouncementItem>> = flow {
    val uid = requireUserId()
    val path = "announcements"
    emitAll(
      db.collection("announcements")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(30)
        .snapshots()
        .map { snap ->
          snap.toObjects(AnnouncementDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeConfig(): Flow<PlatformConfig> = flow {
    val uid = requireUserId()
    val path = "appConfig/global"
    emitAll(
      db.collection("appConfig").document("global")
        .snapshots()
        .map { snap ->
          snap.toObject(AppConfigDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDomain()
            ?: PlatformConfig()
        }
        .catch { error ->
          logFlowError(error, OperationType.GET, path, uid)
          throw error
        }
    )
  }

  fun observeAllUsersAdmin(): Flow<List<PlayerAccount>> = flow {
    val uid = requireUserId()
    val path = "users"
    emitAll(
      db.collection("users")
        .limit(100)
        .snapshots()
        .map { snap ->
          snap.toObjects(UserProfileDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeActivityLogs(isAdmin: Boolean): Flow<List<ActivityAuditItem>> = flow {
    val uid = requireUserId()
    val path = "activityLogs"
    val query = if (isAdmin) {
      db.collection("activityLogs").orderBy("createdAt", Query.Direction.DESCENDING).limit(100)
    } else {
      db.collection("activityLogs").whereEqualTo("userId", uid).limit(60)
    }
    emitAll(
      query.snapshots()
        .map { snap ->
          snap.toObjects(ActivityLogDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .map { it.toDomain() }
            .sortedByDescending { it.createdAtMillis }
        }
        .catch { error ->
          logFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  suspend fun updateProfile(fullName: String, username: String, avatarKey: String): Result<Unit> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "users/$uid"
    return try {
      val authedUid = requireUserId()
      val cleanName = fullName.trim()
      val cleanUser = username.trim()
      require(cleanName.length in 1..60) { "Full name must be 1 to 60 characters." }
      require(cleanUser.length in 3..30) { "Username must be 3 to 30 characters." }

      db.collection("users").document(authedUid).update(
        mapOf(
          "fullName" to cleanName,
          "username" to cleanUser,
          "avatarKey" to avatarKey,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      db.collection("leaderboard").document(authedUid).update(
        mapOf(
          "username" to cleanUser,
          "avatarKey" to avatarKey,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun claimDailyReward(account: PlayerAccount, config: PlatformConfig): Result<Long> {
    val path = "users/${account.userId}"
    return try {
      val uid = requireUserId()
      check(uid == account.userId)
      if (account.accountStatus == "DISABLED") {
        return Result.failure(IllegalStateException("This account is disabled by an administrator."))
      }
      val today = todayUtc()
      if (account.lastDailyClaimDay == today) {
        return Result.failure(IllegalStateException("Daily bonus already claimed for today."))
      }

      val newStreak = (account.loginStreak + 1).coerceAtMost(3650)
      val bonus = (config.dailyBaseBonus + (newStreak - 1) * config.dailyStreakIncrement).toLong()
      val newBalance = account.coinBalance + bonus
      val newDailyPoints = account.dailyRewardPoints + bonus
      val newTotalRewards = account.totalRewardsEarned + bonus

      val userRef = db.collection("users").document(uid)
      val txRef = userRef.collection("transactions").document()
      val notifRef = userRef.collection("notifications").document()
      val logRef = db.collection("activityLogs").document()
      val lbRef = db.collection("leaderboard").document(uid)

      db.runBatch { batch ->
        batch.update(
          userRef,
          mapOf(
            "coinBalance" to newBalance,
            "dailyRewardPoints" to newDailyPoints,
            "totalRewardsEarned" to newTotalRewards,
            "loginStreak" to newStreak.toLong(),
            "lastDailyClaimDay" to today,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          txRef,
          mapOf(
            "userId" to uid,
            "type" to "DAILY_BONUS",
            "title" to "Day $newStreak Daily Login Bonus",
            "amount" to bonus,
            "balanceAfter" to newBalance,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          notifRef,
          mapOf(
            "userId" to uid,
            "category" to "DAILY_REWARD",
            "title" to "Daily Bonus Claimed (+$bonus Coins)",
            "body" to "Day $newStreak streak unlocked! Keep playing M9999 Demo Play daily.",
            "readStatus" to "UNREAD",
            "createdAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          logRef,
          mapOf(
            "userId" to uid,
            "username" to account.username,
            "action" to "DAILY_REWARD",
            "detail" to "Claimed Day $newStreak bonus (+$bonus virtual coins)",
            "pointsDelta" to bonus,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          lbRef,
          mapOf(
            "userId" to uid,
            "username" to account.username,
            "avatarKey" to account.avatarKey,
            "weeklyPoints" to newTotalRewards,
            "monthlyPoints" to newTotalRewards,
            "allTimePoints" to newTotalRewards,
            "totalGamesPlayed" to account.totalGamesPlayed,
            "entryType" to "PLAYER",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
      }.await()

      Result.success(bonus)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun recordGameRound(
    account: PlayerAccount,
    gameId: MiniGameId,
    entryCoins: Long,
    rewardCoins: Long,
    outcome: String,
    summary: String,
    isWheelSpin: Boolean = false,
  ): Result<Long> {
    val path = "users/${account.userId}"
    return try {
      val uid = requireUserId()
      check(uid == account.userId)
      if (account.accountStatus == "DISABLED") {
        return Result.failure(IllegalStateException("This account is disabled by an administrator."))
      }
      if (entryCoins > account.coinBalance) {
        return Result.failure(IllegalStateException("Insufficient virtual coins for this round."))
      }

      val today = todayUtc()
      val wheelSpinsToday = if (isWheelSpin) {
        if (account.lastWheelSpinDay == today) account.dailyWheelSpinsUsed + 1 else 1
      } else {
        if (account.lastWheelSpinDay == today) account.dailyWheelSpinsUsed else 0
      }

      val netDelta = rewardCoins - entryCoins
      val newBalance = (account.coinBalance + netDelta).coerceAtLeast(0L)
      val newGames = account.totalGamesPlayed + 1L
      val newTotalRewards = account.totalRewardsEarned + rewardCoins

      val userRef = db.collection("users").document(uid)
      val historyRef = userRef.collection("gameHistory").document()
      val txRef = userRef.collection("transactions").document()
      val logRef = db.collection("activityLogs").document()
      val lbRef = db.collection("leaderboard").document(uid)

      db.runBatch { batch ->
        batch.update(
          userRef,
          mapOf(
            "coinBalance" to newBalance,
            "totalGamesPlayed" to newGames,
            "totalRewardsEarned" to newTotalRewards,
            "lastWheelSpinDay" to if (isWheelSpin) today else account.lastWheelSpinDay,
            "dailyWheelSpinsUsed" to wheelSpinsToday.toLong(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          historyRef,
          mapOf(
            "userId" to uid,
            "gameId" to gameId.id,
            "gameName" to gameId.title,
            "outcome" to outcome,
            "entryCoins" to entryCoins,
            "rewardCoins" to rewardCoins,
            "summary" to summary.take(120),
            "createdAt" to FieldValue.serverTimestamp(),
          )
        )
        val txType = when {
          isWheelSpin -> "WHEEL_SPIN"
          netDelta >= 0 -> "GAME_WIN"
          else -> "GAME_ENTRY"
        }
        batch.set(
          txRef,
          mapOf(
            "userId" to uid,
            "type" to txType,
            "title" to "${gameId.title}: $summary".take(80),
            "amount" to netDelta,
            "balanceAfter" to newBalance,
            "gameId" to gameId.id,
            "createdAt" to FieldValue.serverTimestamp(),
          ).filterValues { it != null }
        )
        batch.set(
          logRef,
          mapOf(
            "userId" to uid,
            "username" to account.username,
            "action" to gameId.id.uppercase(Locale.US),
            "detail" to "${gameId.title} • $summary".take(160),
            "pointsDelta" to netDelta,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          lbRef,
          mapOf(
            "userId" to uid,
            "username" to account.username,
            "avatarKey" to account.avatarKey,
            "weeklyPoints" to newTotalRewards,
            "monthlyPoints" to newTotalRewards,
            "allTimePoints" to newTotalRewards,
            "totalGamesPlayed" to newGames,
            "entryType" to "PLAYER",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
      }.await()

      Result.success(newBalance)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun markNotificationRead(notifId: String): Result<Unit> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "users/$uid/notifications/$notifId"
    return try {
      val authedUid = requireUserId()
      db.collection("users").document(authedUid)
        .collection("notifications").document(notifId)
        .update("readStatus", "READ")
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun markAllNotificationsRead(unreadIds: List<String>): Result<Unit> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "users/$uid/notifications"
    return try {
      val authedUid = requireUserId()
      if (unreadIds.isEmpty()) return Result.success(Unit)
      val col = db.collection("users").document(authedUid).collection("notifications")
      db.runBatch { batch ->
        unreadIds.forEach { id ->
          batch.update(col.document(id), "readStatus", "READ")
        }
      }.await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // Admin Operations (protected by firestore.rules isAdmin())

  suspend fun adminToggleAccountStatus(targetUserId: String, newStatus: String): Result<Unit> {
    val path = "users/$targetUserId"
    return try {
      requireUserId()
      db.collection("users").document(targetUserId).update(
        mapOf(
          "accountStatus" to newStatus,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun adminGrantCoins(targetUser: PlayerAccount, bonusCoins: Long): Result<Unit> {
    val path = "users/${targetUser.userId}"
    return try {
      requireUserId()
      val newBal = (targetUser.coinBalance + bonusCoins).coerceAtLeast(0L)
      val userRef = db.collection("users").document(targetUser.userId)
      val txRef = userRef.collection("transactions").document()
      db.runBatch { batch ->
        batch.update(
          userRef,
          mapOf(
            "coinBalance" to newBal,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          txRef,
          mapOf(
            "userId" to targetUser.userId,
            "type" to "ADMIN_ADJUST",
            "title" to "Admin Virtual Bonus Adjustment",
            "amount" to bonusCoins,
            "balanceAfter" to newBal,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        )
      }.await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun adminUpdateConfig(config: PlatformConfig): Result<Unit> {
    val path = "appConfig/global"
    return try {
      requireUserId()
      db.collection("appConfig").document("global").set(
        mapOf(
          "dailyBaseBonus" to config.dailyBaseBonus.coerceIn(50, 10000).toLong(),
          "dailyStreakIncrement" to config.dailyStreakIncrement.coerceIn(0, 2000).toLong(),
          "maxDailyWheelSpins" to config.maxDailyWheelSpins.coerceIn(1, 50).toLong(),
          "disabledGames" to config.disabledGames,
          "maintenanceBanner" to config.maintenanceBanner.take(200),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun adminPublishAnnouncement(title: String, message: String, badge: String): Result<Unit> {
    val path = "announcements"
    return try {
      requireUserId()
      val email = auth.currentUser?.email ?: "admin@m9999demo.app"
      db.collection("announcements").document().set(
        mapOf(
          "title" to title.trim().take(100),
          "message" to message.trim().take(500),
          "badge" to badge.trim().take(30).ifBlank { "UPDATE" },
          "authorEmail" to email.take(120),
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun adminDeleteAnnouncement(announcementId: String): Result<Unit> {
    val path = "announcements/$announcementId"
    return try {
      requireUserId()
      db.collection("announcements").document(announcementId).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  suspend fun adminSeedSampleLeaderboard(): Result<Unit> {
    val path = "leaderboard"
    return try {
      requireUserId()
      val samples = listOf(
        Triple("sample_gold_king", "GoldKing_Demo", 14800L),
        Triple("sample_royal_ace", "RoyalAce_Demo", 11450L),
        Triple("sample_obsidian_7", "Obsidian7_Demo", 9200L),
        Triple("sample_luna_spin", "LunaSpin_Demo", 6750L),
      )
      val avatars = listOf("crown", "ace", "diamond", "phoenix")
      db.runBatch { batch ->
        samples.forEachIndexed { idx, (id, name, pts) ->
          batch.set(
            db.collection("leaderboard").document(id),
            mapOf(
              "userId" to id,
              "username" to name,
              "avatarKey" to avatars[idx],
              "weeklyPoints" to (pts / 4),
              "monthlyPoints" to (pts / 2),
              "allTimePoints" to pts,
              "totalGamesPlayed" to (35L - idx * 5L),
              "entryType" to "SAMPLE_DEMO",
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
        }
      }.await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  private fun logFlowError(error: Throwable, op: OperationType, path: String, uid: String) {
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
      handleFirestoreError(exceptionToLog, op, path)
    }
  }

  companion object {
    const val BOOTSTRAP_ADMIN_EMAIL = "imranabdulshakoor00@gmail.com"
  }
}
