package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubTwoWayReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.twoway.FetchingNextResult
import com.kazakago.storeflowable.pagination.twoway.FetchingPrevResult
import com.kazakago.storeflowable.pagination.twoway.FetchingTwoWayResult
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

    override suspend fun saveNextDataToCache(cachedData: List<GithubRepo>?, newData: List<GithubRepo>) {
        githubCache.twoWayReposCache = (cachedData ?: emptyList()) + newData
    }

    override suspend fun savePrevDataToCache(cachedData: List<GithubRepo>?, newData: List<GithubRepo>) {
        githubCache.twoWayReposCache = newData + (cachedData ?: emptyList())
    }

    override suspend fun fetchDataFromOrigin(): FetchingTwoWayResult<List<GithubRepo>> {
        val data = githubApi.getRepos(GITHUB_USER_NAME, INITIAL_PAGE, PER_PAGE)
        return FetchingTwoWayResult(
            data = data,
            nextKey = if (data.isNotEmpty()) (INITIAL_PAGE + 1).toString() else null,
            prevKey = if (INITIAL_PAGE > 1) (INITIAL_PAGE - 1).toString() else null,
        )
    }

    override suspend fun fetchNextDataFromOrigin(nextKey: String): FetchingNextResult<List<GithubRepo>> {
        val nextPage = nextKey.toInt()
        val data = githubApi.getRepos(GITHUB_USER_NAME, nextPage, PER_PAGE)
        return FetchingNextResult(data = data, nextKey = if (data.isNotEmpty()) (nextPage + 1).toString() else null)
    }

    override suspend fun fetchPrevDataFromOrigin(prevKey: String): FetchingPrevResult<List<GithubRepo>> {
        val prevPage = prevKey.toInt()
        val data = githubApi.getRepos(GITHUB_USER_NAME, prevPage, PER_PAGE)
        return FetchingPrevResult(data = data, prevKey = if (prevPage > 1) (prevPage - 1).toString() else null)
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
