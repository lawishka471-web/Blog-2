package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Novel
import com.example.ui.components.BlogVerseTopBar
import com.example.ui.components.CategoryPills
import com.example.ui.components.HeroCarousel
import com.example.ui.components.HorizontalNovelRow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNovelClick: (Novel) -> Unit,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            BlogVerseTopBar(
                currentUser = currentUser,
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
                onLoginClick = onLoginClick
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AmberPrimary)
                    }
                }

                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Text(
                            text = "Connection Error",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadHomeData() }) {
                            Text("Retry", color = AmberPrimary)
                        }
                    }
                }

                is HomeUiState.Success -> {
                    val homeData = state.homeData
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Hero Carousel Banner
                        HeroCarousel(
                            novels = homeData.featured,
                            onNovelClick = onNovelClick
                        )

                        // Genre / Category Pills
                        CategoryPills(
                            categories = homeData.categories,
                            selectedCategorySlug = state.selectedCategorySlug,
                            onCategorySelected = { slug -> viewModel.selectCategory(slug) }
                        )

                        if (state.filteredNovels != null) {
                            // Filtered Novels View
                            HorizontalNovelRow(
                                title = "Filtered Category Novels",
                                novels = state.filteredNovels,
                                onNovelClick = onNovelClick
                            )
                        } else {
                            // Section 1: Trending Novels
                            HorizontalNovelRow(
                                title = "🔥 Trending Novels",
                                novels = homeData.trending,
                                onNovelClick = onNovelClick
                            )

                            // Section 2: Latest Chapters
                            HorizontalNovelRow(
                                title = "⚡ Latest Chapters",
                                novels = homeData.latest,
                                onNovelClick = onNovelClick
                            )

                            // Section 3: Popular Novels
                            HorizontalNovelRow(
                                title = "⭐ Popular Novels",
                                novels = homeData.popular,
                                onNovelClick = onNovelClick
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
