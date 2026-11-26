package com.example.ui.navigation

object NavRoutes {
  // Bottom / Rail primary destinations
  const val HOME = "home"
  const val GAMES = "games"
  const val REWARDS = "rewards"
  const val LEADERBOARD = "leaderboard"
  const val PROFILE = "profile"

  // Secondary destinations
  const val WALLET = "wallet"
  const val NOTIFICATIONS = "notifications"
  const val GAME_HISTORY = "game_history"
  const val SETTINGS = "settings"
  const val HELP_SUPPORT = "help_support"
  const val ABOUT_US = "about_us"
  const val PRIVACY_POLICY = "privacy_policy"
  const val TERMS_CONDITIONS = "terms_conditions"
  const val ADMIN_DASHBOARD = "admin_dashboard"

  // Playable mini-games
  const val GAME_LUCKY_WHEEL = "game_lucky_wheel"
  const val GAME_SLOT_MACHINE = "game_slot_machine"
  const val GAME_DICE = "game_dice_game"
  const val GAME_CARD_REVEAL = "game_card_reveal"
  const val GAME_LUCKY_NUMBER = "game_lucky_number"
  const val GAME_MEMORY_MATCH = "game_memory_match"

  fun routeForGame(gameId: String): String = when (gameId) {
    "lucky_wheel" -> GAME_LUCKY_WHEEL
    "slot_machine" -> GAME_SLOT_MACHINE
    "dice_game" -> GAME_DICE
    "card_reveal" -> GAME_CARD_REVEAL
    "lucky_number" -> GAME_LUCKY_NUMBER
    "memory_match" -> GAME_MEMORY_MATCH
    else -> GAMES
  }
}
