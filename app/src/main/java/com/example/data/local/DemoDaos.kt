package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DemoUserDao {
  @Query("SELECT * FROM demo_users WHERE userId = :userId LIMIT 1")
  fun observeUser(userId: String): Flow<DemoUserEntity?>

  @Query("SELECT * FROM demo_users WHERE userId = :userId LIMIT 1")
  suspend fun getUserById(userId: String): DemoUserEntity?

  @Query("SELECT * FROM demo_users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
  suspend fun getUserByEmail(email: String): DemoUserEntity?

  @Query("SELECT * FROM demo_users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
  suspend fun getUserByUsername(username: String): DemoUserEntity?

  @Query("SELECT * FROM demo_users ORDER BY createdAtMillis DESC")
  fun observeAllUsers(): Flow<List<DemoUserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertUser(user: DemoUserEntity)
}

@Dao
interface DemoTransactionDao {
  @Query("SELECT * FROM demo_transactions WHERE userId = :userId ORDER BY createdAtMillis DESC LIMIT 100")
  fun observeTransactions(userId: String): Flow<List<DemoTransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(tx: DemoTransactionEntity)
}

@Dao
interface DemoGameHistoryDao {
  @Query("SELECT * FROM demo_game_history WHERE userId = :userId ORDER BY createdAtMillis DESC LIMIT 100")
  fun observeGameHistory(userId: String): Flow<List<DemoGameHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: DemoGameHistoryEntity)
}

@Dao
interface DemoNotificationDao {
  @Query("SELECT * FROM demo_notifications WHERE userId = :userId ORDER BY createdAtMillis DESC LIMIT 100")
  fun observeNotifications(userId: String): Flow<List<DemoNotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: DemoNotificationEntity)

  @Query("UPDATE demo_notifications SET readStatus = 'READ' WHERE id = :notifId AND userId = :userId")
  suspend fun markRead(userId: String, notifId: String)

  @Query("UPDATE demo_notifications SET readStatus = 'READ' WHERE userId = :userId")
  suspend fun markAllRead(userId: String)
}

@Dao
interface DemoLeaderboardDao {
  @Query("SELECT * FROM demo_leaderboard")
  fun observeLeaderboard(): Flow<List<DemoLeaderboardEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entry: DemoLeaderboardEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertIfAbsent(entries: List<DemoLeaderboardEntity>)
}

@Dao
interface DemoAnnouncementDao {
  @Query("SELECT * FROM demo_announcements ORDER BY createdAtMillis DESC")
  fun observeAnnouncements(): Flow<List<DemoAnnouncementEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(item: DemoAnnouncementEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertIfAbsent(items: List<DemoAnnouncementEntity>)

  @Query("DELETE FROM demo_announcements WHERE id = :id")
  suspend fun deleteById(id: String)
}

@Dao
interface DemoConfigDao {
  @Query("SELECT * FROM demo_config WHERE id = 'global' LIMIT 1")
  fun observeConfig(): Flow<DemoConfigEntity?>

  @Query("SELECT * FROM demo_config WHERE id = 'global' LIMIT 1")
  suspend fun getConfig(): DemoConfigEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(config: DemoConfigEntity)
}

@Dao
interface DemoActivityLogDao {
  @Query("SELECT * FROM demo_activity_logs ORDER BY createdAtMillis DESC LIMIT 150")
  fun observeAllLogs(): Flow<List<DemoActivityLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(log: DemoActivityLogEntity)
}
