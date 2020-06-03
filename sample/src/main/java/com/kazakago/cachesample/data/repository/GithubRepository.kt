package com.kazakago.cachesample.data.repository

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.mapContent
import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubRepoResponseMapper
import com.kazakago.cachesample.data.api.GithubUserResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubRepoEntityMapper
import com.kazakago.cachesample.data.cache.GithubUserEntityMapper
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.GithubUser
import kotlinx.coroutines.flow.Flow

class GithubRepository {
    private val githubApi = GithubApi()
    private val githubRepoResponseMapper = GithubRepoResponseMapper()
    private val githubUserResponseMapper = GithubUserResponseMapper()
    private val githubRepoEntityMapper = GithubRepoEntityMapper()
    private val githubUserEntityMapper = GithubUserEntityMapper()

    fun flowRepos(userName: String): Flow<State<List<GithubRepo>>> {
        return GithubReposFlowable(githubApi, githubRepoResponseMapper, GithubCache, userName).asFlow()
            .mapContent {
                it.map { githubRepoEntity -> githubRepoEntityMapper.map(githubRepoEntity) }
            }
    }

    suspend fun requestRepos(userName: String) {
        return GithubReposFlowable(githubApi, githubRepoResponseMapper, GithubCache, userName).request()
    }

    suspend fun requestAdditionalRepos(userName: String, fetchOnError: Boolean) {
        return GithubReposFlowable(githubApi, githubRepoResponseMapper, GithubCache, userName).requestAdditional(fetchOnError)
    }

    fun flowUser(userName: String): Flow<State<GithubUser>> {
        return GithubUserFlowable(githubApi, githubUserResponseMapper, GithubCache, userName).asFlow()
            .mapContent {
                githubUserEntityMapper.map(it)
            }
    }

    suspend fun requestUser(userName: String) {
        return GithubUserFlowable(githubApi, githubUserResponseMapper, GithubCache, userName).request()
    }

}