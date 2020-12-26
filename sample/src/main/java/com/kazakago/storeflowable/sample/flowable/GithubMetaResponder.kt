package com.kazakago.storeflowable.sample.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableResponder
import com.kazakago.storeflowable.sample.api.GithubApi
import com.kazakago.storeflowable.sample.cache.GithubInMemoryCache
import com.kazakago.storeflowable.sample.cache.GithubMetaStateManager
import com.kazakago.storeflowable.sample.model.GithubMeta
import java.time.Duration
import java.time.LocalDateTime

class GithubMetaResponder(override val key: Unit = Unit) : StoreFlowableResponder<Unit, GithubMeta> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofSeconds(30)
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
