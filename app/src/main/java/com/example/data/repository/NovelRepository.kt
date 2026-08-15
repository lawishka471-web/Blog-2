package com.example.data.repository

import com.example.data.local.TokenManager
import com.example.data.model.ChapterDetail
import com.example.data.model.Comment
import com.example.data.model.CommentRequest
import com.example.data.model.FollowResponse
import com.example.data.model.HomeResponse
import com.example.data.model.LikeResponse
import com.example.data.model.Novel
import com.example.data.model.NovelDetailResponse
import com.example.data.remote.ApiClient

class NovelRepository(private val tokenManager: TokenManager) {

    private val api = ApiClient.getApi(tokenManager)

    suspend fun getHomeData(): Result<HomeResponse> {
        return try {
            val response = api.getHomeData()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load home data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNovels(category: String? = null, query: String? = null): Result<List<Novel>> {
        return try {
            val response = api.getNovels(category, query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load novels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNovelDetail(slug: String): Result<NovelDetailResponse> {
        return try {
            val response = api.getNovelDetail(slug)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load novel details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChapterDetail(chapterSlug: String): Result<ChapterDetail> {
        return try {
            val response = api.getChapterDetail(chapterSlug)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load chapter content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeChapter(slug: String): Result<LikeResponse> {
        return try {
            val response = api.likeChapter(slug)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to like chapter"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followAuthor(authorId: Long): Result<FollowResponse> {
        return try {
            val response = api.followAuthor(authorId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to follow author"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(slug: String): Result<List<Comment>> {
        return try {
            val response = api.getComments(slug)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load comments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(slug: String, content: String): Result<Comment> {
        return try {
            val response = api.addComment(slug, CommentRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to post comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toggleBookmark(slug: String): Boolean {
        return tokenManager.toggleBookmark(slug)
    }

    fun isBookmarked(slug: String): Boolean {
        return tokenManager.isBookmarked(slug)
    }

    fun getBookmarkedSlugs(): Set<String> {
        return tokenManager.getBookmarks()
    }

    fun isChapterUnlocked(chapterSlug: String): Boolean {
        return tokenManager.isChapterUnlocked(chapterSlug)
    }

    fun unlockChapter(chapterSlug: String) {
        tokenManager.unlockChapter(chapterSlug)
    }
}
