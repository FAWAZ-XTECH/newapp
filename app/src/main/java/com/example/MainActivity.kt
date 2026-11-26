package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.signOutFirebase
import com.example.data.model.DataMode
import com.example.data.repository.M9999Repository
import com.example.ui.navigation.M9999AppShell
import com.example.ui.screens.auth.WelcomeAuthScreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.M9999Theme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.viewmodel.M9999ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      M9999Theme {
        M9999RootContent()
      }
    }
  }
}

@Composable
fun M9999RootContent() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repository = remember(context) { M9999Repository(context) }
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var currentFirebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }
  var activeDemoUserId by remember { mutableStateOf<String?>(null) }
  var isBootstrapping by remember { mutableStateOf(true) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentFirebaseUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose { Firebase.auth.removeAuthStateListener(listener) }
  }

  LaunchedEffect(Unit) {
    val rememberedDemoId = repository.prefs.getDemoUserId()
    if (Firebase.auth.currentUser != null) {
      isBootstrapping = false
    } else if (!rememberedDemoId.isNullOrBlank()) {
      activeDemoUserId = rememberedDemoId
      isBootstrapping = false
    } else {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = {
          currentFirebaseUser = Firebase.auth.currentUser
          isBootstrapping = false
        },
        onUnauthenticated = {
          isBootstrapping = false
        },
        scope = scope,
      )
    }
  }

  if (isBootstrapping) {
    Box(
      modifier = Modifier.fillMaxSize().background(ObsidianBlack),
      contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator(color = GoldBright)
    }
    return
  }

  val firebaseUser = currentFirebaseUser
  val demoUserId = activeDemoUserId

  if (firebaseUser != null) {
    val uid = firebaseUser.uid
    key("cloud_$uid") {
      val appContext = context.applicationContext
      val vm: M9999ViewModel = viewModel(
        key = "cloud_$uid",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return M9999ViewModel(
              repository = M9999Repository(appContext),
              mode = DataMode.FIREBASE_CLOUD,
              currentUserId = uid,
              initialDisplayName = firebaseUser.displayName,
              initialEmail = firebaseUser.email,
            ) as T
          }
        },
      )
      M9999AppShell(
        viewModel = vm,
        onLogout = {
          signOutFirebase(credentialManager, scope) {
            currentFirebaseUser = null
          }
        },
      )
    }
  } else if (!demoUserId.isNullOrBlank()) {
    key("demo_$demoUserId") {
      val appContext = context.applicationContext
      val vm: M9999ViewModel = viewModel(
        key = "demo_$demoUserId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return M9999ViewModel(
              repository = M9999Repository(appContext),
              mode = DataMode.LOCAL_DEMO,
              currentUserId = demoUserId,
            ) as T
          }
        },
      )
      M9999AppShell(
        viewModel = vm,
        onLogout = {
          scope.launch {
            repository.localDemo.logoutDemo()
            activeDemoUserId = null
          }
        },
      )
    }
  } else {
    WelcomeAuthScreen(
      localDemoRepository = repository.localDemo,
      onGoogleAuthSuccess = {
        currentFirebaseUser = Firebase.auth.currentUser
      },
      onDemoAuthSuccess = { demoAccount ->
        activeDemoUserId = demoAccount.userId
      },
    )
  }
}
