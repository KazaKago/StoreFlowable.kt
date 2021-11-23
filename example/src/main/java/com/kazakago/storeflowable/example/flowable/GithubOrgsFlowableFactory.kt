package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubOrgsStateManager
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.pagination.oneway.Fetched
import com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowableFactory
import java.time.Duration
import java.time.LocalDateTime

class GithubOrgsFlowableFactory : PaginationStoreFlowableFactory<Unit, List<GithubOrg>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = GithubOrgsStateManager

    override suspend fun loadDataFromCache(param: Unit): List<GithubOrg>? {
        return githubCache.orgsCache
    }

    override suspend fun saveDataToCache(newData: List<GithubOrg>?, param: Unit) {
        githubCache.orgsCache = newData
        githubCache.orgsCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun saveNextDataToCache(cachedData: List<GithubOrg>, newData: List<GithubOrg>, param: Unit) {
        githubCache.orgsCache = cachedData + newData
    }

    override suspend fun fetchDataFromOrigin(param: Unit): Fetched<List<GithubOrg>> {
        val data = githubApi.getOrgs(null, PER_PAGE)
        return Fetched(
            data = data,
            nextKey = data.lastOrNull()?.id?.toString(),
        )
    }

    override suspend fun fetchNextDataFromOrigin(nextKey: String, param: Unit): Fetched<List<GithubOrg>> {
        val data = githubApi.getOrgs(nextKey.toLong(), PER_PAGE)
        return Fetched(
            data = data,
            nextKey = data.lastOrNull()?.id?.toString(),
        )
    }

    override suspend fun needRefresh(cachedData: List<GithubOrg>, param: Unit): Boolean {
        val createdAt = githubCache.orgsCacheCreatedAt
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
