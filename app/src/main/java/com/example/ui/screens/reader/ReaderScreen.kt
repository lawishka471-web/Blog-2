package com.example.ui.screens.reader

import android.os.Build
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.ui.components.AdLockOverlay
import com.example.ui.components.CommentSection
import com.example.ui.components.GuestAuthDialog
import com.example.ui.components.ReaderBgTheme
import com.example.ui.components.ReaderSettingsSheet
import com.example.ui.components.formatViews
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ReaderSepiaBg
import com.example.ui.theme.ReaderSepiaText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    onNavigateChapter: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val adStatus by viewModel.adStatus.collectAsState()
    val adErrorMessage by viewModel.adErrorMessage.collectAsState()

    var showGuestDialog by remember { mutableStateOf(false) }
    var protectedActionMessage by remember { mutableStateOf("perform this action") }
    var showSettingsSheet by remember { mutableStateOf(false) }

    var readerFontSizeSp by remember { mutableFloatStateOf(16f) }
    var readerBgTheme by remember { mutableStateOf(ReaderBgTheme.DARK) }

    // Preload Rewarded Ad when screen launches or chapter loads
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is ReaderUiState.Success && !(uiState as ReaderUiState.Success).isContentUnlocked) {
            viewModel.preloadAd(context)
        }
    }

    val (currentBgColor, currentTextColor) = when (readerBgTheme) {
        ReaderBgTheme.DARK -> DarkBackground to TextPrimary
        ReaderBgTheme.SEPIA -> ReaderSepiaBg to ReaderSepiaText
        ReaderBgTheme.OLED -> Color.Black to Color.White
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is ReaderUiState.Success) {
                        val detail = (uiState as ReaderUiState.Success).chapterDetail
                        Column {
                            Text(
                                text = detail.novelTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = currentTextColor
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = detail.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text("Reading Chapter", color = currentTextColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTextColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font Settings",
                            tint = currentTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentBgColor)
            )
        },
        containerColor = currentBgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(currentBgColor)
        ) {
            when (val state = uiState) {
                is ReaderUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AmberPrimary)
                    }
                }

                is ReaderUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = state.message, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.loadChapter() }) {
                            Text("Retry", color = AmberPrimary)
                        }
                    }
                }

                is ReaderUiState.Success -> {
                    val detail = state.chapterDetail
                    val scrollState = rememberScrollState()

                    val (topContent20Percent, bottomContent80Percent) = splitHtmlContent(detail.contentHtml)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Chapter Header Info
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = currentTextColor,
                                fontSize = (readerFontSizeSp + 6).sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Author & Date Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            detail.author?.let { author ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = author.avatar,
                                        contentDescription = author.name,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = author.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = TextMuted,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Text(
                                text = detail.createdAt,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Top ~20% Chapter Content (Always visible)
                        Text(
                            text = parseHtmlToString(topContent20Percent),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = currentTextColor,
                                fontSize = readerFontSizeSp.sp,
                                lineHeight = (readerFontSizeSp * 1.55f).sp
                            )
                        )

                        // If Content is NOT Unlocked: Show AdMob Rewarded Ad Lock Overlay Box
                        if (!state.isContentUnlocked) {
                            AdLockOverlay(
                                adStatus = adStatus,
                                adErrorMessage = adErrorMessage,
                                onWatchAdClick = {
                                    viewModel.watchAdToUnlock(context)
                                },
                                onRetryLoadAdClick = {
                                    viewModel.retryLoadAd(context)
                                }
                            )
                        } else {
                            // Unlocked Content 80% Remaining
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = parseHtmlToString(bottomContent80Percent),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = currentTextColor,
                                            fontSize = readerFontSizeSp.sp,
                                            lineHeight = (readerFontSizeSp * 1.55f).sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(32.dp))

                                    // Interactive Actions: Animated Like & Follow Author
                                    InteractiveReaderBar(
                                        isLiked = state.isLiked,
                                        likeCount = state.likeCount,
                                        isAuthorFollowed = state.isAuthorFollowed,
                                        onLikeClick = {
                                            viewModel.likeChapter(onGuestPrompt = {
                                                protectedActionMessage = "like this chapter"
                                                showGuestDialog = true
                                            })
                                        },
                                        onFollowClick = {
                                            viewModel.followAuthor(onGuestPrompt = {
                                                protectedActionMessage = "follow this author"
                                                showGuestDialog = true
                                            })
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Chapter Navigation (Previous / Next)
                                    ChapterNavigationRow(
                                        prevSlug = detail.prevChapterSlug,
                                        nextSlug = detail.nextChapterSlug,
                                        onNavigateChapter = onNavigateChapter
                                    )

                                    Spacer(modifier = Modifier.height(28.dp))

                                    // Comments Section (Protected Action on post comment if guest)
                                    CommentSection(
                                        comments = state.comments,
                                        isLoggedIn = viewModel.isLoggedIn(),
                                        onAddComment = { text ->
                                            viewModel.addComment(text, onGuestPrompt = {
                                                protectedActionMessage = "post a comment"
                                                showGuestDialog = true
                                            })
                                        },
                                        onProtectedActionTriggered = { msg ->
                                            protectedActionMessage = msg
                                            showGuestDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }

            // Reader Settings Modal Bottom Sheet
            if (showSettingsSheet) {
                ReaderSettingsSheet(
                    fontSizeSp = readerFontSizeSp,
                    bgTheme = readerBgTheme,
                    onFontSizeChange = { readerFontSizeSp = it },
                    onBgThemeChange = { readerBgTheme = it },
                    onDismiss = { showSettingsSheet = false }
                )
            }

            // Guest Auth Dialog
            if (showGuestDialog) {
                GuestAuthDialog(
                    actionName = protectedActionMessage,
                    onDismiss = { showGuestDialog = false },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        }
    }
}

@Composable
fun InteractiveReaderBar(
    isLiked: Boolean,
    likeCount: Long,
    isAuthorFollowed: Boolean,
    onLikeClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isLiked) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "likeScale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onLikeClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like Chapter",
                    tint = if (isLiked) ErrorRed else TextMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scaleAnim)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${likeCount} Likes",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isLiked) ErrorRed else TextPrimary
                    )
                )
            }

            // Follow Author Button
            Button(
                onClick = onFollowClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAuthorFollowed) DarkSurfaceVariant else AmberPrimary,
                    contentColor = if (isAuthorFollowed) TextSecondary else Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isAuthorFollowed) Icons.Default.CheckCircle else Icons.Default.Person,
                    contentDescription = "Follow Author",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAuthorFollowed) "Following Author" else "Follow Author",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ChapterNavigationRow(
    prevSlug: String?,
    nextSlug: String?,
    onNavigateChapter: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prevSlug != null) {
            OutlinedButton(
                onClick = { onNavigateChapter(prevSlug) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberPrimary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "Prev",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous", fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (nextSlug != null) {
            Button(
                onClick = { onNavigateChapter(nextSlug) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Next Chapter", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun splitHtmlContent(fullHtml: String): Pair<String, String> {
    val paragraphs = fullHtml.split("</p>")
    if (paragraphs.size <= 2) {
        return fullHtml to ""
    }
    val splitIndex = (paragraphs.size * 0.25f).toInt().coerceAtLeast(1)
    val top20 = paragraphs.take(splitIndex).joinToString("</p>") + "</p>"
    val bottom80 = paragraphs.drop(splitIndex).joinToString("</p>")
    return top20 to bottom80
}

fun parseHtmlToString(html: String): String {
    if (html.isEmpty()) return ""
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(html).toString()
    }
}
