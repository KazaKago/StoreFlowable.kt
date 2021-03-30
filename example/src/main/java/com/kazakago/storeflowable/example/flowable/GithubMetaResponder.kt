package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableResponder
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubInMemoryCache
import com.kazakago.storeflowable.example.cache.GithubMetaStateManager
import com.kazakago.storeflowable.example.model.GithubMeta
import java.time.Duration
import java.time.LocalDateTime

class GithubMetaResponder : StoreFlowableResponder<Unit, GithubMeta> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val key: Unit = Unit

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
        return githubCache.metaCacheCreatedAt?.let { createdAt ->
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } ?: true
    }
}
