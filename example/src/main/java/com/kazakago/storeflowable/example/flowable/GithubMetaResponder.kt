package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableCallback
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubMetaStateManager
import com.kazakago.storeflowable.example.model.GithubMeta
import java.time.Duration
import java.time.LocalDateTime

class GithubMetaResponder : StoreFlowableCallback<Unit, GithubMeta> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val key: Unit = Unit

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = GithubMetaStateManager

    override suspend fun loadData(): GithubMeta? {
        return githubCache.metaCache
    }

    override suspend fun saveData(newData: GithubMeta?) {
        githubCache.metaCache = newData
        githubCache.metaCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(): FetchingResult<GithubMeta> {
        val data = githubApi.getMeta()
        return FetchingResult(data = data)
    }

    override suspend fun needRefresh(cachedData: GithubMeta): Boolean {
        val createdAt = githubCache.metaCacheCreatedAt
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
