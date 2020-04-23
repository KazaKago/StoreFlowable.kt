package com.kazakago.cachesample.data.repository

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubRepoResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubRepoEntityMapper
import com.kazakago.cachesample.data.repository.dispatcher.GithubDispatcher
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.State
import kotlinx.coroutines.flow.Flow

class GithubRepository {
    private val githubDispatcher = GithubDispatcher(GithubApi(), GithubRepoResponseMapper(), GithubCache, GithubRepoEntityMapper())

    fun subscribeRepos(userName: String): Flow<State<List<GithubRepo>>> {
        return githubDispatcher(userName).subscribe()
    }

    suspend fun requestRepos(userName: String) {
        return githubDispatcher(userName).request()
    }

    suspend fun requestAdditionalRepos(userName: String, fetchOnError: Boolean) {
        return githubDispatcher(userName).requestAdditional(fetchOnError)
    }

}