package com.kazakago.cachesample.data.repository.cacheflowable

import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal abstract class CacheFlowable<KEY, DATA>(private val key: KEY) {

    protected abstract val flowAccessor: FlowAccessor<KEY>

    protected abstract val dataSelector: DataSelector<KEY, DATA>

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
