package com.kazakago.storeflowable.sample.repository

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.paging.create
import com.kazakago.storeflowable.sample.flowable.GithubMetaResponder
import com.kazakago.storeflowable.sample.flowable.GithubOrgsResponder
import com.kazakago.storeflowable.sample.flowable.GithubReposResponder
import com.kazakago.storeflowable.sample.flowable.GithubUserResponder
import com.kazakago.storeflowable.sample.model.GithubMeta
import com.kazakago.storeflowable.sample.model.GithubOrg
import com.kazakago.storeflowable.sample.model.GithubRepo
import com.kazakago.storeflowable.sample.model.GithubUser
import kotlinx.coroutines.flow.Flow

class GithubRepository {

    fun followMeta(): Flow<State<GithubMeta>> {
        val githubMetaFlowable = GithubMetaResponder().create()
        return githubMetaFlowable.asFlow()
    }

    suspend fun refreshMeta() {
        val githubMetaFlowable = GithubMetaResponder().create()
        githubMetaFlowable.refresh()
    }

    fun followOrgs(): Flow<State<List<GithubOrg>>> {
        val githubOrgsFlowable = GithubOrgsResponder().create()
        return githubOrgsFlowable.asFlow()
    }

    suspend fun refreshOrgs() {
        val githubOrgsFlowable = GithubOrgsResponder().create()
        githubOrgsFlowable.refresh()
    }

    suspend fun requestAdditionalOrgs(continueWhenError: Boolean) {
        val githubOrgsFlowable = GithubOrgsResponder().create()
        githubOrgsFlowable.requestAdditional(continueWhenError)
    }

    fun followUser(userName: String): Flow<State<GithubUser>> {
        val githubUserFlowable = GithubUserResponder(userName).create()
        return githubUserFlowable.asFlow()
    }

    suspend fun refreshUser(userName: String) {
        val githubUserFlowable = GithubUserResponder(userName).create()
        githubUserFlowable.refresh()
    }

    fun followRepos(userName: String): Flow<State<List<GithubRepo>>> {
        val githubReposFlowable = GithubReposResponder(userName).create()
        return githubReposFlowable.asFlow()
    }

    suspend fun refreshRepos(userName: String) {
        val githubReposFlowable = GithubReposResponder(userName).create()
        githubReposFlowable.refresh()
    }

    suspend fun requestAdditionalRepos(userName: String, continueWhenError: Boolean) {
        val githubReposFlowable = GithubReposResponder(userName).create()
        githubReposFlowable.requestAdditional(continueWhenError)
    }
}
