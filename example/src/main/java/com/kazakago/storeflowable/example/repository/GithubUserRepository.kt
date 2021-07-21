package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.example.flowable.GithubUserFlowableFactory
import com.kazakago.storeflowable.example.model.GithubUser

class GithubUserRepository {

    fun follow(userName: String): FlowLoadingState<GithubUser> {
        val githubUserFlowable = GithubUserFlowableFactory(userName).create()
        return githubUserFlowable.publish()
    }

    suspend fun refresh(userName: String) {
        val githubUserFlowable = GithubUserFlowableFactory(userName).create()
        githubUserFlowable.refresh()
    }
}
