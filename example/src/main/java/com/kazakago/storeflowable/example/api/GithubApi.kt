package com.kazakago.storeflowable.example.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.model.GithubUser
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

class GithubApi {

    private val serialFormatter = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(serialFormatter.asConverterFactory("application/json".toMediaType()))
        .build()

    suspend fun getMeta(): GithubMeta {
        delay(1000) // dummy delay
        return retrofit.create(GithubService::class.java).getMeta()
    }

    suspend fun getOrgs(since: Long?, perPage: Int): List<GithubOrg> {
        delay(1000) // dummy delay
        return retrofit.create(GithubService::class.java).getOrgs(since, perPage)
    }

    suspend fun getUser(userName: String): GithubUser {
        delay(1000) // dummy delay
        return retrofit.create(GithubService::class.java).getUser(userName)
    }

    suspend fun getRepos(userName: String, page: Int, perPage: Int): List<GithubRepo> {
        delay(1000) // dummy delay
        return retrofit.create(GithubService::class.java).getRepos(userName, page, perPage)
    }
}
