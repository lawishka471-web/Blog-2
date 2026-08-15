package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.HomeResponse
import com.example.data.model.Novel
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val homeData: HomeResponse,
        val filteredNovels: List<Novel>? = null,
        val selectedCategorySlug: String? = null
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val novelRepository: NovelRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = novelRepository.getHomeData()
            result.onSuccess { data ->
                _uiState.value = HomeUiState.Success(data)
            }.onFailure { error ->
                _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Failed to load home content")
            }
        }
    }

    fun selectCategory(categorySlug: String?) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            if (categorySlug == null) {
                _uiState.value = currentState.copy(
                    filteredNovels = null,
                    selectedCategorySlug = null
                )
            } else {
                viewModelScope.launch {
                    val result = novelRepository.getNovels(category = categorySlug)
                    result.onSuccess { list ->
                        _uiState.value = currentState.copy(
                            filteredNovels = list,
                            selectedCategorySlug = categorySlug
                        )
                    }
                }
            }
        }
    }
}
