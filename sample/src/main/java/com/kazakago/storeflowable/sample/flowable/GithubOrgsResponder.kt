package com.kazakago.storeflowable.sample.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.paging.PagingStoreFlowableResponder
import com.kazakago.storeflowable.sample.api.GithubApi
import com.kazakago.storeflowable.sample.cache.GithubInMemoryCache
import com.kazakago.storeflowable.sample.cache.GithubOrgsStateManager
import com.kazakago.storeflowable.sample.model.GithubOrg
import java.time.Duration
import java.time.LocalDateTime

class GithubOrgsResponder(override val key: Unit = Unit) : PagingStoreFlowableResponder<Unit, GithubOrg> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = GithubOrgsStateManager

    override suspend fun loadData(): List<GithubOrg>? {
        return githubCache.orgsCache
    }

    override suspend fun saveData(data: List<GithubOrg>?, additionalRequest: Boolean) {
        githubCache.orgsCache = data
        if (!additionalRequest) githubCache.orgsCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(data: List<GithubOrg>?, additionalRequest: Boolean): List<GithubOrg> {
        val since = if (additionalRequest) data?.lastOrNull()?.id else null
        return githubApi.getOrgs(since, PER_PAGE)
    }

    override suspend fun needRefresh(data: List<GithubOrg>): Boolean {
        val expiredTime = githubCache.orgsCacheCreatedAt?.plus(EXPIRED_DURATION)
        return if (expiredTime != null) {
            expiredTime < LocalDateTime.now()
        } else {
            true
        }
    }
}
