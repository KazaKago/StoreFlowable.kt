package com.kazakago.cachesample.data.repository

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubUserResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubUserEntity
import com.kazakago.cachesample.data.cache.state.getOrCreate
import com.kazakago.cachesample.data.repository.dispatcher.CacheStreamDispatcher
import java.util.*

class GithubUserDispatcher(
    private val githubApi: GithubApi,
    private val githubUserResponseMapper: GithubUserResponseMapper,
    private val githubCache: GithubCache
) {

    operator fun invoke(userName: String): CacheStreamDispatcher<GithubUserEntity> = CacheStreamDispatcher(
        loadState = {
            githubCache.userState.getOrCreate(userName)
        },
        saveState = {
            githubCache.userState.getOrCreate(userName).value = it
        },
        loadEntity = {
            githubCache.userCache[userName]
        },
        saveEntity = { entity ->
            githubCache.userCache[userName] = entity
            githubCache.userCreateAdCache[userName] = Calendar.getInstance()
        },
        fetchOrigin = {
            val response = githubApi.getUser(userName)
            githubUserResponseMapper.map(response)
        },
        needRefresh = {
            val expiredTime = githubCache.userCreateAdCache.getOrCreate(userName).apply {
                add(Calendar.MINUTE, 3)
            }
            expiredTime < Calendar.getInstance()
        }
    )
}