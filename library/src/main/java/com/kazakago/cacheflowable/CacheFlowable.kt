package com.kazakago.cacheflowable

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class CacheFlowable<KEY, DATA>(private val key: KEY) {

    protected abstract val flowAccessor: FlowAccessor<KEY>

    protected abstract val dataSelector: DataSelector<KEY, DATA>

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
