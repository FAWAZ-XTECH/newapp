package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DemoDatabase
import com.example.data.local.DemoSessionPreferences
import com.example.data.local.LocalDemoRepository
import com.example.data.local.PasswordHasher
import com.example.data.model.MiniGameId
import com.example.data.model.PlatformConfig
import com.example.data.repository.SecureRng
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: DemoDatabase
  private lateinit var repo: LocalDemoRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, DemoDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repo = LocalDemoRepository(db, DemoSessionPreferences(context))
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app name matches M9999 Demo Play`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("M9999 Demo Play", appName)
  }

  @Test
  fun `password hasher creates salted hash and verifies securely`() {
    val hash1 = PasswordHasher.hashPassword("RoyalPass99")
    val hash2 = PasswordHasher.hashPassword("RoyalPass99")
    assertFalse(hash1 == hash2)
    assertTrue(PasswordHasher.verifyPassword("RoyalPass99", hash1))
    assertFalse(PasswordHasher.verifyPassword("WrongPass", hash1))
  }

  @Test
  fun `secure rng respects bounds`() {
    repeat(100) {
      val v = SecureRng.nextIntRange(1, 10)
      assertTrue(v in 1..10)
    }
  }

  @Test
  fun `demo signup prevents duplicate email and updates wallet on claim and round`() = runBlocking {
    val created = repo.signUpDemo(
      fullName = "Imran Demo",
      email = "imran@example.com",
      username = "ImranRoyal",
      password = "password123",
    )
    assertTrue(created.isSuccess)
    val user = created.getOrThrow()
    assertEquals(1500L, user.coinBalance)

    val dupEmail = repo.signUpDemo(
      fullName = "Imran Clone",
      email = "IMRAN@example.com",
      username = "ImranOther",
      password = "password123",
    )
    assertTrue(dupEmail.isFailure)

    val config = PlatformConfig(dailyBaseBonus = 250, dailyStreakIncrement = 50)
    val claim1 = repo.claimDailyReward(user.userId, config)
    assertTrue(claim1.isSuccess)
    assertEquals(250L, claim1.getOrThrow())

    // Same day duplicate claim must fail
    val claim2 = repo.claimDailyReward(user.userId, config)
    assertTrue(claim2.isFailure)

    // Record a winning game round
    val roundRes = repo.recordGameRound(
      userId = user.userId,
      gameId = MiniGameId.DICE_GAME,
      entryCoins = 50L,
      rewardCoins = 200L,
      outcome = "WIN",
      summary = "Rolled Lucky 7",
    )
    assertTrue(roundRes.isSuccess)
    assertEquals(1500L + 250L - 50L + 200L, roundRes.getOrThrow())
  }
}
