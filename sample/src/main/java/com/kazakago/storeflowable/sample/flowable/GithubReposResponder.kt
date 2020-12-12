package com.kazakago.storeflowable.sample.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.paging.PagingStoreFlowableResponder
import com.kazakago.storeflowable.sample.api.GithubApi
import com.kazakago.storeflowable.sample.cache.GithubInMemoryCache
import com.kazakago.storeflowable.sample.cache.GithubReposStateManager
import com.kazakago.storeflowable.sample.model.GithubRepo
import java.time.Duration
import java.time.LocalDateTime

class GithubReposResponder(override val key: String) : PagingStoreFlowableResponder<String, GithubRepo> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

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
        val expiredTime = githubCache.reposCacheCreatedAt[key]?.plus(EXPIRED_DURATION)
        return if (expiredTime != null) {
            expiredTime < LocalDateTime.now()
        } else {
            true
        }
    }
}