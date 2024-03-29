package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.GettingFrom
import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.cache.RequestKeyManager
import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateFlowAccessor
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.origin.OriginDataManager
import com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowable
import com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

internal class StoreFlowableImpl<DATA>(
    private val dataStateFlowAccessor: DataStateFlowAccessor,
    private val requestKeyManager: RequestKeyManager,
    private val cacheDataManager: CacheDataManager<DATA>,
    originDataManager: OriginDataManager<DATA>,
    dataStateManager: DataStateManager,
    needRefresh: (suspend (cachedData: DATA) -> Boolean),
    asyncDispatcher: CoroutineDispatcher,
) : StoreFlowable<DATA>, PaginationStoreFlowable<DATA>, TwoWayPaginationStoreFlowable<DATA> {

    private val dataSelector = DataSelector(
        requestKeyManager = requestKeyManager,
        cacheDataManager = cacheDataManager,
        originDataManager = originDataManager,
        dataStateManager = dataStateManager,
        needRefresh = needRefresh,
        asyncDispatcher = asyncDispatcher,
    )

    @FlowPreview
    override fun publish(forceRefresh: Boolean): FlowLoadingState<DATA> {
        return flow {
            if (forceRefresh) {
                dataSelector.refreshAsync(clearCacheBeforeFetching = true)
            } else {
                dataSelector.validateAsync()
            }
            emit(Unit)
        }.flatMapConcat {
            dataStateFlowAccessor.getFlow()
        }.map { dataState ->
            dataState.toLoadingState(
                content = cacheDataManager.load(),
                canNextRequest = (requestKeyManager.loadNext() != null),
                canPrevRequest = (requestKeyManager.loadPrev() != null),
            )
        }
    }

    @FlowPreview
    override suspend fun getData(from: GettingFrom): DATA? {
        return runCatching { requireData(from) }.getOrNull()
    }

    @FlowPreview
    override suspend fun requireData(from: GettingFrom): DATA {
        return flow {
            when (from) {
                GettingFrom.Both -> dataSelector.validate()
                GettingFrom.Origin -> dataSelector.refresh(clearCacheBeforeFetching = true)
                GettingFrom.Cache -> Unit
            }
            emit(Unit)
        }.flatMapConcat {
            dataStateFlowAccessor.getFlow()
        }.mapNotNull { dataState ->
            val data = dataSelector.loadValidCacheOrNull()
            when (dataState) {
                is DataState.Fixed -> data ?: throw NoSuchElementException()
                is DataState.Loading -> null
                is DataState.Error -> data ?: throw dataState.exception
            }
        }.first()
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

    override suspend fun clear() {
        dataSelector.clear()
    }
}
