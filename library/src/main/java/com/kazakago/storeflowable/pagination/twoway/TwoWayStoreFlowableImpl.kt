package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.core.pagination.twoway.FlowableTwoWayLoadingState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.FlowableDataStateManager
import com.kazakago.storeflowable.logic.DataSelector
import com.kazakago.storeflowable.origin.GettingFrom
import com.kazakago.storeflowable.origin.OriginDataManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform

internal class TwoWayStoreFlowableImpl<KEY, DATA>(
    private val key: KEY,
    private val flowableDataStateManager: FlowableDataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    originDataManager: OriginDataManager<DATA>,
    needRefresh: (suspend (cachedData: DATA) -> Boolean),
) : TwoWayStoreFlowable<KEY, DATA> {

    private val dataSelector = DataSelector(
        key = key,
        dataStateManager = flowableDataStateManager,
        cacheDataManager = cacheDataManager,
        originDataManager = originDataManager,
        needRefresh = needRefresh,
    )

    override fun publish(forceRefresh: Boolean): FlowableTwoWayLoadingState<DATA> {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                dataSelector.validateAsync(forceRefresh = forceRefresh)
            }
            .map { dataState ->
                val data = cacheDataManager.load()
                dataState.toTwoWayLoadingState(data)
            }
    }

    override suspend fun getData(from: GettingFrom): DATA? {
        return runCatching { requireData(from) }.getOrNull()
    }

    override suspend fun requireData(from: GettingFrom): DATA {
        return flowableDataStateManager.getFlow(key)
            .onStart {
                when (from) {
                    GettingFrom.Both -> dataSelector.validate(forceRefresh = false)
                    GettingFrom.Origin -> dataSelector.validate(forceRefresh = true)
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
        dataSelector.validate(forceRefresh = false)
    }

    override suspend fun refresh() {
        dataSelector.refresh()
    }

    override suspend fun requestAppendingData(continueWhenError: Boolean) {
        dataSelector.requestAppendingData(continueWhenError = continueWhenError)
    }

    override suspend fun requestPrependingData(continueWhenError: Boolean) {
        dataSelector.requestPrependingData(continueWhenError = continueWhenError)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }
}
