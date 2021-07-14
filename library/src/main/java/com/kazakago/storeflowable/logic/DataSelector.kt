package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.origin.OriginDataManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun loadValidCacheOrNull(): DATA? {
        val data = cacheDataManager.load() ?: return null
        return if (!needRefresh(data)) data else null
    }

    suspend fun update(newData: DATA?, nextKey: String?, prevKey: String?) {
        cacheDataManager.save(newData)
        val nextDataState = if (nextKey != null) {
            AdditionalDataState.Fixed(additionalRequestKey = nextKey)
        } else {
            val state = dataStateManager.load(key)
            state.nextKeyOrNull()?.let { AdditionalDataState.Fixed(additionalRequestKey = it) } ?: AdditionalDataState.FixedWithNoMoreAdditionalData()
        }
        val prevDataState = if (prevKey != null) {
            AdditionalDataState.Fixed(additionalRequestKey = prevKey)
        } else {
            val state = dataStateManager.load(key)
            state.prevKeyOrNull()?.let { AdditionalDataState.Fixed(additionalRequestKey = it) } ?: AdditionalDataState.FixedWithNoMoreAdditionalData()
        }
        dataStateManager.save(key, DataState.Fixed(nextDataState = nextDataState, prevDataState = prevDataState))
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
        when (val state = dataStateManager.load(key)) {
            is DataState.Fixed -> when (requestType) {
                RequestType.Refresh -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = ParameterizeRequestType.Refresh)
                RequestType.Next -> when (val nextDataState = state.nextDataState) {
                    is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = ParameterizeRequestType.Next(nextDataState.additionalRequestKey))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> Unit
                    is AdditionalDataState.Loading -> Unit
                    is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = ParameterizeRequestType.Next(nextDataState.additionalRequestKey))
                }
                RequestType.Prev -> when (val prevDataState = state.prevDataState) {
                    is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = ParameterizeRequestType.Prev(prevDataState.additionalRequestKey))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> Unit
                    is AdditionalDataState.Loading -> Unit
                    is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = ParameterizeRequestType.Prev(prevDataState.additionalRequestKey))
                }
            }
            is DataState.Loading -> Unit
            is DataState.Error -> when (requestType) {
                RequestType.Refresh -> if (continueWhenError) doDataAction(forceRefresh = true, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = ParameterizeRequestType.Refresh)
                RequestType.Next -> Unit
                RequestType.Prev -> Unit
            }
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: ParameterizeRequestType) {
        val cachedData = cacheDataManager.load()
        if (cachedData == null || forceRefresh || (requestType is ParameterizeRequestType.Refresh && needRefresh(cachedData)) || requestType !is ParameterizeRequestType.Refresh) {
            prepareFetch(cachedData = cachedData, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
        }
    }

    private suspend fun prepareFetch(cachedData: DATA?, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: ParameterizeRequestType) {
        if (clearCacheBeforeFetching) cacheDataManager.save(null)
        val state = dataStateManager.load(key)
        when (requestType) {
            is ParameterizeRequestType.Refresh -> dataStateManager.save(key, DataState.Loading())
            is ParameterizeRequestType.Next -> dataStateManager.save(key, DataState.Fixed(nextDataState = AdditionalDataState.Loading(requestType.requestKey), prevDataState = state.prevDataStateOrNull()))
            is ParameterizeRequestType.Prev -> dataStateManager.save(key, DataState.Fixed(nextDataState = state.nextDataStateOrNull(), prevDataState = AdditionalDataState.Loading(requestType.requestKey)))
        }
        if (awaitFetching) {
            fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType)
        } else {
            CoroutineScope(defaultDispatcher).launch { fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType) }
        }
    }

    private suspend fun fetchNewData(cachedData: DATA?, clearCacheWhenFetchFails: Boolean, requestType: ParameterizeRequestType) {
        try {
            val fetchingResult = when (requestType) {
                is ParameterizeRequestType.Refresh -> originDataManager.fetch()
                is ParameterizeRequestType.Next -> originDataManager.fetchNext(requestType.requestKey)
                is ParameterizeRequestType.Prev -> originDataManager.fetchPrev(requestType.requestKey)
            }
            when (requestType) {
                is ParameterizeRequestType.Refresh -> cacheDataManager.save(fetchingResult.data)
                is ParameterizeRequestType.Next -> cacheDataManager.saveNext(cachedData, fetchingResult.data)
                is ParameterizeRequestType.Prev -> cacheDataManager.savePrev(cachedData, fetchingResult.data)
            }
            val state = dataStateManager.load(key)
            when (requestType) {
                is ParameterizeRequestType.Refresh -> dataStateManager.save(key, DataState.Fixed(nextDataState = if (fetchingResult.nextKey.isNullOrEmpty()) AdditionalDataState.FixedWithNoMoreAdditionalData() else AdditionalDataState.Fixed(additionalRequestKey = fetchingResult.nextKey), prevDataState = if (fetchingResult.prevKey.isNullOrEmpty()) AdditionalDataState.FixedWithNoMoreAdditionalData() else AdditionalDataState.Fixed(additionalRequestKey = fetchingResult.prevKey)))
                is ParameterizeRequestType.Next -> dataStateManager.save(key, DataState.Fixed(nextDataState = if (fetchingResult.nextKey.isNullOrEmpty()) AdditionalDataState.FixedWithNoMoreAdditionalData() else AdditionalDataState.Fixed(additionalRequestKey = fetchingResult.nextKey), prevDataState = state.prevDataStateOrNull()))
                is ParameterizeRequestType.Prev -> dataStateManager.save(key, DataState.Fixed(nextDataState = state.nextDataStateOrNull(), prevDataState = if (fetchingResult.prevKey.isNullOrEmpty()) AdditionalDataState.FixedWithNoMoreAdditionalData() else AdditionalDataState.Fixed(additionalRequestKey = fetchingResult.prevKey)))
            }
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.save(null)
            val state = dataStateManager.load(key)
            when (requestType) {
                is ParameterizeRequestType.Refresh -> dataStateManager.save(key, DataState.Error(exception))
                is ParameterizeRequestType.Next -> dataStateManager.save(key, DataState.Fixed(nextDataState = AdditionalDataState.Error(requestType.requestKey, exception), prevDataState = state.prevDataStateOrNull()))
                is ParameterizeRequestType.Prev -> dataStateManager.save(key, DataState.Fixed(nextDataState = state.nextDataStateOrNull(), prevDataState = AdditionalDataState.Error(requestType.requestKey, exception)))
            }
        }
    }
}
