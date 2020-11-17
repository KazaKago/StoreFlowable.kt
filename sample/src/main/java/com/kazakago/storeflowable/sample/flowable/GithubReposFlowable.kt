package com.kazakago.storeflowable.sample.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.paging.AbstractPagingStoreFlowable
import com.kazakago.storeflowable.sample.api.GithubApi
import com.kazakago.storeflowable.sample.cache.GithubInMemoryCache
import com.kazakago.storeflowable.sample.cache.GithubReposStateManager
import com.kazakago.storeflowable.sample.model.GithubRepo
import java.time.Duration
import java.time.LocalDateTime

class GithubReposFlowable(private val userName: String) : AbstractPagingStoreFlowable<String, GithubRepo>(userName) {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubReposStateManager

    override suspend fun loadData(): List<GithubRepo>? {
        return githubCache.reposCache[userName]
    }

    override suspend fun saveData(data: List<GithubRepo>?, additionalRequest: Boolean) {
        githubCache.reposCache[userName] = data
        if (!additionalRequest) githubCache.reposCacheCreatedAt[userName] = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(data: List<GithubRepo>?, additionalRequest: Boolean): List<GithubRepo> {
        val page = if (additionalRequest) ((data?.size ?: 0) / PER_PAGE + 1) else 1
        return githubApi.getRepos(userName, page, PER_PAGE)
    }

    override suspend fun needRefresh(data: List<GithubRepo>): Boolean {
        val expiredTime = githubCache.reposCacheCreatedAt[userName]?.plus(EXPIRED_DURATION)
        return if (expiredTime != null) {
            expiredTime < LocalDateTime.now()
        } else {
            true
        }
    }

}