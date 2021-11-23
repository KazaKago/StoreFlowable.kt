package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableFactory
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubMetaStateManager
import com.kazakago.storeflowable.example.model.GithubMeta
import java.time.Duration
import java.time.LocalDateTime

class GithubMetaFlowableFactory : StoreFlowableFactory<Unit, GithubMeta> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = GithubMetaStateManager

    override suspend fun loadDataFromCache(param: Unit): GithubMeta? {
        return githubCache.metaCache
    }

    override suspend fun saveDataToCache(newData: GithubMeta?, param: Unit) {
        githubCache.metaCache = newData
        githubCache.metaCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun fetchDataFromOrigin(param: Unit): GithubMeta {
        return githubApi.getMeta()
    }

    override suspend fun needRefresh(cachedData: GithubMeta, param: Unit): Boolean {
        val createdAt = githubCache.metaCacheCreatedAt
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
