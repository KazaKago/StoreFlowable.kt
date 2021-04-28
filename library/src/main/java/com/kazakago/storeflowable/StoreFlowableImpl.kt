package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.StateContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform

internal class StoreFlowableImpl<KEY, DATA>(private val storeFlowableCallback: StoreFlowableCallback<KEY, DATA>) : StoreFlowable<KEY, DATA> {

    private val dataSelector = DataSelector(
        key = storeFlowableCallback.key,
        dataStateManager = storeFlowableCallback.flowableDataStateManager,
        cacheDataManager = storeFlowableCallback,
        originDataManager = storeFlowableCallback,
        needRefresh = { storeFlowableCallback.needRefresh(it) }
    )

    override fun publish(forceRefresh: Boolean): FlowableState<DATA> {
        return storeFlowableCallback.flowableDataStateManager.getFlow(storeFlowableCallback.key)
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
        return storeFlowableCallback.flowableDataStateManager.getFlow(storeFlowableCallback.key)
            .onStart {
                when (from) {
                    GettingFrom.Mix -> dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
                    GettingFrom.FromOrigin -> dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
                    GettingFrom.FromCache -> Unit // do nothing.
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
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
    }

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = clearCacheWhenFetchFails, continueWhenError = continueWhenError, awaitFetching = true)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }
}
