package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.cacher.GithubReposCacher
import com.kazakago.storeflowable.example.fetcher.GithubReposFetcher
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.from

class GithubReposRepository {

    fun follow(userName: String): FlowLoadingState<List<GithubRepo>> {
        val githubReposFlowable = StoreFlowable.from(GithubReposFetcher, GithubReposCacher, userName)
        return githubReposFlowable.publish()
    }

    suspend fun refresh(userName: String) {
        val githubReposFlowable = StoreFlowable.from(GithubReposFetcher, GithubReposCacher, userName)
        githubReposFlowable.refresh()
    }

    suspend fun requestNext(userName: String, continueWhenError: Boolean) {
        val githubReposFlowable = StoreFlowable.from(GithubReposFetcher, GithubReposCacher, userName)
        githubReposFlowable.requestNextData(continueWhenError)
    }
}
