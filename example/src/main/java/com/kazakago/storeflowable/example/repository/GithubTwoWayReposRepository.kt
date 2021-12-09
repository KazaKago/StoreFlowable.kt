package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.flowable.GithubTwoWayReposFlowableFactory
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.twoway.create
import kotlinx.coroutines.FlowPreview

class GithubTwoWayReposRepository {

    @OptIn(FlowPreview::class)
    fun follow(): FlowLoadingState<List<GithubRepo>> {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create(Unit)
        return githubReposFlowable.publish()
    }

    suspend fun refresh() {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create(Unit)
        githubReposFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create(Unit)
        githubReposFlowable.requestNextData(continueWhenError)
    }

    suspend fun requestPrev(continueWhenError: Boolean) {
        val githubReposFlowable = GithubTwoWayReposFlowableFactory().create(Unit)
        githubReposFlowable.requestPrevData(continueWhenError)
    }

}
