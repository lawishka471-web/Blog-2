package com.example.ui.screens.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ad.AdStatus
import com.example.data.ad.RewardedAdManager
import com.example.data.model.ChapterDetail
import com.example.data.model.Comment
import com.example.data.repository.AuthRepository
import com.example.data.repository.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReaderUiState {
    object Loading : ReaderUiState()
    data class Success(
        val chapterDetail: ChapterDetail,
        val comments: List<Comment>,
        val isContentUnlocked: Boolean = false,
        val isLiked: Boolean = false,
        val likeCount: Long = 0,
        val isAuthorFollowed: Boolean = false
    ) : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
}

class ReaderViewModel(
    val chapterSlug: String,
    private val novelRepository: NovelRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val rewardedAdManager = RewardedAdManager()
    val adStatus: StateFlow<AdStatus> = rewardedAdManager.adStatus

    private val _adErrorMessage = MutableStateFlow<String?>(null)
    val adErrorMessage: StateFlow<String?> = _adErrorMessage.asStateFlow()

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    init {
        loadChapter()
    }

    fun loadChapter() {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            val result = novelRepository.getChapterDetail(chapterSlug)
            result.onSuccess { detail ->
                val commentsResult = novelRepository.getComments(chapterSlug)
                val comments = commentsResult.getOrDefault(emptyList())

                // Check local persistence for previously unlocked chapters
                val isUnlocked = novelRepository.isChapterUnlocked(chapterSlug)

                _uiState.value = ReaderUiState.Success(
                    chapterDetail = detail,
                    comments = comments,
                    isContentUnlocked = isUnlocked,
                    isLiked = detail.isLiked,
                    likeCount = detail.likeCount,
                    isAuthorFollowed = detail.author?.isFollowed ?: false
                )
            }.onFailure { error ->
                _uiState.value = ReaderUiState.Error(error.localizedMessage ?: "Failed to load chapter content")
            }
        }
    }

    /**
     * Preloads the Rewarded Ad.
     */
    fun preloadAd(context: Context) {
        val currentState = _uiState.value
        if (currentState is ReaderUiState.Success && !currentState.isContentUnlocked) {
            rewardedAdManager.loadAd(context)
        }
    }

    /**
     * Triggers Rewarded Ad playback.
     * Unlocks chapter ONLY if onUserEarnedReward callback executes.
     */
    fun watchAdToUnlock(context: Context) {
        _adErrorMessage.value = null
        rewardedAdManager.showAd(
            context = context,
            onUserEarnedReward = {
                // Save locally
                novelRepository.unlockChapter(chapterSlug)
                // Unlock chapter in UI
                val currentState = _uiState.value
                if (currentState is ReaderUiState.Success) {
                    _uiState.value = currentState.copy(isContentUnlocked = true)
                }
                _adErrorMessage.value = null
            },
            onAdClosedWithoutReward = {
                // User closed ad before finishing; keep locked
                _adErrorMessage.value = "Ad was closed early. Watch the complete ad to unlock."
            },
            onAdFailedToShow = { error ->
                _adErrorMessage.value = error.ifBlank { "Ad Not Available" }
            }
        )
    }

    /**
     * Retries loading a Rewarded Ad when unavailable.
     */
    fun retryLoadAd(context: Context) {
        _adErrorMessage.value = null
        rewardedAdManager.loadAd(context)
    }

    fun likeChapter(onGuestPrompt: () -> Unit) {
        if (!isLoggedIn()) {
            onGuestPrompt()
            return
        }

        val currentState = _uiState.value
        if (currentState is ReaderUiState.Success) {
            val newLiked = !currentState.isLiked
            val newCount = if (newLiked) currentState.likeCount + 1 else (currentState.likeCount - 1).coerceAtLeast(0)

            _uiState.value = currentState.copy(isLiked = newLiked, likeCount = newCount)

            viewModelScope.launch {
                val result = novelRepository.likeChapter(chapterSlug)
                result.onSuccess { res ->
                    _uiState.value = currentState.copy(isLiked = res.isLiked, likeCount = res.likeCount)
                }
            }
        }
    }

    fun followAuthor(onGuestPrompt: () -> Unit) {
        if (!isLoggedIn()) {
            onGuestPrompt()
            return
        }

        val currentState = _uiState.value
        if (currentState is ReaderUiState.Success) {
            val authorId = currentState.chapterDetail.author?.id ?: return
            viewModelScope.launch {
                val result = novelRepository.followAuthor(authorId)
                result.onSuccess { res ->
                    _uiState.value = currentState.copy(isAuthorFollowed = res.isFollowed)
                }
            }
        }
    }

    fun addComment(content: String, onGuestPrompt: () -> Unit) {
        if (!isLoggedIn()) {
            onGuestPrompt()
            return
        }

        val currentState = _uiState.value
        if (currentState is ReaderUiState.Success) {
            viewModelScope.launch {
                val result = novelRepository.addComment(chapterSlug, content)
                result.onSuccess { newComment ->
                    val updatedList = listOf(newComment) + currentState.comments
                    _uiState.value = currentState.copy(comments = updatedList)
                }
            }
        }
    }
}
