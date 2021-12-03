package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.oneway.Fetched
import com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowableFactory
import java.time.Duration
import java.time.LocalDateTime

class GithubReposFlowableFactory : PaginationStoreFlowableFactory<String, List<GithubRepo>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubReposStateManager

    override suspend fun loadDataFromCache(param: String): List<GithubRepo>? {
        return githubCache.reposCache[param]
    }

    override suspend fun saveDataToCache(newData: List<GithubRepo>?, param: String) {
        githubCache.reposCache[param] = newData
        githubCache.reposCacheCreatedAt[param] = LocalDateTime.now()
    }

    override suspend fun saveNextDataToCache(cachedData: List<GithubRepo>, newData: List<GithubRepo>, param: String) {
        githubCache.reposCache[param] = cachedData + newData
    }

    override suspend fun fetchDataFromOrigin(param: String): Fetched<List<GithubRepo>> {
        val data = githubApi.getRepos(param, 1, PER_PAGE)
        return Fetched(
            data = data,
            nextKey = if (data.isNotEmpty()) 2.toString() else null,
        )
    }

    override suspend fun fetchNextDataFromOrigin(nextKey: String, param: String): Fetched<List<GithubRepo>> {
        val nextPage = nextKey.toInt()
        val data = githubApi.getRepos(param, nextPage, PER_PAGE)
        return Fetched(
            data = data,
            nextKey = if (data.isNotEmpty()) (nextPage + 1).toString() else null,
        )
    }

    override suspend fun needRefresh(cachedData: List<GithubRepo>, param: String): Boolean {
        val createdAt = githubCache.reposCacheCreatedAt[param]
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
