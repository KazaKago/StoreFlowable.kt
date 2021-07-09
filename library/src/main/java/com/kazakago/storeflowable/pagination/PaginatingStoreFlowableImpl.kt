package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.GettingFrom
import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.StateContent
import com.kazakago.storeflowable.mapState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform

internal class PaginatingStoreFlowableImpl<KEY, DATA>(
    private val key: KEY,
    private val flowableDataStateManager: FlowableDataStateManager<KEY>,
    cacheDataManager: PaginatingCacheDataManager<DATA>,
    originDataManager: PaginatingOriginDataManager<DATA>,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean),
) : PaginatingStoreFlowable<KEY, DATA> {

    private val dataSelector = PaginatingDataSelector(
        key = key,
        dataStateManager = flowableDataStateManager,
        cacheDataManager = cacheDataManager,
        originDataManager = originDataManager,
        needRefresh = needRefresh,
    )

    override fun publish(forceRefresh: Boolean): FlowableState<DATA> {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = false, additionalRequest = false)
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
                    GettingFrom.Both -> dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
                    GettingFrom.Origin -> dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
                    GettingFrom.Cache -> Unit
                }
            }
            .transform { dataState ->
                val data = dataSelector.load()
                when (dataState) {
                    is DataState.Fixed -> if (data != null && !needRefresh(data)) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit
                    is DataState.Error -> if (data != null && !needRefresh(data)) emit(data) else throw dataState.exception
                }
            }
            .first()
    }

    override suspend fun validate() {
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
    }

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = clearCacheWhenFetchFails, continueWhenError = continueWhenError, awaitFetching = true, additionalRequest = false)
    }

    override suspend fun requestAdditionalData(continueWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, continueWhenError = continueWhenError, awaitFetching = true, additionalRequest = true)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }
}
