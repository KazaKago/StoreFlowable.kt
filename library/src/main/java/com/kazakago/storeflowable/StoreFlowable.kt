package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.State
import kotlinx.coroutines.flow.Flow

interface StoreFlowable<KEY, DATA> {

    fun publish(forceRefresh: Boolean = false): FlowableState<DATA>

    @Deprecated("Use publish", ReplaceWith("publish(forceRefresh)"))
    fun asFlow(forceRefresh: Boolean = false): Flow<State<DATA>> = publish()

    suspend fun getData(type: AsDataType = AsDataType.Mix): DATA?

    suspend fun requireData(type: AsDataType = AsDataType.Mix): DATA

    @Deprecated("Use getData() or requireData()", ReplaceWith("requireData(type)"))
    suspend fun get(type: AsDataType = AsDataType.Mix): DATA = requireData(type)

    suspend fun validate()

    suspend fun refresh(clearCacheWhenFetchFails: Boolean = true, continueWhenError: Boolean = true)

    @Deprecated("Use refresh", ReplaceWith("refresh()"))
    suspend fun request() = refresh()

    suspend fun update(newData: DATA?)
}
