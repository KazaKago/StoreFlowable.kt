package com.kazakago.cacheflowable.sample.flowable

import com.kazakago.cacheflowable.FlowableDataStateManager
import com.kazakago.cacheflowable.paging.AbstractPagingCacheFlowable
import com.kazakago.cacheflowable.sample.api.GithubApi
import com.kazakago.cacheflowable.sample.cache.GithubInMemoryCache
import com.kazakago.cacheflowable.sample.cache.GithubOrgsStateManager
import com.kazakago.cacheflowable.sample.model.GithubOrg
import java.util.*

class GithubOrgsFlowable : AbstractPagingCacheFlowable<Unit, GithubOrg>(Unit) {

    companion object {
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
        if (!additionalRequest) githubCache.orgsCreatedAtCache = Calendar.getInstance()
    }

    override suspend fun fetchOrigin(data: List<GithubOrg>?, additionalRequest: Boolean): List<GithubOrg> {
        val since = if (additionalRequest) data?.lastOrNull()?.id else null
        return githubApi.getOrgs(since, PER_PAGE)
    }

    override suspend fun needRefresh(data: List<GithubOrg>): Boolean {
        val expiredTime = githubCache.orgsCreatedAtCache?.apply {
            add(Calendar.MINUTE, 3)
        }
        return if (expiredTime != null) {
            expiredTime < Calendar.getInstance()
        } else {
            true
        }
    }

}