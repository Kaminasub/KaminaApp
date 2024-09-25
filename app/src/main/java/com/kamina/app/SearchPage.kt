package com.kamina.app

import Navbar
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.AutocompleteSuggestion
import com.kamina.app.api.SearchResult
import com.kamina.app.api.fetchAutocompleteSuggestions
import com.kamina.app.api.fetchSearchResults

@Composable
fun SearchPage(navController: NavHostController, userId: Int) {  // Accept userId as a parameter
    var searchQuery by remember { mutableStateOf("") }
    var autocompleteSuggestions by remember { mutableStateOf<List<AutocompleteSuggestion>?>(null) }
    var searchResults by remember { mutableStateOf<List<SearchResult>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 1) {
            isLoading = true
            fetchAutocompleteSuggestions(searchQuery) { suggestions ->
                autocompleteSuggestions = suggestions
                isLoading = false
            }
            fetchSearchResults(searchQuery) { results ->
                searchResults = results
                isLoading = false
            }
        } else {
            autocompleteSuggestions = null
            searchResults = null
        }
    }

    Scaffold(
        topBar = {
            Navbar(navController = navController, avatarChanged = false)  // Navbar now uses navController
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(0.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    // Search input
                    SearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Autocomplete suggestions
                    autocompleteSuggestions?.let {
                        AutocompleteSuggestions(suggestions = it, onSuggestionClick = { suggestion ->
                            searchQuery = suggestion
                        })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search results
                    searchResults?.let {
                        SearchResults(results = it, userId = userId)  // Pass userId to SearchResults
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
fun SearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally  // Center the text horizontally in the Column
    ) {
        Text(
            text = "Search Movies, Series, or Actors",
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
fun AutocompleteSuggestions(suggestions: List<AutocompleteSuggestion>, onSuggestionClick: (String) -> Unit) {
    LazyColumn {
        items(suggestions) { suggestion ->
            Text(
                text = suggestion.suggestion,
                modifier = Modifier
                    .clickable { onSuggestionClick(suggestion.suggestion) }
                    .padding(8.dp), //spacio entre los thumbnails
                color = Color.White
            )
        }
    }
}

@Composable
fun SearchResults(results: List<SearchResult>, userId: Int) {  // Accept userId as a parameter
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)  // Spacing between items
    ) {
        items(results) { result ->
            Column(
                modifier = Modifier
                    .clickable {
                        // Navigate to DetailPageActivity with entityId and userId
                        val intent = Intent(context, DetailPageActivity::class.java).apply {
                            putExtra("entityId", result.id)  // Pass the entityId
                            putExtra("userId", userId)  // Pass the userId
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
                        .clip(RoundedCornerShape(12.dp))  // 12dp radius for carousel items
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


