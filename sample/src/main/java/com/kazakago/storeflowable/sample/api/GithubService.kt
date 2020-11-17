package com.kazakago.storeflowable.sample.api

import com.kazakago.storeflowable.sample.model.GithubMeta
import com.kazakago.storeflowable.sample.model.GithubOrg
import com.kazakago.storeflowable.sample.model.GithubRepo
import com.kazakago.storeflowable.sample.model.GithubUser
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {

    @GET("meta")
    suspend fun getMeta(): GithubMeta

    @GET("organizations")
    suspend fun getOrgs(@Query("since") since: Long?, @Query("per_page") perPage: Int): List<GithubOrg>

    @GET("users/{user_name}")
    suspend fun getUser(@Path("user_name") userName: String): GithubUser

    @GET("users/{user_name}/repos")
    suspend fun getRepos(@Path("user_name") userName: String, @Query("page") page: Int, @Query("per_page") perPage: Int): List<GithubRepo>

}