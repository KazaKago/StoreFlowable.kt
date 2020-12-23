package com.kazakago.storeflowable.sample.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableResponder
import com.kazakago.storeflowable.sample.api.GithubApi
import com.kazakago.storeflowable.sample.cache.GithubInMemoryCache
import com.kazakago.storeflowable.sample.cache.GithubUserStateManager
import com.kazakago.storeflowable.sample.model.GithubUser
import java.time.Duration
import java.time.LocalDateTime

class GithubUserResponder(override val key: String) : StoreFlowableResponder<String, GithubUser> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubUserStateManager

    override suspend fun loadData(): GithubUser? {
        return githubCache.userCache[key]
    }

    override suspend fun saveData(data: GithubUser?) {
        githubCache.userCache[key] = data
        githubCache.userCacheCreateAt[key] = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(): GithubUser {
        return githubApi.getUser(key)
    }

    override suspend fun needRefresh(data: GithubUser): Boolean {
        val expiredTime = githubCache.userCacheCreateAt[key]?.plus(EXPIRED_DURATION)
        return if (expiredTime != null) {
            expiredTime < LocalDateTime.now()
        } else {
            true
        }
    }
}
