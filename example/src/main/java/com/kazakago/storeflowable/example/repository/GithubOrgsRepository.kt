package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.flowable.GithubOrgsFlowableFactory
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.pagination.oneway.create

class GithubOrgsRepository {

    fun follow(): FlowLoadingState<List<GithubOrg>> {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create()
        return githubOrgsFlowable.publish()
    }

    suspend fun refresh() {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create()
        githubOrgsFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubOrgsFlowable = GithubOrgsFlowableFactory().create()
        githubOrgsFlowable.requestNextData(continueWhenError)
    }
}
