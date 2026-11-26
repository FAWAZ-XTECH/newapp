package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.onGoogleSignInClicked
import com.example.data.local.LocalDemoRepository
import com.example.data.model.PlayerAccount
import com.example.data.repository.SecureRng
import com.example.ui.components.GoldGradientButton
import com.example.ui.components.M9999LogoMark
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RubyLoss
import com.example.ui.theme.RubySurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private enum class DemoAuthTab(val label: String) {
  LOGIN("Demo Login"),
  SIGN_UP("Sign Up"),
  FORGOT("Reset Pass"),
}

@Composable
fun WelcomeAuthScreen(
  localDemoRepository: LocalDemoRepository,
  onGoogleAuthSuccess: () -> Unit,
  onDemoAuthSuccess: (PlayerAccount) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isGoogleLoading by remember { mutableStateOf(false) }
  var isDemoLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var activeTab by remember { mutableStateOf(DemoAuthTab.LOGIN) }

  // Login fields
  var loginIdentifier by remember { mutableStateOf("") }
  var loginPassword by remember { mutableStateOf("") }

  // Registration fields (all 5 requested fields)
  var regFullName by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regUsername by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regConfirmPassword by remember { mutableStateOf("") }

  // Forgot / Reset password state
  var resetEmail by remember { mutableStateOf("") }
  var issuedResetCode by remember { mutableStateOf<String?>(null) }
  var enteredResetCode by remember { mutableStateOf("") }
  var newResetPassword by remember { mutableStateOf("") }
  var confirmResetPassword by remember { mutableStateOf("") }

  var showPassword by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(ObsidianBlack, ObsidianCard, ObsidianBlack)
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Column(
        modifier = Modifier.widthIn(max = 560.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Hero Banner Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(22.dp))
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "M9999 Demo Play luxury arena banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(ObsidianBlack.copy(alpha = 0.25f), ObsidianBlack.copy(alpha = 0.9f))
                )
              )
              .padding(18.dp),
            contentAlignment = Alignment.BottomStart,
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              M9999LogoMark(iconSize = 42.dp, showSubtitle = true)
              Text(
                text = "Royal Virtual Arcade • 6 Playable Mini-Games",
                style = MaterialTheme.typography.bodyMedium.copy(color = GoldChampagne),
              )
            }
          }
        }

        // Feedback banners
        if (errorMessage != null) {
          Surface(
            color = RubySurface,
            border = BorderStroke(1.dp, RubyLoss.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("auth_error_banner"),
          ) {
            Text(
              text = errorMessage!!,
              color = RubyLoss,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(12.dp),
            )
          }
        }
        if (successMessage != null) {
          Surface(
            color = EmeraldSurface,
            border = BorderStroke(1.dp, EmeraldWin.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("auth_success_banner"),
          ) {
            Text(
              text = successMessage!!,
              color = EmeraldWin,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(12.dp),
            )
          }
        }

        // Primary Cloud Auth: Google Sign-In (Firebase + Firestore)
        ObsidianCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Cloud Sync Account (Firebase)",
            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Sign in with Google to save your virtual coins, streak rewards, and leaderboard rank to Cloud Firestore.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Spacer(Modifier.height(12.dp))
          Button(
            onClick = {
              errorMessage = null
              successMessage = null
              isGoogleLoading = true
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isGoogleLoading = false
                  onGoogleAuthSuccess()
                },
                onAuthError = { msg ->
                  isGoogleLoading = false
                  errorMessage = msg
                },
                scope = scope,
                onAuthCancelled = { isGoogleLoading = false },
              )
            },
            enabled = !isGoogleLoading && !isDemoLoading,
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 50.dp)
              .testTag("google_sign_in_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = ObsidianBlack,
            ),
          ) {
            if (isGoogleLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = ObsidianBlack,
                strokeWidth = 2.dp,
              )
            } else {
              Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
              Spacer(Modifier.size(8.dp))
              Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianBorder)
          Text(
            text = "OR LOCAL DEMO STORAGE",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
          )
          HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianBorder)
        }

        // Local Demo Auth System (clearly labeled limitations)
        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Local Demo Mode (On-Device Only)",
            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
          )
          Spacer(Modifier.height(2.dp))
          Text(
            text = "Demo limitation: Local demo accounts use on-device Room storage only and do not sync to Firebase Cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Spacer(Modifier.height(10.dp))

          TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = ObsidianElevated,
            contentColor = GoldBright,
          ) {
            DemoAuthTab.entries.forEach { tab ->
              Tab(
                selected = activeTab == tab,
                onClick = {
                  activeTab = tab
                  errorMessage = null
                  successMessage = null
                },
                modifier = Modifier.testTag("auth_tab_${tab.name.lowercase()}"),
                text = {
                  Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                  )
                },
              )
            }
          }

          Spacer(Modifier.height(14.dp))

          AnimatedContent(targetState = activeTab, label = "demoAuthTabAnim") { tab ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              when (tab) {
                DemoAuthTab.LOGIN -> {
                  DemoTextField(
                    value = loginIdentifier,
                    onValueChange = { loginIdentifier = it },
                    label = "Email or Username",
                    leadingIcon = Icons.Rounded.Email,
                    tag = "login_identifier_input",
                  )
                  DemoPasswordField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = "Password",
                    visible = showPassword,
                    onToggleVisibility = { showPassword = !showPassword },
                    tag = "login_password_input",
                  )
                  GoldGradientButton(
                    text = if (isDemoLoading) "Signing In..." else "Login to Demo Account",
                    enabled = !isDemoLoading,
                    icon = Icons.Rounded.PlayArrow,
                    testTag = "demo_login_button",
                    onClick = {
                      errorMessage = null
                      successMessage = null
                      if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                        errorMessage = "Please enter both email/username and password."
                        return@GoldGradientButton
                      }
                      isDemoLoading = true
                      scope.launch {
                        val res = localDemoRepository.loginDemo(loginIdentifier, loginPassword)
                        isDemoLoading = false
                        res.onSuccess { onDemoAuthSuccess(it) }
                        res.onFailure { errorMessage = it.localizedMessage ?: "Login failed." }
                      }
                    },
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    TextButton(
                      onClick = { activeTab = DemoAuthTab.FORGOT },
                      modifier = Modifier.testTag("forgot_password_link"),
                    ) {
                      Text("Forgot Password?", color = GoldPrimary)
                    }
                    OutlinedButton(
                      onClick = {
                        errorMessage = null
                        isDemoLoading = true
                        scope.launch {
                          val res = localDemoRepository.startInstantGuestDemo()
                          isDemoLoading = false
                          res.onSuccess { onDemoAuthSuccess(it) }
                          res.onFailure { errorMessage = it.localizedMessage ?: "Unable to start demo." }
                        }
                      },
                      enabled = !isDemoLoading,
                      modifier = Modifier.heightIn(min = 48.dp).testTag("instant_demo_guest_button"),
                      border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                    ) {
                      Text("Instant Demo Play", color = GoldChampagne)
                    }
                  }
                }

                DemoAuthTab.SIGN_UP -> {
                  DemoTextField(
                    value = regFullName,
                    onValueChange = { regFullName = it },
                    label = "Full Name",
                    leadingIcon = Icons.Rounded.Person,
                    tag = "signup_fullname_input",
                  )
                  DemoTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = "Email Address",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    tag = "signup_email_input",
                  )
                  DemoTextField(
                    value = regUsername,
                    onValueChange = { regUsername = it },
                    label = "Username",
                    leadingIcon = Icons.Rounded.AccountCircle,
                    tag = "signup_username_input",
                  )
                  DemoPasswordField(
                    value = regPassword,
                    onValueChange = { regPassword = it },
                    label = "Password (min 6 chars)",
                    visible = showPassword,
                    onToggleVisibility = { showPassword = !showPassword },
                    tag = "signup_password_input",
                  )
                  DemoPasswordField(
                    value = regConfirmPassword,
                    onValueChange = { regConfirmPassword = it },
                    label = "Confirm Password",
                    visible = showPassword,
                    onToggleVisibility = { showPassword = !showPassword },
                    tag = "signup_confirm_password_input",
                  )
                  GoldGradientButton(
                    text = if (isDemoLoading) "Creating Account..." else "Create Demo Account (+1,500 Coins)",
                    enabled = !isDemoLoading,
                    testTag = "demo_signup_button",
                    onClick = {
                      errorMessage = null
                      successMessage = null
                      if (regPassword != regConfirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@GoldGradientButton
                      }
                      isDemoLoading = true
                      scope.launch {
                        val res = localDemoRepository.signUpDemo(
                          fullName = regFullName,
                          email = regEmail,
                          username = regUsername,
                          password = regPassword,
                        )
                        isDemoLoading = false
                        res.onSuccess {
                          successMessage = "Demo account created!"
                          onDemoAuthSuccess(it)
                        }
                        res.onFailure { errorMessage = it.localizedMessage ?: "Sign up failed." }
                      }
                    },
                  )
                }

                DemoAuthTab.FORGOT -> {
                  DemoTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = "Registered Demo Email",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    tag = "reset_email_input",
                  )
                  if (issuedResetCode == null) {
                    GoldGradientButton(
                      text = "Generate Demo Reset Code",
                      testTag = "request_reset_code_button",
                      onClick = {
                        errorMessage = null
                        successMessage = null
                        scope.launch {
                          val res = localDemoRepository.verifyDemoEmailForReset(resetEmail)
                          res.onSuccess {
                            val code = SecureRng.nextIntRange(100000, 999999).toString()
                            issuedResetCode = code
                            successMessage = "On-device demo reset code: $code"
                          }
                          res.onFailure { errorMessage = it.localizedMessage ?: "Account not found." }
                        }
                      },
                    )
                  } else {
                    DemoTextField(
                      value = enteredResetCode,
                      onValueChange = { enteredResetCode = it },
                      label = "6-Digit Demo Code",
                      leadingIcon = Icons.Rounded.Lock,
                      keyboardType = KeyboardType.Number,
                      tag = "reset_code_input",
                    )
                    DemoPasswordField(
                      value = newResetPassword,
                      onValueChange = { newResetPassword = it },
                      label = "New Password (min 6 chars)",
                      visible = showPassword,
                      onToggleVisibility = { showPassword = !showPassword },
                      tag = "reset_new_password_input",
                    )
                    DemoPasswordField(
                      value = confirmResetPassword,
                      onValueChange = { confirmResetPassword = it },
                      label = "Confirm New Password",
                      visible = showPassword,
                      onToggleVisibility = { showPassword = !showPassword },
                      tag = "reset_confirm_password_input",
                    )
                    GoldGradientButton(
                      text = "Reset Demo Password",
                      testTag = "confirm_reset_password_button",
                      onClick = {
                        errorMessage = null
                        if (enteredResetCode.trim() != issuedResetCode) {
                          errorMessage = "Verification code does not match."
                          return@GoldGradientButton
                        }
                        if (newResetPassword != confirmResetPassword) {
                          errorMessage = "Passwords do not match."
                          return@GoldGradientButton
                        }
                        scope.launch {
                          val res = localDemoRepository.resetDemoPassword(resetEmail, newResetPassword)
                          res.onSuccess {
                            issuedResetCode = null
                            enteredResetCode = ""
                            newResetPassword = ""
                            confirmResetPassword = ""
                            successMessage = "Password reset! You can now log in."
                            activeTab = DemoAuthTab.LOGIN
                          }
                          res.onFailure { errorMessage = it.localizedMessage ?: "Reset failed." }
                        }
                      },
                    )
                  }
                }
              }
            }
          }
        }

        ResponsibleGamingBanner()
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun DemoTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
  tag: String,
  keyboardType: KeyboardType = KeyboardType.Text,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = GoldPrimary) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    modifier = Modifier.fillMaxWidth().testTag(tag),
    shape = RoundedCornerShape(14.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = GoldPrimary,
      unfocusedBorderColor = ObsidianBorder,
      focusedTextColor = TextPrimary,
      unfocusedTextColor = TextPrimary,
      focusedLabelColor = GoldBright,
      unfocusedLabelColor = TextSecondary,
    ),
  )
}

@Composable
private fun DemoPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  visible: Boolean,
  onToggleVisibility: () -> Unit,
  tag: String,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldPrimary) },
    trailingIcon = {
      IconButton(onClick = onToggleVisibility) {
        Icon(
          imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
          contentDescription = if (visible) "Hide password" else "Show password",
          tint = TextSecondary,
        )
      }
    },
    singleLine = true,
    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    modifier = Modifier.fillMaxWidth().testTag(tag),
    shape = RoundedCornerShape(14.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = GoldPrimary,
      unfocusedBorderColor = ObsidianBorder,
      focusedTextColor = TextPrimary,
      unfocusedTextColor = TextPrimary,
      focusedLabelColor = GoldBright,
      unfocusedLabelColor = TextSecondary,
    ),
  )
}
