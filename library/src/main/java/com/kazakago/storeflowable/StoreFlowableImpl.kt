package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.StateContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform

internal class StoreFlowableImpl<KEY, DATA>(
    private val key: KEY,
    private val flowableDataStateManager: FlowableDataStateManager<KEY>,
    cacheDataManager: CacheDataManager<DATA>,
    originDataManager: OriginDataManager<DATA>,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean),
) : StoreFlowable<KEY, DATA> {

    private val dataSelector = DataSelector(
        key = key,
        dataStateManager = flowableDataStateManager,
        cacheDataManager = cacheDataManager,
        originDataManager = originDataManager,
        needRefresh = needRefresh,
    )

    override fun publish(forceRefresh: Boolean): FlowableState<DATA> {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = false)
            }
            .map { dataState ->
                val data = dataSelector.load()
                val content = StateContent.wrap(data)
                dataState.mapState(content)
            }
    }

    override suspend fun getData(from: GettingFrom): DATA? {
        return runCatching { requireData(from) }.getOrNull()
    }

    override suspend fun requireData(from: GettingFrom): DATA {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                when (from) {
                    GettingFrom.Both, GettingFrom.Mix -> dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
                    GettingFrom.Origin, GettingFrom.FromOrigin -> dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
                    GettingFrom.Cache, GettingFrom.FromCache -> Unit // do nothing.
                }
            }
            .transform { dataState ->
                val data = dataSelector.load()
                when (dataState) {
                    is DataState.Fixed -> if (data != null && !needRefresh(data)) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit // do nothing.
                    is DataState.Error -> if (data != null && !needRefresh(data)) emit(data) else throw dataState.exception
                }
            }
            .first()
    }

    override suspend fun validate() {
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
    }

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = clearCacheWhenFetchFails, continueWhenError = continueWhenError, awaitFetching = true)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }
}
