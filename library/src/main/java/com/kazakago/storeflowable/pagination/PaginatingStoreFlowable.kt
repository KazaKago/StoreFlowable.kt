package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.FlowableState

interface PaginatingStoreFlowable<KEY, DATA> : StoreFlowable<KEY, DATA> {

    override fun publish(forceRefresh: Boolean): FlowableState<DATA>

    override suspend fun getData(type: AsDataType): DATA?

    override suspend fun requireData(type: AsDataType): DATA

    override suspend fun validate()

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean)

    suspend fun requestAddition(continueWhenError: Boolean = true)

    override suspend fun update(newData: DATA?)
}
