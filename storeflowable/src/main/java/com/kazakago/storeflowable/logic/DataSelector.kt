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

internal class DataSelector<PARAM, DATA>(
    private val param: PARAM,
    private val requestKeyManager: RequestKeyManager<PARAM>,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
    private val dataStateManager: DataStateManager<PARAM>,
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
        if (nextKey != null) requestKeyManager.saveNext(param, nextKey)
        if (prevKey != null) requestKeyManager.savePrev(param, prevKey)
        dataStateManager.save(param, DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed()))
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
        when (val state = dataStateManager.load(param)) {
            is DataState.Fixed -> when (requestType) {
                RequestType.Refresh -> {
                    if (state.nextDataState !is AdditionalDataState.Loading && state.prevDataState !is AdditionalDataState.Loading) {
                        doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Refresh)
                    }
                }
                RequestType.Next -> {
                    val nextKey = requestKeyManager.loadNext(param)
                    if (!nextKey.isNullOrEmpty()) {
                        when (state.nextDataState) {
                            is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Next(nextKey))
                            is AdditionalDataState.Loading -> Unit
                            is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = KeyedRequestType.Next(nextKey))
                        }
                    }
                }
                RequestType.Prev -> {
                    val prevKey = requestKeyManager.loadPrev(param)
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
                    RequestType.Next, RequestType.Prev -> dataStateManager.save(param, DataState.Error(AdditionalRequestOnErrorStateException()))
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
                    dataStateManager.save(param, DataState.Error(AdditionalRequestOnNullException()))
                }
            }
        }
    }

    private suspend fun prepareFetch(clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: KeyedRequestType) {
        if (clearCacheBeforeFetching) cacheDataManager.save(null)
        val state = dataStateManager.load(param)
        when (requestType) {
            is KeyedRequestType.Refresh -> dataStateManager.save(param, DataState.Loading())
            is KeyedRequestType.Next -> dataStateManager.save(param, DataState.Fixed(AdditionalDataState.Loading(), state.prevDataState))
            is KeyedRequestType.Prev -> dataStateManager.save(param, DataState.Fixed(state.nextDataState, AdditionalDataState.Loading()))
        }
        if (awaitFetching) {
            fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType)
        } else {
            asyncCoroutineScope.launch { fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType) }
        }
    }

    private suspend fun fetchNewData(clearCacheWhenFetchFails: Boolean, requestType: KeyedRequestType) {
        try {
            val result = when (requestType) {
                is KeyedRequestType.Refresh -> originDataManager.fetch()
                is KeyedRequestType.Next -> originDataManager.fetchNext(requestType.requestKey)
                is KeyedRequestType.Prev -> originDataManager.fetchPrev(requestType.requestKey)
            }
            when (requestType) {
                is KeyedRequestType.Refresh -> {
                    cacheDataManager.save(result.data)
                }
                is KeyedRequestType.Next -> {
                    val cachedData = cacheDataManager.load() ?: throw AdditionalRequestOnNullException()
                    cacheDataManager.saveNext(cachedData, result.data)
                }
                is KeyedRequestType.Prev -> {
                    val cachedData = cacheDataManager.load() ?: throw AdditionalRequestOnNullException()
                    cacheDataManager.savePrev(cachedData, result.data)
                }
            }
            val state = dataStateManager.load(param)
            when (requestType) {
                is KeyedRequestType.Refresh -> {
                    requestKeyManager.saveNext(param, result.nextKey)
                    requestKeyManager.savePrev(param, result.prevKey)
                    dataStateManager.save(param, DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed()))
                }
                is KeyedRequestType.Next -> {
                    requestKeyManager.saveNext(param, result.nextKey)
                    dataStateManager.save(param, DataState.Fixed(AdditionalDataState.Fixed(), state.prevDataState))
                }
                is KeyedRequestType.Prev -> {
                    requestKeyManager.savePrev(param, result.prevKey)
                    dataStateManager.save(param, DataState.Fixed(state.nextDataState, AdditionalDataState.Fixed()))
                }
            }
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.save(null)
            val state = dataStateManager.load(param)
            when (requestType) {
                is KeyedRequestType.Refresh -> dataStateManager.save(param, DataState.Error(exception))
                is KeyedRequestType.Next -> dataStateManager.save(param, DataState.Fixed(AdditionalDataState.Error(exception), state.prevDataState))
                is KeyedRequestType.Prev -> dataStateManager.save(param, DataState.Fixed(state.nextDataState, AdditionalDataState.Error(exception)))
            }
        }
    }
}
