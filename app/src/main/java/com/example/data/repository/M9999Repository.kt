package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreRepository
import com.example.data.local.AppUserSettings
import com.example.data.local.DemoDatabase
import com.example.data.local.DemoSessionPreferences
import com.example.data.local.LocalDemoRepository
import com.example.data.model.ActivityAuditItem
import com.example.data.model.AnnouncementItem
import com.example.data.model.DataMode
import com.example.data.model.GameLog
import com.example.data.model.LeaderboardRow
import com.example.data.model.MiniGameId
import com.example.data.model.NotificationItem
import com.example.data.model.PlatformConfig
import com.example.data.model.PlayerAccount
import com.example.data.model.WalletTx
import kotlinx.coroutines.flow.Flow

/**
 * Coordinates strictly separated Cloud Firestore storage and Local Demo Room storage.
 */
class M9999Repository(context: Context) {
  private val appContext = context.applicationContext
  val prefs = DemoSessionPreferences(appContext)
  val localDemo = LocalDemoRepository(DemoDatabase.getInstance(appContext), prefs)
  val cloud = FirestoreRepository(appContext)

  val settingsFlow: Flow<AppUserSettings> = prefs.settingsFlow

  fun observeProfile(mode: DataMode, uid: String): Flow<PlayerAccount?> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeProfile(uid)
    DataMode.LOCAL_DEMO -> localDemo.observeUser(uid)
  }

  fun observeTransactions(mode: DataMode, uid: String): Flow<List<WalletTx>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeTransactions(uid)
    DataMode.LOCAL_DEMO -> localDemo.observeTransactions(uid)
  }

  fun observeGameHistory(mode: DataMode, uid: String): Flow<List<GameLog>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeGameHistory(uid)
    DataMode.LOCAL_DEMO -> localDemo.observeGameHistory(uid)
  }

  fun observeNotifications(mode: DataMode, uid: String): Flow<List<NotificationItem>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeNotifications(uid)
    DataMode.LOCAL_DEMO -> localDemo.observeNotifications(uid)
  }

  fun observeLeaderboard(mode: DataMode): Flow<List<LeaderboardRow>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeLeaderboard()
    DataMode.LOCAL_DEMO -> localDemo.observeLeaderboard()
  }

  fun observeAnnouncements(mode: DataMode): Flow<List<AnnouncementItem>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeAnnouncements()
    DataMode.LOCAL_DEMO -> localDemo.observeAnnouncements()
  }

  fun observeConfig(mode: DataMode): Flow<PlatformConfig> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeConfig()
    DataMode.LOCAL_DEMO -> localDemo.observeConfig()
  }

  fun observeAllUsersAdmin(mode: DataMode): Flow<List<PlayerAccount>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeAllUsersAdmin()
    DataMode.LOCAL_DEMO -> localDemo.observeAllUsersAdmin()
  }

  fun observeActivityLogs(mode: DataMode, isAdmin: Boolean): Flow<List<ActivityAuditItem>> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.observeActivityLogs(isAdmin)
    DataMode.LOCAL_DEMO -> localDemo.observeActivityLogs()
  }

  suspend fun updateProfile(
    mode: DataMode,
    uid: String,
    fullName: String,
    username: String,
    avatarKey: String,
  ): Result<Unit> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.updateProfile(fullName, username, avatarKey)
    DataMode.LOCAL_DEMO -> localDemo.updateProfile(uid, fullName, username, avatarKey)
  }

  suspend fun claimDailyReward(
    mode: DataMode,
    account: PlayerAccount,
    config: PlatformConfig,
  ): Result<Long> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.claimDailyReward(account, config)
    DataMode.LOCAL_DEMO -> localDemo.claimDailyReward(account.userId, config)
  }

  suspend fun recordGameRound(
    mode: DataMode,
    account: PlayerAccount,
    gameId: MiniGameId,
    entryCoins: Long,
    rewardCoins: Long,
    outcome: String,
    summary: String,
    isWheelSpin: Boolean = false,
  ): Result<Long> = when (mode) {
    DataMode.FIREBASE_CLOUD -> cloud.recordGameRound(account, gameId, entryCoins, rewardCoins, outcome, summary, isWheelSpin)
    DataMode.LOCAL_DEMO -> localDemo.recordGameRound(account.userId, gameId, entryCoins, rewardCoins, outcome, summary, isWheelSpin)
  }

  suspend fun markNotificationRead(mode: DataMode, uid: String, notifId: String) {
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.markNotificationRead(notifId)
      DataMode.LOCAL_DEMO -> localDemo.markNotificationRead(uid, notifId)
    }
  }

  suspend fun markAllNotificationsRead(mode: DataMode, uid: String, unreadIds: List<String>) {
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.markAllNotificationsRead(unreadIds)
      DataMode.LOCAL_DEMO -> localDemo.markAllNotificationsRead(uid)
    }
  }

  suspend fun adminToggleAccountStatus(mode: DataMode, targetUserId: String, newStatus: String): Result<Unit> =
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.adminToggleAccountStatus(targetUserId, newStatus)
      DataMode.LOCAL_DEMO -> {
        localDemo.adminToggleAccountStatus(targetUserId, newStatus)
        Result.success(Unit)
      }
    }

  suspend fun adminGrantCoins(mode: DataMode, targetUser: PlayerAccount, bonusCoins: Long): Result<Unit> =
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.adminGrantCoins(targetUser, bonusCoins)
      DataMode.LOCAL_DEMO -> {
        localDemo.adminGrantCoins(targetUser.userId, bonusCoins)
        Result.success(Unit)
      }
    }

  suspend fun adminUpdateConfig(mode: DataMode, config: PlatformConfig): Result<Unit> =
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.adminUpdateConfig(config)
      DataMode.LOCAL_DEMO -> {
        localDemo.adminUpdateConfig(config)
        Result.success(Unit)
      }
    }

  suspend fun adminPublishAnnouncement(mode: DataMode, title: String, message: String, badge: String): Result<Unit> =
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.adminPublishAnnouncement(title, message, badge)
      DataMode.LOCAL_DEMO -> {
        localDemo.adminPublishAnnouncement(title, message, badge)
        Result.success(Unit)
      }
    }

  suspend fun adminDeleteAnnouncement(mode: DataMode, id: String): Result<Unit> =
    when (mode) {
      DataMode.FIREBASE_CLOUD -> cloud.adminDeleteAnnouncement(id)
      DataMode.LOCAL_DEMO -> {
        localDemo.adminDeleteAnnouncement(id)
        Result.success(Unit)
      }
    }
}
