package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.core.State
import kotlinx.coroutines.flow.Flow

interface PagingStoreFlowable<KEY, DATA> {

    fun publish(forceRefresh: Boolean = false): Flow<State<List<DATA>>>

    @Deprecated("Use publish", ReplaceWith("publish(forceRefresh)"))
    fun asFlow(forceRefresh: Boolean = false): Flow<State<List<DATA>>> {
        return publish()
    }

    suspend fun get(type: AsDataType = AsDataType.Mix): List<DATA>

    suspend fun validate()

    suspend fun refresh(clearCacheWhenFetchFails: Boolean = true, continueWhenError: Boolean = true)

    @Deprecated("Use refresh", ReplaceWith("refresh()"))
    suspend fun request() {
        refresh()
    }

    suspend fun requestAdditional(continueWhenError: Boolean = true)

    suspend fun update(newData: List<DATA>?)
}
