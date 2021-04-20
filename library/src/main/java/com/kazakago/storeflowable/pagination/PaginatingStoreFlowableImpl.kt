package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.StateContent
import com.kazakago.storeflowable.mapState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform

internal class PaginatingStoreFlowableImpl<KEY, DATA>(private val storeFlowableCallback: PaginatingStoreFlowableCallback<KEY, DATA>) : PaginatingStoreFlowable<KEY, DATA> {

    private val dataSelector = PaginatingDataSelector(
        key = storeFlowableCallback.key,
        dataStateManager = storeFlowableCallback.flowableDataStateManager,
        cacheDataManager = storeFlowableCallback,
        originDataManager = storeFlowableCallback,
        needRefresh = { storeFlowableCallback.needRefresh(it) }
    )

    override fun publish(forceRefresh: Boolean): FlowableState<DATA> {
        return storeFlowableCallback.flowableDataStateManager.getFlow(storeFlowableCallback.key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = false, additionalRequest = false)
            }
            .map { dataState ->
                val data = dataSelector.load()
                val content = StateContent.wrap(data)
                dataState.mapState(content)
            }
    }

    override suspend fun getData(type: AsDataType): DATA? {
        return runCatching { requireData(type) }.getOrNull()
    }

    override suspend fun requireData(type: AsDataType): DATA {
        return storeFlowableCallback.flowableDataStateManager.getFlow(storeFlowableCallback.key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
                    AsDataType.FromCache -> Unit // do nothing.
                }
            }
            .transform { dataState ->
                val data = dataSelector.load()
                when (dataState) {
                    is DataState.Fixed -> if (data != null && !storeFlowableCallback.needRefresh(data)) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit // do nothing.
                    is DataState.Error -> if (data != null && !storeFlowableCallback.needRefresh(data)) emit(data) else throw dataState.exception
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
