package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.GettingFrom
import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.origin.OriginDataManager
import com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowable
import com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

internal class StoreFlowableImpl<KEY, DATA>(
    private val key: KEY,
    private val flowableDataStateManager: FlowableDataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    originDataManager: OriginDataManager<DATA>,
    needRefresh: (suspend (cachedData: DATA) -> Boolean),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : StoreFlowable<KEY, DATA>, PaginationStoreFlowable<KEY, DATA>, TwoWayPaginationStoreFlowable<KEY, DATA> {

    private val dataSelector = DataSelector(
        key = key,
        dataStateManager = flowableDataStateManager,
        cacheDataManager = cacheDataManager,
        originDataManager = originDataManager,
        needRefresh = needRefresh,
    )

    override fun publish(forceRefresh: Boolean): FlowLoadingState<DATA> {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                CoroutineScope(defaultDispatcher).launch {
                    if (forceRefresh) {
                        dataSelector.refresh(clearCacheBeforeFetching = true)
                    } else {
                        dataSelector.validate()
                    }
                }
            }
            .transform { dataState ->
                val data = cacheDataManager.load()
                val state = dataState.toLoadingState(data)
                if (state != null) emit(state)
            }
    }

    override suspend fun getData(from: GettingFrom): DATA? {
        return runCatching { requireData(from) }.getOrNull()
    }

    override suspend fun requireData(from: GettingFrom): DATA {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                when (from) {
                    GettingFrom.Both -> dataSelector.validate()
                    GettingFrom.Origin -> dataSelector.refresh(clearCacheBeforeFetching = true)
                    GettingFrom.Cache -> Unit
                }
            }
            .transform { dataState ->
                val data = dataSelector.loadValidCacheOrNull()
                when (dataState) {
                    is DataState.Fixed -> if (data != null) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit
                    is DataState.Error -> if (data != null) emit(data) else throw dataState.exception
                }
            }
            .first()
    }

    override suspend fun validate() {
        dataSelector.validate()
    }

    override suspend fun refresh() {
        dataSelector.refresh(clearCacheBeforeFetching = false)
    }

    override suspend fun requestNextData(continueWhenError: Boolean) {
        dataSelector.requestNextData(continueWhenError = continueWhenError)
    }

    override suspend fun requestPrevData(continueWhenError: Boolean) {
        dataSelector.requestPrevData(continueWhenError = continueWhenError)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData, nextKey = null, prevKey = null)
    }

    override suspend fun update(newData: DATA?, nextKey: String?) {
        dataSelector.update(newData, nextKey = nextKey, prevKey = null)
    }

    override suspend fun update(newData: DATA?, nextKey: String?, prevKey: String?) {
        dataSelector.update(newData, nextKey = nextKey, prevKey = prevKey)
    }
}
