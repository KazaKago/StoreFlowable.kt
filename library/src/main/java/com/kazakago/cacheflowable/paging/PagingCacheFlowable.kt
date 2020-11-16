package com.kazakago.cacheflowable.paging

import com.kazakago.cacheflowable.AsDataType
import com.kazakago.cacheflowable.DataState
import com.kazakago.cacheflowable.FlowAccessor
import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.StateContent
import com.kazakago.cacheflowable.mapState
import kotlinx.coroutines.flow.*

abstract class PagingCacheFlowable<KEY, DATA>(private val key: KEY) {

    internal abstract val flowAccessor: FlowAccessor<KEY>

    internal abstract val dataSelector: PagingDataSelector<KEY, DATA>

    fun asFlow(forceRefresh: Boolean = false): Flow<State<List<DATA>>> {
        return flowAccessor.getFlow(key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCache = true, fetchAtError = false, fetchAsync = true, additionalRequest = false)
            }
            .map {
                val data = dataSelector.load()
                val stateContent = StateContent.wrap(data)
                it.mapState(stateContent)
            }
    }

    suspend fun asData(type: AsDataType = AsDataType.Mix): List<DATA>? {
        return flowAccessor.getFlow(key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchAtError = false, fetchAsync = false, additionalRequest = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchAtError = false, fetchAsync = false, additionalRequest = false)
                    AsDataType.FromCache -> Unit //do nothing.
                }
            }
            .transform {
                val data = dataSelector.load()
                when (it) {
                    is DataState.Fixed -> emit(data)
                    is DataState.Loading -> Unit //do nothing.
                    is DataState.Error -> emit(data)
                }
            }
            .first()
    }

    suspend fun validate() {
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchAtError = false, fetchAsync = false, additionalRequest = false)
    }

    suspend fun request() {
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchAtError = true, fetchAsync = false, additionalRequest = false)
    }

    suspend fun requestAdditional(fetchAtError: Boolean = true) {
        return dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchAtError = fetchAtError, fetchAsync = false, additionalRequest = true)
    }

    suspend fun update(newData: List<DATA>?) {
        dataSelector.update(newData)
    }

}
