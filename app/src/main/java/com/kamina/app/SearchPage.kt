package com.kamina.app

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.AutocompleteSuggestion
import com.kamina.app.api.SearchResult
import com.kamina.app.api.UserApiHelper
import com.kamina.app.api.fetchAutocompleteSuggestions
import com.kamina.app.api.fetchSearchResults
import com.kamina.app.ui.components.UserDropdownMenu

@Composable
fun SearchPage(userId: Int, userLanguage: String) {
    val context = LocalContext.current
    var userIconUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user icon if userId is not null
    LaunchedEffect(userId) {
        UserApiHelper.fetchUserIcon(userId.toString()) { iconUrl ->
            userIconUrl = iconUrl
        }
    }

    val painter = rememberAsyncImagePainter(userIconUrl ?: "")
    var isFocused by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var autocompleteSuggestions by remember { mutableStateOf<List<AutocompleteSuggestion>?>(null) }
    var searchResults by remember { mutableStateOf<List<SearchResult>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Triggering fetch operations based on searchQuery
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 1) {
            isLoading = true

            // Fetch autocomplete suggestions with language
            fetchAutocompleteSuggestions(searchQuery, userLanguage) { suggestions ->
                autocompleteSuggestions = suggestions ?: emptyList()  // Handle null case
                isLoading = false
            }

            // Fetch search results with language
            fetchSearchResults(searchQuery, userLanguage) { results ->
                searchResults = results ?: emptyList()  // Handle null case
                isLoading = false
            }
        } else {
            autocompleteSuggestions = null
            searchResults = null
        }
    }

    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF182459),
                                Color(0xFF14143C),
                                Color(0xFF000000)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(x = 1000f, y = -1000f)
                        )
                    )
            ) {
                // Box to hold both User Icon and DropdownMenu for alignment
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 10.dp)
                ) {
                    // User Icon in the top right corner
                    Image(
                        painter = painter,
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused  // Update the state when focus changes
                            }
                            .focusable()
                            .then(
                                if (isFocused) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    ).padding(3.dp)
                                } else {
                                    Modifier
                                }
                            )
                            .clip(CircleShape)
                            .clickable {
                                showDropdownMenu = true
                            }
                    )

                    // Align DropdownMenu under User Icon
                    UserDropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        context = context
                    )
                }

                // Main content for search page
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 55.dp, start = 10.dp, end = 10.dp)
                ) {
                    SearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    autocompleteSuggestions?.let {
                        AutocompleteSuggestions(
                            suggestions = it,
                            onSuggestionClick = { suggestion -> searchQuery = suggestion.suggestion }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    searchResults?.let { results ->
                        if (results.isEmpty()) {
                            Text("No results found", color = Color.White, fontSize = 18.sp)
                        } else {
                            // Pass userLanguage to SearchResults
                            SearchResults(results = results, userId = userId, userLanguage = userLanguage)
                        }
                    }

                    if (isLoading) {
                        Text(text = "Loading...", color = Color.White)
                    }
                }
            }
        }
    )
}

@Composable
fun AutocompleteSuggestions(suggestions: List<AutocompleteSuggestion>, onSuggestionClick: (AutocompleteSuggestion) -> Unit) {
    LazyColumn {
        items(suggestions) { suggestion ->
            Text(
                text = suggestion.suggestion,
                modifier = Modifier
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(8.dp), // Space between suggestions
                color = Color.White
            )
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally  // Center the text horizontally in the Column
    ) {
        Text(
            text = "Search Movies, Series, Actors, or Categories",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)  // Center the title text
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Box to contain and center the TextField
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))  // Apply rounded corners
                .background(Color.White),  // Apply background color
            contentAlignment = Alignment.Center  // Center the content inside the Box
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                singleLine = true  // Ensures the text field is single line
            )
        }
    }
}

@Composable
fun SearchResults(results: List<SearchResult>, userId: Int, userLanguage: String) {  // Add userLanguage as a parameter
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)  // Spacing between items
    ) {
        items(results) { result ->
            // Track focus state for each item
            var isFocused by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .onFocusChanged { isFocused = it.isFocused }  // Update focus state
                    .focusable()  // Make it focusable for TV remote control
                    .then(
                        if (isFocused) {
                            Modifier.border(
                                width = 3.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Modifier.border(
                                width = 0.dp,
                                color = Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    )
                    .clickable {
                        // Navigate to DetailPageActivity with entityId, userId, and userLanguage
                        val intent = Intent(context, DetailPageActivity::class.java).apply {
                            putExtra("entityId", result.id)  // Pass the entityId
                            putExtra("userId", userId)  // Pass the userId
                            putExtra("userLanguage", userLanguage)  // Pass the userLanguage
                        }
                        context.startActivity(intent)
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(result.thumbnail),
                    contentDescription = result.name,
                    modifier = Modifier
                        .width(178.dp)  // Consistent width
                        .height(280.dp) // Consistent height
                        .clip(RoundedCornerShape(12.dp)),  // 12dp radius for carousel items
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
