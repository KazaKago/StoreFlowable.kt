package com.kazakago.cacheflowable.sample.repository

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.sample.flowable.GithubMetaFlowable
import com.kazakago.cacheflowable.sample.flowable.GithubOrgsFlowable
import com.kazakago.cacheflowable.sample.flowable.GithubReposFlowable
import com.kazakago.cacheflowable.sample.flowable.GithubUserFlowable
import com.kazakago.cacheflowable.sample.model.GithubMeta
import com.kazakago.cacheflowable.sample.model.GithubOrg
import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.kazakago.cacheflowable.sample.model.GithubUser
import kotlinx.coroutines.flow.Flow

class GithubRepository {

    fun followMeta(): Flow<State<GithubMeta>> {
        return GithubMetaFlowable().asFlow()
    }

    suspend fun requestMeta() {
        return GithubMetaFlowable().request()
    }

    fun followOrgs(): Flow<State<List<GithubOrg>>> {
        return GithubOrgsFlowable().asFlow()
    }

    suspend fun requestOrgs() {
        return GithubOrgsFlowable().request()
    }

    suspend fun requestAdditionalOrgs(fetchAtError: Boolean) {
        return GithubOrgsFlowable().requestAdditional(fetchAtError)
    }

    fun followUser(userName: String): Flow<State<GithubUser>> {
        return GithubUserFlowable(userName).asFlow()
    }

    suspend fun requestUser(userName: String) {
        return GithubUserFlowable(userName).request()
    }

    fun followRepos(userName: String): Flow<State<List<GithubRepo>>> {
        return GithubReposFlowable(userName).asFlow()
    }

    suspend fun requestRepos(userName: String) {
        return GithubReposFlowable(userName).request()
    }

    suspend fun requestAdditionalRepos(userName: String, fetchAtError: Boolean) {
        return GithubReposFlowable(userName).requestAdditional(fetchAtError)
    }

}