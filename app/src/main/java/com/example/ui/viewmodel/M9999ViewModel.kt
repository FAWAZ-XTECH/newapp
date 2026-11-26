package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppUserSettings
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
import com.example.data.repository.M9999Repository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DataState<out T> {
  data object Loading : DataState<Nothing>
  data class Success<T>(val data: T) : DataState<T>
  data class Error(val message: String) : DataState<Nothing>
}

class M9999ViewModel(
  private val repository: M9999Repository,
  val mode: DataMode,
  val currentUserId: String,
  private val initialDisplayName: String? = null,
  private val initialEmail: String? = null,
) : ViewModel() {

  private val _isAdminVerified = MutableStateFlow(false)
  val isAdminVerified: StateFlow<Boolean> = _isAdminVerified.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _errorBanner = MutableStateFlow<String?>(null)
  val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

  val settings: StateFlow<AppUserSettings> = repository.settingsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppUserSettings())

  init {
    viewModelScope.launch {
      if (mode == DataMode.FIREBASE_CLOUD) {
        repository.cloud.ensureUserInitialized(currentUserId, initialDisplayName, initialEmail)
          .onFailure { err ->
            _errorBanner.value = err.localizedMessage ?: "Failed to initialize cloud profile."
          }
        _isAdminVerified.value = repository.cloud.checkIsAdmin(currentUserId)
      } else {
        repository.localDemo.ensureSeedData()
      }
    }
  }

  val profileState: StateFlow<DataState<PlayerAccount?>> =
    repository.observeProfile(mode, currentUserId)
      .asSafeDataState("profile")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val transactionsState: StateFlow<DataState<List<WalletTx>>> =
    repository.observeTransactions(mode, currentUserId)
      .asSafeDataState("transactions")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val gameHistoryState: StateFlow<DataState<List<GameLog>>> =
    repository.observeGameHistory(mode, currentUserId)
      .asSafeDataState("gameHistory")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val notificationsState: StateFlow<DataState<List<NotificationItem>>> =
    repository.observeNotifications(mode, currentUserId)
      .asSafeDataState("notifications")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val leaderboardState: StateFlow<DataState<List<LeaderboardRow>>> =
    repository.observeLeaderboard(mode)
      .asSafeDataState("leaderboard")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val announcementsState: StateFlow<DataState<List<AnnouncementItem>>> =
    repository.observeAnnouncements(mode)
      .asSafeDataState("announcements")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val configState: StateFlow<PlatformConfig> =
    repository.observeConfig(mode)
      .catch { error ->
        unwrapOrThrowCancellation(error, "appConfig")
        emit(PlatformConfig())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PlatformConfig())

  @OptIn(ExperimentalCoroutinesApi::class)
  val adminUsersState: StateFlow<DataState<List<PlayerAccount>>> =
    _isAdminVerified.flatMapLatest { isAdmin ->
      if (!isAdmin) flowOf(DataState.Success(emptyList()))
      else repository.observeAllUsersAdmin(mode).asSafeDataState("adminUsers")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  @OptIn(ExperimentalCoroutinesApi::class)
  val activityLogsState: StateFlow<DataState<List<ActivityAuditItem>>> =
    _isAdminVerified.flatMapLatest { isAdmin ->
      repository.observeActivityLogs(mode, isAdmin).asSafeDataState("activityLogs")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  fun clearMessages() {
    _statusMessage.value = null
    _errorBanner.value = null
  }

  fun setSoundFeedback(enabled: Boolean) {
    viewModelScope.launch { repository.prefs.setSoundFeedback(enabled) }
  }

  fun setAnimationsEnabled(enabled: Boolean) {
    viewModelScope.launch { repository.prefs.setAnimationsEnabled(enabled) }
  }

  fun updateProfile(
    fullName: String,
    username: String,
    avatarKey: String,
    onResult: (Result<Unit>) -> Unit = {},
  ) {
    viewModelScope.launch {
      val result = repository.updateProfile(mode, currentUserId, fullName, username, avatarKey)
      result.onSuccess { _statusMessage.value = "Profile updated successfully." }
      result.onFailure { _errorBanner.value = it.localizedMessage ?: "Failed to update profile." }
      onResult(result)
    }
  }

  fun changeDemoPassword(currentPassword: String, newPassword: String, onResult: (Result<Unit>) -> Unit) {
    viewModelScope.launch {
      if (mode != DataMode.LOCAL_DEMO) {
        onResult(Result.failure(IllegalStateException("Google Sign-In accounts manage passwords via Google.")))
        return@launch
      }
      val result = repository.localDemo.changeDemoPassword(currentUserId, currentPassword, newPassword)
      onResult(result)
    }
  }

  fun claimDailyReward(onClaimed: (Result<Long>) -> Unit) {
    val account = (profileState.value as? DataState.Success)?.data
    if (account == null) {
      onClaimed(Result.failure(IllegalStateException("Profile is still loading.")))
      return
    }
    viewModelScope.launch {
      val result = repository.claimDailyReward(mode, account, configState.value)
      onClaimed(result)
    }
  }

  fun recordGameRound(
    gameId: MiniGameId,
    entryCoins: Long,
    rewardCoins: Long,
    outcome: String,
    summary: String,
    isWheelSpin: Boolean = false,
    onComplete: (Result<Long>) -> Unit = {},
  ) {
    val account = (profileState.value as? DataState.Success)?.data
    if (account == null) {
      onComplete(Result.failure(IllegalStateException("Account unavailable.")))
      return
    }
    viewModelScope.launch {
      val result = repository.recordGameRound(
        mode = mode,
        account = account,
        gameId = gameId,
        entryCoins = entryCoins,
        rewardCoins = rewardCoins,
        outcome = outcome,
        summary = summary,
        isWheelSpin = isWheelSpin,
      )
      result.onFailure { _errorBanner.value = it.localizedMessage ?: "Unable to save game round." }
      onComplete(result)
    }
  }

  fun markNotificationRead(notifId: String) {
    viewModelScope.launch {
      repository.markNotificationRead(mode, currentUserId, notifId)
    }
  }

  fun markAllNotificationsRead() {
    val items = (notificationsState.value as? DataState.Success)?.data.orEmpty()
    val unreadIds = items.filter { it.unread }.map { it.id }
    if (unreadIds.isEmpty()) return
    viewModelScope.launch {
      repository.markAllNotificationsRead(mode, currentUserId, unreadIds)
      _statusMessage.value = "All notifications marked as read."
    }
  }

  // Admin methods

  fun claimLocalDemoAdminPasscode(passcode: String, onResult: (Result<Unit>) -> Unit) {
    viewModelScope.launch {
      val res = repository.prefs.claimAdminPasscode(passcode)
      if (res.isSuccess) {
        _isAdminVerified.value = true
      }
      onResult(res)
    }
  }

  fun verifyLocalDemoAdminPasscode(passcode: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      val ok = repository.prefs.verifyAdminPasscode(passcode)
      if (ok) _isAdminVerified.value = true
      onResult(ok)
    }
  }

  fun lockLocalDemoAdmin() {
    if (mode == DataMode.LOCAL_DEMO) {
      _isAdminVerified.value = false
    }
  }

  fun adminToggleAccountStatus(targetUserId: String, currentStatus: String) {
    val next = if (currentStatus == "ACTIVE") "DISABLED" else "ACTIVE"
    viewModelScope.launch {
      repository.adminToggleAccountStatus(mode, targetUserId, next)
        .onSuccess { _statusMessage.value = "Account status updated to $next." }
        .onFailure { _errorBanner.value = it.localizedMessage ?: "Admin action failed." }
    }
  }

  fun adminGrantCoins(targetUser: PlayerAccount, bonusCoins: Long) {
    viewModelScope.launch {
      repository.adminGrantCoins(mode, targetUser, bonusCoins)
        .onSuccess { _statusMessage.value = "Granted $bonusCoins virtual coins to ${targetUser.username}." }
        .onFailure { _errorBanner.value = it.localizedMessage ?: "Failed to adjust coins." }
    }
  }

  fun adminUpdateConfig(config: PlatformConfig) {
    viewModelScope.launch {
      repository.adminUpdateConfig(mode, config)
        .onSuccess { _statusMessage.value = "Platform settings saved." }
        .onFailure { _errorBanner.value = it.localizedMessage ?: "Failed to update settings." }
    }
  }

  fun adminPublishAnnouncement(title: String, message: String, badge: String, onDone: (Result<Unit>) -> Unit) {
    viewModelScope.launch {
      val res = repository.adminPublishAnnouncement(mode, title, message, badge)
      res.onSuccess { _statusMessage.value = "Announcement published." }
      res.onFailure { _errorBanner.value = it.localizedMessage ?: "Failed to publish announcement." }
      onDone(res)
    }
  }

  fun adminDeleteAnnouncement(id: String) {
    viewModelScope.launch {
      repository.adminDeleteAnnouncement(mode, id)
        .onSuccess { _statusMessage.value = "Announcement removed." }
        .onFailure { _errorBanner.value = it.localizedMessage ?: "Delete failed." }
    }
  }

  fun adminSeedCloudSampleLeaderboard() {
    if (mode != DataMode.FIREBASE_CLOUD) return
    viewModelScope.launch {
      repository.cloud.adminSeedSampleLeaderboard()
        .onSuccess { _statusMessage.value = "Sample demo leaderboard entries seeded." }
        .onFailure { _errorBanner.value = it.localizedMessage ?: "Failed to seed sample entries." }
    }
  }

  private fun <T> Flow<T>.asSafeDataState(label: String): Flow<DataState<T>> =
    map<T, DataState<T>> { DataState.Success(it) }
      .catch { error ->
        val rootError = unwrapOrThrowCancellation(error, label)
        emit(DataState.Error(rootError.message ?: "Failed to load $label"))
      }

  private fun unwrapOrThrowCancellation(error: Throwable, label: String): Throwable {
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val rootError = firestoreError ?: error
    Log.w(TAG, "Error observing $label", rootError)
    return rootError
  }

  private companion object {
    const val TAG = "M9999ViewModel"
  }
}
