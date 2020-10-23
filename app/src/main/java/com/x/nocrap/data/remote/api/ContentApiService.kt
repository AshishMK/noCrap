package com.x.nocrap.data.remote.api

import com.x.nocrap.data.local.entity.SuccessResponseEntity
import com.x.nocrap.scenes.homeScene.LoginEntity
import com.x.nocrap.scenes.homeScene.VideoListEntity
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ContentApiService {
    @Multipart
    @POST("loginUser")
    fun loginUser(
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("profile_url") profile_url: RequestBody
    ): Observable<LoginEntity>

    @Multipart
    @POST("addLike")
    fun addLike(
        @Part("uid") uid: RequestBody,
        @Part("vid") vid: RequestBody
    ): Observable<SuccessResponseEntity>

    @Multipart
    @POST("updateViews")
    fun updateViews(
        @Part("uid") uid: RequestBody,
        @Part("vid") vid: RequestBody
    ): Observable<SuccessResponseEntity>

    @Multipart
    @POST("getVideos")
    fun getVideos(
        @Part("user_id") user_id: RequestBody,
        @Part("category") category: RequestBody,
        @Part("offset") offset: RequestBody
    ): Observable<VideoListEntity>

    @Multipart
    @POST("reportContent")
    fun reportContent(
        @Part("uid") uid: RequestBody,
        @Part("contentId") contentId: RequestBody,
        @Part("message") message: RequestBody
    ): Observable<SuccessResponseEntity>

    @Multipart
    @POST("getMyLikes")
    fun getUserLikesVideos(
        @Part("uid") uid: RequestBody,
        @Part("offset") offset: RequestBody
    ): Observable<VideoListEntity>

    @Multipart
    @POST("getAppVersion")
    fun getAppVersion(@Part("dummy") dummy: RequestBody): Observable<SuccessResponseEntity>
}