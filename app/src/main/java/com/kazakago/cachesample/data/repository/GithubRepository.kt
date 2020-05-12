package com.kazakago.cachesample.data.repository

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubRepoResponseMapper
import com.kazakago.cachesample.data.api.GithubUserResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubRepoEntityMapper
import com.kazakago.cachesample.data.cache.GithubUserEntityMapper
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.GithubUser
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.mapContent
import kotlinx.coroutines.flow.Flow

class GithubRepository {
    private val githubApi = GithubApi()
    private val githubRepoResponseMapper = GithubRepoResponseMapper()
    private val githubUserResponseMapper = GithubUserResponseMapper()
    private val githubRepoEntityMapper = GithubRepoEntityMapper()
    private val githubUserEntityMapper = GithubUserEntityMapper()

    fun subscribeRepos(userName: String): Flow<State<List<GithubRepo>>> {
        return GithubReposDispatcher(githubApi, githubRepoResponseMapper, GithubCache, userName).getFlow()
            .mapContent {
                it.map { githubRepoEntity -> githubRepoEntityMapper.map(githubRepoEntity) }
            }
    }

    suspend fun requestRepos(userName: String) {
        return GithubReposDispatcher(githubApi, githubRepoResponseMapper, GithubCache, userName).request()
    }

    suspend fun requestAdditionalRepos(userName: String, fetchOnError: Boolean) {
        return GithubReposDispatcher(githubApi, githubRepoResponseMapper, GithubCache, userName).requestAdditional(fetchOnError)
    }

    fun subscribeUser(userName: String): Flow<State<GithubUser>> {
        return GithubUserDispatcher(githubApi, githubUserResponseMapper, GithubCache, userName).getFlow()
            .mapContent {
                githubUserEntityMapper.map(it)
            }
    }

    suspend fun requestUser(userName: String) {
        return GithubUserDispatcher(githubApi, githubUserResponseMapper, GithubCache, userName).request()
    }

}