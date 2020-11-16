package com.kazakago.cacheflowable.sample.flowable

import com.kazakago.cacheflowable.AbstractCacheFlowable
import com.kazakago.cacheflowable.FlowableDataStateManager
import com.kazakago.cacheflowable.sample.api.GithubApi
import com.kazakago.cacheflowable.sample.cache.GithubInMemoryCache
import com.kazakago.cacheflowable.sample.cache.GithubUserStateManager
import com.kazakago.cacheflowable.sample.model.GithubUser
import java.time.Duration
import java.time.LocalDateTime

class GithubUserFlowable(private val userName: String) : AbstractCacheFlowable<String, GithubUser>(userName) {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(3)
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