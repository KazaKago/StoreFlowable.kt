package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.example.flowable.GithubMetaResponder
import com.kazakago.storeflowable.example.flowable.GithubOrgsResponder
import com.kazakago.storeflowable.example.flowable.GithubReposResponder
import com.kazakago.storeflowable.example.flowable.GithubUserResponder
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.pagination.create

class GithubRepository {

    fun followMeta(): FlowableState<GithubMeta> {
        val githubMetaFlowable = GithubMetaResponder().create()
        return githubMetaFlowable.publish()
    }

    suspend fun refreshMeta() {
        val githubMetaFlowable = GithubMetaResponder().create()
        githubMetaFlowable.refresh()
    }

    fun followOrgs(): FlowableState<List<GithubOrg>> {
        val githubOrgsFlowable = GithubOrgsResponder().create()
        return githubOrgsFlowable.publish()
    }

    suspend fun refreshOrgs() {
        val githubOrgsFlowable = GithubOrgsResponder().create()
        githubOrgsFlowable.refresh()
    }

    suspend fun requestAdditionalOrgs(continueWhenError: Boolean) {
        val githubOrgsFlowable = GithubOrgsResponder().create()
        githubOrgsFlowable.requestAddition(continueWhenError)
    }

    fun followUser(userName: String): FlowableState<GithubUser> {
        val githubUserFlowable = GithubUserResponder(userName).create()
        return githubUserFlowable.publish()
    }

    suspend fun refreshUser(userName: String) {
        val githubUserFlowable = GithubUserResponder(userName).create()
        githubUserFlowable.refresh()
    }

    fun followRepos(userName: String): FlowableState<List<GithubRepo>> {
        val githubReposFlowable = GithubReposResponder(userName).create()
        return githubReposFlowable.publish()
    }

    suspend fun refreshRepos(userName: String) {
        val githubReposFlowable = GithubReposResponder(userName).create()
        githubReposFlowable.refresh()
    }

    suspend fun requestAdditionalRepos(userName: String, continueWhenError: Boolean) {
        val githubReposFlowable = GithubReposResponder(userName).create()
        githubReposFlowable.requestAddition(continueWhenError)
    }
}
