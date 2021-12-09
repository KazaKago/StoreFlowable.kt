package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.flowable.GithubOrgsFlowableFactory
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.pagination.oneway.create
import kotlinx.coroutines.FlowPreview

class GithubOrgsRepository {

    @OptIn(FlowPreview::class)
    fun follow(): FlowLoadingState<List<GithubOrg>> {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create(Unit)
        return githubOrgsFlowable.publish()
    }

    suspend fun refresh() {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create(Unit)
        githubOrgsFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create(Unit)
        githubOrgsFlowable.requestNextData(continueWhenError)
    }
}
