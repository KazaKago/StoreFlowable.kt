package com.kazakago.cacheflowable.sample.data.repository

import com.kazakago.cacheflowable.FlowableDataStateManager
import com.kazakago.cacheflowable.paging.AbstractPagingCacheFlowable
import com.kazakago.cacheflowable.sample.data.api.GithubApi
import com.kazakago.cacheflowable.sample.data.api.GithubRepoResponseMapper
import com.kazakago.cacheflowable.sample.data.cache.GithubCache
import com.kazakago.cacheflowable.sample.data.cache.GithubRepoEntity
import com.kazakago.cacheflowable.sample.data.cache.GithubReposStateManager
import java.util.*

internal class GithubReposFlowable(
    private val githubApi: GithubApi,
    private val githubRepoResponseMapper: GithubRepoResponseMapper,
    private val githubCache: GithubCache,
    private val userName: String
) : AbstractPagingCacheFlowable<String, GithubRepoEntity>(userName) {

    companion object {
        private const val PER_PAGE = 10
    }

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubReposStateManager

    override suspend fun loadData(): List<GithubRepoEntity>? {
        return githubCache.reposCache[userName]
    }

    override suspend fun saveData(data: List<GithubRepoEntity>?, additionalRequest: Boolean) {
        githubCache.reposCache[userName] = data
        if (!additionalRequest) githubCache.reposCreateAdCache[userName] = Calendar.getInstance()
    }

    override suspend fun fetchOrigin(data: List<GithubRepoEntity>?, additionalRequest: Boolean): List<GithubRepoEntity> {
        val page = if (additionalRequest) ((data?.size ?: 0) / PER_PAGE + 1) else 1
        val response = githubApi.getRepos(userName, page, PER_PAGE)
        return response.map { githubRepoResponseMapper.map(it) }
    }

    override suspend fun needRefresh(data: List<GithubRepoEntity>): Boolean {
        val expiredTime = githubCache.reposCreateAdCache.getOrCreate(userName).apply {
            add(Calendar.MINUTE, 3)
        }
        return expiredTime < Calendar.getInstance()
    }

}