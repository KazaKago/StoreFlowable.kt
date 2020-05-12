package com.kazakago.cachesample.data.repository

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubRepoResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubRepoEntity
import com.kazakago.cachesample.data.cache.state.PagingDataState
import com.kazakago.cachesample.data.cache.state.getOrCreate
import com.kazakago.cachesample.data.repository.dispatcher.PagingCacheStreamDispatcher
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class GithubReposDispatcher(
    private val githubApi: GithubApi,
    private val githubRepoResponseMapper: GithubRepoResponseMapper,
    private val githubCache: GithubCache,
    private val userName: String
) : PagingCacheStreamDispatcher<GithubRepoEntity>() {

    companion object {
        private const val PER_PAGE = 10
    }

    override fun loadDataStateFlow(): StateFlow<PagingDataState> {
        return githubCache.reposState.getOrCreate(userName)
    }

    override suspend fun saveDataState(state: PagingDataState) {
        githubCache.reposState.getOrCreate(userName).value = state
    }

    override suspend fun loadEntity(): List<GithubRepoEntity>? {
        return githubCache.reposCache[userName]
    }

    override suspend fun saveEntity(entity: List<GithubRepoEntity>?, additionalRequest: Boolean) {
        githubCache.reposCache[userName] = entity
        if (!additionalRequest) githubCache.reposCreateAdCache[userName] = Calendar.getInstance()
    }

    override suspend fun fetchOrigin(entity: List<GithubRepoEntity>?, additionalRequest: Boolean): List<GithubRepoEntity> {
        val page = if (additionalRequest) ((entity?.size ?: 0) / PER_PAGE + 1) else 1
        val response = githubApi.getRepos(userName, page, PER_PAGE)
        return response.map { githubRepoResponseMapper.map(it) }
    }

    override suspend fun needRefresh(entity: List<GithubRepoEntity>): Boolean {
        val expiredTime = githubCache.reposCreateAdCache.getOrCreate(userName).apply {
            add(Calendar.MINUTE, 3)
        }
        return expiredTime < Calendar.getInstance()
    }

}