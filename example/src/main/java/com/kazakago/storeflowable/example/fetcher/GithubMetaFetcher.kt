package com.kazakago.storeflowable.example.fetcher

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.fetcher.Fetcher

object GithubMetaFetcher : Fetcher<Unit, GithubMeta> {

    private val githubApi = GithubApi()

    override suspend fun fetch(param: Unit): GithubMeta {
        return githubApi.getMeta()
    }
}
