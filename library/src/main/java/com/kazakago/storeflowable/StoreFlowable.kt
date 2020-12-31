package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import kotlinx.coroutines.flow.Flow

interface StoreFlowable<KEY, DATA> {

    fun publish(forceRefresh: Boolean = false): Flow<State<DATA>>

    @Deprecated("Use publish", ReplaceWith("publish(forceRefresh)"))
    fun asFlow(forceRefresh: Boolean = false): Flow<State<DATA>> {
        return publish()
    }

    suspend fun get(type: AsDataType = AsDataType.Mix): DATA

    suspend fun validate()

    suspend fun refresh(clearCacheWhenFetchFails: Boolean = true, continueWhenError: Boolean = true)

    @Deprecated("Use refresh", ReplaceWith("refresh()"))
    suspend fun request() {
        refresh()
    }

    suspend fun update(newData: DATA?)
}
