package com.kazakago.storeflowable.sample.flowable

import com.kazakago.storeflowable.AbstractStoreFlowable
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.sample.api.GithubApi
import com.kazakago.storeflowable.sample.cache.GithubInMemoryCache
import com.kazakago.storeflowable.sample.cache.GithubUserStateManager
import com.kazakago.storeflowable.sample.model.GithubUser
import java.time.Duration
import java.time.LocalDateTime

class GithubUserFlowable(private val userName: String) : AbstractStoreFlowable<String, GithubUser>(userName) {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubUserStateManager

    override suspend fun loadData(): GithubUser? {
        return githubCache.userCache[userName]
    }

    override suspend fun saveData(data: GithubUser?) {
        githubCache.userCache[userName] = data
        githubCache.userCacheCreateAt[userName] = LocalDateTime.now()
    }

    override suspend fun fetchOrigin(): GithubUser {
        return githubApi.getUser(userName)
    }

    override suspend fun needRefresh(data: GithubUser): Boolean {
        val expiredTime = githubCache.userCacheCreateAt[userName]?.plus(EXPIRED_DURATION)
        return if (expiredTime != null) {
            expiredTime < LocalDateTime.now()
        } else {
            true
        }
    }

}