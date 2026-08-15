package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChapterSummary
import com.example.data.model.Novel
import com.example.data.repository.AuthRepository
import com.example.data.repository.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(
        val novel: Novel,
        val chapters: List<ChapterSummary>,
        val isBookmarked: Boolean,
        val isAuthorFollowed: Boolean
    ) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

class NovelDetailsViewModel(
    private val novelSlug: String,
    private val novelRepository: NovelRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    init {
        loadNovelDetails()
    }

    fun loadNovelDetails() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            val result = novelRepository.getNovelDetail(novelSlug)
            result.onSuccess { response ->
                val isBookmarked = novelRepository.isBookmarked(response.novel.slug)
                val isFollowed = response.novel.author?.isFollowed ?: false
                _uiState.value = DetailsUiState.Success(
                    novel = response.novel,
                    chapters = response.chapters,
                    isBookmarked = isBookmarked,
                    isAuthorFollowed = isFollowed
                )
            }.onFailure { error ->
                _uiState.value = DetailsUiState.Error(error.localizedMessage ?: "Failed to load novel details")
            }
        }
    }

    fun toggleBookmark() {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val newBookmarkState = novelRepository.toggleBookmark(currentState.novel.slug)
            _uiState.value = currentState.copy(isBookmarked = newBookmarkState)
        }
    }

    fun followAuthor(onGuestPrompt: () -> Unit) {
        if (!isLoggedIn()) {
            onGuestPrompt()
            return
        }

        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val authorId = currentState.novel.author?.id ?: return
            viewModelScope.launch {
                val result = novelRepository.followAuthor(authorId)
                result.onSuccess { res ->
                    _uiState.value = currentState.copy(isAuthorFollowed = res.isFollowed)
                }
            }
        }
    }
}
