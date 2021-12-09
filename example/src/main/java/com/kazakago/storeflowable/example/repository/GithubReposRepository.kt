package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.flowable.GithubReposFlowableFactory
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.oneway.create
import kotlinx.coroutines.FlowPreview

class GithubReposRepository {

    @OptIn(FlowPreview::class)
    fun follow(userName: String): FlowLoadingState<List<GithubRepo>> {
        val githubReposFlowable = GithubReposFlowableFactory().create(userName)
        return githubReposFlowable.publish()
    }

    suspend fun refresh(userName: String) {
        val githubReposFlowable = GithubReposFlowableFactory().create(userName)
        githubReposFlowable.refresh()
    }

    suspend fun requestNext(userName: String, continueWhenError: Boolean) {
        val githubReposFlowable = GithubReposFlowableFactory().create(userName)
        githubReposFlowable.requestNextData(continueWhenError)
    }
}
