package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableCallback
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubUserStateManager
import com.kazakago.storeflowable.example.model.GithubUser
import java.time.Duration
import java.time.LocalDateTime

class GithubUserFlowableCallback(userName: String) : StoreFlowableCallback<String, GithubUser> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val key: String = userName

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubUserStateManager

    override suspend fun loadDataFromCache(): GithubUser? {
        return githubCache.userCache[key]
    }

    override suspend fun saveDataToCache(newData: GithubUser?) {
        githubCache.userCache[key] = newData
        githubCache.userCacheCreateAt[key] = LocalDateTime.now()
    }

    override suspend fun fetchDataFromOrigin(): FetchingResult<GithubUser> {
        val data = githubApi.getUser(key)
        return FetchingResult(data = data)
    }

    override suspend fun needRefresh(cachedData: GithubUser): Boolean {
        val createdAt = githubCache.userCacheCreateAt[key]
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
