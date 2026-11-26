package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    DemoUserEntity::class,
    DemoTransactionEntity::class,
    DemoGameHistoryEntity::class,
    DemoNotificationEntity::class,
    DemoLeaderboardEntity::class,
    DemoAnnouncementEntity::class,
    DemoConfigEntity::class,
    DemoActivityLogEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class DemoDatabase : RoomDatabase() {
  abstract fun userDao(): DemoUserDao
  abstract fun transactionDao(): DemoTransactionDao
  abstract fun gameHistoryDao(): DemoGameHistoryDao
  abstract fun notificationDao(): DemoNotificationDao
  abstract fun leaderboardDao(): DemoLeaderboardDao
  abstract fun announcementDao(): DemoAnnouncementDao
  abstract fun configDao(): DemoConfigDao
  abstract fun activityLogDao(): DemoActivityLogDao

  companion object {
    @Volatile
    private var instance: DemoDatabase? = null

    fun getInstance(context: Context): DemoDatabase {
      return instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          DemoDatabase::class.java,
          "m9999_demo_local.db",
        ).build().also { instance = it }
      }
    }
  }
}
