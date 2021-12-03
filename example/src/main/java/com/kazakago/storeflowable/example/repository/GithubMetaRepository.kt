package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.example.flowable.GithubMetaFlowableFactory
import com.kazakago.storeflowable.example.model.GithubMeta

class GithubMetaRepository {

    fun follow(): FlowLoadingState<GithubMeta> {
        val githubMetaFlowable = GithubMetaFlowableFactory().create(Unit)
        return githubMetaFlowable.publish()
    }

    suspend fun refresh() {
        val githubMetaFlowable = GithubMetaFlowableFactory().create(Unit)
        githubMetaFlowable.refresh()
    }
}
