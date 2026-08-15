package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _tokenState = MutableStateFlow<String?>(getToken())
    val tokenState: StateFlow<String?> = _tokenState.asStateFlow()

    private val _userState = MutableStateFlow<User?>(getUser())
    val userState: StateFlow<User?> = _userState.asStateFlow()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _tokenState.value = token
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(user: User) {
        prefs.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_AVATAR, user.avatar)
            .putBoolean(KEY_USER_IS_AUTHOR, user.isAuthor)
            .apply()
        _userState.value = user
    }

    fun getUser(): User? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        if (id == -1L) return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val avatar = prefs.getString(KEY_USER_AVATAR, null)
        val isAuthor = prefs.getBoolean(KEY_USER_IS_AUTHOR, false)
        return User(id = id, name = name, email = email, avatar = avatar, isAuthor = isAuthor)
    }

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_AVATAR)
            .remove(KEY_USER_IS_AUTHOR)
            .apply()
        _tokenState.value = null
        _userState.value = null
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNull_orEmpty()
    }

    // Bookmarks management
    fun toggleBookmark(novelSlug: String): Boolean {
        val set = getBookmarks().toMutableSet()
        val isBookmarked = if (set.contains(novelSlug)) {
            set.remove(novelSlug)
            false
        } else {
            set.add(novelSlug)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, set).apply()
        return isBookmarked
    }

    fun isBookmarked(novelSlug: String): Boolean {
        return getBookmarks().contains(novelSlug)
    }

    fun getBookmarks(): Set<String> {
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()
    }

    // Unlocked chapters management
    fun unlockChapter(chapterSlug: String) {
        val set = getUnlockedChapters().toMutableSet()
        set.add(chapterSlug)
        prefs.edit().putStringSet(KEY_UNLOCKED_CHAPTERS, set).apply()
    }

    fun isChapterUnlocked(chapterSlug: String): Boolean {
        return getUnlockedChapters().contains(chapterSlug)
    }

    fun getUnlockedChapters(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED_CHAPTERS, emptySet()) ?: emptySet()
    }

    // Custom Base URL configuration
    fun getApiBaseUrl(): String {
        val url = (prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL).trim()
        return if (url.endsWith("/")) url else "$url/"
    }

    fun saveApiBaseUrl(url: String) {
        val formattedUrl = url.trim().let { if (it.endsWith("/")) it else "$it/" }
        prefs.edit().putString(KEY_BASE_URL, formattedUrl).apply()
    }

    private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()

    companion object {
        private const val PREF_NAME = "blog_verse_prefs"
        private const val KEY_TOKEN = "sanctum_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_USER_IS_AUTHOR = "user_is_author"
        private const val KEY_BOOKMARKS = "user_bookmarks"
        private const val KEY_UNLOCKED_CHAPTERS = "user_unlocked_chapters"
        private const val KEY_BASE_URL = "api_base_url"

        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/api/"
    }
}
