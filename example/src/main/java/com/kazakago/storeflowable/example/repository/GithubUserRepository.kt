package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.example.flowable.GithubUserFlowableFactory
import com.kazakago.storeflowable.example.model.GithubUser
import kotlinx.coroutines.FlowPreview

class GithubUserRepository {

    @OptIn(FlowPreview::class)
    fun follow(userName: String): FlowLoadingState<GithubUser> {
        val githubUserFlowable = GithubUserFlowableFactory().create(userName)
        return githubUserFlowable.publish()
    }

    suspend fun refresh(userName: String) {
        val githubUserFlowable = GithubUserFlowableFactory().create(userName)
        githubUserFlowable.refresh()
    }
}
