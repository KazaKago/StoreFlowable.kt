package com.kazakago.storeflowable.example.fetcher

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.fetcher.Fetcher

object GithubUserFetcher : Fetcher<String, GithubUser> {

    private val githubApi = GithubApi()

    override suspend fun fetch(param: String): GithubUser {
        return githubApi.getUser(param)
    }
}
