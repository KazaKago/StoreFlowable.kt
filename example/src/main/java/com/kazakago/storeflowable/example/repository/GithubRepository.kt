package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowableLoadingState
import com.kazakago.storeflowable.core.pagination.oneway.FlowableOneWayLoadingState
import com.kazakago.storeflowable.core.pagination.twoway.FlowableTwoWayLoadingState
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.example.flowable.*
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.pagination.oneway.create
import com.kazakago.storeflowable.pagination.twoway.create

class GithubRepository {

    fun followMeta(): FlowableLoadingState<GithubMeta> {
        val githubMetaFlowable = GithubMetaFlowableFactory().create()
        return githubMetaFlowable.publish()
    }

    suspend fun refreshMeta() {
        val githubMetaFlowable = GithubMetaFlowableFactory().create()
        githubMetaFlowable.refresh()
    }

    fun followOrgs(): FlowableOneWayLoadingState<List<GithubOrg>> {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create()
        return githubOrgsFlowable.publish()
    }

    suspend fun refreshOrgs() {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create()
        githubOrgsFlowable.refresh()
    }

    suspend fun requestAdditionalOrgs(continueWhenError: Boolean) {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create()
        githubOrgsFlowable.requestNextData(continueWhenError)
    }

    fun followUser(userName: String): FlowableLoadingState<GithubUser> {
        val githubUserFlowable = GithubUserFlowableFactory(userName).create()
        return githubUserFlowable.publish()
    }

    suspend fun refreshUser(userName: String) {
        val githubUserFlowable = GithubUserFlowableFactory(userName).create()
        githubUserFlowable.refresh()
    }

    fun followRepos(userName: String): FlowableOneWayLoadingState<List<GithubRepo>> {
        val githubReposFlowable = GithubReposFlowableFactory(userName).create()
        return githubReposFlowable.publish()
    }

    suspend fun refreshRepos(userName: String) {
        val githubReposFlowable = GithubReposFlowableFactory(userName).create()
        githubReposFlowable.refresh()
    }

    suspend fun requestNextRepos(userName: String, continueWhenError: Boolean) {
        val githubReposFlowable = GithubReposFlowableFactory(userName).create()
        githubReposFlowable.requestNextData(continueWhenError)
    }

    fun followTwoWayRepos(): FlowableTwoWayLoadingState<List<GithubRepo>> {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        return githubReposFlowable.publish()
    }

    suspend fun refreshTwoWayRepos() {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        githubReposFlowable.refresh()
    }

    suspend fun requestTwoWayNextRepos(continueWhenError: Boolean) {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        githubReposFlowable.requestNextData(continueWhenError)
    }

    suspend fun requestTwoWayPrevRepos(continueWhenError: Boolean) {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        githubReposFlowable.requestPrevData(continueWhenError)
    }

}
