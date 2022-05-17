package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.cacher.GithubTwoWayReposCacher
import com.kazakago.storeflowable.example.fetcher.GithubTwoWayReposFetcher
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.from

class GithubTwoWayReposRepository {

    fun follow(): FlowLoadingState<List<GithubRepo>> {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposFetcher, GithubTwoWayReposCacher, Unit)
        return githubReposFlowable.publish()
    }

    suspend fun refresh() {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposFetcher, GithubTwoWayReposCacher, Unit)
        githubReposFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposFetcher, GithubTwoWayReposCacher, Unit)
        githubReposFlowable.requestNextData(continueWhenError)
    }

    suspend fun requestPrev(continueWhenError: Boolean) {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposFetcher, GithubTwoWayReposCacher, Unit)
        githubReposFlowable.requestPrevData(continueWhenError)
    }

}
