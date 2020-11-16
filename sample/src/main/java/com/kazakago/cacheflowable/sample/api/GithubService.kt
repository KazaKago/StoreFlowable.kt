package com.kazakago.cacheflowable.sample.api

import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.kazakago.cacheflowable.sample.model.GithubUser
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {

    @GET("users/{user_name}")
    suspend fun getUser(@Path("user_name") userName: String): GithubUser

    @GET("users/{user_name}/repos")
    suspend fun getRepos(@Path("user_name") userName: String, @Query("page") page: Int, @Query("per_page") perPage: Int): List<GithubRepo>

}