package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubInMemoryCache
import com.kazakago.storeflowable.example.cache.GithubReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.paging.PagingStoreFlowableResponder
import java.time.Duration
import java.time.LocalDateTime

class GithubReposResponder(userName: String) : PagingStoreFlowableResponder<String, GithubRepo> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofSeconds(30)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val key: String = userName

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubReposStateManager

    override suspend fun loadData(): List<GithubRepo>? {
        return githubCache.reposCache[key]
    }

    override suspend fun saveData(data: List<GithubRepo>?, additionalRequest: Boolean) {
        githubCache.reposCache[key] = data
        if (!additionalRequest) githubCache.reposCacheCreatedAt[key] = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(data: List<GithubRepo>?, additionalRequest: Boolean): List<GithubRepo> {
        val page = if (additionalRequest) ((data?.size ?: 0) / PER_PAGE + 1) else 1
        return githubApi.getRepos(key, page, PER_PAGE)
    }

    override suspend fun needRefresh(data: List<GithubRepo>): Boolean {
        return githubCache.reposCacheCreatedAt[key]?.let { createdAt ->
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } ?: true
    }
}
