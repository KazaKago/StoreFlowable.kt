package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent
import com.kazakago.storeflowable.mapState
import kotlinx.coroutines.flow.*

internal class PagingStoreFlowableImpl<KEY, DATA>(private val storeFlowableResponder: PagingStoreFlowableResponder<KEY, DATA>) : PagingStoreFlowable<KEY, DATA> {

    private val dataSelector = PagingDataSelector(
        key = storeFlowableResponder.key,
        dataStateManager = storeFlowableResponder.flowableDataStateManager,
        cacheDataManager = storeFlowableResponder,
        originDataManager = storeFlowableResponder,
        needRefresh = { storeFlowableResponder.needRefresh(it) }
    )

    override fun asFlow(forceRefresh: Boolean): Flow<State<List<DATA>>> {
        return storeFlowableResponder.flowableDataStateManager.getFlow(storeFlowableResponder.key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = true, fetchWhenError = true, fetchAsync = true, additionalRequest = false)
            }
            .map {
                val data = dataSelector.load()
                val stateContent = StateContent.wrap(data)
                it.mapState(stateContent)
            }
    }

    override suspend fun get(type: AsDataType): List<DATA> {
        return storeFlowableResponder.flowableDataStateManager.getFlow(storeFlowableResponder.key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, fetchWhenError = true, fetchAsync = false, additionalRequest = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, fetchWhenError = true, fetchAsync = false, additionalRequest = false)
                    AsDataType.FromCache -> Unit //do nothing.
                }
            }
            .transform {
                val data = dataSelector.load()
                when (it) {
                    is DataState.Fixed -> if (data != null && !storeFlowableResponder.needRefresh(data)) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit //do nothing.
                    is DataState.Error -> if (data != null && !storeFlowableResponder.needRefresh(data)) emit(data) else throw it.exception
                }
            }
            .first()
    }

    override suspend fun validate() {
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, fetchWhenError = true, fetchAsync = false, additionalRequest = false)
    }

    override suspend fun request(fetchWhenError: Boolean) {
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, fetchWhenError = fetchWhenError, fetchAsync = false, additionalRequest = false)
    }

    override suspend fun requestAdditional(fetchWhenError: Boolean) {
        return dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, fetchWhenError = fetchWhenError, fetchAsync = false, additionalRequest = true)
    }

    override suspend fun update(newData: List<DATA>?) {
        dataSelector.update(newData)
    }
}
