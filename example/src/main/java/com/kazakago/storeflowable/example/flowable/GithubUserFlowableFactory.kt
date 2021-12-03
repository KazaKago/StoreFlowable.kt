package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableFactory
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubUserStateManager
import com.kazakago.storeflowable.example.model.GithubUser
import java.time.Duration
import java.time.LocalDateTime

class GithubUserFlowableFactory : StoreFlowableFactory<String, GithubUser> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val flowableDataStateManager: FlowableDataStateManager<String> = GithubUserStateManager

    override suspend fun loadDataFromCache(param: String): GithubUser? {
        return githubCache.userCache[param]
    }

    override suspend fun saveDataToCache(newData: GithubUser?, param: String) {
        githubCache.userCache[param] = newData
        githubCache.userCacheCreateAt[param] = LocalDateTime.now()
    }

    override suspend fun fetchDataFromOrigin(param: String): GithubUser {
        return githubApi.getUser(param)
    }

    override suspend fun needRefresh(cachedData: GithubUser, param: String): Boolean {
        val createdAt = githubCache.userCacheCreateAt[param]
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
