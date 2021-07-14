package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubTwoWayReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.FetchingResult
import com.kazakago.storeflowable.pagination.twoway.TwoWayFetchingResult
import com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowableFactory
import java.time.Duration
import java.time.LocalDateTime

class GithubTwoWayReposFlowableFactory : TwoWayStoreFlowableFactory<Unit, List<GithubRepo>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val GITHUB_USER_NAME = "github"
        private const val INITIAL_PAGE = 4
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val key = Unit

    override val flowableDataStateManager = GithubTwoWayReposStateManager

    override suspend fun loadDataFromCache(): List<GithubRepo>? {
        return githubCache.twoWayReposCache
    }

    override suspend fun saveDataToCache(newData: List<GithubRepo>?) {
        githubCache.twoWayReposCache = newData
        githubCache.twoWayReposCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun saveAppendingDataToCache(cachedData: List<GithubRepo>?, newData: List<GithubRepo>) {
        githubCache.twoWayReposCache = (cachedData ?: emptyList()) + newData
    }

    override suspend fun savePrependingDataToCache(cachedData: List<GithubRepo>?, newData: List<GithubRepo>) {
        githubCache.twoWayReposCache = newData + (cachedData ?: emptyList())
    }

    override suspend fun fetchDataFromOrigin(): TwoWayFetchingResult<List<GithubRepo>> {
        val data = githubApi.getRepos(GITHUB_USER_NAME, INITIAL_PAGE, PER_PAGE)
        githubCache.twoWayReposNextPage = INITIAL_PAGE + 1
        githubCache.twoWayReposPrevPage = INITIAL_PAGE - 1
        return TwoWayFetchingResult(data = data, noMoreAppendingData = data.isEmpty(), noMorePrependingData = data.isEmpty())
    }

    override suspend fun fetchAppendingDataFromOrigin(cachedData: List<GithubRepo>?): FetchingResult<List<GithubRepo>> {
        val data = githubApi.getRepos(GITHUB_USER_NAME, githubCache.twoWayReposNextPage, PER_PAGE)
        githubCache.twoWayReposNextPage += 1
        return FetchingResult(data = data, noMoreAdditionalData = data.isEmpty())
    }

    override suspend fun fetchPrependingDataFromOrigin(cachedData: List<GithubRepo>?): FetchingResult<List<GithubRepo>> {
        val data = githubApi.getRepos(GITHUB_USER_NAME, githubCache.twoWayReposPrevPage, PER_PAGE)
        githubCache.twoWayReposPrevPage -= 1
        return FetchingResult(data = data, noMoreAdditionalData = githubCache.twoWayReposPrevPage <= 0)
    }

    override suspend fun needRefresh(cachedData: List<GithubRepo>): Boolean {
        val createdAt = githubCache.twoWayReposCacheCreatedAt
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
