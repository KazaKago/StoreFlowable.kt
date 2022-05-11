package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.cache.RequestKeyManager
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.exception.AdditionalRequestOnErrorStateException
import com.kazakago.storeflowable.exception.AdditionalRequestOnNullException
import com.kazakago.storeflowable.origin.OriginDataManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class DataSelector<DATA>(
    private val requestKeyManager: RequestKeyManager,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
    private val dataStateManager: DataStateManager,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean),
    asyncDispatcher: CoroutineDispatcher,
) {

    private val asyncCoroutineScope = CoroutineScope(SupervisorJob() + asyncDispatcher)

    suspend fun loadValidCacheOrNull(): DATA? {
        val data = cacheDataManager.load() ?: return null
        return if (!needRefresh(data)) data else null
    }

    suspend fun update(newData: DATA?, nextKey: String?, prevKey: String?) {
        cacheDataManager.save(newData)
        if (nextKey != null) requestKeyManager.saveNext(nextKey)
        if (prevKey != null) requestKeyManager.savePrev(prevKey)
        dataStateManager.save(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed()))
    }

    suspend fun clear() {
        cacheDataManager.save(null)
        requestKeyManager.saveNext(null)
        requestKeyManager.savePrev(null)
        dataStateManager.save(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed()))
    }

    suspend fun validate() {
        doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, requestType = RequestType.Refresh)
    }

    suspend fun validateAsync() {
        doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = false, requestType = RequestType.Refresh)
    }

    suspend fun refresh(clearCacheBeforeFetching: Boolean) {
        doStateAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, requestType = RequestType.Refresh)
    }

    suspend fun refreshAsync(clearCacheBeforeFetching: Boolean) {
        doStateAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = false, requestType = RequestType.Refresh)
    }

    suspend fun requestNextData(continueWhenError: Boolean) {
        doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, continueWhenError = continueWhenError, awaitFetching = true, requestType = RequestType.Next)
    }

    suspend fun requestPrevData(continueWhenError: Boolean) {
        doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, continueWhenError = continueWhenError, awaitFetching = true, requestType = RequestType.Prev)
    }

    private suspend fun doStateAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean, awaitFetching: Boolean, requestType: RequestType) {
        when (val state = dataStateManager.load()) {
            is DataState.Fixed -> when (requestType) {
                RequestType.Refresh -> {
                    if (state.nextDataState !is AdditionalDataState.Loading && state.prevDataState !is AdditionalDataState.Loading) {
                        doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Refresh)
                    }
                }
                RequestType.Next -> {
                    val nextKey = requestKeyManager.loadNext()
                    if (!nextKey.isNullOrEmpty()) {
                        when (state.nextDataState) {
                            is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Next(nextKey))
                            is AdditionalDataState.Loading -> Unit
                            is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Next(nextKey))
                        }
                    }
                }
                RequestType.Prev -> {
                    val prevKey = requestKeyManager.loadPrev()
                    if (!prevKey.isNullOrEmpty()) {
                        when (state.prevDataState) {
                            is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Prev(prevKey))
                            is AdditionalDataState.Loading -> Unit
                            is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Prev(prevKey))
                        }
                    }
                }
            }
            is DataState.Loading -> Unit
            is DataState.Error -> {
                when (requestType) {
                    RequestType.Refresh -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Refresh)
                    RequestType.Next, RequestType.Prev -> dataStateManager.save(DataState.Error(AdditionalRequestOnErrorStateException()))
                }
            }
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: KeyedRequestType) {
        val cachedData = cacheDataManager.load()
        when (requestType) {
            is KeyedRequestType.Refresh -> {
                if (cachedData == null || forceRefresh || needRefresh(cachedData)) {
                    prepareFetch(clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                }
            }
            is KeyedRequestType.Next, is KeyedRequestType.Prev -> {
                if (cachedData != null) {
                    prepareFetch(clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                } else {
                    dataStateManager.save(DataState.Error(AdditionalRequestOnNullException()))
                }
            }
        }
    }

    private suspend fun prepareFetch(clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: KeyedRequestType) {
        if (clearCacheBeforeFetching) cacheDataManager.save(null)
        val state = dataStateManager.load()
        when (requestType) {
            is KeyedRequestType.Refresh -> dataStateManager.save(DataState.Loading())
            is KeyedRequestType.Next -> dataStateManager.save(DataState.Fixed(AdditionalDataState.Loading(), state.prevDataState))
            is KeyedRequestType.Prev -> dataStateManager.save(DataState.Fixed(state.nextDataState, AdditionalDataState.Loading()))
        }
        if (awaitFetching) {
            fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType)
        } else {
            asyncCoroutineScope.launch { fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType) }
        }
    }

    private suspend fun fetchNewData(clearCacheWhenFetchFails: Boolean, requestType: KeyedRequestType) {
        try {
            when (requestType) {
                is KeyedRequestType.Refresh -> {
                    val result = originDataManager.fetch()
                    cacheDataManager.save(result.data)
                    requestKeyManager.saveNext(result.nextKey)
                    requestKeyManager.savePrev(result.prevKey)
                    dataStateManager.save(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed()))
                }
                is KeyedRequestType.Next -> {
                    val result = originDataManager.fetchNext(requestType.requestKey)
                    val cachedData = cacheDataManager.load() ?: throw AdditionalRequestOnNullException()
                    cacheDataManager.saveNext(cachedData, result.data)
                    requestKeyManager.saveNext(result.nextKey)
                    val state = dataStateManager.load()
                    dataStateManager.save(DataState.Fixed(AdditionalDataState.Fixed(), state.prevDataState))
                }
                is KeyedRequestType.Prev -> {
                    val result = originDataManager.fetchPrev(requestType.requestKey)
                    val cachedData = cacheDataManager.load() ?: throw AdditionalRequestOnNullException()
                    cacheDataManager.savePrev(cachedData, result.data)
                    requestKeyManager.savePrev(result.prevKey)
                    val state = dataStateManager.load()
                    dataStateManager.save(DataState.Fixed(state.nextDataState, AdditionalDataState.Fixed()))
                }
            }
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.save(null)
            val state = dataStateManager.load()
            when (requestType) {
                is KeyedRequestType.Refresh -> dataStateManager.save(DataState.Error(exception))
                is KeyedRequestType.Next -> dataStateManager.save(DataState.Fixed(AdditionalDataState.Error(exception), state.prevDataState))
                is KeyedRequestType.Prev -> dataStateManager.save(DataState.Fixed(state.nextDataState, AdditionalDataState.Error(exception)))
            }
        }
    }
}
