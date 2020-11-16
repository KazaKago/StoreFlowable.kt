package com.kazakago.cacheflowable.sample.flowable

import com.kazakago.cacheflowable.AbstractCacheFlowable
import com.kazakago.cacheflowable.FlowableDataStateManager
import com.kazakago.cacheflowable.sample.api.GithubApi
import com.kazakago.cacheflowable.sample.cache.GithubInMemoryCache
import com.kazakago.cacheflowable.sample.cache.GithubMetaStateManager
import com.kazakago.cacheflowable.sample.model.GithubMeta
import java.time.Duration
import java.time.LocalDateTime

class GithubMetaFlowable : AbstractCacheFlowable<Unit, GithubMeta>(Unit) {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(3)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = GithubMetaStateManager

    override suspend fun loadData(): GithubMeta? {
        return githubCache.metaCache
    }

    override suspend fun saveData(data: GithubMeta?) {
        githubCache.metaCache = data
        githubCache.metaCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(): GithubMeta {
        return githubApi.getMeta()
    }

    override suspend fun needRefresh(data: GithubMeta): Boolean {
        val expiredTime = githubCache.metaCacheCreatedAt?.plus(EXPIRED_DURATION)
        return if (expiredTime != null) {
            expiredTime < LocalDateTime.now()
        } else {
            true
        }
    }

}