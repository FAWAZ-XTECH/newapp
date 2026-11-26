package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.demoPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "m9999_demo_prefs"
)

data class AppUserSettings(
  val soundFeedbackEnabled: Boolean = true,
  val animationsEnabled: Boolean = true,
  val rememberedDemoUserId: String? = null,
  val adminPasscodeClaimed: Boolean = false,
)

class DemoSessionPreferences(private val context: Context) {
  private val keyDemoUserId = stringPreferencesKey("demo_user_id")
  private val keyDemoAdminHash = stringPreferencesKey("demo_admin_passcode_hash")
  private val keySoundFeedback = booleanPreferencesKey("sound_feedback_enabled")
  private val keyAnimations = booleanPreferencesKey("animations_enabled")

  val settingsFlow: Flow<AppUserSettings> = context.demoPrefsDataStore.data.map { prefs ->
    AppUserSettings(
      soundFeedbackEnabled = prefs[keySoundFeedback] ?: true,
      animationsEnabled = prefs[keyAnimations] ?: true,
      rememberedDemoUserId = prefs[keyDemoUserId],
      adminPasscodeClaimed = !prefs[keyDemoAdminHash].isNullOrBlank(),
    )
  }

  suspend fun setDemoUserId(userId: String?) {
    context.demoPrefsDataStore.edit { prefs ->
      if (userId.isNullOrBlank()) {
        prefs.remove(keyDemoUserId)
      } else {
        prefs[keyDemoUserId] = userId
      }
    }
  }

  suspend fun getDemoUserId(): String? = context.demoPrefsDataStore.data.first()[keyDemoUserId]

  suspend fun claimAdminPasscode(passcode: String): Result<Unit> {
    if (passcode.length < 6) {
      return Result.failure(IllegalArgumentException("Admin passcode must be at least 6 characters."))
    }
    val existing = context.demoPrefsDataStore.data.first()[keyDemoAdminHash]
    if (!existing.isNullOrBlank()) {
      return Result.failure(IllegalStateException("Local demo admin passcode is already configured."))
    }
    val hash = PasswordHasher.hashPassword(passcode)
    context.demoPrefsDataStore.edit { it[keyDemoAdminHash] = hash }
    return Result.success(Unit)
  }

  suspend fun verifyAdminPasscode(passcode: String): Boolean {
    val stored = context.demoPrefsDataStore.data.first()[keyDemoAdminHash] ?: return false
    return PasswordHasher.verifyPassword(passcode, stored)
  }

  suspend fun setSoundFeedback(enabled: Boolean) {
    context.demoPrefsDataStore.edit { it[keySoundFeedback] = enabled }
  }

  suspend fun setAnimationsEnabled(enabled: Boolean) {
    context.demoPrefsDataStore.edit { it[keyAnimations] = enabled }
  }
}
