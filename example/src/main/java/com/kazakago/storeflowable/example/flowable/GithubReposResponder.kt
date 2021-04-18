package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableCallback
import java.time.Duration
import java.time.LocalDateTime

class GithubReposResponder(userName: String) : PaginatingStoreFlowableCallback<String, List<GithubRepo>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val key: String = userName

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubReposStateManager

    override suspend fun loadData(): List<GithubRepo>? {
        return githubCache.reposCache[key]
    }

    override suspend fun saveData(newData: List<GithubRepo>?) {
        githubCache.reposCache[key] = newData
        githubCache.reposCacheCreatedAt[key] = LocalDateTime.now()
    }

    override suspend fun saveAdditionalData(cachedData: List<GithubRepo>?, fetchedData: List<GithubRepo>) {
        githubCache.reposCache[key] = (cachedData ?: emptyList()) + fetchedData
    }

    override suspend fun fetchOrigin(): FetchingResult<List<GithubRepo>> {
        val data = githubApi.getRepos(key, 1, PER_PAGE)
        return FetchingResult(data = data, noMoreAdditionalData = data.isEmpty())
    }

    override suspend fun fetchAdditionalOrigin(cachedData: List<GithubRepo>?): FetchingResult<List<GithubRepo>> {
        val page = ((cachedData?.size ?: 0) / PER_PAGE + 1)
        val data = githubApi.getRepos(key, page, PER_PAGE)
        return FetchingResult(data = data, noMoreAdditionalData = data.isEmpty())
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
