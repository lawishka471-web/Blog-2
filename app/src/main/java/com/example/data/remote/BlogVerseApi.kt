package com.example.data.remote

import com.example.data.model.AuthResponse
import com.example.data.model.ChapterDetail
import com.example.data.model.Comment
import com.example.data.model.CommentRequest
import com.example.data.model.FollowResponse
import com.example.data.model.HomeResponse
import com.example.data.model.LikeResponse
import com.example.data.model.LoginRequest
import com.example.data.model.Novel
import com.example.data.model.NovelDetailResponse
import com.example.data.model.RegisterRequest
import com.example.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BlogVerseApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("user")
    suspend fun getUserProfile(): Response<User>

    @GET("novels")
    suspend fun getNovels(
        @Query("category") category: String? = null,
        @Query("query") query: String? = null
    ): Response<List<Novel>>

    @GET("home")
    suspend fun getHomeData(): Response<HomeResponse>

    @GET("novels/{slug}")
    suspend fun getNovelDetail(@Path("slug") slug: String): Response<NovelDetailResponse>

    @GET("read/{chapter_slug}")
    suspend fun getChapterDetail(@Path("chapter_slug") chapterSlug: String): Response<ChapterDetail>

    @POST("chapters/{slug}/like")
    suspend fun likeChapter(@Path("slug") slug: String): Response<LikeResponse>

    @POST("authors/{id}/follow")
    suspend fun followAuthor(@Path("id") authorId: Long): Response<FollowResponse>

    @GET("chapters/{slug}/comments")
    suspend fun getComments(@Path("slug") slug: String): Response<List<Comment>>

    @POST("chapters/{slug}/comments")
    suspend fun addComment(
        @Path("slug") slug: String,
        @Body request: CommentRequest
    ): Response<Comment>
}
