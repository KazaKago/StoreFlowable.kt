package com.kazakago.storeflowable.example.repository

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.example.cacher.GithubUserCacher
import com.kazakago.storeflowable.example.fetcher.GithubUserFetcher
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.from

class GithubUserRepository {

    fun follow(userName: String): FlowLoadingState<GithubUser> {
        val githubUserFlowable = StoreFlowable.from(GithubUserCacher, GithubUserFetcher, userName)
        return githubUserFlowable.publish()
    }

    suspend fun refresh(userName: String) {
        val githubUserFlowable = StoreFlowable.from(GithubUserCacher, GithubUserFetcher, userName)
        githubUserFlowable.refresh()
    }
}
