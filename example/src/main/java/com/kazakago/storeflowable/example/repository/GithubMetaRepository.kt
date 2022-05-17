package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.cacher.GithubMetaCacher
import com.kazakago.storeflowable.example.fetcher.GithubMetaFetcher
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.from

class GithubMetaRepository {

    fun follow(): FlowLoadingState<GithubMeta> {
        val githubMetaFlowable = StoreFlowable.from(GithubMetaFetcher, GithubMetaCacher, Unit)
        return githubMetaFlowable.publish()
    }

    suspend fun refresh() {
        val githubMetaFlowable = StoreFlowable.from(GithubMetaFetcher, GithubMetaCacher, Unit)
        githubMetaFlowable.refresh()
    }
}
