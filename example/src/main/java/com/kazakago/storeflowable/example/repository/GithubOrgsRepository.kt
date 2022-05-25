package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.cacher.GithubOrgsCacher
import com.kazakago.storeflowable.example.fetcher.GithubOrgsFetcher
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.from

class GithubOrgsRepository {

    fun follow(): FlowLoadingState<List<GithubOrg>> {
        val githubOrgsFlowable = StoreFlowable.from(GithubOrgsCacher, GithubOrgsFetcher)
        return githubOrgsFlowable.publish()
    }

    suspend fun refresh() {
        val githubOrgsFlowable = StoreFlowable.from(GithubOrgsCacher, GithubOrgsFetcher)
        githubOrgsFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubOrgsFlowable = StoreFlowable.from(GithubOrgsCacher, GithubOrgsFetcher)
        githubOrgsFlowable.requestNextData(continueWhenError)
    }
}
