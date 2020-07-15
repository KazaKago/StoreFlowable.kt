package com.kazakago.cacheflowable.paging

import com.kazakago.cacheflowable.AsDataType
import com.kazakago.cacheflowable.DataState
import com.kazakago.cacheflowable.FlowAccessor
import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.StateContent
import com.kazakago.cacheflowable.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class PagingCacheFlowable<KEY, DATA>(private val key: KEY) {

    internal abstract val flowAccessor: FlowAccessor<KEY>

    internal abstract val dataSelector: PagingDataSelector<KEY, DATA>

    @ExperimentalCoroutinesApi
    fun asFlow(forceRefresh: Boolean = false): Flow<State<List<DATA>>> {
        return flowAccessor.getFlow(key)
            .onStart {
                CoroutineScope(Dispatchers.IO).launch {
                    dataSelector.doStateAction(forceRefresh, clearCache = true, fetchOnError = false, additionalRequest = false)
                }
            }
            .map {
                val data = dataSelector.load()
                val stateContent = StateContent.wrap(data)
                it.mapState(stateContent)
            }
    }

    @ExperimentalCoroutinesApi
    suspend fun asData(type: AsDataType = AsDataType.Mix): List<DATA>? {
        return flowAccessor.getFlow(key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false, additionalRequest = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false, additionalRequest = false)
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
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false, additionalRequest = false)
    }

    suspend fun request() {
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true, additionalRequest = false)
    }

    suspend fun requestAdditional(fetchOnError: Boolean = true) {
        return dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = fetchOnError, additionalRequest = true)
    }

    suspend fun update(newData: List<DATA>?) {
        dataSelector.update(newData)
    }

}
