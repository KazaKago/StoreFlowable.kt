package com.kazakago.cachesample.data.repository

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubRepoResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubRepoEntity
import com.kazakago.cachesample.data.cache.state.getOrCreate
import com.kazakago.cachesample.data.repository.dispatcher.PagingCacheStreamDispatcher
import java.util.*

class GithubReposDispatcher(
    private val githubApi: GithubApi,
    private val githubRepoResponseMapper: GithubRepoResponseMapper,
    private val githubCache: GithubCache
) {

    companion object {
        private const val PER_PAGE = 10
    }

    operator fun invoke(userName: String): PagingCacheStreamDispatcher<GithubRepoEntity> = PagingCacheStreamDispatcher(
        loadState = {
            githubCache.reposState.getOrCreate(userName)
        },
        saveState = {
            githubCache.reposState.getOrCreate(userName).value = it
        },
        loadEntity = {
            githubCache.reposCache[userName]
        },
        saveEntity = { entity, additionalRequest ->
            githubCache.reposCache[userName] = entity
            if (!additionalRequest) githubCache.reposCreateAdCache[userName] = Calendar.getInstance()
        },
        fetchOrigin = { entity, additionalRequest ->
            val page = if (additionalRequest) ((entity?.size ?: 0) / PER_PAGE + 1) else 1
            val response = githubApi.getRepos(userName, page, PER_PAGE)
            response.map { githubRepoResponseMapper.map(it) }
        },
        needRefresh = {
            val expiredTime = githubCache.reposCreateAdCache.getOrCreate(userName).apply {
                add(Calendar.MINUTE, 3)
            }
            expiredTime < Calendar.getInstance()
        }
    )
}