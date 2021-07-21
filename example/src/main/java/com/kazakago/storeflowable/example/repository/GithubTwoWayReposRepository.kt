package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.flowable.GithubTwoWayReposFlowableFactory
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.twoway.create

class GithubTwoWayReposRepository {

    fun follow(): FlowLoadingState<List<GithubRepo>> {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        return githubReposFlowable.publish()
    }

    suspend fun refresh() {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        githubReposFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        githubReposFlowable.requestNextData(continueWhenError)
    }

    suspend fun requestPrev(continueWhenError: Boolean) {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create()
        githubReposFlowable.requestPrevData(continueWhenError)
    }

}
