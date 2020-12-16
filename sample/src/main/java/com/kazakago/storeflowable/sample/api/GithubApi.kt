package com.kazakago.storeflowable.sample.api

import com.kazakago.storeflowable.sample.model.GithubMeta
import com.kazakago.storeflowable.sample.model.GithubOrg
import com.kazakago.storeflowable.sample.model.GithubRepo
import com.kazakago.storeflowable.sample.model.GithubUser
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GithubApi {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
        .build()

    suspend fun getMeta(): GithubMeta {
        return retrofit.create(GithubService::class.java).getMeta()
    }

    suspend fun getOrgs(since: Long?, perPage: Int): List<GithubOrg> {
        return retrofit.create(GithubService::class.java).getOrgs(since, perPage)
    }

    suspend fun getUser(userName: String): GithubUser {
        return retrofit.create(GithubService::class.java).getUser(userName)
    }

    suspend fun getRepos(userName: String, page: Int, perPage: Int): List<GithubRepo> {
        return retrofit.create(GithubService::class.java).getRepos(userName, page, perPage)
    }
}
