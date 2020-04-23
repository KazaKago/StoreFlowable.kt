package com.kazakago.cachesample.data.repository.dispatcher

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubRepoResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubRepoEntityMapper
import com.kazakago.cachesample.data.cache.getOrCreate
import com.kazakago.cachesample.domain.model.GithubRepo
import kotlinx.coroutines.flow.asFlow
import java.util.*

class GithubDispatcher(
    private val githubApi: GithubApi,
    private val githubRepoResponseMapper: GithubRepoResponseMapper,
    private val githubCache: GithubCache,
    private val githubRepoEntityMapper: GithubRepoEntityMapper
) {

    companion object {
        private const val PER_PAGE = 10
    }

    operator fun invoke(userName: String): PagingCacheStreamDispatcher<GithubRepo> = PagingCacheStreamDispatcher(
        loadState = {
            githubCache.reposState.getOrCreate(userName).asFlow()
        },
        saveState = {
            githubCache.reposState.getOrCreate(userName).send(it)
        },
        loadEntity = {
            githubCache.reposCache[userName]?.map { githubRepoEntityMapper.map(it) }
        },
        saveEntity = { entity, additionalRequest ->
            githubCache.reposCache[userName] = entity?.map { githubRepoEntityMapper.reverse(it) }
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