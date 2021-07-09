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

    suspend fun load(): DATA? {
        return cacheDataManager.load()
    }

    suspend fun update(newData: DATA?) {
        cacheDataManager.save(newData)
        dataStateManager.save(key, DataState.Fixed(appendingDataState = AdditionalDataState.Fixed(), prependingDataState = AdditionalDataState.Fixed()))
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean, awaitFetching: Boolean, requestType: RequestType) {
        when (val state = dataStateManager.load(key)) {
            is DataState.Fixed -> when (requestType) {
                RequestType.Refresh -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                RequestType.Append -> when (state.appendingDataState) {
                    is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                    is AdditionalDataState.FixedWithNoMoreData -> Unit
                    is AdditionalDataState.Loading -> Unit
                    is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                }
                RequestType.Prepend -> when (state.prependingDataState) {
                    is AdditionalDataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                    is AdditionalDataState.FixedWithNoMoreData -> Unit
                    is AdditionalDataState.Loading -> Unit
                    is AdditionalDataState.Error -> if (continueWhenError) doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
                }
            }
            is DataState.Loading -> Unit
            is DataState.Error -> if (continueWhenError) doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: RequestType) {
        val cachedData = cacheDataManager.load()
        if (cachedData == null || forceRefresh || (requestType == RequestType.Refresh && needRefresh(cachedData)) || requestType != RequestType.Refresh) {
            prepareFetch(cachedData = cachedData, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, requestType = requestType)
        }
    }

    private suspend fun prepareFetch(cachedData: DATA?, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, requestType: RequestType) {
        if (clearCacheBeforeFetching) cacheDataManager.save(null)
        val state = dataStateManager.load(key)
        when (requestType) {
            RequestType.Refresh -> dataStateManager.save(key, DataState.Loading())
            RequestType.Append -> dataStateManager.save(key, DataState.Fixed(appendingDataState = AdditionalDataState.Loading(), prependingDataState = state.prependingDataState()))
            RequestType.Prepend -> dataStateManager.save(key, DataState.Fixed(appendingDataState = state.appendingDataState(), prependingDataState = AdditionalDataState.Loading()))
        }
        if (awaitFetching) {
            fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType)
        } else {
            CoroutineScope(defaultDispatcher).launch { fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, requestType = requestType) }
        }
    }

    private suspend fun fetchNewData(cachedData: DATA?, clearCacheWhenFetchFails: Boolean, requestType: RequestType) {
        try {
            val fetchingResult = when (requestType) {
                RequestType.Refresh -> originDataManager.fetch()
                RequestType.Append -> originDataManager.fetchAppending(cachedData)
                RequestType.Prepend -> originDataManager.fetchPrepending(cachedData)
            }
            when (requestType) {
                RequestType.Refresh -> cacheDataManager.save(fetchingResult.data)
                RequestType.Append -> cacheDataManager.saveAppending(cachedData, fetchingResult.data)
                RequestType.Prepend -> cacheDataManager.savePrepending(cachedData, fetchingResult.data)
            }
            val state = dataStateManager.load(key)
            when (requestType) {
                RequestType.Refresh -> dataStateManager.save(key, DataState.Fixed(appendingDataState = if (fetchingResult.noMoreAppendingData) AdditionalDataState.FixedWithNoMoreData() else AdditionalDataState.Fixed(), prependingDataState = if (fetchingResult.noMorePrependingData) AdditionalDataState.FixedWithNoMoreData() else AdditionalDataState.Fixed()))
                RequestType.Append -> dataStateManager.save(key, DataState.Fixed(appendingDataState = if (fetchingResult.noMoreAppendingData) AdditionalDataState.FixedWithNoMoreData() else AdditionalDataState.Fixed(), prependingDataState = state.prependingDataState()))
                RequestType.Prepend -> dataStateManager.save(key, DataState.Fixed(appendingDataState = state.appendingDataState(), prependingDataState = if (fetchingResult.noMorePrependingData) AdditionalDataState.FixedWithNoMoreData() else AdditionalDataState.Fixed()))
            }
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.save(null)
            val state = dataStateManager.load(key)
            when (requestType) {
                RequestType.Refresh -> dataStateManager.save(key, DataState.Error(exception))
                RequestType.Append -> dataStateManager.save(key, DataState.Fixed(appendingDataState = AdditionalDataState.Error(exception), prependingDataState = state.prependingDataState()))
                RequestType.Prepend -> dataStateManager.save(key, DataState.Fixed(appendingDataState = state.appendingDataState(), prependingDataState = AdditionalDataState.Error(exception)))
            }
        }
    }
}
