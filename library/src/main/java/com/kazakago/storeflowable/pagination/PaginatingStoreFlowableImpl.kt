package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.StateContent
import com.kazakago.storeflowable.mapState
import kotlinx.coroutines.flow.*

internal class PaginatingStoreFlowableImpl<KEY, DATA>(private val storeFlowableResponder: PaginatingStoreFlowableResponder<KEY, DATA>) : PaginatingStoreFlowable<KEY, DATA> {

    private val dataSelector = PaginatingDataSelector(
        key = storeFlowableResponder.key,
        dataStateManager = storeFlowableResponder.flowableDataStateManager,
        cacheDataManager = storeFlowableResponder,
        originDataManager = storeFlowableResponder,
        needRefresh = { storeFlowableResponder.needRefresh(it) }
    )

    override fun publish(forceRefresh: Boolean): FlowableState<DATA> {
        return storeFlowableResponder.flowableDataStateManager.getFlow(storeFlowableResponder.key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = false, additionalRequest = false)
            }
            .map {
                val data = dataSelector.load()
                val content = StateContent.wrap(data)
                it.mapState(content)
            }
    }

    override suspend fun getData(type: AsDataType): DATA? {
        return prepareData(type).transform {
            val data = dataSelector.load()
            when (it) {
                is DataState.Fixed -> if (data != null && !storeFlowableResponder.needRefresh(data)) emit(data) else emit(null)
                is DataState.Loading -> Unit // do nothing.
                is DataState.Error -> if (data != null && !storeFlowableResponder.needRefresh(data)) emit(data) else emit(null)
            }
        }.first()
    }

    override suspend fun requireData(type: AsDataType): DATA {
        return prepareData(type).transform {
            val data = dataSelector.load()
            when (it) {
                is DataState.Fixed -> if (data != null && !storeFlowableResponder.needRefresh(data)) emit(data) else throw NoSuchElementException()
                is DataState.Loading -> Unit // do nothing.
                is DataState.Error -> if (data != null && !storeFlowableResponder.needRefresh(data)) emit(data) else throw it.exception
            }
        }.first()
    }

    private suspend fun prepareData(type: AsDataType): Flow<DataState> {
        return storeFlowableResponder.flowableDataStateManager.getFlow(storeFlowableResponder.key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
                    AsDataType.FromCache -> Unit // do nothing.
                }
            }
    }


    override suspend fun validate() {
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true, additionalRequest = false)
    }

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = clearCacheWhenFetchFails, continueWhenError = continueWhenError, awaitFetching = true, additionalRequest = false)
    }

    override suspend fun requestAddition(continueWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, continueWhenError = continueWhenError, awaitFetching = true, additionalRequest = true)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }
}
