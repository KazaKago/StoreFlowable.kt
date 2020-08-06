package com.kazakago.cacheflowable.sample.repository

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.kazakago.cacheflowable.sample.model.GithubUser
import kotlinx.coroutines.flow.Flow

class GithubRepository {

    fun flowRepos(userName: String): Flow<State<List<GithubRepo>>> {
        return GithubReposFlowable(userName).asFlow()
    }

    suspend fun requestRepos(userName: String) {
        return GithubReposFlowable(userName).request()
    }

    suspend fun requestAdditionalRepos(userName: String, fetchOnError: Boolean) {
        return GithubReposFlowable(userName).requestAdditional(fetchOnError)
    }

    fun flowUser(userName: String): Flow<State<GithubUser>> {
        return GithubUserFlowable(userName).asFlow()
    }

    suspend fun requestUser(userName: String) {
        return GithubUserFlowable(userName).request()
    }

}