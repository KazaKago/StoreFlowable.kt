package com.kazakago.cacheflowable.sample.data.repository

import com.kazakago.cacheflowable.AbstractCacheFlowable
import com.kazakago.cacheflowable.FlowableDataStateManager
import com.kazakago.cacheflowable.sample.data.api.GithubApi
import com.kazakago.cacheflowable.sample.data.api.GithubUserResponseMapper
import com.kazakago.cacheflowable.sample.data.cache.GithubCache
import com.kazakago.cacheflowable.sample.data.cache.GithubUserEntity
import com.kazakago.cacheflowable.sample.data.cache.GithubUserStateManager
import java.util.*

internal class GithubUserFlowable(
    private val githubApi: GithubApi,
    private val githubUserResponseMapper: GithubUserResponseMapper,
    private val githubCache: GithubCache,
    private val userName: String
) : AbstractCacheFlowable<String, GithubUserEntity>(userName) {

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubUserStateManager

    override suspend fun loadData(): GithubUserEntity? {
        return githubCache.userCache[userName]
    }

    override suspend fun saveData(data: GithubUserEntity?) {
        githubCache.userCache[userName] = data
        githubCache.userCreateAdCache[userName] = Calendar.getInstance()
    }

    override suspend fun fetchOrigin(): GithubUserEntity {
        val response = githubApi.getUser(userName)
        return githubUserResponseMapper.map(response)
    }

    override suspend fun needRefresh(data: GithubUserEntity): Boolean {
        val expiredTime = githubCache.userCreateAdCache.getOrCreate(userName).apply {
            add(Calendar.MINUTE, 3)
        }
        return expiredTime < Calendar.getInstance()
    }

}