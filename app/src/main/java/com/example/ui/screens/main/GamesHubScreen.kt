package com.example.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.GameCategory
import com.example.data.model.MiniGameId
import com.example.data.model.PlatformConfig
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GamesHubScreen(
  config: PlatformConfig,
  onOpenGame: (MiniGameId) -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(GameCategory.ALL) }

  val filtered = remember(searchQuery, selectedCategory) {
    MiniGameId.entries.filter { game ->
      val catMatch = selectedCategory == GameCategory.ALL || game.category == selectedCategory
      val q = searchQuery.trim()
      val textMatch = q.isEmpty() ||
        game.title.contains(q, ignoreCase = true) ||
        game.subtitle.contains(q, ignoreCase = true)
      catMatch && textMatch
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("M9999 Game Center", style = MaterialTheme.typography.headlineLarge, color = GoldBright)
        Text(
          text = "All 6 mini-games run on fair SecureRandom outcomes using fictional virtual coins.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
      }
    }

    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search mini-games...") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldPrimary) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = TextSecondary)
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("games_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = ObsidianBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
        ),
      )
    }

    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(GameCategory.entries) { category ->
          val selected = selectedCategory == category
          FilterChip(
            selected = selected,
            onClick = { selectedCategory = category },
            label = { Text(category.label) },
            modifier = Modifier.heightIn(min = 48.dp).testTag("games_filter_${category.name.lowercase()}"),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GoldPrimary,
              selectedLabelColor = ObsidianBlack,
              containerColor = ObsidianCard,
              labelColor = TextSecondary,
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = selected,
              borderColor = ObsidianBorder,
              selectedBorderColor = GoldBright,
            ),
          )
        }
      }
    }

    if (filtered.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Rounded.SportsEsports,
          title = "No matching games",
          message = "Try clearing your search or switching game categories.",
        )
      }
    } else {
      items(filtered, key = { it.id }) { game ->
        CompactGameRowCard(
          game = game,
          enabled = config.isGameEnabled(game.id),
          onClick = { onOpenGame(game) },
        )
      }
    }

    item {
      ResponsibleGamingBanner()
    }
  }
}
