package com.kazakago.cachesample.data.api

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GithubApi {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
        .build()

    suspend fun getRepos(userName: String, page: Int, perPage: Int): List<GithubRepoResponse> {
        return retrofit.create(GithubService::class.java).getRepos(userName, page, perPage).body()!!
    }

    suspend fun getUser(userName: String): GithubUserResponse {
        return retrofit.create(GithubService::class.java).getUser(userName).body()!!
    }

}