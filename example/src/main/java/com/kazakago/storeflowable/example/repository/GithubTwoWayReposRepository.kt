package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.cacher.GithubTwoWayReposCacher
import com.kazakago.storeflowable.example.fetcher.GithubTwoWayReposFetcher
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.from

class GithubTwoWayReposRepository {

    fun follow(): FlowLoadingState<List<GithubRepo>> {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposCacher, GithubTwoWayReposFetcher)
        return githubReposFlowable.publish()
    }

    suspend fun refresh() {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposCacher, GithubTwoWayReposFetcher)
        githubReposFlowable.refresh()
    }

    suspend fun requestNext(continueWhenError: Boolean) {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposCacher, GithubTwoWayReposFetcher)
        githubReposFlowable.requestNextData(continueWhenError)
    }

    suspend fun requestPrev(continueWhenError: Boolean) {
        val githubReposFlowable = StoreFlowable.from(GithubTwoWayReposCacher, GithubTwoWayReposFetcher)
        githubReposFlowable.requestPrevData(continueWhenError)
    }

}
