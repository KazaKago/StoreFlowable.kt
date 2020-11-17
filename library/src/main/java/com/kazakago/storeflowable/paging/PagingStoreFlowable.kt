package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.FlowAccessor
import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent
import com.kazakago.storeflowable.mapState
import kotlinx.coroutines.flow.*

abstract class PagingStoreFlowable<KEY, DATA>(private val key: KEY) {

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

    suspend fun get(type: AsDataType = AsDataType.Mix): List<DATA> {
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
                    is DataState.Fixed -> if (data != null) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit //do nothing.
                    is DataState.Error -> if (data != null) emit(data) else throw it.exception
                }
            }
            .first()
    }

    suspend fun getOrNull(type: AsDataType = AsDataType.Mix): List<DATA>? {
        return runCatching { get(type) }.getOrNull()
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
