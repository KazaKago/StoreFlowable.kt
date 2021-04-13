package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.core.FlowableState

interface PaginatingStoreFlowable<KEY, DATA> {

    fun publish(forceRefresh: Boolean = false): FlowableState<DATA>

    suspend fun get(type: AsDataType = AsDataType.Mix): DATA

    suspend fun validate()

    suspend fun refresh(clearCacheWhenFetchFails: Boolean = true, continueWhenError: Boolean = true)

    suspend fun requestAddition(continueWhenError: Boolean = true)

    suspend fun update(newData: DATA?)
}
