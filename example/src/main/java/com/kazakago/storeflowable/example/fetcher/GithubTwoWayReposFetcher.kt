package com.kazakago.storeflowable.example.fetcher

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.fetcher.TwoWayPaginationFetcher

object GithubTwoWayReposFetcher : TwoWayPaginationFetcher<Unit, GithubRepo> {

    private val githubApi = GithubApi()

    override suspend fun fetch(param: Unit): TwoWayPaginationFetcher.Result.Initial<GithubRepo> {
        val data = githubApi.getRepos("github", 4, 20)
        return TwoWayPaginationFetcher.Result.Initial(
            data = data,
            nextRequestKey = if (data.isNotEmpty()) 5.toString() else null,
            prevRequestKey = if (data.isNotEmpty()) 3.toString() else null,
        )
    }

    override suspend fun fetchNext(nextKey: String, param: Unit): TwoWayPaginationFetcher.Result.Next<GithubRepo> {
        val nextPage = nextKey.toInt()
        val data = githubApi.getRepos("github", nextPage, 20)
        return TwoWayPaginationFetcher.Result.Next(
            data = data,
            nextRequestKey = if (data.isNotEmpty()) (nextPage + 1).toString() else null,
        )
    }

    override suspend fun fetchPrev(prevKey: String, param: Unit): TwoWayPaginationFetcher.Result.Prev<GithubRepo> {
        val prevPage = prevKey.toInt()
        val data = githubApi.getRepos("github", prevPage, 20)
        return TwoWayPaginationFetcher.Result.Prev(
            data = data,
            prevRequestKey = if (prevPage > 1) (prevPage - 1).toString() else null,
        )
    }
}
