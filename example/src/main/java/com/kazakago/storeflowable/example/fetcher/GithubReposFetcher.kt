package com.kazakago.storeflowable.example.fetcher

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.fetcher.PaginationFetcher

object GithubReposFetcher : PaginationFetcher<String, GithubRepo> {

    private val githubApi = GithubApi()

    override suspend fun fetch(param: String): PaginationFetcher.Result<GithubRepo> {
        val data = githubApi.getRepos(param, 1, 20)
        return PaginationFetcher.Result(
            data = data,
            nextRequestKey = if (data.isNotEmpty()) 2.toString() else null,
        )
    }

    override suspend fun fetchNext(nextKey: String, param: String): PaginationFetcher.Result<GithubRepo> {
        val nextPage = nextKey.toInt()
        val data = githubApi.getRepos(param, nextPage, 20)
        return PaginationFetcher.Result(
            data = data,
            nextRequestKey = if (data.isNotEmpty()) (nextPage + 1).toString() else null,
        )
    }
}
