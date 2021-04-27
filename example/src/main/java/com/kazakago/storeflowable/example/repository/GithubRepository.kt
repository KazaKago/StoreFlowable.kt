package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.example.flowable.GithubMetaFlowableCallback
import com.kazakago.storeflowable.example.flowable.GithubOrgsFlowableCallback
import com.kazakago.storeflowable.example.flowable.GithubReposFlowableCallback
import com.kazakago.storeflowable.example.flowable.GithubUserFlowableCallback
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.model.GithubUser

class GithubRepository {

    fun followMeta(): FlowableState<GithubMeta> {
        val githubMetaFlowable = GithubMetaFlowableCallback().create()
        return githubMetaFlowable.publish()
    }

    suspend fun refreshMeta() {
        val githubMetaFlowable = GithubMetaFlowableCallback().create()
        githubMetaFlowable.refresh()
    }

    fun followOrgs(): FlowableState<List<GithubOrg>> {
        val githubOrgsFlowable = GithubOrgsFlowableCallback().create()
        return githubOrgsFlowable.publish()
    }

    suspend fun refreshOrgs() {
        val githubOrgsFlowable = GithubOrgsFlowableCallback().create()
        githubOrgsFlowable.refresh()
    }

    suspend fun requestAdditionalOrgs(continueWhenError: Boolean) {
        val githubOrgsFlowable = GithubOrgsFlowableCallback().create()
        githubOrgsFlowable.requestAdditionalData(continueWhenError)
    }

    fun followUser(userName: String): FlowableState<GithubUser> {
        val githubUserFlowable = GithubUserFlowableCallback(userName).create()
        return githubUserFlowable.publish()
    }

    suspend fun refreshUser(userName: String) {
        val githubUserFlowable = GithubUserFlowableCallback(userName).create()
        githubUserFlowable.refresh()
    }

    fun followRepos(userName: String): FlowableState<List<GithubRepo>> {
        val githubReposFlowable = GithubReposFlowableCallback(userName).create()
        return githubReposFlowable.publish()
    }

    suspend fun refreshRepos(userName: String) {
        val githubReposFlowable = GithubReposFlowableCallback(userName).create()
        githubReposFlowable.refresh()
    }

    suspend fun requestAdditionalRepos(userName: String, continueWhenError: Boolean) {
        val githubReposFlowable = GithubReposFlowableCallback(userName).create()
        githubReposFlowable.requestAdditionalData(continueWhenError)
    }
}
