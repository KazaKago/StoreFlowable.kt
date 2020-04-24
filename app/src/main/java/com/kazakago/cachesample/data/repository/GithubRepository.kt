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
    private val githubReposDispatcher = GithubReposDispatcher(GithubApi(), GithubRepoResponseMapper(), GithubCache)
    private val githubUserDispatcher = GithubUserDispatcher(GithubApi(), GithubUserResponseMapper(), GithubCache)
    private val githubRepoEntityMapper = GithubRepoEntityMapper()
    private val githubUserEntityMapper = GithubUserEntityMapper()

    fun subscribeRepos(userName: String): Flow<State<List<GithubRepo>>> {
        return githubReposDispatcher(userName).subscribe()
            .mapContent {
                it.map { githubRepoEntity -> githubRepoEntityMapper.map(githubRepoEntity) }
            }
    }

    suspend fun requestRepos(userName: String) {
        return githubReposDispatcher(userName).request()
    }

    suspend fun requestAdditionalRepos(userName: String, fetchOnError: Boolean) {
        return githubReposDispatcher(userName).requestAdditional(fetchOnError)
    }

    fun subscribeUser(userName: String): Flow<State<GithubUser>> {
        return githubUserDispatcher(userName).subscribe()
            .mapContent {
                githubUserEntityMapper.map(it)
            }
    }

    suspend fun requestUser(userName: String) {
        return githubUserDispatcher(userName).request()
    }

}