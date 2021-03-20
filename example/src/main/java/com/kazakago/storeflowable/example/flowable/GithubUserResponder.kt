package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableResponder
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubInMemoryCache
import com.kazakago.storeflowable.example.cache.GithubUserStateManager
import com.kazakago.storeflowable.example.model.GithubUser
import java.time.Duration
import java.time.LocalDateTime

class GithubUserResponder(userName: String) : StoreFlowableResponder<String, GithubUser> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofSeconds(30)
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubInMemoryCache

    override val key: String = userName

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
        return githubCache.userCacheCreateAt[key]?.let { createdAt ->
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } ?: true
    }
}
