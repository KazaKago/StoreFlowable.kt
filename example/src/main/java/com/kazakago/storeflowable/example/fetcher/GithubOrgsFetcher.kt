package com.kazakago.storeflowable.example.fetcher

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.fetcher.PaginationFetcher

object GithubOrgsFetcher : PaginationFetcher<Unit, GithubOrg> {

    private val githubApi = GithubApi()

    override suspend fun fetch(param: Unit): PaginationFetcher.Result<GithubOrg> {
        val data = githubApi.getOrgs(null, 20)
        return PaginationFetcher.Result(
            data = data,
            nextRequestKey = data.lastOrNull()?.id?.toString(),
        )
    }

    override suspend fun fetchNext(nextKey: String, param: Unit): PaginationFetcher.Result<GithubOrg> {
        val data = githubApi.getOrgs(nextKey.toLong(), 20)
        return PaginationFetcher.Result(
            data = data,
            nextRequestKey = data.lastOrNull()?.id?.toString(),
        )
    }
}
