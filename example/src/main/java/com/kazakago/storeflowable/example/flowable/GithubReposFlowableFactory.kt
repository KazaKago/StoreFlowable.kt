package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.datastate.FlowableDataStateManager
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.oneway.Fetched
import com.kazakago.storeflowable.pagination.oneway.OneWayStoreFlowableFactory
import java.time.Duration
import java.time.LocalDateTime

class GithubReposFlowableFactory(userName: String) : OneWayStoreFlowableFactory<String, List<GithubRepo>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val key: String = userName

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubReposStateManager

    override suspend fun loadDataFromCache(): List<GithubRepo>? {
        return githubCache.reposCache[key]
    }

    override suspend fun saveDataToCache(newData: List<GithubRepo>?) {
        githubCache.reposCache[key] = newData
        githubCache.reposCacheCreatedAt[key] = LocalDateTime.now()
    }

    override suspend fun saveNextDataToCache(cachedData: List<GithubRepo>?, newData: List<GithubRepo>) {
        githubCache.reposCache[key] = (cachedData ?: emptyList()) + newData
    }

    override suspend fun fetchDataFromOrigin(): Fetched<List<GithubRepo>> {
        val data = githubApi.getRepos(key, 1, PER_PAGE)
        return Fetched(
            data = data,
            nextKey = if (data.isNotEmpty()) 2.toString() else null,
        )
    }

    override suspend fun fetchNextDataFromOrigin(nextKey: String): Fetched<List<GithubRepo>> {
        val nextPage = nextKey.toInt()
        val data = githubApi.getRepos(key, nextPage, PER_PAGE)
        return Fetched(
            data = data,
            nextKey = if (data.isNotEmpty()) (nextPage + 1).toString() else null,
        )
    }

    override suspend fun needRefresh(cachedData: List<GithubRepo>): Boolean {
        val createdAt = githubCache.reposCacheCreatedAt[key]
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
