package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubTwoWayReposStateManager
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.pagination.twoway.FetchedInitial
import com.kazakago.storeflowable.pagination.twoway.FetchedNext
import com.kazakago.storeflowable.pagination.twoway.FetchedPrev
import com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowableFactory
import java.time.Duration
import java.time.LocalDateTime

class GithubTwoWayReposFlowableFactory : TwoWayPaginationStoreFlowableFactory<Unit, List<GithubRepo>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val GITHUB_USER_NAME = "github"
        private const val INITIAL_PAGE = 4
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val flowableDataStateManager = GithubTwoWayReposStateManager

    override suspend fun loadDataFromCache(param: Unit): List<GithubRepo>? {
        return githubCache.twoWayReposCache
    }

    override suspend fun saveDataToCache(newData: List<GithubRepo>?, param: Unit) {
        githubCache.twoWayReposCache = newData
        githubCache.twoWayReposCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun saveNextDataToCache(cachedData: List<GithubRepo>, newData: List<GithubRepo>, param: Unit) {
        githubCache.twoWayReposCache = cachedData + newData
    }

    override suspend fun savePrevDataToCache(cachedData: List<GithubRepo>, newData: List<GithubRepo>, param: Unit) {
        githubCache.twoWayReposCache = newData + cachedData
    }

    override suspend fun fetchDataFromOrigin(param: Unit): FetchedInitial<List<GithubRepo>> {
        val data = githubApi.getRepos(GITHUB_USER_NAME, INITIAL_PAGE, PER_PAGE)
        return FetchedInitial(
            data = data,
            nextKey = if (data.isNotEmpty()) 5.toString() else null,
            prevKey = if (data.isNotEmpty()) 3.toString() else null,
        )
    }

    override suspend fun fetchNextDataFromOrigin(nextKey: String, param: Unit): FetchedNext<List<GithubRepo>> {
        val nextPage = nextKey.toInt()
        val data = githubApi.getRepos(GITHUB_USER_NAME, nextPage, PER_PAGE)
        return FetchedNext(
            data = data,
            nextKey = if (data.isNotEmpty()) (nextPage + 1).toString() else null,
        )
    }

    override suspend fun fetchPrevDataFromOrigin(prevKey: String, param: Unit): FetchedPrev<List<GithubRepo>> {
        val prevPage = prevKey.toInt()
        val data = githubApi.getRepos(GITHUB_USER_NAME, prevPage, PER_PAGE)
        return FetchedPrev(
            data = data,
            prevKey = if (prevPage > 1) (prevPage - 1).toString() else null,
        )
    }

    override suspend fun needRefresh(cachedData: List<GithubRepo>, param: Unit): Boolean {
        val createdAt = githubCache.twoWayReposCacheCreatedAt
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
