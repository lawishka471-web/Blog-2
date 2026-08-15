package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val avatar: String? = null,
    @Json(name = "is_author") val isAuthor: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String? = null,
    @Json(name = "access_token") val accessToken: String? = null,
    val user: User? = null,
    val message: String? = null,
    val success: Boolean? = true
) {
    fun getEffectiveToken(): String? = token ?: accessToken
}

@JsonClass(generateAdapter = true)
data class Category(
    val id: Int,
    val name: String,
    val slug: String
)

@JsonClass(generateAdapter = true)
data class Author(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    @Json(name = "is_followed") val isFollowed: Boolean = false,
    @Json(name = "followers_count") val followersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class Novel(
    val id: Long,
    val title: String,
    val slug: String,
    @Json(name = "cover_url") val coverUrl: String,
    val author: Author? = null,
    val category: Category? = null,
    @Json(name = "category_name") val categoryName: String? = null,
    val description: String? = null,
    val status: String = "Ongoing",
    @Json(name = "view_count") val viewCount: Long = 0,
    @Json(name = "like_count") val likeCount: Long = 0,
    @Json(name = "total_chapters") val totalChapters: Int = 0,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    val rating: Float = 4.8f,
    val featured: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ChapterSummary(
    val id: Long,
    val title: String,
    val slug: String,
    @Json(name = "chapter_number") val chapterNumber: Int,
    @Json(name = "view_count") val viewCount: Long = 0,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "read_time_minutes") val readTimeMinutes: Int = 5
)

@JsonClass(generateAdapter = true)
data class ChapterDetail(
    val id: Long,
    @Json(name = "novel_slug") val novelSlug: String,
    @Json(name = "novel_title") val novelTitle: String,
    val title: String,
    val slug: String,
    @Json(name = "chapter_number") val chapterNumber: Int,
    @Json(name = "content_html") val contentHtml: String,
    val author: Author? = null,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    @Json(name = "like_count") val likeCount: Long = 0,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "prev_chapter_slug") val prevChapterSlug: String? = null,
    @Json(name = "next_chapter_slug") val nextChapterSlug: String? = null
)

@JsonClass(generateAdapter = true)
data class Comment(
    val id: Long,
    @Json(name = "user_name") val userName: String,
    @Json(name = "user_avatar") val userAvatar: String? = null,
    val content: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "likes_count") val likesCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CommentRequest(
    val content: String
)

@JsonClass(generateAdapter = true)
data class LikeResponse(
    @Json(name = "is_liked") val isLiked: Boolean,
    @Json(name = "like_count") val likeCount: Long,
    val message: String
)

@JsonClass(generateAdapter = true)
data class FollowResponse(
    @Json(name = "is_followed") val isFollowed: Boolean,
    @Json(name = "followers_count") val followersCount: Int,
    val message: String
)

@JsonClass(generateAdapter = true)
data class NovelDetailResponse(
    val novel: Novel,
    val chapters: List<ChapterSummary>
)

@JsonClass(generateAdapter = true)
data class HomeResponse(
    val featured: List<Novel>,
    val trending: List<Novel>,
    val latest: List<Novel>,
    val popular: List<Novel>,
    val categories: List<Category>
)
