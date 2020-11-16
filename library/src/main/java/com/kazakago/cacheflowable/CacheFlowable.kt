package com.kazakago.cacheflowable

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.StateContent
import kotlinx.coroutines.flow.*

abstract class CacheFlowable<KEY, DATA>(private val key: KEY) {

    internal abstract val flowAccessor: FlowAccessor<KEY>

    internal abstract val dataSelector: DataSelector<KEY, DATA>

    fun asFlow(forceRefresh: Boolean = false): Flow<State<DATA>> {
        return flowAccessor.getFlow(key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCache = true, fetchAtError = false, fetchAsync = true)
            }
            .map {
                val data = dataSelector.load()
                val stateContent = StateContent.wrap(data)
                it.mapState(stateContent)
            }
    }

    suspend fun asData(type: AsDataType = AsDataType.Mix): DATA? {
        return flowAccessor.getFlow(key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchAtError = false, fetchAsync = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchAtError = false, fetchAsync = false)
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
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchAtError = false, fetchAsync = false)
    }

    suspend fun request() {
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchAtError = true, fetchAsync = false)
    }

    suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }

}
