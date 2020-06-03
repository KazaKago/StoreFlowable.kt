package com.kazakago.cacheflowable.sample.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {
    @GET("users/{user_name}/repos")
    suspend fun getRepos(@Path("user_name") userName: String, @Query("page") page: Int, @Query("per_page") perPage: Int): Response<List<GithubRepoResponse>>

    @GET("users/{user_name}")
    suspend fun getUser(@Path("user_name") userName: String): Response<GithubUserResponse>
}