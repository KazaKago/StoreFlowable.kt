package com.kazakago.cacheflowable

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class CacheFlowable<KEY, DATA>(private val key: KEY) {

    internal abstract val flowAccessor: FlowAccessor<KEY>

    internal abstract val dataSelector: DataSelector<KEY, DATA>

    @ExperimentalCoroutinesApi
    fun asFlow(forceRefresh: Boolean = false): Flow<State<DATA>> {
        return flowAccessor.getFlow(key)
            .onStart {
                CoroutineScope(Dispatchers.IO).launch {
                    dataSelector.doStateAction(forceRefresh, clearCache = true, fetchOnError = false)
                }
            }
            .map {
                val data = dataSelector.load()
                val stateContent = StateContent.wrap(data)
                it.mapState(stateContent)
            }
    }

    @ExperimentalCoroutinesApi
    suspend fun asData(type: AsDataType = AsDataType.Mix): DATA? {
        return flowAccessor.getFlow(key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
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
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
    }

    suspend fun request() {
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
    }

    suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }

}
