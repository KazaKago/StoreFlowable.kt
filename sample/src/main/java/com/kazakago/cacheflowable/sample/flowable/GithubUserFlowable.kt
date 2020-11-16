package com.kazakago.cacheflowable.sample.flowable

import com.kazakago.cacheflowable.AbstractCacheFlowable
import com.kazakago.cacheflowable.FlowableDataStateManager
import com.kazakago.cacheflowable.sample.api.GithubApi
import com.kazakago.cacheflowable.sample.cache.GithubInMemoryCache
import com.kazakago.cacheflowable.sample.cache.GithubUserStateManager
import com.kazakago.cacheflowable.sample.model.GithubUser
import java.util.*

class GithubUserFlowable(private val userName: String) : AbstractCacheFlowable<String, GithubUser>(userName) {

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubUserStateManager

    override suspend fun loadData(): GithubUser? {
        return githubCache.userCache[userName]
    }

    override suspend fun saveData(data: GithubUser?) {
        githubCache.userCache[userName] = data
        githubCache.userCreateAdCache[userName] = Calendar.getInstance()
    }

    override suspend fun fetchOrigin(): GithubUser {
        return githubApi.getUser(userName)
    }

    override suspend fun needRefresh(data: GithubUser): Boolean {
        val expiredTime = githubCache.userCreateAdCache[userName]?.apply {
            add(Calendar.MINUTE, 3)
        }
        return if (expiredTime != null) {
            expiredTime < Calendar.getInstance()
        } else {
            true
        }
    }

}